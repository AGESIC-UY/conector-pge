package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AttributedURIImpl implements AttributedURI {
    private final URI uri;
    private final Map<QName, String> extMap = new HashMap<QName, String>();

    public AttributedURIImpl(URI uri) {
        this.uri = uri;
    }

    public AttributedURIImpl(String uri) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex.toString());
        }
    }

    public URI getURI() {
        return this.uri;
    }

    public String getNamespaceURI() {
        return "http://www.w3.org/2005/08/addressing";
    }

    public Map<QName, String> getAttributes() {
        return this.extMap;
    }

    public void addAttribute(QName name, String value) throws AddressingException {
        this.extMap.put(name, value);
    }
}