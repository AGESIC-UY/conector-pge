package biz.ideasoft.soa.esb.actions;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.MessagePayloadProxy;
import org.jboss.soa.esb.message.body.content.BytesBody;
import org.jboss.soa.esb.util.XPathNamespaceContext;
import org.xml.sax.InputSource;

import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;
import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.esb.util.XPathXmlDogUtil;

/**
* Configuration Example:
 *<pre>{@code
 *
 *<action name="set-wsa-properties" class="biz.ideasoft.soa.esb.actions.SetWsaPropertiesAction"/>
 *
 * }</pre>
*/
public class SetWsaPropertiesAction extends AbstractActionLifecycle {
	private Logger logger = Logger.getLogger(SetWsaPropertiesAction.class);
	protected ConfigTree _config;
	protected Map<String, String> namespaces;
	private boolean actionRequired;
	private boolean toRequired;
	private boolean messageIDRequired;
	
	public SetWsaPropertiesAction(ConfigTree config) {
		_config = config;

		actionRequired = Boolean.parseBoolean(config.getAttribute("action-header-required", "true"));
		toRequired = Boolean.parseBoolean(config.getAttribute("to-header-required", "true"));
		messageIDRequired = Boolean.parseBoolean(config.getAttribute("messageID-header-required", "true"));

		namespaces = new HashMap<String, String>();
		namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");
	}

	private static XPathFactory xpf = XPathFactory.newInstance();
	private static MessagePayloadProxy payloadProxy;

    static {
        payloadProxy = new MessagePayloadProxy(new ConfigTree("config"), new String[] {BytesBody.BYTES_LOCATION}, new String[] {BytesBody.BYTES_LOCATION});
    }

    public Message process(Message message) throws Exception {
    	try {
    		/*
    		String wsaReplyTo = selectAsString(message, "/soapenv:Envelope/soapenv:Header/wsa:ReplyTo/wsa:Address", namespaces);
    		String wsaMessageID = selectAsString(message, "/soapenv:Envelope/soapenv:Header/wsa:MessageID", namespaces);
			String wsaTo = selectAsString(message, "/soapenv:Envelope/soapenv:Header/wsa:To", namespaces);
			String wsaAction = selectAsString(message, "/soapenv:Envelope/soapenv:Header/wsa:Action", namespaces);
			if (wsaTo == null) {
				Throwable t = new Throwable("The Header \"To\" can not be null or empty");
				SoapUtil.throwFaultException(t);
			}
			if (actionRequired && wsaAction == null) {
				Throwable t = new Throwable("The Header \"Action\" can not be null or empty");
				SoapUtil.throwFaultException(t);
			}
			message.getProperties().setProperty("wsaTo", wsaTo);
			message.getProperties().setProperty("wsaAction", wsaAction == null ? "" : wsaAction);
			if (wsaReplyTo != null) {
				message.getProperties().setProperty("wsaReplyTo", wsaReplyTo);
			}
			if (wsaMessageID != null) {
				message.getProperties().setProperty("wsaMessageID", wsaMessageID);
			}
			*/
    		
    		List<String> xpaths = new LinkedList<String>();
			xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:ReplyTo/wsa:Address/text()");
			xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:MessageID/text()");
			xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:To/text()");
			xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:Action/text()");
			xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:RelatesTo/text()");
			
			XPathXmlDogUtil xpathXmlDogUtil = new XPathXmlDogUtil();
			List<Object> listResultsXPath = xpathXmlDogUtil.executeMultipleXPathWithCollections(message, xpaths, namespaces);

			String wsaReplyTo = null; // selectAsString(message,
			// "/soapenv:Envelope/soapenv:Header/wsa:ReplyTo/wsa:Address",
			// namespaces);
			String wsaMessageID = null; // selectAsString(message,
			// "/soapenv:Envelope/soapenv:Header/wsa:MessageID",
			// namespaces);
			String wsaTo = null; // selectAsString(message,
			// "/soapenv:Envelope/soapenv:Header/wsa:To",
			// namespaces);
			String wsaAction = null; // selectAsString(message,
			// "/soapenv:Envelope/soapenv:Header/wsa:Action",
			// namespaces);
			
			if (listResultsXPath.get(0) instanceof Collection) {
				wsaReplyTo = ((List<String>) listResultsXPath.get(0)).get(0);
			} else {
				wsaReplyTo = (String) listResultsXPath.get(0);
			}
			
			if (listResultsXPath.get(1) instanceof Collection) {
				wsaMessageID = ((List<String>) listResultsXPath.get(1)).get(0);
			} else {
				wsaMessageID = (String) listResultsXPath.get(1);
			}
			
			if (listResultsXPath.get(2) instanceof Collection) {
				wsaTo = ((List<String>) listResultsXPath.get(2)).get(0);
			} else {
				wsaTo = (String) listResultsXPath.get(2);
			}
			
			if (listResultsXPath.get(3) instanceof Collection) {
				wsaAction = ((List<String>) listResultsXPath.get(3)).get(0);
			} else {
				wsaAction = (String) listResultsXPath.get(3);
			}
			
			if (toRequired && wsaTo == null) {
				Throwable t = new Throwable("The Header \"To\" can not be null or empty");
				SoapUtil.throwFaultException(t);
			}
			if (actionRequired && wsaAction == null) {
				Throwable t = new Throwable("The Header \"Action\" can not be null or empty");
				SoapUtil.throwFaultException(t);
			}
			//// Descomentar para simular lo que hace el data power
			/*if (messageIDRequired && wsaMessageID == null) {
				wsaMessageID=UUID.randomUUID().toString();
			}*/
			
			if (messageIDRequired && wsaMessageID == null) {
				Throwable t = new Throwable("The Header \"MessageID\" can not be null or empty");
				SoapUtil.throwFaultException(t, AgesicLoggerConstants.NO_EXIST_MESSAGE_ID_ERROR_CODE);
			}
			
			message.getProperties().setProperty("wsaTo", wsaTo == null ? "" : wsaTo);
			message.getProperties().setProperty("wsaAction", wsaAction == null ? "" : wsaAction);
			if (wsaReplyTo != null) {
				message.getProperties().setProperty("wsaReplyTo", wsaReplyTo);
			} else {
				message.getProperties().remove("wsaReplyTo");
			}
			if (wsaMessageID != null) {
				message.getProperties().setProperty("wsaMessageID", wsaMessageID);
			} else {
				message.getProperties().remove("wsaMessageID");
			}
			
			/**
			 * wsaRelatesTo
			 **/
			if (listResultsXPath.get(4) instanceof Collection) {
				List<String> wsaRelatesToList = (List<String>) listResultsXPath.get(4);
				String[] wsaRelatesTo = Arrays.copyOf(wsaRelatesToList.toArray(), wsaRelatesToList.size() , String[].class);
				message.getProperties().setProperty("wsaRelatesTo", wsaRelatesTo);
				
			} else if (listResultsXPath.get(4) != null) {				
				String[] wsaRelatesTo = new String[1];
				wsaRelatesTo[0] = (String) listResultsXPath.get(4);
				message.getProperties().setProperty("wsaRelatesTo", wsaRelatesTo);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		return message;
	}

    public static String selectAsString(final Message message, final String xpathExp, final Map<String,String> namespaces ) throws XPathExpressionException {
		XPath xpath = getXPath( namespaces );
		String string = (String) xpath.evaluate( xpathExp, getInputSource(message), XPathConstants.STRING);
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