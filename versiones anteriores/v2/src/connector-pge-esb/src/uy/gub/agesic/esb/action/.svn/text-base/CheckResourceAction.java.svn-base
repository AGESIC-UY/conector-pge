package uy.gub.agesic.esb.action;

import java.net.URI;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class CheckResourceAction extends AbstractActionPipelineProcessor {
	
	public static final Logger log = Logger.getLogger(CheckResourceAction.class); 
	
	protected ConfigTree config;

	protected String actionInfo;
	
	public CheckResourceAction(ConfigTree config) throws ConfigurationException, RegistryException {
		this.config = config;
		
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";
	}
	
	public Message process(Message message) throws ActionProcessingException {
		String location = (String) message.getProperties().getProperty("location");
		
		String query = (String) message.getProperties().getProperty("Query");
		if (query != null) {
			query = "?" + query;
		}
		
		boolean soapFault = (Boolean) message.getProperties().getProperty("soapFault");
		if (soapFault) {
			String error = "No se encontr\u00F3 el recurso para la URL " + location + query;
			if (log.isInfoEnabled()) {
				URI msgID = message.getHeader().getCall().getMessageID();
				log.info(actionInfo + " [" + msgID + "] " + error);
			}
			Exception e = new Exception(error);
			throw SoapUtil.createActionPipelineException(error, "The resource requested was not found", null, e);
		}
		return message;
	}
}