package uy.gub.agesic.pge.exceptions;

public class PGEContextException extends Exception {
    private static final long serialVersionUID = 7230871535650935860L;

    public PGEContextException(String message) {
        super(message);
    }

    public PGEContextException(Exception e) {
        super(e);
    }

    public PGEContextException(String message, Exception e) {
        super(message, e);
    }
}