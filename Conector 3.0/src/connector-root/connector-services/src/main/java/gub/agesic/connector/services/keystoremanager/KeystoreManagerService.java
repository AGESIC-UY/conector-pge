package gub.agesic.connector.services.keystoremanager;

import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.KeystoreModalData;
import gub.agesic.connector.exceptions.ConnectorException;

public interface KeystoreManagerService {

    void checkCertificateExpirationDate(final Path keystorePath, final String alias,
            final Path newFilePath, final String keystorePassword) throws ConnectorException;

    void checkCertificateExpirationDate(final Path keystorePath, final Path newFilePath,
            final String keystorePassword) throws ConnectorException;

    Path getConnectorKeystore(long id, String keystoreName) throws ConnectorException;

    Path getGlobalConfigurationKeystore(String type, String keystoreName) throws ConnectorException;

    void setGlobalConfigurationKeystoresFilePaths(ConnectorGlobalConfiguration globalConfiguration)
            throws ConnectorException;

    void setKeystoresFilePaths(Connector connector);

    void uploadKeystoresConnector(Connector connector, MultipartFile keystoreOrgFile,
            MultipartFile keystoreSSLFile, MultipartFile keystoreTrustoreFile)
            throws ConnectorException;

    void uploadKeystoresGlobalConfiguration(ConnectorGlobalConfiguration globalConfiguration,
            MultipartFile keystoreOrgFile, MultipartFile keystoreSSLFile,
            MultipartFile keystoreTrustoreFile) throws ConnectorException;

    KeystoreModalData getConnectorKeystoreData(Connector connector, String keystoreName,
            final String nombreModal) throws ConnectorException;
}
