package uy.gub.agesic.pge.core.ws.addressing;

import java.util.List;

public interface ElementExtensible extends AddressingType {
    List<Object> getElements();

    void addElement(Object paramObject);

    boolean removeElement(Object paramObject);
}