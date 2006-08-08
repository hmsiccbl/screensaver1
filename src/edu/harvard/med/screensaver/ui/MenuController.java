// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem;

public class MenuController extends AbstractController
{
  
  // static data members
  
  private static Logger log = Logger.getLogger(MenuController.class);
  

  // JSF application methods

  /**
   * Logs out the user of the current session and redirects to the login page.
   */
  // TODO: should we move this to a more session-centric class? (maybe LoginController and then rename?)
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    getHttpSession().invalidate();
    return LOGOUT_ACTION_RESULT;
  }
  
  public String goSearch()
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

  public String goImportScreenResult()
  {
    return GO_IMPORT_SCREEN_RESULT_ACTION_RESULT;
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
