package biz.ideasoft.soa.esb.actions.errors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;

import org.apache.log4j.Logger;
import org.jboss.internal.soa.esb.util.StreamUtils;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.Properties;
import org.jboss.soa.esb.message.format.MessageFactory;
import org.xml.sax.InputSource;

import biz.ideasoft.soa.esb.actions.logger.AgesicLoggerConstants;
import biz.ideasoft.soa.esb.actions.logger.ErrorPropertiesHandler;

import biz.ideasoft.soa.esb.util.SoapUtil;

public abstract class MessageErrorHandler extends AbstractActionPipelineProcessor {

	protected ConfigTree _config;

	private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
	private XMLDog dog;
	private String origin;
	private String intermediate;
	
	private String serviceCategory;
	private String serviceName;

	private ServiceInvoker invoker;

	private static Logger log = Logger.getLogger(MessageErrorHandler.class);
	
	public static final String PREFIX_UUID = "uuid:";

	
	public MessageErrorHandler(ConfigTree config) throws ActionProcessingFaultException {
		_config = config;
		origin = config.getAttribute(AgesicLoggerConstants.MESSAGE_ORIGIN);
		intermediate = config.getAttribute(AgesicLoggerConstants.MESSAGE_INTERMEDIATE);

		// me fijo el servicio de logging a utilizar
		boolean loggerJMS = Boolean.parseBoolean(config.getAttribute("loggerJMS", "false"));
		if (loggerJMS) {
			serviceCategory = config.getAttribute("serviceCategoryJMS", "AgesicPlataforma");
			serviceName = config.getAttribute("serviceNameJMS", "ConnectorLoggerJMS");
		} else {
			serviceCategory = config.getAttribute("serviceCategory", "AgesicPlataforma");
			serviceName = config.getAttribute("serviceName", "TraceLogger");
		}
		
		try {
			invoker = new ServiceInvoker(serviceCategory, serviceName);
		} catch (MessageDeliverException e) {
			log.error(e.getLocalizedMessage(), e);
			SoapUtil.throwFaultException(e);
		}
		
		nsContext.declarePrefix("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

	}

	public Message process(Message message) {
		String messageIDWithPrefix= PREFIX_UUID + message.getHeader().getCall().getMessageID().toString();
		message.getProperties().setProperty(AgesicLoggerConstants.TRANSACTION_ID, messageIDWithPrefix);
		return message;
	}

	public abstract String getErrorDsc(Message message, String errorCode, Throwable th);
		
	public abstract void sendErrorMessage(Message message, ServiceInvoker invoker);
	
	public abstract Message prepareLoggerMessage(Message message, String errorCode, String errorDsc);
	
	protected String getErrorFromException(Throwable th) {
		return th.getMessage();		
	}
	
	
	public void processException(Message message, Throwable th) {
		
		String errorCodeStr = getErrorCodeFromException(th);
		Integer errorCode = Integer.valueOf(errorCodeStr);
		String errorDsc = getErrorDsc(message, errorCode.toString(), th);
		
		notifyError(message, th, errorCode.toString(), errorDsc);
		
		Message loggerMsg = prepareLoggerMessage(message, errorCode.toString(), errorDsc);
		sendErrorMessage(loggerMsg, invoker);
		
	}

	protected void notifyError(Message message, Throwable th, String errorCode, String errorDsc) {
		
		try {
				
			TransformerFactory transFact = TransformerFactory.newInstance();
			
			// cargo el template
			InputStream stream = StreamUtils.getResource("/xsl/ErrorTemplate.xsl");
			StreamSource transformSource = new StreamSource(stream);
			Transformer transformer = transFact.newTransformer(transformSource);
			
			String xmlDummy = "<dummy/>";
			ByteArrayInputStream in = new ByteArrayInputStream(xmlDummy.getBytes());
			Source source = new StreamSource(new InputStreamReader(in, "UTF-8"));
			
			// properties que se cargan para la transformacion..
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			
			// stream donde se va poner el resultado
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			OutputStreamWriter resultWriter = new OutputStreamWriter(outputStream, "UTF-8");
			StreamResult transformResult = new StreamResult(resultWriter);
			
			Properties prop = message.getProperties();
			
			String wsaFaultMessageID = PREFIX_UUID + UUID.randomUUID().toString();
			prop.setProperty(AgesicLoggerConstants.FAULT_MESSAGE_ID, wsaFaultMessageID);
			
			String wsaRelatesTo = "";
			if (prop.getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO) != null) {
				String[] list = (String[]) prop.getProperty(AgesicLoggerConstants.MESSAGE_RELATES_TO); 
				
//				String[] wsaRelatesToList = Arrays.copyOf(list, list.length, String[].class);
				StringBuilder relatesBuilder = new StringBuilder();
				for(String item: list) {
					relatesBuilder.append(item);
					relatesBuilder.append(",");
				}
				
				// agrego el wsaMessageID original del mensaje si no es un error de MessageID
				if (prop.getProperty(AgesicLoggerConstants.MESSAGE_ID) != null){
					relatesBuilder.append(prop.getProperty(AgesicLoggerConstants.MESSAGE_ID).toString());
				}
				wsaRelatesTo = relatesBuilder.toString();
				
			} else {
				if (prop.getProperty(AgesicLoggerConstants.MESSAGE_ID) != null){
					wsaRelatesTo = prop.getProperty(AgesicLoggerConstants.MESSAGE_ID).toString();
				}
			}
			
			synchronized (transformer) {
				transformer.setParameter("faultString", errorCode + " " + errorDsc);
				transformer.setParameter("intermediate", intermediate);
				transformer.setParameter("relatesTo", wsaRelatesTo);
				transformer.setParameter("messageId", wsaFaultMessageID);
				transformer.transform(source, transformResult);
			}
			
			String faultMessageString = new String(outputStream.toByteArray());
			
			Message msg = ((ActionProcessingFaultException) th).getFaultMessage();
			msg.getBody().add(faultMessageString);
			
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	 
	protected String getSharedErrorDsc(String errorCode, Throwable th) {
		
		String faultDsc = "";
		int errorCodeNum = Integer.parseInt(errorCode);
		String errorDsc = ErrorPropertiesHandler.getInstance().getProperty(errorCode);
		
		if (getErrorFromException(th) != null && !getErrorFromException(th).equalsIgnoreCase("null") 
				&& !getErrorFromException(th).equalsIgnoreCase("java.lang.NullPointerException")
				&& !getErrorFromException(th).contains("NullPointerException")){
			faultDsc = getErrorFromException(th);
		}
		
		switch (errorCodeNum) {
		
			case AgesicLoggerConstants.INVALID_URL:
			case AgesicLoggerConstants.CERTIFICATE_ERROR_COD:
			case AgesicLoggerConstants.IO_ERROR_CODE:
			case AgesicLoggerConstants.GET_KEY_ERROR_COD:
			case AgesicLoggerConstants.GET_KEYSTORE_ERROR_COD:
			case AgesicLoggerConstants.ERROR_CREATE_RST_MESSAGE_COD:
			case AgesicLoggerConstants.PARSE_SECURITY_TOKEN_MESSAGE_COD:
			case AgesicLoggerConstants.ASSERTION_NOT_FOUND_COD:
			case AgesicLoggerConstants.UNMARSHAL_SECURITY_TOKEN_MESSAGE_COD:
			case AgesicLoggerConstants.GENERIC_ERROR_STS_INVOCATION_COD:
			
				return MessageFormat.format(errorDsc, faultDsc);
				
			case AgesicLoggerConstants.NO_EXIST_MESSAGE_ID_ERROR_CODE:
				return errorDsc;
				
			 default:
				 return null;		
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private String getErrorCodeFromException(Throwable th) {
		
		try {
			
			if (!(th instanceof ActionProcessingFaultException)) {
				log.warn("Invalid Exception type processing error code.");
				return "999";
			}
			
			ActionProcessingFaultException exception = (ActionProcessingFaultException) th;
			
			String errorCode = null;
			
			dog = new XMLDog(nsContext, null, null);
			Expression errorExp = dog.addXPath("/soapenv:Envelope/soapenv:Body/soapenv:Fault/@errorCode");
			
			Message faultMessage= exception.getFaultMessage();
			if (faultMessage == null) {
				log.warn("ActionProcessingFaultException without faultMessage detected when processing error code.");
				return "999";
			}
					
			InputSource source = new InputSource(new ByteArrayInputStream(exception.getFaultMessage().getBody().get().toString().getBytes()));
			XPathResults results = dog.sniff(source);
			Object errorCodeRes = results.getResult(errorExp);
			if (errorCodeRes != null) {
				List<NodeItem> errorCodeList = (List<NodeItem>) errorCodeRes;
				if (errorCodeList.size() > 0) {
					errorCode = errorCodeList.get(0).value;	
				} else {
					log.error("No error code found. \"999\" returned");
					return "999";
				}
				
			}
			
			return errorCode;
			
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			return "999";
		}
		
	}
	
	protected Message prepareBasicLoggerMessage(Message message, String errorCode, String errorDsc) {
		
		Message loggerMsg = MessageFactory.getInstance().getMessage(); 
		Properties msgProperties = message.getProperties();
		Properties loggerMsgProperties = loggerMsg.getProperties();

		if (origin != null) {
			loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ORIGIN, origin);
		}

		loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_TYPE, "E");

		String transactionID = (String) msgProperties.getProperty(AgesicLoggerConstants.TRANSACTION_ID);
		if (transactionID != null) {
			loggerMsgProperties.setProperty(AgesicLoggerConstants.TRANSACTION_ID, transactionID);
		}

		String wsaMessageID = (String) msgProperties.getProperty(AgesicLoggerConstants.MESSAGE_ID);
		if (wsaMessageID != null) {
			loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ID, wsaMessageID);
		}
		
		String additionalInformation = (String) msgProperties.getProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER);
		if (message.getProperties().getProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER) != null) {
			loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ADDITIONAL_INFORMATION_LOGGER, additionalInformation);
		}

		loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_ERROR_CODE, errorCode);
		loggerMsgProperties.setProperty(AgesicLoggerConstants.MESSAGE_CONTENT, errorDsc);
		
		String wsaFaultMessageID = (String) msgProperties.getProperty(AgesicLoggerConstants.FAULT_MESSAGE_ID);
		if (wsaFaultMessageID != null) {
			loggerMsgProperties.setProperty(AgesicLoggerConstants.FAULT_MESSAGE_ID, wsaFaultMessageID);
		}
		
		return loggerMsg;
	}
	
	protected void sendAsyncErrorMessage(Message loggerMsg, ServiceInvoker invoker) {
		
		try {
			
			loggerMsg.getHeader().getCall().setReplyTo(null);
			invoker.deliverAsync(loggerMsg);
			
		} catch (MessageDeliverException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}
	
}
