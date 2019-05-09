package gub.agesic.connector.integration.actions;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.integration.pgeclient.beans.STSResponse;
import gub.agesic.connector.integration.pgeclient.client.PGEClient;
import gub.agesic.connector.integration.pgeclient.exceptions.RequestSecurityTokenException;
import gub.agesic.connector.integration.pgeclient.opensaml.OpenSamlBootstrap;
import gub.agesic.connector.services.dbaccess.ConnectorService;

/**
 * Created by adriancur on 31/10/17.
 */

@Service
public class WSInvokeService implements MessageProcessor<String, String> {

    public static final String HEADER_CONFIGURATION = "configuration";
    public static final String HEADER_STS_EXECUTION_TIME = "stsExecutionTime";
    private static final String XPATH_OPERATION_NAME = "local-name(/Envelope//Body/*[1])";
    private static final String REGEX_PATH = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\\\?([^#]*))?(#(.*))?";
    private final Logger logger = Logger.getLogger(WSInvokeService.class);

    @Autowired
    private ConnectorService connectorService;

    @Autowired
    @Qualifier(value = "PGEClientCache")
    private PGEClient pgeClient;

    @Autowired
    private WSInvoke wsinvoke;

    @PostConstruct
    public void init() throws Exception {
        OpenSamlBootstrap.bootstrap();
    }

    @Override
    public Message<String> process(final Message<String> message) throws MessageProcessorException {
        Configuration configuration = null;
        logger.debug("CALL INVOKE SERVICE");

        final Optional<Connector> connector;
        final ConnectorTypeHolder connectorTypeHolder;

        try {
            // Search path and port from url
            final String path = getPathFromUrl(message);
            final String port = getPortFromUrl(message);

            connectorTypeHolder = connectorService.getConnectorTypeByPort(port);
            connector = connectorService.getConnectorByPathAndPort(path, connectorTypeHolder);
        } catch (final ConnectorException exception) {
            throw new MessageProcessorException("No se pudo obtener la configuracion del conector",
                    exception);
        }

        if (connector.isPresent()) {
            // Find operation and reference it by a transient field to use
            // later.
            final Optional<RoleOperation> roleOperation = connectorService
                    .getRoleoperationsOperationFromWSDL(connector.get(),
                            getOperationFromMessage(message));
            connector.get().setActualRoleOperation(roleOperation.get());
            final String policyName;

            try {
                policyName = connectorService
                        .getGlobalConfigurationByType(
                                connectorTypeHolder.getConnectorType().getEnvironment())
                        .getPolicyName();
            } catch (final NoSuchElementException e) {
                throw new MessageProcessorException(
                        "No existe ninguna configuración global para el ambiente seleccionado. Es necesaria una para definir el tipo de token a utilizar.");
            }

            // Using local or global configuration
            if (connector.get().isEnableLocalConfiguration()) {
                logger.debug("Using local configuration");
                configuration = connector.get().getLocalConfiguration();
            } else {
                try {
                    logger.debug("Using global configuration");
                    configuration = connectorService.getGlobalConfigurationByType(
                            connectorTypeHolder.getConnectorType().getEnvironment());
                } catch (final NoSuchElementException e) {
                    throw new MessageProcessorException(
                            "No hay configuración disponible para el conector seleccionado.");
                }
            }

            // Authenticate to the PGE
            final STSResponse response;
            try {
                response = pgeClient.requestSecurityToken(configuration, connector.get(),
                        policyName);
            } catch (final RequestSecurityTokenException exception) {
                throw new MessageProcessorException("Error al solicitar un token saml al STS",
                        exception);
            }

            // Invoke service
            final String payload;
            try {
                payload = wsinvoke.processToken(response.getAssertion(), message, connector.get());
            } catch (IOException | SOAPException exception) {
                throw new MessageProcessorException("Error al transformar mensaje soap", exception);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("RETURN INVOKE SERVICE: " + message);
            }

            return MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders())
                    .setHeader("serviceUrl", connector.get().getUrl())
                    .setHeader(HEADER_CONFIGURATION, configuration)
                    .setHeader(HEADER_STS_EXECUTION_TIME, response.getResponseTime()).build();
        } else {
            throw new MessageProcessorException(
                    "No se encontro un conector asociado a ese contexto");
        }

    }

    private String getPathFromUrl(final Message<String> message) {
        final String url = getUrlFromMessageHeader(message);
        // Search by url after application context.
        final String path = url.replaceFirst(REGEX_PATH, "$5");
        return path.substring(StringUtils.ordinalIndexOf(path, "/", 2));
    }

    private String getUrlFromMessageHeader(final Message<String> message) {
        // Getting the path from message
        return (String) message.getHeaders().get(HttpHeaders.REQUEST_URL);
    }

    private String getPortFromUrl(final Message<String> message) throws MessageProcessorException {
        final String url = getUrlFromMessageHeader(message);

        try {
            // Getting the port from url string
            return String.valueOf(new URL(url).getPort());
        } catch (final MalformedURLException e) {
            // Throw business exception
            final String portError = "No se pudo obtener correctamente el puerto de la URL.";
            logger.error(portError);
            throw new MessageProcessorException(portError, e);
        }
    }

    private String getOperationFromMessage(final Message<String> message)
            throws MessageProcessorException {
        Document doc = null;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Getting the content of the message
        final String body = message.getPayload();

        // Get first element name of the soap message body
        final XPathExpression xpathExpression = XPathExpressionFactory
                .createXPathExpression(XPATH_OPERATION_NAME);

        try {
            // Load a String XML
            final InputSource source = new InputSource(new StringReader(body));

            // Parse the XML file as an input source
            doc = factory.newDocumentBuilder().parse(source);

        } catch (final Exception e) {
            logger.error(e);
            throw new MessageProcessorException("Error al procesar mensaje soap", e);
        }

        final Node nodeSource = doc.getDocumentElement();
        return xpathExpression.evaluateAsString(nodeSource);
    }
}
