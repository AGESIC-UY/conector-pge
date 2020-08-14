package uy.gub.agesic.esb.action;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;
import org.jboss.soa.esb.message.format.MessageFactory;

import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.FullConnector;
import uy.gub.agesic.esb.action.logger.ConnectorLoggerConstants;

import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;
import biz.ideasoft.soa.esb.util.SoapUtil;

public class ConnectorLoggerAction {

	public static final Logger log = Logger.getLogger(ConnectorLoggerAction.class); 
	
	public static final String TRACE_LEVEL = "traceLevel";
	
	private String origin;
	private String serviceCategory;
	private String serviceName;
	private Level traceLevel;
	
	private ServiceInvoker invoker;
	
	public ConnectorLoggerAction(ConfigTree config) throws ActionProcessingException {
				
		origin = config.getAttribute(AgesicLoggerConstants.MESSAGE_ORIGIN);
		
		// me fijo el servicio de logging a utilizar
		boolean loggerJMS = Boolean.parseBoolean(config.getAttribute("loggerJMS", "false"));
		if (loggerJMS) {
			serviceCategory = config.getAttribute("serviceCategoryJMS", "AgesicPlataforma");
			serviceName = config.getAttribute("serviceNameJMS", "ConnectorLoggerJMS");
		} else {
			serviceCategory = config.getAttribute("serviceCategory", "AgesicPlataforma");
			serviceName = config.getAttribute("serviceName", "TraceLogger");
		}
		
		traceLevel = Level.toLevel(config.getAttribute(TRACE_LEVEL, "info"));
		
		try {
			invoker = new ServiceInvoker(serviceCategory, serviceName);
		} catch (MessageDeliverException e) {
			log.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		
	}
	
	public Message process(Message message) throws ActionProcessingException {
		
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
			} else if (transactionID != null) {
				loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ID, transactionID);
			}
			
			Object wsaRelatesTo = msgProperties.getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO);
			if (message.getProperties().getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO) != null) {
				loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO, wsaRelatesTo);
			}
			
			if (message.getBody().get("connectorType") != null) {
				loggerMsgProperties.setProperty(ConnectorLoggerConstants.CONNECTOR_TYPE, message.getBody().get("connectorType"));
			}
			
			if (message.getBody().get("fullConnector") != null) {
				Connector connector = ((FullConnector) message.getBody().get("fullConnector")).getConnector();
				loggerMsg.getProperties().setProperty(ConnectorLoggerConstants.CONNECTOR_NAME, connector.getName());
			} else {
				loggerMsg.getProperties().setProperty(ConnectorLoggerConstants.CONNECTOR_NAME, msgProperties.getProperty(ConnectorLoggerConstants.CONNECTOR_NAME));
			}
			
			loggerMsg.getHeader().getCall().setReplyTo(null);
			invoker.deliverAsync(loggerMsg);
			
		} catch (MessageDeliverException e) {
			log.error(e.getLocalizedMessage());
		}
		
		return message;
	}
	
}
