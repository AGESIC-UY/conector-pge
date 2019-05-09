package biz.ideasoft.soa.esb.actions.logger;

import org.apache.log4j.Level;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;
import org.jboss.soa.esb.message.format.MessageFactory;

import biz.ideasoft.soa.esb.actions.LogMessageAction;
import biz.ideasoft.soa.esb.util.SoapUtil;

public class LoggerAction extends LogMessageAction {
	
	public static final String SEND_TRACE_MESSAGE = "sendTraceMsg";
	public static final String TRACE_LEVEL = "traceLevel";
	
	private boolean sendTraceMessage;
	private String origin;
	private String serviceCategory;
	private String serviceName;
	private Level traceLevel;
	
	private ServiceInvoker invoker;
	
	public LoggerAction(ConfigTree config) throws ActionProcessingException {
		
		super(config);
		
		sendTraceMessage = Boolean.parseBoolean(config.getAttribute(SEND_TRACE_MESSAGE, "false"));
		origin = config.getAttribute(AgesicLoggerConstants.MESSAGE_ORIGIN);
		serviceCategory = config.getAttribute("serviceCategory", "AgesicPlataforma");
		serviceName = config.getAttribute("serviceName", "TraceLogger");
		traceLevel = Level.toLevel(config.getAttribute(TRACE_LEVEL, "info"));
		
		try {
			invoker = new ServiceInvoker(serviceCategory, serviceName);
		} catch (MessageDeliverException e) {
			log.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		
	}
	
	public Message process(Message message) throws ActionProcessingException {
		
		// standard logging
		super.process(message);
		
		if (sendTraceMessage) {
			try {

				Message loggerMsg = MessageFactory.getInstance().getMessage();
				Properties msgProperties = message.getProperties();
				Properties loggerMsgProperties = loggerMsg.getProperties();
				
				if (origin != null) {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ORIGIN, origin);
				}
				
				if (traceLevel.equals(Level.INFO)) {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_TYPE, "I");
				} else {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_TYPE, "D");
				}
				
				String transactionID = (String) msgProperties.getProperty(AgesicLoggerConstants.TRANSACTION_ID);
				if (transactionID != null) {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.TRANSACTION_ID, transactionID);
				}
				
				String wsaMessageID = (String) msgProperties.getProperty(AgesicLoggerConstants.MESSAGE_ID);
				if (wsaMessageID != null) {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ID, wsaMessageID);
				}
				
				Object wsaRelatesTo = msgProperties.getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO);
				if (message.getProperties().getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO) != null) {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO, wsaRelatesTo);
				}
				
				String additionalInformation = (String) msgProperties.getProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER);
				if (message.getProperties().getProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER) != null) {
					loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER, additionalInformation);
				}
				
				loggerMsg.getHeader().getCall().setReplyTo(null);
				invoker.deliverAsync(loggerMsg);
				
			} catch (MessageDeliverException e) {
				log.error(e.getLocalizedMessage());
			}		
		}
		
		return message;
	}

}
