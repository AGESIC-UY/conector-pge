package uy.gub.agesic.pge.core.ws.addressing;

import java.util.ArrayList;
import java.util.List;

public class ElementExtensibleImpl implements ElementExtensible {
    private final List<Object> elements = new ArrayList();

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