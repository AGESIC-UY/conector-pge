package gub.agesic.connector.integration.pgeclient.exceptions;

public class AssertionException extends Exception {

    private static final long serialVersionUID = 4678505971718224800L;

    public AssertionException(final String message, final Exception e) {
        super(message, e);
    }
}
