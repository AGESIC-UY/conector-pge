package gub.agesic.connector.services.connectorparser;

import static gub.agesic.connector.services.filemanager.DefaultFileManagerService.FILE_SEPARATOR;
import static gub.agesic.connector.services.filemanager.DefaultFileManagerService.XML;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.KEYSTORE_ORG_FILENAME;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.KEYSTORE_SSL_FILENAME;
import static gub.agesic.connector.services.keystoremanager.DefaultKeystoreManagerService.KEYSTORE_TRUSTSTORE_FILENAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorLocalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.entity.UserCredentials;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.filemanager.FileManagerService;
import gub.agesic.connector.services.wsdlparser.WSDLParserService;
import gub.agesic.connector.services.xpathparser.XPathParserService;

@Service
public class DefaultConnectorParserService implements ConnectorParserService {
    private static final String CONNECTOR_NAME = "/connector/name";
    private static final String CONNECTOR_DESCRIPTION = "/connector/description";
    private static final String CONNECTOR_TYPE = "/connector/type";
    private static final String CONNECTOR_PATH = "/connector/path";
    private static final String CONNECTOR_URL = "/connector/url";
    private static final String CONNECTOR_WSDL = "/connector/wsdl";
    private static final String CONNECTOR_KEYSTORE_ORG = "/connector/keystoreOrg";
    private static final String CONNECTOR_KEYSTORE_SSL = "/connector/keystoreSsl";
    private static final String CONNECTOR_KEYSTORE = "/connector/keystore";
    private static final String CONNECTOR_ALIAS_KEYSTORE_ORG = "/connector/aliasKeystore";
    private static final String CONNECTOR_PASS_KEYSTORE_ORG = "/connector/passwordKeystoreOrg";
    private static final String CONNECTOR_PASS_KEYSTORE_SSL = "/connector/passwordKeystoreSSL";
    private static final String CONNECTOR_PASS_KEYSTORE = "/connector/passwordKeystore";
    private static final String CONNECTOR_WSA_TO = "/connector/wsaTo";
    private static final String CONNECTOR_USERNAME = "/connector/username";
    private static final String CONNECTOR_ISSUER = "/connector/issuer";
    private static final String CONNECTOR_ROLE_OPERATIONS = "/connector/role_operation";
    private static final String CONNECTOR_TAG = "/connector/tag";
    private static final String CONNECTOR_ENABLE_CACHE_TOKENS = "/connector/enableCacheTokens";
    private static final String CONNECTOR_ENABLE_LOCAL_CONF = "/connector/enableLocalConf";
    private static final String CONNECTOR_ENABLE_USER_TOKEN = "/connector/enableUserToken";
    private static final String CONNECTOR_ENABLE_SSL = "/connector/enableSSL";
    private static final String CONNECTOR_ENABLE_STS_LOCAL = "/connector/enableSTSLocal";
    private static final String CONNECTOR_USERNAME_TOKEN_NAME = "/connector/userNameTokenName";
    private static final String CONNECTOR_VERSION = "/connector/version";
    private static final String EXPORTED_CONECTOR_FILE_PREFIX = "Conector_";
    private static final String PGE_OLD_VERSION = "2.0";
    private static final String WSDL_ZIP = "WSDL.zip";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultConnectorParserService.class);
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
                connectorDirectory + EXPORTED_CONECTOR_FILE_PREFIX + connector.getName() + XML);
        try {

            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // connector element
            final Document doc = docBuilder.newDocument();
            final Element connectorElement = doc.createElement("connector");
            doc.appendChild(connectorElement);

            addChildElement(doc, connectorElement, "name", connector.getName());
            addChildElement(doc, connectorElement, "description", connector.getDescription());
            addChildElement(doc, connectorElement, "type", connector.getType());
            addChildElement(doc, connectorElement, "path", connector.getPath());
            addChildElement(doc, connectorElement, "url", connector.getUrl());

            // Export wsdl+xsd as a zip file
            final Path path = fileManagerService.getConnectorWSDL(connector.getId(), null);
            addChildElement(doc, connectorElement, "wsdl", encodeFileToBase64(
                    fileManagerService.getConnectorWSDLAndSchemasOnZipFile(path)));
            if (connector.isEnableLocalConfiguration()) {
                final File keystore = new File(connector.getLocalConfiguration().getDirKeystore());
                final File keystoreOrg = new File(
                        connector.getLocalConfiguration().getDirKeystoreOrg());
                final File keystoreSsl = new File(
                        connector.getLocalConfiguration().getDirKeystoreSsl());
                addChildElement(doc, connectorElement, "keystoreOrg",
                        encodeFileToBase64(keystoreOrg));
                addChildElement(doc, connectorElement, "keystore", encodeFileToBase64(keystore));
                addChildElement(doc, connectorElement, "aliasKeystore",
                        connector.getLocalConfiguration().getAliasKeystore());
                addChildElement(doc, connectorElement, "keystoreSsl",
                        encodeFileToBase64(keystoreSsl));
            }
            addChildElement(doc, connectorElement, "wsaTo", connector.getWsaTo());
            addChildElement(doc, connectorElement, "username", connector.getUsername());
            addChildElement(doc, connectorElement, "issuer", connector.getIssuer());

            if (connector.getUserCredentials() != null) {
                addChildElement(doc, connectorElement, "userNameTokenName",
                        connector.getUserCredentials().getUserNameTokenName());
            }

            // roleOperations elements
            final List<RoleOperation> roleOperationsList = connector.getRoleOperations();
            addRoleOperationElements(roleOperationsList, doc, connectorElement);

            addChildElement(doc, connectorElement, "enableCacheTokens",
                    String.valueOf(connector.isEnableCacheTokens()));
            addChildElement(doc, connectorElement, "tag", connector.getTag());
            addChildElement(doc, connectorElement, "enableLocalConf",
                    String.valueOf(connector.isEnableLocalConfiguration()));
            addChildElement(doc, connectorElement, "enableUserToken",
                    String.valueOf(connector.isEnableUserCredentials()));
            addChildElement(doc, connectorElement, "enableSSL",
                    String.valueOf(connector.isEnableSsl()));
            addChildElement(doc, connectorElement, "enableSTSLocal",
                    String.valueOf(connector.isEnableSTSLocal()));
            addChildElement(doc, connectorElement, "version", "3.0");

            // write the content into xml file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(new File(String.valueOf(pathOutput)));

            transformer.transform(source, result);

            LOGGER.debug("File " + pathOutput + " saved!");

            return pathOutput;
        } catch (final ParserConfigurationException pce) {
            final String errorMessage = "ERROR: No se pudo exportar el Conector. Hubo un error al crear un DocumentBuilder.";
            LOGGER.error(errorMessage, pce);
            throw new ConnectorException(errorMessage, pce);
        } catch (final TransformerException tfe) {
            final String errorMessage = "ERROR: No se pudo exportar el Conector. Hubo un error al transformar el Conector a un XML.";
            LOGGER.error(errorMessage, tfe);
            throw new ConnectorException(errorMessage, tfe);
        }
    }

    private void addRoleOperationElements(final List<RoleOperation> roleOperationsList,
            final Document doc, final Element connectorElement) {
        for (final RoleOperation roleOperation : roleOperationsList) {
            final Element roleOperationsElement = doc.createElement("role_operation");
            connectorElement.appendChild(roleOperationsElement);
            addChildElement(doc, roleOperationsElement, "role", roleOperation.getRole());
            addChildElement(doc, roleOperationsElement, "operation",
                    roleOperation.getOperationInputName());
            addChildElement(doc, roleOperationsElement, "operationFromWSDL",
                    roleOperation.getOperationFromWSDL());
            addChildElement(doc, roleOperationsElement, "wsaAction", roleOperation.getWsaAction());
            addChildElement(doc, roleOperationsElement, "soapAction", roleOperation.getWsaAction());
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

        final Node nameNode = xPathParserService.getXPathResultNode(CONNECTOR_NAME, nodeSource);
        final String connectorName = xPathParserService.getStringNodeValue(nameNode);
        connector.setName(connectorName);

        final Node pathNode = xPathParserService.getXPathResultNode(CONNECTOR_PATH, nodeSource);
        final String connectorPath = xPathParserService.getStringNodeValue(pathNode);
        connector.setPath(connectorPath);

        final Node typeNode = xPathParserService.getXPathResultNode(CONNECTOR_TYPE, nodeSource);
        final String connectorType = getConnectorType(typeNode);
        connector.setType(connectorType);

        connectorService.checkConnectorPathAndTypeAvailabilityForType(connectorName, connectorPath,
                connectorType);

        final Node descriptionNode = xPathParserService.getXPathResultNode(CONNECTOR_DESCRIPTION,
                nodeSource);
        connector.setDescription(xPathParserService.getStringNodeValue(descriptionNode));

        final Node urlNode = xPathParserService.getXPathResultNode(CONNECTOR_URL, nodeSource);
        connector.setUrl(xPathParserService.getStringNodeValue(urlNode));

        final Node wsaToNode = xPathParserService.getXPathResultNode(CONNECTOR_WSA_TO, nodeSource);
        connector.setWsaTo(xPathParserService.getStringNodeValue(wsaToNode));

        final Node usernameNode = xPathParserService.getXPathResultNode(CONNECTOR_USERNAME,
                nodeSource);
        connector.setUsername(xPathParserService.getStringNodeValue(usernameNode));

        final Node issuerNode = xPathParserService.getXPathResultNode(CONNECTOR_ISSUER, nodeSource);
        connector.setIssuer(xPathParserService.getStringNodeValue(issuerNode));

        final Node tagNode = xPathParserService.getXPathResultNode(CONNECTOR_TAG, nodeSource);
        connector.setTag(xPathParserService.getStringNodeValue(tagNode));

        final Node enableCacheTokensNode = xPathParserService
                .getXPathResultNode(CONNECTOR_ENABLE_CACHE_TOKENS, nodeSource);
        connector.setEnableCacheTokens(
                xPathParserService.getBooleanNodeValue(enableCacheTokensNode));

        final Node enableSSL = xPathParserService.getXPathResultNode(CONNECTOR_ENABLE_SSL,
                nodeSource);
        connector.setEnableSsl(xPathParserService.getBooleanNodeValue(enableSSL));

        final Node enableSTSLocal = xPathParserService
                .getXPathResultNode(CONNECTOR_ENABLE_STS_LOCAL, nodeSource);
        connector.setEnableSTSLocal(xPathParserService.getBooleanNodeValue(enableSTSLocal));

        // LOCAL CONFIGURATION
        final Node enableLocalConfNode = xPathParserService
                .getXPathResultNode(CONNECTOR_ENABLE_LOCAL_CONF, nodeSource);
        final boolean hasLocalConfig = xPathParserService.getBooleanNodeValue(enableLocalConfNode);
        connector.setEnableLocalConfiguration(hasLocalConfig);
        if (hasLocalConfig) {
            final ConnectorLocalConfiguration localConfiguration = new ConnectorLocalConfiguration();

            final Node aliasKeystoreOrgNode = xPathParserService
                    .getXPathResultNode(CONNECTOR_ALIAS_KEYSTORE_ORG, nodeSource);
            localConfiguration
                    .setAliasKeystore(xPathParserService.getStringNodeValue(aliasKeystoreOrgNode));

            final Node passwordKeystoreOrgNode = xPathParserService
                    .getXPathResultNode(CONNECTOR_PASS_KEYSTORE_ORG, nodeSource);
            localConfiguration.setPasswordKeystoreOrg(
                    xPathParserService.getStringNodeValue(passwordKeystoreOrgNode));

            final Node passwordKeystoreSslNode = xPathParserService
                    .getXPathResultNode(CONNECTOR_PASS_KEYSTORE_SSL, nodeSource);
            localConfiguration.setPasswordKeystoreSsl(
                    xPathParserService.getStringNodeValue(passwordKeystoreSslNode));

            final Node passwordKeystoreNode = xPathParserService
                    .getXPathResultNode(CONNECTOR_PASS_KEYSTORE, nodeSource);
            localConfiguration.setPasswordKeystore(
                    xPathParserService.getStringNodeValue(passwordKeystoreNode));

            connector.setLocalConfiguration(localConfiguration);

            importKeystores(nodeSource, connectorDirectory);
        }
        // END LOCAL CONFIGURATION

        // USER CREDENTIALS
        final Node enableUserTokenNode = xPathParserService
                .getXPathResultNode(CONNECTOR_ENABLE_USER_TOKEN, nodeSource);
        final boolean hasEnabledUserCredentials = xPathParserService
                .getBooleanNodeValue(enableUserTokenNode);
        connector.setEnableUserCredentials(hasEnabledUserCredentials);
        if (hasEnabledUserCredentials) {
            final UserCredentials userCredentials = new UserCredentials();
            final Node usernameTokenNameNode = xPathParserService
                    .getXPathResultNode(CONNECTOR_USERNAME_TOKEN_NAME, nodeSource);
            userCredentials.setUserNameTokenName(
                    xPathParserService.getStringNodeValue(usernameTokenNameNode));
            connector.setUserCredentials(userCredentials);
        }
        // END USER CREDENTIALS

        // ROLE OPERATIONS
        final List<Node> roleOperationsNode = xPathParserService
                .getXPathResultNodeList(CONNECTOR_ROLE_OPERATIONS, nodeSource);
        final List<RoleOperation> roleOperationList = getRoleOperationsList(roleOperationsNode);
        connector.setRoleOperations(roleOperationList);
        // END ROLE OPERATIONS

        // WSDL
        final Node wsdlNode = xPathParserService.getXPathResultNode(CONNECTOR_WSDL, nodeSource);
        final String stringNodeValue = xPathParserService.getStringNodeValue(wsdlNode);
        if (stringNodeValue.length() == 0) {
            final String errorMessage = "ERROR: El Conector a importar no contiene un WSDL";
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        } else {
            decodeBase64ToFile(stringNodeValue, connectorDirectory + FILE_SEPARATOR + WSDL_ZIP);
            fileManagerService.unZip(connectorDirectory + FILE_SEPARATOR + WSDL_ZIP,
                    connectorDirectory.toString(), "");
        }
        // Dado que los conectores de la verión anterior no tienen xsd, hay que
        // comentar los imports
        final Node versionNode = xPathParserService.getXPathResultNode(CONNECTOR_VERSION,
                nodeSource);
        if (PGE_OLD_VERSION.equals(xPathParserService.getStringNodeValue(versionNode))) {
            final Path wsdlPath = fileManagerService.getConnectorWSDL(connector.getId(), null);
            wsdlParserService.commentXSDImportTags(wsdlPath);
        }
        // Se comenta por error---> revisar la comparación del type
        // Valido algunos campos del conector a la hora de importarlo
        // validateFieldsImportedConnector(connector);
        // END WSDL
    }

    private String getConnectorType(final Node typeNode) {
        String type = xPathParserService.getStringNodeValue(typeNode);
        switch (type) {
        case "Prod":
            type = "Produccion";
            break;
        case "Test":
            type = "Testing";
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
            final Node roleNode = xPathParserService.getNodeByName(node, "role");
            String role = "";
            if (roleNode != null) {
                role = roleNode.getTextContent();
            }

            final Node operationInputNode = xPathParserService.getNodeByName(node, "operation");
            String operationInputName = "";
            if (operationInputNode != null) {
                operationInputName = operationInputNode.getTextContent();
            }

            final Node operationNode = xPathParserService.getNodeByName(node, "operationFromWSDL");
            String operationName = "";
            if (operationNode != null) {
                operationName = operationNode.getTextContent();
            }

            final Node wsaActionNode = xPathParserService.getNodeByName(node, "wsaAction");
            String wsaAction = "";
            if (wsaActionNode != null) {
                wsaAction = wsaActionNode.getTextContent();
            }

            roleOperations
                    .add(new RoleOperation(role, operationInputName, operationName, wsaAction));
        }
        return roleOperations;
    }

    public void decodeBase64ToFile(final String base64, final String outputPath)
            throws ConnectorException {
        byte[] data = null;
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

    // private void validateFieldsImportedConnector(final Connector connector)
    // throws ConnectorException
    // {
    // if (connector.getType() == null || connector.getType().equals(""))
    // {
    // final String errorMessage = "ERROR: el tipo del conector se encuentra
    // vacio.";
    // LOGGER.error(errorMessage);
    // throw new ConnectorException(errorMessage);
    // }
    // else if (!connector.getType().equals(ConnectorType.PRODUCCION) &&
    // !connector.getType().equals(ConnectorType.TEST))
    // {
    // final String errorMessage = "ERROR: el tipo del conector no se encuentra
    // permitido.";
    // LOGGER.error(errorMessage);
    // throw new ConnectorException(errorMessage);
    // }
    //
    // if (connector.getUrl() == null || connector.getUrl().equals(""))
    // {
    // final String errorMessage = "ERROR: la url del conector se encuentra
    // vacia.";
    // LOGGER.error(errorMessage);
    // throw new ConnectorException(errorMessage);
    // }
    //
    // if (connector.getPath() == null || connector.getPath().equals(""))
    // {
    // final String errorMessage = "ERROR: el path del conector se encuentra
    // vacio.";
    // LOGGER.error(errorMessage);
    // throw new ConnectorException(errorMessage);
    // }
    // }

}
