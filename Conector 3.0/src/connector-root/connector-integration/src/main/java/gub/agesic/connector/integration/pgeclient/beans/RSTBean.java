package gub.agesic.connector.integration.pgeclient.beans;

public class RSTBean {

    private String issuer;

    private String role;

    private String policyName;

    private String userName;

    private String service;

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service
     *            the service to set
     */
    public void setService(final String service) {
        this.service = service;
    }

    /**
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @param issuer
     *            the issuer to set
     */
    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(final String role) {
        this.role = role;
    }

    /**
     * @return the policyName
     */
    public String getPolicyName() {
        return policyName;
    }

    /**
     * @param policyName
     *            the policyName to set
     */
    public void setPolicyName(final String policyName) {
        this.policyName = policyName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

}
