package biz.ideasoft.soa.esb.actions;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.MessagePayloadProxy;
import org.jboss.soa.esb.message.body.content.BytesBody;
import org.jboss.soa.esb.util.Util;

/**
 * Simple action that logs a message contents using a Log4J logger and the specified log level (debug by default).
 * 
 * @author martin
 */
public class LogMessageAction extends AbstractActionPipelineProcessor
{
	public static final Logger log = Logger.getLogger(LogMessageAction.class); 
	public static final String PRE_MESSAGE = "message";
	public static final String FULL_MESSAGE = "printfull";
	public static final String BINARY_MESSAGE = "printBinary";
    public static final String LOG_LEVEL = "level";
    public static final String DEFAULT_PRE_MESSAGE = "Message structure";
    
    private MessagePayloadProxy payloadProxy;
    private Level level;
	private String printlnMessage;
	private boolean printFullMessage;
	private boolean printBinaryMessage;
	
	private String serviceName;
	private String serviceCategory;
	private String action;

	public LogMessageAction(ConfigTree config)	{
		action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		serviceName = configParent.getAttribute("service-name");
		serviceCategory = configParent.getAttribute("service-category");

		printlnMessage = config.getAttribute(PRE_MESSAGE, DEFAULT_PRE_MESSAGE);
		printFullMessage = (config.getAttribute(FULL_MESSAGE, "false").equalsIgnoreCase("true") ? true : false);
		printBinaryMessage = (config.getAttribute(BINARY_MESSAGE, "false").equalsIgnoreCase("true") ? true : false);
		level = Level.toLevel(config.getAttribute(LOG_LEVEL, "debug"));

        String primaryDataLocation = config.getAttribute("datalocation");
        if(primaryDataLocation != null) {
            config.setAttribute(MessagePayloadProxy.GET_PAYLOAD_LOCATION, primaryDataLocation);
            payloadProxy = new MessagePayloadProxy(config);
        } else {
            payloadProxy = new MessagePayloadProxy(config, new String[] {BytesBody.BYTES_LOCATION}, new String[] {BytesBody.BYTES_LOCATION});
        }
        payloadProxy.setNullGetPayloadHandling(MessagePayloadProxy.NullPayloadHandling.LOG);
    }

	public Message process(Message message) throws ActionProcessingException {
		if (!log.isEnabledFor(level)) {
			return message;
		}
		
        Object messageObject = null;
        try {
            messageObject = payloadProxy.getPayload(message);
        } catch (MessageDeliverException e) {
            log.error(e.getLocalizedMessage(), e);
        	throw new ActionProcessingException(e);
        }

        StringWriter sw = new StringWriter();
        PrintWriter stream = new PrintWriter(sw);
        URI msgID = message.getHeader().getCall().getMessageID();
        stream.println("["+ serviceCategory + " - " + serviceName + " - " + action + "] " + " [" + msgID + "] " + printlnMessage + ": ");

        String messageStr = null;
		
		if (printFullMessage && (message != null)) {
			// the message should be responsible for converting itself to a string
            messageStr = message.toString();
			stream.println("[ "+messageStr+" ]");
            
		} else {
			if (messageObject instanceof byte[]) {
				messageStr = printBinaryMessage ? Util.format(new String((byte[]) messageObject)) : "-- Binary Message --";
				stream.println("[" + messageStr + "].");
			} else {
				if (messageObject != null) {
                    messageStr = Util.format(messageObject.toString());
					stream.println("[" + messageStr + "].");
                }
				for (int i = 0; i < message.getAttachment().getUnnamedCount(); i++) {
					Message attachedMessage = (Message) message.getAttachment()
							.itemAt(i);
                    try {
                        Object payload = payloadProxy.getPayload(attachedMessage);
                        if(payload instanceof byte[]) {
                            stream.println("attachment " + i + ": ["
                                    + new String((byte[]) payload)
                                    + "].");
                        } else {
                            stream.println("attachment " + i + ": ["
                                    + payload
                                    + "].");
                        }
                    } catch (MessageDeliverException e) {
                    	log.error(e.getLocalizedMessage(), e);
                    	throw new ActionProcessingException(e);
                    }
                }
			}
		}
		
		stream.flush();
		
		log.log(level, sw.toString());
		return message;
	}

	public Level getLevel() {
		return level;
	}
	
}