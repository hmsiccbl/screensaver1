// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import edu.harvard.med.screensaver.util.ComparableNullSafeComparator;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public class ScreensaverUserComparator<U extends ScreensaverUser> extends NullSafeComparator<U>
{
  private static final NullSafeComparator<Integer> NullSafeEntityIdComparator = new ComparableNullSafeComparator<Integer>();

  public static <U extends ScreensaverUser> ScreensaverUserComparator<U> getInstance()
  {
    return new ScreensaverUserComparator<U>();
  }
  
  @Override
  protected int doCompare(U u1, U u2)
  {
    int result = u1.getFullNameLastFirst().compareTo(u2.getFullNameLastFirst());
    if (result == 0) {
      if (u1.getEntityId() == null && u2.getEntityId() == null && u1 != u2) {
        throw new IllegalStateException("cannot determine ordering of ScreensaverUsers that are missing entity IDs");
      }
      result = NullSafeEntityIdComparator.compare(u1.getEntityId(), u2.getEntityId());
    }
    return result;
  }
}

