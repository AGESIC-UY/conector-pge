package biz.ideasoft.soa.esb.actions;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.MessagePayloadProxy;
import org.jboss.soa.esb.message.body.content.BytesBody;
import org.jboss.soa.esb.util.XPathNamespaceContext;
import org.xml.sax.InputSource;

import biz.ideasoft.soa.esb.util.SoapUtil;


public abstract class AbstractXPathAction extends AbstractActionLifecycle {
	protected ConfigTree _config;
	protected Map<String, String> namespaces;
	
	public AbstractXPathAction(ConfigTree config) {
		_config = config;
		
		namespaces = new HashMap<String, String>();
		namespaces.put("soapenv", SoapUtil.SOAP_ENV_NAMESPACE);
	}

	private static XPathFactory xpf = XPathFactory.newInstance();
	private static MessagePayloadProxy payloadProxy;

    static {
        payloadProxy = new MessagePayloadProxy(new ConfigTree("config"), new String[] {BytesBody.BYTES_LOCATION}, new String[] {BytesBody.BYTES_LOCATION});
    }

    public static String selectAsString(Message message, String xpathExp, Map<String,String> namespaces ) throws XPathExpressionException {
		XPath xpath = getXPath( namespaces );
		String string = (String) xpath.evaluate( xpathExp, getInputSource(message), XPathConstants.STRING);
		return string;
	}

    public static String selectAsString(InputSource source, String xpathExp, Map<String,String> namespaces ) throws XPathExpressionException {
		XPath xpath = getXPath( namespaces );
		String string = (String) xpath.evaluate( xpathExp, source, XPathConstants.STRING);
		return string;
	}

	private static InputSource getInputSource(Message message) throws XPathExpressionException {
		Object payload;

		try {
			payload = payloadProxy.getPayload(message);
		} catch (MessageDeliverException e) {
			throw new XPathExpressionException(e);
		}

		if (payload instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) payload));
		} else if (payload instanceof String) {
			return new InputSource(new StringReader((String) payload));
		} else {
			throw new XPathExpressionException("Unsupport expression input object type: " + payload.getClass().getName());
		}
	}

	private static XPath getXPath(final Map<String, String> namespaces) {
		final XPath xpath = xpf.newXPath();
		setNamespaces(xpath, namespaces);
		return xpath;
	}

	private static void setNamespaces(final XPath xpath, final Map<String, String> namespaces) {
		if (namespaces == null)
			return;

		final XPathNamespaceContext namespaceContext = new XPathNamespaceContext();
		for (Entry<String, String> entry : namespaces.entrySet())
			namespaceContext.setMapping(entry.getKey(), entry.getValue());

		xpath.setNamespaceContext(namespaceContext);
	}
}