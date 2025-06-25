package gub.agesic.connector.integration.actions;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.enums.SoapVersion;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.dbaccess.DefaultConnectorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.client.PGEClient;
import uy.gub.agesic.pge.client.PGEClientBasic;
import uy.gub.agesic.pge.client.PGEClientCache;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfig;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.exceptions.ConfigurationException;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;
import uy.gub.agesic.pge.opensaml.OpenSamlBootstrap;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by adriancur on 31/10/17.
 */

@Service
@PropertySources({
        @PropertySource("file:${connector.web.configLocation}/pge-client.properties"),
        @PropertySource("file:${connector.web.configLocation}/connector.properties")
})
public class WSInvokeService implements MessageProcessor<String, String> {

    public static final String HEADER_CONFIGURATION = "configuration";
    public static final String HEADER_STS_EXECUTION_TIME = "stsExecutionTime";
    private static final String XPATH_OPERATION_NAME = "local-name(/Envelope//Body/*[1])";
    public static final String REGEX_PATH = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\\\?([^#]*))?(#(.*))?";
    private final Logger logger = Logger.getLogger(WSInvokeService.class);

    @Autowired
    private ConnectorService connectorService;

    @Autowired
    private WSInvoke wsinvoke;

    @Autowired
    private Environment environment;

    private PGEClient pgeClient;

    @PostConstruct
    public void init() throws Exception {
        OpenSamlBootstrap.bootstrap();
        pgeClient = new PGEClientCache(new PGEClientBasic());
    }

    @Override
    public Message<String> process(final Message<String> message) throws MessageProcessorException {
        Configuration configuration;
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
            throw new MessageProcessorException("No se pudo obtener la configuracion del servicio",
                    exception);
        }

        if (connector.isPresent()) {
            // Find operation and reference it by a transient field to use
            // later.
            final List<RoleOperation> roleOperations = connectorService
                    .getRoleOperationsFromWSDL(connector.get(),
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

            // Using local or global configuration
            if (connector.get().isEnableLocalConfiguration()) {
                logger.debug("Using local configuration");
                configuration = connector.get().getLocalConfiguration();
            } else {
                try {
                    logger.debug("Using global configuration");
                    configuration = connectorService.getGlobalConfigurationByType(
                            connectorTypeHolder.getConnectorType().getName());
                } catch (final NoSuchElementException e) {
                    throw new MessageProcessorException(
                            "No hay configuración disponible para el servicio seleccionado.", e);
                }
            }

            String policyName;
            // Using local or global policy name
            if (connector.get().isEnableLocalPolicyName()) {
                logger.debug("Using local policy name");
                policyName = connector.get().getPolicyName();
            } else {
                if (configuration instanceof ConnectorGlobalConfiguration) {
                    policyName = ((ConnectorGlobalConfiguration)configuration).getPolicyName();
                } else {
                    try {
                        ConnectorGlobalConfiguration globalConfiguration = connectorService.getGlobalConfigurationByType(connectorTypeHolder.getConnectorType().getName());
                        policyName = globalConfiguration.getPolicyName();
                    } catch (final NoSuchElementException e) {
                        throw new MessageProcessorException(
                                "No existe ninguna configuración global para el ambiente seleccionado. Es necesaria una para definir el tipo de token a utilizar.");
                    }
                }
            }

            if (policyName.isEmpty()) {
                throw new MessageProcessorException("Tipo de token local no definido.");
            }

            String stsUrl;
            // Using local or global STS URL
            if (connector.get().isEnableSTSLocal()) {
                logger.debug("Using local STS URL");
                stsUrl = connector.get().getStsLocalUrl();
            } else {
                if (configuration instanceof ConnectorGlobalConfiguration) {
                    stsUrl = ((ConnectorGlobalConfiguration)configuration).getStsGlobalUrl();
                } else {
                    try {
                        ConnectorGlobalConfiguration globalConfiguration = connectorService.getGlobalConfigurationByType(connectorTypeHolder.getConnectorType().getName());
                        stsUrl = globalConfiguration.getStsGlobalUrl();
                    } catch (final NoSuchElementException e) {
                        throw new MessageProcessorException(
                                "No existe ninguna configuración global para el ambiente seleccionado. Es necesaria una para definir la URL del STS a utilizar.");
                    }
                }
            }

            if (stsUrl.isEmpty()) {
                throw new MessageProcessorException("STS URL no definida.");
            }

            // Authenticate to the PGE
            final STSResponse response;
            try {
                // PGE Config
                PGEConfig pgeConfig = new PGEConfig();

                // STS Config
                RoleOperation actualRoleOperation = connector.get().getActualRoleOperation();
                PGEConfig.STSConfig stsConfig = loadSTSConfig(connector.get(), actualRoleOperation, policyName, stsUrl);
                pgeConfig.setSTSConfig(stsConfig);

                // Keystore
                PGEConfig.KeyStore keyStore = loadKeyStoreConfig(configuration);
                pgeConfig.setKeyStore(keyStore);

                // SAML Config
                PGEConfig.SAMLConfig samlConfig = loadSAMLConfig();
                pgeConfig.setSAMLConfig(samlConfig);

                // PGE Configuraion
                PGEConfiguration pgeConfiguration = new PGEConfiguration(pgeConfig);
                response = pgeClient.requestSecurityToken(pgeConfiguration);

            } catch (final RequestSecurityTokenException exception) {
                throw new MessageProcessorException("Error al solicitar un token saml al STS",
                        exception);
            } catch (final ConfigurationException exception) {
                throw new MessageProcessorException("Error al cargar la configuracion en la libreria PGE",
                        exception);
            } catch (Exception exception) {
                throw new MessageProcessorException("Error inicializando la libreria PGE", exception);
            }

            // Invoke service
            final String payload;
            try {
                payload = wsinvoke.processToken(response.getAssertion(), message, connector.get());
            } catch (IOException | SOAPException exception) {
                throw new MessageProcessorException("Error al transformar mensaje soap", exception);
            } catch (Exception exception) {
                throw new MessageProcessorException("Error transformando mensaje soap", exception);
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
                    "No se encontró un servicio asociado a ese contexto");
        }

    }

    private PGEConfig.STSConfig loadSTSConfig(Connector connector, RoleOperation roleOperation, String policy, String stsUrl) {
        List<PGEConfig.STSConfig.Property> stsProperties = new ArrayList<>();
        PGEConfig.STSConfig.Property stsProperty;

        // Issuer
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.ISSUER);
        stsProperty.setValue(connector.getIssuer());
        stsProperties.add(stsProperty);
        // Username
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.USERNAME);
        stsProperty.setValue(connector.getUsername());
        stsProperties.add(stsProperty);
        // WSA To
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.WSA_TO);
        stsProperty.setValue(connector.getWsaTo());
        stsProperties.add(stsProperty);
        // SAML version
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.SAML_VERSION);
        stsProperty.setValue(connector.getSamlVersion());
        stsProperties.add(stsProperty);
        // Cache token enabled
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.CACHE_TOKEN_ENABLED);
        stsProperty.setValue(connector.isEnableCacheTokens() ? "1" : "0");
        stsProperties.add(stsProperty);
        // Role
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.ROLE);
        stsProperty.setValue(roleOperation.getRole());
        stsProperties.add(stsProperty);
        // SOAP version
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.SOAP_VERSION);
        stsProperty.setValue(roleOperation.getSoapVersion());
        stsProperties.add(stsProperty);
        // Policy
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.POLICY);
        stsProperty.setValue(policy);
        stsProperties.add(stsProperty);

        PGEConfig.STSConfig stsConfig = new PGEConfig.STSConfig();
        // STS URL
        stsConfig.setUrl(stsUrl);
        stsConfig.setProperty(stsProperties);

        return stsConfig;
    }

    private PGEConfig.KeyStore loadKeyStoreConfig(Configuration configuration) {
        List<PGEConfig.KeyStore.Auth> auths = new ArrayList<>();
        PGEConfig.KeyStore.Auth auth = new PGEConfig.KeyStore.Auth();

        // Alias Keystore Org
        auth.setKey(ConfigProperties.KEY_STORE_ALIAS);
        auth.setValue(configuration.getAliasKeystore());
        auths.add(auth);
        // Alias Keystore SSL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.KEY_STORE_ALIAS_SSL);
        auth.setValue(configuration.getAliasKeystoreSSL());
        auths.add(auth);
        // Keystore Org URL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.KEY_STORE_FILE_URL);
        auth.setValue(configuration.getDirKeystoreOrg());
        auths.add(auth);
        // Keystore Org Pass
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.KEY_STORE_PASS);
        auth.setValue(configuration.getPasswordKeystoreOrg());
        auths.add(auth);
        // SSL Keystore URL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.SSL_KEY_STORE_URL);
        auth.setValue(configuration.getDirKeystoreSsl());
        auths.add(auth);
        // SSL Keystore Pass
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.SSL_KEY_STORE_PASS);
        auth.setValue(configuration.getPasswordKeystoreSsl());
        auths.add(auth);
        // Truststore URL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.TRUST_STORE_URL);
        auth.setValue(configuration.getDirKeystore());
        auths.add(auth);
        // Truststore Pass
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.TRUST_STORE_PASS);
        auth.setValue(configuration.getPasswordKeystore());
        auths.add(auth);

        PGEConfig.KeyStore keyStore = new PGEConfig.KeyStore();
        keyStore.setAuth(auths);

        return keyStore;
    }

    private PGEConfig.SAMLConfig loadSAMLConfig() {
        List<PGEConfig.SAMLConfig.Property> samlProperties = new ArrayList<>();
        PGEConfig.SAMLConfig.Property samlProperty;

        // PGE Client Config
        String property = environment.getProperty("pge.client.saml1_name_id_format");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SAML1_NAME_ID_FORMAT);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.saml1_confirmation_method");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SAML1_CONFIRMATION_METHOD);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.saml1_authentication_method");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SAML1_AUTHENTICATION_METHOD);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.saml2_name_id_format");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SAML2_NAME_ID_FORMAT);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.saml2_confirmation_method");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SAML2_CONFIRMATION_METHOD);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.saml2_authn_context");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SAML2_AUTHN_CONTEXT);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.signature_algorithm");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SIGNATURE_ALGORITHM);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("pge.client.signature_canonicalization_algorithm");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.SIGNATURE_CANONICALIZATION_ALGORITHM);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }

        // Connector Config
        property = environment.getProperty("connector.pool_connection_manager.maxtotalopenconnections");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.MAX_TOTAL_OPEN_CONNECTIONS);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("connector.pool_connection_manager.defaultmaxconnectionsperroute");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.MAX_CONNECTIONS_PER_ROUTE);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }
        property = environment.getProperty("connector.pool_connection_manager.maxconnectionshostnameport");
        if (property != null && !property.isEmpty()) {
            samlProperty = new PGEConfig.SAMLConfig.Property();
            samlProperty.setKey(ConfigProperties.MAX_CONNECTIONS_HOSTNAME_PORT);
            samlProperty.setValue(property);
            samlProperties.add(samlProperty);
        }

        PGEConfig.SAMLConfig samlConfig = new PGEConfig.SAMLConfig();
        samlConfig.setProperty(samlProperties);

        return samlConfig;
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
        Document doc;
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
