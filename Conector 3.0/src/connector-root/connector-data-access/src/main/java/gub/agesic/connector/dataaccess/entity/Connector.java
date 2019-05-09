package gub.agesic.connector.dataaccess.entity;

import java.io.Serializable;
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

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "CONNECTOR")
public class Connector extends GenericEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ConnectorLocalConfiguration localConfiguration;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "TYPE", length = 50)
    private String type;

    @Column(name = "DESCRIPTION", length = 800)
    private String description;

    @Transient
    private RoleOperation actualRoleOperation;

    @Column(name = "PATH", length = 512)
    private String path;

    @Column(name = "URL", length = 512)
    private String url;

    @Column(name = "WSA_TO", length = 512)
    private String wsaTo;

    @Column(name = "USERNAME", length = 100)
    private String username;

    @Column(name = "ISSUER", length = 100)
    private String issuer;

    @Column(name = "POLICY_NAME", length = 100)
    private String policyName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private UserCredentials userCredentials;

    @Column(name = "USER_CREDENTIALS")
    private boolean enableUserCredentials;

    @Column(name = "LOCAL_CONFIG")
    private boolean enableLocalConfiguration;

    @Column(name = "CACHE_CONFIG")
    private boolean enableCacheTokens;

    @Column(name = "SSL_ENABLED")
    private boolean enableSsl;

    @Column(name = "STS_LOCAL_ENABLED")
    private boolean enableSTSLocal;

    @Column(name = "STS_LOCAL_URL", length = 512)
    private String stsLocalUrl;

    @Column(name = "TAG", length = 100)
    private String tag;

    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RoleOperation> roleOperations;

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public boolean isEnableCacheTokens() {
        return enableCacheTokens;
    }

    public void setEnableCacheTokens(final boolean enableCacheTokens) {
        this.enableCacheTokens = enableCacheTokens;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getShortDescription() {
        if (description != null && description.length() >= 100) {
            return description.substring(0, 100);
        }
        return description;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getWsaTo() {
        return wsaTo;
    }

    public void setWsaTo(final String wsaTo) {
        this.wsaTo = wsaTo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(final String policyName) {
        this.policyName = policyName;
    }

    public List<RoleOperation> getRoleOperations() {
        return roleOperations;
    }

    public void setRoleOperations(final List<RoleOperation> roleOperations) {
        this.roleOperations = roleOperations;
    }

    @Override
    public String toString() {
        return "Conector Id = " + getId() + " - Name: " + name + " - Type: " + type;
    }

    public UserCredentials getUserCredentials() {
        return userCredentials;
    }

    public void setUserCredentials(final UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    public ConnectorLocalConfiguration getLocalConfiguration() {
        return localConfiguration;
    }

    public void setLocalConfiguration(final ConnectorLocalConfiguration localConfiguration) {
        this.localConfiguration = localConfiguration;
    }

    public RoleOperation getActualRoleOperation() {
        return actualRoleOperation;
    }

    public void setActualRoleOperation(final RoleOperation actualRoleOperation) {
        this.actualRoleOperation = actualRoleOperation;
    }

    public boolean isEnableLocalConfiguration() {
        return enableLocalConfiguration;
    }

    public void setEnableLocalConfiguration(final boolean enableLocalConfiguration) {
        this.enableLocalConfiguration = enableLocalConfiguration;
    }

    public boolean isEnableUserCredentials() {
        return enableUserCredentials;
    }

    public void setEnableUserCredentials(final boolean enableUserCredentials) {
        this.enableUserCredentials = enableUserCredentials;
    }

    public boolean isNew() {
        return getId() == null;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(final boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public boolean isEnableSTSLocal() {
        return enableSTSLocal;
    }

    public void setEnableSTSLocal(final boolean enableSTSLocal) {
        this.enableSTSLocal = enableSTSLocal;
    }

    public String getStsLocalUrl() {
        return stsLocalUrl;
    }

    public void setStsLocalUrl(final String stsLocalUrl) {
        this.stsLocalUrl = stsLocalUrl;
    }
}
