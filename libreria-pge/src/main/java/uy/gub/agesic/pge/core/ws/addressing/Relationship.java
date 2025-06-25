package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.net.URI;

public interface Relationship extends AttributeExtensible {
    URI getID();

    QName getType();

    void setType(QName paramQName);
}