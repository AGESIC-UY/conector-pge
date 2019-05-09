package gub.agesic.connector.integration.pgeclient.beans;

public class StoreBean {

    String alias;

    String storeFilePath;

    String storePwd;

    boolean checkValidity;

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * @return the storeFilePath
     */
    public String getStoreFilePath() {
        return storeFilePath;
    }

    /**
     * @param storeFilePath
     *            the storeFilePath to set
     */
    public void setStoreFilePath(final String storeFilePath) {
        this.storeFilePath = storeFilePath;
    }

    /**
     * @return the storePwd
     */
    public String getStorePwd() {
        return storePwd;
    }

    /**
     * @param storePwd
     *            the storePwd to set
     */
    public void setStorePwd(final String storePwd) {
        this.storePwd = storePwd;
    }

    public boolean isCheckValidity() {
        return checkValidity;
    }

    public void setCheckValidity(final boolean checkValidity) {
        this.checkValidity = checkValidity;
    }
}
