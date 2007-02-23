// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;

/**
 * Maintains the "current" ScreensaverUser entity and provides a single-method
 * logging facility for logging user activity. In the context of our web
 * application, the current user will be taken from the current HTTP session;
 * see {@link WebCurrentScreensaverUser}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CurrentScreensaverUser
{
  // static members

  private static Logger log = Logger.getLogger("userActivity");


  // instance data members
  
  private ScreensaverUser _user;


  // public constructors and methods
  
  public void setScreensaverUser(ScreensaverUser user)
  {
    _user = user;
  }

  public ScreensaverUser getScreensaverUser()
  {
    return _user;
  }
  
  public void logActivity(String s)
  {
    log.info(this + " " + s);
  }
  
  public String toString()
  {
    return _user.toString();
  }
}

