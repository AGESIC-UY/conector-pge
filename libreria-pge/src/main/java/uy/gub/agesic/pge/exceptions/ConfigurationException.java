package uy.gub.agesic.pge.exceptions;

public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 7230871535650935860L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Exception e) {
        super(e);
    }

    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }
}