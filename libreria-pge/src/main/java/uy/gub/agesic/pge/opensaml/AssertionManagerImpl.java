package uy.gub.agesic.pge.opensaml;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.*;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.*;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uy.gub.agesic.pge.AgesicConstants;
import uy.gub.agesic.pge.AssertionManager;
import uy.gub.agesic.pge.beans.ClientCredential;
import uy.gub.agesic.pge.beans.SAMLAssertion;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.core.xml.util.WSTrustUtil;
import uy.gub.agesic.pge.enums.SamlVersion;
import uy.gub.agesic.pge.exceptions.AssertionException;
import uy.gub.agesic.pge.exceptions.NoAssertionFoundException;
import uy.gub.agesic.pge.exceptions.ParserException;
import uy.gub.agesic.pge.exceptions.UnmarshalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AssertionManagerImpl implements AssertionManager {

    private static final Logger log = Logger.getLogger(AssertionManagerImpl.class);
    private static X509Certificate x509Certificate;

    @SuppressWarnings("unchecked")
    public static Subject getSubjectForSignedToken(final XMLObjectBuilderFactory builderFactory, final PGEConfiguration config) {

        String role = config.getSTSPropValue(ConfigProperties.ROLE);
        String nameIDFormat = config.getSAMLPropValue(ConfigProperties.SAML1_NAME_ID_FORMAT);
        String confirmMethod = config.getSAMLPropValue(ConfigProperties.SAML1_CONFIRMATION_METHOD);

        // Create the NameIdentifier
        final SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory
                .getBuilder(NameIdentifier.DEFAULT_ELEMENT_NAME);
        final NameIdentifier nameId = (NameIdentifier) nameIdBuilder.buildObject();
        nameId.setNameIdentifier(role);
        nameId.setFormat(nameIDFormat == null ? NameIdentifier.EMAIL : nameIDFormat);

        // Create the SubjectConfirmation
        final SAMLObjectBuilder confirmationMethodBuilder = (SAMLObjectBuilder) builderFactory
                .getBuilder(ConfirmationMethod.DEFAULT_ELEMENT_NAME);
        final ConfirmationMethod confirmationMethod = (ConfirmationMethod) confirmationMethodBuilder
                .buildObject();
        confirmationMethod.setConfirmationMethod(confirmMethod == null ? AgesicConstants.SAML10_BEARER_CONFIRMATION_METHOD : confirmMethod);

        final SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder) builderFactory
                .getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        final SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder
                .buildObject();
        subjectConfirmation.getConfirmationMethods().add(confirmationMethod);

        // Create the Subject
        final SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder) builderFactory
                .getBuilder(Subject.DEFAULT_ELEMENT_NAME);
        final Subject subject = (Subject) subjectBuilder.buildObject();

        subject.setNameIdentifier(nameId);
        subject.setSubjectConfirmation(subjectConfirmation);

        return subject;
    }

    public static org.opensaml.saml2.core.Subject getSubjectForSignedTokenSaml2(final XMLObjectBuilderFactory builderFactory, final PGEConfiguration config) {
        String role = config.getSTSPropValue(ConfigProperties.ROLE);
        String nameIDFormat = config.getSAMLPropValue(ConfigProperties.SAML2_NAME_ID_FORMAT);
        String confirmMethod = config.getSAMLPropValue(ConfigProperties.SAML2_CONFIRMATION_METHOD);

        // Create the NameIdentifier
        SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
        NameID nameId = (NameID) nameIdBuilder.buildObject();
        nameId.setValue(role);
        nameId.setFormat(nameIDFormat == null ? NameID.ENTITY : nameIDFormat);

        SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        org.opensaml.saml2.core.SubjectConfirmation subjectConfirmation = (org.opensaml.saml2.core.SubjectConfirmation) subjectConfirmationBuilder.buildObject();
        subjectConfirmation.setMethod(confirmMethod == null ? AgesicConstants.SAML20_BEARER_CONFIRMATION_METHOD : confirmMethod);

        // Create the Subject
        SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.Subject.DEFAULT_ELEMENT_NAME);
        org.opensaml.saml2.core.Subject subject = (org.opensaml.saml2.core.Subject) subjectBuilder.buildObject();

        subject.setNameID(nameId);
        subject.getSubjectConfirmations().add(subjectConfirmation);

        return subject;
    }

    @Override
    public ClientCredential getCredentialFromAssertion(final SAMLAssertion samlAssertion, final PGEConfiguration config) {
        final String samlVersion = config.getSTSPropValue(ConfigProperties.SAML_VERSION);

        final Assertion assertion = samlAssertion.getAssertionSaml1();
        Signature signature = assertion.getSignature();
        if (samlVersion.equals(SamlVersion.V2_0.getName())) {
            signature = samlAssertion.getAssertionSaml2().getSignature();
        }

        final KeyInfo keyInfo = signature.getKeyInfo();
        final List<X509Data> x509DataList = keyInfo.getX509Datas();
        final X509Data x509Data = x509DataList.get(0);
        final List<org.opensaml.xml.signature.X509Certificate> x509Certs = x509Data
                .getX509Certificates();

        if (x509Certs.size() > 1) {
            log.warn("The Assertion has more than one certificate.");
        }

        final org.opensaml.xml.signature.X509Certificate x509Cert = x509Certs.get(0);
        final String base64Cert = x509Cert.getValue();

        X509Certificate x509Cert2 = null;
        try {
            x509Cert2 = SecurityHelper.buildJavaX509Cert(base64Cert);

        } catch (final CertificateException e) {
            e.printStackTrace();
        }

        // Build the OpenSAML credential
        final BasicX509Credential credential = new BasicX509Credential();
        final Collection<X509CRL> crls = new ArrayList<>();
        credential.setEntityCertificate(x509Cert2);
        credential.setCRLs(crls);
        credential.setPublicKey(Objects.requireNonNull(x509Cert2).getPublicKey());

        // Wrap it in a PGE ClientCredential
        final ClientCredential result = new ClientCredential();
        result.setCredential(credential);
        return result;
    }

    @Override
    public SAMLAssertion generateSignedAssertion(final ClientCredential signingCredential, final PGEConfiguration config) throws AssertionException {

        Long stsTimeOut = config.getSTSLongPropValue(ConfigProperties.STS_TIMEOUT);
        int minutes = (int) (stsTimeOut == null ? WSTrustUtil.DEFAULT_TOKENTIMEOUT : TimeUnit.MILLISECONDS.toMinutes(stsTimeOut));
        String saml1AuthenticationMethod = config.getSAMLPropValue(ConfigProperties.SAML1_AUTHENTICATION_METHOD);
        String saml2AuthnContext = config.getSAMLPropValue(ConfigProperties.SAML2_AUTHN_CONTEXT);
        String signatureAlgorithm = config.getSAMLPropValue(ConfigProperties.SIGNATURE_ALGORITHM);
        String canonicalizationAlgorithm = config.getSAMLPropValue(ConfigProperties.SIGNATURE_CANONICALIZATION_ALGORITHM);

        final Credential credential = signingCredential.getCredential();

        final DateTime authenticationInstant = new DateTime();
        final DateTime issueInstant = new DateTime();
        final DateTime conditionTimeNotBefore = new DateTime().minusMinutes(minutes);
        final DateTime conditionTimeNotAfter = new DateTime().plusMinutes(minutes);

        try {
            // SAML User Info
            final String strIssuer = config.getSTSPropValue(ConfigProperties.ISSUER);
            final String strRole = config.getSTSPropValue(ConfigProperties.ROLE);
            final String strPolicyName = config.getSTSPropValue(ConfigProperties.POLICY);
            final String userName = config.getSTSPropValue(ConfigProperties.USERNAME);
            final String samlVersion = config.getSTSPropValue(ConfigProperties.SAML_VERSION);

            DefaultBootstrap.bootstrap();
            final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

            // Create signature
            final Signature signature = (Signature) Configuration.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                    .buildObject(Signature.DEFAULT_ELEMENT_NAME);

            signature.setSigningCredential(credential);
            signature.setSignatureAlgorithm(signatureAlgorithm == null ? SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1 : signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(
                    canonicalizationAlgorithm == null ? SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS : canonicalizationAlgorithm);

            final KeyInfoBuilder keyInfoBuilder = (KeyInfoBuilder) builderFactory
                    .getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
            final KeyInfo keyinfo = keyInfoBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            KeyInfoHelper.addCertificate(keyinfo, x509Certificate);
            signature.setKeyInfo(keyinfo);

            // Init Assertions
            final SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
            final Assertion assertionSaml1 = (Assertion) assertionBuilder.buildObject();

            final SAMLObjectBuilder assertionBuilderSaml2 = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.Assertion.DEFAULT_ELEMENT_NAME);
            final org.opensaml.saml2.core.Assertion assertionSaml2 = (org.opensaml.saml2.core.Assertion) assertionBuilderSaml2.buildObject();

            // Create assertionID
            final IdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
            final String strAssertionID = idGenerator.generateIdentifier();

            if (samlVersion.equals(SamlVersion.V2_0.getName())) {
                // SAML 2.0
                // Create authentication statement subject
                final org.opensaml.saml2.core.Subject subject = getSubjectForSignedTokenSaml2(builderFactory, config);

                // Create Authentication Statement
                SAMLObjectBuilder authStatementBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
                AuthnStatement authnStatement = (AuthnStatement) authStatementBuilder.buildObject();
                authnStatement.setAuthnInstant(authenticationInstant);

                SAMLObjectBuilder authContextBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.AuthnContext.DEFAULT_ELEMENT_NAME);
                org.opensaml.saml2.core.AuthnContext authnContext = (org.opensaml.saml2.core.AuthnContext) authContextBuilder.buildObject();

                SAMLObjectBuilder authContextClassRefBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
                org.opensaml.saml2.core.AuthnContextClassRef authnContextClassRef = (org.opensaml.saml2.core.AuthnContextClassRef) authContextClassRefBuilder.buildObject();
                authnContextClassRef.setAuthnContextClassRef(saml2AuthnContext == null ? AuthnContext.X509_AUTHN_CTX : saml2AuthnContext);

                authnContext.setAuthnContextClassRef(authnContextClassRef);
                authnStatement.setAuthnContext(authnContext);

                // Builder Attributes
                SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.AttributeStatement.DEFAULT_ELEMENT_NAME);
                org.opensaml.saml2.core.AttributeStatement attrStatement = (org.opensaml.saml2.core.AttributeStatement) attrStatementBuilder.buildObject();

                // Create the attribute statement
                SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(org.opensaml.saml2.core.Attribute.DEFAULT_ELEMENT_NAME);
                org.opensaml.saml2.core.Attribute attrUser = (org.opensaml.saml2.core.Attribute) attrBuilder.buildObject();

                // Set custom Attributes
                XMLObjectBuilder stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
                XSString attrValueUser = (XSString) stringBuilder.buildObject(org.opensaml.saml2.core.AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                attrValueUser.setValue(strPolicyName);

                attrUser.getAttributeValues().add(attrValueUser);
                attrStatement.getAttributes().add(attrUser);

                // Conditions
                final SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) builderFactory
                        .getBuilder(org.opensaml.saml2.core.Conditions.DEFAULT_ELEMENT_NAME);
                final org.opensaml.saml2.core.Conditions conditions = (org.opensaml.saml2.core.Conditions) conditionsBuilder.buildObject();

                conditions.setNotBefore(conditionTimeNotBefore);
                conditions.setNotOnOrAfter(conditionTimeNotAfter);

                // Create Issuer
                SAMLObjectBuilder issuerBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
                Issuer issuer = (Issuer) issuerBuilder.buildObject();
                issuer.setValue(strIssuer);

                // Create the assertion
                assertionSaml2.setIssuer(issuer);
                assertionSaml2.setIssueInstant(issueInstant);
                assertionSaml2.setVersion(SAMLVersion.VERSION_20);
                assertionSaml2.setSubject(subject);
                assertionSaml2.getAuthnStatements().add(authnStatement);
                assertionSaml2.getAttributeStatements().add(attrStatement);
                assertionSaml2.setConditions(conditions);
                assertionSaml2.setID(strAssertionID);
                assertionSaml2.setSignature(signature);

            } else {
                // SAML 1.0
                // Create authentication statement subject
                final Subject authStatementSubject = getSubjectForSignedToken(builderFactory, config);

                // Create Authentication Statement
                final SAMLObjectBuilder authStatementBuilder = (SAMLObjectBuilder) builderFactory
                        .getBuilder(AuthenticationStatement.DEFAULT_ELEMENT_NAME);
                final AuthenticationStatement authnStatement = (AuthenticationStatement) authStatementBuilder
                        .buildObject();
                authnStatement.setSubject(authStatementSubject);
                authnStatement.setAuthenticationMethod(saml1AuthenticationMethod == null ? AgesicConstants.SAML10_PASSWD_AUTH_METHOD : saml1AuthenticationMethod);
                authnStatement.setAuthenticationInstant(authenticationInstant);

                // Create the attribute statement
                final SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder) builderFactory
                        .getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
                final Attribute attrGroups = (Attribute) attrBuilder.buildObject();
                attrGroups.setAttributeName(AgesicConstants.USER_ATTRIBUTE_NAME);
                attrGroups.setAttributeNamespace(strPolicyName);

                final XMLObjectBuilder stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
                final XSString attrNewValue = (XSString) stringBuilder
                        .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                attrNewValue.setValue(userName);

                attrGroups.getAttributeValues().add(attrNewValue);

                final SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) builderFactory
                        .getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
                final AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder
                        .buildObject();
                attrStatement.getAttributes().add(attrGroups);

                final Subject attrSubject = getSubjectForSignedToken(builderFactory, config);
                attrStatement.setSubject(attrSubject);

                // Conditions
                final SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) builderFactory
                        .getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
                final Conditions conditions = (Conditions) conditionsBuilder.buildObject();

                conditions.setNotBefore(conditionTimeNotBefore);
                conditions.setNotOnOrAfter(conditionTimeNotAfter);

                // Create the assertion
                assertionSaml1.setIssuer(strIssuer);
                assertionSaml1.setIssueInstant(issueInstant);
                assertionSaml1.setVersion(SAMLVersion.VERSION_10);
                assertionSaml1.getAuthenticationStatements().add(authnStatement);
                assertionSaml1.getAttributeStatements().add(attrStatement);
                assertionSaml1.setConditions(conditions);
                assertionSaml1.setID(strAssertionID);
                assertionSaml1.setSignature(signature);
            }

            // Get the marshaller factory
            final MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();

            // Get the Subject marshaller and generate assertion's dom
            // representation
            final Marshaller marshaller;
            final Element element;

            if (samlVersion.equals(SamlVersion.V2_0.getName())) {
                marshaller = marshallerFactory.getMarshaller(assertionSaml2);
                element = marshaller.marshall(assertionSaml2);
                element.setIdAttribute(org.opensaml.saml2.core.Assertion.ID_ATTRIB_NAME, true);
            } else {
                marshaller = marshallerFactory.getMarshaller(assertionSaml1);
                element = marshaller.marshall(assertionSaml1);
                element.setIdAttribute(Assertion.ASSERTIONID_ATTRIB_NAME, true);
            }

            // Sign SAML Assertion
            Signer.signObject(signature);

            // Print the assertion to standard output
            log.info("Assertion successfully created");
            log.debug(XMLHelper.prettyPrintXML(element));

            final SAMLAssertion samlAssertion = new SAMLAssertion();

            if (samlVersion.equals(SamlVersion.V2_0.getName())) {
                samlAssertion.setAssertionSaml2(assertionSaml2);
            } else {
                samlAssertion.setAssertionSaml1(assertionSaml1);
            }

            return samlAssertion;

        } catch (final SignatureException e) {
            throw new AssertionException(
                    "An error occurred while trying to sign the assertion: " + e.getMessage(), e);
        } catch (final MarshallingException e) {
            throw new AssertionException(
                    "An error occurred while trying to marshall the assertion: " + e.getMessage(),
                    e);
        } catch (final CertificateEncodingException e) {
            throw new AssertionException(
                    "An internal encoding error occurred while trying to create the assertion: "
                            + e.getMessage(),
                    e);
        } catch (final ConfigurationException e) {
            throw new AssertionException(
                    "A configuration error occurred with the bootstrap configuration: "
                            + e.getMessage(),
                    e);
        } catch (final NoSuchAlgorithmException e) {
            throw new AssertionException(
                    "An error occurred while trying to generate the UUID for the assertion: "
                            + e.getMessage(),
                    e);
        } catch (final Exception e) {
            throw new AssertionException(
                    "An error occurred while trying to generate signed assertion: "
                            + e.getMessage(),
                    e);
        }
    }

    @Override
    public ClientCredential getCredential(final String keyStorePwd, final String keyStoreFilePath,
                                          final String alias) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, UnrecoverableKeyException {

        final File keyStoreFile = new File(keyStoreFilePath);
        FileInputStream keyStoreFis = null;

        try {
            keyStoreFis = new FileInputStream(keyStoreFile);
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreFis, keyStorePwd.toCharArray());
            x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
            final java.security.Key key = keyStore.getKey(alias, keyStorePwd.toCharArray());
            final BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(x509Certificate);

            final Collection<X509CRL> crls = new ArrayList<X509CRL>();
            credential.setCRLs(crls);
            credential.setPrivateKey((PrivateKey) key);
            credential.setPublicKey(x509Certificate.getPublicKey());
            credential.getKeyNames().add(alias);

            final ClientCredential result = new ClientCredential();
            result.setCredential(credential);
            return result;
        } finally {
            if (keyStoreFis != null) {
                try {
                    keyStoreFis.close();
                } catch (final Exception exception) {
                    log.error("Error al cerrar el stream al leer el keystore " + keyStoreFilePath);
                }
            }
        }

    }

    @Override
    public SAMLAssertion getAssertionFromSOAP(final String string, final String samlVersion)
            throws NoAssertionFoundException, UnmarshalException, ParserException {

        // Get parser pool manager
        final BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);

        // Parse string message
        final StringReader in2 = new StringReader(string);
        Document doc = null;

        try {
            doc = ppMgr.parse(in2);
        } catch (final XMLParserException e) {
            e.printStackTrace();
            throw new ParserException(e.getMessage());
        }
        final String ns = samlVersion.equals(SamlVersion.V2_0.getName()) ? SAMLConstants.SAML20_NS : SAMLConstants.SAML1_NS;
        final NodeList assertionList = doc.getElementsByTagNameNS(ns, "Assertion");

        if (assertionList.getLength() == 0) {
            throw new NoAssertionFoundException();
        }

        if (assertionList.getLength() > 1) {
            log.warn("More than one assertion found in message");
        }

        final Element samlNode = (Element) assertionList.item(0);

        // Get appropriate unmarshaller
        final UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlNode);

        final SAMLAssertion samlAssertion = new SAMLAssertion();
        try {
            if (samlVersion.equals(SamlVersion.V2_0.getName())) {
                samlAssertion.setAssertionSaml2((org.opensaml.saml2.core.Assertion) unmarshaller.unmarshall(samlNode));
            } else {
                samlAssertion.setAssertionSaml1((Assertion) unmarshaller.unmarshall(samlNode));
            }
        } catch (final UnmarshallingException e) {
            e.printStackTrace();
            throw new UnmarshalException(e.getMessage());
        }

        return samlAssertion;
    }

}

