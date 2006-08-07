// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

public class LoginController extends AbstractController
{
  
  private static final String AUTHENTICATION_ID_DESCRIPTION = "eCommons ID";
  
  private String _userId;
  private String _password;

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
  

  // JSF application methods
  
  public String login()
  {
    showMessage("invalidCredentials", null);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String forgotIdOrPassword()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

}
