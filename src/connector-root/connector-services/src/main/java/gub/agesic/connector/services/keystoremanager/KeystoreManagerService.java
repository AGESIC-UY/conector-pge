package gub.agesic.connector.services.keystoremanager;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.KeystoreModalData;
import gub.agesic.connector.exceptions.ConnectorException;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Date;

public interface KeystoreManagerService {

    Date getCertificateExpirationDate(final Path keystorePath, final String aliasKeystore,
                                      final String keystorePassword) throws ConnectorException;

    Path getConnectorKeystore(long id, String keystoreName) throws ConnectorException;

    Path getGlobalConfigurationKeystore(String type, String keystoreName) throws ConnectorException;

    void setGlobalConfigurationKeystoresFilePaths(ConnectorGlobalConfiguration globalConfiguration, MultipartFile keystoreOrgFile,
                                                  MultipartFile keystoreSSLFile, MultipartFile keystoreTruststoreFile)
            throws ConnectorException;

    void setKeystoresFilePaths(Connector connector, MultipartFile keystoreOrgFile,
                               MultipartFile keystoreSSLFile, MultipartFile keystoreTrustoreFile) throws ConnectorException;

    void uploadKeystoresByConfiguration(Configuration configuration, MultipartFile keystoreOrgFile,
                                        MultipartFile keystoreSSLFile, MultipartFile keystoreTruststoreFile) throws ConnectorException;

    KeyStore loadKeystore(final Path filePath, final String keystorePassword) throws ConnectorException;

    void uploadFileAndLoadKeystore(MultipartFile file, String fileName, String filePassword) throws ConnectorException;

    void checkFileExtension(final MultipartFile file) throws ConnectorException;

    KeystoreModalData getConnectorKeystoreData(Connector connector, String keystoreName,
                                               final String nombreModal) throws ConnectorException;
}
