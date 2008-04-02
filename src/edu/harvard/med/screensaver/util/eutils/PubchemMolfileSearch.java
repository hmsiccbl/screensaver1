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
 * Uses PUG to do a molfile structure search on PubChem, returning a list of PubChem
 * CIDs.
 * <p>
 * For details on how the PUG interface works, see the
 * <a href="ftp://ftp.ncbi.nlm.nih.gov/pubchem/specifications/pubchem_pug.pdf">PubChem PUG
 * documentation</a>.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PubchemMolfileSearch extends PubchemPugClient
{
  // static members

  private static Logger log = Logger.getLogger(PubchemMolfileSearch.class);

  
  // private instance fields

  private String _molfile;

  
  // public constructors and methods

  /**
   * Return the list of PubChem CIDs for this molfile string, as reported by the PubChem PUG
   * interface, searching for exact match, with non-conflicting stereoisometry. Report an
   * error to the log and return null on error.
   * @param molfile the molfile to search for PubChem CIDs with
   * @return the list of PubChem CIDs for this molfile string. return null on error.
   * @throws EutilsException 
   */
  synchronized public List<String> getPubchemCidsForMolfile(String molfile) throws EutilsException
  {
    _molfile = molfile;
    try {
      Document searchDocument = createSearchDocumentForMolfile(molfile);
      return getResultsForSearchDocument(searchDocument);
    }
    catch (NonAsciiException e) {
      reportError(e.getMessage());
      return null;
    }
  }

  public void reportError(String error)
  {
    log.error("Error for molfile '" + _molfile + "': " + error);
  }
  
  
  // private methods

  /**
   * Create and return a PUG search request XML document for the molfile string.
   * @param molfile the molfile string to create a PUG search request XML document for
   * @return the XML document for the SMILES string
   * @throws NonAsciiException 
   */
  private Document createSearchDocumentForMolfile(String molfile) throws NonAsciiException {
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
    element = createParentedElement(document, element, "PCT-QueryCompoundCS_query_file");
    createParentedTextNode(document, element, getAsciiHexadecimalForMolfile(molfile));
  
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
  
  private String getAsciiHexadecimalForMolfile(String molfile) throws NonAsciiException
  {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < molfile.length(); i ++) {
      char ch = molfile.charAt(i);
      int ascii = (int) ch;
      if ((ascii & 0xff00) != 0) {
        if (ch == '\n') {
          System.out.println("right");
        }
        throw new NonAsciiException("non-ascii character '" + ch + "' (" + ascii + ") in molfile at index " + i);
      }
      String asciiStr = Integer.toHexString(ascii).toUpperCase();
      if (asciiStr.length() == 1) {
        asciiStr = "0" + asciiStr;
      }
      stringBuilder.append(asciiStr);
    }
    return stringBuilder.toString();
  }
  
  private class NonAsciiException extends Exception
  {
    private static final long serialVersionUID = -1340094385751937975L;

    public NonAsciiException(String message)
    {
      super(message);
    }
  }
}

