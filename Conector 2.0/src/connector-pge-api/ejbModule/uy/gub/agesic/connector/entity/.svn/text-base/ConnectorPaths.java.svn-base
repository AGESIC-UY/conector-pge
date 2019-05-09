package uy.gub.agesic.connector.entity;

import java.io.Serializable;

public class ConnectorPaths implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String wsdl;
	private String keystoreOrg;
	private String keystore;
	private String keystoreSsl;
	private String connectorDirPath;
	
	public String getWsdl() {
		return wsdl;
	}
	public void setWsdl(String wsdl) {
		this.wsdl = wsdl;
	}
	public String getKeystoreOrg() {
		return keystoreOrg;
	}
	public void setKeystoreOrg(String keystoreOrg) {
		this.keystoreOrg = keystoreOrg;
	}
	public String getKeystore() {
		return keystore;
	}
	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}
	public String getKeystoreSsl() {
		return keystoreSsl;
	}
	public void setKeystoreSsl(String keystoreSsl) {
		this.keystoreSsl = keystoreSsl;
	}
	
	public String getConnectorDirPath() {
		return connectorDirPath;
	}
	public void setConnectorDirPath(String connectorDirPath) {
		this.connectorDirPath = connectorDirPath;
	}
	
	public String toString(){
		return ((wsdl == null)? "" : "wsdl= " + wsdl + "\n") +
				((keystoreOrg == null)? "" : "keystoreOrg= " + keystoreOrg + "\n") +
				((keystore == null)? "" : "keystore= " + keystore + "\n") +
				((keystoreSsl == null)? "" : "keystoreSsl= " + keystoreSsl + "\n"); 
	}
}
