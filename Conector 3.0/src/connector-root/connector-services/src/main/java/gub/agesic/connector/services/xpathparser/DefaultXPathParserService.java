package gub.agesic.connector.services.xpathparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gub.agesic.connector.exceptions.ConnectorException;

@Service
public class DefaultXPathParserService implements XPathParserService {

    public static final String HTTP_SCHEMAS_WSDL = "http://schemas.xmlsoap.org/wsdl";

    private static final Logger LOGGER = LoggerFactory.getLogger(XPathParserService.class);

    private static final String ERROR_INTERNO_AL_CERRAR_STREAM = "ERROR: Hubo un error interno al cerrar el stream.";

    @Override
    public boolean getBooleanNodeValue(final Node node) {
        if (node == null) {
            return false;
        } else {
            return "true".equals(node.getNodeValue());
        }
    }

    @Override
    public String getStringNodeValue(final Node node) {
        if (node == null) {
            return "";
        } else {
            return node.getNodeValue();
        }
    }

    @Override
    public String getNamedItemValue(final Node node, final String namedItem) {
        final Node namedItemNode = node.getAttributes().getNamedItem(namedItem);
        if (namedItemNode == null) {
            return "";
        } else {
            return namedItemNode.getNodeValue();
        }
    }

    @Override
    public Node getNodeByName(final Node node, final String name) {
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);
            if (name.equals(childNode.getNodeName())) {
                return childNode;
            }
        }
        return null;
    }

    @Override
    public Node getXPathResultNode(final String filterExpression, final Node nodeSource) {
        return getXPathResultNode(filterExpression, nodeSource, 0);
    }

    @Override
    public Node getXPathResultNode(final String filterExpression, final Node nodeSource,
            final int itemPos) {
        final List<Node> nodeList = getXPathResultNodeList(filterExpression, nodeSource);
        if (nodeList.size() > 0) {
            return nodeList.get(0).getChildNodes().item(itemPos);
        } else {
            return null;
        }
    }

    @Override
    public List<Node> getXPathResultNodeList(final String filterExpression, final Node nodeSource) {
        final XPathExpression xpathExpression = XPathExpressionFactory
                .createXPathExpression(filterExpression);
        return xpathExpression.evaluateAsNodeList(nodeSource);
    }

    @Override
    public Node getFileDocumentElement(final DocumentBuilderFactory factory, final Path filePath)
            throws ConnectorException {
        final File file = new File(filePath.toString());
        final Document doc;
        InputStream source = null;
        try {
            source = new FileInputStream(file);
            doc = factory.newDocumentBuilder().parse(source);
            return doc.getDocumentElement();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            final String errorMessage = "ERROR: No se pudo parsear el WSDL";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (final IOException exception) {
                    final String errorMessage = ERROR_INTERNO_AL_CERRAR_STREAM;
                    LOGGER.error(errorMessage, exception);
                    throw new ConnectorException("ERROR: Ocurrió un error inesperado", exception);
                }
            }
        }
    }

    @Override
    public Node getFileDocumentElement(final DocumentBuilderFactory factory,
            final MultipartFile file) throws ConnectorException {
        final Document doc;
        try {
            doc = factory.newDocumentBuilder().parse(file.getInputStream());
            return doc.getDocumentElement();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            final String errorMessage = "ERROR: No se pudo parsear el WSDL";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public boolean isWSDLFile(final Path filePath) throws ConnectorException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Node nodeSource = getFileDocumentElement(factory, filePath);

        final NamedNodeMap attributes = nodeSource.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node attributeNode = attributes.item(i);
            if (attributeNode.getNodeValue().contains(HTTP_SCHEMAS_WSDL)) {
                return true;
            }
        }
        return false;
    }

}
