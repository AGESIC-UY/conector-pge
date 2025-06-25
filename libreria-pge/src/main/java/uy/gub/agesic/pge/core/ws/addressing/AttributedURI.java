package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.net.URI;

public interface AttributedURI extends AddressingType, AttributeExtensible {
    URI getURI();

    void addAttribute(QName paramQName, String paramString);
}