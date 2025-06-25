package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;

public class AddressingConstantsImpl implements AddressingConstants {
    static final String URI_ADDRESSING = "http://www.w3.org/2005/08/addressing";
    static final String PREFIX_ADDRESSING = "wsa";

    public String getNamespaceURI() {
        return "http://www.w3.org/2005/08/addressing";
    }

    public String getNamespacePrefix() {
        return "wsa";
    }

    public String getWSDLNamespaceURI() {
        return "http://schemas.xmlsoap.org/wsdl/";
    }

    public String getWSDLNamespacePrefix() {
        return "wsdl";
    }

    public QName getWSDLExtensibilityQName() {
        return null;
    }

    public QName getWSDLActionQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "Action", "wsa");
    }

    public String getAnonymousURI() {
        return "http://www.w3.org/2005/08/addressing/anonymous";
    }

    public String getNoneURI() {
        return "http://www.w3.org/2005/08/addressing/none";
    }

    public QName getFromQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "From", "wsa");
    }

    public QName getToQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "To", "wsa");
    }

    public QName getReplyToQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "ReplyTo", "wsa");
    }

    public QName getFaultToQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "FaultTo", "wsa");
    }

    public QName getActionQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "Action", "wsa");
    }

    public QName getMessageIDQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "MessageID", "wsa");
    }

    public QName getRelationshipReplyQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "Reply", "wsa");
    }

    public QName getRelatesToQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "RelatesTo", "wsa");
    }

    public String getRelationshipTypeName() {
        return "RelationshipType";
    }

    public QName getMetadataQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "Metadata", "wsa");
    }

    public QName getAddressQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "Address", "wsa");
    }

    public QName getReferenceParametersQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "ReferenceParameters", "wsa");
    }

    public String getPackageName() {
        return getClass().getPackage().getName();
    }

    public String getIsReferenceParameterName() {
        throw new UnsupportedOperationException();
    }

    public QName getInvalidMapQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "InvalidMessageInformationHeader", "wsa");
    }

    public QName getMapRequiredQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "MessageAddressingHeaderRequired", "wsa");
    }

    public QName getDestinationUnreachableQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "DestinationUnreachable", "wsa");
    }

    public QName getActioNotSupportedQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "ActionNotSupported", "wsa");
    }

    public QName getEndpointUnavailableQName() {
        return new QName("http://www.w3.org/2005/08/addressing", "EndpointUnavailable", "wsa");
    }

    public String getDefaultFaultAction() {
        return "http://www.w3.org/2005/08/addressing/fault";
    }

    public String getActionNotSupportedText() {
        return "Action not supported";
    }

    public String getDestinationUnreachableText() {
        return "Destination unreachable";
    }

    public String getEndpointUnavailableText() {
        return "Endpoint unavailable";
    }

    public String getInvalidMapText() {
        return "Invalid Map";
    }

    public String getMapRequiredText() {
        return "A required header representing a Message Addressing Property is not present";
    }
}