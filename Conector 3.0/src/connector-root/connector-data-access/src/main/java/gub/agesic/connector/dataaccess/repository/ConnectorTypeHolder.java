package gub.agesic.connector.dataaccess.repository;

/**
 * Created by adriancur on 23/11/17.
 */
public class ConnectorTypeHolder {

    private ConnectorType connectorType;
    private final boolean sslEnabled;

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(final ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public ConnectorTypeHolder(final ConnectorType connectorType, final boolean sslEnabled) {
        this.connectorType = connectorType;
        this.sslEnabled = sslEnabled;
    }
}