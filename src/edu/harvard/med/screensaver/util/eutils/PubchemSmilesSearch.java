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

  
  // public constructors and methods

  public List<String> getPubchemCidsForSmiles(String smiles)
  {
    Document searchDocument = createSearchDocumentForSmiles(smiles);
    Document outputDocument = getXMLForPugQuery(searchDocument);
    while (! isJobCompleted(outputDocument)) {
      sleep(1000);
      String reqid = getReqidFromOutputDocument(outputDocument);
      if (reqid == null) {
        // TODO: see if this still happens any more now that we have added that check ofr
        // PUG data or server errors
        log.error("missing reqid!");
        printDocumentToOutputStream(outputDocument, System.out);
        System.exit(100134);
        outputDocument = getXMLForPugQuery(searchDocument);
        continue;
      }
      Document pollDocument = createPollDocumentForReqid(reqid);      
      outputDocument = getXMLForPugQuery(pollDocument);
    }
    if (outputDocument == null) {
      return new ArrayList<String>();
    }
    
    List<String> pubchemCids = new ArrayList<String>();
    if (hasResults(outputDocument)) {
      Document resultsDocument = getXMLForEutilsQuery(
        "efetch.fcgi",
        "&db=pccompound" +
        "&rettype=uilist&" +
        "WebEnvRq=1&" +
        "&query_key=" + getQueryKeyFromDocument(outputDocument) +
        "&WebEnv=" + getWebenvFromDocument(outputDocument));
      NodeList nodes = resultsDocument.getElementsByTagName("Id");
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        pubchemCids.add(getTextContent(node));
      }
    }
    return pubchemCids;
  }


  public void reportError(String error)
  {
    log.error(error);
  }
  
  
  // private methods

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
  
  private boolean isJobCompleted(Document document)
  {
    NodeList nodes = document.getElementsByTagName("PCT-Status");
    if (nodes.getLength() != 1) {
      throw new RuntimeException("no PCT-Status node in response");
    }
    Element element = (Element) nodes.item(0);
    String statusValue = element.getAttribute("value");
    if (statusValue.equals("data-error") || statusValue.equals("server-error")) {
      String errorMessage =
        getTextContent(document.getElementsByTagName("PCT-Status-Message_message").item(0));
      reportError("PUG server reported data or server error: " + errorMessage);
      return true;
    }
    return statusValue.equals("success");
  }
  
  private boolean hasResults(Document document)
  {
    NodeList nodes = document.getElementsByTagName("PCT-Entrez_webenv");
    return nodes.getLength() != 0;
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

