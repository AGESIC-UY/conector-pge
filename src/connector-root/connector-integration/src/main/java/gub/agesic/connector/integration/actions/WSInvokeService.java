package gub.agesic.connector.integration.actions;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.enums.SoapVersion;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.integration.services.PoolConnectionService;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.dbaccess.DefaultConnectorService;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import uy.gub.agesic.pge.XMLUtils;
import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.client.PGEClient;
import uy.gub.agesic.pge.client.PGEClientBasic;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;
import uy.gub.agesic.pge.opensaml.OpenSamlBootstrap;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    private PoolConnectionService poolConnectionService;

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
            final List<RoleOperation> roleOperations = connectorService
                    .getRoleoperationsOperationFromWSDL(connector.get(),
                            getOperationFromMessage(message), getSoapVersionFromMessage(message));

            if (roleOperations.isEmpty())
                throw new MessageProcessorException("No se pudo determinar la operación asociada a la petición");

            connector.get().setActualRoleOperation(roleOperations.get(0));

            if (roleOperations.size() > 1) {
                for (RoleOperation operation : roleOperations) {
                    String action = operation.getWsaAction();
                    if (!action.isEmpty() && message.getHeaders().containsValue(String.format("\"%s\"", action))) {
                        connector.get().setActualRoleOperation(operation);
                    }
                }
            }

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
                final ConnectorGlobalConfiguration globalConfiguration = connectorService.getGlobalConfigurationByType(connector.get().getType());
                String stsGlobalUrl = globalConfiguration.getStsGlobalUrl();

                //Configuration
                uy.gub.agesic.pge.pojos.Configuration conf = new uy.gub.agesic.pge.pojos.Configuration();
                conf.setAliasKeystore(configuration.getAliasKeystore());
                conf.setDirKeystore(configuration.getDirKeystore());
                conf.setDirKeystoreOrg(configuration.getDirKeystoreOrg());
                conf.setDirKeystoreSsl(configuration.getDirKeystoreSsl());
                conf.setPasswordKeystore(configuration.getPasswordKeystore());
                conf.setPasswordKeystoreOrg(configuration.getPasswordKeystoreOrg());
                conf.setPasswordKeystoreSsl(configuration.getPasswordKeystoreSsl());

                //Connector
                RoleOperation ro = connector.get().getActualRoleOperation();

                uy.gub.agesic.pge.pojos.RoleOperation roleOperation = new uy.gub.agesic.pge.pojos.RoleOperation();
                roleOperation.setOperationFromWSDL(ro.getOperationFromWSDL());
                roleOperation.setOperationInputName(ro.getOperationInputName());
                roleOperation.setRole(ro.getRole());
                roleOperation.setSoapVersion(ro.getSoapVersion());
                roleOperation.setWsaAction(ro.getWsaAction());

                uy.gub.agesic.pge.pojos.Connector conn = new uy.gub.agesic.pge.pojos.Connector();
                conn.setActualRoleOperation(roleOperation);
                conn.setEnableCacheTokens(connector.get().isEnableCacheTokens());
                conn.setEnableSTSLocal(connector.get().isEnableSTSLocal());
                conn.setIssuer(connector.get().getIssuer());
                conn.setStsLocalUrl(connector.get().getStsLocalUrl());
                conn.setUsername(connector.get().getUsername());
                conn.setWsaTo(connector.get().getWsaTo());

                //############
                KeyStore keystore;
                KeyStore truststoreSSL;
                try {
                    keystore = XMLUtils.prepareKeystore(configuration.getDirKeystoreSsl(), configuration.getPasswordKeystoreSsl());
                    truststoreSSL = XMLUtils.prepareKeystore(configuration.getDirKeystore(), configuration.getPasswordKeystore());
                } catch (final KeyStoreException | IOException e) {
                    throw new RequestSecurityTokenException(e, 901);
                }
                final String url;
                if (connector.get().isEnableSTSLocal()) {
                    url = connector.get().getStsLocalUrl();
                } else {
                    url = stsGlobalUrl;
                }

                CloseableHttpClient closeableHttpClient = prepareClient(keystore, configuration, truststoreSSL, url);
                //############

                PGEClient pgeClient = new PGEClientBasic();
                response = pgeClient.requestSecurityToken(conf, conn, policyName, url, closeableHttpClient);
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

    private CloseableHttpClient prepareClient(final KeyStore keystore, final Configuration configuration, final KeyStore keyStoreSSL, final String url) {
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(keyStoreSSL, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(final X509Certificate[] chain,
                                                 final String authType) throws CertificateException {
                            return true;
                        }
                    })
                    .loadKeyMaterial(keystore, configuration.getPasswordKeystoreSsl().toCharArray())
                    .build();
            final HttpClientBuilder builder = HttpClientBuilder.create();
            final SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
                    sslContext.getSocketFactory(), new DefaultHostnameVerifier());

            builder.setSSLSocketFactory(sslConnectionFactory);
            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslConnectionFactory).build();

            builder.setConnectionManager(poolConnectionService.getPoolConnectionManagerByUrl(url, registry));
            builder.setConnectionManagerShared(true);
            return builder.build();
        } catch (final Exception ex) {
            logger.error(String.format("couldn't create httpClient!! %s", ex.getMessage()));
            return null;
        }
    }

    private String getPathFromUrl(final Message<String> message) {
        final String url = getUrlFromMessageHeader(message);
        // Search by url after application context.
        return url.replaceFirst(REGEX_PATH, "$5");
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

    private String getSoapVersionFromMessage(final Message<String> message)
            throws MessageProcessorException {
        String messageSoapVersion;
        if (message.getPayload().contains(DefaultConnectorService.NAMESPACE_SOAP_1_1)) {
            messageSoapVersion = SoapVersion.V1_1.getName();
        } else if (message.getPayload().contains(DefaultConnectorService.NAMESPACE_SOAP_1_2)) {
            messageSoapVersion = SoapVersion.V1_2.getName();
        } else {
            throw new MessageProcessorException("No se pudo determinar la versión de SOAP asociada a la petición.");
        }
        return messageSoapVersion;
    }
}
