package gub.agesic.connector.services.keystoremanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gub.agesic.connector.dataaccess.entity.Certificado;
import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.ConnectorLocalConfiguration;
import gub.agesic.connector.dataaccess.entity.KeystoreModalData;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.filemanager.FileManagerService;

@Service
public class DefaultKeystoreManagerService implements KeystoreManagerService {

    public static final String KEYSTORE = ".keystore";
    public static final String TRUSTSTORE = ".truststore";
    public static final String KEYSTORE_TRUSTSTORE_FILENAME = "keystoreFile" + TRUSTSTORE;
    public static final String KEYSTORE_ORG_FILENAME = "keystoreOrgFile" + KEYSTORE;
    public static final String KEYSTORE_SSL_FILENAME = "keystoreSSLFile" + KEYSTORE;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultKeystoreManagerService.class);
    private final ConnectorService connectorService;
    private final FileManagerService fileManagerService;

    @Autowired
    public DefaultKeystoreManagerService(final ConnectorService connectorService,
            final FileManagerService fileManagerService) {
        this.connectorService = connectorService;
        this.fileManagerService = fileManagerService;
    }

    private void checkCertificateExpirationDate(final Certificate certificate,
            final String keystore) throws ConnectorException {
        final X509Certificate castedCertificate = (X509Certificate) certificate;
        try {
            castedCertificate.checkValidity();
        } catch (final CertificateExpiredException e) {
            final String errorMessage = "ERROR: Ha caducado el Certificado dentro del keystore: "
                    + keystore + ".\n Es necesario construir uno nuevo.";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final CertificateNotYetValidException e) {
            final String errorMessage = "ERROR: El Certificado dentro del keystore: " + keystore
                    + " aún no es válido.\n Es necesario construir uno nuevo.";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public void checkCertificateExpirationDate(final Path keystorePath, final Path newFilePath,
            final String keystorePassword) throws ConnectorException {

        final KeyStore ks = loadKeystore(keystorePath, newFilePath, keystorePassword);
        Enumeration<String> aliases;
        try {
            aliases = ks.aliases();
        } catch (final KeyStoreException exception) {
            final String errorMessage = "ERROR: Ocurri� un error interno al leer el keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage, exception);
            throw new ConnectorException(errorMessage, exception);
        }

        if (!aliases.hasMoreElements()) {
            final String errorMessage = "ERROR: No se encontraron certificados en el keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        }

        Certificate certificate;
        try {
            certificate = ks.getCertificate(aliases.nextElement());
        } catch (final KeyStoreException exception) {
            final String errorMessage = "ERROR: Ocurri� un error interno al leer el keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage, exception);
            throw new ConnectorException(errorMessage, exception);
        }

        checkCertificateExpirationDate(certificate, keystorePath.getFileName().toString());
    }

    @Override
    public void checkCertificateExpirationDate(final Path keystorePath, final String aliasKeystore,
            final Path newFilePath, final String keystorePassword) throws ConnectorException {
        final KeyStore ks = loadKeystore(keystorePath, newFilePath, keystorePassword);

        final Certificate certificate;
        try {
            certificate = ks.getCertificate(aliasKeystore);
        } catch (final KeyStoreException e) {
            final String errorMessage = "ERROR: No se pudo obtener el certificado de ese keystore: "
                    + keystorePath.getFileName() + ".\n Es posible que el alias \"" + aliasKeystore
                    + "\" no sea el adecuado.";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

        if (certificate == null) {
            final String errorMessage = "ERROR: No existe un certificado dentro del keystore: "
                    + keystorePath.getFileName() + ".\n Es posible que el alias: \"" + aliasKeystore
                    + "\" no sea el adecuado.";
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        } else {
            checkCertificateExpirationDate(certificate, keystorePath.getFileName().toString());
        }
    }

    private KeyStore loadKeystore(final Path keystorePath, final Path newFilePath,
            final String keystorePassword) throws ConnectorException {
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (final KeyStoreException e) {
            final String errorMessage = "ERROR: Al obtener el keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
        try (FileInputStream fis = new FileInputStream(newFilePath.toString())) {
            ks.load(fis, keystorePassword.toCharArray());
            return ks;
        } catch (final FileNotFoundException e) {
            final String errorMessage = "ERROR: No se encontró el keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final IOException e) {
            final String errorMessage = "ERROR: No se pudo parsear el keystore: "
                    + keystorePath.getFileName()
                    + ".\n Es posible que la clave introducida sea errónea";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final CertificateException e) {
            final String errorMessage = "ERROR: La contraseña es inválida para el certificado dentro del keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final NoSuchAlgorithmException e) {
            final String errorMessage = "ERROR: El algoritmo no es el adecuado para el keystore: "
                    + keystorePath.getFileName();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    @Override
    public Path getConnectorKeystore(final long id, final String keystoreName)
            throws ConnectorException {
        final Connector connector = connectorService.getConnector(id);
        final boolean enableLocalConfiguration = connector.isEnableLocalConfiguration();
        final ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();
        if (enableLocalConfiguration && localConfiguration != null) {
            return getKeystorePath(keystoreName, localConfiguration);
        }
        final String errorMessage = "ERROR: No se encontró el Keystore deseado para el conector: "
                + id;
        LOGGER.error(errorMessage);
        throw new ConnectorException(errorMessage);
    }

    @Override
    public Path getGlobalConfigurationKeystore(final String type, final String keystoreName)
            throws ConnectorException {
        final ConnectorGlobalConfiguration globalConfiguration = connectorService
                .getGlobalConfigurationByType(type);
        if (globalConfiguration != null) {
            return getKeystorePath(keystoreName, globalConfiguration);
        }
        final String errorMessage = "ERROR: No se encontró el Keystore " + keystoreName
                + " para el ambiente " + type;
        LOGGER.error(errorMessage);
        throw new ConnectorException(errorMessage);
    }

    private Path getKeystorePath(final String keystoreName, final Configuration configuration)
            throws ConnectorException {
        switch (keystoreName) {
        case KEYSTORE_ORG_FILENAME:
            return Paths.get(configuration.getDirKeystoreOrg());
        case KEYSTORE_SSL_FILENAME:
            return Paths.get(configuration.getDirKeystoreSsl());
        case KEYSTORE_TRUSTSTORE_FILENAME:
            return Paths.get(configuration.getDirKeystore());
        default:
            break;
        }
        throw new ConnectorException("No se encontró un Keystore con el nombre " + keystoreName);
    }

    @Override
    public void setGlobalConfigurationKeystoresFilePaths(
            final ConnectorGlobalConfiguration globalConfiguration) throws ConnectorException {
        final String globalConfigurationDirectoryPath;
        try {
            globalConfigurationDirectoryPath = fileManagerService
                    .getGlobalConfigurationDirectory(globalConfiguration.getType());
        } catch (final IOException e) {
            final String errorMessage = "ERROR: No se pudo obtener la carpeta de Configuración Global para el ambiente "
                    + globalConfiguration.getType();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
        globalConfiguration
                .setDirKeystore(globalConfigurationDirectoryPath + KEYSTORE_TRUSTSTORE_FILENAME);
        globalConfiguration
                .setDirKeystoreOrg(globalConfigurationDirectoryPath + KEYSTORE_ORG_FILENAME);
        globalConfiguration
                .setDirKeystoreSsl(globalConfigurationDirectoryPath + KEYSTORE_SSL_FILENAME);
    }

    @Override
    public void setKeystoresFilePaths(final Connector connector) {
        if (connector.isEnableLocalConfiguration()) {
            final ConnectorLocalConfiguration localConfiguration = connector
                    .getLocalConfiguration();
            final String connectorDirectoryPath = fileManagerService
                    .getConnectorDirectory(String.valueOf(connector.getId()));
            localConfiguration
                    .setDirKeystore(connectorDirectoryPath + KEYSTORE_TRUSTSTORE_FILENAME);
            localConfiguration.setDirKeystoreOrg(connectorDirectoryPath + KEYSTORE_ORG_FILENAME);
            localConfiguration.setDirKeystoreSsl(connectorDirectoryPath + KEYSTORE_SSL_FILENAME);
            connector.setLocalConfiguration(localConfiguration);
        }
    }

    @Override
    public void uploadKeystoresConnector(final Connector connector,
            final MultipartFile keystoreOrgFile, final MultipartFile keystoreSSLFile,
            final MultipartFile keystoreTrustoreFile) throws ConnectorException {
        final ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();

        uploadKeystore(keystoreTrustoreFile, Paths.get(localConfiguration.getDirKeystore()));
        uploadKeystoreAndCheckExpirationDate(keystoreOrgFile, localConfiguration.getAliasKeystore(),
                Paths.get(localConfiguration.getDirKeystoreOrg()),
                localConfiguration.getPasswordKeystoreOrg());
        uploadKeystoreAndCheckExpirationDateSSL(keystoreSSLFile,
                Paths.get(localConfiguration.getDirKeystoreSsl()),
                localConfiguration.getPasswordKeystoreSsl());
    }

    @Override
    public void uploadKeystoresGlobalConfiguration(
            final ConnectorGlobalConfiguration globalConfiguration,
            final MultipartFile keystoreOrgFile, final MultipartFile keystoreSSLFile,
            final MultipartFile keystoreTrustoreFile) throws ConnectorException {

        uploadKeystore(keystoreTrustoreFile, Paths.get(globalConfiguration.getDirKeystore()));
        uploadKeystoreAndCheckExpirationDate(keystoreOrgFile,
                globalConfiguration.getAliasKeystore(),
                Paths.get(globalConfiguration.getDirKeystoreOrg()),
                globalConfiguration.getPasswordKeystoreOrg());
        uploadKeystoreAndCheckExpirationDateSSL(keystoreSSLFile,
                Paths.get(globalConfiguration.getDirKeystoreSsl()),
                globalConfiguration.getPasswordKeystoreSsl());
    }

    private void uploadKeystoreAndCheckExpirationDate(final MultipartFile keystoreFile,
            final String aliasKeystore, final Path newFilePath, final String keystorePassword)
            throws ConnectorException {
        final Path keystorePath = uploadKeystore(keystoreFile, newFilePath);
        checkCertificateExpirationDate(keystorePath, aliasKeystore, newFilePath, keystorePassword);
    }

    private void uploadKeystoreAndCheckExpirationDateSSL(final MultipartFile keystoreFile,
            final Path newFilePath, final String keystorePassword) throws ConnectorException {
        final Path keystorePath = uploadKeystore(keystoreFile, newFilePath);
        checkCertificateExpirationDate(keystorePath, newFilePath, keystorePassword);
    }

    private Path uploadKeystore(final MultipartFile keystoreFile, final Path newFilePath)
            throws ConnectorException {
        if (!keystoreFile.isEmpty() && !"".equals(keystoreFile.getOriginalFilename())) {
            final String fileExtension = fileManagerService
                    .getFileExtension(keystoreFile.getOriginalFilename());

            if (!(KEYSTORE.equals(fileExtension) || TRUSTSTORE.equals(fileExtension))) {
                final String errorMessage = "ERROR: El archivo "
                        + keystoreFile.getOriginalFilename() + " tiene extensión " + fileExtension
                        + ". Debe tener extensión: " + KEYSTORE + " o " + TRUSTSTORE;
                LOGGER.error(errorMessage);
                throw new ConnectorException(errorMessage);
            } else {
                return fileManagerService.uploadFileToPath(keystoreFile, newFilePath);
            }
        } else {
            if (fileManagerService.existsFile(newFilePath)) {
                return newFilePath;
            } else {
                final String errorMessage = "ERROR: Debe subir un archivo de Keystore";
                LOGGER.error(errorMessage);
                throw new ConnectorException(errorMessage);
            }
        }
    }

    private String getKeystorePass(final String keystoreName, final Configuration configuration)
            throws ConnectorException {
        switch (keystoreName) {
        case KEYSTORE_ORG_FILENAME:
            return configuration.getPasswordKeystoreOrg();
        case KEYSTORE_SSL_FILENAME:
            return configuration.getPasswordKeystoreSsl();
        case KEYSTORE_TRUSTSTORE_FILENAME:
            return configuration.getPasswordKeystore();
        default:
            break;
        }
        throw new ConnectorException("No se encontró un Keystore con el nombre " + keystoreName);
    }

    @Override
    public KeystoreModalData getConnectorKeystoreData(final Connector connector,
            final String keystoreName, final String nombreModal) throws ConnectorException {

        // Determino que configuracion esta utilizando el conector y cargo el
        // filepath asociado
        final Configuration config;
        final Path filePath;

        if (connector.getLocalConfiguration() != null) {
            config = connector.getLocalConfiguration();
            filePath = getConnectorKeystore(connector.getId(), keystoreName);
        } else {
            config = connectorService.getGlobalConfigurationByType(connector.getType());
            filePath = getGlobalConfigurationKeystore(connector.getType(), keystoreName);
        }

        // Cargo la keystore
        KeyStore ks = null;
        try {
            ks = loadKeystore(filePath, filePath, getKeystorePass(keystoreName, config));
        } catch (final ConnectorException exception) {
            /*
             * En caso que no se pueda leer el keystore, no se carga la
             * informaci�n del keystore. Por ejemplo, al importar un conector
             * con configuraci�n local, el archivo xml no tiene la contrase�a,
             * por lo que no se puede levantar el keystore
             */
            if (!(exception.getCause() instanceof IOException)) {
                throw exception;
            }
        }

        // Comienzo a armar el objeto data para mostrar en el modal
        final KeystoreModalData ksmd = new KeystoreModalData();
        try {
            // Recorro por todos los alias del keystore
            Enumeration<String> aliases;
            if (ks == null) {
                // Puede ser null en caso que no se carguen los keystores
                aliases = Collections.emptyEnumeration();
            } else {
                aliases = ks.aliases();
            }
            while (aliases.hasMoreElements()) {

                // Comienzo a armar el objeto data del certificado para mostrar
                // en el modal.
                final Certificado cert = new Certificado();

                final String alias = aliases.nextElement();
                final X509Certificate castedCertificate = (X509Certificate) ks
                        .getCertificate(alias);

                cert.setAlias(alias);
                cert.setFechaCreacion(new SimpleDateFormat("dd-MM-yyyy")
                        .format(castedCertificate.getNotBefore()));
                cert.setFechaVencimiento(
                        new SimpleDateFormat("dd-MM-yyyy").format(castedCertificate.getNotAfter()));
                cert.setProveedor(castedCertificate.getIssuerDN().getName());
                cert.setTipo(castedCertificate.getType());

                ksmd.getCertificados().add(cert);
            }
            ksmd.setNombre(keystoreName.substring(0, keystoreName.indexOf(".")));
            ksmd.setNombreModal(nombreModal);
            return ksmd;
        } catch (final KeyStoreException exception) {
            throw new ConnectorException("Error al intentar leer los aliases del keystore",
                    exception);
        }
    }

}
