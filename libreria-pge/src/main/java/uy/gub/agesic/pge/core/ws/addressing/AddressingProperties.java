package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Map;

public interface AddressingProperties extends AddressingType, Map<QName, AddressingType> {
    AttributedURI getTo();

    void setTo(AttributedURI paramAttributedURI);

    AttributedURI getAction();

    void setAction(AttributedURI paramAttributedURI);

    AttributedURI getMessageID();

    void setMessageID(AttributedURI paramAttributedURI);

    URI getReplyTo();

    void setReplyTo(URI paramURI);

    Relationship[] getRelatesTo();

    void setRelatesTo(Relationship[] paramArrayOfRelationship);

    ReferenceParameters getReferenceParameters();
}