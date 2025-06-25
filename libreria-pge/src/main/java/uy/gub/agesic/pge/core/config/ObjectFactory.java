package uy.gub.agesic.pge.core.config;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public PGEConfig.STSConfig createPGEConfigSTSConfig() {
        return new PGEConfig.STSConfig();
    }

    public PGEConfig.STSConfig.Property createPGEConfigSTSConfigProperty() {
        return new PGEConfig.STSConfig.Property();
    }

    public PGEConfig createPGEConfig() {
        return new PGEConfig();
    }

    public PGEConfig.KeyStore.Auth createPGEConfigKeyStoreAuth() {
        return new PGEConfig.KeyStore.Auth();
    }

    public PGEConfig.KeyStore createPGEConfigKeyStore() {
        return new PGEConfig.KeyStore();
    }
}