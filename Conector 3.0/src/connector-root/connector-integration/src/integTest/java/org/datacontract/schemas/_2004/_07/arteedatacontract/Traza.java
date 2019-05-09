
package org.datacontract.schemas._2004._07.arteedatacontract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Traza complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Traza">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Anio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AnioAnterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Confidencial" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Descargado" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ElementosFisicos" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ErrorTraza" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ErrorTrazaDescripcion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Inciso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IncisoAnterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MesaDestino" type="{http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity}Mesa" minOccurs="0"/>
 *         &lt;element name="MesaDestinoDescripcion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Notificacion" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Numero" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumeroAnterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OrganismoDestino" type="{http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity}Organismo" minOccurs="0"/>
 *         &lt;element name="OrganismoDestinoDescripcion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OrganismoOrigen" type="{http://schemas.datacontract.org/2004/07/ARTEEDataContract.Entity}Organismo" minOccurs="0"/>
 *         &lt;element name="OrganismoOrigenDescripcion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Recibido" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="SeccionDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SeccionOrigen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="TimestampTexto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UnidadEjecutora" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UnidadEjecutoraAnterior" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Traza", propOrder = {
    "anio",
    "anioAnterior",
    "confidencial",
    "descargado",
    "elementosFisicos",
    "errorTraza",
    "errorTrazaDescripcion",
    "id",
    "inciso",
    "incisoAnterior",
    "mesaDestino",
    "mesaDestinoDescripcion",
    "notificacion",
    "numero",
    "numeroAnterior",
    "organismoDestino",
    "organismoDestinoDescripcion",
    "organismoOrigen",
    "organismoOrigenDescripcion",
    "recibido",
    "seccionDestino",
    "seccionOrigen",
    "timestamp",
    "timestampTexto",
    "unidadEjecutora",
    "unidadEjecutoraAnterior"
})
public class Traza {

