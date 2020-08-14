package gub.agesic.connector.services.wsdlparser;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.enums.SoapVersion;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.pojo.SoapVersionInfo;
import gub.agesic.connector.services.filemanager.FileManagerService;
import gub.agesic.connector.services.xpathparser.XPathParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultWSDLParserService implements WSDLParserService {

    public static final String OPERATION_NAME = "/definitions//binding/operation";
    public static final String SERVICE_PORT = "/definitions//service/port";
    public static final String XSD_IMPORTS = "//schema/import";
    private static final String XSD_INCLUDE = "//schema/include";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWSDLParserService.class);
    private static final String XSD_EXTENSION = ".xsd";

    private final FileManagerService fileManagerService;
    private final XPathParserService xPathParserService;

    @Autowired
    public DefaultWSDLParserService(final FileManagerService fileManagerService,
                                    final XPathParserService xPathParserService) {
        this.fileManagerService = fileManagerService;
        this.xPathParserService = xPathParserService;
    }

    @Override
    public Connector getWSDLData(final Model model, final String prefixNameConnector,
                                 final Connector connector) throws ConnectorException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Path filePath = fileManagerService.getConnectorWSDL(0, prefixNameConnector);
        final Node wsdl = xPathParserService.getFileDocumentElement(factory, filePath);

        final SoapVersionInfo soapVersion = xPathParserService.soapVersionInfo(wsdl);
        final boolean isMultipleSoapVersion = soapVersion.getVersion().equals(SoapVersion.MULTIPLE);
        connector.setMultipleVersion(isMultipleSoapVersion);

        final List<Node> operationsNodeList = xPathParserService
                .getXPathResultNodeList(OPERATION_NAME, wsdl);
        final List<RoleOperation> roleOperationsList = getRoleOperationsList(operationsNodeList, soapVersion.getPrefix());
        if (!roleOperationsList.isEmpty()) {
            connector.setRoleOperations(roleOperationsList);
        }

        Node soapAddressNode = xPathParserService.getXPathResultNode(SERVICE_PORT, wsdl, 1);
        String url = getURL(soapAddressNode);
        connector.setUrl(url == null ? "" : url);

        if (isMultipleSoapVersion) {
            connector.setUrlV2("");
            final List<Node> soapAddressNodeList = xPathParserService.getXPathResultNodeList(SERVICE_PORT, wsdl);
            for (Node node : soapAddressNodeList) {
                soapAddressNode = node.getChildNodes().item(1);
                if (nodePrefix(soapAddressNode.getNodeName()).equals(soapVersion.getPrefix())) {
                    url = getURL(soapAddressNode);
                    if (url != null) {
                        connector.setUrlV2(url);
                    }
                    break;
                }
            }
        }

        final List<Path> xsdImportsPathList = new ArrayList<Path>();
        getXSDSImports(factory, filePath, prefixNameConnector, xsdImportsPathList);
        if (!checkXSDSExistence(xsdImportsPathList)) {
            final String errorMessage = "ERROR: No se encontraron todos los XSDs importados.";
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        }

        return connector;
    }

    private void getXSDSImports(final DocumentBuilderFactory factory, final Path filePath,
                                final String prefixNameConnector, final List<Path> result) throws ConnectorException {
        final Node nodeSource = xPathParserService.getFileDocumentElement(factory, filePath);

        final List<Node> auxXSDImportsList = xPathParserService.getXPathResultNodeList(XSD_IMPORTS,
                nodeSource);
        final List<Node> auxXSDIncludeList = xPathParserService.getXPathResultNodeList(XSD_INCLUDE,
                nodeSource);

        final List<Node> schemas = new ArrayList<Node>();
        schemas.addAll(auxXSDIncludeList);
        schemas.addAll(auxXSDImportsList);

        for (final Node schema : schemas) {
            final String schemaLocation = xPathParserService.getNamedItemValue(schema, "schemaLocation");

            if (StringUtils.isEmpty(schemaLocation)) {
                continue;
            }

            final Path xsdPath = fileManagerService.getFilePathInUploadTempFolder(prefixNameConnector + schemaLocation);
            if (!result.contains(xsdPath)) {
                result.add(xsdPath);
                getXSDSImports(factory, xsdPath, prefixNameConnector, result);
            }
        }
    }

