package uy.gub.agesic.esb.action;

import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class CheckConnectorAction extends AbstractActionPipelineProcessor {
	protected ConfigTree config;

	public CheckConnectorAction(ConfigTree config) throws ConfigurationException, RegistryException {
		this.config = config;
	}
	
	public Message process(Message message) throws ActionProcessingException {
		String location = (String) message.getProperties().getProperty("location");
		String url = (String) message.getProperties().getProperty("routePhysicalURL");
		if (url == null) {
			String error = "No se encontro una configuracion para la URL " + location;
			Exception e = new Exception(error);
			throw SoapUtil.createActionPipelineException(error, "The configuration requested was not found", null, e);
		}
		return message;
	}
}