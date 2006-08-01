// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

/**
 * The required column for an RNAi library contents Excel spreadsheet.
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum RequiredRNAiLibraryColumn {
  
  PLATE("Plate"),
  WELL("Well"),
  VENDOR_IDENTIFIER("Vendor Identifier"),
  ENTREZGENE_SYMBOL("EntrezGene Symbol"),
  ENTREZGENE_ID("EntrezGene ID"),
  GENBANK_ACCESSION_NUMBER("GenBank Accession Number"),
  SEQUENCES("Sequences");
  
  private String _defaultColumnHeader;
  
  
  /**
   * Construct a new <code>RequiredRNAiLibraryColumn</code> object.
   * @param defaultColumnHeader the default column header
   */
  private RequiredRNAiLibraryColumn(String defaultColumnHeader)
  {
    _defaultColumnHeader = defaultColumnHeader;
  }
  
  /**
   * Return the default column header.
   * @return the default column header
   */
  public String getDefaultColumnHeader()
  {
    return _defaultColumnHeader;
  }
}
