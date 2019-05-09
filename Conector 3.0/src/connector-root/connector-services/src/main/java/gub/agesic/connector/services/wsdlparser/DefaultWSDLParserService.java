package gub.agesic.connector.services.wsdlparser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.filemanager.FileManagerService;
import gub.agesic.connector.services.xpathparser.XPathParserService;

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

        final List<Node> operationsNodeList = xPathParserService
                .getXPathResultNodeList(OPERATION_NAME, wsdl);
        final List<RoleOperation> roleOperationsList = getRoleOperationsList(operationsNodeList);
        if (!roleOperationsList.isEmpty()) {
            connector.setRoleOperations(roleOperationsList);
        }

        final Node soapAddressNode = xPathParserService.getXPathResultNode(SERVICE_PORT, wsdl, 1);
        final String url = getURL(soapAddressNode);
        if (url == null) {
            connector.setUrl("");
        } else {
            connector.setUrl(url);
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
            final String schemaLocation = xPathParserService.getNamedItemValue(schema,
                    "schemaLocation");
            final Path xsdPath = fileManagerService
                    .getFilePathInUploadTempFolder(prefixNameConnector + schemaLocation);
            if (hasXSDExtension(xsdPath.toString())) {
                if (!result.contains(xsdPath)) {
                    result.add(xsdPath);
                    getXSDSImports(factory, xsdPath, prefixNameConnector, result);
                }
            } else {
                throw new ConnectorException("Un xsd importado no tiene terminación .xsd");
            }

        }
    }

    private boolean hasXSDExtension(final String filePath) throws ConnectorException {
        return XSD_EXTENSION.equals(fileManagerService.getFileExtension(filePath));
    }

    private boolean checkXSDSExistence(final List<Path> xsdImportsPathList) {
        boolean exists = true;
        for (final Path xsdImportPath : xsdImportsPathList) {
            exists = fileManagerService.existsFile(xsdImportPath);
            if (!exists) {
                return exists;
            }
        }
        return exists;
    }

    private List<RoleOperation> getRoleOperationsList(final List<Node> operationsList) {
        final List<RoleOperation> roleOperations = new ArrayList<>();
        for (final Node operation : operationsList) {
            final String operationName = xPathParserService.getNamedItemValue(operation, "name");
            String wsaAction = "";
            String operationInputName = "";

            if (isDocumentLiteralOperation(operation)) {
                operationInputName = getDocumentLiteralOperation(operation);
            }

            final NodeList childNodes = operation.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                final Node childNode = childNodes.item(j);
                if (childNode.getNodeName().contains("operation")) {
                    wsaAction = xPathParserService.getNamedItemValue(childNode, "soapAction");
                } else if (childNode.getNodeName().contains("input")) {
                    operationInputName = xPathParserService.getNamedItemValue(childNode, "name");
                }
            }

            roleOperations.add(new RoleOperation("", operationInputName, operationName, wsaAction));
        }
        return roleOperations;
    }

    private String getDocumentLiteralOperation(final Node operation) {
        final Node wsdlDefinition = operation.getParentNode().getParentNode();
        final Node message = xPathParserService
                .getXPathResultNodeList("//portType/operation/input[@message]", wsdlDefinition)
                .get(0);
        final String messageCompleteName = message.getAttributes().getNamedItem("message")
                .getTextContent();
        String messageName = messageCompleteName;
        if (messageCompleteName.contains(":")) {
            messageName = messageCompleteName.substring(messageCompleteName.indexOf(":") + 1,
                    messageCompleteName.length());
        }
        final Node part = xPathParserService.getXPathResultNodeList(
                "//message[@name='" + messageName + "']/part", wsdlDefinition).get(0);
        final String elementCompleteName = part.getAttributes().getNamedItem("element")
                .getNodeValue();
        if (elementCompleteName.contains(":")) {
            return elementCompleteName.substring(elementCompleteName.indexOf(":") + 1,
                    elementCompleteName.length());
        }
        return elementCompleteName;
    }

    private boolean isDocumentLiteralOperation(final Node operation) {
        final Node wsdlDefinition = operation.getParentNode().getParentNode();
        final Node binding = xPathParserService
                .getXPathResultNodeList("//binding/binding", wsdlDefinition).get(0);
        final String bindingStyle = xPathParserService.getNamedItemValue(binding, "style");
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
            final Path filePath) throws ConnectorException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Node nodeSource = xPathParserService.getFileDocumentElement(factory, file);

        final Node soapAddressNode = xPathParserService.getXPathResultNode(SERVICE_PORT, nodeSource,
                1);
        soapAddressNode.getAttributes().getNamedItem("location").setNodeValue(location);

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();

            final DOMSource source = new DOMSource(nodeSource.getOwnerDocument());
            final StreamResult result = new StreamResult(new File(filePath.toString()));
            transformer.transform(source, result);
        } catch (final TransformerException exception) {
            final String message = "Error al modificar el wsdl con la url del conector";
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

}
