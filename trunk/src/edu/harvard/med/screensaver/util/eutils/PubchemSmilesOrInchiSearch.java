// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Uses PUG to do a SMILES or InChI structure search on PubChem, returning a list of PubChem
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
public class PubchemSmilesOrInchiSearch extends PubchemPugClient
{
  // static members

  private static Logger log = Logger.getLogger(PubchemSmilesOrInchiSearch.class);


  // private instance fields

  private String _smilesOrInchi;

  
  // public constructors and methods

  /**
   * Return the list of PubChem CIDs for this SMILES or InChI string, as reported by the PubChem PUG
   * interface, searching for exact match, with non-conflicting stereoisometry. Report an
   * error to the log and return null on error.
   * @param smilesOrInchi the smiles to search for PubChem CIDs with
   * @return the list of PubChem CIDs for this SMILES or InChI string. return null on error.
   */
  synchronized public List<String> getPubchemCidsForSmilesOrInchi(String smilesOrInchi)
  {
    _smilesOrInchi = smilesOrInchi;
    Document searchDocument = createSearchDocumentForSmilesOrInchi(smilesOrInchi);
    return getResultsForSearchDocument(searchDocument);
  }

  public void reportError(String error)
  {
    log.error("Error for smiles or inchi '" + _smilesOrInchi + "': " + error);
  }
  
  
  // private methods

  /**
   * Create and return a PUG search request XML document for the SMILES or InChI string.
   * @param smilesOrInchi the SMILES or InChI string to create a PUG search request XML document for
   * @return the XML document for the SMILES or InChI string
   */
  private Document createSearchDocumentForSmilesOrInchi(String smilesOrInchi) {
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
    createParentedTextNode(document, element, smilesOrInchi);
  
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
}

