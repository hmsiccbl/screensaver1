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


/**
 * JSF backing bean for login view. Properties are not needed for userID and
 * password, as the form submission is actually sent to the special
 * j_security_check servlet (see login.jsp), and so this form never actually
 * processes a successful login.
 * 
 * @author ant
 */
public class Login extends AbstractBackingBean
{
  
  public static Logger log = Logger.getLogger(Login.class);
  
  private static final String AUTHENTICATION_ID_DESCRIPTION = "User ID";
  
  public String getAuthenticationIdDescription()
  {
    return AUTHENTICATION_ID_DESCRIPTION;
  }


  // JSF application methods
  
  public String forgotIdOrPassword()
  {
    // TODO: implement!
    // this will force another login attempt
    return LOGOUT_ACTION_RESULT;
  }

  public String tryAgain()
  {
    // this will force another login attempt
    return LOGOUT_ACTION_RESULT;
  }

}
