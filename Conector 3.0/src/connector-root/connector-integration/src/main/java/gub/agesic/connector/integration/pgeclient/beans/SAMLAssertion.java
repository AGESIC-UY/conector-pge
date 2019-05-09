package gub.agesic.connector.integration.pgeclient.beans;

import org.opensaml.saml1.core.Assertion;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

public class SAMLAssertion {

    private Assertion assertion;

    /**
     * @return the assertion
     */
    public Assertion getAssertion() {
        return assertion;
    }

    /**
     * @param assertion
     *            the assertion to set
     */
    public void setAssertion(final Assertion assertion) {
        this.assertion = assertion;
    }

    public Element getDOM() {
        return assertion.getDOM();
    }

    @Override
    public String toString() {
        return XMLHelper.prettyPrintXML(assertion.getDOM());
    }

}
