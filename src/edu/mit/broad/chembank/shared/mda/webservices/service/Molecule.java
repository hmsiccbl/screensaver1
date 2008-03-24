
package edu.mit.broad.chembank.shared.mda.webservices.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Molecule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Molecule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chembankId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="smiles" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="inchi" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Molecule", propOrder = {
    "chembankId",
    "smiles",
    "inchi"
})
public class Molecule {

    @XmlElement(required = true)
    protected String chembankId;
    @XmlElement(required = true)
    protected String smiles;
    @XmlElement(required = true)
    protected String inchi;

    /**
     * Gets the value of the chembankId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChembankId() {
        return chembankId;
    }

    /**
     * Sets the value of the chembankId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChembankId(String value) {
        this.chembankId = value;
    }

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
     * Gets the value of the inchi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInchi() {
        return inchi;
    }

    /**
     * Sets the value of the inchi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInchi(String value) {
        this.inchi = value;
    }

}
