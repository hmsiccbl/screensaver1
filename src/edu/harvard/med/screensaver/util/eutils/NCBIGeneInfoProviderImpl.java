// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.io.File;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.Workbook;
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
public class NCBIGeneInfoProviderImpl extends EutilsUtils
{
  
  // static and instance fields
  
  private static final Logger log = Logger.getLogger(NCBIGeneInfoProviderImpl.class);
  private ParseErrorManager _errorManager;
  private Integer _entrezgeneId;
  private Cell _cell;

  
  public static void main(String [] args)
  {
    ParseErrorManager manager = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(""), manager);
    Cell.Factory factory = new Cell.Factory(workbook, 0, manager); 
    NCBIGeneInfoProviderImpl provider = new NCBIGeneInfoProviderImpl(manager);
    NCBIGeneInfo info = provider.getGeneInfoForEntrezgeneId(400714, factory.getCell((short) 0, 0));
    
    for (WorkbookParseError error : manager.getErrors()) {
      System.out.println("error = " + error);
    }
    
    System.out.println("symbol = " + info.getEntrezgeneSymbol());
    System.out.println("gene name = " + info.getGeneName());
    
  }
  
  // public constructor and instance method
  
  /**
   * Construct a <code>NCBIGeneInfoProvider</code> object.
   * @param errorManager
   */
  public NCBIGeneInfoProviderImpl(ParseErrorManager errorManager)
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
    Document efetchDocument = getXMLForEutilsQuery("esummary.fcgi", "&db=gene&id=" + entrezgeneId);
    if (efetchDocument == null) {
      return null;
    }
    NodeList nodes = efetchDocument.getElementsByTagName("Item");
    if (nodes.getLength() == 0) {
      reportError("no such EntrezGene ID");
      return null;
    }
    String geneName = getGeneNameFromNodeList(nodes);
    String speciesName = getSpeciesNameFromNodeList(nodes);
    String entrezgeneSymbol = getEntrezgeneSymbolFromNodeList(nodes); 
    if (geneName == null || speciesName == null) {
      return null;
    }
    return new NCBIGeneInfo(geneName, speciesName, entrezgeneSymbol);
  }

  
  // protected instance methods
  
  protected void reportError(String nestedMessage)
  {
    String errorMessage = (_entrezgeneId == null) ?
      "Error querying NCBI: " + nestedMessage :
      "Error querying NCBI for EntrezGene ID " + _entrezgeneId + ": " + nestedMessage;      
    log.error(errorMessage);
    if (_cell == null) {
      _errorManager.addError(errorMessage);
    }
    else {
      _errorManager.addError(errorMessage, _cell);      
    }
  }

  
  // private instance methods

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
}
