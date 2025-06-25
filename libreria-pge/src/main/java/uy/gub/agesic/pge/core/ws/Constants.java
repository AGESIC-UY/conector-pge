package uy.gub.agesic.pge.core.ws;

import javax.xml.namespace.QName;

public class Constants {
    public static final String WSS_SOAP_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0";
    public static final String WSSE_LOCAL = "Security";
    public static final String WSSE_PREFIX = "wsse";
    public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE_BINARY_SECURITY_TOKEN = "BinarySecurityToken";
    public static final String WSSE_USERNAME_TOKEN = "UsernameToken";
    public static final String WSSE_USERNAME = "Username";
    public static final String WSSE_PASSWORD = "Password";
    public static final String WSSE_PASSWORD_TEXT_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0#PasswordText";
    public static final String WSSE_ENCODING_TYPE = "EncodingType";
    public static final String WSSE_VALUE_TYPE = "ValueType";
    public static final String WSU_PREFIX = "wsu";
    public static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public static final String XML_SIGNATURE_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String XML_ENCRYPTION_NS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String XML_ENCRYPTION_PREFIX = "ds";
    public static final String ID = "Id";
    public static final String WSU_ID = "wsu:Id";
    public static final String BASE64_ENCODING_TYPE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";
    public static final String PASSWORD_TEXT_TYPE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
    public static final String PASSWORD_DIGEST_TYPE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest";
    public static final String WSSE_HEADER = "wsse:Security";
    public static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";
    public static final String XENC_DATAREFERENCE = "DataReference";
    public static final String XENC_REFERENCELIST = "ReferenceList";
    public static final String XENC_ELEMENT_TYPE = "http://www.w3.org/2001/04/xmlenc#Element";
    public static final String XENC_CONTENT_TYPE = "http://www.w3.org/2001/04/xmlenc#Content";
    public static final QName WSSE_HEADER_QNAME = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
}