package biz.ideasoft.soa.esb.actions.soap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.soap.SOAPProcessor;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Body;
import org.jboss.soa.esb.message.Message;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.esb.util.XPathUtil;

/**
 * Configuration Example:
 * 
 * <pre>
 * {@code
 * 
 *               <action name="invokeService" class="biz.ideasoft.soa.esb.actions.soap.SOAPInvokeServiceAction">
 *               	<property name="categoryRedirect" value="Category"/>
 *               	<property name="serviceRedirect" value="ServiceName"/>
 * 
 *               	<property name="RedirectOperationsName" value="operationName"/>
 *               	<property name="ProcessOperationsName" value="otherOperationName"/>
 *               </action>
 * 
 * }
 * </pre>
 */
public class SkipSOAPProcessor extends SOAPProcessor {

	private Logger logger = Logger.getLogger(SkipSOAPProcessor.class);
	private ConfigTree _config;

	protected Map<String, String> namespaces;

	protected Set<String> setRedirect;
	protected Set<String> setProcess;

	private String categoryRedirect;
	private String serviceRedirect;
	private Service wsService;

	private static String xpath = "/soapenv:Envelope/soapenv:Body";
	private static String emptySoap = 
			"<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
			"<env:Header/>" + 
			"<env:Body/>" + 
			"</env:Envelope>";

	public SkipSOAPProcessor(ConfigTree config) throws ConfigurationException {
		super(config);

		_config = config;

		namespaces = new HashMap<String, String>();
		namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

		setRedirect = new HashSet<String>();
		setProcess = new HashSet<String>();

		String operationsName = config.getAttribute("RedirectOperationsName");
		addToSet(setRedirect, operationsName);
		operationsName = config.getAttribute("ProcessOperationsName");
		addToSet(setProcess, operationsName);

		categoryRedirect = config.getAttribute("categoryRedirect");
		serviceRedirect = config.getAttribute("serviceRedirect");

		wsService = new Service(categoryRedirect, serviceRedirect);

	}

	private void addToSet(Set<String> set, String str) {
		StringTokenizer tokenizer = new StringTokenizer(str, ",");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			set.add(token);
		}
	}

	public Message process(Message message) throws ActionProcessingException {
		try {
			Element element = XPathUtil.selectAsElement(message, xpath, namespaces);
			NodeList list = element.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				String localName = node.getLocalName();
				if (localName != null) {
					if (setRedirect.contains(localName)) {
						ServiceInvoker invoker = new ServiceInvoker(wsService);

						Message copy = message.copy();
						copy.getHeader().getCall().setTo(null);
						copy.getHeader().getCall().setFrom(null);
						copy.getHeader().getCall().setReplyTo(null);
						invoker.deliverAsync(copy);

//						message.getHeader().getCall().setReplyTo(null);
//						message.getBody().add(Body.DEFAULT_LOCATION,
//						emptySoap);
//						return message;
						copy = message.copy();
						copy.getHeader().getCall().setReplyTo(null);
						copy.getBody().add(Body.DEFAULT_LOCATION, emptySoap);
						return copy;
					} else if (setProcess.contains(localName)) {
						Message replyMessage = super.process(message);
						return replyMessage;
					}
				}
			}
		} catch (XPathExpressionException e) {
			logger.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		} catch (MessageDeliverException e) {
			logger.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		return message;
	}

}
