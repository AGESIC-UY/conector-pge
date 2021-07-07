package uy.gub.agesic.soa.wsa.esb.actions.sts.v2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionProcessingFaultException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Body;
import org.jboss.soa.esb.message.Message;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.w3c.dom.Element;

import uy.gub.agesic.AssertionManager;
import uy.gub.agesic.PGEFactory;
import uy.gub.agesic.beans.SAMLAssertion;
import uy.gub.agesic.exceptions.NoAssertionFoundException;
import uy.gub.agesic.exceptions.ParserException;
import uy.gub.agesic.exceptions.RequestSecurityTokenException;
import uy.gub.agesic.exceptions.UnmarshalException;
import uy.gub.agesic.soa.wsa.esb.actions.sts.v2.opensaml.OpenSamlBootstrap;
import biz.ideasoft.soa.esb.actions.soap.WSInvoker;
import biz.ideasoft.soa.esb.util.SoapUtil;

public class WSInvokeAction extends WSInvoker {
	private Logger logger = Logger.getLogger(WSInvokeAction.class);
	protected String actionInfo;
	protected ConfigTree _config;
	
	protected String userNameTokenNameProperty;
	protected String userNameTokenPasswordProperty;
	
	protected String actor;
	
	private String defaultResponseMessage = "empty_response";

	public WSInvokeAction(ConfigTree config) throws ConfigurationException {
		super(config);
		_config = config;
		
		String action = config.getAttribute("action");
		
		userNameTokenNameProperty = config.getAttribute("userNameTokenNameProperty");
		userNameTokenPasswordProperty = config.getAttribute("userNameTokenPasswordProperty");
		actor = config.getAttribute("actor");
		
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");		
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";

	}

	public Message process(Message message) throws ActionProcessingFaultException {
		return super.process(message);
	}
	
	protected void invokeWS(String endpoint, String soapAction, Message message) throws ActionProcessingFaultException {
		try {
			String token = (String) message.getProperties().getProperty(RequestSignedTokenAction.XMLTOKEN_PROP_NAME);
			if (token != null) {
				processToken(token, message);
			}
			super.invokeWS(endpoint, soapAction, message);

		} catch (Exception e) {
			URI msgID = message.getHeader().getCall().getMessageID();
			String error = actionInfo + " [" + msgID + "] " + "InvokeWS error";
			logger.error(error, e);
			if (e instanceof ActionProcessingFaultException) {
				throw (ActionProcessingFaultException) e;
			}
			throw SoapUtil.createActionPipelineException(error, null, null, e);
		}
	}

