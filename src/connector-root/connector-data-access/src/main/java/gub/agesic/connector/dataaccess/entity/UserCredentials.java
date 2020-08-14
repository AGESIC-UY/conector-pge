package gub.agesic.connector.dataaccess.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Created by adriancur on 19/10/17.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "USER_CREDENTIALS")
public class UserCredentials extends GenericEntity {

    @Column(name = "UNT_NAME", length = 100)
    private String userNameTokenName;

    @Column(name = "UNT_PASSWORD", length = 100)
    private String userNameTokenPassword;

    public String getUserNameTokenPassword() {
        return userNameTokenPassword;
    }

    public void setUserNameTokenPassword(final String userNameTokenPassword) {
        this.userNameTokenPassword = userNameTokenPassword;
    }

    public String getUserNameTokenName() {
        return userNameTokenName;
    }

    public void setUserNameTokenName(final String userNameTokenName) {
        this.userNameTokenName = userNameTokenName;
    }
}