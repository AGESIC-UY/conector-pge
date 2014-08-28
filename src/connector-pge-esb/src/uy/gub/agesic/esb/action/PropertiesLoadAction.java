package uy.gub.agesic.esb.action;

import java.io.InputStream;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;
import org.jboss.soa.esb.services.registry.RegistryException;

public class PropertiesLoadAction extends AbstractActionPipelineProcessor {
	protected ConfigTree config;
	protected static Logger _logger = Logger.getLogger(PropertiesLoadAction.class);
	protected String actionInfo;
	protected java.util.Properties properties = new java.util.Properties();

	public PropertiesLoadAction(ConfigTree config) throws ConfigurationException, RegistryException {
		this.config = config;
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";

		String propsFile = config.getAttribute("propertiesFile");
		InputStream is = PropertiesLoadAction.class.getResourceAsStream(propsFile);
		try {
			properties.load(is);
			is.close();		
		} catch (Exception e) {
			String error = actionInfo + e.getLocalizedMessage();
			_logger.error(error, e);			
		}
		
	}
	
	public Message process(Message message) throws ActionProcessingException {
		Properties msgProps = message.getProperties();
		if (!properties.isEmpty()) {
			Set<Object> set = properties.keySet();
			for (Object key : set) {
				if (msgProps.getProperty(String.valueOf(key)) == null) {
					msgProps.setProperty(String.valueOf(key), properties.get(key));
				}
			}
		}
		return message;
	}
}