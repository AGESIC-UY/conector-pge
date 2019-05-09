/**
 *
 */
package gub.agesic.connector.integration.pgeclient.beans;

/**
 * Contains RequestSecurityTokenResponse information
 *
 * @author guzman.llambias
 *
 */
public class STSResponse {

    private final long responseTime;

    private final SAMLAssertion assertion;

    public STSResponse(final long responseTime, final SAMLAssertion assertion) {
        super();
        this.responseTime = responseTime;
        this.assertion = assertion;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public SAMLAssertion getAssertion() {
        return assertion;
    }

}
