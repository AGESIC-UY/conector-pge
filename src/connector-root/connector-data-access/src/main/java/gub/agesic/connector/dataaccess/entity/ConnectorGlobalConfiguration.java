package gub.agesic.connector.dataaccess.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
}
