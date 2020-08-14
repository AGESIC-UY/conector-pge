package uy.gub.agesic.esb.error;

import java.text.MessageFormat;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;

import uy.gub.agesic.esb.action.logger.ConnectorLoggerConstants;

import biz.ideasoft.soa.esb.actions.errors.MessageErrorHandler;
import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;
import biz.ideasoft.soa.esb.actions.logger.ErrorPropertiesHandler;

public class ConnectorMessageErrorHandler extends MessageErrorHandler { 

	private static Logger log = Logger.getLogger(ConnectorMessageErrorHandler.class);
	
	public ConnectorMessageErrorHandler(ConfigTree config) throws ActionProcessingFaultException {
		super(config);
	}

	@Override
	public String getErrorDsc(Message message, String errorCodeStr, Throwable th) {
		
		// me fijo si es un error comun
		String sharedErrorDsc = getSharedErrorDsc(errorCodeStr, th);
		if (sharedErrorDsc != null) {
			return sharedErrorDsc;
			
		} else {
			
			String errorDsc = ErrorPropertiesHandler.getInstance().getProperty(errorCodeStr);
			String faultDsc = "";
			
			if (getErrorFromException(th) != null && !getErrorFromException(th).equals("null")){
				faultDsc = getErrorFromException(th);
			} else if (errorCodeStr.equals("999")) {
				// en el caso que sea un error desconocido, y que la exception no tenga mensaje, agrego el stacktrace en la respuesta
				faultDsc = ExceptionUtils.getStackTrace(th);
			}
			
			Integer errorCode = Integer.valueOf(errorCodeStr);
			if (errorCode.equals(AgesicLoggerConstants.ERROR_CONNECTION_URL)) {
				return MessageFormat.format(errorDsc, (String)message.getProperties().getProperty("routePhysicalURL"));
			} else if (errorCode.equals(AgesicLoggerConstants.NO_EXIST_MESSAGE_ID_ERROR_CODE)) {
				return errorDsc;
			} else {
				return MessageFormat.format(errorDsc, faultDsc);
			}
			
		}
		
	}

	@Override
	public void sendErrorMessage(Message loggerMsg, ServiceInvoker invoker) {
		
		try {
			
			loggerMsg.getHeader().getCall().setReplyTo(null);
			invoker.deliverAsync(loggerMsg);
			
		} catch (MessageDeliverException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		
	}

	@Override
	public Message prepareLoggerMessage(Message message, String errorCode, String errorDsc) {
		
		Message loggerMsg = prepareBasicLoggerMessage(message, errorCode, errorDsc);
		
		loggerMsg.getProperties().setProperty(ConnectorLoggerConstants.CONNECTOR_TYPE, message.getBody().get("connectorType"));
		if (message.getProperties().getProperty("name") != null) {
			loggerMsg.getProperties().setProperty(ConnectorLoggerConstants.CONNECTOR_NAME, message.getProperties().getProperty("name"));
		} else {
			loggerMsg.getProperties().setProperty(ConnectorLoggerConstants.CONNECTOR_NAME, message.getProperties().getProperty("Path"));
		}
		
		loggerMsg.getProperties().setProperty(ConnectorLoggerConstants.MESSAGE_ID, message.getProperties().getProperty(ConnectorLoggerConstants.TRANSACTION_ID));
		
		return loggerMsg;
	}
	
}
