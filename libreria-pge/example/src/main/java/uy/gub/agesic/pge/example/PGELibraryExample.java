package uy.gub.agesic.pge.example;

import uy.gub.agesic.pge.beans.STSResponse;
import uy.gub.agesic.pge.client.PGEClient;
import uy.gub.agesic.pge.client.PGEClientBasic;
import uy.gub.agesic.pge.client.PGEClientCache;
import uy.gub.agesic.pge.core.config.ConfigProperties;
import uy.gub.agesic.pge.core.config.PGEConfig;
import uy.gub.agesic.pge.core.config.PGEConfiguration;
import uy.gub.agesic.pge.exceptions.ConfigurationException;
import uy.gub.agesic.pge.exceptions.RequestSecurityTokenException;

import java.util.ArrayList;
import java.util.List;

public class PGELibraryExample {

    public static void main(String[] args) {
        System.out.println("Ejemplo de Biblioteca PGE");

        // Authenticate to the PGE
        final STSResponse response;
        try {
            PGEClient pgeClient = new PGEClientCache(new PGEClientBasic());

            // PGE Config
            PGEConfig pgeConfig = new PGEConfig();

            // STS Config
            PGEConfig.STSConfig stsConfig = loadSTSConfig();
            pgeConfig.setSTSConfig(stsConfig);

            // Keystore
            PGEConfig.KeyStore keyStore = loadKeyStoreConfig();
            pgeConfig.setKeyStore(keyStore);

            // SAML Config
            PGEConfig.SAMLConfig samlConfig = loadSAMLConfig();
            pgeConfig.setSAMLConfig(samlConfig);

            // PGE Configuraion
            PGEConfiguration pgeConfiguration = new PGEConfiguration(pgeConfig);
            response = pgeClient.requestSecurityToken(pgeConfiguration);
            System.out.println("SAML Assertion Response ID");
            System.out.println(response.getAssertion().getAssertionSaml1().getID());

        } catch (final RequestSecurityTokenException exception) {
            System.out.println("Error al solicitar un token saml al STS" + exception.getMessage());
        } catch (final ConfigurationException exception) {
            System.out.println("Error al cargar la configuracion en la libreria PGE" + exception.getMessage());
        } catch (Exception exception) {
            System.out.println("Error inicializando la libreria PGE" + exception.getMessage());
        }
    }

