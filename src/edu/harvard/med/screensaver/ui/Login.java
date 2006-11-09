// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.security.Principal;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

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
  
  private static final String SCREENSAVER_USER_SESSION_ATTRIBUTE = "screensaverUser";

  public static Logger log = Logger.getLogger(Login.class);
  
  private static final String AUTHENTICATION_ID_DESCRIPTION = "User ID";
  
  private DAO _dao;
  
  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public String getAuthenticationIdDescription()
  {
    return AUTHENTICATION_ID_DESCRIPTION;
  }

  public ScreensaverUser getScreensaverUser()
  {
    Principal principal = getExternalContext().getUserPrincipal();
    if (principal == null) {
      return null;
    }
    String eCommonsIdOrLoginId = principal.getName();
    ScreensaverUser user = (ScreensaverUser) getHttpSession().getAttribute(SCREENSAVER_USER_SESSION_ATTRIBUTE);
    if (user == null) {
      user = _dao.findEntityByProperty(ScreensaverUser.class, "ECommonsId", eCommonsIdOrLoginId);
      if (user == null) {
        user = _dao.findEntityByProperty(ScreensaverUser.class, "loginId", eCommonsIdOrLoginId);
      }
    }
    getHttpSession().setAttribute(SCREENSAVER_USER_SESSION_ATTRIBUTE, user);
    return user;
  }


  // JSF application methods
  
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpAndDatabaseSessions();
    return LOGOUT_ACTION_RESULT;
  }

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
