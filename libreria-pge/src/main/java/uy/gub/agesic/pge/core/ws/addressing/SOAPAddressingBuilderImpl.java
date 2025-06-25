package uy.gub.agesic.pge.core.ws.addressing;

import java.net.URI;
import java.net.URISyntaxException;

public class SOAPAddressingBuilderImpl extends AddressingBuilder {
    public AttributedURI newURI(URI uri) {
        return new AttributedURIImpl(uri);
    }

    public AttributedURI newURI(String uri) throws URISyntaxException {
        return newURI(new URI(uri));
    }

    public AddressingProperties newAddressingProperties() {
        return new SOAPAddressingPropertiesImpl();
    }

    public AddressingConstants newAddressingConstants() {
        return new AddressingConstantsImpl();
    }

    public String getNamespaceURI() {
        return "http://www.w3.org/2005/08/addressing";
    }
}