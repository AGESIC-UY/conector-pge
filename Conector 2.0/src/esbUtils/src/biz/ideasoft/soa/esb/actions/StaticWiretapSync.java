package biz.ideasoft.soa.esb.actions;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;
import org.jboss.soa.esb.services.routing.MessageRouterException;

public class StaticWiretapSync extends AbstractActionPipelineProcessor {
	
	private Service routeService;
	private ServiceInvoker invoker;
	private long defaultTimeoutMillis = 1000 * 60 * 5; //5 min
	private long timeoutMillis = defaultTimeoutMillis;
	
    public StaticWiretapSync(ConfigTree config) throws ConfigurationException, RegistryException {
        ConfigTree[] route = config.getChildren("route");
        if (route != null) {
        	String category = route[0].getAttribute("service-category");
        	String name = route[0].getAttribute("service-name");
            routeService = new Service(category, name);
        } else {
        	throw new ConfigurationException("You must configure router service destination");
        }
        String timeoutStr = config.getAttribute("timeout");
        if (timeoutStr != null ) {
        	try {
        		timeoutMillis = Long.parseLong(timeoutStr);
        	} catch (Exception e) {
        		_logger.warn(e.getMessage(), e);
        	}
        }
    }
    
	public Message process(Message message) throws ActionProcessingException {
		try {
			return routeMessageSync(message);
		} catch (MessageRouterException e) {
			_logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	protected Message routeMessageSync(Message message) throws MessageRouterException {
		try {
			Message copy = message.copy();
			copy.getHeader().getCall().setReplyTo(null);
			
			if (invoker == null) {
				invoker = new ServiceInvoker(routeService);
			}
			if (invoker == null) {
				_logger.warn("Invoker not found for service " + routeService);
				return null;
			}
            return invoker.deliverSync(copy, timeoutMillis);
		} catch (Exception e) {
			_logger.error(e.getLocalizedMessage(), e);
		}
		return message;
	}
    
    protected static Logger _logger = Logger.getLogger(StaticWiretapSync.class);
}
