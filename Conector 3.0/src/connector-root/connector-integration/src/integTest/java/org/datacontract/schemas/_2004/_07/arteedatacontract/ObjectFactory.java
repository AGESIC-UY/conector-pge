
package org.datacontract.schemas._2004._07.arteedatacontract;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.datacontract.schemas._2004._07.arteedatacontract package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Organismo_QNAME = new QName("http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity", "Organismo");
    private final static QName _Traza_QNAME = new QName("http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity", "Traza");
    private final static QName _Mesa_QNAME = new QName("http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity", "Mesa");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.datacontract.schemas._2004._07.arteedatacontract
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Traza }
     * 
     */
    public Traza createTraza() {
        return new Traza();
    }

    /**
     * Create an instance of {@link Organismo }
     * 
     */
    public Organismo createOrganismo() {
        return new Organismo();
    }

    /**
     * Create an instance of {@link Mesa }
     * 
     */
    public Mesa createMesa() {
        return new Mesa();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Organismo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity", name = "Organismo")
    public JAXBElement<Organismo> createOrganismo(Organismo value) {
        return new JAXBElement<Organismo>(_Organismo_QNAME, Organismo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Traza }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity", name = "Traza")
    public JAXBElement<Traza> createTraza(Traza value) {
        return new JAXBElement<Traza>(_Traza_QNAME, Traza.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Mesa }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity", name = "Mesa")
    public JAXBElement<Mesa> createMesa(Mesa value) {
        return new JAXBElement<Mesa>(_Mesa_QNAME, Mesa.class, null, value);
    }

}
