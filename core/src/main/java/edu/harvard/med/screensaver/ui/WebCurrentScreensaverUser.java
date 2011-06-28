// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.auth.ScreensaverLoginModule;
import edu.harvard.med.screensaver.ui.arch.util.servlet.ScreensaverServletFilter;

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
 * @see EntityViewPolicy
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WebCurrentScreensaverUser extends CurrentScreensaverUser
{
  private static Logger log = Logger.getLogger(WebCurrentScreensaverUser.class);


  private GenericEntityDAO _dao;
  private boolean allowGuestLogin = false;

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
          
          Object temp = ((HttpSession)facesContext.getExternalContext().getSession(false)).getAttribute(ScreensaverServletFilter.LOGIN_PENDING);
          Boolean pendingLogin = temp != null && (Boolean)temp;
          String remoteUser = facesContext.getExternalContext().getRemoteUser();
          if(isAllowGuestLogin() && StringUtils.isEmpty(remoteUser) && !pendingLogin) {
            //remoteUser = "guest"; // for [#3107] Non-authenticated access for LINCS guest users
            setScreensaverUserGuest(new GuestUser("Guest"));
          }else {
            ScreensaverUser screensaverUser = findScreensaverUserForUsername(remoteUser);
            setScreensaverUser(screensaverUser);
            if( pendingLogin ) {
                 ((HttpSession)facesContext.getExternalContext().getSession(false)).setAttribute(ScreensaverServletFilter.LOGIN_PENDING,
                                    Boolean.FALSE);
            }
          }
          
        }
      });
      // Spring instantiates this object as a session-scoped bean, the first time a JSF page is requested for a newly logged-in user
      logActivity("login");
    }
    return super.getScreensaverUser();
  }
       
  private void setScreensaverUserGuest(ScreensaverUser user)
  {
    super.setScreensaverUser(user);
  }
  
  @Transactional(propagation = Propagation.MANDATORY)
  public void setScreensaverUser(final ScreensaverUser user)
  {
    if (user != null) {
      // semi-HACK: fetch relationships needed by data access policy
      _dao.need(user, ScreensaverUser.roles);
      if (user instanceof ScreeningRoomUser) {
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensLed);
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensCollaborated);
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.LabHead.to(LabHead.labMembers));
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensLed.to(Screen.leadScreener));
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensLed.to(Screen.labHead));
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensLed.to(Screen.collaborators));
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensCollaborated.to(Screen.leadScreener));
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensCollaborated.to(Screen.labHead));
        _dao.need((ScreeningRoomUser) user, ScreeningRoomUser.screensCollaborated.to(Screen.collaborators));
      }
      if (user instanceof LabHead) {
        _dao.need((LabHead) user, LabHead.screensHeaded);
        _dao.need((LabHead) user, LabHead.labMembers);
        _dao.need((LabHead) user, LabHead.screensHeaded.to(Screen.leadScreener));
        _dao.need((LabHead) user, LabHead.screensHeaded.to(Screen.labHead));
        _dao.need((LabHead) user, LabHead.screensHeaded.to(Screen.collaborators));
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
    builder.append("[").append(sessionId).append(']').append(' ');
    if (super.getScreensaverUser() == null) {
      builder.append("<unauthenticated>");
    }
    else {
      builder.append(super.toString());
    }
    return builder.toString();
  }

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
  
  public void setAllowGuestLogin(boolean allowGuestLogin)
  {
    this.allowGuestLogin = allowGuestLogin;
  }

  @Override
  public boolean isAllowGuestLogin()
  {
    return allowGuestLogin;
  }

  /**
    * for [#3107] Non-authenticated access for LINCS guest users
    */
  public class GuestUser extends ScreeningRoomUser
  {
    String name;
    public GuestUser(String name)
    {
      this.name = name;
    }
    @Override
    public String getFirstName()
    {
      return name;
    }
    @Override
    public String getLastName()
    {
      return "";
    }
    @Override
    public String getFullNameFirstLast()
    {
      return name;
    }
    @Override
    public Map<String,Boolean> getIsUserInRoleOfNameMap()
    {
      Map<String,Boolean> roles = Maps.newHashMap();
      roles.put(ScreensaverUserRole.SCREENSAVER_USER.getValue(), Boolean.TRUE);
      roles.put(ScreensaverUserRole.SCREENS_ADMIN.getValue(), true);
      return roles;
    }
    @Override
    protected boolean validateRole(ScreensaverUserRole role)
    {
      return ScreensaverUserRole.SCREENSAVER_USER == role || ScreensaverUserRole.SCREENS_ADMIN == role;
    }
    
    public Integer getEntityId() { return 0; }
    public String getLoginId() {return name; }
  };
}
