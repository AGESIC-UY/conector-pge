package biz.ideasoft.soa.esb.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathException;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

import org.apache.log4j.Logger;
import org.jaxen.saxpath.SAXPathException;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;
import org.jboss.soa.esb.services.routing.MessageRouterException;

import biz.ideasoft.soa.esb.util.PropertiesConfiguration;
import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.esb.util.XPathUtil;

public class CBRSyncInvoker extends AbstractActionPipelineProcessor {
	public static final String DEFAULT_ROUTE_TAG = "default-route";
	public static final String ROUTE_TO_TAG = "route-to";
	public static final String NAMESPACE_TAG = "namespace";
	public static final String XPATH_TAG = "xpath";
	public static final String DESTINATION_NAME_PROPERTY_NAME = "route-to-destination-name";
		
	private static Logger log = Logger.getLogger(CBRSyncInvoker.class);
	
	private Map<Service, ServiceInvoker> invokers = new LinkedHashMap<Service, ServiceInvoker>();
	private Service defaultRouteService;
	private Map<String, Service> destinations = new HashMap<String, Service>();
	private long timeoutMillis = 1000 * 60 * 5; //5 min
	
	private Map<String, String> xpaths;
	
	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	private XMLDog dog;
	private Map<String, Expression> exprsCache;


	public CBRSyncInvoker(ConfigTree config) throws ConfigurationException, RegistryException, MessageRouterException {
		String timeout = PropertiesConfiguration.getProperty("CBRSyncInvoker.ServiceInvoker.timeout");
		if (timeout != null) {
			try {
				timeoutMillis = Long.parseLong(timeout);
			} catch (Exception e) {				
				log.warn(e.getLocalizedMessage(), e);
			}
		}
		
		nsContext.declarePrefix("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
		ConfigTree[] nsList = config.getChildren(NAMESPACE_TAG);
		if (nsList != null && nsList.length > 0) {
			for (ConfigTree ct : nsList) {
				String url = ct.getRequiredAttribute("url");
				String prefix = ct.getRequiredAttribute("prefix");
				nsContext.declarePrefix(prefix, url);
			}
		}
		dog = new XMLDog(nsContext, null, null);

		xpaths = new HashMap<String, String>();
		exprsCache = new HashMap<String, Expression>();
		ConfigTree[] xpathList = config.getChildren(XPATH_TAG);
		if (xpathList != null && xpathList.length > 0) {
			for (ConfigTree ct : xpathList) {
				String xpath = ct.getRequiredAttribute("expr");
				String destination = ct.getRequiredAttribute("destination");
				xpaths.put(xpath, destination);
				try {
					Expression xpathExpr = dog.addXPath(xpath);
					exprsCache.put(xpath, xpathExpr);
				} catch (SAXPathException e) {
					log.error("Error compiling xpath expression", e);
				}
			}
		}		

		ConfigTree[] destList = config.getChildren(ROUTE_TO_TAG);
		if (destList != null && destList.length > 0) {
			for (ConfigTree ct : destList) {
				String category = ct.getAttribute(ListenerTagNames.SERVICE_CATEGORY_NAME_TAG, "");
				String serviceName = ct.getRequiredAttribute(ListenerTagNames.SERVICE_NAME_TAG);
				String name = ct.getRequiredAttribute("destination-name");
				Service service = new Service(category, serviceName);
				destinations.put(name, service);
				invokers.put(service, null);
			}
		}

		ConfigTree[] defaultList = config.getChildren(DEFAULT_ROUTE_TAG);
		if (defaultList != null && defaultList.length > 0) {
			ConfigTree ct = defaultList[0];
			String category = ct.getAttribute(ListenerTagNames.SERVICE_CATEGORY_NAME_TAG, "");
			String name = ct.getRequiredAttribute(ListenerTagNames.SERVICE_NAME_TAG);
			defaultRouteService = new Service(category, name);
			invokers.put(defaultRouteService, null);
		}
		
	}

	public Message process(Message message) throws ActionProcessingException {
		List<Service> outgoingDestinations = null;
		try {
			outgoingDestinations = executeRules(message);
		} catch (Exception e) {
			log.warn("Error when executing the rules", e);
			outgoingDestinations = new ArrayList<Service>();
		}
		try {
			if (outgoingDestinations.size() == 0) {
				if (defaultRouteService != null) {
					outgoingDestinations.add(defaultRouteService);
					log.debug("Route to default destination: " + defaultRouteService);
				} else {
					log.warn("No destination was found to route message");
					return message;
				}
			}
			return routeMessageSync(message, outgoingDestinations);
		} catch (MessageRouterException e) {
			log.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		return null;
	}
	
	protected List<Service> executeRules(Message message) {
		List<Service> outgoingDestinations = new ArrayList<Service>();
		
		try {
			XPathResults results = dog.sniff(XPathUtil.getInputSource(message));
			Iterator<String> iter = exprsCache.keySet().iterator();
			while (iter.hasNext()) {
				String xpath = iter.next();
				Expression expr = exprsCache.get(xpath);
				Object obj = results.getResult(expr);
				boolean ok = false;
				if (obj instanceof Boolean) {
					ok = (Boolean) obj;
				} else if (obj instanceof List) {
					ok = ((List) obj).size() > 0;
				}
				
				if (ok) {
					String destination = xpaths.get(xpath);
					log.debug("Redirect to " + destination);
					message.getProperties().setProperty(DESTINATION_NAME_PROPERTY_NAME, destination);
					Service service = destinations.get(destination);
					if (service != null) {
						outgoingDestinations.add(service);
						break;
					}
				}
			}
		} catch (XPathException e) {
			log.error(e.getLocalizedMessage(), e);
		}		
		
		return outgoingDestinations;
	}

	protected Message routeMessageSync(Message message, List<Service> outgoingDestinations) throws MessageRouterException {
		try {
			Message result = null;
			Message copy = message.copy();
			copy.getHeader().getCall().setReplyTo(null);
			for (Service service : outgoingDestinations) {
				ServiceInvoker invoker = getInvoker(service);
				if (invoker == null) {
					log.warn("Invoker not found for service " + service);
					return null;
				}
	            result = invoker.deliverSync(copy, timeoutMillis);
			}
			return result;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return message;
	}
	
    private ServiceInvoker getInvoker(Service recipient) throws RegistryException, MessageDeliverException {
        ServiceInvoker invoker = invokers.get(recipient);

        // We lazily create the invokers...
        if(invoker == null) {
            if(!invokers.containsKey(recipient)) {
                // We don't create an invoker for the Service if it wasn't
                // already "registered" via the addRecipient method.
                return null;
            }
            invoker = new ServiceInvoker(recipient);
            invokers.put(recipient, invoker);
        }

        return invoker;
    }

}