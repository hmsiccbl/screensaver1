// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.screens.Publication;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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
  public synchronized Publication getPublicationForPubmedId(Integer pubmedId) throws EutilsException
  {
    _pubmedId = pubmedId;
    Document esummaryDocument = getXMLForEutilsQuery("esummary.fcgi", "&db=pubmed&id=" + pubmedId);
    if (esummaryDocument == null) {
      return null;
    }
    NodeList nodes = esummaryDocument.getElementsByTagName("Item");
    Publication publication = new Publication();
    publication.setPubmedId(pubmedId);
    publication.setTitle(getNamedItemFromNodeList(nodes, "Title"));
    publication.setYearPublished(getYearPublishedFromNodeList(nodes));
    publication.setAuthors(getAuthorsFromNodeList(nodes));
    publication.setVolume(getNamedItemFromNodeList(nodes, "Volume"));
    publication.setJournal(getNamedItemFromNodeList(nodes, "FullJournalName"));
    publication.setPages(getNamedItemFromNodeList(nodes, "Pages"));
    return publication;
  }

  
  // protected instance methods
  
  private String getJournalItem(Document doc, String itemName)
  {
    NodeList journalNodes = doc.getElementsByTagName("*").item(0).getChildNodes();
    for (int i = 0; i < journalNodes.getLength(); ++i) {
      Node journalNode = journalNodes.item(i);
      if (journalNode.getNodeName().equals(itemName)) {
        return journalNode.getTextContent();
      }
    }
    return null;
  }

  protected void reportError(String nestedMessage)
    {
    String errorMessage = (_pubmedId == null) ?
      "Error querying NCBI: " + nestedMessage :
      "Error querying NCBI for pubmed id " + _pubmedId + ": " + nestedMessage;
    log.error(errorMessage);
  }

  
  // private instance methods

  /**
   * @param nodes the list of "Item" element nodes
   * @return the year published from the list of "Item" element nodes
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
   * @param nodes the list of "Item" element nodes
   * @return the authors (comma-delimited list) from the list of "Item" element nodes
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
}
