// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

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
  private ParseErrorManager _errorManager;
  private Integer _entrezgeneId;
  private Cell _cell;

  
  protected void reportError(Exception e)
  {
    String message;
    if (_entrezgeneId == null) {
      message = "Error querying NCBI: " + e.getMessage();
    }
    else {
      message = "eError querying NCBI for " + _entrezgeneId + ": " + e.getMessage();      
    }
    _errorManager.addError(message, _cell);
  }
  
  
  // public constructor and instance methods
  
  /**
   * Construct a <code>NCBIGeneInfoProvider</code> object.
   * @param errorManager
   */
  public NCBIGeneInfoProvider(ParseErrorManager errorManager)
  {
    _errorManager = errorManager;
    initializeDocumentBuilder();
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
    _entrezgeneId = entrezgeneId;
    _cell = cell;
    Document efetchDocument = getXMLForQuery("esummary.fcgi", "&db=gene&id=" + entrezgeneId);
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
