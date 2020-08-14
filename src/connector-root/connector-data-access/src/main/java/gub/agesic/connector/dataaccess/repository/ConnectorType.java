package gub.agesic.connector.dataaccess.repository;

/**
 * Created by adriancur on 23/11/17.
 */
public enum ConnectorType {

    PRODUCCION("Produccion"), TEST("Testing");

    private final String environment;

    ConnectorType(final String environmentP) {
        environment = environmentP;
    }

    public String getEnvironment() {
        return environment;
    }
}