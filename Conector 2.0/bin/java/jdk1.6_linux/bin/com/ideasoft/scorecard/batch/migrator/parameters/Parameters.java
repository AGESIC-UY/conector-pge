//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.08.18 at 10:24:49 AM UYT 
//


package com.ideasoft.scorecard.batch.migrator.parameters;

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
 *         &lt;element ref="{}scorecard"/>
 *         &lt;element ref="{}metadata"/>
 *         &lt;element ref="{}o3Schema"/>
 *         &lt;element ref="{}output"/>
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
    "scorecard",
    "metadata",
    "o3Schema",
    "output"
})
@XmlRootElement(name = "parameters")
public class Parameters {

    @XmlElement(required = true)
    protected String scorecard;
    @XmlElement(required = true)
    protected String metadata;
    @XmlElement(required = true)
    protected String o3Schema;
    @XmlElement(required = true)
    protected Output output;

    /**
     * Gets the value of the scorecard property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScorecard() {
        return scorecard;
    }

    /**
     * Sets the value of the scorecard property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScorecard(String value) {
        this.scorecard = value;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetadata(String value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the o3Schema property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getO3Schema() {
        return o3Schema;
    }

    /**
     * Sets the value of the o3Schema property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setO3Schema(String value) {
        this.o3Schema = value;
    }

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link Output }
     *     
     */
    public Output getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link Output }
     *     
     */
    public void setOutput(Output value) {
        this.output = value;
    }

}