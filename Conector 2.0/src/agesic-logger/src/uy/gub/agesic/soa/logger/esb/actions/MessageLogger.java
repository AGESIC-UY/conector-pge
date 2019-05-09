package uy.gub.agesic.soa.logger.esb.actions;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;

import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;

public class MessageLogger extends AbstractActionLifecycle {

	protected ConfigTree _config;
	private Logger logger = Logger.getLogger("uy.gub.agesic.soa.logger.esb.actions.MessageLogger.class");
	
	public MessageLogger(ConfigTree config) {
		_config = config; 
	} 
	
	public Message process(Message message) {
		
		Properties prop = message.getProperties();
		
		String msgType = (String) prop.getProperty(AgesicLoggerConstants.MESSAGE_TYPE);
		if (msgType != null) {
			// determine kind of message (I - Initial message, E - Error, D - Debug)
			if (prop.getProperty(AgesicLoggerConstants.MESSAGE_TYPE).equals("I")) {
				logInitialMessage(prop);
			} else {
				logMessage(prop);
			}
		} else {
			logger.error("ERROR: Message Type property was not found in the message");
		}
		
		return message;
	}
	
	private void logInitialMessage(Properties prop) {
		
		String wsaMessageID = getMessageID(prop);
		String origin = getMessageOrigin(prop);
		String transactionID = getTransactionID(prop);
		String wsaRelatesTo = getMessageRelatesTo(prop);
		String additionalInformation = getAdditionalInformation(prop);
		
		try {
			MDC.put("origin", origin);
			MDC.put("transactionID", transactionID);
			MDC.put("messageID", wsaMessageID);
			MDC.put("relatesTo", wsaRelatesTo);
			MDC.put("errorCode", "");
			MDC.put("message", "");
			MDC.put("additionalInformation", additionalInformation);
			
			logger.info("");
			
		} finally {
			MDC.remove("origin");
			MDC.remove("transactionID");
			MDC.remove("messageID");
			MDC.remove("relatesTo");
			MDC.remove("errorCode");
			MDC.remove("message");
			MDC.remove("additionalInformation");
		}	
	}
	
	private void logMessage(Properties prop) {
		
		String origin = getMessageOrigin(prop);
		String transactionID = getTransactionID(prop);
		String errorCode = getErrorCode(prop);
		String messageContent = getMessageContent(prop);
		String additionalInformation = getAdditionalInformation(prop);
		
		try {
			MDC.put("origin", origin);
			MDC.put("transactionID", transactionID);
			MDC.put("relatesTo", "");
			MDC.put("errorCode", errorCode);
			MDC.put("message", messageContent);
			MDC.put("additionalInformation", additionalInformation);
			
			if (prop.getProperty(AgesicLoggerConstants.MESSAGE_TYPE).equals("D")) {
				String wsaMessageID = getMessageID(prop);
				MDC.put("messageID", wsaMessageID);
				logger.debug("");
				
			} else {
				String wsaFaultMessageID = getFaultMessageID(prop);
				MDC.put("messageID", wsaFaultMessageID);
				logger.error("");
				
			}
			
		} finally {
			MDC.remove("origin");
			MDC.remove("transactionID");
			MDC.remove("messageID");
			MDC.remove("relatesTo");
			MDC.remove("errorCode");
			MDC.remove("message");
			MDC.remove("additionalInformation");
		}	
	}
	
	private String getMessageID(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.MESSAGE_ID) != null) {
			return "[" + (String) prop.getProperty(AgesicLoggerConstants.MESSAGE_ID) + "]";
		} else {
			return "";
		}
	}
	
	private String getFaultMessageID(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.FAULT_MESSAGE_ID) != null) {
			return "[" + (String) prop.getProperty(AgesicLoggerConstants.FAULT_MESSAGE_ID) + "]";
		} else {
			return "";
		}
	}
	
	private String getMessageOrigin(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.MESSAGE_ORIGIN) != null) {
			return "[" + (String) prop.getProperty(AgesicLoggerConstants.MESSAGE_ORIGIN) + "]";
		} else {
			return "";
		}
	}
	
	private String getTransactionID(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.TRANSACTION_ID) != null) {
			return "[" + (String) prop.getProperty(AgesicLoggerConstants.TRANSACTION_ID) + "]";
		} else {
			return "";
		}
	}
	
	private String getMessageRelatesTo(Properties prop) {
		
		StringBuilder wsaRelatesTo = new StringBuilder();
		
		if (prop.getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO) != null) {
			Object[] list = (Object[])prop.getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO); 
			
			String[] wsaRelatesToList = Arrays.copyOf(list, list.length, String[].class);
			for(String item: wsaRelatesToList) {
				wsaRelatesTo.append("[" + item + "]");
				wsaRelatesTo.append(" ");
			}
			
			// quito el ultimo espacio generado
			wsaRelatesTo = wsaRelatesTo.deleteCharAt(wsaRelatesTo.length() - 1);
			
		} else {
			wsaRelatesTo.append("");
		}
		
		return wsaRelatesTo.toString();
	}
	
	private String getErrorCode(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.MESSAGE_ERROR_CODE) != null) {
			return "[" + (String) prop.getProperty(AgesicLoggerConstants.MESSAGE_ERROR_CODE) + "]";
		} else {
			return "";
		}
	}
	
	private String getMessageContent(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.MESSAGE_CONTENT) != null) {
			return (String) prop.getProperty(AgesicLoggerConstants.MESSAGE_CONTENT);
		} else {
			return "";
		}
	}
	
	private String getAdditionalInformation(Properties prop) {
		if (prop.getProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER) != null) {
			return "[" + (String) prop.getProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER) + "]";
		} else {
			return "";
		}
	}
	
	

}