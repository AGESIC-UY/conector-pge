package uy.gub.agesic.pge.ps;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class PublishSubscribeHeadersHandler implements SOAPHandler<SOAPMessageContext> {
    private final String producerId;
    private final String topicId;
    private String notificationId;

    public PublishSubscribeHeadersHandler(String producerId, String topicId) {
        this.producerId = producerId;
        this.topicId = topicId;
    }

    public Set<QName> getHeaders() {
        return new TreeSet<>();
    }

    public void close(MessageContext mc) {
    }

    public boolean handleFault(SOAPMessageContext mc) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext mc) {
        Boolean outbound = (Boolean) mc.get("javax.xml.ws.handler.message.outbound");
        SOAPMessage message = mc.getMessage();
        if (outbound) {
            try {
                SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
                SOAPFactory factory = SOAPFactory.newInstance();

                String prefix = "ps";
                String uri = "http://ps.agesic.gub.uy";
                SOAPElement producerElem = factory.createElement("producer", prefix, uri);
                SOAPElement topicElem = factory.createElement("topic", prefix, uri);
                producerElem.addTextNode(this.producerId);
                topicElem.addTextNode(this.topicId);

                SOAPHeader header = envelope.getHeader();
                header.addChildElement(producerElem);
                header.addChildElement(topicElem);
            } catch (Exception e) {
                System.out.println("Exception in handler: " + e);
                e.printStackTrace();
            }
        } else {
            try {
                Iterator<SOAPHeaderElement> it = message.getSOAPHeader().getChildElements(new QName("http://ps.agesic.gub.uy", "notificationId"));
                if (it.hasNext()) {
                    SOAPHeaderElement elem = it.next();
                    this.notificationId = elem.getValue();
                }
            } catch (Exception e) {
                System.out.println("Exception in handler: " + e);
                e.printStackTrace();
            }
        }
        return true;
    }

    public String getNotificationId() {
        return this.notificationId;
    }
}