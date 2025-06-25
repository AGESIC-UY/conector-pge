package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

public class AddressingException extends WebServiceException {
    private static final long serialVersionUID = -5398512371386503063L;
    protected static String fMessage = null;
    protected QName code;
    protected String reason;
    protected Object detail;

    public AddressingException() {
    }

    public AddressingException(String message) {
        super(message);
    }

    public AddressingException(Throwable cause) {
        super(cause);
    }

    public AddressingException(String message, Throwable cause) {
        super(message, cause);
    }

    public QName getCode() {
        return this.code;
    }

    public QName getSubcode() {
        return null;
    }

    public String getReason() {
        return this.reason;
    }

    public Object getDetail() {
        return this.detail;
    }
}