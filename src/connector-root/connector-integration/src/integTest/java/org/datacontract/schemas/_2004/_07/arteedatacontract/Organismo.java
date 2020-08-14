
package org.datacontract.schemas._2004._07.arteedatacontract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Organismo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Organismo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Activo" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ClavePublica" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Descripcion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Direccion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Dominio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
@XmlType(name = "Organismo", propOrder = {
    "activo",
    "clavePublica",
    "descripcion",
    "direccion",
    "dominio",
    "email",
    "id",
    "telefono"
})
public class Organismo {

    @XmlElement(name = "Activo")
    protected Boolean activo;
    @XmlElement(name = "ClavePublica", nillable = true)
    protected String clavePublica;
    @XmlElement(name = "Descripcion", nillable = true)
    protected String descripcion;
    @XmlElement(name = "Direccion", nillable = true)
    protected String direccion;
    @XmlElement(name = "Dominio", nillable = true)
    protected String dominio;
    @XmlElement(name = "Email", nillable = true)
    protected String email;
    @XmlElement(name = "Id")
    protected Integer id;
    @XmlElement(name = "Telefono", nillable = true)
    protected String telefono;

    /**
     * Gets the value of the activo property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isActivo() {
        return activo;
    }

    /**
     * Sets the value of the activo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActivo(Boolean value) {
        this.activo = value;
    }

    /**
     * Gets the value of the clavePublica property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClavePublica() {
        return clavePublica;
    }

    /**
     * Sets the value of the clavePublica property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClavePublica(String value) {
        this.clavePublica = value;
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
     * Gets the value of the direccion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * Sets the value of the direccion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDireccion(String value) {
        this.direccion = value;
    }

    /**
     * Gets the value of the dominio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDominio() {
        return dominio;
    }

    /**
     * Sets the value of the dominio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDominio(String value) {
        this.dominio = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
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
