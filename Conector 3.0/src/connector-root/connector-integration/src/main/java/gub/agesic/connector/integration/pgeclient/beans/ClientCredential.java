package gub.agesic.connector.integration.pgeclient.beans;

import org.opensaml.xml.security.credential.Credential;

public class ClientCredential {

    Credential credential;

    /**
     * @return the credential
     */
    public Credential getCredential() {
        return credential;
    }

    /**
     * @param credential
     *            the credential to set
     */
    public void setCredential(final Credential credential) {
        this.credential = credential;
    }

}
