// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// ScreeningRoomUser is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Comparator;

import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class ScreeningRoomUserByLabComparator implements Comparator<ScreeningRoomUser>
{
  private static ScreeningRoomUserByLabComparator _instance = new ScreeningRoomUserByLabComparator();
  
  public static ScreeningRoomUserByLabComparator getInstance()
  {
    return _instance;
  }
  
  public int compare(ScreeningRoomUser o1, ScreeningRoomUser o2)
  {
    int result = o1.getLabName().compareTo(o2.getLabName());
    if (result == 0) {
      return o1.getFullNameLastFirst().compareTo(o2.getFullNameLastFirst());
    }
    return result;
  }
}

