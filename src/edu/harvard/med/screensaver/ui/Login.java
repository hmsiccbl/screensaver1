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
import java.util.List;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.authentication.ScreensaverLoginModule;

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
  private DAO _unrestrictedAccessDao;
  
  // TODO: temporary code for conditionally controlling application's functionality for beta release
  private boolean _isUserAllowedAccessToScreens;


  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setUnrestrictedAccessDao(DAO dao)
  {
    _unrestrictedAccessDao = dao;
  }

  public String getAuthenticationIdDescription()
  {
    return AUTHENTICATION_ID_DESCRIPTION;
  }

  /**
   * Returns the ScreensaverUser that is logged in to the current HTTP session.
   * 
   * @return the ScreensaverUser that is logged in to the current HTTP session
   */
  public ScreensaverUser getScreensaverUser()
  {
    ScreensaverUser user = getSessionCachedScreensaverUser();
    if (user == null) {
      Principal principal = getExternalContext().getUserPrincipal();
      user = getScreensaverUserForPrincipal(principal);
                
      setSessionCachedScreensaverUser(user);

      // TODO: temporary code for conditionally controlling application's functionality for beta release
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction() 
        {
          List<ScreenResult> screenResults = _dao.findAllEntitiesWithType(ScreenResult.class);
          _isUserAllowedAccessToScreens = screenResults.size() > 0;
        }
      });
    }
    return user;
  }


  // JSF application methods
  
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpSession();
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
  
  // TODO: temporary code for conditionally controlling application's functionality for beta release
  public boolean isUserAllowedAccessToScreens()
  {
    return _isUserAllowedAccessToScreens;
  }
  
  
  // private methods
  
  /**
   * Returns a ScreensaverUser object for the specified Principal.
   * 
   * @motivation Normally, the ScreensaverUser instance would be the same object
   *             as the Principal instance, but
   *             <code>getExternalContext().getUserPrincipal()</code> does not
   *             return the ScreensaverUserPrincipal object that we provided to
   *             Tomcat during our JAAS authentication process (in
   *             {@link ScreensaverLoginModule#commit}, and so we cannot get at
   *             the ScreensaverUser object that would have available via the
   *             ScreensaverUserPrincipal object. So we have to requery the
   *             database to find the ScreensaverUser given only the user's
   *             login ID.
   * @return the ScreensaverUser that is logged in to the current HTTP session
   */
  private ScreensaverUser getScreensaverUserForPrincipal(final Principal principal)
  {
    if (principal == null) {
      return null;
    }
    final ScreensaverUser[] result = new ScreensaverUser[1];
    _unrestrictedAccessDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        String eCommonsIdOrLoginId = principal.getName();
        ScreensaverUser user = _unrestrictedAccessDao.findEntityByProperty(ScreensaverUser.class, "ECommonsId", eCommonsIdOrLoginId);
        if (user == null) {
          user = _unrestrictedAccessDao.findEntityByProperty(ScreensaverUser.class, "loginId", eCommonsIdOrLoginId);
        }
        if (user != null) {
          _unrestrictedAccessDao.need(user,
                                      "hbnScreensLed",
                                      "hbnScreensHeaded",
                                      "hbnScreensCollaborated",
                                      "hbnLabHead",
                                      "hbnLabHead.hbnLabMembers",
                                      "hbnLabMembers");
        }
        result[0] = user;
      }
    });
    return result[0];
  }
  
  private void setSessionCachedScreensaverUser(ScreensaverUser user)
  {
    getHttpSession().setAttribute(SCREENSAVER_USER_SESSION_ATTRIBUTE, user);
  }

  private ScreensaverUser getSessionCachedScreensaverUser()
  {
    ScreensaverUser user = (ScreensaverUser) getHttpSession().getAttribute(SCREENSAVER_USER_SESSION_ATTRIBUTE);
    return user;
  }
}
