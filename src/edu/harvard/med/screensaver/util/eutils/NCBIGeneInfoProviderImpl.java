// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
public class NCBIGeneInfoProviderImpl extends EutilsUtils implements NCBIGeneInfoProvider
{
  
  // static and instance fields
  
  private static final Logger log = Logger.getLogger(NCBIGeneInfoProviderImpl.class);
  private Integer _entrezgeneId;

  
  // public constructor and instance method
  
  /**
   * Construct a <code>NCBIGeneInfoProvider</code> object.
   */
  public NCBIGeneInfoProviderImpl()
  {
    initializeDocumentBuilder();
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.util.eutils.NCBIGeneInfoProvider#getGeneInfoForEntrezgeneId(java.lang.Integer, edu.harvard.med.screensaver.io.workbook.Cell)
   */
  public synchronized NCBIGeneInfo getGeneInfoForEntrezgeneId(Integer entrezgeneId) throws EutilsException
  {
    _entrezgeneId = entrezgeneId;
    Document efetchDocument = getXMLForEutilsQuery("esummary.fcgi", "&db=gene&id=" + entrezgeneId);
    if (efetchDocument == null) {
      return null;
    }
    NodeList nodes = efetchDocument.getElementsByTagName("Item");
    if (nodes.getLength() == 0) {
      throw new EutilsException("Error querying NCBI for EntrezGene ID " + _entrezgeneId + ": no such EntrezGene ID");
    }
    String geneName = getGeneNameFromNodeList(nodes);
    String speciesName = getSpeciesNameFromNodeList(nodes);
    String entrezgeneSymbol = getEntrezgeneSymbolFromNodeList(nodes); 
    if (geneName == null || speciesName == null) {
      return null;
    }
    return new NCBIGeneInfo(geneName, speciesName, entrezgeneSymbol);
  }

  
  // private instance methods

  /**
   * Get the gene name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   * @throws EutilsException 
   */
  private String getGeneNameFromNodeList(NodeList nodes) throws EutilsException
  {
    return getNamedItemFromNodeList(nodes, "Description");
  }

  /**
   * Get the species name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   * @throws EutilsException 
   */
  private String getSpeciesNameFromNodeList(NodeList nodes) throws EutilsException
  {
    return getNamedItemFromNodeList(nodes, "Orgname");
  }

  /**
   * Get the species name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   * @throws EutilsException 
   */
  private String getEntrezgeneSymbolFromNodeList(NodeList nodes) throws EutilsException
  {
    return getNamedItemFromNodeList(nodes, "Name");
  }
}
