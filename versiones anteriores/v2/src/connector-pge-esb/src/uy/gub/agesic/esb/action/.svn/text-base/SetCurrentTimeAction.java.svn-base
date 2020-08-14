package uy.gub.agesic.esb.action;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.actions.BeanConfiguredAction;
import org.jboss.soa.esb.message.Message;

public class SetCurrentTimeAction extends AbstractActionPipelineProcessor implements BeanConfiguredAction {
	public static String CURRENT_TIME_BODY_NAME = "currentTime";
	public static String CURRENT_TIME_MILLIS_BODY_NAME = "currentTimeMillis";
	
	private String bodyName = CURRENT_TIME_BODY_NAME;
	private String millisBodyName = CURRENT_TIME_MILLIS_BODY_NAME;
	
	private SimpleDateFormat sdf;
	private FieldPosition fp;

	public SetCurrentTimeAction() {
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		fp = new FieldPosition(DateFormat.DATE_FIELD);
	}
	
	public void setBodyName(String n) {
		bodyName = n;
	}

	public void setMillisBodyName(String millisBodyName) {
		this.millisBodyName = millisBodyName;
	}

	public Message process(Message message) throws ActionProcessingException {
		long currentTimeMillis = System.currentTimeMillis();
		
        StringBuffer b = new StringBuffer();
        sdf.format(new Date(currentTimeMillis), b, fp);
        b.insert(b.length() - 2, ':');

		message.getBody().add(bodyName, b.toString());
		message.getBody().add(millisBodyName, currentTimeMillis);
		
		return message;
	}
	
}