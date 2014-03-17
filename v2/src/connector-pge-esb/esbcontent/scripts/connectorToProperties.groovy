import org.jboss.soa.esb.message.*
import uy.gub.agesic.connector.*
import java.util.LinkedList
import java.util.HashMap
import java.util.List
import biz.ideasoft.soa.esb.util.XPathXmlDogUtil

def location = "http://" + message.getProperties().getProperty("host") + message.getProperties().getProperty("Path"); 
message.getProperties().setProperty("location", location);
message.getProperties().setProperty("Content-Type", new ResponseHeader("Content-Type", "text/xml;charset=UTF-8"));
def fullConnector = message.getBody().remove("fullConnector");
if (fullConnector != null) {
	def connector = fullConnector.getConnector();
	if (connector != null) {
		def wsaTo = connector.getWsaTo();
		def name = connector.getName();
		def userName = connector.getUsername();
		def aliasKeystore = connector.getAliasKeystore();
		def passwordKeystoreOrg = connector.getPasswordKeystoreOrg();
		def passwordKeystore = connector.getPasswordKeystore();
		def passwordKeystoreSsl = connector.getPasswordKeystoreSsl();
		def issuer = connector.getIssuer();
		def policyName = connector.getPolicyName();
		def wsaAction = "";
		def rolOperation = "";
		def roles = connector.getRole_operation();
		if (roles != null && roles.size() > 0) {
			def xpaths = new LinkedList<String>();
	   		xpaths.add("local-name(/soapenv:Envelope/soapenv:Body/child::*[1])");
	   		def namespaces = new HashMap<String, String>();
			namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");  
			def xmlDogUtil = new XPathXmlDogUtil();
			def operation = xmlDogUtil.executeMultipleXPath(message, xpaths, namespaces);
			if (operation != null && operation.size() > 0) {
				def operationName = operation.get(0).toString();
				def size = roles.size();
				def count = 0;
				for (count = 0; count < size; count ++) {
					if (roles.get(count).getOperation().equals(operationName)) {
						rolOperation = roles.get(count).getRole();
						wsaAction = roles.get(count).getWsaAction();
					}
				}
			}
		}
		def url = connector.getUrl();
		if (wsaTo != null) {
			message.getProperties().setProperty("wsaTo", wsaTo);
		}
		if (!wsaAction.equals("")) {
			message.getProperties().setProperty("wsaAction", wsaAction);
		}
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
		if (!rolOperation.equals("")) {
			message.getProperties().setProperty("role", rolOperation);
		}
		if (url != null) {
			message.getProperties().setProperty("routePhysicalURL", url);
		}
	}
	
	def paths = fullConnector.getConnectorPaths();
	if (paths != null) {
		def keystoreOrg = paths.getKeystoreOrg();
		if (keystoreOrg != null) {
			message.getProperties().setProperty("keyStoreOrgFilePath", keystoreOrg);
		}
		def keystore = paths.getKeystore();
		if (keystore != null) {
			message.getProperties().setProperty("keyStoreFilePath", keystore);
		}
		def keystoreSsl = paths.getKeystoreSsl();
		if (keystoreSsl != null) {	
			message.getProperties().setProperty("trustStoreFilePath", keystoreSsl);
		}
	}
}

def xpaths = new LinkedList<String>();
xpaths.add("/soapenv:Envelope/soapenv:Header/pge:MessageType/text() = 'TEST'");
def namespaces = new HashMap<String, String>();
namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
namespaces.put("pge", "http://pge.agesic.gub.uy/MessageType/");
def xmlDogUtil = new XPathXmlDogUtil();
def elements = xmlDogUtil.executeMultipleXPath(message, xpaths, namespaces);
message.getProperties().setProperty("skipSTS", (elements != null && elements.size() > 0 && elements.get(0)));


