package gub.agesic.connector.services.dbaccess;

import gub.agesic.connector.dataaccess.entity.Configuration;
import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.enums.ExpirationStatus;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.exceptions.ConnectorException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by adriancur on 04/10/17.
 */

public interface ConnectorService {

    /* Querys */
    boolean existsConnectorByName(String name, String type);

    boolean existsConnectorByName(String name, String type, long id);

    ConnectorGlobalConfiguration getGlobalConfigurationByType(String type)
            throws NoSuchElementException;

    Connector getConnector(Long id);

    Optional<Connector> getConnectorByPathAndPort(String path,
                                                  ConnectorTypeHolder connectorTypeHolder) throws ConnectorException;

    List<Connector> getConnectorList();

    List<Connector> getFilteredConnectorList(String type, String tag);

    void checkConnectorPathAndTypeAvailabilityForType(String name, String path, String type, long id)
        throws ConnectorException;

    void checkConnectorPathAndTypeAvailabilityForType(String name, String path, String type)
        throws ConnectorException;

    /* Save and update */
    void saveConnector(Connector connector) throws ConnectorException;

    void saveGlobalConfig(ConnectorGlobalConfiguration globalConfig) throws ConnectorException;

    void updateConnectorPath(Connector connector);

    void deleteConnector(Long id);

    List<RoleOperation> getRoleOperationsFromWSDL(Connector connector,
                                                  String operation, String soapVersion);

    String getHost();

    String getPortByTypeAndProtocol(String type, boolean isSslEnabled);

    ConnectorTypeHolder getConnectorTypeByPort(final String port) throws ConnectorException;

    String getPortByConnector(final Connector connector);

    String getLocationBasedOnConnector(Connector connector);

    int getMaxUploadSize() throws ConnectorException;

    ExpirationStatus getGlobalCertificateStatus();

    ExpirationStatus calculateExpirationStatus(Connector connector);

    ExpirationStatus calculateExpirationStatusKeyStoreOrg(Configuration configuration, int expirationNoticeDays);

    ExpirationStatus calculateExpirationStatusKeyStoreSSL(Configuration configuration, int expirationNoticeDays);

    ExpirationStatus calculateExpirationStatusTruststore(Configuration configuration, int expirationNoticeDays, String envType);

    void setAllCertsExpirationStatus(Connector connector);
}
