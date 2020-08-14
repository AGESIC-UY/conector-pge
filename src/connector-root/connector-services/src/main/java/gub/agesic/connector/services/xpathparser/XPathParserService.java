package gub.agesic.connector.services.xpathparser;

import java.nio.file.Path;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import gub.agesic.connector.pojo.SoapVersionInfo;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;

/**
 * Interfaz que provee métodos para el parseo de archivos a través de XPath.
 */

import gub.agesic.connector.exceptions.ConnectorException;

public interface XPathParserService {

    boolean getBooleanNodeValue(final Node node);

    String getStringNodeValue(final Node node);

    String getNamedItemValue(final Node node, final String namedItem);

    Node getNodeByName(final Node node, final String name);

    Node getXPathResultNode(final String filterExpression, final Node nodeSource);

    Node getXPathResultNode(final String filterExpression, final Node nodeSource,
            final int itemPos);

    List<Node> getXPathResultNodeList(final String filterExpression, final Node nodeSource);

    Node getFileDocumentElement(DocumentBuilderFactory factory, Path filePath)
            throws ConnectorException;

    boolean isWSDLFile(Path filePath) throws ConnectorException;

    Node getFileDocumentElement(final DocumentBuilderFactory factory, final MultipartFile file)
            throws ConnectorException;

    SoapVersionInfo soapVersionInfo(final Node nodeSource);
}
