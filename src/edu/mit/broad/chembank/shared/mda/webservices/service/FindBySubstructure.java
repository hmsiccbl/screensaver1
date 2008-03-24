
package edu.mit.broad.chembank.shared.mda.webservices.service;

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
 *         &lt;element name="smilesOrSmarts" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "smilesOrSmarts"
})
@XmlRootElement(name = "findBySubstructure")
public class FindBySubstructure {

    @XmlElement(required = true)
    protected String smilesOrSmarts;

    /**
     * Gets the value of the smilesOrSmarts property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmilesOrSmarts() {
        return smilesOrSmarts;
    }

    /**
     * Sets the value of the smilesOrSmarts property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmilesOrSmarts(String value) {
        this.smilesOrSmarts = value;
    }

}
