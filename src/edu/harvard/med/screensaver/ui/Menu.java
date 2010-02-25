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
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.ui.libraries.LibraryDetailViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.searchresults.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenerSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.StaffSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.StudySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
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
  private StaffSearchResults _staffBrowser;
  private ScreenerSearchResults _screenersBrowser;
  private ActivitySearchResults _activitiesBrowser;
  private ScreenDetailViewer _screenDetailViewer;
  private UserViewer _userViewer;
  private WellSearchResults _wellsBrowser;
  private LibraryDetailViewer _libraryDetailViewer;

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
              ScreenerSearchResults screenersBrowser,
              StaffSearchResults staffBrowser,
              ActivitySearchResults activitiesBrowser,
              ScreenDetailViewer screenDetailViewer,
              UserViewer userViewer,
              LibraryDetailViewer libraryDetailViewer,
              WellSearchResults wellsBrowser)
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
    
    _libraryDetailViewer = libraryDetailViewer;
    _wellsBrowser = wellsBrowser;
  }

  // JSF application methods

  @UICommand
  public String viewMain()
  {
    return VIEW_MAIN;
  }

  @UICommand
  public String viewNews()
  {
    return VIEW_NEWS;
  }

  @UICommand
  public String viewDownloads()
  {
    return VIEW_DOWNLOADS;
  }

  @UICommand
  public String viewHelp()
  {
    return VIEW_HELP;
  }

  @UICommand
  public String logout()
  {
    log.info("logout for session "  + getHttpSession().getId());
    closeHttpSession();
    return VIEW_GOODBYE;
  }

  @UICommand
  public String findReagents()
  {
    return FIND_REAGENTS;
  }

  @UICommand
  public String findWells()
  {
    return FIND_WELLS;
  }
  
  @UICommand
  public String browseWells()
  {
    _wellsBrowser.searchAll();
    return BROWSE_WELLS;
  }

  @UICommand
  public String browseLibraries()
  {
    _librariesBrowser.searchAll();
    return BROWSE_LIBRARIES;
  }

  @UICommand
  public String browseRnaiLibraries()
  {
    _librariesBrowser.searchLibraryScreenType(ScreenType.RNAI);
    return BROWSE_LIBRARIES;
  }

  @UICommand
  public String browseSmallMoleculeLibraries()
  {
    _librariesBrowser.searchLibraryScreenType(ScreenType.SMALL_MOLECULE);
    return BROWSE_LIBRARIES;
  }

  @UICommand
  public String browseUsers()
  {
    if (!(getScreensaverUser() instanceof AdministratorUser)) {
      throw new OperationRestrictedException("only administrators can browse users");
    }
    _screenersBrowser.searchAll();
    return BROWSE_SCREENERS;
  }

  @UICommand
  public String browseAssociates()
  {
    if (!(getScreensaverUser() instanceof ScreeningRoomUser)) {
      throw new OperationRestrictedException("only screening room users can browser associates");
    }
    _screenersBrowser.searchAssociatedUsers((ScreeningRoomUser) getScreensaverUser());
    _screenersBrowser.setTitle(getMessage("screensaver.ui.users.UsersBrowser.title.searchScreenAssociates"));
    return BROWSE_SCREENERS;
  }

  @UICommand
  public String browseStaff()
  {
    _staffBrowser.searchAll();
    _staffBrowser.setTitle(getMessage("screensaver.ui.users.UsersBrowser.title.searchStaff"));
    return BROWSE_STAFF;
  }

  @UICommand
  public String browseStudies()
  {
    _studiesBrowser.searchAll();
    return BROWSE_STUDIES;
  }

  @UICommand
  public String browseScreens()
  {
    _screensBrowser.searchAll();
    // default to descending sort order on screen number
    _screensBrowser.getColumnManager().setSortAscending(false);
    return BROWSE_SCREENS;
  }

  @UICommand
  public String browseMyScreens()
  {
    if (!(getScreensaverUser() instanceof ScreeningRoomUser)) {
      throw new OperationRestrictedException("only screening room users can their own screens");
    }
    _screensBrowser.searchScreensForUser((ScreeningRoomUser) getScreensaverUser());
    return BROWSE_MY_SCREENS;
  }

  @UICommand
  public String browseCherryPickRequests()
  {
    _cherryPickRequestsBrowser.searchAll();
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UICommand
  public String browseRnaiCherryPickRequests()
  {
    _cherryPickRequestsBrowser.searchScreenType(ScreenType.RNAI);
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UICommand
  public String browseSmallMoleculeCherryPickRequests()
  {
    _cherryPickRequestsBrowser.searchScreenType(ScreenType.SMALL_MOLECULE);
    return BROWSE_CHERRY_PICK_REQUESTS;
  }

  @UICommand
  public String browseLabActivities()
  {
    ScreensaverUser user = getScreensaverUser();
    if (user instanceof AdministratorUser &&
      (user.isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN) ||
      user.isUserInRole(ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN))) {
      _activitiesBrowser.searchAll();
    }
    else {
      _activitiesBrowser.searchActivitiesForUser(user);
    }
    return BROWSE_ACTIVITIES;
  }

  @UICommand
  public String addLibrary()
  {
    return _libraryDetailViewer.editNewEntity(new Library((AdministratorUser) getScreensaverUser()));
  }
  
  @UICommand
  public String addScreen()
  {
    return _screenDetailViewer.editNewEntity(new Screen((AdministratorUser) getScreensaverUser()));
  }

  @UICommand
  public String addScreeningRoomUser()
  {
    return _userViewer.editNewEntity(new ScreeningRoomUser((AdministratorUser) getScreensaverUser()));
  }

  @UICommand
  public String addLabHead()
  {
    return _userViewer.editNewEntity(new LabHead((AdministratorUser) getScreensaverUser()));
  }
}