    @XmlElement(name = "Anio", nillable = true)
    protected String anio;
    @XmlElement(name = "AnioAnterior", nillable = true)
    protected String anioAnterior;
    @XmlElement(name = "Confidencial")
    protected Boolean confidencial;
    @XmlElement(name = "Descargado")
    protected Boolean descargado;
    @XmlElement(name = "ElementosFisicos")
    protected Boolean elementosFisicos;
    @XmlElement(name = "ErrorTraza")
    protected Integer errorTraza;
    @XmlElement(name = "ErrorTrazaDescripcion", nillable = true)
    protected String errorTrazaDescripcion;
    @XmlElement(name = "Id")
    protected Integer id;
    @XmlElement(name = "Inciso", nillable = true)
    protected String inciso;
    @XmlElement(name = "IncisoAnterior", nillable = true)
    protected String incisoAnterior;
    @XmlElement(name = "MesaDestino", nillable = true)
    protected Mesa mesaDestino;
    @XmlElement(name = "MesaDestinoDescripcion", nillable = true)
    protected String mesaDestinoDescripcion;
    @XmlElement(name = "Notificacion", nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar notificacion;
    @XmlElement(name = "Numero", nillable = true)
    protected String numero;
    @XmlElement(name = "NumeroAnterior", nillable = true)
    protected String numeroAnterior;
    @XmlElement(name = "OrganismoDestino", nillable = true)
    protected Organismo organismoDestino;
    @XmlElement(name = "OrganismoDestinoDescripcion", nillable = true)
    protected String organismoDestinoDescripcion;
    @XmlElement(name = "OrganismoOrigen", nillable = true)
    protected Organismo organismoOrigen;
    @XmlElement(name = "OrganismoOrigenDescripcion", nillable = true)
    protected String organismoOrigenDescripcion;
    @XmlElement(name = "Recibido", nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar recibido;
    @XmlElement(name = "SeccionDestino", nillable = true)
    protected String seccionDestino;
    @XmlElement(name = "SeccionOrigen", nillable = true)
    protected String seccionOrigen;
    @XmlElement(name = "Timestamp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;
    @XmlElement(name = "TimestampTexto", nillable = true)
    protected String timestampTexto;
    @XmlElement(name = "UnidadEjecutora", nillable = true)
    protected String unidadEjecutora;
    @XmlElement(name = "UnidadEjecutoraAnterior", nillable = true)
    protected String unidadEjecutoraAnterior;

    /**
     * Gets the value of the anio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnio() {
        return anio;
    }

    /**
     * Sets the value of the anio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnio(String value) {
        this.anio = value;
    }

    /**
     * Gets the value of the anioAnterior property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnioAnterior() {
        return anioAnterior;
    }

    /**
     * Sets the value of the anioAnterior property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnioAnterior(String value) {
        this.anioAnterior = value;
    }

    /**
     * Gets the value of the confidencial property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isConfidencial() {
        return confidencial;
    }

    /**
     * Sets the value of the confidencial property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConfidencial(Boolean value) {
        this.confidencial = value;
    }

    /**
     * Gets the value of the descargado property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDescargado() {
        return descargado;
    }

    /**
     * Sets the value of the descargado property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDescargado(Boolean value) {
        this.descargado = value;
    }

    /**
     * Gets the value of the elementosFisicos property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isElementosFisicos() {
        return elementosFisicos;
    }

    /**
     * Sets the value of the elementosFisicos property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setElementosFisicos(Boolean value) {
        this.elementosFisicos = value;
    }

    /**
     * Gets the value of the errorTraza property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getErrorTraza() {
        return errorTraza;
    }

    /**
     * Sets the value of the errorTraza property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setErrorTraza(Integer value) {
        this.errorTraza = value;
    }

    /**
     * Gets the value of the errorTrazaDescripcion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorTrazaDescripcion() {
        return errorTrazaDescripcion;
    }

    /**
     * Sets the value of the errorTrazaDescripcion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorTrazaDescripcion(String value) {
        this.errorTrazaDescripcion = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
    }

    /**
     * Gets the value of the inciso property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInciso() {
        return inciso;
    }

    /**
     * Sets the value of the inciso property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInciso(String value) {
        this.inciso = value;
    }

    /**
     * Gets the value of the incisoAnterior property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncisoAnterior() {
        return incisoAnterior;
    }

    /**
     * Sets the value of the incisoAnterior property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncisoAnterior(String value) {
        this.incisoAnterior = value;
    }

    /**
     * Gets the value of the mesaDestino property.
     * 
     * @return
     *     possible object is
     *     {@link Mesa }
     *     
     */
    public Mesa getMesaDestino() {
        return mesaDestino;
    }

    /**
     * Sets the value of the mesaDestino property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mesa }
     *     
     */
    public void setMesaDestino(Mesa value) {
        this.mesaDestino = value;
    }

    /**
     * Gets the value of the mesaDestinoDescripcion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMesaDestinoDescripcion() {
        return mesaDestinoDescripcion;
    }

    /**
     * Sets the value of the mesaDestinoDescripcion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMesaDestinoDescripcion(String value) {
        this.mesaDestinoDescripcion = value;
    }

    /**
     * Gets the value of the notificacion property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getNotificacion() {
        return notificacion;
    }

    /**
     * Sets the value of the notificacion property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setNotificacion(XMLGregorianCalendar value) {
        this.notificacion = value;
    }

    /**
     * Gets the value of the numero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Sets the value of the numero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumero(String value) {
        this.numero = value;
    }

    /**
     * Gets the value of the numeroAnterior property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumeroAnterior() {
        return numeroAnterior;
    }

    /**
     * Sets the value of the numeroAnterior property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumeroAnterior(String value) {
        this.numeroAnterior = value;
    }

    /**
     * Gets the value of the organismoDestino property.
     * 
     * @return
     *     possible object is
     *     {@link Organismo }
     *     
     */
    public Organismo getOrganismoDestino() {
        return organismoDestino;
    }

    /**
     * Sets the value of the organismoDestino property.
     * 
     * @param value
     *     allowed object is
     *     {@link Organismo }
     *     
     */
    public void setOrganismoDestino(Organismo value) {
        this.organismoDestino = value;
    }

    /**
     * Gets the value of the organismoDestinoDescripcion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganismoDestinoDescripcion() {
        return organismoDestinoDescripcion;
    }

    /**
     * Sets the value of the organismoDestinoDescripcion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganismoDestinoDescripcion(String value) {
        this.organismoDestinoDescripcion = value;
    }

    /**
     * Gets the value of the organismoOrigen property.
     * 
     * @return
     *     possible object is
     *     {@link Organismo }
     *     
     */
    public Organismo getOrganismoOrigen() {
        return organismoOrigen;
    }

    /**
     * Sets the value of the organismoOrigen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Organismo }
     *     
     */
    public void setOrganismoOrigen(Organismo value) {
        this.organismoOrigen = value;
    }

    /**
     * Gets the value of the organismoOrigenDescripcion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganismoOrigenDescripcion() {
        return organismoOrigenDescripcion;
    }

    /**
     * Sets the value of the organismoOrigenDescripcion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganismoOrigenDescripcion(String value) {
        this.organismoOrigenDescripcion = value;
    }

    /**
     * Gets the value of the recibido property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRecibido() {
        return recibido;
    }

    /**
     * Sets the value of the recibido property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRecibido(XMLGregorianCalendar value) {
        this.recibido = value;
    }

    /**
     * Gets the value of the seccionDestino property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeccionDestino() {
        return seccionDestino;
    }

    /**
     * Sets the value of the seccionDestino property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeccionDestino(String value) {
        this.seccionDestino = value;
    }

    /**
     * Gets the value of the seccionOrigen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeccionOrigen() {
        return seccionOrigen;
    }

    /**
     * Sets the value of the seccionOrigen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeccionOrigen(String value) {
        this.seccionOrigen = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the timestampTexto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestampTexto() {
        return timestampTexto;
    }

    /**
     * Sets the value of the timestampTexto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestampTexto(String value) {
        this.timestampTexto = value;
    }

    /**
     * Gets the value of the unidadEjecutora property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnidadEjecutora() {
        return unidadEjecutora;
    }

    /**
     * Sets the value of the unidadEjecutora property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnidadEjecutora(String value) {
        this.unidadEjecutora = value;
    }

    /**
     * Gets the value of the unidadEjecutoraAnterior property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnidadEjecutoraAnterior() {
        return unidadEjecutoraAnterior;
    }

    /**
     * Sets the value of the unidadEjecutoraAnterior property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnidadEjecutoraAnterior(String value) {
        this.unidadEjecutoraAnterior = value;
    }

}
