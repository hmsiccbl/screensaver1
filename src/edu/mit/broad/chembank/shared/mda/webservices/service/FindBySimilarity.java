
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
 *         &lt;element name="smiles" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="threshold" type="{http://www.w3.org/2001/XMLSchema}double"/>
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
    "smiles",
    "threshold"
})
@XmlRootElement(name = "findBySimilarity")
public class FindBySimilarity {

    @XmlElement(required = true)
    protected String smiles;
    protected double threshold;

    /**
     * Gets the value of the smiles property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmiles() {
        return smiles;
    }

    /**
     * Sets the value of the smiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmiles(String value) {
        this.smiles = value;
    }

    /**
     * Gets the value of the threshold property.
     * 
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Sets the value of the threshold property.
     * 
     */
    public void setThreshold(double value) {
        this.threshold = value;
    }

}
