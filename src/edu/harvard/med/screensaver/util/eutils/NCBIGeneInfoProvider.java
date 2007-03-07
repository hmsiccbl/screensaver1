// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.model.libraries.Gene;


/**
 * Retrieves the information needed from NCBI for a {@link Gene}, based on the EntrezGene ID.
 * The information needed from NCBI is encapsulated in an {@link NCBIGeneInfo} object.
 * <p>
 * Uses <a href="http://eutils.ncbi.nlm.nih.gov/entrez/query/static/eutils_help.html">NCBI
 * E-utilities</a> to gather the necessary information.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class NCBIGeneInfoProvider extends EutilsQueryPerformer
{
  
  // static fields
  
  private static final Logger log = Logger.getLogger(NCBIGeneInfoProvider.class);
  private static final String EFETCH_URL_PREFIX =
    "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&retmode=xml&id=";
  private static final int NUM_RETRIES = 5;
  private static final int CONNECT_TIMEOUT = 5000; // in millisecs

  
  // instance fields
  
  private ParseErrorManager _errorManager;
  private DocumentBuilder _documentBuilder;
  private Integer _entrezgeneId;
  private Cell _cell;

  
  // public constructor and instance methods
  
  /**
   * Construct a <code>NCBIGeneInfoProvider</code> object.
   * @param errorManager
   */
  public NCBIGeneInfoProvider(ParseErrorManager errorManager)
  {
    _errorManager = errorManager;
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      _documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      _errorManager.addError(
        "unable to initialize the XML document builder: " + e.getMessage());
    }
  }
  
  /**
   * Get the {@link NCBIGeneInfo information needed from NCBI} for a {@link Gene}, based on
   * the EntrezGene ID. If any errors occur, report the error and return null.
   * @param entrezgeneId the EntrezGene ID for the Gene.
   * @param cell the cell to specify when reporting an error
   * @return the NCBIGeneInfo
   */
  public synchronized NCBIGeneInfo getGeneInfoForEntrezgeneId(Integer entrezgeneId, Cell cell)
  {
    if (_documentBuilder == null) {
      return null;
    }
    _entrezgeneId = entrezgeneId;
    _cell = cell;
    InputStream efetchContent = getContent(EFETCH_URL_PREFIX + entrezgeneId);
    if (efetchContent == null) {
      return null;
    }
    
    Document efetchDocument = getEfetchDocument(efetchContent);
    if (efetchDocument == null) {
      return null;
    }
    NodeList nodes = efetchDocument.getElementsByTagName("Item");
    String geneName = getGeneNameFromNodeList(nodes);
    String speciesName = getSpeciesNameFromNodeList(nodes);
    String entrezgeneSymbol = getEntrezgeneSymbolFromNodeList(nodes); 
    if (geneName == null || speciesName == null) {
      return null;
    }
    return new NCBIGeneInfo(geneName, speciesName, entrezgeneSymbol);
  }

  /**
   * Get the content of the response to an HTTP request as an {@link InputStream}.
   * Report an error and return null if there is any problem making the request.
   * @param url the URL of the HTTP request
   * @return the content of the response. Return null if there is any problem making the
   * request.
   */
  private InputStream getContent(String url)
  {
    for (int i = 0; i < NUM_RETRIES; i ++) {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(CONNECT_TIMEOUT);
        connection.connect();
        return connection.getInputStream();
      }
      catch (Exception e) {
        log.warn(
          "unable to get URL connection to NCBI for " + _entrezgeneId + ": " +
          e.getMessage());
      }
    }
    _errorManager.addError(
      "unable to get URL connection to NCBI for " + _entrezgeneId + " after " +
      NUM_RETRIES + " tries.",
      _cell);
    return null;
  }

  /**
   * Translate the EFetch result content into a DOM Document. Return the Document. Report an
   * error and return null if there is a problem building the Document.
   * @param efetchContent the EFetch result content as an input stream
   * @return the DOM Document. Return null if there is a problem building the Document.
   */
  private Document getEfetchDocument(InputStream efetchContent)
  {
    try {
      return _documentBuilder.parse(efetchContent);
    }
    catch (Exception e) {
      _errorManager.addError(
        "unable to get content from NCBI for " + _entrezgeneId + ": " + e.getMessage(),
        _cell);
      return null;
    }
  }
  
  /**
   * Get the gene name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   */
  private String getGeneNameFromNodeList(NodeList nodes)
  {
    return getNamedItemFromNodeList(nodes, "Description");
  }

  /**
   * Get the species name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   */
  private String getSpeciesNameFromNodeList(NodeList nodes)
  {
    return getNamedItemFromNodeList(nodes, "Orgname");
  }

  /**
   * Get the species name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   */
  private String getEntrezgeneSymbolFromNodeList(NodeList nodes)
  {
    return getNamedItemFromNodeList(nodes, "Name");
  }
  
  /**
   * Find the element node in the node list that has an attribute named "Name" with the
   * specified attribute value. Return the text content of that element node. 
   * @param nodes the list of element nodes
   * @param attributeValue the attribute value
   * @return the text content of the specified element node. Return null if the specified
   * element node is not found. 
   */
  private String getNamedItemFromNodeList(NodeList nodes, String attributeValue)
  {
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node.getAttributes().getNamedItem("Name").getNodeValue().equals(attributeValue)) {
        return getTextContent(node);
      }
    }
    _errorManager.addError(
      "NCBI EFetch did not return " + attributeValue + " for " + _entrezgeneId,
      _cell);
    return null;
  }
}
