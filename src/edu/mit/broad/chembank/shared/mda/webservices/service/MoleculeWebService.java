
package edu.mit.broad.chembank.shared.mda.webservices.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.3-b02-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "MoleculeWebService", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface MoleculeWebService {


    /**
     * 
     * @param smiles
     * @param threshold
     * @return
     *     returns edu.mit.broad.chembank.shared.mda.webservices.service.ArrayOfMolecule
     * @throws FindBySimilarity1Fault1
     */
    @WebMethod(action = "findBySimilarity")
    @WebResult(name = "findBySimilarityReturn", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service")
    @RequestWrapper(localName = "findBySimilarity", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service", className = "edu.mit.broad.chembank.shared.mda.webservices.service.FindBySimilarity")
    @ResponseWrapper(localName = "findBySimilarityResponse", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service", className = "edu.mit.broad.chembank.shared.mda.webservices.service.FindBySimilarityResponse")
    public ArrayOfMolecule findBySimilarity(
        @WebParam(name = "smiles", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service")
        String smiles,
        @WebParam(name = "threshold", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service")
        double threshold)
        throws FindBySimilarity1Fault1
    ;

    /**
     * 
     * @param smilesOrSmarts
     * @return
     *     returns edu.mit.broad.chembank.shared.mda.webservices.service.ArrayOfMolecule
     * @throws FindBySubstructure2Fault1
     */
    @WebMethod(action = "findBySubstructure")
    @WebResult(name = "findBySubstructureReturn", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service")
    @RequestWrapper(localName = "findBySubstructure", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service", className = "edu.mit.broad.chembank.shared.mda.webservices.service.FindBySubstructure")
    @ResponseWrapper(localName = "findBySubstructureResponse", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service", className = "edu.mit.broad.chembank.shared.mda.webservices.service.FindBySubstructureResponse")
    public ArrayOfMolecule findBySubstructure(
        @WebParam(name = "smilesOrSmarts", targetNamespace = "http://edu.mit.broad.chembank.shared.mda.webservices.service")
        String smilesOrSmarts)
        throws FindBySubstructure2Fault1
    ;

}