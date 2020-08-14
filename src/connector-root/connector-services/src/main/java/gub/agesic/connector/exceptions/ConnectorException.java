package gub.agesic.connector.exceptions;

/**
 * Created by abrusco on 16/11/17.
 */
public class ConnectorException extends Exception {

    public ConnectorException(final String message) {
        super(message);
    }

    public ConnectorException(final String message, final Exception e) {
        super(message, e);
    }
}
