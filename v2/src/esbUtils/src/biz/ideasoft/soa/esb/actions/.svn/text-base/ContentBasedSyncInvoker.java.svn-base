package biz.ideasoft.soa.esb.actions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.ContentBasedWiretap;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;
import org.jboss.soa.esb.services.routing.MessageRouterException;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class ContentBasedSyncInvoker extends ContentBasedWiretap {
	public static final String DEFAULT_ROUTE_TAG = "default-route";
	
	private static Logger log = Logger.getLogger(ContentBasedSyncInvoker.class);
	
	private Map<Service, ServiceInvoker> invokers = new LinkedHashMap<Service, ServiceInvoker>();
	private Service defaultRouteService;
	private long timeout = 300000;

	public ContentBasedSyncInvoker(ConfigTree config) throws ConfigurationException, RegistryException, MessageRouterException {
		super(config);

		ConfigTree[] destList = _config.getChildren(DEFAULT_ROUTE_TAG);
		if (destList != null && destList.length > 0) {
			ConfigTree ct = destList[0];
			String category = ct.getAttribute(ListenerTagNames.SERVICE_CATEGORY_NAME_TAG, "");
			String name = ct.getRequiredAttribute(ListenerTagNames.SERVICE_NAME_TAG);
			defaultRouteService = new Service(category, name);
			invokers.put(defaultRouteService, null);
		}
		
		timeout = config.getLongAttribute("timeout", timeout);
		
		for (Service srv : _destinations.values()) {
			invokers.put(srv, null);
		}
	}

	public Message process(Message message) throws ActionProcessingException {
		List<Service> outgoingDestinations = null;
		try {
			outgoingDestinations = executeRules(message);
		} catch (MessageRouterException e) {
			outgoingDestinations = new ArrayList<Service>();
		}
		try {
			if (outgoingDestinations.size() == 0) {
				if (defaultRouteService != null) {
					outgoingDestinations.add(defaultRouteService);
				} else {
					return super.process(message);
				}
			}
			return routeMessageSync(message, outgoingDestinations);
		} catch (MessageRouterException e) {
			log.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		return null;
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
	            result = invoker.deliverSync(copy, timeout);
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