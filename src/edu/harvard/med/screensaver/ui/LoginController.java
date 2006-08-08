// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import org.apache.log4j.Logger;

public class LoginController extends AbstractController
{
  
  public static Logger log = Logger.getLogger(LoginController.class);
  
  private static final String AUTHENTICATION_ID_DESCRIPTION = "eCommons ID";
  
  private String _userId;
  private String _password;
  private boolean _disableAdministrativePrivileges;

  public String getAuthenticationIdDescription()
  {
    return AUTHENTICATION_ID_DESCRIPTION;
  }

  public String getPassword()
  {
    return _password;
  }

  public void setPassword(String password)
  {
    _password = password;
  }

  public String getUserId()
  {
    return _userId;
  }

  public void setUserId(String userId)
  {
    _userId = userId;
  }
  
  public boolean isDisableAdministrativePrivileges()
  {
    return _disableAdministrativePrivileges;
  }

  public void setDisableAdministrativePrivileges(
    boolean disableAdministrativePrivileges)
  {
    _disableAdministrativePrivileges = disableAdministrativePrivileges;
  }


  // JSF application methods
  
  public String login()
  {
    log.info("login for session " + getHttpSession().getId());
    showMessage("invalidCredentials");
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String forgotIdOrPassword()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

}
