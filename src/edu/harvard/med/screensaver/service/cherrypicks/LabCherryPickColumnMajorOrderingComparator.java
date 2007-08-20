// $HeadURL$
// $Id$
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

public class LabCherryPickColumnMajorOrderingComparator implements Comparator<LabCherryPick>
{
  private static LabCherryPickColumnMajorOrderingComparator _instance;
  
  public static LabCherryPickColumnMajorOrderingComparator getInstance()
  {
    if (_instance == null) {
      _instance = new LabCherryPickColumnMajorOrderingComparator();
    }
    return _instance;
  }
  
  public int compare(LabCherryPick lcp1, LabCherryPick lcp2)
  {
    Well well1 = lcp1.getSourceWell();
    Well well2 = lcp2.getSourceWell();
    return 
    well1.getColumn() < well2.getColumn() ? -1 :
      well1.getColumn() > well2.getColumn() ? 1 :
        well1.getRow() < well2.getRow() ? -1 :
          well1.getRow() > well2.getRow() ? 1 : 0;
  }
}

