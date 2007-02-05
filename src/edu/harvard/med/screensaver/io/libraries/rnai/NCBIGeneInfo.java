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
 * Encapsulates the information needed from NCBI for a {@link Gene}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class NCBIGeneInfo
{
  
  // private instance fields
  
  private String _geneName;
  private String _speciesName;
  private String _entrezgeneSymbol;
  

  // public constructor and instance methods
  
  /**
   * Construct a new <code>NCBIGeneInfo</code> object.
   * @param geneName the gene name
   * @param speciesName the species name
   */
  public NCBIGeneInfo(String geneName, String speciesName, String entrezgeneSymbol)
  {
    _geneName = geneName;
    _speciesName = speciesName;
    _entrezgeneSymbol = entrezgeneSymbol;
  }
  
  /**
   * Get the gene name.
   * @return the gene name
   */
  public String getGeneName()
  {
    return _geneName;
  }
  
  /**
   * Get the species name.
   * @return the species name
   */
  public String getSpeciesName()
  {
    return _speciesName;
  }
  
  public String getEntrezgeneSymbol()
  {
    return _entrezgeneSymbol;
  }
}
