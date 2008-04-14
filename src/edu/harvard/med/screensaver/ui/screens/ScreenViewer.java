// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screenresults.heatmaps.HeatMapViewer;
import edu.harvard.med.screensaver.ui.searchresults.ReagentSearchResults;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

public class ScreenViewer extends StudyViewer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenViewer.class);


  // instance data members

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

  public ScreenViewer(GenericEntityDAO dao,
                      ScreenDetailViewer screenDetailViewer,
                      ReagentSearchResults reagentSearchResults,
                      ScreenResultsDAO screenResultsDao,
                      ScreenResultViewer screenResultViewer,
                      HeatMapViewer heatMapViewer,
                      ScreenResultImporter screenResultImporter)
  {
    super(dao, screenDetailViewer, null, reagentSearchResults);
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _screenDetailViewer = screenDetailViewer;
    _screenResultViewer = screenResultViewer;
    _heatMapViewer = heatMapViewer;
    _screenResultImporter = screenResultImporter;
  }


  // public methods

  public Screen getScreen()
  {
    return _screen;
  }

  public void setScreen(Screen screen)
  {
    setStudy(screen);
    _screen = screen;
    _screenDetailViewer.setScreen(screen);
    _screenResultImporter.setScreen(screen);
    ScreenResult screenResult = screen.getScreenResult();
    _heatMapViewer.setScreenResult(screenResult);
    _screenResultViewer.setScreenResult(screenResult);
    resetView();
  }
  

  /* JSF Application methods */

  @UIControllerMethod
  public String viewScreen(final Screen screenIn)
  {
    // TODO: implement as aspect
    if (screenIn.isRestricted()) {
      showMessage("restrictedEntity", "Screen " + screenIn.getScreenNumber());
      log.warn("user unauthorized to view " + screenIn);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(screenIn,
                                            true,
                                            "labHead",
                                            "labHead.labMembers",
                                            "leadScreener",
                                            "billingInformation");
          _dao.needReadOnly(screen, "collaborators.labHead");
          _dao.needReadOnly(screen, "labActivities.performedBy");
          _dao.needReadOnly(screen, "abaseTestsets", "attachedFiles", "fundingSupports", "keywords", "lettersOfSupport", "publications");
          _dao.needReadOnly(screen, "statusItems");
          _dao.needReadOnly(screen, "cherryPickRequests");
          _dao.needReadOnly(screen, "annotationTypes.annotationValues");
          _dao.needReadOnly(screen.getScreenResult(), "plateNumbers");
          _dao.needReadOnly(screen.getScreenResult(),
                            "resultValueTypes.derivedTypes",
                            "resultValueTypes.typesDerivedFrom");
          setScreen(screen);
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String viewLastScreen()
  {
    return viewScreen(getScreen());
  }


  // private methods

  private void resetView()
  {
  }
}

