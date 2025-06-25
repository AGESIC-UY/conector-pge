package uy.gub.agesic.pge.core.security;

import org.apache.log4j.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class KeyStoreUtil {
    private static final Logger LOGGER = Logger.getLogger(KeyStoreUtil.class);

    public static KeyStore loadKeyStore(String keyStoreUrl, String keyStorePwd) {
        LOGGER.info("Loading keystore at " + keyStoreUrl);
        InputStream keyStoreFis = getKeyStoreInputStream(keyStoreUrl);

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreFis, keyStorePwd.toCharArray());
            return keyStore;
        } catch (Exception e) {
            LOGGER.error("Error loading keystore at " + keyStoreUrl, e);
            throw new RuntimeException(e);
        }
    }

    public static InputStream getKeyStoreInputStream(String keyStore) {
        InputStream is;
        try {
            File file = new File(keyStore);
            is = new FileInputStream(file);
            return is;
        } catch (Exception e) {
            URL url;
            try {
                url = new URL(keyStore);
                is = url.openStream();
                return is;
            } catch (Exception ex) {
                url = SecurityActions.loadResource(KeyStoreUtil.class, keyStore);
                if (url != null) {
                    try {
                        is = url.openStream();
                        return is;
                    } catch (IOException e1) {
                        LOGGER.error("Error getting keystore input stream from " + keyStore, e);
                    }
                }
                throw new RuntimeException("Keystore not located: " + keyStore);
            }
        }
    }

    public static String getKeystorePath(String keyStore) {
        InputStream is = null;
        try {
            File file = new File(keyStore);
            is = new FileInputStream(file);
            return keyStore;
        } catch (Exception e) {
            URL url;
            try {
                url = new URL(keyStore);
                return url.toString();
            } catch (Exception ex) {
                url = SecurityActions.loadResource(KeyStoreUtil.class, keyStore);
                if (url != null) {
                    return url.toString();
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                    }
                }
            }
            throw new RuntimeException("Keystore not located: " + keyStore);
        }
    }

    public static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
        fac.init(trustStore);
        return fac.getTrustManagers();
    }

    public static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
        String alg = KeyManagerFactory.getDefaultAlgorithm();
        char[] keyPass = (keyPassword != null) ? keyPassword.toCharArray() : null;
        KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
        fac.init(keyStore, keyPass);
        return fac.getKeyManagers();
    }
}