
package edu.mit.broad.chembank.shared.mda.webservices.service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.mit.broad.chembank.shared.mda.webservices.service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FindBySimilarityFault1_QNAME = new QName("http://edu.mit.broad.chembank.shared.mda.webservices.service", "findBySimilarityFault1");
    private final static QName _FindBySubstructureFault1_QNAME = new QName("http://edu.mit.broad.chembank.shared.mda.webservices.service", "findBySubstructureFault1");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.mit.broad.chembank.shared.mda.webservices.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FindBySubstructureResponse }
     * 
     */
    public FindBySubstructureResponse createFindBySubstructureResponse() {
        return new FindBySubstructureResponse();
    }

    /**
     * Create an instance of {@link WebServiceException }
     * 
     */
    public WebServiceException createWebServiceException() {
        return new WebServiceException();
    }

    /**
     * Create an instance of {@link ArrayOfMolecule }
     * 
     */
    public ArrayOfMolecule createArrayOfMolecule() {
        return new ArrayOfMolecule();
    }

    /**
     * Create an instance of {@link FindBySubstructure }
     * 
     */
    public FindBySubstructure createFindBySubstructure() {
        return new FindBySubstructure();
    }

    /**
     * Create an instance of {@link Molecule }
     * 
     */
    public Molecule createMolecule() {
        return new Molecule();
    }

    /**
     * Create an instance of {@link FindBySimilarityResponse }
     * 
     */
    public FindBySimilarityResponse createFindBySimilarityResponse() {
        return new FindBySimilarityResponse();
    }

    /**
     * Create an instance of {@link FindBySimilarity }
     * 
     */
    public FindBySimilarity createFindBySimilarity() {
        return new FindBySimilarity();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebServiceException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service", name = "findBySimilarityFault1")
    public JAXBElement<WebServiceException> createFindBySimilarityFault1(WebServiceException value) {
        return new JAXBElement<WebServiceException>(_FindBySimilarityFault1_QNAME, WebServiceException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebServiceException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service", name = "findBySubstructureFault1")
    public JAXBElement<WebServiceException> createFindBySubstructureFault1(WebServiceException value) {
        return new JAXBElement<WebServiceException>(_FindBySubstructureFault1_QNAME, WebServiceException.class, null, value);
    }

}
