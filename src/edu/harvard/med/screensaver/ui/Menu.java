// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;


import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.searchresults.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.StudySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.UserSearchResults;
import edu.harvard.med.screensaver.ui.users.UserViewer;

import org.apache.log4j.Logger;


public class Menu extends AbstractBackingBean
{

  // static data members

  private static Logger log = Logger.getLogger(Menu.class);

  // instance data members

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ScreenSearchResults _screensBrowser;
  private StudySearchResults _studiesBrowser;
  private CherryPickRequestSearchResults _cherryPickRequestsBrowser;
  private LibrarySearchResults _librariesBrowser;
  private UserSearchResults<AdministratorUser> _staffBrowser;
  private UserSearchResults<ScreeningRoomUser> _screenersBrowser;
  private ActivitySearchResults _activitiesBrowser;
  private ScreenDetailViewer _screenDetailViewer;
  private UserViewer _userViewer;


  // public methods

  /**
   * @motivation for CGLIB2
   */
  protected Menu()
  {
  }

  public Menu(GenericEntityDAO dao,
              LibrariesDAO librariesDao,
              ScreenSearchResults screensBrowser,
              StudySearchResults studiesBrowser,
              CherryPickRequestSearchResults cherryPickRequestsBrowser,
              LibrarySearchResults librariesBrowser,
              UserSearchResults<ScreeningRoomUser> screenersBrowser,
              UserSearchResults<AdministratorUser> staffBrowser,
              ActivitySearchResults activitiesBrowser,
              ScreenDetailViewer screenDetailViewer,
              UserViewer userViewer)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _screensBrowser = screensBrowser;
    _studiesBrowser = studiesBrowser;
    _cherryPickRequestsBrowser = cherryPickRequestsBrowser;
    _librariesBrowser = librariesBrowser;
    _staffBrowser = staffBrowser;
    _screenersBrowser = screenersBrowser;
    _activitiesBrowser = activitiesBrowser;
    _screenDetailViewer = screenDetailViewer;
    _userViewer = userViewer;
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
  public String findReagents()
  {
    return FIND_REAGENTS;
  }

  @UIControllerMethod
  public String findWells()
  {
    return FIND_WELLS;
  }

  @UIControllerMethod
  public String browseLibraries()
  {
    _librariesBrowser.searchLibraryScreenType(null);
    return BROWSE_LIBRARIES;
  }

  @UIControllerMethod
  public String browseRnaiLibraries()
  {
    _librariesBrowser.searchLibraryScreenType(ScreenType.RNAI);
    return BROWSE_LIBRARIES;
  }

  @UIControllerMethod
  public String browseSmallMoleculeLibraries()
  {
    _librariesBrowser.searchLibraryScreenType(ScreenType.SMALL_MOLECULE);
    return BROWSE_LIBRARIES;
  }

  @UIControllerMethod
  public String browseScreeners()
  {
    _screenersBrowser.searchUsers();
    return BROWSE_SCREENERS;
  }

  @UIControllerMethod
  public String browseStaff()
  {
    _staffBrowser.searchUsers();
    return BROWSE_STAFF;
  }

  @UIControllerMethod
  public String browseStudies()
  {
    _studiesBrowser.searchStudies();
    return BROWSE_STUDIES;
  }

  @UIControllerMethod
  public String browseScreens()
  {
    _screensBrowser.searchAllScreens();
    return BROWSE_SCREENS;
  }

  @UIControllerMethod
  public String browseMyScreens()
  {
    if (!(getScreensaverUser() instanceof ScreeningRoomUser)) {
      reportSystemError("invalid user type");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _screensBrowser.searchScreensForUser((ScreeningRoomUser) getScreensaverUser());
    return BROWSE_MY_SCREENS;
  }

  @UIControllerMethod
  public String browseCherryPickRequests()
  {
    if (getScreensaverUser() instanceof AdministratorUser &&
      (getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      getScreensaverUser().isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN))) {
      _cherryPickRequestsBrowser.searchAll();
    }
    else {
      showMessage("restrictedEntity", "all cherry pick requests");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UIControllerMethod
  public String browseRnaiCherryPickRequests()
  {
    _cherryPickRequestsBrowser.searchScreenType(ScreenType.RNAI);
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UIControllerMethod
  public String browseSmallMoleculeCherryPickRequests()
  {
    _cherryPickRequestsBrowser.searchScreenType(ScreenType.SMALL_MOLECULE);
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UIControllerMethod
  public String browseLabActivities()
  {
    ScreensaverUser user = getScreensaverUser();
    if (user instanceof AdministratorUser &&
      (user.isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN))) {
      _activitiesBrowser.searchAllActivities();
    }
    else {
      _activitiesBrowser.searchActivitiesForUser(user);
    }
    return BROWSE_ACTIVITIES;
  }
  
  @UIControllerMethod
  public String addScreen()
  {
    return _screenDetailViewer.editNewScreen(null, null);
  }
  
  @UIControllerMethod
  public String addScreeningRoomUser()
  {
    return _userViewer.editNewUser(new ScreeningRoomUser());
  }
  
  @UIControllerMethod
  public String addLabHead()
  {
    return _userViewer.editNewUser(new LabHead());
  }
  
  @Override
  public String reload()
  {
    return VIEW_MAIN;
  }
}
