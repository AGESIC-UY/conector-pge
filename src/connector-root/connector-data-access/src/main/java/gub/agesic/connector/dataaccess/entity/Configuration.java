package gub.agesic.connector.dataaccess.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CONFIGURATION")
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
public abstract class Configuration extends GenericEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ALIAS_KEYSTORE", length = 100)
    private String aliasKeystore;

    @Column(name = "ALIAS_KEYSTORE_SSL", length = 100)
    private String aliasKeystoreSSL;

    @Column(name = "PASSWORD_KEYSTORE_ORG", length = 50)
    private String passwordKeystoreOrg;

    @Column(name = "PASSWORD_KEYSTORE_SSL", length = 50)
    private String passwordKeystoreSsl;

    // Truststore
    @Column(name = "PASSWORD_KEYSTORE", length = 50)
    private String passwordKeystore;

    @Column(name = "DIR_KEYSTORE_ORG", length = 200)
    private String dirKeystoreOrg;

    @Column(name = "DIR_KEYSTORE_SSL", length = 200)
    private String dirKeystoreSsl;

    // Truststore
    @Column(name = "DIR_KEYSTORE", length = 200)
    private String dirKeystore;

    @Column(name = "EXPIRATION_NOTIFICATION_ENABLED")
    private boolean enableExpirationNotification;

    @Column(name = "EXPIRATION_NOTICE_DAYS", length = 10)
    private int expirationNoticeDays;

    @Column(name = "SERVICE_TIMEOUT", length = 10)
    private int serviceTimeOut;

    public String getAliasKeystore() {
        return aliasKeystore;
    }

    public void setAliasKeystore(final String aliasKeystore) {
        this.aliasKeystore = aliasKeystore;
    }

    public String getAliasKeystoreSSL() {
        return aliasKeystoreSSL;
    }

    public void setAliasKeystoreSSL(final String aliasKeystoreSSL) {
        this.aliasKeystoreSSL = aliasKeystoreSSL;
    }

    public String getPasswordKeystoreOrg() {
        return passwordKeystoreOrg;
    }

    public void setPasswordKeystoreOrg(final String passwordKeystoreOrg) {
        this.passwordKeystoreOrg = passwordKeystoreOrg;
    }

    public String getPasswordKeystoreSsl() {
        return passwordKeystoreSsl;
    }

    public void setPasswordKeystoreSsl(final String passwordKeystoreSsl) {
        this.passwordKeystoreSsl = passwordKeystoreSsl;
    }

    public String getPasswordKeystore() {
        return passwordKeystore;
    }

    public void setPasswordKeystore(final String passwordKeystore) {
        this.passwordKeystore = passwordKeystore;
    }


    public String getDirKeystoreOrg() {
        return dirKeystoreOrg;
    }

    public void setDirKeystoreOrg(final String dirKeystoreOrg) {
        this.dirKeystoreOrg = dirKeystoreOrg;
    }

    public String getDirKeystoreSsl() {
        return dirKeystoreSsl;
    }

    public void setDirKeystoreSsl(final String dirKeystoreSsl) {
        this.dirKeystoreSsl = dirKeystoreSsl;
    }

    public String getDirKeystore() {
        return dirKeystore;
    }

    public void setDirKeystore(String dirKeystore) {
        this.dirKeystore = dirKeystore;
    }

    public boolean isEnableExpirationNotification() {
        return enableExpirationNotification;
    }

    public void setEnableExpirationNotification(boolean enableExpirationNotification) {
        this.enableExpirationNotification = enableExpirationNotification;
    }

    public int getExpirationNoticeDays() {
        return expirationNoticeDays;
    }

    public void setExpirationNoticeDays(int expirationNoticeDays) {
        this.expirationNoticeDays = expirationNoticeDays;
    }

    public int getServiceTimeOut() {
        return serviceTimeOut;
    }

    public void setServiceTimeOut(int serviceTimeOut) {
        this.serviceTimeOut = serviceTimeOut;
    }
}
