// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.util.NullSafeComparator;

public class ScreensaverUserComparator<U extends ScreensaverUser> extends NullSafeComparator<U>
{
  public static <U extends ScreensaverUser> ScreensaverUserComparator<U> getInstance()
  {
    return new ScreensaverUserComparator<U>();
  }
  
  @Override
  protected int doCompare(U u1, U u2)
  {
    return u1.getFullNameLastFirst().compareTo(u2.getFullNameLastFirst());
  }
}

