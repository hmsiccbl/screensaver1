// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
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
    if (i1.equals(i2)) {
      return 0;
    }
    int result = i1.getDatePerformed().compareTo(i2.getDatePerformed());
    if (result == 0) {
      // handle comparison of checklist item events that occurred on the same day
      result = i1.getDateCreated().compareTo(i2.getDateCreated());
    }
    if (result == 0) {
      result = Integer.valueOf(i1.hashCode()).compareTo(i2.hashCode());
    }
    return result;
  }
}