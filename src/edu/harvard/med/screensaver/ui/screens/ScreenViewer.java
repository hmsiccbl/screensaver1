// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapViewer;
import edu.harvard.med.screensaver.ui.searchresults.ReagentSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class ScreenViewer extends StudyViewer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenViewer.class);


  // instance data members

  private ScreenViewer _thisProxy;
  private ScreenSearchResults _screensBrowser;
  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private ScreenDetailViewer _screenDetailViewer;
  private ScreenResultViewer _screenResultViewer;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;

  private Screen _screen;




  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenViewer()
  {
  }

  public ScreenViewer(ScreenViewer screenViewer,
                      GenericEntityDAO dao,
                      ScreenResultsDAO screenResultsDao,
                      ScreenDetailViewer screenDetailViewer,
                      ScreenSearchResults screensBrowser,
                      ReagentSearchResults reagentSearchResults,
                      ScreenResultViewer screenResultViewer,
                      HeatMapViewer heatMapViewer,
                      ScreenResultImporter screenResultImporter)
  {
    super(screenViewer, dao, screenDetailViewer, null, reagentSearchResults);
    _thisProxy = screenViewer;
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _screenDetailViewer = screenDetailViewer;
    _screenResultViewer = screenResultViewer;
    _heatMapViewer = heatMapViewer;
    _screenResultImporter = screenResultImporter;
    _screensBrowser = screensBrowser;
  }


  // public methods

  @Override
  public String reload()
  {
    if (_screen == null || _screen.getEntityId() == null) {
      _screen = null;
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewScreen(getScreen());
  }

  public Screen getScreen()
  {
    return _screen;
  }

  @Transactional
  public void setScreen(Screen screen)
  {
    log.debug("setScreen(): loading data for " + screen);
    screen = _dao.reloadEntity(screen,
                               true,
                               "labHead",
                               "labHead.labMembers",
                               "leadScreener");
    _dao.needReadOnly(screen, "billingInformation.billingItems");
    _dao.needReadOnly(screen, "collaborators.labHead");
    _dao.needReadOnly(screen, "labActivities.performedBy");
    _dao.needReadOnly(screen,
                      "attachedFiles",
                      "fundingSupports",
                      "keywords",
                      "publications");
    _dao.needReadOnly(screen, "statusItems");
    _dao.needReadOnly(screen, "cherryPickRequests.requestedBy");
    _dao.needReadOnly(screen, "annotationTypes.annotationValues");
    _dao.needReadOnly(screen.getScreenResult(), "plateNumbers");
    _dao.needReadOnly(screen.getScreenResult(),
                      "resultValueTypes.derivedTypes",
                      "resultValueTypes.typesDerivedFrom");
    _screen = screen;

    setStudy(screen);
    _screenDetailViewer.setScreen(screen);
    _screenResultImporter.setScreen(screen);
    ScreenResult screenResult = screen.getScreenResult();
    _heatMapViewer.setScreenResult(screenResult);
    _screenResultViewer.setScreenResult(screenResult);
    resetView();
  }


  /* JSF Application methods */

  @UIControllerMethod
  public String viewScreen()
  {
    Integer entityId = Integer.parseInt(getRequestParameter("entityId").toString());
    if (entityId == null) {
      throw new IllegalArgumentException("missing 'entityId' request parameter");
    }
    Screen screen = _dao.findEntityById(Screen.class, entityId);
    if (screen == null) {
      throw new IllegalArgumentException(Screen.class.getSimpleName() + " " + entityId + " does not exist");
    }
    return _thisProxy.viewScreen(screen);
  }

  @UIControllerMethod
  @Transactional
  public String viewScreen(Screen screen)
  {
    // TODO: implement as aspect
    if (screen.isRestricted()) {
      showMessage("restrictedEntity", "Screen " + screen.getScreenNumber());
      log.warn("user unauthorized to view " + screen);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    // calling viewScreen() is a request to view the most up-to-date, persistent
    // version of the screen, which means the screensBrowser must also be
    // updated to reflect the persistent version of the screen
    _screensBrowser.refetch();

    // all screens are viewed within the context of a search results, providing the user with screen search options at all times
    // screensBrowser will call our setScreen() method
    if (!_screensBrowser.viewEntity(screen)) {
      _screensBrowser.searchAllScreens();
      // note: calling viewEntity(screen) will only work as long as
      // ScreenSearchResults continues to use InMemoryDataTableModel
      _screensBrowser.viewEntity(screen);
    }
    return BROWSE_SCREENS;
  }

  // private methods

  private void resetView()
  {
  }
}

