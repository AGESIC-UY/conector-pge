package uy.gub.agesic.esb.action;

import java.net.URI;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.common.Environment;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;

import biz.ideasoft.soa.esb.util.SoapUtil;


public class QueryPathBasedRouter extends AbstractActionPipelineProcessor {
	
	protected static Logger _logger = Logger.getLogger(QueryPathBasedRouter.class);
	protected String actionInfo;
	
	private Service wsdlService;
	private Service invokeService;
	private long timeout = 30000;

	public QueryPathBasedRouter(ConfigTree config) throws ConfigurationException, RegistryException {
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";
		
		String wsdlServiceName = config.getAttribute("wsdlServiceName");
		String wsdlCategoryName = config.getAttribute("wsdlCategoryName");
		String wsInvokerServiceName = config.getAttribute("wsInvokerServiceName");
		String wsInvokerCategoryName = config.getAttribute("wsInvokerCategoryName");
		
		timeout = config.getLongAttribute("timeout", 30000);

		
		wsdlService = new Service(wsdlCategoryName, wsdlServiceName);
		
		invokeService = new Service(wsInvokerCategoryName, wsInvokerServiceName);
    }
	
	public Message process(Message msg) throws ActionProcessingException {
		Object obj = msg.getProperties().getProperty("Query");
		if (obj != null && obj.toString().equalsIgnoreCase("wsdl")) {
			return invokeService(new Service(wsdlService.getCategory(), wsdlService.getName()), msg);	
		}
		return invokeService(new Service(invokeService.getCategory(), invokeService.getName()), msg);
	}
	
	private Message invokeService(Service service, Message msg) throws ActionProcessingFaultException {
		URI msgID = msg.getHeader().getCall().getMessageID();
		Message copy = null;
		try {
			copy = msg.copy();
			copy.getProperties().setProperty(Environment.EXCEPTION_ON_DELIVERY_FAILURE, "true");
			copy.getHeader().getCall().setReplyTo(null);
			ServiceInvoker invoker = new ServiceInvoker(service);
			return invoker.deliverSync(copy, timeout);
		} catch (MessageDeliverException mde) {
			String error = actionInfo + " [" + msgID + "] " + mde.getLocalizedMessage();
			_logger.error(error, mde);
			if (copy != null) {
				URI copyMsgID = copy.getHeader().getCall().getMessageID();
				error = actionInfo + " [" + copyMsgID + "] from message [" + msgID + "] " + mde.getLocalizedMessage();
				_logger.error(error, mde);
			}
			throw SoapUtil.createActionPipelineException("Timeout Exceeded", null, null, new RuntimeException("Error"));
		} catch (Exception e) {
			String error = actionInfo + " [" + msgID + "] " + e.getLocalizedMessage();
			_logger.error(error, e);			
			throw SoapUtil.createActionPipelineException(error, null, null, e);
		}
	}
	
}
