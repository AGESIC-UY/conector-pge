package uy.gub.agesic.pge.handler;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.util.Set;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger log = Logger.getLogger(LoggingHandler.class);

    public void close(MessageContext arg0) {
    }

    public boolean handleFault(SOAPMessageContext ctx) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext ctx) {
        SOAPMessage msg = ctx.getMessage();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            msg.writeTo(out);
            String strMsg = out.toString();
            log.info("Start SoapMessage---------------------------------------------------------");
            log.info(strMsg);
            log.info("End SoapMessage-----------------------------------------------------------");
        } catch (Exception e) {
            log.error("Error printing SOAP message: " + e.getMessage());
        }
        return true;
    }

    public Set<QName> getHeaders() {
        return null;
    }
}