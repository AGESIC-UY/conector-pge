
package uy.red.pge.servicios.agesic.artee.descargarexpediente;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.datacontract.schemas._2004._07.arteedatacontract.Traza;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the uy.red.pge.servicios.agesic.artee.descargarexpediente package. 
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

    private final static QName _Traza_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/DescargarExpediente", "Traza");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: uy.red.pge.servicios.agesic.artee.descargarexpediente
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DescargarExpedienteIn }
     * 
     */
    public DescargarExpedienteIn createDescargarExpedienteIn() {
        return new DescargarExpedienteIn();
    }

    /**
     * Create an instance of {@link DescargarExpedienteOut }
     * 
     */
    public DescargarExpedienteOut createDescargarExpedienteOut() {
        return new DescargarExpedienteOut();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Traza }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/DescargarExpediente", name = "Traza")
    public JAXBElement<Traza> createTraza(Traza value) {
        return new JAXBElement<Traza>(_Traza_QNAME, Traza.class, null, value);
    }

}
