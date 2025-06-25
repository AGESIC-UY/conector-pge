package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class AttributeExtensibleImpl implements AttributeExtensible {
    private final Map<QName, String> extMap = new HashMap<QName, String>();

    public Map<QName, String> getAttributes() {
        return this.extMap;
    }

    public void addAttribute(QName name, String value) throws AddressingException {
        this.extMap.put(name, value);
    }

    public String getNamespaceURI() {
        return "http://www.w3.org/2005/08/addressing";
    }
}