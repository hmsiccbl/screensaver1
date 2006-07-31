// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

public class NCBIGeneInfo
{
  private String _geneName;
  private String _speciesName;
  public NCBIGeneInfo(String geneName, String speciesName)
  {
    _geneName = geneName;
    _speciesName = speciesName;
  }
  public String getGeneName()
  {
    return _geneName;
  }
  public String getSpeciesName()
  {
    return _speciesName;
  }
}
