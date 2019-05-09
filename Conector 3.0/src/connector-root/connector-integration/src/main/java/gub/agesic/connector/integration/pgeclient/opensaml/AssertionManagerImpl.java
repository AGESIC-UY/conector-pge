package gub.agesic.connector.integration.pgeclient.opensaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.Attribute;
import org.opensaml.saml1.core.AttributeStatement;
import org.opensaml.saml1.core.AttributeValue;
import org.opensaml.saml1.core.AuthenticationStatement;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.ConfirmationMethod;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml1.core.Subject;
import org.opensaml.saml1.core.SubjectConfirmation;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.integration.pgeclient.AgesicConstants;
import gub.agesic.connector.integration.pgeclient.AssertionManager;
import gub.agesic.connector.integration.pgeclient.beans.ClientCredential;
import gub.agesic.connector.integration.pgeclient.beans.SAMLAssertion;
import gub.agesic.connector.integration.pgeclient.exceptions.AssertionException;
import gub.agesic.connector.integration.pgeclient.exceptions.NoAssertionFoundException;
import gub.agesic.connector.integration.pgeclient.exceptions.ParserException;
import gub.agesic.connector.integration.pgeclient.exceptions.UnmarshalException;

public class AssertionManagerImpl implements AssertionManager {

    private static X509Certificate x509Certificate;
    private static final Logger log = Logger.getLogger(AssertionManagerImpl.class);

