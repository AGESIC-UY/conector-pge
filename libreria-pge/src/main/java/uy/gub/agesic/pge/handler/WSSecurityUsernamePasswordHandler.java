package uy.gub.agesic.pge.handler;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

public class WSSecurityUsernamePasswordHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger log = Logger.getLogger(WSSecurityUsernamePasswordHandler.class);

    public boolean handleMessage(SOAPMessageContext msgContext) {
        Boolean outbound = (Boolean) msgContext.get("javax.xml.ws.handler.message.outbound");
        if (outbound == null) {
            throw new IllegalStateException("Cannot obtain required property: javax.xml.ws.handler.message.outbound");
        }

        if (outbound) {
            SOAPMessage soapMessage = msgContext.getMessage();
            String samlActor = (String) msgContext.get("actorDP");
            dispatchMessage(samlActor, soapMessage);
        }

        return true;
    }

    private void dispatchMessage(String samlActor, SOAPMessage soapMessage) {
        try {
            SOAPEnvelope soapEnv = soapMessage.getSOAPPart().getEnvelope();
            SOAPHeader soapHeader = soapEnv.getHeader();
            SOAPHeaderElement samlHeader = getSAMLSoapHeader(soapHeader);
            samlHeader.setActor(samlActor);
        } catch (SOAPException e) {
            log.error("Internal error occured handling outbound message:", e);
        }
    }

    private SOAPHeaderElement getSAMLSoapHeader(SOAPHeader soapHeader) {
        SOAPHeaderElement headerElement = null;
        Iterator<SOAPHeaderElement> allHeaders = soapHeader.examineAllHeaderElements();
        boolean found = false;

        while (allHeaders.hasNext() && !found) {
            headerElement = allHeaders.next();
            Name headerName = headerElement.getElementName();
            if ("wsse:Security".equalsIgnoreCase(headerName.getQualifiedName()) && "Assertion".equalsIgnoreCase(headerElement.getFirstChild().getLocalName())) {
                found = true;
            }
        }

        return headerElement;
    }

    public void close(MessageContext arg0) {
    }

    public boolean handleFault(SOAPMessageContext arg0) {
        return true;
    }

    public Set<QName> getHeaders() {
        return null;
    }
}