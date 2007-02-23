//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.security.Principal;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.authentication.ScreensaverLoginModule;

import org.apache.log4j.Logger;

/**
 * Like {@link CurrentScreensaverUser}, maintains the current ScreensaverUser
 * entity, but also knows how to find that current user within the context of a
 * web application (using servlet session information). Also, thanks to Spring
 * 2.0's session-scoped bean feature, declare a single bean of type
 * WebCurrentScreensaverUser, inject into other beans (which can be either
 * session-scoped themselves, or even singletons), and they will have access to
 * the current ScreensaverUser (i.e., the user being serviced in the current
 * HTTP request).
 * 
 * @see WebDataAccessPolicy
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WebCurrentScreensaverUser extends CurrentScreensaverUser
{
  // static members

  private static Logger log = Logger.getLogger(WebCurrentScreensaverUser.class);


  // instance data members

  private DAO _dao;


  // public constructors and methods

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  @Override
  public ScreensaverUser getScreensaverUser()
  {
    ScreensaverUser screensaverUser = super.getScreensaverUser();
    if (screensaverUser == null) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext == null) {
        throw new IllegalStateException("cannot determine current screensaver user outside of JSF context");
      }
      screensaverUser = findScreensaverUserForPrincipal(facesContext.getExternalContext().getUserPrincipal());
      setScreensaverUser(screensaverUser);
      // Spring instantiates this object as a session-scoped bean, the first time a JSF page is requested for a newly logged-in user
      logActivity("login");
    }
    return screensaverUser;
  }
  
  @Override
  public void logActivity(String s)
  {
    super.logActivity(s);
  }
    
  public String toString()
  {
    String sessionId = ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getId();
    StringBuilder builder = new StringBuilder();
    builder.append("[...").append(sessionId.substring(sessionId.length() - 6)).append(']').append(' ');
    if (super.getScreensaverUser() == null) {
      builder.append("<unauthenticated>");
    } 
    else {
      builder.append(super.toString());
    }
    return builder.toString();
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
  private ScreensaverUser findScreensaverUserForPrincipal(final Principal principal)
  {
    if (principal == null) {
      return null;
    }
    final ScreensaverUser[] result = new ScreensaverUser[1];
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        String eCommonsIdOrLoginId = principal.getName();
        ScreensaverUser user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                                        "ECommonsId", 
                                                        eCommonsIdOrLoginId.toLowerCase());
        if (user == null) {
          user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                          "loginId", 
                                          eCommonsIdOrLoginId);
        }
        if (user != null) {
          // HACK: fetch relationships needed by data access policy
          _dao.need(user,
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
}
