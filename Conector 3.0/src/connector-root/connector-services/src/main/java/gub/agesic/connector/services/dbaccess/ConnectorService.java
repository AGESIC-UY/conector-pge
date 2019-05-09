package gub.agesic.connector.services.dbaccess;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import gub.agesic.connector.dataaccess.entity.Connector;
import gub.agesic.connector.dataaccess.entity.ConnectorGlobalConfiguration;
import gub.agesic.connector.dataaccess.entity.RoleOperation;
import gub.agesic.connector.dataaccess.repository.ConnectorTypeHolder;
import gub.agesic.connector.exceptions.ConnectorException;

/**
 * Created by adriancur on 04/10/17.
 */

public interface ConnectorService {

    /* Querys */
    boolean existsConnectorByName(String name, String type);

    ConnectorGlobalConfiguration getGlobalConfigurationByType(String type)
            throws NoSuchElementException;

    Connector getConnector(Long id);

    Optional<Connector> getConnectorByPathAndPort(String path,
            ConnectorTypeHolder connectorTypeHolder) throws ConnectorException;

    List<Connector> getConnectorList();

    List<Connector> getFilteredConnectorList(String type, String tag);

    void checkConnectorPathAndTypeAvailabilityForType(String name, String path, String type)
            throws ConnectorException;

    /* Save and update */
    void saveConnector(Connector connector) throws ConnectorException;

    void saveGlobalConfig(ConnectorGlobalConfiguration globalConfig) throws ConnectorException;

    void deleteConnector(Long id);

    Optional<RoleOperation> getRoleoperationsOperationFromWSDL(Connector connector,
            String operation);

    ConnectorTypeHolder getConnectorTypeByPort(final String port) throws ConnectorException;

    String getLocationBasedOnConnector(Connector connector) throws ConnectorException;
}
