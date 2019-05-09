package gub.agesic.connector.dataaccess.entity;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "LOCAL_CONFIGURATION")
@DiscriminatorValue("LOCAL")
public class ConnectorLocalConfiguration extends Configuration implements Serializable {

}
