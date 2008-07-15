// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import edu.harvard.med.screensaver.util.NullSafeComparator;

class ChecklistItemEventComparator extends NullSafeComparator<ChecklistItemEvent>
{
  @Override
  protected int doCompare(ChecklistItemEvent i1, ChecklistItemEvent i2)
  {
    int result = i1.getDatePerformed().compareTo(i2.getDatePerformed());
    if (result == 0) {
      // handle comparison of checklist item events that occurred on the same day
      result = i1.getEntryActivity().getDateCreated().compareTo(i2.getEntryActivity().getDateCreated());
    }
    return result;
  }
}