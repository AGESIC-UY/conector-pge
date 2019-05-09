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

import biz.ideasoft.soa.esb.saml.CachedSamlToken;
import biz.ideasoft.soa.esb.saml.SamlTokenCache;
import biz.ideasoft.soa.esb.saml.SamlTokenValidator;
import biz.ideasoft.soa.esb.util.SoapUtil;

public class RequestCachedSignedTokenAction extends RequestSignedTokenAction {

	private Logger logger = Logger.getLogger(RequestCachedSignedTokenAction.class);
	
	protected String actionInfo;
	protected String userNameTokenNameProperty;
	protected String userNameTokenPasswordProperty;
	protected String actor;
	
	public RequestCachedSignedTokenAction(ConfigTree config) {
		
		super(config);
		
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");		
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";
		
		userNameTokenNameProperty = config.getAttribute("userNameTokenNameProperty");
		userNameTokenPasswordProperty = config.getAttribute("userNameTokenPasswordProperty");
		actor = config.getAttribute("actor");
		
		
	}
	
	public Message process(Message message) throws Exception {
		
		boolean skip = (Boolean) message.getProperties().getProperty("skipSTS", Boolean.FALSE);
		skip = skip ? true : Boolean.valueOf(skipSTS);
		if (skip) {
			logger.info("---------------Skip sts invocation---------------");
			return message;
		}
		
		boolean useSamlTokenCache = (Boolean)message.getProperties().getProperty("samlTokenCache", Boolean.FALSE);
		if (useSamlTokenCache) {
		
			String connectorName = (String) message.getProperties().getProperty("name");
			boolean connectorType = (Boolean) message.getBody().get("connectorType");
			String connectorFullName = connectorName + "_" + (connectorType ? "Prod" : "Test");
			
			SamlTokenCache tokenCache = SamlTokenCache.getInstance();
			
			// obtengo el objecto que representa un conector, de manera de sincronizar el pedido de sts.
			String connector = tokenCache.getConnectorName(connectorFullName);
			
			// En caso que haya concurrencia entre el mismo conector, solo dejo pasar uno a la vez. 
			// Distintos conectores pueden acceder concurrentemente
			synchronized (connector) {
				
				SamlTokenValidator validator = new SamlTokenValidator();
				CachedSamlToken token = tokenCache.getSamlToken(connectorFullName);
				
				if (token != null) {
					
					boolean validToken = validator.checkTokenValidity(token);
	
					if (validToken) {
						message.getProperties().setProperty(XMLTOKEN_PROP_NAME, token.getToken());
						logger.info("---------------Using cached token---------------");
						
						// agrego el token al mensaje
						processToken(token.getToken(), message);
						
						
						return message;
					} else {
						logger.info("---------------Cached token expired---------------");
					}
				
				}
				
				// o bien no tenia token, o el token esta vencido
				Message stsMsg = super.process(message);
				String newToken = (String) stsMsg.getProperties().getProperty(XMLTOKEN_PROP_NAME);
				
				CachedSamlToken newCachedToken = validator.createCachedToken(newToken);
				if (newCachedToken != null) {
					tokenCache.addSamlToken(connectorFullName, newCachedToken);
				}
				
				// agrego el token al mensaje
				processToken(newCachedToken.getToken(), stsMsg);
				
				return stsMsg;
			}
			
		} else {
			// Obtengo el token y se agrega al mensaje
			Message msg = super.process(message);
			String token = (String) msg.getProperties().getProperty(XMLTOKEN_PROP_NAME);
			processToken(token, msg);
			return msg;
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
	
	@SuppressWarnings("rawtypes")
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
			
			}
			
		} catch (SOAPException e) {
			String error = actionInfo + "InsertTokenOnMessage error";
			logger.error(error, e);
			throw SoapUtil.createActionPipelineException(error, null, null, e);
		}
	}

}
