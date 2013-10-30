package uy.gub.agesic.esb.action;

import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.BeanConfiguredAction;
import org.jboss.soa.esb.message.Message;

public class SetElapsedTimeAction extends AbstractActionPipelineProcessor implements BeanConfiguredAction {
	private String bodyName = "elapsedTime";
	private String millisBodyName = SetCurrentTimeAction.CURRENT_TIME_MILLIS_BODY_NAME;

	public SetElapsedTimeAction() {
	}
	
	public void setBodyName(String n) {
		bodyName = n;
	}

	public void setMillisBodyName(String millisBodyName) {
		this.millisBodyName = millisBodyName;
	}

	public Message process(Message message) throws ActionProcessingException {
		Object currentMillis = message.getBody().get(millisBodyName);
		if (currentMillis instanceof Long) {
			long elapsed = System.currentTimeMillis() - (Long) currentMillis;
			message.getBody().add(bodyName, elapsed);
		}
		return message;
	}
}