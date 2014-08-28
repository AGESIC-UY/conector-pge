package uy.gub.agesic.esb.action;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class PathContextAction extends AbstractActionPipelineProcessor {

	protected ConfigTree config;
	private boolean connectorType;
	
	public PathContextAction(ConfigTree config) throws ConfigurationException, RegistryException {
		
		this.config = config;
		connectorType = Boolean.parseBoolean(config.getAttribute("connectorType"));
	}
	
	public Message process(Message message) throws ActionProcessingException {
		
		String path = (String) message.getProperties().getProperty("Path");
		
		message.getBody().add("Path", path);
		message.getBody().add("connectorType", connectorType);
		
		String stsUrl =  null;
		if (connectorType) {
			stsUrl = (String) message.getProperties().getProperty("stsURL.production");
		} else {
			stsUrl = (String) message.getProperties().getProperty("stsURL.test");
		}
		
		if (stsUrl != null) {
			message.getProperties().setProperty("stsURL", stsUrl);
		} else {
			SoapUtil.throwFaultException(new Exception("STS url could not be found."));
		}
		
		return message;
	}
	
}
