package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

public class ReferenceParametersImpl implements ReferenceParameters {
    private final AttributeExtensible attrExt = new AttributeExtensibleImpl();
    private final ElementExtensible elmtExt = new ElementExtensibleImpl();

    public Map<QName, String> getAttributes() {
        return this.attrExt.getAttributes();
    }

    public void addAttribute(QName name, String value) throws AddressingException {
        this.attrExt.addAttribute(name, value);
    }

    public List<Object> getElements() {
        return this.elmtExt.getElements();
    }

    public void addElement(Object element) {
        this.elmtExt.addElement(element);
    }

    public boolean removeElement(Object element) {
        return this.elmtExt.removeElement(element);
    }

    public String getNamespaceURI() {
        return "http://www.w3.org/2005/08/addressing";
    }
}