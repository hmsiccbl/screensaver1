// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;


import org.apache.log4j.Logger;

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
import edu.harvard.med.screensaver.service.screens.ScreenGenerator;
import edu.harvard.med.screensaver.ui.activities.ActivitySearchResults;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.attachedFiles.AttachedFileSearchResults;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyPlateSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopySearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryDetailViewer;
import edu.harvard.med.screensaver.ui.libraries.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.screens.StudySearchResults;
import edu.harvard.med.screensaver.ui.users.ScreenerSearchResults;
import edu.harvard.med.screensaver.ui.users.StaffSearchResults;
import edu.harvard.med.screensaver.ui.users.UserViewer;


public class Menu extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(Menu.class);

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ScreenGenerator _screenGenerator;
  private ScreenSearchResults _screensBrowser;
  private StudySearchResults _studiesBrowser;
  private CherryPickRequestSearchResults _cherryPickRequestsBrowser;
  private LibrarySearchResults _librariesBrowser;
  private LibraryCopySearchResults _copiesBrowser;
  private LibraryCopyPlateSearchResults _libraryCopyPlatesBrowser;
  private StaffSearchResults _staffBrowser;
  private ScreenerSearchResults _screenersBrowser;
  private ActivitySearchResults _activitiesBrowser;
  private ScreenDetailViewer _screenDetailViewer;
  private UserViewer _userViewer;
  private WellSearchResults _wellsBrowser;
  private LibraryDetailViewer _libraryDetailViewer;
  private AttachedFileSearchResults _attachedFilesBrowser;

  /**
   * @motivation for CGLIB2
   */
  protected Menu()
  {
  }

  public Menu(GenericEntityDAO dao,
              LibrariesDAO librariesDao,
              ScreenGenerator screenGenerator,
              ScreenSearchResults screensBrowser,
              StudySearchResults studiesBrowser,
              CherryPickRequestSearchResults cherryPickRequestsBrowser,
              LibrarySearchResults librariesBrowser,
              LibraryCopySearchResults copiesBrowser,
              LibraryCopyPlateSearchResults libraryCopyPlatesBrowser,
              ScreenerSearchResults screenersBrowser,
              StaffSearchResults staffBrowser,
              ActivitySearchResults activitiesBrowser,
              ScreenDetailViewer screenDetailViewer,
              UserViewer userViewer,
              LibraryDetailViewer libraryDetailViewer,
              WellSearchResults wellsBrowser,
              AttachedFileSearchResults attachedFilesBrowser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _screenGenerator = screenGenerator;
    _screensBrowser = screensBrowser;
    _studiesBrowser = studiesBrowser;
    _cherryPickRequestsBrowser = cherryPickRequestsBrowser;
    _librariesBrowser = librariesBrowser;
    _copiesBrowser = copiesBrowser;
    _libraryCopyPlatesBrowser = libraryCopyPlatesBrowser;
    _staffBrowser = staffBrowser;
    _screenersBrowser = screenersBrowser;
    _activitiesBrowser = activitiesBrowser;
    _screenDetailViewer = screenDetailViewer;
    _userViewer = userViewer;
    _libraryDetailViewer = libraryDetailViewer;
    _wellsBrowser = wellsBrowser;
    _attachedFilesBrowser = attachedFilesBrowser;
  }

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
  public String browseLibraryCopies()
  {
    _copiesBrowser.searchAll();
    return BROWSE_LIBRARY_COPIES;
  }

  @UICommand
  public String browseLibraryCopyPlates()
  {
    _libraryCopyPlatesBrowser.searchAll();
    return BROWSE_LIBRARY_COPY_PLATES;
  }

  @UICommand
  public String browseScreeners()
  {
    if (!(getScreensaverUser() instanceof AdministratorUser)) {
      throw new OperationRestrictedException("only administrators can browse screeners");
    }
    _screenersBrowser.searchAll("Screeners");
    return BROWSE_SCREENERS;
  }

  @UICommand
  public String browseAssociates()
  {
    if (!(getScreensaverUser() instanceof ScreeningRoomUser)) {
      throw new OperationRestrictedException("only screening room users can browser associates");
    }
    _screenersBrowser.searchAssociatedUsers((ScreeningRoomUser) getScreensaverUser());
    return BROWSE_SCREENERS;
  }

  @UICommand
  public String browseStaff()
  {
    _staffBrowser.searchAll("Staff");
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
    return BROWSE_SCREENS;
  }

  @UICommand
  public String browseRnaiScreens()
  {
    return browseScreensOfScreenType(ScreenType.RNAI);
  }

  @UICommand
  public String browseSmallMoleculeScreens()
  {
    return browseScreensOfScreenType(ScreenType.SMALL_MOLECULE);
  }

  private String browseScreensOfScreenType(ScreenType screenType)
  {
    _screensBrowser.searchAll();
    TableColumn<Screen,ScreenType> column = (TableColumn<Screen,ScreenType>) _screensBrowser.getColumnManager().getColumn("Screen Type");
    column.clearCriteria();
    column.addCriterion(new Criterion<ScreenType>(Operator.EQUAL, screenType));
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
  public String browseAttachedFiles()
  {
    _attachedFilesBrowser.searchAll();
    return BROWSE_ATTACHED_FILES;
  }

  @UICommand
  public String browseLettersOfSupport()
  {
    _attachedFilesBrowser.searchForTypes(_attachedFilesBrowser.getLetterOfSupportAttachedFileTypes(),
                                         "Letters of Support");
    return BROWSE_ATTACHED_FILES;
  }

  @UICommand
  public String addLibrary()
  {
    return _libraryDetailViewer.editNewEntity(new Library((AdministratorUser) getScreensaverUser()));
  }
  
  @UICommand
  public String addScreen()
  {
    Screen screen = _screenGenerator.createPrimaryScreen((AdministratorUser) getScreensaverUser(), null, null);
    return _screenDetailViewer.editNewEntity(screen);
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
