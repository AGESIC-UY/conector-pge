package gub.agesic.connector.integration.pgeclient.exceptions;

public class RequestSecurityTokenException extends Exception {

    private static final long serialVersionUID = 7230871535650935860L;

    private Integer codError;

    public RequestSecurityTokenException(final String message) {
        super(message);
    }

    public RequestSecurityTokenException(final String message, final Integer codError) {
        super(message);
        setCodError(codError);
    }

    public RequestSecurityTokenException(final Exception e) {
        super(e);
    }

    public RequestSecurityTokenException(final Exception e, final Integer codError) {
        super(e);
        setCodError(codError);
    }

    public Integer getCodError() {
        return codError;
    }

    public void setCodError(final Integer codError) {
        this.codError = codError;
    }
}
