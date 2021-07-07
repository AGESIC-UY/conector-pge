package biz.ideasoft.soa.esb.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.ResponseHeader;
import org.jboss.soa.esb.message.format.MessageFactory;
import org.jboss.soa.esb.message.format.MessageType;
import org.jboss.ws.core.soap.SOAPMessageMarshaller;

public class SoapUtil {
	protected static Logger _logger = Logger.getLogger(SoapUtil.class);

	public static final String CLIENT_ERROR = "Client";
	public static final String SERVER_ERROR = "Server";
	public static final int WSA_NAME_TYPE = 1;
	public static final int SOAP_NAME_TYPE = 2;
		
	public static final String SOAP_ENV_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

	public static Message getFaultMessage(String faultCode, String faultString, String detail, Integer faultType, Integer errorToLog) {
		SOAPMessage soapMessage = null;
		String error = "internal Error";
		try {
			javax.xml.soap.MessageFactory msgFactory = javax.xml.soap.MessageFactory.newInstance();
			soapMessage = msgFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			SOAPFault fault = soapBody.addFault();			
			
			if (errorToLog != null){
				fault.setAttribute("errorCode", String.valueOf(errorToLog));
			}
			
			fault.setFaultCode(createName(faultCode, faultType));
			fault.setFaultString(faultString);
			if (detail != null) {
				fault.addDetail().addChildElement(detail);
			}
		} catch (SOAPException e) {
			_logger.error("Can not create SoapFaultMessage", e);
			error = e.getLocalizedMessage();
		}

		SOAPMessageMarshaller marshaller = new SOAPMessageMarshaller();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			marshaller.write(soapMessage, os);
			String str = new String(os.toByteArray());
			return createBasicMessage(str);
		} catch (IOException e) {
			_logger.error("Can not parse SoapFaultMessage", e);
			error = e.getLocalizedMessage();
		}
		return createBasicMessage(error);
	}
	
	public static Message getFaultMessage(String faultCode, String faultString, String detail, Integer faultType) {
		return getFaultMessage(faultCode, faultString, detail, faultType, null);
	}

	public static Message createBasicMessage(String text) {
		Message msg = MessageFactory.getInstance().getMessage(MessageType.JBOSS_XML);
		msg.getBody().add(text);
		msg.getProperties().setProperty("Content-Type", new ResponseHeader("Content-Type", "text/xml;charset=UTF-8"));
		return msg;
	}

	public static ActionProcessingFaultException createActionPipelineException(String message, String faultCode, Integer faultType, Throwable th) {
		return createActionPipelineException(message, faultCode, faultType, th, null);
	}
	
	public static ActionProcessingFaultException createActionPipelineException(String message, String faultCode, Integer faultType, Throwable th, Integer errorToLog) {
		String cause = message == null ? th.getLocalizedMessage() : message;
		String code = faultCode == null ? SERVER_ERROR : faultCode;		
		Message faultMessage = SoapUtil.getFaultMessage(code, cause == null ? th.toString() : cause, null, faultType, errorToLog);
		ActionProcessingFaultException ape = new ActionProcessingFaultException(faultMessage, cause);
		if (th != null) {
			ape.setStackTrace(th.getStackTrace());
		}
		return ape;
	}

	public static void throwFaultException(Throwable th) throws ActionProcessingFaultException {
		throwFaultException(th,null); 
	}
	
	public static void throwFaultException(Throwable th, Integer errorToLog) throws ActionProcessingFaultException {
		if (!(th instanceof ActionProcessingFaultException)) {			
			throw SoapUtil.createActionPipelineException(null, SERVER_ERROR, SOAP_NAME_TYPE,th, errorToLog);
		}
		throw (ActionProcessingFaultException) th;
	}
	
	public static Name createName(String code, Integer type) throws SOAPException {
		type = type == null ? 0 : type;
		switch(type) {
		case WSA_NAME_TYPE:
			return SOAPFactory.newInstance().createName(code, "wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
		case SOAP_NAME_TYPE:
			return SOAPFactory.newInstance().createName(code, "env", "http://schemas.xmlsoap.org/soap/envelope/");
		default :
			return SOAPFactory.newInstance().createName(code, "env", "http://schemas.xmlsoap.org/soap/envelope/");
		}
	}
	
	public static Throwable getDeepestException(Throwable exc) {
		while (exc.getCause() != null) {
			exc = exc.getCause();
		}
		return exc;
	}
}
