package gub.agesic.connector.dataaccess.entity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "GLOBAL_CONFIGURATION")
@DiscriminatorValue("GLOBAL")
public class ConnectorGlobalConfiguration extends Configuration implements Serializable {

    @Column(name = "TYPE", length = 50)
    private String type;

    @Column(name = "POLICY_NAME", length = 100)
    private String policyName;

    @Column(name = "STS_GLOBAL_URL", length = 512)
    private String stsGlobalUrl;

    @Transient
    private String host;

    @Transient
    private String port;

    @Transient
    private String portSsl;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(final String policyName) {
        this.policyName = policyName;
    }

    public String getStsGlobalUrl() {
        return stsGlobalUrl;
    }

    public void setStsGlobalUrl(final String stsGlobalUrl) {
        this.stsGlobalUrl = stsGlobalUrl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPortSsl() {
        return portSsl;
    }

    public void setPortSsl(String portSsl) {
        this.portSsl = portSsl;
    }
}
