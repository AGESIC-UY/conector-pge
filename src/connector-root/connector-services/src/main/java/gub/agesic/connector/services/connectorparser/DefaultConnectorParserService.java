package gub.agesic.connector.services.connectorparser;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorLocalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.entity.UserCredentials;
import gub.agesic.connector.dataaccess.enums.EnvironmentType;
import gub.agesic.connector.enums.SamlVersion;
import gub.agesic.connector.enums.SoapVersion;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.filemanager.FileManagerService;
import gub.agesic.connector.services.wsdlparser.WSDLParserService;
import gub.agesic.connector.services.xpathparser.XPathParserService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static gub.agesic.connector.services.filemanager.DefaultFileManagerService.FILE_SEPARATOR;
import static gub.agesic.connector.services.filemanager.DefaultFileManagerService.XML;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.*;

@Service
public class DefaultConnectorParserService implements ConnectorParserService {
    private static final String ROOT_PATH = "/connector/";
    private static final String CONNECTOR_ELEMENT_NAME = "connector";

    private static final String CONNECTOR_NAME = "name";
    private static final String CONNECTOR_DESCRIPTION = "description";
    private static final String CONNECTOR_TYPE = "type";
    private static final String CONNECTOR_SAML_VERSION = "samlVersion";
    private static final String CONNECTOR_PATH = "path";
    private static final String CONNECTOR_URL = "url";
    private static final String CONNECTOR_URL_V2 = "urlV2";
    private static final String CONNECTOR_WSDL = "wsdl";
    private static final String CONNECTOR_KEYSTORE_ORG = "keystoreOrg";
    private static final String CONNECTOR_KEYSTORE_SSL = "keystoreSsl";
    private static final String CONNECTOR_KEYSTORE = "keystore";
    private static final String CONNECTOR_ALIAS_KEYSTORE_ORG = "aliasKeystore";
    private static final String CONNECTOR_ALIAS_KEYSTORE_SSL = "aliasKeystoreSSL";
    private static final String CONNECTOR_PASS_KEYSTORE_ORG = "passwordKeystoreOrg";
    private static final String CONNECTOR_PASS_KEYSTORE_SSL = "passwordKeystoreSSL";
    private static final String CONNECTOR_PASS_KEYSTORE = "passwordKeystore";
    private static final String CONNECTOR_WSA_TO = "wsaTo";
    private static final String CONNECTOR_USERNAME = "username";
    private static final String CONNECTOR_ISSUER = "issuer";
    private static final String CONNECTOR_ROLE_OPERATIONS = "role_operation";

    private static final String CONNECTOR_ROLE = "role";
    private static final String CONNECTOR_OPERATION = "operation";
    private static final String CONNECTOR_OPERATION_FROM_WSDL = "operationFromWSDL";
    private static final String CONNECTOR_WSA_ACTION = "wsaAction";
    private static final String CONNECTOR_SOAP_ACTION = "soapAction";
    private static final String CONNECTOR_SOAP_VERSION = "soapVersion";

    private static final String CONNECTOR_TAG = "tag";
    private static final String CONNECTOR_ENABLE_CACHE_TOKENS = "enableCacheTokens";
    private static final String CONNECTOR_ENABLE_LOCAL_CONF = "enableLocalConf";
    private static final String CONNECTOR_ENABLE_USER_TOKEN = "enableUserToken";
    private static final String CONNECTOR_ENABLE_SSL = "enableSSL";
    private static final String CONNECTOR_ENABLE_STS_LOCAL = "enableSTSLocal";
    private static final String CONNECTOR_ENABLE_LOCAL_POLICY_NAME = "enableLocalPolicyName";
    private static final String CONNECTOR_POLICY_NAME = "policyName";
    private static final String CONNECTOR_ENABLE_LOCAL_SERVICE_TIMEOUT = "enableLocalServiceTimeOut";
    private static final String CONNECTOR_LOCAL_SERVICE_TIMEOUT = "localServiceTimeOut";
    private static final String CONNECTOR_ENABLE_LOCAL_EXPIRATION_NOTIFICATION = "enableLocalExpirationNotification";
    private static final String CONNECTOR_LOCAL_EXPIRATION_NOTICE_DAYS = "localExpirationNoticeDays";
    private static final String CONNECTOR_USERNAME_TOKEN_NAME = "userNameTokenName";
    private static final String CONNECTOR_MULTIPLE_VERSION = "multipleVersion";
    private static final String CONNECTOR_VERSION = "version";

