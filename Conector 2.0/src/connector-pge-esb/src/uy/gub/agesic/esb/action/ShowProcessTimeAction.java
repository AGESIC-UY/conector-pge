package uy.gub.agesic.esb.action;

import java.net.URI;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.BeanConfiguredAction;
import org.jboss.soa.esb.message.Message;

public class ShowProcessTimeAction extends AbstractActionPipelineProcessor implements BeanConfiguredAction {
	public static final Logger log = Logger.getLogger(ShowProcessTimeAction.class); 

	public ShowProcessTimeAction() {
	}
	
	public Message process(Message message) throws ActionProcessingException {
		if (log.isInfoEnabled()) {
			Object body = message.getBody().get("currentInitialElapsedTime");
			long elapsedTotalTime = body != null ? (Long) body : 0;
			
			body = message.getBody().get("currentStsElapsedTime");
			long elapsedStsTime = body != null ? (Long) body : 0;
	
			body = message.getBody().get("currentWSElapsedTime");
			long elapsedWSTime = body != null ? (Long) body : 0;
			
			URI msgID = message.getHeader().getCall().getMessageID();
			log.info("[" + msgID + "] " + "Total elpased time in millis: " + elapsedTotalTime);
			log.info("[" + msgID + "] " + "Sts elpased time in millis: " + elapsedStsTime);
			log.info("[" + msgID + "] " + "WS elpased time in millis: " + elapsedWSTime);
			log.info("[" + msgID + "] " + "ESB overhead time in millis: " + (elapsedTotalTime - elapsedWSTime - elapsedStsTime));
		}
		return message;
	}
	
}