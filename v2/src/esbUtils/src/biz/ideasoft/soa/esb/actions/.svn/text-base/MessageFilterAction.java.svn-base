package biz.ideasoft.soa.esb.actions;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import biz.ideasoft.soa.esb.util.SoapUtil;

public class MessageFilterAction extends AbstractActionPipelineProcessor {
	
	private ConfigTree config;
	private int maxLength = -1;
	public static final Logger log = Logger.getLogger(MessageFilterAction.class); 

	public MessageFilterAction(ConfigTree config)	{
		this.config = config;
		String maxLength = config.getAttribute("maxLength", "-1");
		try {
			this.maxLength = Integer.parseInt(maxLength);
		} catch (NumberFormatException e) {			
			log.warn(e.getLocalizedMessage(), e);
		}

	}
	
	public Message process(Message message) throws ActionProcessingException {
		if (maxLength < 0) {
			return message;
		}
		Object payload = message.getBody().get();
		if (payload instanceof byte[]) {
			int length = ((byte[]) payload).length;
			if (length <= maxLength) {
				return message;
			}
		} else if (payload instanceof String) {
			int length = payload.toString().length();
			if (length <= maxLength) {
				return message;
			}
		}
		
		throw SoapUtil.createActionPipelineException("Invalid message size", null, null, null);
	}

}
