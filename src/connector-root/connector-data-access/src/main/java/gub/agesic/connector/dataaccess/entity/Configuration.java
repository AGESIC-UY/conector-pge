package gub.agesic.connector.dataaccess.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CONFIGURATION")
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "DTYPE")
public abstract class Configuration extends GenericEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ALIAS_KEYSTORE", length = 100)
    private String aliasKeystore;

    @Column(name = "PASSWORD_KEYSTORE_ORG", length = 50)
    private String passwordKeystoreOrg;

    @Column(name = "PASSWORD_KEYSTORE", length = 50)
    private String passwordKeystore;

    @Column(name = "PASSWORD_KEYSTORE_SSL", length = 50)
    private String passwordKeystoreSsl;

    @Column(name = "DIR_KEYSTORE_ORG", length = 200)
    private String dirKeystoreOrg;

    @Column(name = "DIR_KEYSTORE", length = 200)
    private String dirKeystore;

    @Column(name = "DIR_KEYSTORE_SSL", length = 200)
    private String dirKeystoreSsl;

    public String getAliasKeystore() {
        return aliasKeystore;
    }

    public void setAliasKeystore(final String aliasKeystore) {
        this.aliasKeystore = aliasKeystore;
    }

    public String getPasswordKeystoreOrg() {
        return passwordKeystoreOrg;
    }

    public void setPasswordKeystoreOrg(final String passwordKeystoreOrg) {
        this.passwordKeystoreOrg = passwordKeystoreOrg;
    }

    public String getPasswordKeystore() {
        return passwordKeystore;
    }

    public void setPasswordKeystore(final String passwordKeystore) {
        this.passwordKeystore = passwordKeystore;
    }

    public String getPasswordKeystoreSsl() {
        return passwordKeystoreSsl;
    }

    public void setPasswordKeystoreSsl(final String passwordKeystoreSsl) {
        this.passwordKeystoreSsl = passwordKeystoreSsl;
    }

    public String getDirKeystoreOrg() {
        return dirKeystoreOrg;
    }

    public void setDirKeystoreOrg(final String dirKeystoreOrg) {
        this.dirKeystoreOrg = dirKeystoreOrg;
    }

    public String getDirKeystore() {
        return dirKeystore;
    }

    public void setDirKeystore(final String dirKeystore) {
        this.dirKeystore = dirKeystore;
    }

    public String getDirKeystoreSsl() {
        return dirKeystoreSsl;
    }

    public void setDirKeystoreSsl(final String dirKeystoreSsl) {
        this.dirKeystoreSsl = dirKeystoreSsl;
    }
}
