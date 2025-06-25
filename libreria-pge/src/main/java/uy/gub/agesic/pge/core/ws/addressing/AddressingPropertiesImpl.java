package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.net.URI;
import java.util.*;

public class AddressingPropertiesImpl implements AddressingProperties {
    private final List<Object> elements = new ArrayList();
    private final ReferenceParameters refParams = new ReferenceParametersImpl();
    private final Map<QName, AddressingType> addrTypes = new HashMap<>();

    private AttributedURI to;
    private AttributedURI action;
    private AttributedURI messageId;
    private Relationship[] relatesTo;
    private URI replyTo;
    private EndpointReference faultTo;
    private EndpointReference from;

    public AttributedURI getTo() {
        return this.to;
    }

    public void setTo(AttributedURI to) {
        this.to = to;
    }

    public AttributedURI getAction() {
        return this.action;
    }

    public void setAction(AttributedURI action) {
        this.action = action;
    }

    public AttributedURI getMessageID() {
        return this.messageId;
    }

    public void setMessageID(AttributedURI iri) {
        this.messageId = iri;
    }

    public Relationship[] getRelatesTo() {
        return this.relatesTo;
    }

    public void setRelatesTo(Relationship[] relatesTo) {
        this.relatesTo = relatesTo;
    }

    public URI getReplyTo() {
        return this.replyTo;
    }

    public void setReplyTo(URI replyTo) {
        this.replyTo = replyTo;
    }

    public EndpointReference getFaultTo() {
        return this.faultTo;
    }

    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
    }

    public EndpointReference getFrom() {
        return this.from;
    }

    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    public ReferenceParameters getReferenceParameters() {
        return this.refParams;
    }

    public int size() {
        return this.addrTypes.size();
    }

    public boolean isEmpty() {
        return this.addrTypes.isEmpty();
    }

    public boolean containsKey(Object arg0) {
        return this.addrTypes.containsKey(arg0);
    }

    public boolean containsValue(Object arg0) {
        return this.addrTypes.containsValue(arg0);
    }

    public AddressingType get(Object arg0) {
        return this.addrTypes.get(arg0);
    }

    public AddressingType put(QName arg0, AddressingType arg1) {
        return this.addrTypes.put(arg0, arg1);
    }

    public AddressingType remove(Object arg0) {
        return this.addrTypes.remove(arg0);
    }

    public void putAll(Map<? extends QName, ? extends AddressingType> arg0) {
        this.addrTypes.putAll(arg0);
    }

    public void clear() {
        this.addrTypes.clear();
    }

    public Set<QName> keySet() {
        return this.addrTypes.keySet();
    }

    public Collection<AddressingType> values() {
        return this.addrTypes.values();
    }

    public Set<Map.Entry<QName, AddressingType>> entrySet() {
        return this.addrTypes.entrySet();
    }

    public List<Object> getElements() {
        return this.elements;
    }

    public void addElement(Object element) {
        this.elements.add(element);
    }

    public boolean removeElement(Object element) {
        return this.elements.remove(element);
    }

    public String getNamespaceURI() {
        return "http://www.w3.org/2005/08/addressing";
    }
}