    @SuppressWarnings("unchecked")
    private Subject getSubjectForSignedToken(final String role,
            final XMLObjectBuilderFactory builderFactory) {

        // Create the NameIdentifier
        final SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory
                .getBuilder(NameIdentifier.DEFAULT_ELEMENT_NAME);
        final NameIdentifier nameId = (NameIdentifier) nameIdBuilder.buildObject();
        nameId.setNameIdentifier(role);
        nameId.setFormat(NameIdentifier.EMAIL);

        // Create the SubjectConfirmation
        final SAMLObjectBuilder confirmationMethodBuilder = (SAMLObjectBuilder) builderFactory
                .getBuilder(ConfirmationMethod.DEFAULT_ELEMENT_NAME);
        final ConfirmationMethod confirmationMethod = (ConfirmationMethod) confirmationMethodBuilder
                .buildObject();
        confirmationMethod.setConfirmationMethod(AgesicConstants.SAML10_BEARER_CONFIRMATION_METHOD);

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

    @Override
    public ClientCredential getCredentialFromAssertion(final SAMLAssertion samlAssertion) {
        final Assertion assertion = samlAssertion.getAssertion();

        final Signature signature = assertion.getSignature();
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
        final Collection<X509CRL> crls = new ArrayList<X509CRL>();
        credential.setEntityCertificate(x509Cert2);
        credential.setCRLs(crls);
        credential.setPublicKey(x509Cert2.getPublicKey());

        // Wrap it in a PGE ClientCredential
        final ClientCredential result = new ClientCredential();
        result.setCredential(credential);
        return result;

    }

    @Override
    public SAMLAssertion generateSignedAssertion(final ClientCredential signingCredential,
            final Connector connector, final String strPolicyName) throws AssertionException {

        final Credential credential = signingCredential.getCredential();

        final DateTime authenticationInstant = new DateTime();
        final DateTime issueInstant = new DateTime();
        final DateTime conditionTimeNotBefore = new DateTime().minusMinutes(15);
        final DateTime conditionTimeNotAfter = new DateTime().plusMinutes(15);

        // SAML User Info
        final String strIssuer = connector.getIssuer();
        final String strRole = connector.getActualRoleOperation().getRole();
        // final String strPolicyName = connector.getPolicyName();
        final String userName = connector.getUsername();

        try {

            DefaultBootstrap.bootstrap();
            final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

            // Create authentication statement subject
            final Subject authStatementSubject = getSubjectForSignedToken(strRole, builderFactory);

            // Create Authentication Statement
            final SAMLObjectBuilder authStatementBuilder = (SAMLObjectBuilder) builderFactory
                    .getBuilder(AuthenticationStatement.DEFAULT_ELEMENT_NAME);
            final AuthenticationStatement authnStatement = (AuthenticationStatement) authStatementBuilder
                    .buildObject();
            authnStatement.setSubject(authStatementSubject);
            authnStatement.setAuthenticationMethod(AgesicConstants.SAML10_PASSWD_AUTH_METHOD);
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

            final Subject attrSubject = getSubjectForSignedToken(strRole, builderFactory);
            attrStatement.setSubject(attrSubject);

            final SAMLObjectBuilder conditionsBuilder = (SAMLObjectBuilder) builderFactory
                    .getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
            final Conditions conditions = (Conditions) conditionsBuilder.buildObject();

            conditions.setNotBefore(conditionTimeNotBefore);
            conditions.setNotOnOrAfter(conditionTimeNotAfter);

            // Create assertionID
            final IdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();
            final String strAssertionID = idGenerator.generateIdentifier();

            // Create the assertion
            final SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) builderFactory
                    .getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
            final Assertion assertion = (Assertion) assertionBuilder.buildObject();
            assertion.setIssuer(strIssuer);
            assertion.setIssueInstant(issueInstant);
            assertion.setVersion(SAMLVersion.VERSION_10);

            assertion.getAuthenticationStatements().add(authnStatement);
            assertion.getAttributeStatements().add(attrStatement);
            assertion.setConditions(conditions);

            // Create signature
            final Signature signature = (Signature) Configuration.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                    .buildObject(Signature.DEFAULT_ELEMENT_NAME);

            signature.setSigningCredential(credential);
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
            signature.setCanonicalizationAlgorithm(
                    SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            final KeyInfoBuilder keyInfoBuilder = (KeyInfoBuilder) builderFactory
                    .getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
            final KeyInfo keyinfo = keyInfoBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            KeyInfoHelper.addCertificate(keyinfo, x509Certificate);
            signature.setKeyInfo(keyinfo);

            assertion.setID(strAssertionID);
            assertion.setSignature(signature);

            // Get the marshaller factory
            final MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();

            // Get the Subject marshaller and generate assertion's dom
            // representation
            final Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
            final Element element = marshaller.marshall(assertion);

            element.setIdAttribute(Assertion.ASSERTIONID_ATTRIB_NAME, true);

            // Sign SAML Assertion
            Signer.signObject(signature);

            // Print the assertion to standard output
            log.info("Assertion succesfully created");
            log.debug(XMLHelper.prettyPrintXML(element));

            final SAMLAssertion samlAssertion = new SAMLAssertion();
            samlAssertion.setAssertion(assertion);

            return samlAssertion;

        } catch (final SignatureException e) {
            throw new AssertionException(
                    "An error ocurred while trying to sign the assertion: " + e.getMessage(), e);
        } catch (final MarshallingException e) {
            throw new AssertionException(
                    "An error ocurred while trying to marshall the assertion: " + e.getMessage(),
                    e);
        } catch (final CertificateEncodingException e) {
            throw new AssertionException(
                    "An internal encoding error ocurred while trying to create the assertion: "
                            + e.getMessage(),
                    e);
        } catch (final ConfigurationException e) {
            throw new AssertionException(
                    "A configuration error ocurred with the bootstrap configuration: "
                            + e.getMessage(),
                    e);
        } catch (final NoSuchAlgorithmException e) {
            throw new AssertionException(
                    "An error ocurred while trying to generate the UUID for the assertion: "
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
    public SAMLAssertion getAssertionFromSOAP(final String string)
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
        final NodeList assertionList = doc.getElementsByTagNameNS(SAMLConstants.SAML1_NS,
                "Assertion");

        if (assertionList.getLength() == 0) {
            throw new NoAssertionFoundException();
        }

        if (assertionList.getLength() > 1) {
            log.warn("More than one assertion found in message");
        }

        final Element samlNode = (Element) assertionList.item(0);

        // Get apropriate unmarshaller
        final UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlNode);

        final Assertion assertion;
        try {
            assertion = (Assertion) unmarshaller.unmarshall(samlNode);
        } catch (final UnmarshallingException e) {
            e.printStackTrace();
            throw new UnmarshalException(e.getMessage());
        }
        final SAMLAssertion samlAssertion = new SAMLAssertion();
        samlAssertion.setAssertion(assertion);
        return samlAssertion;

    }

}
