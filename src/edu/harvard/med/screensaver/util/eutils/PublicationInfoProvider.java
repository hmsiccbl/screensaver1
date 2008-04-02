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
import edu.harvard.med.screensaver.model.screens.Publication;


/**
 * Retrieves the information needed from PubMed for a {@link Gene}, based on the PubMed ID.
 * The information needed from PubMed is encapsulated in an {@link PublicationInfo} object.
 * <p>
 * Uses <a href="http://eutils.ncbi.nlm.nih.gov/entrez/query/static/eutils_help.html">NCBI
 * E-utilities</a> to gather the necessary information.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class PublicationInfoProvider extends EutilsUtils
{
  
  // static and instance fields
  
  private static final Logger log = Logger.getLogger(PublicationInfoProvider.class);
  private Integer _pubmedId;

  
  // public constructor and instance method
  
  /**
   * Construct a <code>PublicationInfoProvider</code> object.
   */
  public PublicationInfoProvider()
  {
    initializeDocumentBuilder();
  }
  
  /**
   * Get the {@link PublicationInfo information needed from PubChem} for a {@link Publication},
   * based on the PubMed ID. If any errors occur, report the error and return null.
   * @param pubmedId the EntrezGene ID for the Gene.
   * @return the publication info
   * @throws EutilsException 
   */
  public synchronized PublicationInfo getPublicationInfoForPubmedId(Integer pubmedId) throws EutilsException
  {
    _pubmedId = pubmedId;
    Document esummaryDocument = getXMLForEutilsQuery("esummary.fcgi", "&db=pubmed&id=" + pubmedId);
    if (esummaryDocument == null) {
      return null;
    }
    NodeList nodes = esummaryDocument.getElementsByTagName("Item");
    String yearPublished = getYearPublishedFromNodeList(nodes);
    String authors = getAuthorsFromNodeList(nodes);
    String title = getTitleFromNodeList(nodes); 
    if (yearPublished == null || authors == null || title == null) {
      return null;
    }
    return new PublicationInfo(yearPublished, authors, title);
  }

  
  // protected instance methods
  
  protected void reportError(String nestedMessage)
  {
    String errorMessage = (_pubmedId == null) ?
      "Error querying NCBI: " + nestedMessage :
      "Error querying NCBI for pubmed id " + _pubmedId + ": " + nestedMessage;
    log.error(errorMessage);
  }

  
  // private instance methods

  /**
   * Get the gene name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   * @throws EutilsException 
   */
  private String getYearPublishedFromNodeList(NodeList nodes) throws EutilsException
  {
    String date = getNamedItemFromNodeList(nodes, "PubDate");
    if (date == null) {
      return null;
    }
    return date.split(" ")[0];
  }

  /**
   * Get the species name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   */
  private String getAuthorsFromNodeList(NodeList nodes)
  {
    String authors = "";
    for (String author : getNamedItemsFromNodeList(nodes, "Author")) {
      authors += author + ", ";
    }
    if (authors.equals("")) {
      return null;
    }
    return authors.substring(0, authors.length() - 2);
  }

  /**
   * Get the species name from the list of "Item" element nodes.
   * @param nodes the list of "Item" element nodes
   * @return the species name from the list of "Item" element nodes
   * @throws EutilsException 
   */
  private String getTitleFromNodeList(NodeList nodes) throws EutilsException
  {
    return getNamedItemFromNodeList(nodes, "Title");
  }
}
