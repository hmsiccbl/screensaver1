
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
 *         &lt;element name="findBySimilarityReturn" type="{http://edu.mit.broad.chembank.shared.mda.webservices.service}ArrayOfMolecule"/>
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
    "findBySimilarityReturn"
})
@XmlRootElement(name = "findBySimilarityResponse")
public class FindBySimilarityResponse {

    @XmlElement(required = true)
    protected ArrayOfMolecule findBySimilarityReturn;

    /**
     * Gets the value of the findBySimilarityReturn property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMolecule }
     *     
     */
    public ArrayOfMolecule getFindBySimilarityReturn() {
        return findBySimilarityReturn;
    }

    /**
     * Sets the value of the findBySimilarityReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMolecule }
     *     
     */
    public void setFindBySimilarityReturn(ArrayOfMolecule value) {
        this.findBySimilarityReturn = value;
    }

}
