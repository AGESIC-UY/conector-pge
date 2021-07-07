package uy.gub.agesic.esb.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.ResponseHeader;
import org.jboss.soa.esb.services.registry.RegistryException;

import uy.gub.agesic.connector.PasswordManager;
import uy.gub.agesic.connector.entity.Connector;
import uy.gub.agesic.connector.entity.ConnectorPaths;
import uy.gub.agesic.connector.entity.FullConnector;
import uy.gub.agesic.connector.entity.RoleOperation;

import biz.ideasoft.soa.esb.util.SoapUtil;
import biz.ideasoft.soa.esb.util.XPathXmlDogUtil;

public class ConnectorToPropertiesAction extends AbstractActionPipelineProcessor {
	protected ConfigTree config;
	
	private static Log log = LogFactory.getLog(ConnectorToPropertiesAction.class);

	public ConnectorToPropertiesAction(ConfigTree config) throws ConfigurationException, RegistryException {
		this.config = config;
	}
	
	public Message process(Message message) throws ActionProcessingException {
		try {
			String location = "http://" + message.getProperties().getProperty("host") + message.getProperties().getProperty("Path"); 
			message.getProperties().setProperty("location", location);
			message.getProperties().setProperty("Content-Type", new ResponseHeader("Content-Type", "text/xml;charset=UTF-8"));
			FullConnector fullConnector = (FullConnector) message.getBody().remove("fullConnector");
			if (fullConnector != null) {
				Connector connector = fullConnector.getConnector();
				if (connector != null) {
					String wsaTo = connector.getWsaTo();
					String name = connector.getName();
					String userName = connector.getUsername();
					
					String aliasKeystore = connector.getAliasKeystore(); // alias Keystore organismo
					String passwordKeystoreOrg = PasswordManager.decrypt(connector.getPasswordKeystoreOrg()); // password Keystore organismo
					String passwordKeystore = PasswordManager.decrypt(connector.getPasswordKeystore()); // password keystore ssl
					String passwordKeystoreSsl = PasswordManager.decrypt(connector.getPasswordKeystoreSsl()); // password truststore
					
					String issuer = connector.getIssuer();
					String policyName = connector.getPolicyName();
					String userNameTokenName = connector.getUserNameTokenName();
					String userNameTokenPassword = PasswordManager.decrypt(connector.getUserNameTokenPassword());
					String wsaAction = "";
					String rolOperation = "";
					String soapAction = "";
					List<RoleOperation> roles = connector.getRole_operation();
					
					boolean samlTokenCache = connector.isEnableCacheTokens();
					
					if (roles != null && roles.size() > 0) {
						LinkedList<String> xpaths = new LinkedList<String>();
				   		xpaths.add("local-name(/soapenv:Envelope/soapenv:Body/child::*[1])");
				   		HashMap<String, String> namespaces = new HashMap<String, String>();
						namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");  
						XPathXmlDogUtil xmlDogUtil = new XPathXmlDogUtil();
						List<Object> operation = xmlDogUtil.executeMultipleXPath(message, xpaths, namespaces);
						if (operation != null && operation.size() > 0) {
							String operationName = operation.get(0).toString();
							int size = roles.size();
							int count = 0;
							for (count = 0; count < size; count ++) {
								if (roles.get(count).getOperation().equals(operationName)) {
									rolOperation = roles.get(count).getRole();
									wsaAction = roles.get(count).getWsaAction();
									if (roles.get(count).getSoapAction() != null) {
										soapAction = roles.get(count).getSoapAction();
									}
								}
							}
						}
					}
					String url = connector.getUrl();
					if (wsaTo != null) {
						message.getProperties().setProperty("wsaTo", wsaTo);
					}
					if (!wsaAction.equals("")) {
						message.getProperties().setProperty("wsaAction", wsaAction);
					}
					
					message.getProperties().setProperty("soapAction", soapAction);
					
					if (name != null) {
						message.getProperties().setProperty("name", name);
					}
					if (userName != null) {
						message.getProperties().setProperty("userName", userName);
					}
					if (aliasKeystore != null) {
						message.getProperties().setProperty("alias", aliasKeystore);
					}
					if (passwordKeystoreOrg != null) {
						message.getProperties().setProperty("keyStoreOrgPwd", passwordKeystoreOrg);
					}
					if (passwordKeystore != null) {
						message.getProperties().setProperty("keyStorePwd", passwordKeystore);
					}
					if (passwordKeystoreSsl != null) {
						message.getProperties().setProperty("trustStorePwd", passwordKeystoreSsl);
					}
					if (issuer != null) {
						message.getProperties().setProperty("issuer", issuer);
					}
					if (policyName != null) {
						message.getProperties().setProperty("policyName", policyName);
					}
					if (userNameTokenName != null) {
						message.getProperties().setProperty("userNameTokenName", userNameTokenName);
					}
					if (userNameTokenPassword != null) {
						message.getProperties().setProperty("userNameTokenPassword", userNameTokenPassword);
					}					
					if (rolOperation != null && !rolOperation.equals("")) {
						message.getProperties().setProperty("role", rolOperation);
					} else {
						message.getProperties().setProperty("role", issuer);
					}
					if (url != null) {
						message.getProperties().setProperty("routePhysicalURL", url);
					}
					message.getProperties().setProperty("samlTokenCache", samlTokenCache);
				}
				
				ConnectorPaths paths = fullConnector.getConnectorPaths();
				if (paths != null) {
					String keystoreOrg = paths.getKeystoreOrg();
					if (keystoreOrg != null) {
						message.getProperties().setProperty("keyStoreOrgFilePath", keystoreOrg);
					}
					String keystore = paths.getKeystore();
					if (keystore != null) {
						message.getProperties().setProperty("keyStoreFilePath", keystore);
					}
					String keystoreSsl = paths.getKeystoreSsl();
					if (keystoreSsl != null) {	
						message.getProperties().setProperty("trustStoreFilePath", keystoreSsl);
					}
				}
			}
	
			LinkedList<String> xpaths = new LinkedList<String>();
			xpaths.add("/soapenv:Envelope/soapenv:Header/pge:MessageType/text() = 'TEST'");
			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
			namespaces.put("pge", "http://pge.agesic.gub.uy/MessageType/");
			XPathXmlDogUtil xmlDogUtil = new XPathXmlDogUtil();
			List<Object> elements = xmlDogUtil.executeMultipleXPath(message, xpaths, namespaces);
			boolean existElement = elements != null && elements.size() > 0 && elements.get(0) != null;
			boolean skipSTS = false;
			if (existElement) {
				if (elements.get(0) instanceof Boolean) {
					skipSTS = (Boolean) elements.get(0);
				}
			}
			message.getProperties().setProperty("skipSTS", skipSTS);
		} catch (Exception e) {
			
			log.error(e.getMessage(),e);
			String errorMsg = "Error al pasar los datos del Conector a properties del mensaje ESB";
			
			if (!(e instanceof NullPointerException)) {
				errorMsg += " - " + e.getLocalizedMessage();
        	}
			
			throw SoapUtil.createActionPipelineException(errorMsg, null, SoapUtil.SOAP_NAME_TYPE, e);
		}
		return message;
	}
}