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

import javax.faces.event.ActionEvent;

import edu.harvard.med.screensaver.ui.libraries.LibrariesBrowserController;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem;

public class MenuController extends AbstractController
{
  
  // static data members
  
  private static Logger log = Logger.getLogger(MenuController.class);
  
  
  // instance fields
  
  private LibrariesBrowserController _librariesBrowser;
  
  
 
  // bean property methods
  
  /**
   * @return the librariesBrowser
   */
  public LibrariesBrowserController getLibrariesBrowser()
  {
    return _librariesBrowser;
  }

  /**
   * @param librariesBrowser the librariesBrowser
   */
  public void setLibrariesBrowser(LibrariesBrowserController librariesBrowser)
  {
    _librariesBrowser = librariesBrowser;
  }

  public String getUserPrincipalName()
  {
    Principal principal = getExternalContext().getUserPrincipal();
    if (principal == null) {
      return "";
    }
    return principal.getName();
  }
  
  public boolean isAuthenticatedUser()
  {
    return getExternalContext().getUserPrincipal() != null;
  }
  

  // JSF application methods

  /**
   * Logs out the user of the current session and redirects to the login page.
   */
  // TODO: should we move this to a more session-centric class? (maybe LoginController and then rename?)
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpAndDatabaseSessions();
    return LOGOUT_ACTION_RESULT;
  }
  
  public String goQuery()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String goMyScreens()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String goMyAccount()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String goHelp()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String goEditUser()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String goEditLibraries()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  

  // JSF event listener methods
  
  public void internalNodeEventListener(ActionEvent event)
  {
    HtmlCommandNavigationItem node = (HtmlCommandNavigationItem) event.getComponent();
//    boolean isOpen = node.isOpen();
//    node.setOpen(!isOpen);
//    node.toggleOpen();
    log.debug((node.isOpen() ? "expanded" : "collapsed") + " navigation node " + node.getId());
//    getFacesContext().renderResponse();
  }
  
}
