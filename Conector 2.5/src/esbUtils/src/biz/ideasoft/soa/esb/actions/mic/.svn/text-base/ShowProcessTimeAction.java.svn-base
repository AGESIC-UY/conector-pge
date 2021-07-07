package biz.ideasoft.soa.esb.actions.mic;

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
		Object body = message.getBody().get("currentInitialElapsedTime");
		long elapsedTotalTime = body != null ? (Long) body : 0;
		
		body = message.getBody().get("currentWSElapsedTime");
		long elapsedWSTime = body != null ? (Long) body : 0;
		
		log.info("Total elpased time in millis: " + elapsedTotalTime);
		log.info("WS elpased time in millis: " + elapsedWSTime);
		log.info("ESB overhead time in millis: " + (elapsedTotalTime - elapsedWSTime));

		return message;
	}
	
}