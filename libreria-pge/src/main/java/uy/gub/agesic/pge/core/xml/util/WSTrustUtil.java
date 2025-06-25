package uy.gub.agesic.pge.core.xml.util;

//import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
//import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
//import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
//import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
//import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
//import org.picketlink.identity.federation.saml.v1.assertion.*;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml1.core.*;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uy.gub.agesic.pge.AgesicConstants;
import uy.gub.agesic.pge.beans.ClientCredential;
import uy.gub.agesic.pge.beans.RSTBean;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfig;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.enums.SamlVersion;
import uy.gub.agesic.pge.enums.SoapVersion;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;
import uy.gub.agesic.pge.opensaml.AssertionManagerImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class WSTrustUtil {
    private static final Logger log = Logger.getLogger(WSTrustUtil.class);
    public static final Long DEFAULT_TOKENTIMEOUT = 900000L;

    public static SOAPMessage createRequestSecurityTokenMessage(ClientCredential credential, PGEConfiguration config, String serviceName) throws RequestSecurityTokenException {
        SamlVersion samlVersion = SamlVersion.fromString(config.getSTSPropValue(ConfigProperties.SAML_VERSION));
        String soapVersion = config.getSTSPropValue(ConfigProperties.SOAP_VERSION);
        String strIssuer = config.getSTSPropValue(ConfigProperties.ISSUER);
        String role = config.getSTSPropValue(ConfigProperties.ROLE);
        String policy = config.getSTSPropValue(ConfigProperties.POLICY);
        String username = config.getSTSPropValue(ConfigProperties.USERNAME);
        Long tokenTimeOut = config.getSTSLongPropValue(ConfigProperties.TOKEN_TIMEOUT);

        String saml1AuthenticationMethod = config.getSAMLPropValue(ConfigProperties.SAML1_AUTHENTICATION_METHOD);
        String saml2AuthnContext = config.getSAMLPropValue(ConfigProperties.SAML2_AUTHN_CONTEXT);
        String signatureAlgorithm = config.getSAMLPropValue(ConfigProperties.SIGNATURE_ALGORITHM);
        String canonicalizationAlgorithm = config.getSAMLPropValue(ConfigProperties.SIGNATURE_CANONICALIZATION_ALGORITHM);

        final DateTime authenticationInstant = new DateTime();
        final DateTime issueInstant = new DateTime();
        if (tokenTimeOut == null) {
            tokenTimeOut = DEFAULT_TOKENTIMEOUT;
        }
        final DateTime conditionTimeNotBefore = new DateTime().minus(tokenTimeOut);
        final DateTime conditionTimeNotAfter = new DateTime().plus(tokenTimeOut);

        try {
            // SAML User Info
            DefaultBootstrap.bootstrap();
            final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

            // Create signature
            final Signature signature = (Signature) Configuration.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                    .buildObject(Signature.DEFAULT_ELEMENT_NAME);

            signature.setSigningCredential(credential.getCredential());
            signature.setSignatureAlgorithm(signatureAlgorithm == null ? SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1 : signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(
                    canonicalizationAlgorithm == null ? SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS : canonicalizationAlgorithm);

            final KeyInfoBuilder keyInfoBuilder = (KeyInfoBuilder) builderFactory
                    .getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
            final KeyInfo keyinfo = keyInfoBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            X509Certificate x509Certificate = ((BasicX509Credential) credential.getCredential()).getEntityCertificate();
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

            if (samlVersion == SamlVersion.V2_0) {
                // SAML 2.0
                // Create authentication statement subject
                final org.opensaml.saml2.core.Subject subject = AssertionManagerImpl.getSubjectForSignedTokenSaml2(builderFactory, config);

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
                attrValueUser.setValue(policy);

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
                final Subject authStatementSubject = AssertionManagerImpl.getSubjectForSignedToken(builderFactory, config);

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
                attrGroups.setAttributeNamespace(policy);

                final XMLObjectBuilder stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
                final XSString attrNewValue = (XSString) stringBuilder
                        .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                attrNewValue.setValue(username);

                attrGroups.getAttributeValues().add(attrNewValue);

                final SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) builderFactory
                        .getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
                final AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder
                        .buildObject();
                attrStatement.getAttributes().add(attrGroups);

                final Subject attrSubject = AssertionManagerImpl.getSubjectForSignedToken(builderFactory, config);
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

            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPPart part = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();

            // Get the marshaller factory
            final MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();

            // Get the Subject marshaller and generate assertion's dom
            // representation
            final Marshaller marshaller;
            final Element assertion;

            if (samlVersion == SamlVersion.V2_0) {
                marshaller = marshallerFactory.getMarshaller(assertionSaml2);
                assertion = marshaller.marshall(assertionSaml2);
                assertion.setIdAttribute(org.opensaml.saml2.core.Assertion.ID_ATTRIB_NAME, true);
                final String xmlns = soapVersion.equals(SoapVersion.V1_2.getName()) ? AgesicConstants.NAMESPACE_SOAP_1_2 : AgesicConstants.NAMESPACE_SOAP_1_1;
                envelope.addNamespaceDeclaration("soapenv", xmlns);
                envelope.addNamespaceDeclaration("wst", "http://docs.oasis-open.org/ws-sx/ws-trust/200512");
                envelope.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
            } else {
                envelope.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
                marshaller = marshallerFactory.getMarshaller(assertionSaml1);
                assertion = marshaller.marshall(assertionSaml1);
                assertion.setIdAttribute(Assertion.ASSERTIONID_ATTRIB_NAME, true);
            }

            // Sign SAML Assertion
            Signer.signObject(signature);

            // Print the assertion to standard output
            log.info("Assertion succesfully created");
            log.debug(XMLHelper.prettyPrintXML(assertion));

            addHeader(soapMessage, samlVersion, soapVersion);
            addRequestSecurityToken(soapMessage, policy, serviceName, assertion, role, samlVersion);

            return soapMessage;
        } catch (Exception e) {
            throw new RequestSecurityTokenException(e.getMessage(), e);
        }
    }

    private static void addHeader(SOAPMessage soapMessage, SamlVersion samlVersion, String soapVersion) throws SOAPException {
        String messageID = UUID.randomUUID().toString();
        SOAPFactory factory = SOAPFactory.newInstance();

        if (samlVersion == SamlVersion.V2_0) {
            SOAPElement msgIdElement = factory.createElement("MessageID");
            msgIdElement.addTextNode(String.format("uuid-%s", messageID));
            soapMessage.getSOAPHeader().addChildElement(msgIdElement);

            SOAPElement actionElement = factory.createElement("Action");
            actionElement.addTextNode("http://docs.oasis-open.org/ws-sx/ws-trust/200512/RequestSecurityToken");
            soapMessage.getSOAPHeader().addChildElement(actionElement);
        } else {
            SOAPElement actionElement = factory.createElement("Action", "wsa", "http://www.w3.org/2005/08/addressing");
            final String xmlns = soapVersion.equals(SoapVersion.V1_2.getName()) ? AgesicConstants.NAMESPACE_SOAP_1_2 : AgesicConstants.NAMESPACE_SOAP_1_1;
            QName actionName = new QName(xmlns, "mustUnderstand", "env");
            actionElement.addAttribute(actionName, "1");
            actionElement.addTextNode(" ");
            soapMessage.getSOAPHeader().addChildElement(actionElement);

            SOAPElement msgIdElement = factory.createElement("MessageID", "wsa", "http://www.w3.org/2005/08/addressing");
            msgIdElement.addTextNode(String.format("urn:uuid:%s", messageID));
            soapMessage.getSOAPHeader().addChildElement(msgIdElement);
        }
    }

    private static void addRequestSecurityToken(SOAPMessage soapMessage, String policyName, String service, Element assertion, String role, SamlVersion samlVersion) throws SOAPException {
        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement requestSecurityToken;
        if (samlVersion == SamlVersion.V2_0) {
            requestSecurityToken = factory.createElement("RequestSecurityToken");
            requestSecurityToken.setPrefix("wst");

            SOAPElement tokenType = factory.createElement("TokenType");
            tokenType.setPrefix("wst");
            tokenType.addTextNode("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
            requestSecurityToken.addChildElement(tokenType);

            SOAPElement appliesTo = factory.createElement("AppliesTo", "wsp", "http://schemas.xmlsoap.org/ws/2004/09/policy");
            SOAPElement endpointReference = factory.createElement("EndpointReference");
            endpointReference.setPrefix("wsa");
            SOAPElement address = factory.createElement("Address");
            address.setPrefix("wsa");

            address.addTextNode(service);
            endpointReference.addChildElement(address);
            appliesTo.addChildElement(endpointReference);
            requestSecurityToken.addChildElement(appliesTo);

            SOAPElement requestType = factory.createElement("RequestType");
            requestType.setPrefix("wst");
            requestType.addTextNode("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue");
            requestSecurityToken.addChildElement(requestType);

            SOAPElement issuer = factory.createElement("Issuer");
            issuer.setPrefix("wst");
            address = factory.createElement("Address");
            address.setPrefix("wsa");
            address.addTextNode(policyName);
            issuer.addChildElement(address);
            requestSecurityToken.addChildElement(issuer);

            SOAPElement base = factory.createElement("Base");
            base.setPrefix("wst");
            Document doc = base.getOwnerDocument();
            assertion = (Element) doc.importNode(assertion, true);
            base.appendChild(assertion);
            requestSecurityToken.addChildElement(base);

        } else {
            requestSecurityToken = factory.createElement("RequestSecurityToken", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            requestSecurityToken.addNamespaceDeclaration("wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");

            SOAPElement tokenType = factory.createElement("TokenType", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            tokenType.addTextNode("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1");
            requestSecurityToken.addChildElement(tokenType);

            SOAPElement appliesTo = factory.createElement("AppliesTo", "wsp", "http://schemas.xmlsoap.org/ws/2004/09/policy");
            SOAPElement endpointReference = factory.createElement("EndpointReference", "wsa", "http://www.w3.org/2005/08/addressing");
            SOAPElement address = factory.createElement("Address", "wsa", "http://www.w3.org/2005/08/addressing");
            address.addTextNode(service);
            endpointReference.addChildElement(address);
            appliesTo.addChildElement(endpointReference);
            requestSecurityToken.addChildElement(appliesTo);

            SOAPElement requestType = factory.createElement("RequestType", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            requestType.addTextNode("http://schemas.xmlsoap.org/ws/2005/02/trust/Issue");
            requestSecurityToken.addChildElement(requestType);

            SOAPElement issuer = factory.createElement("Issuer", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            address = factory.createElement("Address", "wsa", "http://www.w3.org/2005/08/addressing");
            address.addTextNode(policyName);
            issuer.addChildElement(address);
            requestSecurityToken.addChildElement(issuer);

            SOAPElement base = factory.createElement("Base", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            Document doc = base.getOwnerDocument();
            assertion = (Element) doc.importNode(assertion, true);
            base.appendChild(assertion);
            requestSecurityToken.addChildElement(base);

            SOAPElement secondaryParameters = factory.createElement("SecondaryParameters", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            SOAPElement roleElement = factory.createElement("SecondaryParameters", "wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
            roleElement.addTextNode(role);
            secondaryParameters.addChildElement(roleElement);
            requestSecurityToken.addChildElement(secondaryParameters);

        }
        soapMessage.getSOAPBody().addChildElement(requestSecurityToken);
    }
}