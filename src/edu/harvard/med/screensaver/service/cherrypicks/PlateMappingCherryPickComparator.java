// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Comparator;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;

public class PlateMappingCherryPickComparator implements Comparator<LabCherryPick>
{
  private static PlateMappingCherryPickComparator _instance = new PlateMappingCherryPickComparator();
  
  public static PlateMappingCherryPickComparator getInstance()
  {
    return _instance;
  }

  public int compare(LabCherryPick cp1, LabCherryPick cp2)
  {
    Well well1 = cp1.getSourceWell();
    Well well2 = cp2.getSourceWell();
    int result = well1.getPlateNumber().compareTo(well2.getPlateNumber());
    if (result == 0) {
      result = cp1.getSourceCopy().getName().compareTo(cp2.getSourceCopy().getName());
    }
    // ordering by wells is not important w.r.t. plate mapping requirements, but
    // need to define a total ordering all cherry picks
    if (result == 0) {
      result = well1.getWellName().compareTo(well2.getWellName());
    }
    return result;
  }
}

