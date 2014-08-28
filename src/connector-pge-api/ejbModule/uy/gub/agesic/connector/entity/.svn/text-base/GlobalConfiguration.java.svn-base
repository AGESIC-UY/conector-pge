package uy.gub.agesic.connector.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "GLOBAL_CONFIGURATION") //, uniqueConstraints={@UniqueConstraint(columnNames={"NAME","TYPE"}), @UniqueConstraint(columnNames={"TYPE","PATH"})})
public class GlobalConfiguration extends AbstractEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public GlobalConfiguration() {
		super();
	}
	
	public GlobalConfiguration(String type) {
		this.type = type;
	}
	
	@Column(name="ALIAS_KEYSTORE", length=100)
	private String aliasKeystore;
	
	@Column(name="PASSWORD_KEYSTORE_ORG", length=50)
	private String passwordKeystoreOrg;
	
	@Column(name="PASSWORD_KEYSTORE", length=50)
	private String passwordKeystore;
	
	@Column(name="PASSWORD_KEYSTORE_SSL", length=50)
	private String passwordKeystoreSsl;
	
	@XmlTransient
	@Transient
	private boolean existKeystoreOrg;
	
	@Column(name="TYPE", length=50)
	private String type;

	@XmlTransient
	@Transient
	private boolean existKeystore;
	
	
	@XmlTransient
	@Transient
	private boolean existKeystoreSsl;
	
	@XmlTransient
	@Transient
	private String keystoreOrgName;
	

	@XmlTransient
	@Transient
	private String keystoreSslName;
	

	@XmlTransient
	@Transient
	private String keystoreName;
	
	
	public String getKeystoreOrgName() {
		return keystoreOrgName;
	}
	
	public String getKeystoreName() {
		return keystoreName;
	}
	

	public String getKeystoreSslName() {
		return keystoreSslName;
	}

	public String getAliasKeystore() {
		return aliasKeystore;
	}

	public void setAliasKeystore(String aliasKeystore) {
		this.aliasKeystore = aliasKeystore;
	}

	public boolean isExistKeystoreOrg() {
		return existKeystoreOrg;
	}

	public void setExistKeystoreOrg(boolean existKeystoreOrg) {
		this.existKeystoreOrg = existKeystoreOrg;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public void setKeystoreOrgName(String keystoreOrgName) {
		this.keystoreOrgName = keystoreOrgName;
	}

	public void setKeystoreSslName(String keystoreSslName) {
		this.keystoreSslName = keystoreSslName;
	}

	public void setKeystoreName(String keystoreName) {
		this.keystoreName = keystoreName;
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
	

	public String getPasswordKeystoreSsl() {
		return passwordKeystoreSsl;
	}

	public void setPasswordKeystoreSsl(String passwordKeystoreSsl) {
		this.passwordKeystoreSsl = passwordKeystoreSsl;
	}
	
}
