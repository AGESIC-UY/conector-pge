
package uy.red.pge.servicios.agesic.artee.enviarexpediente;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the uy.red.pge.servicios.agesic.artee.enviarexpediente package. 
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

    private final static QName _UnidadEjecutora_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "UnidadEjecutora");
    private final static QName _AnioAnterior_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "AnioAnterior");
    private final static QName _SubdominioDestino_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "SubdominioDestino");
    private final static QName _UnidadEjecutoraAnterior_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "UnidadEjecutoraAnterior");
    private final static QName _Anio_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Anio");
    private final static QName _ElementosFisicos_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "ElementosFisicos");
    private final static QName _SeccionDestino_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "SeccionDestino");
    private final static QName _DominioDestino_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "DominioDestino");
    private final static QName _IncisoAnterior_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "IncisoAnterior");
    private final static QName _SeccionOrigen_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "SeccionOrigen");
    private final static QName _NumeroAnterior_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "NumeroAnterior");
    private final static QName _Inciso_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Inciso");
    private final static QName _DominioOrigen_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "DominioOrigen");
    private final static QName _Confidencial_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Confidencial");
    private final static QName _Numero_QNAME = new QName("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Numero");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: uy.red.pge.servicios.agesic.artee.enviarexpediente
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EnviarExpedienteIn }
     * 
     */
    public EnviarExpedienteIn createEnviarExpedienteIn() {
        return new EnviarExpedienteIn();
    }

    /**
     * Create an instance of {@link EnviarExpedienteOut }
     * 
     */
    public EnviarExpedienteOut createEnviarExpedienteOut() {
        return new EnviarExpedienteOut();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "UnidadEjecutora")
    public JAXBElement<String> createUnidadEjecutora(String value) {
        return new JAXBElement<String>(_UnidadEjecutora_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "AnioAnterior")
    public JAXBElement<String> createAnioAnterior(String value) {
        return new JAXBElement<String>(_AnioAnterior_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "SubdominioDestino")
    public JAXBElement<String> createSubdominioDestino(String value) {
        return new JAXBElement<String>(_SubdominioDestino_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "UnidadEjecutoraAnterior")
    public JAXBElement<String> createUnidadEjecutoraAnterior(String value) {
        return new JAXBElement<String>(_UnidadEjecutoraAnterior_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "Anio")
    public JAXBElement<String> createAnio(String value) {
        return new JAXBElement<String>(_Anio_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "ElementosFisicos")
    public JAXBElement<Boolean> createElementosFisicos(Boolean value) {
        return new JAXBElement<Boolean>(_ElementosFisicos_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "SeccionDestino")
    public JAXBElement<String> createSeccionDestino(String value) {
        return new JAXBElement<String>(_SeccionDestino_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "DominioDestino")
    public JAXBElement<String> createDominioDestino(String value) {
        return new JAXBElement<String>(_DominioDestino_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "IncisoAnterior")
    public JAXBElement<String> createIncisoAnterior(String value) {
        return new JAXBElement<String>(_IncisoAnterior_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "SeccionOrigen")
    public JAXBElement<String> createSeccionOrigen(String value) {
        return new JAXBElement<String>(_SeccionOrigen_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "NumeroAnterior")
    public JAXBElement<String> createNumeroAnterior(String value) {
        return new JAXBElement<String>(_NumeroAnterior_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "Inciso")
    public JAXBElement<String> createInciso(String value) {
        return new JAXBElement<String>(_Inciso_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "DominioOrigen")
    public JAXBElement<String> createDominioOrigen(String value) {
        return new JAXBElement<String>(_DominioOrigen_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "Confidencial")
    public JAXBElement<Boolean> createConfidencial(Boolean value) {
        return new JAXBElement<Boolean>(_Confidencial_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", name = "Numero")
    public JAXBElement<String> createNumero(String value) {
        return new JAXBElement<String>(_Numero_QNAME, String.class, null, value);
    }

}
