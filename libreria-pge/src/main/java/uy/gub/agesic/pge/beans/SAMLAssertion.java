package uy.gub.agesic.pge.beans;

import org.opensaml.saml1.core.Assertion;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import uy.gub.agesic.pge.enums.SamlVersion;

public class SAMLAssertion {
    private Assertion assertionSaml1;
    private org.opensaml.saml2.core.Assertion assertionSaml2;

    /**
     * @return the SAML 1.0 assertion
     */
    public Assertion getAssertionSaml1() {
        return assertionSaml1;
    }

    /**
     * @param assertionSaml1 the SAML 1.0 assertion to set
     */
    public void setAssertionSaml1(final Assertion assertionSaml1) {
        this.assertionSaml1 = assertionSaml1;
    }

    /**
     * @return the SAML 2.0 assertion
     */
    public org.opensaml.saml2.core.Assertion getAssertionSaml2() {
        return assertionSaml2;
    }

    /**
     * @param assertionSaml2 the SAML 2.0 assertion to set
     */
    public void setAssertionSaml2(org.opensaml.saml2.core.Assertion assertionSaml2) {
        this.assertionSaml2 = assertionSaml2;
    }

    public Element getDOM(String samlVersion) {
        return samlVersion.equals(SamlVersion.V2_0.getName()) ? assertionSaml2.getDOM() : assertionSaml1.getDOM();
    }

    public String toString(String samlVersion) {
        return XMLHelper.prettyPrintXML(getDOM(samlVersion));
    }
}