
package edu.mit.broad.chembank.shared.mda.webservices.service;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfMolecule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfMolecule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="molecule" type="{http://edu.mit.broad.chembank.shared.mda.webservices.service}Molecule" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfMolecule", propOrder = {
    "molecule"
})
public class ArrayOfMolecule {

    protected List<Molecule> molecule;

    /**
     * Gets the value of the molecule property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the molecule property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMolecule().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Molecule }
     * 
     * 
     */
    public List<Molecule> getMolecule() {
        if (molecule == null) {
            molecule = new ArrayList<Molecule>();
        }
        return this.molecule;
    }

}
