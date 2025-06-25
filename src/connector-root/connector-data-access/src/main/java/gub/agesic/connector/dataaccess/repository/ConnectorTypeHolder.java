package gub.agesic.connector.dataaccess.repository;

import gub.agesic.connector.dataaccess.enums.EnvironmentType;

/**
 * Created by adriancur on 23/11/17.
 */
public class ConnectorTypeHolder {

    private final boolean sslEnabled;
    private EnvironmentType connectorType;

    public ConnectorTypeHolder(final EnvironmentType connectorType, final boolean sslEnabled) {
        this.connectorType = connectorType;
        this.sslEnabled = sslEnabled;
    }

    public EnvironmentType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(final EnvironmentType connectorType) {
        this.connectorType = connectorType;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }
}