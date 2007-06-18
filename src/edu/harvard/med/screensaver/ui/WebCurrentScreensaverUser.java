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

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.authentication.ScreensaverLoginModule;

import org.apache.log4j.Logger;

/**
 * Like {@link CurrentScreensaverUser}, maintains the current ScreensaverUser
 * entity, but also knows how to find that current user within the context of a
 * web application (using servlet session information), in a lazy fashion.
 * Also, thanks to Spring 2.0's session-scoped bean feature, we can declare a
 * single bean of type WebCurrentScreensaverUser and inject it into other UI
 * Spring beans (which can be either session-scoped themselves, or even
 * singletons), and they will have access to the current ScreensaverUser (i.e.,
 * the user being serviced in the current HTTP request). We can't just inject a
 * ScreensaverUser instance directly, since a user hasn't necessarily
 * authenticated themselves when Spring session-scoped beans are instantiated.
 * 
 * @see WebDataAccessPolicy
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WebCurrentScreensaverUser extends CurrentScreensaverUser
{
  // static members

  private static Logger log = Logger.getLogger(WebCurrentScreensaverUser.class);


  // instance data members

  private GenericEntityDAO _dao;


  // public constructors and methods

  public void setDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  @Override
  public ScreensaverUser getScreensaverUser()
  {
    ScreensaverUser screensaverUser = super.getScreensaverUser();
    if (screensaverUser == null) {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction() 
        {
          FacesContext facesContext = FacesContext.getCurrentInstance();
          if (facesContext == null) {
            throw new IllegalStateException("cannot determine current screensaver user outside of JSF context");
          }
          ScreensaverUser screensaverUser = findScreensaverUserForUsername(facesContext.getExternalContext().getRemoteUser());
          setScreensaverUser(screensaverUser);
        }
      });
      // Spring instantiates this object as a session-scoped bean, the first time a JSF page is requested for a newly logged-in user
      logActivity("login");
    }
    return super.getScreensaverUser();
  }

  // must be called within a transaction
  // TODO: annotate as @Transactional(REQUIRED) once we move to annotated txns
  public void setScreensaverUser(final ScreensaverUser user)
  {
    if (user != null) {
      // semi-HACK: fetch relationships needed by data access policy
      _dao.need(user, "screensaverUserRoles");
      if (user instanceof ScreeningRoomUser) {
        _dao.need(user,
                  "hbnScreensLed",
                  "hbnScreensHeaded",
                  "hbnScreensCollaborated",
                  "hbnLabHead.hbnLabMembers",
                  "hbnLabMembers");
      }
    }
    super.setScreensaverUser(user);
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

  // TODO: make this into a service, and use in ScreensaverLoginModule
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
  private ScreensaverUser findScreensaverUserForUsername(String username)
  {
    if (username == null) {
      return null;
    }
    int switchToUserPos = username.indexOf(':');
    
    ScreensaverUser user = null;    
    if (switchToUserPos > 0) {
      username = username.substring(switchToUserPos + 1);
      user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                       "ECommonsId", 
                                       username.toLowerCase());
    }
    else {
      user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                       "loginId", 
                                       username);
      if (user == null) {
        user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                         "ECommonsId", 
                                         username.toLowerCase());
      }
    }
    if (user == null) {
      log.warn("could not find a user for username " + username);
    }
    return user;
  }
}
