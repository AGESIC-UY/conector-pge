package uy.gub.agesic.esb.action.logger;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;


public class ConnectorMessageLogger extends AbstractActionLifecycle {

	protected ConfigTree _config;
	private Logger logger = Logger.getLogger(ConnectorMessageLogger.class);
	
	public ConnectorMessageLogger(ConfigTree config) {
		_config = config; 
	} 
	
	public Message process(Message message) {
		
		Properties prop = message.getProperties();
		
		String msgType = (String) prop.getProperty(ConnectorLoggerConstants.MESSAGE_TYPE);
		if (msgType != null) {
			// determine kind of message (I - Initial message, E - Error, D - Debug)
			if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_TYPE).equals("I")) {
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
		String connectorName = getConnectorName(prop);
		String transactionID = getTransactionID(prop);
		String wsaRelatesTo = getMessageRelatesTo(prop);
		
		try {
			MDC.put("origin", origin);
			MDC.put("connectorName", connectorName);
			MDC.put("transactionID", transactionID);
			MDC.put("messageID", wsaMessageID);
			MDC.put("relatesTo", wsaRelatesTo);
			MDC.put("errorCode", "");
			MDC.put("message", "");
			
			logger.info("");
			
		} finally {
			MDC.remove("origin");
			MDC.remove("connectorName");
			MDC.remove("transactionID");
			MDC.remove("messageID");
			MDC.remove("relatesTo");
			MDC.remove("errorCode");
			MDC.remove("message");
		}	
	}
	
	private void logMessage(Properties prop) {
		
		String wsaMessageID = getMessageID(prop);
		String origin = getMessageOrigin(prop);
		String connectorName = getConnectorName(prop);
		String transactionID = getTransactionID(prop);
		String errorCode = getErrorCode(prop);
		String messageContent = getMessageContent(prop);
		
		try {
			MDC.put("origin", origin);
			MDC.put("connectorName", connectorName);
			MDC.put("transactionID", transactionID);
			MDC.put("messageID", wsaMessageID);
			MDC.put("relatesTo", "");
			MDC.put("errorCode", errorCode);
			MDC.put("message", messageContent);
			
			if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_TYPE).equals("D")) {
				logger.debug("");
			} else {
				logger.error("");
			}
			
		} finally {
			MDC.remove("origin");
			MDC.remove("connectorName");
			MDC.remove("transactionID");
			MDC.remove("messageID");
			MDC.remove("relatesTo");
			MDC.remove("errorCode");
			MDC.remove("message");
		}	
	}
	
	private String getMessageID(Properties prop) {
		if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_ID) != null) {
			return "[" + (String) prop.getProperty(ConnectorLoggerConstants.MESSAGE_ID) + "]";
		} else {
			return "";
		}
	}
	
	private String getMessageOrigin(Properties prop) {
		if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_ORIGIN) != null) {
			return "[" + (String) prop.getProperty(ConnectorLoggerConstants.MESSAGE_ORIGIN) + "]";
		} else {
			return "";
		}
	}
	
	private String getConnectorName(Properties prop) {
		String connectorType = null;
		if (prop.getProperty(ConnectorLoggerConstants.CONNECTOR_TYPE) != null) {
			connectorType = (Boolean) prop.getProperty(ConnectorLoggerConstants.CONNECTOR_TYPE) ? "PROD" : "TEST";
		} else {
			connectorType = "";
		}
		
		String connectorName = null;
		if (prop.getProperty(ConnectorLoggerConstants.CONNECTOR_NAME) != null) {
			connectorName = (String) prop.getProperty(ConnectorLoggerConstants.CONNECTOR_NAME);
		} else  {
			connectorName = "";
		}
		
		return "[" + connectorType + " " + connectorName + "]";
	}
	
	private String getTransactionID(Properties prop) {
		if (prop.getProperty(ConnectorLoggerConstants.TRANSACTION_ID) != null) {
			return "[" + (String) prop.getProperty(ConnectorLoggerConstants.TRANSACTION_ID) + "]";
		} else {
			return "";
		}
	}
	
	private String getMessageRelatesTo(Properties prop) {
		
		StringBuilder wsaRelatesTo = new StringBuilder();
		
		if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_RELATES_TO) != null) {
			Object[] list = (Object[])prop.getProperty(ConnectorLoggerConstants.MESSAGE_RELATES_TO); 
			
			//String[] wsaRelatesToList = Arrays.copyOf(list, list.length, String[].class);
			String[] wsaRelatesToList = transformToString(list);
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
	
	private String[] transformToString (Object[] list){
		String[] result = new String[list.length];
		int i = 0;
		for(Object o: list){
			String s = String.valueOf(o);
			result[i] = s;
			i++;
		}
		return result;
	}
	
	private String getErrorCode(Properties prop) {
		if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_ERROR_CODE) != null) {
			return "[" + (String) prop.getProperty(ConnectorLoggerConstants.MESSAGE_ERROR_CODE) + "]";
		} else {
			return "";
		}
	}
	
	private String getMessageContent(Properties prop) {
		if (prop.getProperty(ConnectorLoggerConstants.MESSAGE_CONTENT) != null) {
			return (String) prop.getProperty(ConnectorLoggerConstants.MESSAGE_CONTENT);
		} else {
			return "";
		}
	}
	
}