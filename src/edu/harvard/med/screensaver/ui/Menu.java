// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.control.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;

import org.apache.log4j.Logger;


public class Menu extends AbstractBackingBean
{

  // static data members

  private static Logger log = Logger.getLogger(Menu.class);

  // instance data members

  private GenericEntityDAO _dao;
  private ScreenSearchResults _screensBrowser;


  // public methods

  /**
   * @motivation for CGLIB2
   */
  protected Menu()
  {
  }

  public Menu(GenericEntityDAO dao,
              ScreenSearchResults screensBrowser)
  {
    _dao = dao;
    _screensBrowser = screensBrowser;
  }


  // JSF application methods

  @UIControllerMethod
  public String viewMain()
  {
    return VIEW_MAIN;
  }

  @UIControllerMethod
  public String viewNews()
  {
    return VIEW_NEWS;
  }

  @UIControllerMethod
  public String viewDownloads()
  {
    return VIEW_DOWNLOADS;
  }

  @UIControllerMethod
  public String viewHelp()
  {
    return VIEW_HELP;
  }

  @UIControllerMethod
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpSession();
    return VIEW_GOODBYE;
  }

  @UIControllerMethod
  public String browseScreens()
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        List<Screen> screens = _dao.findAllEntitiesOfType(Screen.class,
                                                          true,
                                                          "screenResult",
                                                          "hbnLabHead",
                                                          "hbnLeadScreener",
                                                          "billingInformation",
                                                          "statusItems");
        for (Iterator<Screen> iter = screens.iterator(); iter.hasNext();) {
          Screen screen = iter.next();
          if (screen.isRestricted()) {
            iter.remove();
          }
        }
        _screensBrowser.setContents(screens);
      }
    });
    return BROWSE_SCREENS;
  }

  @UIControllerMethod
  public String browseMyScreens()
  {
    final String[] result = { REDISPLAY_PAGE_ACTION_RESULT };
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Set<Screen> screens = new HashSet<Screen>();
        if (getScreensaverUser() instanceof ScreeningRoomUser) {
          ScreeningRoomUser screener = (ScreeningRoomUser) getScreensaverUser();
          screens.addAll(screener.getScreensHeaded());
          screens.addAll(screener.getScreensLed());
          screens.addAll(screener.getScreensCollaborated());
          if (screens.size() == 0) {
            showMessage("screens.noScreensForUser");
          }
          else {
            for (Iterator<Screen> iter = screens.iterator(); iter.hasNext();) {
              Screen screen = iter.next();
              // note: it would be odd if the data access policy restricted
              // access to the screens we've determined to be "my screens",
              // above, but we'll filter anyway, just to be safe.
              if (screen.isRestricted()) {
                iter.remove();
              }
              else {
                _dao.needReadOnly(screen, "screenResult");
              }
            }
            _screensBrowser.setContents(new ArrayList<Screen>(screens));
            result[0] = BROWSE_SCREENS;
          }
        }
        else {
          // admin user!
          showMessage("screens.noScreensForUser");
        }
      }
    });
    return result[0];
  }
}
