package uy.gub.agesic.pge.core.config;

import com.sun.xml.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import uy.gub.agesic.pge.exceptions.ConfigurationException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PGEConfiguration {
    private static final Pattern EXPANSION_PATTERN = Pattern.compile("(\\$\\{([^}]+?)\\})", Pattern.MULTILINE);
    private static Cipher dcipher;
    private final PGEConfig config;
    private final Map<String, String> stsValues = new HashMap<>();
    private final Map<String, String> authValues = new HashMap<>();
    private final Map<String, String> samlValues = new HashMap<>();

    public PGEConfiguration(PGEConfig config) {
        this.config = config;
        loadConfig();
    }

    public PGEConfiguration(String configFile) throws ConfigurationException {
        try {
            configFile = resolveEnvVars(configFile);
            JAXBContext context = JAXBContext.newInstance(PGEConfig.class);
            InputStream inputStream = getConfigStream(configFile);
            this.config = (PGEConfig) context.createUnmarshaller().unmarshal(inputStream);
            loadConfig();
        } catch (JAXBException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    private void loadConfig() {
        List<PGEConfig.KeyStore.Auth> authTypes = this.config.getKeyStore().getAuth();
        for (PGEConfig.KeyStore.Auth authType : authTypes) {
            this.authValues.put(authType.getKey(), authType.getValue());
        }

        List<PGEConfig.STSConfig.Property> stsPropertyTypes = this.config.getSTSConfig().getProperty();
        for (PGEConfig.STSConfig.Property stsPropertyType : stsPropertyTypes) {
            this.stsValues.put(stsPropertyType.getKey(), stsPropertyType.getValue());
        }

        List<PGEConfig.SAMLConfig.Property> samlPropertyTypes = this.config.getSAMLConfig().getProperty();
        for (PGEConfig.SAMLConfig.Property samlPropertyType : samlPropertyTypes) {
            this.samlValues.put(samlPropertyType.getKey(), samlPropertyType.getValue());
        }
    }

    public String getSTSURL() {
        return this.config.getSTSConfig().getUrl();
    }

    public String getKeyStoreAuthValue(String key) throws ConfigurationException {
        String value = this.authValues.get(key);
        if (value.startsWith("MASK-")) {
            String salt = this.authValues.get(ConfigProperties.SALT);
            int iterationCount = Integer.parseInt(this.authValues.get(ConfigProperties.ITERATION_COUNT));
            try {
                // provide password, salt, iteration count for generating PBEKey of fixed-key-size PBE ciphers
                KeySpec keySpec = new PBEKeySpec(value.toCharArray(), salt.getBytes(), iterationCount);
                // create a secret (symmetric) key using PBE with MD5 and DES
                SecretKey secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
                // construct a parameter set for password-based encryption as defined in the PKCS #5 standard
                AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt.getBytes(), iterationCount);

                dcipher = Cipher.getInstance(secretKey.getAlgorithm());
                // initialize the ciphers with the given secret key
                dcipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);

                return decrypt(value);
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }

        return resolveEnvVars(value);
    }

    private String decrypt(String str) {
        try {
            // decode with base64 to get bytes
            byte[] dec = BASE64DecoderStream.decode(str.getBytes());
            byte[] utf8 = dcipher.doFinal(dec);
            // create new string based on the specified charset

            return new String(utf8, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getSTSPropValue(String key) {
        return this.stsValues.get(key);
    }

    public String getSAMLPropValue(String key) {
        return this.samlValues.get(key);
    }

    public Long getSTSLongPropValue(String key) {
        if (this.stsValues.get(key) != null) {
            return Long.parseLong(this.stsValues.get(key));
        }

        return null;
    }

    private String resolveEnvVars(String input) {
        if (input == null) return null;

        Map<?, ?> props = System.getProperties();
        Matcher matcher = EXPANSION_PATTERN.matcher(input);
        StringBuilder expanded = new StringBuilder(input.length());
        while (matcher.find()) {
            String propName = matcher.group(2);
            String value = (String) props.get(propName);
            if (value == null) value = matcher.group(0);
            matcher.appendReplacement(expanded, "");
            expanded.append(value);
        }
        matcher.appendTail(expanded);

        return expanded.toString();
    }

    private InputStream getConfigStream(String configPath) {
        try {
            File file = new File(configPath);
            return new FileInputStream(file);
        } catch (Exception e) {
            URL url;
            try {
                url = new URL(configPath);
                return url.openStream();
            } catch (Exception ex) {
                url = SecurityActions.loadResource(PGEConfiguration.class, configPath);
                if (url != null) {
                    try {
                        return url.openStream();
                    } catch (IOException e1) {
                        return null;
                    }
                }
            }
            throw new RuntimeException("Config file not located: " + configPath);
        }
    }
}