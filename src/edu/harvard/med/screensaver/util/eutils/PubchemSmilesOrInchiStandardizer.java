// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/util/eutils/PubchemSmilesOrInchiSearch.java $
// $Id: PubchemSmilesOrInchiSearch.java 2283 2008-04-02 19:29:28Z js163 $
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

/**
 * Uses PUG to do a SMILES or InChI standardization; see <a
 * href="http://pubchem.ncbi.nlm.nih.gov/pug/pughelp.html#33">PubChem
 * Standardization Tasks</a>.
 * <p>
 * For details on how the PUG interface works, see the <a
 * href="ftp://ftp.ncbi.nlm.nih.gov/pubchem/specifications/pubchem_pug.pdf">PubChem
 * PUG documentation</a>.
 * <p>
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class PubchemSmilesOrInchiStandardizer extends PubchemPugClient
{
  // static members

  private static Logger log = Logger.getLogger(PubchemSmilesOrInchiStandardizer.class);


  // private instance fields

  private String _smilesOrInchi;
  private CompoundIdType _type;

  
  // public constructors and methods

  /**
   * Return the SMILES or InChI string, as produced by the PubChem standardizer
   * PUG service. Report an error to the log and return null on error.
   * 
   * @param smilesOrInchi the smiles to search for PubChem CIDs with
   * @return the standardized SMILES orInChI string. return null on empty result
   *         or error.
   * @throws EutilsException
   */
  synchronized public String getPubchemStandardizedSmilesOrInchi(String smilesOrInchi, CompoundIdType type) throws EutilsException
  {
    _smilesOrInchi = smilesOrInchi;
    _type = type;
    Document searchDocument = createSearchDocumentForSmilesOrInchi(smilesOrInchi, type);
    List<String> results = getResultsForSearchDocument(searchDocument);
    if (results != null && results.size() > 0) {
      return results.get(0);
    }
    return null;
  }

  @Override
  protected List<String> getResultsFromOutputDocument(Document outputDocument) throws EutilsException {
    return extractResultFromOutputDocument(outputDocument);
  }

  @Override
  protected boolean hasResults(Document outputDocument) {
    NodeList nodes = outputDocument.getElementsByTagName("PCT-Structure_structure_string");
    return nodes.getLength() != 0;
  }

  public void reportError(String error)
  {
    log.error("Error for smiles or inchi '" + _smilesOrInchi + "': " + error);
  }
  
  
  // private methods
  
  

/*
<PCT-Data>
  <PCT-Data_input>
    <PCT-InputData>
      <PCT-InputData_standardize>
        <PCT-Standardize>
          <PCT-Standardize_structure>
            <PCT-Structure>
              <PCT-Structure_structure>
                <PCT-Structure_structure_string>C1=NC2=C(N1)C(=O)N=C(N2)N
                </PCT-Structure_structure_string>
              </PCT-Structure_structure>
              <PCT-Structure_format>
                <PCT-StructureFormat value="smiles"/>
              </PCT-Structure_format>
            </PCT-Structure>
          </PCT-Standardize_structure>
          <PCT-Standardize_oformat>
            <PCT-StructureFormat value="smiles"/>
          </PCT-Standardize_oformat>
        </PCT-Standardize>
      </PCT-InputData_standardize>
    </PCT-InputData>
  </PCT-Data_input>
</PCT-Data>
*/

  /**
   * Create and return a PUG search request XML document for the SMILES or InChI string.
   * @param smilesOrInchi the SMILES or InChI string to create a PUG search request XML document for
   * @param type 
   * @return the XML document for the SMILES or InChI string
   */
  private Document createSearchDocumentForSmilesOrInchi(String smilesOrInchi, CompoundIdType type) {
    Document document = _documentBuilder.newDocument();
    
    // every elt has a single child, up to PCT-QueryCompoundCS
    
    Element element = createParentedElement(document, document, "PCT-Data");
    element = createParentedElement(document, element, "PCT-Data_input");
    element = createParentedElement(document, element, "PCT-InputData");
    element = createParentedElement(document, element, "PCT-InputData_standardize");
    Element standardizeElement = createParentedElement(document, element, "PCT-Standardize");
    {
      element = createParentedElement(document, standardizeElement, "PCT-Standardize_structure");
      Element structureElement = createParentedElement(document, element, "PCT-Structure");
      {    
        element = createParentedElement(document, structureElement, "PCT-Structure_structure");
        element = createParentedElement(document, element, "PCT-Structure_structure_string");
        createParentedTextNode(document, element, smilesOrInchi);
      }
      {
        element = createParentedElement(document, structureElement, "PCT-Structure_format");
        element = createParentedElement(document, element, "PCT-StructureFormat");
        element.setAttribute("value", type.toString().toLowerCase());
      }
    }
    {
      element = createParentedElement(document, standardizeElement, "PCT-Standardize_oformat");
      element = createParentedElement(document, element, "PCT-StructureFormat");
      element.setAttribute("value", type.toString().toLowerCase());
    }

    return document;
  }

  private List<String> extractResultFromOutputDocument(Document resultsDocument)
  {
    List<String> compoundIds = new ArrayList<String>();
    NodeList nodes = resultsDocument.getElementsByTagName("PCT-Structure_structure_string");
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      compoundIds.add(getTextContent(node));
    }
    return compoundIds;
  }
}