    private static final String EXPORTED_CONNECTOR_FILE_PREFIX = "Servicio_";
    private static final String PGE_OLD_VERSION = "2.0";
    private static final String PGE_VERSION = "4.0";
    private static final String WSDL_ZIP = "WSDL.zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectorParserService.class);

    private final FileManagerService fileManagerService;
    private final XPathParserService xPathParserService;
    private final ConnectorService connectorService;
    private final WSDLParserService wsdlParserService;

    @Autowired
    public DefaultConnectorParserService(final FileManagerService fileManagerService,
                                         final XPathParserService xPathParserService, final ConnectorService connectorService,
                                         final WSDLParserService wsdlParserService) {
        this.fileManagerService = fileManagerService;
        this.xPathParserService = xPathParserService;
        this.connectorService = connectorService;
        this.wsdlParserService = wsdlParserService;
    }

    @Override
    public Path exportConnectorData(final Connector connector) throws ConnectorException {

        final String connectorDirectory = fileManagerService
                .getConnectorDirectory(connector.getId().toString());
        final Path pathOutput = Paths.get(
                connectorDirectory + EXPORTED_CONNECTOR_FILE_PREFIX + connector.getName() + XML);
        try {

            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // connector element
            final Document doc = docBuilder.newDocument();
            final Element connectorElement = doc.createElement(CONNECTOR_ELEMENT_NAME);
            doc.appendChild(connectorElement);

            addChildElement(doc, connectorElement, CONNECTOR_NAME, connector.getName());
            addChildElement(doc, connectorElement, CONNECTOR_DESCRIPTION, connector.getDescription());
            addChildElement(doc, connectorElement, CONNECTOR_TYPE, connector.getType());
            addChildElement(doc, connectorElement, CONNECTOR_SAML_VERSION, connector.getSamlVersion());
            addChildElement(doc, connectorElement, CONNECTOR_PATH, connector.getPath());
            addChildElement(doc, connectorElement, CONNECTOR_URL, connector.getUrl());

            if (connector.isMultipleVersion()) {
                addChildElement(doc, connectorElement, CONNECTOR_URL_V2, connector.getUrlV2());
            }

            // Export wsdl+xsd as a zip file
            final Path path = fileManagerService.getConnectorWSDL(connector.getId(), null);
            addChildElement(doc, connectorElement, CONNECTOR_WSDL, encodeFileToBase64(
                    fileManagerService.getConnectorWSDLAndSchemasOnZipFile(path)));
            if (connector.isEnableLocalConfiguration()) {
                final File keystore = new File(connector.getLocalConfiguration().getDirKeystore());
                final File keystoreOrg = new File(
                        connector.getLocalConfiguration().getDirKeystoreOrg());
                final File keystoreSsl = new File(
                        connector.getLocalConfiguration().getDirKeystoreSsl());
                addChildElement(doc, connectorElement, CONNECTOR_KEYSTORE_ORG,
                        encodeFileToBase64(keystoreOrg));
                addChildElement(doc, connectorElement, CONNECTOR_KEYSTORE, encodeFileToBase64(keystore));
                addChildElement(doc, connectorElement, CONNECTOR_ALIAS_KEYSTORE_ORG,
                        connector.getLocalConfiguration().getAliasKeystore());
                addChildElement(doc, connectorElement, CONNECTOR_ALIAS_KEYSTORE_SSL,
                        connector.getLocalConfiguration().getAliasKeystoreSSL());
                addChildElement(doc, connectorElement, CONNECTOR_KEYSTORE_SSL,
                        encodeFileToBase64(keystoreSsl));
            }
            addChildElement(doc, connectorElement, CONNECTOR_WSA_TO, connector.getWsaTo());
            addChildElement(doc, connectorElement, CONNECTOR_USERNAME, connector.getUsername());
            addChildElement(doc, connectorElement, CONNECTOR_ISSUER, connector.getIssuer());

            if (connector.getUserCredentials() != null) {
                addChildElement(doc, connectorElement, CONNECTOR_USERNAME_TOKEN_NAME,
                        connector.getUserCredentials().getUserNameTokenName());
            }

            // roleOperations elements
            final List<RoleOperation> roleOperationsList = connector.getRoleOperations();
            addRoleOperationElements(roleOperationsList, doc, connectorElement);

            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_CACHE_TOKENS,
                    String.valueOf(connector.isEnableCacheTokens()));
            addChildElement(doc, connectorElement, CONNECTOR_TAG, connector.getTag());
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_LOCAL_CONF,
                    String.valueOf(connector.isEnableLocalConfiguration()));
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_USER_TOKEN,
                    String.valueOf(connector.isEnableUserCredentials()));
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_SSL,
                    String.valueOf(connector.isEnableSsl()));
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_STS_LOCAL,
                    String.valueOf(connector.isEnableSTSLocal()));

            // Policy Name
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_LOCAL_POLICY_NAME,
                    String.valueOf(connector.isEnableLocalPolicyName()));
            if (connector.isEnableLocalPolicyName()) {
                addChildElement(doc, connectorElement, CONNECTOR_POLICY_NAME,
                        String.valueOf(connector.getPolicyName()));
            }

            // Timeout
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_LOCAL_SERVICE_TIMEOUT,
                    String.valueOf(connector.isEnableLocalServiceTimeOut()));
            if (connector.isEnableLocalServiceTimeOut()) {
                addChildElement(doc, connectorElement, CONNECTOR_LOCAL_SERVICE_TIMEOUT,
                        String.valueOf(connector.getLocalServiceTimeOut()));
            }

            // Expiration Notification
            addChildElement(doc, connectorElement, CONNECTOR_ENABLE_LOCAL_EXPIRATION_NOTIFICATION,
                    String.valueOf(connector.isEnableLocalExpirationNotification()));
            if (connector.isEnableLocalExpirationNotification()) {
                addChildElement(doc, connectorElement, CONNECTOR_LOCAL_EXPIRATION_NOTICE_DAYS,
                        String.valueOf(connector.getLocalExpirationNoticeDays()));
            }

            addChildElement(doc, connectorElement, CONNECTOR_MULTIPLE_VERSION,
                    String.valueOf(connector.isMultipleVersion()));

            addChildElement(doc, connectorElement, CONNECTOR_VERSION, PGE_VERSION);

            // write the content into xml file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(new File(String.valueOf(pathOutput)));

            transformer.transform(source, result);

            LOGGER.debug("File " + pathOutput + " saved!");

            return pathOutput;
        } catch (final ParserConfigurationException pce) {
            final String errorMessage = "ERROR: No se pudo exportar el Servicio. Hubo un error al crear un DocumentBuilder.";
            LOGGER.error(errorMessage, pce);
            throw new ConnectorException(errorMessage, pce);
        } catch (final TransformerException tfe) {
            final String errorMessage = "ERROR: No se pudo exportar el Servicio. Hubo un error al transformar el Servicio a un XML.";
            LOGGER.error(errorMessage, tfe);
            throw new ConnectorException(errorMessage, tfe);
        }
    }

    private void addRoleOperationElements(final List<RoleOperation> roleOperationsList,
                                          final Document doc, final Element connectorElement) {
        for (final RoleOperation roleOperation : roleOperationsList) {
            final Element roleOperationsElement = doc.createElement(CONNECTOR_ROLE_OPERATIONS);
            connectorElement.appendChild(roleOperationsElement);
            addChildElement(doc, roleOperationsElement, CONNECTOR_ROLE, roleOperation.getRole());
            addChildElement(doc, roleOperationsElement, CONNECTOR_OPERATION,
                    roleOperation.getOperationInputName());
            addChildElement(doc, roleOperationsElement, CONNECTOR_OPERATION_FROM_WSDL,
                    roleOperation.getOperationFromWSDL());
            addChildElement(doc, roleOperationsElement, CONNECTOR_WSA_ACTION, roleOperation.getWsaAction());
            addChildElement(doc, roleOperationsElement, CONNECTOR_SOAP_ACTION, roleOperation.getWsaAction());
            addChildElement(doc, roleOperationsElement, CONNECTOR_SOAP_VERSION, roleOperation.getSoapVersion());
        }
    }

    private void addChildElement(final Document doc, final Element parentElement,
                                 final String elementKey, final String elementValue) {
        final Element childElement = doc.createElement(elementKey);
        childElement.appendChild(doc.createTextNode(elementValue));
        parentElement.appendChild(childElement);
    }

    @Override
    public Connector importConnectorData(final Model model, final String prefixNameConnector,
                                         final Connector connector) throws ConnectorException {

        final Path filePath = fileManagerService.getConnectorXML(prefixNameConnector);
        final File file = new File(filePath.toString());
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Document doc;
        final Node nodeSource;
        try (InputStream source = new FileInputStream(file)) {
            doc = factory.newDocumentBuilder().parse(source);
            nodeSource = doc.getDocumentElement();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            final String errorMessage = "ERROR: No se pudo parsear el XML.";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

        populateConnector(connector, nodeSource);

        return connector;
    }

    private void populateConnector(final Connector connector, final Node nodeSource)
            throws ConnectorException {
        final Path connectorDirectory = fileManagerService
                .createConnectorDirectory(connector.getId().toString());

        final Node nameNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_NAME, nodeSource);
        final String connectorName = xPathParserService.getStringNodeValue(nameNode);
        connector.setName(connectorName);

        final Node pathNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_PATH, nodeSource);
        final String connectorPath = xPathParserService.getStringNodeValue(pathNode);
        connector.setPath(connectorPath);

        final Node typeNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_TYPE, nodeSource);
        final String connectorType = getConnectorType(typeNode);
        connector.setType(connectorType);

        connectorService.checkConnectorPathAndTypeAvailabilityForType(connectorName, connectorPath, connectorType);

        final Node samlVersionNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_SAML_VERSION, nodeSource);
        String samlVersion = samlVersionNode != null ? xPathParserService.getStringNodeValue(samlVersionNode) : SamlVersion.V1_1.getName();
        connector.setSamlVersion(samlVersion);

        final Node descriptionNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_DESCRIPTION,
                nodeSource);
        connector.setDescription(xPathParserService.getStringNodeValue(descriptionNode));

        final Node urlNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_URL, nodeSource);
        connector.setUrl(xPathParserService.getStringNodeValue(urlNode));

        final Node wsaToNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_WSA_TO, nodeSource);
        connector.setWsaTo(xPathParserService.getStringNodeValue(wsaToNode));

        final Node tagNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_TAG, nodeSource);
        connector.setTag(xPathParserService.getStringNodeValue(tagNode));

        final Node urlV2Node = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_URL_V2, nodeSource);
        connector.setUrlV2(xPathParserService.getStringNodeValue(urlV2Node));

        final Node usernameNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_USERNAME,
                nodeSource);
        connector.setUsername(xPathParserService.getStringNodeValue(usernameNode));

        final Node issuerNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_ISSUER, nodeSource);
        connector.setIssuer(xPathParserService.getStringNodeValue(issuerNode));

        final Node enableCacheTokensNode = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_CACHE_TOKENS, nodeSource);
        connector.setEnableCacheTokens(
                xPathParserService.getBooleanNodeValue(enableCacheTokensNode));

        final Node enableSSL = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_SSL,
                nodeSource);
        connector.setEnableSsl(xPathParserService.getBooleanNodeValue(enableSSL));

        final Node enableSTSLocal = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_STS_LOCAL, nodeSource);
        connector.setEnableSTSLocal(xPathParserService.getBooleanNodeValue(enableSTSLocal));

        final Node enableLocalPolicyName = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_LOCAL_POLICY_NAME, nodeSource);
        boolean isLocalPolicyNameEnabled = xPathParserService.getBooleanNodeValue(enableLocalPolicyName);
        connector.setEnableLocalPolicyName(isLocalPolicyNameEnabled);

        if (isLocalPolicyNameEnabled) {
            final Node policyNameNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_POLICY_NAME, nodeSource);
            connector.setPolicyName(xPathParserService.getStringNodeValue(policyNameNode));
        }

        final Node enableLocalServiceTimeOut = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_LOCAL_SERVICE_TIMEOUT, nodeSource);
        boolean isLocalServiceTimeOutEnabled = xPathParserService.getBooleanNodeValue(enableLocalServiceTimeOut);
        connector.setEnableLocalServiceTimeOut(isLocalServiceTimeOutEnabled);

        if (isLocalServiceTimeOutEnabled) {
            final Node localServiceTimeOutNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_LOCAL_SERVICE_TIMEOUT, nodeSource);
            connector.setLocalServiceTimeOut(xPathParserService.getIntegerNodeValue(localServiceTimeOutNode));
        }

        final Node enableLocalExpirationNotification = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_LOCAL_EXPIRATION_NOTIFICATION, nodeSource);
        boolean isLocalExpirationNotificationEnabled = xPathParserService.getBooleanNodeValue(enableLocalExpirationNotification);
        connector.setEnableLocalExpirationNotification(isLocalExpirationNotificationEnabled);

        if (isLocalExpirationNotificationEnabled) {
            final Node localExpirationNoticeDaysNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_LOCAL_EXPIRATION_NOTICE_DAYS, nodeSource);
            connector.setLocalExpirationNoticeDays(xPathParserService.getIntegerNodeValue(localExpirationNoticeDaysNode));
        }

        final Node multipleVersion = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_MULTIPLE_VERSION, nodeSource);
        connector.setMultipleVersion(xPathParserService.getBooleanNodeValue(multipleVersion));

        // LOCAL CONFIGURATION
        final Node enableLocalConfNode = xPathParserService
                .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_LOCAL_CONF, nodeSource);
        final boolean hasLocalConfig = xPathParserService.getBooleanNodeValue(enableLocalConfNode);
        connector.setEnableLocalConfiguration(hasLocalConfig);
        if (hasLocalConfig) {
            final ConnectorLocalConfiguration localConfiguration = new ConnectorLocalConfiguration();

            final Node aliasKeystoreOrgNode = xPathParserService
                    .getXPathResultNode(ROOT_PATH + CONNECTOR_ALIAS_KEYSTORE_ORG, nodeSource);
            localConfiguration
                    .setAliasKeystore(xPathParserService.getStringNodeValue(aliasKeystoreOrgNode));

            final Node aliasKeystoreSSLNode = xPathParserService
                    .getXPathResultNode(ROOT_PATH + CONNECTOR_ALIAS_KEYSTORE_SSL, nodeSource);
            localConfiguration
                    .setAliasKeystore(xPathParserService.getStringNodeValue(aliasKeystoreSSLNode));

            final Node passwordKeystoreOrgNode = xPathParserService
                    .getXPathResultNode(ROOT_PATH + CONNECTOR_PASS_KEYSTORE_ORG, nodeSource);
            localConfiguration.setPasswordKeystoreOrg(
                    xPathParserService.getStringNodeValue(passwordKeystoreOrgNode));

            final Node passwordKeystoreSslNode = xPathParserService
                    .getXPathResultNode(ROOT_PATH + CONNECTOR_PASS_KEYSTORE_SSL, nodeSource);
            localConfiguration.setPasswordKeystoreSsl(
                    xPathParserService.getStringNodeValue(passwordKeystoreSslNode));

            final Node passwordKeystoreNode = xPathParserService
                    .getXPathResultNode(ROOT_PATH + CONNECTOR_PASS_KEYSTORE, nodeSource);
            localConfiguration.setPasswordKeystore(
                    xPathParserService.getStringNodeValue(passwordKeystoreNode));

            connector.setLocalConfiguration(localConfiguration);

            importKeystores(nodeSource, connectorDirectory);
            // END LOCAL CONFIGURATION

            // USER CREDENTIALS
            final Node enableUserTokenNode = xPathParserService
                    .getXPathResultNode(ROOT_PATH + CONNECTOR_ENABLE_USER_TOKEN, nodeSource);
            final boolean hasEnabledUserCredentials = xPathParserService
                    .getBooleanNodeValue(enableUserTokenNode);
            connector.setEnableUserCredentials(hasEnabledUserCredentials);
            if (hasEnabledUserCredentials) {
                final UserCredentials userCredentials = new UserCredentials();
                final Node usernameTokenNameNode = xPathParserService
                        .getXPathResultNode(ROOT_PATH + CONNECTOR_USERNAME_TOKEN_NAME, nodeSource);
                userCredentials.setUserNameTokenName(
                        xPathParserService.getStringNodeValue(usernameTokenNameNode));
                connector.setUserCredentials(userCredentials);
            }
            // END USER CREDENTIALS
        }

        // ROLE OPERATIONS
        final List<Node> roleOperationsNode = xPathParserService
                .getXPathResultNodeList(ROOT_PATH + CONNECTOR_ROLE_OPERATIONS, nodeSource);
        final List<RoleOperation> roleOperationList = getRoleOperationsList(roleOperationsNode);
        connector.setRoleOperations(roleOperationList);
        // END ROLE OPERATIONS

        // WSDL
        final Node wsdlNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_WSDL, nodeSource);
        final String stringNodeValue = xPathParserService.getStringNodeValue(wsdlNode);
        if (stringNodeValue.length() == 0) {
            final String errorMessage = "ERROR: El Servicio a importar no contiene un WSDL";
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        } else {
            decodeBase64ToFile(stringNodeValue, connectorDirectory + FILE_SEPARATOR + WSDL_ZIP);
            fileManagerService.unZip(connectorDirectory + FILE_SEPARATOR + WSDL_ZIP,
                    connectorDirectory.toString(), "");
        }
        // Dado que los servicios de la versión anterior no tienen xsd, hay que
        // comentar los imports
        final Node versionNode = xPathParserService.getXPathResultNode(ROOT_PATH + CONNECTOR_VERSION,
                nodeSource);
        if (PGE_OLD_VERSION.equals(xPathParserService.getStringNodeValue(versionNode))) {
            final Path wsdlPath = fileManagerService.getConnectorWSDL(connector.getId(), null);
            wsdlParserService.commentXSDImportTags(wsdlPath);
        }
        // END WSDL
    }

    private String getConnectorType(final Node typeNode) {
        String type = xPathParserService.getStringNodeValue(typeNode);
        switch (type) {
            case "Prod":
                type = EnvironmentType.PRODUCTION.getName();
                break;
            case "Test":
                type = EnvironmentType.TESTING.getName();
                break;
            default:
                break;
        }
        return type;
    }

    private void importKeystores(final Node nodeSource, final Path connectorDirectory)
            throws ConnectorException {

        final Node versionNode = xPathParserService.getXPathResultNode(CONNECTOR_VERSION,
                nodeSource);
        if (PGE_OLD_VERSION.equals(xPathParserService.getStringNodeValue(versionNode))) {
            /*
             * La versión 2.x del conector tiene el truststore y el keystore ssl
             * cambiados en el archivo de exportación, por lo que hay que leerlo
             * cruzado.
             */
            importSingleKeystore(nodeSource, connectorDirectory, CONNECTOR_KEYSTORE_SSL,
                    KEYSTORE_TRUSTSTORE_FILENAME);
            importSingleKeystore(nodeSource, connectorDirectory, CONNECTOR_KEYSTORE,
                    KEYSTORE_SSL_FILENAME);
        } else {
            importSingleKeystore(nodeSource, connectorDirectory, CONNECTOR_KEYSTORE_SSL,
                    KEYSTORE_SSL_FILENAME);
            importSingleKeystore(nodeSource, connectorDirectory, CONNECTOR_KEYSTORE,
                    KEYSTORE_TRUSTSTORE_FILENAME);
        }
        importSingleKeystore(nodeSource, connectorDirectory, CONNECTOR_KEYSTORE_ORG,
                KEYSTORE_ORG_FILENAME);
    }

    private void importSingleKeystore(final Node nodeSource, final Path connectorDirectory,
                                      final String filterExpression, final String keystoreName) throws ConnectorException {
        final Node keystoreNode = xPathParserService.getXPathResultNode(filterExpression,
                nodeSource);
        try {
            decodeBase64ToFile(xPathParserService.getStringNodeValue(keystoreNode),
                    connectorDirectory + FILE_SEPARATOR + keystoreName);
        } catch (final Exception e) {
            final String errorMessage = "ERROR: No se pudo decompilar y almacenar el keystore";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    private List<RoleOperation> getRoleOperationsList(final List<Node> operationsList) {
        final List<RoleOperation> roleOperations = new ArrayList<>();
        for (final Node node : operationsList) {
            final Node roleNode = xPathParserService.getNodeByName(node, CONNECTOR_ROLE);
            String role = roleNode != null ? roleNode.getTextContent() : "";

            final Node operationInputNode = xPathParserService.getNodeByName(node, CONNECTOR_OPERATION);
            String operationInputName = operationInputNode != null ? operationInputNode.getTextContent() : "";

            final Node operationNode = xPathParserService.getNodeByName(node, CONNECTOR_OPERATION_FROM_WSDL);
            String operationName = operationNode != null ? operationNode.getTextContent() : "";

            final Node wsaActionNode = xPathParserService.getNodeByName(node, CONNECTOR_WSA_ACTION);
            String wsaAction = wsaActionNode != null ? wsaActionNode.getTextContent() : "";

            final Node soapVersionNode = xPathParserService.getNodeByName(node, CONNECTOR_SOAP_VERSION);
            String soapVersion = soapVersionNode != null ? soapVersionNode.getTextContent() : SoapVersion.V1_1.getName();

            roleOperations.add(new RoleOperation(role, operationInputName, operationName, wsaAction, soapVersion));
        }
        return roleOperations;
    }

    public void decodeBase64ToFile(final String base64, final String outputPath)
            throws ConnectorException {
        byte[] data;
        try {
            data = Base64.getDecoder().decode(base64.getBytes());
        } catch (final IllegalArgumentException e) {
            final String errorMessage = "ERROR: No se pudo decodificar archivo en base64";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

        final File file = new File(outputPath);
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (final IOException e) {
            final String errorMessage = "ERROR: No se pudo almacenar el Keystore o wsdl en "
                    + outputPath;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    public String encodeFileToBase64(final File file) throws ConnectorException {
        try {
            final byte[] data = FileUtils.readFileToByteArray(file);
            return Base64.getEncoder().encodeToString(data);
        } catch (final FileNotFoundException e) {
            final String errorMessage = "ERROR: No se encontró el archivo del Keystore \""
                    + file.getName() + "\" en la ruta " + file.getAbsolutePath();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final IOException e) {
            final String errorMessage = "ERROR: No se pudo codificar en Base64 al Keystore \""
                    + file.getName() + "\" en la ruta " + file.getAbsolutePath();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

}
