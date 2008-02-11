// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

/**
 * The parsed column for an RNAi library contents Excel spreadsheet.
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum ParsedRNAiLibraryColumn {
  
  PLATE("Plate"),
  WELL("Well"),
  VENDOR_IDENTIFIER("Vendor Identifier"),
  ENTREZGENE_SYMBOL("EntrezGene Symbol"),
  ENTREZGENE_ID("EntrezGene ID"),
  GENBANK_ACCESSION_NUMBER("GenBank Accession Number"),
  SEQUENCES("Sequences", false),
  OLD_ENTREZGENE_IDS("Old EntrezGene IDs", false);
  
  private String _defaultColumnHeader;
  private boolean _isRequired;
  
  /**
   * Construct a new, required <code>ParsedRNAiLibraryColumn</code> object.
   * @param defaultColumnHeader the default column header
   */
  private ParsedRNAiLibraryColumn(String defaultColumnHeader)
  {
    this(defaultColumnHeader, true);
  }

  /**
   * Construct a new <code>ParsedRNAiLibraryColumn</code> object.
   * @param defaultColumnHeader the default column header
   * @param isRequired true whenever this column header is required to parse the file
   */
  private ParsedRNAiLibraryColumn(String defaultColumnHeader, boolean isRequired)
  {
    _defaultColumnHeader = defaultColumnHeader;
    _isRequired = isRequired;
  }
  
  /**
   * Return the default column header.
   * @return the default column header
   */
  public String getDefaultColumnHeader()
  {
    return _defaultColumnHeader;
  }
  
  /**
   * Return true whenever this column header is required to parse the file.
   * @return true whenever this column header is required to parse the file
   */
  public boolean isRequired()
  {
    return _isRequired;
  }
}
