package uy.gub.agesic.pge.handler;

import uy.gub.agesic.pge.core.ws.addressing.*;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WSAddressingHandler implements SOAPHandler<SOAPMessageContext> {
    private static final AddressingBuilder ADDR_BUILDER;
    private static final AddressingConstantsImpl ADDR_CONSTANTS;
    private static final Set<QName> HEADERS = new HashSet<QName>();

    static {
        ADDR_CONSTANTS = new AddressingConstantsImpl();
        ADDR_BUILDER = AddressingBuilder.getAddressingBuilder();

        HEADERS.add(ADDR_CONSTANTS.getActionQName());
        HEADERS.add(ADDR_CONSTANTS.getToQName());
    }

    public boolean handleMessage(SOAPMessageContext msgContext) {
        Boolean outbound = (Boolean) msgContext.get("javax.xml.ws.handler.message.outbound");
        if (outbound == null) {
            throw new IllegalStateException("Cannot obtain required property: javax.xml.ws.handler.message.outbound");
        }

        if (outbound) {
            SOAPAddressingPropertiesImpl sOAPAddressingPropertiesImpl;
            SOAPAddressingProperties addrProps = (SOAPAddressingProperties) msgContext.get("uy.gub.pge.ws.addressing.context");

            if (addrProps == null) {
                sOAPAddressingPropertiesImpl = (SOAPAddressingPropertiesImpl) ADDR_BUILDER.newAddressingProperties();

                if (sOAPAddressingPropertiesImpl.getAction() == null) {
                    sOAPAddressingPropertiesImpl.setAction(getAction(msgContext));
                }

                if (sOAPAddressingPropertiesImpl.getMessageID() == null) {
                    sOAPAddressingPropertiesImpl.setMessageID(newURI("urn:uuid:" + UUID.randomUUID()));
                }

                if (sOAPAddressingPropertiesImpl.getReplyTo() == null) {
                    sOAPAddressingPropertiesImpl.setReplyTo(newAnonymousURI());
                }

                SOAPMessage soapMessage = msgContext.getMessage();
                sOAPAddressingPropertiesImpl.writeHeaders(soapMessage);
            }
        }

        return true;
    }

    public void close(MessageContext messageContext) {
    }

    public boolean handleFault(SOAPMessageContext messageContext) {
        return true;
    }

    public Set<QName> getHeaders() {
        return Collections.unmodifiableSet(HEADERS);
    }

    private AttributedURI getAction(MessageContext msgContext) {
        String action = "UNSPECIFIED";
        if (msgContext.get("javax.xml.ws.soap.http.soapaction.uri") != null) {
            action = msgContext.get("javax.xml.ws.soap.http.soapaction.uri").toString();
        }
        return newURI(action);
    }

    private URI newAnonymousURI() {
        return newURI(ADDR_CONSTANTS.getAnonymousURI()).getURI();
    }

    private AttributedURI newURI(String uri) {
        try {
            return ADDR_BUILDER.newURI(uri);
        } catch (URISyntaxException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }
}