    private static PGEConfig.STSConfig loadSTSConfig() {
        List<PGEConfig.STSConfig.Property> stsProperties = new ArrayList<>();
        PGEConfig.STSConfig.Property stsProperty;

        // Issuer
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.ISSUER);
        stsProperty.setValue("PGE-Library-Example");
        stsProperties.add(stsProperty);
        // Username
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.USERNAME);
        stsProperty.setValue("pge-library");
        stsProperties.add(stsProperty);
        // WSA To
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.WSA_TO);
        stsProperty.setValue("http://testservicios.pge.red.uy/timestamp");
        stsProperties.add(stsProperty);
        // SAML version
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.SAML_VERSION);
        stsProperty.setValue("1.1"); // "1.1" | "2.0"
        stsProperties.add(stsProperty);
        // Cache token enabled
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.CACHE_TOKEN_ENABLED);
        stsProperty.setValue("0"); // Yes: "1" | No: "0"
        stsProperties.add(stsProperty);
        // Role
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.ROLE);
        stsProperty.setValue("ou=test,o=agesic");
        stsProperties.add(stsProperty);
        // SOAP version
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.SOAP_VERSION);
        stsProperty.setValue("1.1"); // "1.1" | "1.2" | "multiple"
        stsProperties.add(stsProperty);
        // Policy
        stsProperty = new PGEConfig.STSConfig.Property();
        stsProperty.setKey(ConfigProperties.POLICY);
        stsProperty.setValue("urn:tokensimple");
        stsProperties.add(stsProperty);

        PGEConfig.STSConfig stsConfig = new PGEConfig.STSConfig();
        // STS URL
        stsConfig.setUrl("https://testservicios.pge.red.uy:6051/TrustServer/SecurityTokenServiceProtected");
        stsConfig.setProperty(stsProperties);

        return stsConfig;
    }

    private static PGEConfig.KeyStore loadKeyStoreConfig() {
        List<PGEConfig.KeyStore.Auth> auths = new ArrayList<>();
        PGEConfig.KeyStore.Auth auth = new PGEConfig.KeyStore.Auth();

        // Alias
        auth.setKey(ConfigProperties.KEY_STORE_ALIAS);
        auth.setValue("conector.pge.red.uy");
        auths.add(auth);
        // Alias SSL
        auth.setKey(ConfigProperties.KEY_STORE_ALIAS_SSL);
        auth.setValue("conector.pge.red.uy");
        auths.add(auth);
        // Keystore Org URL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.KEY_STORE_FILE_URL);
        auth.setValue("libs/conector.keystore");
        auths.add(auth);
        // Keystore Org Pass
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.KEY_STORE_PASS);
        auth.setValue("ConectorPGE2019");
        auths.add(auth);
        // SSL Keystore URL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.SSL_KEY_STORE_URL);
        auth.setValue("libs/conector.keystore");
        auths.add(auth);
        // SSL Keystore Pass
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.SSL_KEY_STORE_PASS);
        auth.setValue("ConectorPGE2019");
        auths.add(auth);
        // Truststore URL
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.TRUST_STORE_URL);
        auth.setValue("libs/conector.truststore");
        auths.add(auth);
        // Truststore Pass
        auth = new PGEConfig.KeyStore.Auth();
        auth.setKey(ConfigProperties.TRUST_STORE_PASS);
        auth.setValue("ConectorPGE2019");
        auths.add(auth);

        PGEConfig.KeyStore keyStore = new PGEConfig.KeyStore();
        keyStore.setAuth(auths);

        return keyStore;
    }

    private static PGEConfig.SAMLConfig loadSAMLConfig() {
        List<PGEConfig.SAMLConfig.Property> samlProperties = new ArrayList<>();
        PGEConfig.SAMLConfig.Property samlProperty;

        // PGE Client Config
        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SAML1_NAME_ID_FORMAT);
        samlProperty.setValue("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SAML1_CONFIRMATION_METHOD);
        samlProperty.setValue("urn:oasis:names:tc:SAML:1.0:cm:bearer");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SAML1_AUTHENTICATION_METHOD);
        samlProperty.setValue("urn:oasis:names:tc:SAML:1.0:am:password");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SAML2_NAME_ID_FORMAT);
        samlProperty.setValue("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SAML2_CONFIRMATION_METHOD);
        samlProperty.setValue("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SAML2_AUTHN_CONTEXT);
        samlProperty.setValue("urn:oasis:names:tc:SAML:2.0:ac:classes:X509");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SIGNATURE_ALGORITHM);
        samlProperty.setValue("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.SIGNATURE_CANONICALIZATION_ALGORITHM);
        samlProperty.setValue("http://www.w3.org/2001/10/xml-exc-c14n#");
        samlProperties.add(samlProperty);

        // Connector Config
        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.MAX_TOTAL_OPEN_CONNECTIONS);
        samlProperty.setValue("200");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.MAX_CONNECTIONS_PER_ROUTE);
        samlProperty.setValue("10");
        samlProperties.add(samlProperty);

        samlProperty = new PGEConfig.SAMLConfig.Property();
        samlProperty.setKey(ConfigProperties.MAX_CONNECTIONS_HOSTNAME_PORT);
        samlProperty.setValue("50");
        samlProperties.add(samlProperty);

        PGEConfig.SAMLConfig samlConfig = new PGEConfig.SAMLConfig();
        samlConfig.setProperty(samlProperties);

        return samlConfig;
    }
}
