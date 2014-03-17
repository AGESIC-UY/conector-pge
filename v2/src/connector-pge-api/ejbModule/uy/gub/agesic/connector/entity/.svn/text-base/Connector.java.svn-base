package uy.gub.agesic.connector.entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "CONNECTOR") //, uniqueConstraints={@UniqueConstraint(columnNames={"NAME","TYPE"}), @UniqueConstraint(columnNames={"TYPE","PATH"})})
public class Connector extends AbstractEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TYPE_TEST = "Test";
	public static final String TYPE_PROD = "Prod";

	public static final String TYPE_PROD_LABEL = "Producci\u00F3n";
	public static final String TYPE_TEST_LABEL = "Test";

	public static final String NAME_FILE_WSDL = "wsdl.xml";
	public static final String NAME_FILE_KEYSTORE_ORG = "keystore_org.xml";
	public static final String NAME_FILE_KEYSTORE = "keystore.xml";
	public static final String NAME_FILE_KEYSTORE_SSL = "keystoreSsl.xml";

	public static final String NAME_FILE_KEYSTORE_ORG2 = "organismo.keystore";
	public static final String NAME_FILE_KEYSTORE2 = "ssl.keystore";
	public static final String NAME_FILE_KEYSTORE_SSL2 = "ssl.trustore";
	
	
	
	public Connector() {
		super();
	}

	public Connector (Connector con){
		super();
		
		this.setName(con.getName());
		this.setDescription(con.getDescription());
		this.setType(con.getType());
		this.setPath(con.getPath());	
		this.setUrl(con.getUrl());
		this.setWsdl(con.getWsdl());
		this.setExistWsdl(con.isExistWsdl());
		this.setKeystoreOrg(con.getKeystoreOrg());
		this.setExistKeystoreOrg(con.isExistKeystoreOrg());
		this.setKeystoreOrgName(con.getKeystoreOrgName());
		this.setKeystore(con.getKeystore());
		this.setExistKeystore(con.isExistKeystore());
		this.setKeystoreName(con.getKeystoreName());
		this.setAliasKeystore(con.getAliasKeystore());
		this.setPasswordKeystoreOrg(con.getPasswordKeystoreOrg());
		this.setPasswordKeystore(con.getPasswordKeystore());
		this.setKeystoreSsl(con.getKeystoreSsl());
		this.setExistKeystoreSsl(con.isExistKeystoreSsl());
		this.setKeystoreSslName(con.getKeystoreSslName());
		this.setPasswordKeystoreSsl(con.getPasswordKeystoreSsl());
		this.setWsaTo(con.getWsaTo());
		this.setUsername(con.getUsername());
		this.setIssuer(con.getIssuer());
		this.setPolicyName(con.getPolicyName());
		this.setEnableCacheTokens(con.isEnableCacheTokens());
		this.setTag(con.getTag());
		this.setEnableLocalConf(con.isEnableLocalConf());
		
		
		
		List<RoleOperation> role_operation = new LinkedList<RoleOperation>();
		List<RoleOperation> role_operationOld = con.getRole_operation();
		for(RoleOperation rolOp : role_operationOld){
			RoleOperation newRoleOP = new RoleOperation();
			newRoleOP.setOperation(rolOp.getOperation());
			newRoleOP.setOperationFromWSDL(rolOp.getOperationFromWSDL());
			if (rolOp.getRole() != null) { 
				newRoleOP.setRole(rolOp.getRole());
			} else {
				newRoleOP.setRole(con.getIssuer());
			}
			newRoleOP.setWsaAction(rolOp.getWsaAction());
			newRoleOP.setSoapAction(rolOp.getSoapAction());
			
			role_operation.add(newRoleOP);
		}
		
		this.setRole_operation(role_operation);
		
	}

	@Column(name="NAME", length=100)
	private String name;
	
	@Column(name="DESCRIPTION", length=800)
	private String description;
	
	@Column(name="TYPE", length=50)
	private String type;

	@Column(name="PATH", length=100)
	private String path;	

	@Column(name="URL", length=200)
	private String url;
	
	@Transient
	private byte[] wsdl;
	
	@XmlTransient
	@Transient
	private boolean existWsdl;
	
	@Transient
	private byte[] keystoreOrg;
	
	@XmlTransient
	@Transient
	private boolean existKeystoreOrg;

	@XmlTransient
	@Transient
	private String keystoreOrgName;

	@Transient
	private byte[] keystore;

	@XmlTransient
	@Transient
	private boolean existKeystore;

	@XmlTransient
	@Transient
	private String keystoreName;

	@Column(name="ALIAS_KEYSTORE", length=100)
	private String aliasKeystore;
	
	@XmlTransient
	@Column(name="PASSWORD_KEYSTORE_ORG", length=50)
	private String passwordKeystoreOrg;
	
	@XmlTransient
	@Column(name="PASSWORD_KEYSTORE", length=50)
	private String passwordKeystore;

	@Transient
	private byte[] keystoreSsl;
	
	@XmlTransient
	@Transient
	private boolean existKeystoreSsl;

	@XmlTransient
	@Transient
	private String keystoreSslName;

	@XmlTransient
	@Column(name="PASSWORD_KEYSTORE_SSL", length=50)
	private String passwordKeystoreSsl;
	
	@Column(name="WSA_TO", length=512)
	private String wsaTo;

	@Column(name="USERNAME", length=100)
	private String username;
	
	@Column(name="ISSUER", length=100)
	private String issuer;
	
	@Column(name="POLICY_NAME", length=100)
	private String policyName;
	
	@Column(name="UNT_NAME", length=100)
	private String userNameTokenName;

	@XmlTransient
	@Column(name="UNT_PASSWORD", length=100)
	private String userNameTokenPassword;
	
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
	private List<RoleOperation> role_operation = new LinkedList<RoleOperation>();
	
	@Column(name="CACHE_CONFIG", length=100)
	private boolean enableCacheTokens;
	
	@Column(name="TAG", length=100)
	private String tag;
	
	@XmlTransient
	@OneToOne
	private Connector connectorAssociated;
	
	@Column(name="LOCAL_CERT_CONFIG")
	private boolean enableLocalConf;
	
	@Column(name="USERNAME_TOKEN_ACTIVE")
	private boolean enableUserToken;
	
	@Column(name="CONNECTOR_VERSION")
	private String version;
	
	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isEnableLocalConf() {
		return enableLocalConf;
	}

	public void setEnableLocalConf(boolean enableLocalConf) {
		this.enableLocalConf = enableLocalConf;
	}

	public Connector getConnectorAssociated() {
		return connectorAssociated;
	}

	public void setConnectorAssociated(Connector connectorAssociated) {
		this.connectorAssociated = connectorAssociated;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isEnableCacheTokens() {
		return enableCacheTokens;
	}

	public void setEnableCacheTokens(boolean enableCacheTokens) {
		this.enableCacheTokens = enableCacheTokens;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;		
	}
	
	public String getShortDescription() {
		if (description != null) {
			if (description.length() >= 100) {
				return description.substring(0, 100);
			}
		}
		return description;		
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getTypeLabel() {
		return TYPE_PROD.equals(type) ? TYPE_PROD_LABEL : TYPE_TEST_LABEL; 
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPathToWsdl() {
		if (path != null) {
			if (path.startsWith("/")) {
				return path.substring(1, path.length());
			}
		}
		return path;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public byte[] getWsdl() {
		return wsdl;
	}

	public void setWsdl(byte[] wsdl) {
		this.wsdl = wsdl;
	}

	public byte[] getKeystoreOrg() {
		return keystoreOrg;
	}

	public void setKeystoreOrg(byte[] keystoreOrg) {
		this.keystoreOrg = keystoreOrg;
	}

	public byte[] getKeystore() {
		return keystore;
	}

	public void setKeystore(byte[] keystore) {
		this.keystore = keystore;
	}

	public String getWsaTo() {
		return wsaTo;
	}

	public void setWsaTo(String wsaTo) {
		this.wsaTo = wsaTo;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getAliasKeystore() {
		return aliasKeystore;
	}

	public void setAliasKeystore(String aliasKeystore) {
		this.aliasKeystore = aliasKeystore;
	}

	public String getPasswordKeystoreOrg() {
		return passwordKeystoreOrg;
	}

	public void setPasswordKeystoreOrg(String passwordKeystoreOrg) {
		this.passwordKeystoreOrg = passwordKeystoreOrg;
	}

	public String getPasswordKeystore() {
		return passwordKeystore;
	}

	public void setPasswordKeystore(String passwordKeystore) {
		this.passwordKeystore = passwordKeystore;
	}

	public byte[] getKeystoreSsl() {
		return keystoreSsl;
	}

	public void setKeystoreSsl(byte[] keystoreSsl) {
		this.keystoreSsl = keystoreSsl;
	}

	public String getPasswordKeystoreSsl() {
		return passwordKeystoreSsl;
	}

	public void setPasswordKeystoreSsl(String passwordKeystoreSsl) {
		this.passwordKeystoreSsl = passwordKeystoreSsl;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getUserNameTokenName() {
		return userNameTokenName;
	}

	public String getUserNameTokenPassword() {
		return userNameTokenPassword;
	}

	public void setUserNameTokenName(String userNameTokenName) {
		this.userNameTokenName = userNameTokenName;
	}

	public void setUserNameTokenPassword(String userNameTokenPassword) {
		this.userNameTokenPassword = userNameTokenPassword;
	}

	public List<RoleOperation> getRole_operation() {
		return role_operation;
	}

	public void setRole_operation(List<RoleOperation> role_operation) {
		this.role_operation = role_operation;
	}

	public boolean isExistWsdl() {
		return existWsdl;
	}

	public void setExistWsdl(boolean existWsdl) {
		this.existWsdl = existWsdl;
	}

	public boolean isExistKeystoreOrg() {
		return existKeystoreOrg;
	}

	public void setExistKeystoreOrg(boolean existKeystoreOrg) {
		this.existKeystoreOrg = existKeystoreOrg;
	}

	public boolean isExistKeystore() {
		return existKeystore;
	}

	public void setExistKeystore(boolean existKeystore) {
		this.existKeystore = existKeystore;
	}

	public boolean isExistKeystoreSsl() {
		return existKeystoreSsl;
	}

	public void setExistKeystoreSsl(boolean existKeystoreSsl) {
		this.existKeystoreSsl = existKeystoreSsl;
	}
	
	public String getKeystoreOrgName() {
		return keystoreOrgName;
	}

	public void setKeystoreOrgName(String keystoreOrgName) {
		this.keystoreOrgName = keystoreOrgName;
	}

	public String getKeystoreName() {
		return keystoreName;
	}

	public void setKeystoreName(String keystoreName) {
		this.keystoreName = keystoreName;
	}

	public String getKeystoreSslName() {
		return keystoreSslName;
	}

	public void setKeystoreSslName(String keystoreSslName) {
		this.keystoreSslName = keystoreSslName;
	}

	public String toString(){
		return "Conector Id = "+ getId() + " - Name: "+ name + " - Type: " + type;
	}

	public boolean isEnableUserToken() {
		return enableUserToken;
	}

	public void setEnableUserToken(boolean enableUserToken) {
		this.enableUserToken = enableUserToken;
	}
	
	
	
}