//    private boolean hasXSDExtension(final String filePath) throws ConnectorException {
//        return XSD_EXTENSION.equals(fileManagerService.getFileExtension(filePath));
//    }

    private boolean checkXSDSExistence(final List<Path> xsdImportsPathList) {
        for (final Path xsdImportPath : xsdImportsPathList) {
            if (!fileManagerService.existsFile(xsdImportPath)) return false;
        }
        return true;
    }

    private List<RoleOperation> getRoleOperationsList(final List<Node> operationsList, final String prefix) throws ConnectorException {
        final List<RoleOperation> roleOperations = new ArrayList<>();
        for (final Node operation : operationsList) {
            final String operationName = xPathParserService.getNamedItemValue(operation, "name");
            String wsaAction = "";
            String operationInputName = "";
            String soapVersion = SoapVersion.V1_1.getName();

            if (isDocumentLiteralOperation(operation)) operationInputName = getDocumentLiteralOperation(operation);

            final NodeList childNodes = operation.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                final Node childNode = childNodes.item(j);
                final String nodeName = childNode.getNodeName();

                if (!prefix.equals(SoapVersion.UNDEFINED.getName()) && prefix.equals(nodePrefix(nodeName)))
                    soapVersion = SoapVersion.V1_2.getName();

                if (nodeName.contains("operation")) {
                    wsaAction = xPathParserService.getNamedItemValue(childNode, "soapAction");
                } else if (operationInputName.isEmpty() && nodeName.contains("input")) {
                    operationInputName = xPathParserService.getNamedItemValue(childNode, "name");
                }
            }

            if (operationInputName.isEmpty() && operationName.isEmpty()) {
                final String errorMessage = "No se pudo determinar correctamente el nombre de las operaciones.";
                LOGGER.error(errorMessage);
                throw new ConnectorException(errorMessage);
            }

            roleOperations.add(new RoleOperation("", operationInputName, operationName, wsaAction, soapVersion));
        }
        return roleOperations;
    }

    private String getDocumentLiteralOperation(final Node operation) {
        final String operationName = xPathParserService.getNamedItemValue(operation, "name");
        final Node wsdlDefinition = operation.getParentNode().getParentNode();

        String filterExpression = "//portType/operation[@name='" + operationName + "']/input[@message]";
        final List<Node> messageList = xPathParserService.getXPathResultNodeList(filterExpression, wsdlDefinition);
        if (messageList.isEmpty()) return "";
        final String messageCompleteName = messageList.get(0).getAttributes().getNamedItem("message").getTextContent();

        final String separator = ":";
        String messageName = messageCompleteName;
        if (messageCompleteName.contains(separator))
            messageName = messageCompleteName.substring(messageCompleteName.indexOf(separator) + 1);

        filterExpression = "//message[@name='" + messageName + "']/part";
        final List<Node> partList = xPathParserService.getXPathResultNodeList(filterExpression, wsdlDefinition);
        if (partList.isEmpty()) return "";
        final String elementCompleteName = partList.get(0).getAttributes().getNamedItem("element").getNodeValue();

        if (elementCompleteName.contains(separator))
            return elementCompleteName.substring(elementCompleteName.indexOf(separator) + 1);
        return elementCompleteName;
    }

    private boolean isDocumentLiteralOperation(final Node operation) {
        final Node wsdlDefinition = operation.getParentNode().getParentNode();
        final Node binding = xPathParserService
                .getXPathResultNodeList("//binding/binding", wsdlDefinition).get(0);
        final String style = xPathParserService.getNamedItemValue(binding, "style");
        final String bindingStyle = style.isEmpty() ? "document" : style;
        final Node operationBody = xPathParserService
                .getXPathResultNodeList("//binding/operation/input/body", wsdlDefinition).get(0);
        final String useStyle = xPathParserService.getNamedItemValue(operationBody, "use");
        return "document".equals(bindingStyle) && "literal".equals(useStyle);
    }

    private String getURL(final Node soapAddressNode) {
        if (soapAddressNode == null) {
            return null;
        } else {
            return soapAddressNode.getAttributes().getNamedItem("location").getNodeValue();
        }
    }

    @Override
    public void modifyLocationAndSave(final MultipartFile file, final String location,
                                      final Path filePath, String connectorPath) throws ConnectorException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Node nodeSource = xPathParserService.getFileDocumentElement(factory, file);

        // Cambio el soap:address location del wsdl
        final List<Node> soapAddressNodeList = xPathParserService.getXPathResultNodeList(SERVICE_PORT, nodeSource);
        for (Node node : soapAddressNodeList) {
            final Node soapAddressNode = node.getChildNodes().item(1);
            soapAddressNode.getAttributes().getNamedItem("location").setNodeValue(location);
        }

        // Cambio la ruta a los ficheros que se importan o incluyen (xsd,xml)
        String separator = "/";
        int pathSeparator = connectorPath.lastIndexOf(separator);
        connectorPath = connectorPath.substring(pathSeparator + 1);
        final String schemaLocationKey = "schemaLocation";

        final List<Node> importsList = xPathParserService.getXPathResultNodeList(XSD_IMPORTS, nodeSource);
        final List<Node> includeList = xPathParserService.getXPathResultNodeList(XSD_INCLUDE, nodeSource);

        final List<Node> schemas = new ArrayList<Node>();
        schemas.addAll(importsList);
        schemas.addAll(includeList);

        for (final Node schema : schemas) {
            String schemaLocation = xPathParserService.getNamedItemValue(schema, schemaLocationKey);
            String schemaLocationPath = "";
            String schemaLocationName = schemaLocation;

            pathSeparator = schemaLocation.lastIndexOf(separator);
            if (pathSeparator > 0) {
                schemaLocationPath = schemaLocation.substring(0, pathSeparator);
                schemaLocationName = schemaLocation.substring(pathSeparator + 1);
            }
            if (!schemaLocation.isEmpty() && !schemaLocationPath.equals(connectorPath)) {
                schema.getAttributes().getNamedItem(schemaLocationKey).setNodeValue(connectorPath + separator + schemaLocationName);
            }
        }

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();

            final DOMSource source = new DOMSource(nodeSource.getOwnerDocument());
            final StreamResult result = new StreamResult(new File(filePath.toString()));
            transformer.transform(source, result);
        } catch (final TransformerException exception) {
            final String message = "Error al modificar el wsdl";
            LOGGER.error(message, exception);
            throw new ConnectorException(message, exception);
        }
    }

    @Override
    public void commentXSDImportTags(final Path wsdlPath) throws ConnectorException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Node wsdl = xPathParserService.getFileDocumentElement(factory, wsdlPath);
        commentSchemaTag(wsdl, "import");
        commentSchemaTag(wsdl, "include");
        try {
            Files.deleteIfExists(wsdlPath);
            Files.write(wsdlPath, nodeToString(wsdl).getBytes());
        } catch (final IOException exception) {
            throw new ConnectorException("Error al guardar wsdl con xml schemas comentados",
                    exception);
        }
    }

    private void commentSchemaTag(final Node wsdl, final String child) throws ConnectorException {
        final List<Node> imports = xPathParserService.getXPathResultNodeList("//schema/" + child,
                wsdl);
        final String prefix = "\nCommented automatically by Conector 3.0\n";
        for (final Node importTag : imports) {
            final Node schema = importTag.getParentNode();
            final Comment comment = schema.getOwnerDocument()
                    .createComment(prefix + nodeToString(importTag));
            schema.appendChild(comment);
            schema.removeChild(importTag);
        }
    }

    private String nodeToString(final Node node) throws ConnectorException {

        final StringWriter sw = new StringWriter();
        try {
            final Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (final TransformerException exception) {
            throw new ConnectorException("Error al pasar a texto un xml", exception);
        }

    }

    private String nodePrefix(String nodeName) {
        return nodeName.contains(":") ? nodeName.substring(0, nodeName.indexOf(":")) : "";
    }

}
