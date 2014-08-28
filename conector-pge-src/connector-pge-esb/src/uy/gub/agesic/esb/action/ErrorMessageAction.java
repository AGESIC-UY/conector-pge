package uy.gub.agesic.esb.action;

import java.net.URI;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.services.registry.RegistryException;

public class ErrorMessageAction extends AbstractActionPipelineProcessor {
	protected ConfigTree config;
	protected static Logger _logger = Logger.getLogger(ErrorMessageAction.class);
	private String actionInfo;
	
	private SimpleDateFormat sdf;
	private FieldPosition fp;
	
	private List<String> bodyNames = new ArrayList<String>();

	public ErrorMessageAction(ConfigTree config) throws ConfigurationException, RegistryException {
		this.config = config;
		String action = config.getAttribute("action");
		int i = 1;
		String bodyName = config.getAttribute("body" + i);
		do {
			bodyNames.add(bodyName);
			i++;
			bodyName = config.getAttribute("body" + i);
		} while (bodyName != null);
		
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		fp = new FieldPosition(DateFormat.DATE_FIELD);		
		
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";		
	}
	
	public Message process(Message message) throws ActionProcessingException {
		return message;
	}
	
	@Override
	public void processException(Message message, Throwable th) {
		URI msgID = message.getHeader().getCall().getMessageID();
    	try {    		
    		for (String bodyName : bodyNames) {
    			if (bodyName != null) {
    			_logger.info(actionInfo + " [" + msgID + "] " + " Body: " + bodyName + " --> " + message.getBody().get(bodyName));
    			}
			}
    		
    		long currentTimeMillis = System.currentTimeMillis();    		
            StringBuffer b = new StringBuffer();
            sdf.format(new Date(currentTimeMillis), b, fp);
            b.insert(b.length() - 2, ':');
            
            _logger.info(actionInfo + " [" + msgID + "] " + " ActualTime: " + b);            
            _logger.info(actionInfo + " [" + msgID + "] " + " ActualMillis: " + currentTimeMillis);            
		} catch (Exception e) {
			String error = actionInfo + " [" + msgID + "] " + e.getLocalizedMessage();
			_logger.error(error, e);			
		}
		super.processException(message, th);
	}
	
	public void initialise() throws ActionLifecycleException {
		super.initialise();
	}

	public void destroy() throws ActionLifecycleException {
		super.destroy();
	}

}