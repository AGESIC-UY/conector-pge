package uy.gub.agesic.pge.pojos;

public class Configuration {

    private String aliasKeystore;

    private String aliasKeystoreSSL;

    private String passwordKeystoreOrg;

    private String passwordKeystore;

    private String passwordKeystoreSsl;

    private String dirKeystoreOrg;

    private String dirKeystore;

    private String dirKeystoreSsl;

    public Configuration() {
    }

    public Configuration(String aliasKeystore, String aliasKeystoreSSL, String passwordKeystoreOrg, String passwordKeystore, String passwordKeystoreSsl, String dirKeystoreOrg, String dirKeystore, String dirKeystoreSsl) {
        this.aliasKeystore = aliasKeystore;
        this.aliasKeystoreSSL = aliasKeystoreSSL;
        this.passwordKeystoreOrg = passwordKeystoreOrg;
        this.passwordKeystore = passwordKeystore;
        this.passwordKeystoreSsl = passwordKeystoreSsl;
        this.dirKeystoreOrg = dirKeystoreOrg;
        this.dirKeystore = dirKeystore;
        this.dirKeystoreSsl = dirKeystoreSsl;
    }

    public String getAliasKeystore() {
        return aliasKeystore;
    }

    public void setAliasKeystore(String aliasKeystore) {
        this.aliasKeystore = aliasKeystore;
    }

    public String getAliasKeystoreSSL() {
        return aliasKeystoreSSL;
    }

    public void setAliasKeystoreSSL(String aliasKeystoreSSL) {
        this.aliasKeystoreSSL = aliasKeystoreSSL;
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

    public String getDirKeystoreOrg() {
        return dirKeystoreOrg;
    }

    public void setDirKeystoreOrg(String dirKeystoreOrg) {
        this.dirKeystoreOrg = dirKeystoreOrg;
    }

    public String getDirKeystore() {
        return dirKeystore;
    }

    public void setDirKeystore(String dirKeystore) {
        this.dirKeystore = dirKeystore;
    }

    public String getDirKeystoreSsl() {
        return dirKeystoreSsl;
    }

    public void setDirKeystoreSsl(String dirKeystoreSsl) {
        this.dirKeystoreSsl = dirKeystoreSsl;
    }
}
