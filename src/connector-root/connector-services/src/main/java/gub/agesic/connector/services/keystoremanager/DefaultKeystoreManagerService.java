package gub.agesic.connector.services.keystoremanager;

import gub.agesic.connector.dataaccess.entity.*;
import gub.agesic.connector.dataaccess.enums.EnvironmentType;
import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.dbaccess.ConnectorService;
import gub.agesic.connector.services.filemanager.FileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultKeystoreManagerService implements KeystoreManagerService {

    public static final List<String> ALLOWED_CERTIFICATE_EXTENSION = Collections.unmodifiableList(Arrays.asList(".keystore", ".truststore", ".pfx"));
    public static final String KEYSTORE_TRUSTSTORE_FILENAME = "keystoreFile";
    public static final String KEYSTORE_ORG_FILENAME = "keystoreOrgFile";
    public static final String KEYSTORE_SSL_FILENAME = "keystoreSSLFile";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKeystoreManagerService.class);

    private final Environment environment;
    private final ConnectorService connectorService;
    private final FileManagerService fileManagerService;

    @Autowired
    public DefaultKeystoreManagerService(Environment environment, final @Lazy ConnectorService connectorService,
                                         final FileManagerService fileManagerService) {
        this.environment = environment;
        this.connectorService = connectorService;
        this.fileManagerService = fileManagerService;
    }

    @Override
    public Date getCertificateExpirationDate(final Path path,
                                             final String alias,
                                             final String password) throws ConnectorException {
        if (alias == null) {
            final String errorMessage = "Alias no definida para el Keystore: " + path.getFileName();
            LOGGER.error(errorMessage);
            throw new ConnectorException("Alias no definida");
        }

        final KeyStore ks = loadKeystore(path, password);

        final Certificate certificate;
        try {
            certificate = ks.getCertificate(alias);
        } catch (final KeyStoreException e) {
            final String errorMessage = "El KeyStore no pudo ser cargado";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

        if (certificate == null) {
            final String errorMessage = "No existe un certificado dentro del keystore: " + path.getFileName() + "para el alias: \"" + alias;
            LOGGER.error(errorMessage);
            throw new ConnectorException("Sin certificado (" + alias + ")");
        }

        final X509Certificate castedCertificate = (X509Certificate) certificate;
        LOGGER.error("fecha de expiracion " + castedCertificate.getNotAfter());

        return castedCertificate.getNotAfter();
    }

    /*
    * Load file into keystore using provided password
    * */
    public KeyStore loadKeystore(final Path filePath, final String keystorePassword) throws ConnectorException {
        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (final KeyStoreException e) {
            final String errorMessage = "ERROR: Al cargar el manejador del KeyStore";
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

        String fileName = getKeystoreName(String.valueOf(filePath.getFileName()));

        try (FileInputStream fis = new FileInputStream(filePath.toString())) {
            ks.load(fis, keystorePassword.toCharArray());
            return ks;
        } catch (final FileNotFoundException e) {
            final String errorMessage = "ERROR: No se encontró el keystore: " + filePath.getFileName();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final IOException e) {
            // Verificar el mensaje de error para capturar el problema de contraseña incorrecta
            if (e.getMessage().contains("Keystore was tampered with, or password was incorrect")) {
                final String errorMessage = "ERROR: La contraseña del " + fileName + " es incorrecta";
                LOGGER.error(errorMessage, e);
                throw new ConnectorException(errorMessage, e);
            } else {
                final String errorMessage = "ERROR: No se pudo parsear el " + fileName;
                LOGGER.error(errorMessage, e);
                throw new ConnectorException(errorMessage, e);
            }
        } catch (final CertificateException e) {
            final String errorMessage = "ERROR: Certificado inválido en " + fileName;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        } catch (final NoSuchAlgorithmException e) {
            final String errorMessage = "ERROR: El algoritmo no es adecuado para el " + fileName;
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }
    }

    private String getKeystoreName(String fileName) {
        if (fileName.contains(KEYSTORE_ORG_FILENAME)) {
            return "Keystore Organismo";
        } else if (fileName.contains(KEYSTORE_SSL_FILENAME)) {
            return "Keystore SSL";
        } else if (fileName.contains(KEYSTORE_TRUSTSTORE_FILENAME)) {
            return "Truststore";
        } else {
            return "";
        }
    }

    @Override
    public void uploadFileAndLoadKeystore(MultipartFile file, String fileName, String filePassword) throws ConnectorException {
        String fileExtension = fileManagerService.getFileExtension(file.getOriginalFilename());
        Path filePath = fileManagerService.getFilePathInUploadTempFolder(fileName + fileExtension);
        // Copiar fichero a la carpeta temporal (temp/)
        fileManagerService.uploadFileToPath(file, filePath);
        // Cargar fichero al KeyStore. Se chequea el password
        loadKeystore(filePath, filePassword);
    }

    @Override
    public Path getConnectorKeystore(final long id,
                                     final String keystoreName) throws ConnectorException {
        final Connector connector = connectorService.getConnector(id);
        final ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();
        if (localConfiguration != null) {
            return getKeystorePath(keystoreName, localConfiguration);
        }
        final String errorMessage = "ERROR: No se encontró el Keystore deseado para el servicio: " + id;
        LOGGER.error(errorMessage);
        throw new ConnectorException(errorMessage);
    }

    @Override
    public Path getGlobalConfigurationKeystore(final String type,
                                               final String keystoreName) throws ConnectorException {
        final ConnectorGlobalConfiguration globalConfiguration = connectorService.getGlobalConfigurationByType(type);
        if (globalConfiguration != null) {
            return getKeystorePath(keystoreName, globalConfiguration);
        }
        final String errorMessage = "ERROR: No se encontró el Keystore " + keystoreName + " para el ambiente " + type;
        LOGGER.error(errorMessage);
        throw new ConnectorException(errorMessage);
    }

    private Path getKeystorePath(final String keystoreName,
                                 final Configuration configuration)
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
    public void setGlobalConfigurationKeystoresFilePaths(final ConnectorGlobalConfiguration globalConfiguration,
                                                         MultipartFile keystoreOrgFile,
                                                         MultipartFile keystoreSSLFile,
                                                         MultipartFile keystoreTruststoreFile) throws ConnectorException {
        final String globalConfigurationDirectoryPath;
        try {
            globalConfigurationDirectoryPath = fileManagerService.getGlobalConfigurationDirectory(globalConfiguration.getType());
        } catch (final IOException e) {
            final String errorMessage = "ERROR: No se pudo obtener la carpeta de Configuración Global para el ambiente " + globalConfiguration.getType();
            LOGGER.error(errorMessage, e);
            throw new ConnectorException(errorMessage, e);
        }

        ConnectorGlobalConfiguration currentGlobalConfiguration = new ConnectorGlobalConfiguration();
        try {
            currentGlobalConfiguration = connectorService.getGlobalConfigurationByType(globalConfiguration.getType());
        } catch (NoSuchElementException e) {
            LOGGER.info("No existe ninguna configuración global para el ambiente seleccionado. Se crea una por defecto.");
        }

        String filePath;
        String fileExtension;
        if (keystoreTruststoreFile.getOriginalFilename().isEmpty()) {
            // if no file was uploaded, use current file path
            filePath = currentGlobalConfiguration.getDirKeystore();
        } else {
            // update the file name keeping the extension
            fileExtension = fileManagerService.getFileExtension(keystoreTruststoreFile.getOriginalFilename());
            filePath = globalConfigurationDirectoryPath + KEYSTORE_TRUSTSTORE_FILENAME + fileExtension;
        }
        globalConfiguration.setDirKeystore(filePath);

        if (keystoreOrgFile.getOriginalFilename().isEmpty()) {
            // if no file was uploaded, use current file path
            filePath = currentGlobalConfiguration.getDirKeystoreOrg();
        } else {
            // update the file name keeping the extension
            fileExtension = fileManagerService.getFileExtension(keystoreOrgFile.getOriginalFilename());
            filePath = globalConfigurationDirectoryPath + KEYSTORE_ORG_FILENAME + fileExtension;
        }
        globalConfiguration.setDirKeystoreOrg(filePath);

        if (keystoreSSLFile.getOriginalFilename().isEmpty()) {
            // if no file was uploaded, use current file path
            filePath = currentGlobalConfiguration.getDirKeystoreSsl();
        } else {
            // update the file name keeping the extension
            fileExtension = fileManagerService.getFileExtension(keystoreSSLFile.getOriginalFilename());
            filePath = globalConfigurationDirectoryPath + KEYSTORE_SSL_FILENAME + fileExtension;
        }
        globalConfiguration.setDirKeystoreSsl(filePath);
    }

    @Override
    public void setKeystoresFilePaths(final Connector connector,
                                      MultipartFile keystoreOrgFile,
                                      MultipartFile keystoreSSLFile,
                                      MultipartFile keystoreTruststoreFile) throws ConnectorException {
        if (connector.isEnableLocalConfiguration()) {
            final ConnectorLocalConfiguration localConfiguration = connector.getLocalConfiguration();
            final String connectorDirectoryPath = fileManagerService.getConnectorDirectory(String.valueOf(connector.getId()));

            String filePath;
            String fileExtension;
            if (keystoreTruststoreFile == null) {
                // on import
                filePath = connectorDirectoryPath + KEYSTORE_TRUSTSTORE_FILENAME;
            } else if (keystoreTruststoreFile.getOriginalFilename().isEmpty()) {
                // if no file was uploaded, use current file path
                filePath = localConfiguration.getDirKeystore();
            } else {
                // update the file name keeping the extension
                fileExtension = fileManagerService.getFileExtension(keystoreTruststoreFile.getOriginalFilename());
                filePath = connectorDirectoryPath + KEYSTORE_TRUSTSTORE_FILENAME + fileExtension;
            }
            localConfiguration.setDirKeystore(filePath);

            if (keystoreOrgFile == null) {
                // on import
                filePath = connectorDirectoryPath + KEYSTORE_ORG_FILENAME;
            } else if (keystoreOrgFile.getOriginalFilename().isEmpty()) {
                // if no file was uploaded, use current file path
                filePath = localConfiguration.getDirKeystoreOrg();
            } else {
                // update the file name keeping the extension
                fileExtension = fileManagerService.getFileExtension(keystoreOrgFile.getOriginalFilename());
                filePath = connectorDirectoryPath + KEYSTORE_ORG_FILENAME + fileExtension;
            }
            localConfiguration.setDirKeystoreOrg(filePath);

            if (keystoreSSLFile == null) {
                // on import
                filePath = connectorDirectoryPath + KEYSTORE_SSL_FILENAME;
            } else if (keystoreSSLFile.getOriginalFilename().isEmpty()) {
                // if no file was uploaded, use current file path
                filePath = localConfiguration.getDirKeystoreSsl();
            } else {
                // update the file name keeping the extension
                fileExtension = fileManagerService.getFileExtension(keystoreSSLFile.getOriginalFilename());
                filePath = connectorDirectoryPath + KEYSTORE_SSL_FILENAME + fileExtension;
            }
            localConfiguration.setDirKeystoreSsl(filePath);

            connector.setLocalConfiguration(localConfiguration);
        }
    }

    @Override
    public void uploadKeystoresByConfiguration(final Configuration configuration,
                                               final MultipartFile keystoreOrgFile,
                                               final MultipartFile keystoreSSLFile,
                                               final MultipartFile keystoreTruststoreFile) throws ConnectorException {
        Path newFilePath;

        // Truststore SSL
        if (!keystoreTruststoreFile.getOriginalFilename().isEmpty()) {
            newFilePath = Paths.get(configuration.getDirKeystore());
            // Check extension
            uploadKeystore(keystoreTruststoreFile, newFilePath);
            // Check password and load into KeyStore
            loadKeystore(newFilePath, configuration.getPasswordKeystore());
        }

        // Keystore Org
        if (!keystoreOrgFile.getOriginalFilename().isEmpty()) {
            newFilePath = Paths.get(configuration.getDirKeystoreOrg());
            // Check extension
            uploadKeystore(keystoreOrgFile, newFilePath);
            // Check password and load into KeyStore
            loadKeystore(newFilePath, configuration.getPasswordKeystoreOrg());
        }

        // Keystore SSL
        if (!keystoreSSLFile.getOriginalFilename().isEmpty()) {
            newFilePath = Paths.get(configuration.getDirKeystoreSsl());
            // Check extension
            uploadKeystore(keystoreSSLFile, newFilePath);
            // Check password and load into KeyStore
            loadKeystore(newFilePath, configuration.getPasswordKeystoreSsl());
        }
    }

    /*
    * Check extension and copy file to local filesystem
    * */
    private Path uploadKeystore(final MultipartFile file,
                                final Path path) throws ConnectorException {
        if (!file.isEmpty() && !file.getOriginalFilename().isEmpty()) {
            checkFileExtension(file);
            return fileManagerService.uploadFileToPath(file, path);
        } else {
            if (fileManagerService.existsFile(path)) {
                return path;
            } else {
                final String errorMessage = "ERROR: Debe subir un archivo de Keystore";
                LOGGER.error(errorMessage);
                throw new ConnectorException(errorMessage);
            }
        }
    }

    public void checkFileExtension(final MultipartFile file) throws ConnectorException {
        final String fileExtension = fileManagerService.getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_CERTIFICATE_EXTENSION.contains(fileExtension)) {
            List<String> extensions = ALLOWED_CERTIFICATE_EXTENSION.stream().map(e -> "[" + e + "]").collect(Collectors.toList());
            String errorMessage = String.format("ERROR: El archivo %s tiene extensión %s. \n Debe tener una de las siguientes extensiones: %s",
                    file.getOriginalFilename(),
                    fileExtension,
                    String.join(", ", extensions));
            LOGGER.error(errorMessage);
            throw new ConnectorException(errorMessage);
        }
    }

    private String getKeystorePass(final String keystoreName,
                                   final Configuration configuration) throws ConnectorException {
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
                                                      final String keystoreName,
                                                      final String nombreModal) throws ConnectorException {

        // Determino que configuracion esta utilizando el servicio y cargo el
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
            ks = loadKeystore(filePath, getKeystorePass(keystoreName, config));
        } catch (final ConnectorException exception) {

            // ===== CHECK THIS ========
            /*
             * En caso que no se pueda leer el keystore, no se carga la
             * información del keystore. Por ejemplo, al importar un servicio
             * con configuración local, el archivo xml no tiene la contrasena,
             * por lo que no se puede levantar el keystore
             */
            if (!(exception.getCause() instanceof IOException)) {
                throw exception;
            }
            // ==========================
        }

        String stringExpirationDateKeystore = "", stringExpirationDateKeystoreSSL = "", stringExpirationDateTruststore = "";

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

            // Obtener las alias
            String aliasKeystore = connector.getLocalConfiguration().getAliasKeystore();
            String aliasKeystoreSSL = connector.getLocalConfiguration().getAliasKeystoreSSL();
            String aliasTruststore;
            if (connector.getType().equals(EnvironmentType.TESTING.getName())) {
                aliasTruststore = environment.getProperty("connector.truststore.alias.test");
            } else {
                aliasTruststore = environment.getProperty("connector.truststore.alias.prod");
            }

            while (aliases.hasMoreElements()) {
                // Comienzo a armar el objeto data del certificado para mostrar
                // en el modal.
                final Certificado cert = new Certificado();

                final String alias = aliases.nextElement();
                final X509Certificate castedCertificate = (X509Certificate) Objects.requireNonNull(ks)
                        .getCertificate(alias);

                cert.setAlias(alias);
                cert.setFechaCreacion(new SimpleDateFormat("dd-MM-yyyy")
                        .format(castedCertificate.getNotBefore()));
                String stringExpirationDate = new SimpleDateFormat("dd-MM-yyyy").format(castedCertificate.getNotAfter());
                cert.setFechaVencimiento(stringExpirationDate);
                cert.setProveedor(castedCertificate.getIssuerDN().getName());
                cert.setTipo(castedCertificate.getType());

                ksmd.getCertificados().add(cert);

                if (keystoreName.equals(KEYSTORE_ORG_FILENAME) && alias.equals(aliasKeystore)) {
                    stringExpirationDateKeystore = stringExpirationDate;
                } else if (keystoreName.equals(KEYSTORE_SSL_FILENAME) && alias.equals(aliasKeystoreSSL)) {
                    stringExpirationDateKeystoreSSL = stringExpirationDate;
                } else if (keystoreName.equals(KEYSTORE_TRUSTSTORE_FILENAME) && alias.equals(aliasTruststore)) {
                    stringExpirationDateTruststore = stringExpirationDate;
                }
            }

            ksmd.setNombre(keystoreName);
            ksmd.setNombreModal(nombreModal);

            // Verificar si se encontro fecha de expiracion asociada al alias
            if (keystoreName.equals(KEYSTORE_ORG_FILENAME)) {
                if (stringExpirationDateKeystore.isEmpty()) {
                    connector.setExpDateKeystoreOrg("Sin certificado (" + aliasKeystore + ")");
                } else {
                    connector.setExpDateKeystoreOrg(stringExpirationDateKeystore);
                }
            }

            if (keystoreName.equals(KEYSTORE_SSL_FILENAME)) {
                if (stringExpirationDateKeystoreSSL.isEmpty()) {
                    connector.setExpDateKeystoreSSL("Sin certificado (" + aliasKeystoreSSL + ")");
                } else {
                    connector.setExpDateKeystoreSSL(stringExpirationDateKeystoreSSL);
                }
            }

            if (keystoreName.equals(KEYSTORE_TRUSTSTORE_FILENAME)) {
                if (aliasTruststore == null) {
                    connector.setExpDateKeystoreTruststore("Alias no definida");
                } else if (stringExpirationDateTruststore.isEmpty()) {
                    connector.setExpDateKeystoreTruststore("Sin certificado (" + aliasTruststore + ")");
                } else {
                    connector.setExpDateKeystoreTruststore(stringExpirationDateTruststore + " (" + aliasTruststore + ")");
                }
            }

            return ksmd;
        } catch (final KeyStoreException exception) {
            throw new ConnectorException("Error al intentar leer los aliases del keystore",
                    exception);
        }
    }

}

