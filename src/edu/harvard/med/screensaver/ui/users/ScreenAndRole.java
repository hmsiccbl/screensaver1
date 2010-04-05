// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.Pair;

public class ScreenAndRole extends Pair<Screen,String> implements Comparable<ScreenAndRole>
{

  public ScreenAndRole(Screen screen, String role)
  {
    super(screen, role);
  }
  
  public Screen getScreen() { return getFirst(); }
  
  public String getRole() { return getSecond(); } 
  
  public LabActivity getLastLabActivity() 
  {
    if (getScreen().getLabActivities().size() > 0) { 
      return getScreen().getLabActivities().last();
    }
    return null;
  }

  public int compareTo(ScreenAndRole other)
  {
    return -1 * getScreen().getScreenNumber().compareTo(other.getScreen().getScreenNumber());
  }

}
