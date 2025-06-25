package uy.gub.agesic.pge.core.ws.addressing;

import org.w3c.dom.Element;
import uy.gub.agesic.pge.core.xml.util.DOMUtils;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.net.URI;
import java.util.List;

public class SOAPAddressingPropertiesImpl extends AddressingPropertiesImpl implements SOAPAddressingProperties {
    private static final AddressingConstants ADDR = new AddressingConstantsImpl();
    private boolean mustunderstand;

    public void readHeaders(SOAPMessage message) throws AddressingException {
    }

    public void writeHeaders(SOAPMessage message) throws AddressingException {
        try {
            SOAPHeader soapHeader = message.getSOAPHeader();
            if (soapHeader == null) {
                soapHeader = message.getSOAPPart().getEnvelope().addHeader();
            }
            soapHeader.addNamespaceDeclaration(ADDR.getNamespacePrefix(), ADDR.getNamespaceURI());

            if (getTo() != null) {
                SOAPElement element = soapHeader.addChildElement(ADDR.getToQName());
                element.addTextNode(getTo().getURI().toString());
            }
            appendRequiredHeader(soapHeader, ADDR.getActionQName(), getAction());

            if ((getReplyTo() != null || getFaultTo() != null) && null == getMessageID())
                throw new AddressingException("Required addressing header missing:" + ADDR.getMessageIDQName());
            if (getMessageID() != null) {
                SOAPElement wsaMessageId = soapHeader.addChildElement(ADDR.getMessageIDQName());
                wsaMessageId.addTextNode(getMessageID().getURI().toString());
            }

            if (getRelatesTo() != null) {
                for (Relationship rel : getRelatesTo()) {
                    SOAPElement wsaRelatesTo = soapHeader.addChildElement(ADDR.getRelatesToQName());
                    if (rel.getType() != null) {
                        wsaRelatesTo.setAttribute(ADDR.getRelationshipTypeName(), getQualifiedName(rel.getType()));
                    }
                    wsaRelatesTo.addTextNode(rel.getID().toString());
                }
            }

            if (getReplyTo() != null) {
                URI epr = getReplyTo();
                SOAPElement wsaReplyTo = soapHeader.addChildElement(ADDR.getReplyToQName());
                wsaReplyTo.addTextNode(epr.toString());
            }

            ReferenceParameters refParams = getReferenceParameters();
            if (refParams.getElements().size() > 0) {
                for (Object obj : refParams.getElements()) {
                    SOAPElement refElement = appendElement(soapHeader, obj);
                    QName refQName = new QName(ADDR.getNamespaceURI(), "IsReferenceParameter", ADDR.getNamespacePrefix());
                    refElement.addAttribute(refQName, "true");
                }
            }
            appendElements(soapHeader, getElements());
        } catch (SOAPException ex) {
            throw new AddressingException("Cannot read ws-addressing headers", ex);
        }
    }

    private void appendRequiredHeader(SOAPHeader soapHeader, QName name, AttributedURI value) throws SOAPException {
        if (null == value) {
            throw new AddressingException("Required addressing property missing: " + name);
        }
        SOAPElement element = soapHeader.addChildElement(name);
        element.addTextNode(value.getURI().toString());

        if (this.mustunderstand) {
            String envNS = soapHeader.getParentElement().getNamespaceURI();
            element.addNamespaceDeclaration("soap", envNS);
            element.addAttribute(new QName(envNS, "mustUnderstand", "soap"), "1");
        }
    }

    public void setMu(boolean mu) {
        this.mustunderstand = mu;
    }

    private void appendElements(SOAPElement soapElement, List<Object> elements) {
        try {
            for (Object obj : elements) {
                appendElement(soapElement, obj);
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception ex) {
            throw new AddressingException("Cannot append elements", ex);
        }
    }

    private SOAPElement appendElement(SOAPElement soapElement, Object obj) {
        SOAPElement child;
        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            if (obj instanceof Element) {
                child = factory.createElement((Element) obj);
                soapElement.addChildElement(child);
            } else if (obj instanceof String) {
                Element el = DOMUtils.parse((String) obj);
                child = factory.createElement(el);
                soapElement.addChildElement(child);
            } else {
                throw new AddressingException("Unsupported element: " + obj.getClass().getName());
            }
            return child;
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception ex) {
            throw new AddressingException("Cannot append elements", ex);
        }
    }

    private String getQualifiedName(QName qname) {
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();
        return (prefix != null && prefix.length() > 0) ? (prefix + ":" + localPart) : localPart;
    }
}