
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
 *         &lt;element name="Expediente" type="{http://schemas.microsoft.com/Message}StreamBody"/>
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
    "expediente"
})
@XmlRootElement(name = "DescargarExpedienteOut")
public class DescargarExpedienteOut {

    @XmlElement(name = "Expediente", required = true)
    protected byte[] expediente;

    /**
     * Gets the value of the expediente property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getExpediente() {
        return expediente;
    }

    /**
     * Sets the value of the expediente property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setExpediente(byte[] value) {
        this.expediente = ((byte[]) value);
    }

}
