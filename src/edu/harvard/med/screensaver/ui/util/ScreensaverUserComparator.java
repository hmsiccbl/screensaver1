// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Comparator;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public class ScreensaverUserComparator implements Comparator<ScreensaverUser>
{
  private static ScreensaverUserComparator _instance = new ScreensaverUserComparator();
  
  public static ScreensaverUserComparator getInstance()
  {
    return _instance;
  }
  
  public int compare(ScreensaverUser o1, ScreensaverUser o2)
  {
    return o1.generateFullName().compareTo(o2.generateFullName());
  }
}

