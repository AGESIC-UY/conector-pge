package uy.gub.agesic.soa.logger.esb.actions;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;

public class MessageValidator extends AbstractActionLifecycle {

	protected ConfigTree _config;
	private Logger logger = Logger.getLogger("uy.gub.agesic.soa.wsa.esb.logger.MessageValidator.class");
	
	public MessageValidator(ConfigTree config) {
		_config = config; 
	}
	
	public Message process(Message message) throws ActionProcessingException {
		
		// check if all properties needed for creating the trace are present
		Properties properties = message.getProperties();
		
		if (properties.getProperty("origin") == null) {
			logger.error("ERROR: Origin property was not found in the message");
		
		} else {
			if (!properties.getProperty("origin").equals("R") && !properties.getProperty("origin").equals("PS") 
					&& !properties.getProperty("origin").equals("O")) {
				logger.error("ERROR: Origin property defined is unknown");
			}
		}
		
		if (properties.getProperty("msgType") == null) {
			logger.error("ERROR: Message Type property was not found in the message");
		
		} else {
			if (!properties.getProperty("msgType").equals("I") && !properties.getProperty("msgType").equals("E") 
					&& !properties.getProperty("msgType").equals("D")) {
				logger.error("ERROR: Message Type property defined is unknown");
			}
		}
		
		if (properties.getProperty("logTxnId") == null) {
			logger.error("ERROR: Log transaction id property was not found in the message");
		}
		 
		if (properties.getProperty("msgType") != null && properties.getProperty("msgType").equals("E")) {
			if (properties.getProperty("msgErrorCode") == null) {
				logger.error("ERROR: Error Code property was not found in the message");
			}
		}
		
		return message;
		
	}
	
}
