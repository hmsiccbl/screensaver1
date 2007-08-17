// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Uses PUG to do a SMILES structure search on PubChem, returning a list of PubChem
 * CIDs.
 * <p>
 * For details on how the PUG interface works, see the
 * <a href="ftp://ftp.ncbi.nlm.nih.gov/pubchem/specifications/pubchem_pug.pdf">PubChem PUG
 * documentation</a>.
 * <p>
 * If things go well, this class should supercede {@link PubchemCidListProvider}. It should be
 * a more comprehensive way to retrieve PubChem CIDs for compounds structures, but that has yet to
 * be shown out.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PubchemSmilesSearch extends PubchemPugClient
{
  // static members

  private static Logger log = Logger.getLogger(PubchemSmilesSearch.class);


  // private instance fields

  private String _smiles;

  
  // public constructors and methods

  /**
   * Return the list of PubChem CIDs for this SMILES string, as reported by the PubChem PUG
   * interface, searching for exact match, with non-conflicting stereoisometry. Report an
   * error to the log and return null on error.
   * @param smiles the smiles to search for PubChem CIDs with
   * @return the list of PubChem CIDs for this SMILES string. return null on error.
   */
  synchronized public List<String> getPubchemCidsForSmiles(String smiles)
  {
    _smiles = smiles;
    Document searchDocument = createSearchDocumentForSmiles(smiles);
    Document outputDocument = getXMLForPugQuery(searchDocument);
    while (! isJobCompleted(outputDocument)) {
      sleep(1000);
      String reqid = getReqidFromOutputDocument(outputDocument);
      Document pollDocument = createPollDocumentForReqid(reqid);      
      outputDocument = getXMLForPugQuery(pollDocument);
    }
    if (! isJobSuccessfullyCompleted(outputDocument)) {
      return null;
    }
    if (! hasResults(outputDocument)) {
      return new ArrayList<String>();
    }
    return getResultsFromOutputDocument(outputDocument);
  }


  public void reportError(String error)
  {
    log.error("Error for smiles '" + _smiles + "': " + error);
  }
  
  
  // private methods

  /**
   * Create and return a PUG search request XML document for the SMILES string.
   * @param smiles the SMILES string to create a PUG search request XML document for
   * @return the XML document for the SMILES string
   */
  private Document createSearchDocumentForSmiles(String smiles) {
    Document document = _documentBuilder.newDocument();
    
    // every elt has a single child, up to PCT-QueryCompoundCS
    
    Element element = createParentedElement(document, document, "PCT-Data");
    element = createParentedElement(document, element, "PCT-Data_input");
    element = createParentedElement(document, element, "PCT-InputData");
    element = createParentedElement(document, element, "PCT-InputData_query");
    element = createParentedElement(document, element, "PCT-Query");
    element = createParentedElement(document, element, "PCT-Query_type");
    element = createParentedElement(document, element, "PCT-QueryType");
    element = createParentedElement(document, element, "PCT-QueryType_css");
    Element queryCompoundCS = createParentedElement(document, element, "PCT-QueryCompoundCS");
  
    // PCT-QueryCompoundCS has three children
    
    element = createParentedElement(document, queryCompoundCS, "PCT-QueryCompoundCS_query");
    element = createParentedElement(document, element, "PCT-QueryCompoundCS_query_data");
    createParentedTextNode(document, element, smiles);
  
    element = createParentedElement(document, queryCompoundCS, "PCT-QueryCompoundCS_type");
    element = createParentedElement(document, element, "PCT-QueryCompoundCS_type_identical");
    element = createParentedElement(document, element, "PCT-CSIdentity");

    // match any structure with non-conflicting stereochemistry, ie, non-specified in one or
    // both structures, or specified and matching in both
    element.setAttribute("value", "same-nonconflict-stereo");
    createParentedTextNode(document, element, "6");
    
    element = createParentedElement(document, queryCompoundCS, "PCT-QueryCompoundCS_results");
    createParentedTextNode(document, element, "2000000");
    return document;
  }


  private Element createParentedElement(Document document, Node parent, String elementName)
  {
    Element element = (Element) document.createElement(elementName);
    parent.appendChild(element);
    return element;
  }

  private Text createParentedTextNode(Document document, Node parent, String text)
  {
    Text textNode = (Text) document.createTextNode(text);
    parent.appendChild(textNode);
    return textNode;
  }

  private boolean isJobCompleted(Document outputDocument)
  {
    if (outputDocument == null) {
      // this happens when there was a connection error in one of the handshakes, with the
      // specified TIMEOUT and NUM_RETRIES. the error has already been reported. the job should
      // be considered completed in this circumstance
      return true;
    }
    String statusValue = getStatusValueFromOutputDocument(outputDocument); 
    return ! (statusValue.equals("running") || statusValue.equals("queued"));
  }

  /**
   * Get the status value from the non-null output document.
   */
  private String getStatusValueFromOutputDocument(Document outputDocument)
  {
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Status");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("PCT-Status node count in response != 1: " + nodes.getLength());
    }
    Element element = (Element) nodes.item(0);
    return element.getAttribute("value");
  }
  
  /**
   * Check the output document to see if the job completed successfully. If it has not, and the
   * error has not previously been reported, then report the error. In any case return true
   * whenever the job completed successfully.
   * @param outputDocument the output document to check for successful job completion
   * @return true whenever the job completed successfully
   */
  private boolean isJobSuccessfullyCompleted(Document outputDocument)
  {
    if (outputDocument == null) {
      // this happens when there was a connection error in one of the handshakes, with the
      // specified TIMEOUT and NUM_RETRIES. the error has already been reported.
      return false;
    }
    String statusValue = getStatusValueFromOutputDocument(outputDocument);
    if (statusValue.equals("success")) {
      return true;
    }
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Status-Message_message");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("PCT-Status-Message_message node count in response != 1: " + nodes.getLength());
    }
    String errorMessage = getTextContent(nodes.item(0));
    reportError(
      "PUG server reported non-success status '" + statusValue +
      "' with error message '" + errorMessage + "'");
    return false;
  }
  
  /**
   * Check the output document to see if this successfully completed job has any results. Return
   * true whenever there are results.
   * @param outputDocument the output document to check to see if there are any results
   * @return true whenever there are results
   */
  private boolean hasResults(Document outputDocument)
  {
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Entrez_webenv");
    return nodes.getLength() != 0;
  }
  
  private List<String> getResultsFromOutputDocument(Document outputDocument) {
    Document resultsDocument = getXMLForEutilsQuery(
      "efetch.fcgi",
      "&db=pccompound" +
      "&rettype=uilist&" +
      "WebEnvRq=1&" +
      "&query_key=" + getQueryKeyFromDocument(outputDocument) +
      "&WebEnv=" + getWebenvFromDocument(outputDocument));

    if (resultsDocument == null) {
      // there was a connection error that has already been reported
      return null;
    }

    List<String> pubchemCids = new ArrayList<String>();
    NodeList nodes = resultsDocument.getElementsByTagName("Id");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      pubchemCids.add(getTextContent(node));
    }
    return pubchemCids;
  }


  private String getWebenvFromDocument(Document document)
  {
    NodeList nodes = document.getElementsByTagName("PCT-Entrez_webenv");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("no PCT-Entrez_webenv node in response");      
    }
    Element element = (Element) nodes.item(0);
    return getTextContent(element);
  }
  
  private String getQueryKeyFromDocument(Document document)
  {
    NodeList nodes = document.getElementsByTagName("PCT-Entrez_query-key");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("no PCT-Entrez_query-key node in response");      
    }
    Element element = (Element) nodes.item(0);
    return getTextContent(element);
  }
  
  private void sleep(long numMillisecondsToSleep) {
    try {
      Thread.sleep(numMillisecondsToSleep);
    }
    catch (InterruptedException e) {
    }
  }


  private String getReqidFromOutputDocument(Document outputDocument)
  {
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Waiting_reqid");
    if (nodes.getLength() != 1) {
      reportError("unexpected count of PCT-Waiting_reqid nodes: " + nodes.getLength());
      return null;
    }
    return getTextContent(nodes.item(0));
  }

  private Document createPollDocumentForReqid(String reqid)
  {

    Document document = _documentBuilder.newDocument();
    
    // every elt has a single child, up to PCT-Request
    
    Element element = createParentedElement(document, document, "PCT-Data");
    element = createParentedElement(document, element, "PCT-Data_input");
    element = createParentedElement(document, element, "PCT-InputData");
    element = createParentedElement(document, element, "PCT-InputData_request");
    Element pctRequest = createParentedElement(document, element, "PCT-Request");
  
    // PCT-Request has two children
    
    element = createParentedElement(document, pctRequest, "PCT-Request_reqid");
    createParentedTextNode(document, element, reqid);
  
    element = createParentedElement(document, pctRequest, "PCT-Request_type");
    element.setAttribute("value", "status");

    return document; 
  }
}

