package biz.ideasoft.soa.esb.actions.soap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.jboss.internal.soa.esb.message.format.xml.MessageImpl;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Body;
import org.jboss.soa.esb.message.Message;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.esb.util.XPathUtil;

/**
* Configuration Example:
 *<pre>{@code
 *
 *               <action name="invokeService" class="biz.ideasoft.soa.esb.actions.soap.SOAPInvokeServiceAction">
 *               	<property name="categoryAsync" value="Category"/>
 *               	<property name="serviceAsync" value="ServiceName"/>
 *               	<property name="categorySync" value="Category"/>
 *               	<property name="serviceSync" value="serviceNameSync"/>

 *               	<property name="AsyncOperationsName" value="operationName"/>
 *               	<property name="SyncOperationsName" value="otherOperationName"/>
 *               </action>
 *
 * }</pre>
*/
public class SOAPInvokeServiceAction extends AbstractActionLifecycle {

	private Logger logger = Logger.getLogger(SOAPInvokeServiceAction.class);
	private ConfigTree _config;

	protected Map<String, String> namespaces;
	
	protected Set<String> setAsync;
	protected Set<String> setSync;
	
	private	 String categoryAsync; 
	private	 String serviceAsync;
	
	private	 String categorySync;
	private	 String serviceSync;
 

	public SOAPInvokeServiceAction(ConfigTree config) {
		_config = config;
		
		namespaces = new HashMap<String, String>();
		namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
		
		setAsync = new HashSet<String>();
		setSync = new HashSet<String>();

		String operationsName = config.getAttribute("AsyncOperationsName");
		addToSet(setAsync, operationsName);
		operationsName = config.getAttribute("SyncOperationsName");
		addToSet(setSync, operationsName);
		
		categoryAsync = config.getAttribute("categoryAsync");
		serviceAsync = config.getAttribute("serviceAsync");

		categorySync = config.getAttribute("categorySync");
		serviceSync = config.getAttribute("serviceSync");
	}
	
	private void addToSet(Set<String> set, String str) {
		StringTokenizer tokenizer = new StringTokenizer(str, ",");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			set.add(token);
		}
	}

	
    public Message process(Message message) throws Exception {
    	try {
    		Element element = XPathUtil.selectAsElement(message, "/soapenv:Envelope/soapenv:Body", namespaces);
    		NodeList list = element.getChildNodes();
    		for (int i = 0; i < list.getLength(); i ++) {
    			Node node = list.item(i);
    			String localName = node.getLocalName();
    			if (localName != null) {
	    			if (setAsync.contains(localName)) {
	    				Service wsService = new Service(categoryAsync, serviceAsync);
	    				ServiceInvoker invoker = new ServiceInvoker(wsService);
	    				invoker.deliverAsync(message);
	    				String emptySoap = "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
	    				   "<env:Header/>" +
	    				   "<env:Body/>" +
	    				"</env:Envelope>";
	    				Message copy = message.copy();
	    				copy.getHeader().getCall().setReplyTo(null);
	    				copy.getBody().add(Body.DEFAULT_LOCATION, emptySoap);
	    				return copy;
	    			} else if (setSync.contains(localName)) {
	    				Service wsService = new Service(categorySync, serviceSync);
	    				ServiceInvoker invoker = new ServiceInvoker(wsService);
	    				
    					Message request = new MessageImpl();
    					request.getBody().add(Body.DEFAULT_LOCATION, message.getBody().get());
    					return invoker.deliverSync(request, 30000);
	    			}
    			}
    		}
		} catch (XPathExpressionException e) {
			logger.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		return message;
	}

}
