package uy.gub.agesic.soa.wsa.esb.actions.sts.v2;

import java.net.URI;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import uy.gub.agesic.AgesicConstants;
import uy.gub.agesic.XMLUtils;
import uy.gub.agesic.beans.RSTBean;
import uy.gub.agesic.beans.SAMLAssertion;
import uy.gub.agesic.beans.StoreBean;
import uy.gub.agesic.exceptions.RequestSecurityTokenException;
import uy.gub.agesic.soa.wsa.esb.actions.sts.v2.opensaml.OpenSamlBootstrap;
import uy.gub.agesic.sts.client.PGEClient;
import biz.ideasoft.soa.esb.util.SoapUtil;

public class RequestSignedTokenAction extends AbstractActionLifecycle {
	private Logger logger = Logger.getLogger(RequestSignedTokenAction.class);
	
	private String actionInfo;
	
	protected ConfigTree _config;
	protected Map<String, String> namespaces;
	
	protected RSTBean bean = new RSTBean();
	protected StoreBean keyStoreOrg = new StoreBean();
	protected StoreBean keyStore = new StoreBean();
	protected StoreBean trustStore = new StoreBean();
	protected PGEClient client = new PGEClient();
	
	protected String servicePropertyName;
	protected String aliasProperty;
	protected String keyStoreOrgFilePathProperty;
	protected String keyStoreFilePathProperty;
	protected String keyStoreOrgPwdProperty;
	protected String keyStorePwdProperty;
	protected String skipSTS;
	protected String keyStoreOrgCheckValidityStr;
	
	protected String trustStoreFilePathProperty;
	protected String trustStorePwdProperty;
	
	protected String issuerProperty;
	protected String policyNameProperty;
	protected String roleProperty;
	protected String userNameProperty;
	
	protected String stsURL;
	private int timeoutMillis = 5000;
	
	public static final String XMLTOKEN_PROP_NAME = "xmlToken";
	