	protected void invokeWSHTTPS(String endpoint, String soapAction, Message message) throws ActionProcessingFaultException {
		try {
			String token = (String) message.getProperties().getProperty(RequestSignedTokenAction.XMLTOKEN_PROP_NAME);
			if (token != null) {
				processToken(token, message);
			}
			
			super.invokeWSHTTPS(endpoint, soapAction, message);

		} catch (Exception e) {
			URI msgID = message.getHeader().getCall().getMessageID();
			String error = actionInfo + " [" + msgID + "] " + "InvokeWSHTTPS error";
			logger.error(error, e);
			if (e instanceof ActionProcessingFaultException) {
				throw (ActionProcessingFaultException) e;
			}
			throw SoapUtil.createActionPipelineException(error, null, null, e);
		}
	}

	
	private void processToken(String token, Message message) throws RequestSecurityTokenException, IOException, SOAPException, ActionProcessingFaultException  {
		URI msgID = message.getHeader().getCall().getMessageID();
		if (logger.isDebugEnabled()) {
			logger.debug(actionInfo + " [" + msgID + "] " + "ProcessToken : " + token);
		}
		
		if (token == null) {
			return;
		}
		
		SAMLAssertion samlAssertion;
		try {
			AssertionManager assertionBuilder = PGEFactory.getAssertionManager();
			
			OpenSamlBootstrap.bootstrap();
			
			samlAssertion = assertionBuilder.getAssertionFromSOAP(token);
			
			logger.info(actionInfo + " [" + msgID + "] "  + "Assertion was built successfully");
			if (logger.isDebugEnabled()) {
				logger.debug(actionInfo + " [" + msgID + "] "  + samlAssertion.toString());
			}
			
		} catch (ParserException e) {
			logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			throw new RequestSecurityTokenException("Unable to parse RequestSecurityTokenResponse message");
		} catch (NoAssertionFoundException e) {
			logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			throw new RequestSecurityTokenException("No assertion was found");
		} catch (UnmarshalException e) {
			logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			throw new RequestSecurityTokenException("Unmarshal error: Cannot build assertion from RequestSecurityTokenResponse message");
		} catch (org.opensaml.xml.ConfigurationException e) {
			logger.error(actionInfo + " [" + msgID + "] "  + e.getLocalizedMessage(), e);
			throw new RequestSecurityTokenException(e);
		}			
		
		MessageFactoryImpl messageFactory = new MessageFactoryImpl();
		
		Body body = message.getBody();
		String xmlEnvelope = body.get().toString();
		
		InputStream is = new ByteArrayInputStream(xmlEnvelope.getBytes("UTF-8"));
		SOAPMessage soapMessage = messageFactory.createMessage(null, is);
		
		insertTokenOnMessage(samlAssertion, soapMessage, message);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		soapMessage.writeTo(out);
		String msg = new String(out.toString());
		
		if (logger.isDebugEnabled()) {
			logger.debug(actionInfo + " [" + msgID + "] "  + "Mensaje con token : " + msg);
		}
				
		body.add(msg);
		
	}
	
	private void insertTokenOnMessage(SAMLAssertion samlAssertion, SOAPMessage soapMessage, Message message) throws ActionProcessingFaultException {
		
		Element elemToken = samlAssertion.getDOM();
		
		try {
			SOAPHeader header = soapMessage.getSOAPHeader();
			if(header == null) {
				header = soapMessage.getSOAPPart().getEnvelope().addHeader();
			}
			
			QName securityName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse");
			Iterator iter = header.getChildElements(securityName);
			SOAPElement security = null;
			if (iter.hasNext()) {
				security = (SOAPElement) iter.next();
			} else {				
				security = header.addChildElement(securityName);
			}
			
			security.addChildElement(SOAPFactory.newInstance().createElement(elemToken));
			
			if (actor != null) {
				QName envelopeQName = soapMessage.getSOAPPart().getEnvelope().getElementQName();
				QName actorQName = new QName(envelopeQName.getNamespaceURI(), "actor", envelopeQName.getPrefix());
				security.addAttribute(actorQName, actor);
			}
			
			if (userNameTokenNameProperty != null && userNameTokenPasswordProperty != null) {
				String userName = (String) message.getProperties().getProperty(userNameTokenNameProperty);
				String password = (String) message.getProperties().getProperty(userNameTokenPasswordProperty);
				if (userName != null && userName.trim().length() > 0 && password != null && password.trim().length() > 0) {
					QName usernameTokenName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UsernameToken", "wsse");
					SOAPElement securityToken = header.addChildElement(securityName);
					SOAPElement usernameToken = securityToken.addChildElement(usernameTokenName);
					
					QName usernameName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Username", "wsse");
					QName passwordName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Password", "wsse");
					
					usernameToken.addChildElement(usernameName).addTextNode(userName);
					usernameToken.addChildElement(passwordName).addTextNode(password);					
				}
				
				/*
				<wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
				   <wsse:UsernameToken>
				    <wsse:Username>sample</wsse:Username>
				    <wsse:Password >oracle</wsse:Password>
				   </wsse:UsernameToken>
				  </wsse:Security>
			    */
			}
		} catch (SOAPException e) {
			String error = actionInfo + "InsertTokenOnMessage error";
			logger.error(error, e);
			throw SoapUtil.createActionPipelineException(error, null, null, e);
		}
	}

}