
package uy.red.pge.servicios.agesic.artee.descargarexpediente;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DominioOrigen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Subdominio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dominioOrigen",
    "subdominio"
})
@XmlRootElement(name = "DescargarExpedienteIn")
public class DescargarExpedienteIn {

    @XmlElement(name = "DominioOrigen", nillable = true)
    protected String dominioOrigen;
    @XmlElement(name = "Subdominio", nillable = true)
    protected String subdominio;

    /**
     * Gets the value of the dominioOrigen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDominioOrigen() {
        return dominioOrigen;
    }

    /**
     * Sets the value of the dominioOrigen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDominioOrigen(String value) {
        this.dominioOrigen = value;
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

}