	public RequestSignedTokenAction(ConfigTree config) {
		_config = config;
		
		skipSTS = config.getAttribute("skipSTS", "false");
			
		String alias = config.getAttribute("alias", "cd7cb547b6229be0a95d442daae50270_be45dff3-4f56-4728-8332-77080b0c1c08");
		String keyStoreOrgFilePath = config.getAttribute("keyStoreOrgFilePath");
		String keyStoreOrgPwd = config.getAttribute("keyStoreOrgPwd");

		String keyStoreFilePath = config.getAttribute("keyStoreFilePath");
		String keyStorePwd = config.getAttribute("keyStorePwd");
		
		String trustStoreFilePath = config.getAttribute("trustStoreFilePath");
		String trustStorePwd = config.getAttribute("trustStorePwd");
		
		String issuer = config.getAttribute("issuer");
		String policyName = config.getAttribute("policyName");
		String role = config.getAttribute("role");
		String userName = config.getAttribute("userName");
		
		String service = config.getAttribute("service");
		
		stsURL = config.getAttribute("stsURL");
		
		resolveConfigProperties(config);
			
		bean.setIssuer(issuer);
		bean.setPolicyName(policyName);
		bean.setRole(role);
		bean.setUserName(userName);
		bean.setService(service);
		
		keyStoreOrg.setAlias(alias);
		keyStoreOrg.setStoreFilePath(keyStoreOrgFilePath);
		keyStoreOrg.setStorePwd(keyStoreOrgPwd);
		if (keyStoreOrgCheckValidityStr != null) {
			keyStoreOrg.setCheckValidity(Boolean.parseBoolean(keyStoreOrgCheckValidityStr));
		}

		keyStore.setStoreFilePath(keyStoreFilePath);
		keyStore.setStorePwd(keyStorePwd);
		
		trustStore.setStoreFilePath(trustStoreFilePath);
		trustStore.setStorePwd(trustStorePwd);
		
    	String timeout = config.getAttribute("timeoutMillis");
		if (timeout != null) {
			try {
				timeoutMillis = Integer.parseInt(timeout);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		String action = config.getAttribute("action");
		ConfigTree configParent = config.getParent();
		String serviceName = configParent.getAttribute("service-name");
		String serviceCategory = configParent.getAttribute("service-category");
		actionInfo = "["+ serviceCategory + " - " + serviceName + " - " + action + "] ";		
		
	}
	
	private void resolveConfigProperties(ConfigTree config) {
		aliasProperty = config.getAttribute("aliasProperty");
		keyStoreOrgFilePathProperty = config.getAttribute("keyStoreOrgFilePathProperty");
		keyStoreFilePathProperty = config.getAttribute("keyStoreFilePathProperty");
		keyStoreOrgPwdProperty = config.getAttribute("keyStoreOrgPwdProperty");
		keyStorePwdProperty = config.getAttribute("keyStorePwdProperty");
		
		trustStoreFilePathProperty = config.getAttribute("trustStoreFilePathProperty");
		trustStorePwdProperty = config.getAttribute("trustStorePwdProperty");
		
		issuerProperty = config.getAttribute("issuerProperty");
		policyNameProperty = config.getAttribute("policyNameProperty");
		roleProperty = config.getAttribute("roleProperty");
		userNameProperty = config.getAttribute("userNameProperty");
		
		servicePropertyName = config.getAttribute("service-property");
		
		keyStoreOrgCheckValidityStr = config.getAttribute("keyStoreOrgCheckValidity", "false");
	}
	
	public Message process(Message message) throws Exception {
		URI msgID = message.getHeader().getCall().getMessageID();
		try {
			boolean skip = (Boolean) message.getProperties().getProperty("skipSTS", Boolean.FALSE);
			skip = skip ? true : Boolean.valueOf(skipSTS);
			if (skip) {
				logger.info("Skip sts invocation");
				return message;
			}
			OpenSamlBootstrap.bootstrap();
			
			RSTBean rstBean = new RSTBean();
			rstBean.setIssuer(issuerProperty != null ?
					(String) message.getProperties().getProperty(issuerProperty) : bean.getIssuer());
			rstBean.setPolicyName(policyNameProperty != null ?
					(String) message.getProperties().getProperty(policyNameProperty) : bean.getPolicyName());
			rstBean.setRole(roleProperty != null ?
					(String) message.getProperties().getProperty(roleProperty) : bean.getRole());
			rstBean.setUserName(userNameProperty != null ?
					(String) message.getProperties().getProperty(userNameProperty) : bean.getUserName());
			rstBean.setService(servicePropertyName != null ? 
					(String) message.getProperties().getProperty(servicePropertyName) : bean.getService());
	
			StoreBean keyStoreOrg2 = keyStoreOrg;
			if (keyStoreOrgFilePathProperty != null) {
				keyStoreOrg2 = new StoreBean();
				keyStoreOrg2.setAlias(aliasProperty != null ?
						(String) message.getProperties().getProperty(aliasProperty) : keyStoreOrg.getAlias());
				keyStoreOrg2.setStorePwd(keyStoreOrgPwdProperty != null ?
						(String) message.getProperties().getProperty(keyStoreOrgPwdProperty) : keyStoreOrg.getStorePwd());
				keyStoreOrg2.setStoreFilePath((String) message.getProperties().getProperty(keyStoreOrgFilePathProperty));
				if (keyStoreOrgCheckValidityStr != null) {
					keyStoreOrg2.setCheckValidity(Boolean.parseBoolean(keyStoreOrgCheckValidityStr));
				}
			}

			StoreBean keyStore2 = keyStore;
			if (keyStoreFilePathProperty != null) {
				keyStore2 = new StoreBean();
				keyStore2.setStorePwd(keyStorePwdProperty != null ?
						(String) message.getProperties().getProperty(keyStorePwdProperty) : keyStore.getStorePwd());
				keyStore2.setStoreFilePath((String) message.getProperties().getProperty(keyStoreFilePathProperty));
			}
			
			if (keyStoreOrg2.getStoreFilePath() == null) {
				keyStoreOrg2.setStoreFilePath(keyStore2.getStoreFilePath());
				keyStoreOrg2.setStorePwd(keyStore2.getStorePwd());
			}
	
			StoreBean trustStore2 = trustStore;
			if (trustStoreFilePathProperty != null) {
				trustStore2 = new StoreBean();
				trustStore2.setStorePwd(trustStorePwdProperty != null ?
						(String) message.getProperties().getProperty(trustStorePwdProperty) : trustStore.getStorePwd());
				trustStore2.setStoreFilePath((String) message.getProperties().getProperty(trustStoreFilePathProperty));
			}
	
			//Authenticate to the PGE
			String stsUrl = stsURL != null 
					? (message.getProperties().getProperty(stsURL) != null ? (String) message.getProperties().getProperty(stsURL) : stsURL) 
					: AgesicConstants.STS_URL_SSL;
			SAMLAssertion assertionResponse = client.requestSecurityToken(rstBean, keyStoreOrg2, keyStore2, trustStore2, stsUrl, timeoutMillis);
			
	    	String xmlString = XMLUtils.xmlToString(assertionResponse.getDOM());
	    	message.getProperties().setProperty(XMLTOKEN_PROP_NAME, xmlString);
	    	if (logger.isDebugEnabled()) {
	    		logger.debug(actionInfo + " [" + msgID + "] " + "[RequestSignedTokenAction] TOKEN: " + xmlString);
	    	}
	    	
			return message;
		} catch (RequestSecurityTokenException e) {
			logger.error(e.getMessage(), e);
			String error = e.getMessage();
        	
			// obtengo el codigo de error asociado a la exception, para loguear el error
			if (e.getCodError() != 0){
				throw SoapUtil.createActionPipelineException(error, "Security Token error", SoapUtil.SOAP_NAME_TYPE, e, e.getCodError());
			} else {
				throw SoapUtil.createActionPipelineException(error, "Security Token error", SoapUtil.SOAP_NAME_TYPE, e);
			}
			
		} catch (Exception e) {
			logger.error(actionInfo + " [" + msgID + "] " + e.getMessage(), e);
			String error = e.getMessage();
        	throw SoapUtil.createActionPipelineException(error, "Request sign error", SoapUtil.SOAP_NAME_TYPE, e);
		}
	}

}