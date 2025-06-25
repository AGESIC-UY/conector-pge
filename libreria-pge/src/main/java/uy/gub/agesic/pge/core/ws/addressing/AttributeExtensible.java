package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.util.Map;

public interface AttributeExtensible extends AddressingType {
    Map<QName, String> getAttributes();

    void addAttribute(QName paramQName, String paramString) throws AddressingException;
}