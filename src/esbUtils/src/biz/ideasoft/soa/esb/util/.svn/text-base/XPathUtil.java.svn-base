package biz.ideasoft.soa.esb.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.util.XPathNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathUtil {
	
	private static XPathFactory xpf = XPathFactory.newInstance();

    public static String selectAsString(final Message message, final String xpathExp, final Map<String,String> namespaces ) throws XPathExpressionException {
		XPath xpath = getXPath(namespaces);
		String string = (String) xpath.evaluate(xpathExp, getInputSource(message), XPathConstants.STRING);
		return string;
	}

    public static Element selectAsElement(final Message message, final String xpathExp, final Map<String,String> namespaces, XPath xpath ) throws XPathExpressionException {
		if (xpath == null) {
			xpath = getXPath(namespaces);
		}
		Element element = (Element) xpath.evaluate(xpathExp, getInputSource(message), XPathConstants.NODE);
		return element;
	}

    public static Element selectAsElement(final Message message, final String xpathExp, final Map<String,String> namespaces ) throws XPathExpressionException {
    	XPath xpath = null;
		return selectAsElement(message, xpathExp, namespaces, xpath);
	}

    public static Element selectAsElement(final Message message, final String xpathExp, final Map<String,String> namespaces, XPathExpression xPathExp ) throws XPathExpressionException {
    	Element element = null;
		if (xPathExp == null) {
			XPath xpath = getXPath(namespaces);
			element = (Element) xpath.evaluate(xpathExp, getInputSource(message), XPathConstants.NODE);
		} else {
			element = (Element) xPathExp.evaluate(getInputSource(message), XPathConstants.NODE);
		}
		return element;
	}

	public static XPath getXPath(final Map<String, String> namespaces) {
		final XPath xpath = xpf.newXPath();
		setNamespaces(xpath, namespaces);
		return xpath;
	}

	public static void setNamespaces(final XPath xpath, final Map<String, String> namespaces) {
		if (namespaces == null)
			return;

		final XPathNamespaceContext namespaceContext = new XPathNamespaceContext();
		for (Entry<String, String> entry : namespaces.entrySet())
			namespaceContext.setMapping(entry.getKey(), entry.getValue());

		xpath.setNamespaceContext(namespaceContext);
	}
	
	public static Document parse(Message message) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return parser.parse(getInputSource(message));
	}


	public static InputSource getInputSource(Message message) throws XPathExpressionException {
		Object payload;

		payload = message.getBody().get();
		if (payload == null) {
			return new InputSource(new StringReader(""));
		}

		if (payload instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) payload));
		} else if (payload instanceof String) {
			return new InputSource(new StringReader((String) payload));
		} else {
			String name = null;
			try {
				name = payload.getClass().getName();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			throw new XPathExpressionException("Unsupport expression input object type: " + name + " ,payload " + payload);
		}
	}

}
