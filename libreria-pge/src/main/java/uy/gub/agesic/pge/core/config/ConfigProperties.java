package uy.gub.agesic.pge.core.config;

public interface ConfigProperties {

    // STS Config
    String SSLCONTEXT_INITIALIZER = "SSLContextInitializer";
    String STS_TIMEOUT = "STSTimeOut";
    String TOKEN_TIMEOUT = "TokenTimeOut";
    String ISSUER = "Issuer";
    String POLICY = "Policy";
    String ROLE = "Role";
    String USERNAME = "Username";
    String WSA_TO = "WsaTo";
    String SAML_VERSION = "SAMLVersion";
    String SOAP_VERSION = "SOAPVersion";
    String CACHE_TOKEN_ENABLED = "CacheTokenEnabled";

    // Keystore
    String TRUST_STORE_URL = "TrustStoreURL";
    String TRUST_STORE_PASS = "TrustStorePass";
    String SSL_KEY_STORE_URL = "SSLKeyStoreURL";
    String SSL_KEY_STORE_PASS = "SSLKeyStorePass";
    String KEY_STORE_ALIAS = "KeyStoreAlias";
    String KEY_STORE_ALIAS_SSL = "KeyStoreAliasSSL";
    String KEY_STORE_FILE_URL = "KeyStoreURL";
    String KEY_STORE_PASS = "KeyStorePass";
    String SALT = "salt";
    String ITERATION_COUNT = "iterationCount";

    // SAML Config
    String SAML1_NAME_ID_FORMAT = "SAML1NameIDFormat";
    String SAML1_CONFIRMATION_METHOD = "SAML1ConfirmationMethod";
    String SAML1_AUTHENTICATION_METHOD = "SAML1AuthenticationMethod";

    String SAML2_NAME_ID_FORMAT = "SAML2NameIDFormat";
    String SAML2_CONFIRMATION_METHOD = "SAML2ConfirmationMethod";
    String SAML2_AUTHN_CONTEXT = "SAML2AuthnContext";

    String SIGNATURE_ALGORITHM = "SignatureAlgorithm";
    String SIGNATURE_CANONICALIZATION_ALGORITHM = "SignatureCanonicalizationAlgorithm";

    String MAX_TOTAL_OPEN_CONNECTIONS = "MaxTotalOpenConnections";
    String MAX_CONNECTIONS_PER_ROUTE = "MaxConnectionsPerRoute";
    String MAX_CONNECTIONS_HOSTNAME_PORT = "MaxConnectionsHostnamePort";
}