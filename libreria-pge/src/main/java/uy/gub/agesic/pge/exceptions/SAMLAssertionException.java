package uy.gub.agesic.pge.exceptions;

public class SAMLAssertionException extends Exception {
    private static final long serialVersionUID = 4678505971718224800L;

    public SAMLAssertionException(Exception e) {
        super(e.getMessage(), e);
    }

    public SAMLAssertionException(String message, Exception e) {
        super(message, e);
    }
}