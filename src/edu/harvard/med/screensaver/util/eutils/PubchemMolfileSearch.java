// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/util/eutils/PubchemSmilesSearch.java $
// $Id: PubchemSmilesSearch.java 1723 2007-08-20 20:26:50Z ant4 $
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


  // silly little test
  public static void main(String [] args)
  {
    PubchemMolfileSearch pubchemMolfileSearch = new PubchemMolfileSearch();
    String molfile =
	"Structure942\n" +
	"csChFnd70/09300411422D\n" +
	"\n" +
	" 23 24  0  0  0  0  0  0  0  0999 V2000\n" +
	"    9.7074    2.0948    0.0000 N   0  0  3  0  0  0  0  0  0  0  0  0\n" +
	"   10.9224    1.4024    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"   12.1112    2.0948    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    9.7074    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"   10.9224    4.1520    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"   12.1112    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    1.2536    4.1243    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    2.4963    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    3.6846    1.4024    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    4.9004    2.0948    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    3.6846    4.1520    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    4.9004    3.4564    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    2.4963    2.1213    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"   10.9224    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    8.4916    4.2188    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    1.9086    5.3928    0.0000 F   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    0.5734    2.8831    0.0000 F   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    0.0000    4.8073    0.0000 F   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    6.0758    1.4024    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"   13.3537    4.1243    0.0000 Cl  0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    8.4916    1.3746    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    6.1273    4.1785    0.0000 Cl  0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"    7.2776    2.0948    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
	"  2  1  1  0  0  0  0\n" +
	"  3  2  1  0  0  0  0\n" +
	"  4  1  1  0  0  0  0\n" +
	"  5  4  1  0  0  0  0\n" +
	"  6  5  2  0  0  0  0\n" +
	"  7  8  1  0  0  0  0\n" +
	"  8 11  2  0  0  0  0\n" +
	"  9 10  1  0  0  0  0\n" +
	" 10 19  1  0  0  0  0\n" +
	" 11 12  1  0  0  0  0\n" +
	" 12 10  2  0  0  0  0\n" +
	" 13  9  2  0  0  0  0\n" +
	" 14  2  2  0  0  0  0\n" +
	" 15  4  2  0  0  0  0\n" +
	" 16  7  1  0  0  0  0\n" +
	" 17  7  1  0  0  0  0\n" +
	" 18  7  1  0  0  0  0\n" +
	" 19 23  1  0  0  0  0\n" +
	" 20  6  1  0  0  0  0\n" +
	" 21  1  1  0  0  0  0\n" +
	" 22 12  1  0  0  0  0\n" +
	" 23 21  1  0  0  0  0\n" +
	"  3  6  1  0  0  0  0\n" +
	"  8 13  1  0  0  0  0\n" +
	"M  END\n" +
	"\n";
    for (String pubchemCid : pubchemMolfileSearch.getPubchemCidsForMolfile(molfile)) {
      System.out.println("PubChem CID: " + pubchemCid);
    }
  }
  
  // private instance fields

  private String _molfile;

  
  // public constructors and methods

  /**
   * Return the list of PubChem CIDs for this molfile string, as reported by the PubChem PUG
   * interface, searching for exact match, with non-conflicting stereoisometry. Report an
   * error to the log and return null on error.
   * @param molfile the molfile to search for PubChem CIDs with
   * @return the list of PubChem CIDs for this molfile string. return null on error.
   */
  synchronized public List<String> getPubchemCidsForMolfile(String molfile)
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

