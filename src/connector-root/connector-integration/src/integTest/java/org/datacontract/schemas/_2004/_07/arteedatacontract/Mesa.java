
package org.datacontract.schemas._2004._07.arteedatacontract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Mesa complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Mesa">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Activa" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Confidencial" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Descripcion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="PorDefecto" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="Subdominio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Telefono" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Mesa", propOrder = {
    "activa",
    "confidencial",
    "descripcion",
    "id",
    "porDefecto",
    "subdominio",
    "telefono"
})
public class Mesa {

    @XmlElement(name = "Activa")
    protected Boolean activa;
    @XmlElement(name = "Confidencial")
    protected Boolean confidencial;
    @XmlElement(name = "Descripcion", nillable = true)
    protected String descripcion;
    @XmlElement(name = "Id")
    protected Integer id;
    @XmlElement(name = "PorDefecto")
    protected Boolean porDefecto;
    @XmlElement(name = "Subdominio", nillable = true)
    protected String subdominio;
    @XmlElement(name = "Telefono", nillable = true)
    protected String telefono;

    /**
     * Gets the value of the activa property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isActiva() {
        return activa;
    }

    /**
     * Sets the value of the activa property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActiva(Boolean value) {
        this.activa = value;
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
     * Gets the value of the descripcion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Sets the value of the descripcion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescripcion(String value) {
        this.descripcion = value;
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
     * Gets the value of the porDefecto property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPorDefecto() {
        return porDefecto;
    }

    /**
     * Sets the value of the porDefecto property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPorDefecto(Boolean value) {
        this.porDefecto = value;
    }

    /**
     * Gets the value of the subdominio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubdominio() {
        return subdominio;
    }

    /**
     * Sets the value of the subdominio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubdominio(String value) {
        this.subdominio = value;
    }

    /**
     * Gets the value of the telefono property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Sets the value of the telefono property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTelefono(String value) {
        this.telefono = value;
    }

}
