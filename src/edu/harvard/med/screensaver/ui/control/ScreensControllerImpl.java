//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.io.cherrypicks.CherryPickRequestExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.InvalidCherryPickWellException;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapFilesBuilder;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapper;
import edu.harvard.med.screensaver.service.libraries.rnai.LibraryPoolToDuplexWellMapper;
import edu.harvard.med.screensaver.ui.screenresults.HeatMapViewer;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.screens.ScreensBrowser;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreensControllerImpl extends AbstractUIController implements ScreensController
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ScreensController.class);
  private static final String BROWSE_SCREENS = "browseScreens";
  private static final String VIEW_SCREEN = "viewScreen";
  private static String VIEW_SCREEN_RESULT_IMPORT_ERRORS = "viewScreenResultImportErrors";


  // private instance fields

  private DAO _dao;
  private LibrariesController _librariesController;
  private ScreensBrowser _screensBrowser;
  private ScreenViewer _screenViewer;
  private ScreenResultViewer _screenResultViewer;
  private CherryPickRequestViewer _cherryPickRequestViewer;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;
  private ScreenResultExporter _screenResultExporter;
  private Screen _currentScreen;
  private CherryPickRequestAllocator _cherryPickRequestAllocator;
  private CherryPickRequestPlateMapper _cherryPickRequestPlateMapper;
  private CherryPickRequestPlateMapFilesBuilder _cherryPickRequestPlateMapFilesBuilder;
  private LibraryPoolToDuplexWellMapper _libraryPoolToDuplexWellMapper;
  private CherryPickRequestExporter _cherryPickRequestExporter;


  // public getters and setters

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }

  public ScreensBrowser getScreensBrowser()
  {
    return _screensBrowser;
  }

  public void setScreensBrowser(ScreensBrowser screensBrowser)
  {
    _screensBrowser = screensBrowser;
  }

  public void setScreenViewer(ScreenViewer screenViewer)
  {
    _screenViewer = screenViewer;
  }

  public void setScreenResultViewer(ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
  }

  public void setCherryPickRequestViewer(CherryPickRequestViewer cherryPickRequestViewer)
  {
    _cherryPickRequestViewer = cherryPickRequestViewer;
  }

  public void setHeatMapViewer(HeatMapViewer heatMapViewer) 
  {
    _heatMapViewer = heatMapViewer;
  }

  public void setScreenResultImporter(ScreenResultImporter screenResultImporter) 
  {
    _screenResultImporter = screenResultImporter;
  }

  public void setScreenResultExporter(ScreenResultExporter screenResultExporter) 
  {
    _screenResultExporter = screenResultExporter;
  }


  // public controller methods

  public void setCherryPickRequestAllocator(CherryPickRequestAllocator cherryPickRequestAllocator)
  {
    _cherryPickRequestAllocator = cherryPickRequestAllocator;
  }

  public void setCherryPickRequestPlateMapper(CherryPickRequestPlateMapper cherryPickRequestPlateMapper)
  {
    _cherryPickRequestPlateMapper = cherryPickRequestPlateMapper;
  }

  public void setCherryPickRequestPlateMapFilesBuilder(CherryPickRequestPlateMapFilesBuilder cherryPickRequestPlateMapFilesBuilder)
  {
    _cherryPickRequestPlateMapFilesBuilder = cherryPickRequestPlateMapFilesBuilder;
  }

  public void setLibraryPoolToDuplexWellMapper(LibraryPoolToDuplexWellMapper libraryPoolToDuplexWellMapper)
  {
    _libraryPoolToDuplexWellMapper = libraryPoolToDuplexWellMapper;
  }

  public void setCherryPickRequestExporter(CherryPickRequestExporter cherryPickRequestExporter)
  {
    _cherryPickRequestExporter = cherryPickRequestExporter;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#browseScreens()
   */
  @UIControllerMethod
  public String browseScreens()
  {
    logUserActivity("browseScreens");
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        List<Screen> screens = _dao.findAllEntitiesWithType(Screen.class);
        for (Iterator iter = screens.iterator(); iter.hasNext();) {
          Screen screen = (Screen) iter.next();
          // note: it would be odd if the data access policy restricted
          // access to the screens we've determined to be "my screens",
          // above, but we'll filter anyway, just to be safe.
          if (screen.isRestricted()) {
            iter.remove();
          }
          else {
            _dao.need(screen, 
                      "screenResult", 
                      // TODO: only need this for screensAdmin or
                      // readEverythingAdmin users; query would be faster if not
                      // requested
                      "statusItems"); 
          }
        }
        _screensBrowser.setScreenSearchResults(new ScreenSearchResults(screens,
                                                                       ScreensControllerImpl.this, 
                                                                       _dao));
      }
    });
    return BROWSE_SCREENS;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#browseMyScreens()
   */
  @UIControllerMethod
  public String browseMyScreens()
  {
    logUserActivity("browseMyScreens");
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
            for (Iterator iter = screens.iterator(); iter.hasNext();) {
              Screen screen = (Screen) iter.next();
              // note: it would be odd if the data access policy restricted
              // access to the screens we've determined to be "my screens",
              // above, but we'll filter anyway, just to be safe.
              if (screen.isRestricted()) {
                iter.remove();
              }
              else {
                _dao.need(screen, "screenResult");
              }
            }
            _screensBrowser.setScreenSearchResults(new ScreenSearchResults(new ArrayList<Screen>(screens),
                                                                           ScreensControllerImpl.this, 
                                                                           _dao));
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

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#viewScreen(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults)
   */
  @UIControllerMethod
  public String viewScreen(final Screen screenIn, ScreenSearchResults screenSearchResults)
  {
    logUserActivity("viewScreen " + screenIn);

    _screenViewer.setDao(_dao);

    _screenResultImporter.setDao(_dao);
    _screenResultImporter.setScreenResultParser(new ScreenResultParser(_dao));

    _screenResultViewer.setDao(_dao);
    _screenResultViewer.setScreenResultExporter(_screenResultExporter);
    _screenResultViewer.setLibrariesController(_librariesController);

    _screensBrowser.setScreenSearchResults(screenSearchResults);

    _heatMapViewer.setDao(_dao);
    _heatMapViewer.setLibrariesController(_librariesController);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          Screen screen = _currentScreen = _dao.reloadEntity(screenIn);
          // we call _dao.need() twice, since the method is not smart enough to know that the resulting SQL would be too horrendous
          _dao.need(screen, 
                    "abaseTestsets",
                    "attachedFiles",
                    "billingInformation",
                    "fundingSupports",
                    "keywords",
                    "lettersOfSupport",
                    "publications",
                    "statusItems",
                    "screeningRoomActivities",
                    "cherryPickRequests",
                    "hbnCollaborators",
                    "hbnLabHead",
                    "hbnLabHead.hbnLabMembers",
                    "hbnLeadScreener");
          _dao.need(screen.getScreenResult(),
                    "plateNumbers",
                    "hbnResultValueTypes",
                    "hbnResultValueTypes.hbnDerivedTypes",
                    "hbnResultValueTypes.hbnTypesDerivedFrom");

          _screenViewer.setScreen(screen);
          _screenResultImporter.setScreen(screen);
          ScreenResult screenResult = screen.getScreenResult();
          _heatMapViewer.setScreenResult(screenResult);
          _screenResultViewer.setScreenResult(screenResult);
          if (screenResult != null && screenResult.getResultValueTypes().size() > 0) {
            _screenResultViewer.setScreenResultSize(screenResult.getResultValueTypesList().get(0).getResultValues().size());
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#editScreen(edu.harvard.med.screensaver.model.screens.Screen)
   */
  @UIControllerMethod
  public String editScreen(final Screen screen)
  {
    logUserActivity("editScreen " + screen);
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          _dao.reattachEntity(screen); // checks if up-to-date
          _dao.need(screen, "hbnLabHead.hbnLabMembers");
        }
      });
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewLastScreen(); // on error, reload (and not in edit mode)
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#saveScreen(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.db.DAOTransaction)
   */
  @UIControllerMethod
  public String saveScreen(final Screen screen, final DAOTransaction updater)
  {
    logUserActivity("saveScreen " + screen);
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(screen);
          if (updater != null) {
            updater.runTransaction();
          }
        }
      });
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
      viewLastScreen();
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      viewLastScreen();
    }
    screenSearchResultsChanged();
    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String editCherryPickRequest(final CherryPickRequest cherryPickRequest)
  {
    logUserActivity("editCherryPickRequest " + cherryPickRequest);
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          _dao.reattachEntity(cherryPickRequest); // checks if up-to-date
          //_dao.need(cherryPickRequest, "");
        }
      });
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    // on error, reload
    return viewCherryPickRequest(cherryPickRequest);
  }

  @UIControllerMethod
  public String saveCherryPickRequest(final CherryPickRequest cherryPickRequest, final DAOTransaction updater)
  {
    logUserActivity("saveCherryPickRequest " + cherryPickRequest);
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          _dao.reattachEntity(cherryPickRequest);
          if (updater != null) {
            updater.runTransaction();
          }
        }
      });
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
      viewCherryPickRequest(cherryPickRequest);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      viewCherryPickRequest(cherryPickRequest);
    }
    return VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deleteScreenResult(edu.harvard.med.screensaver.model.screenresults.ScreenResult)
   */
  @UIControllerMethod
  public String deleteScreenResult(ScreenResult screenResult)
  {
    logUserActivity("deleteScreenResult " + screenResult);
    if (screenResult != null) {
      try {
        _dao.deleteScreenResult(screenResult);
      }
      catch (ConcurrencyFailureException e) {
        showMessage("concurrentModificationConflict");
      }
      catch (DataAccessException e) {
        showMessage("databaseOperationFailed", e.getMessage());
      }
    }
    screenSearchResultsChanged();
    return viewLastScreen();
  }

  @UIControllerMethod
  public String deleteCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    logUserActivity("deleteCherryPickRequest " + cherryPickRequest);
    if (cherryPickRequest != null) {
      try {
        _dao.deleteCherryPickRequest(cherryPickRequest);
      }
      catch (ConcurrencyFailureException e) {
        showMessage("concurrentModificationConflict");
      }
      catch (DataAccessException e) {
        showMessage("databaseOperationFailed", e.getMessage());
      }
    }
    return viewScreen(cherryPickRequest.getScreen(), null);
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#viewLastScreen()
   */
  @UIControllerMethod
  public String viewLastScreen()
  {
    logUserActivity("viewLastScreen " + _currentScreen);
    return viewScreen(_currentScreen, 
                      _screensBrowser.getScreenSearchResults());
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#viewScreenResultImportErrors()
   */
  @UIControllerMethod
  public String viewScreenResultImportErrors()
  {
    logUserActivity("viewScreenResultImportErrors");
    return VIEW_SCREEN_RESULT_IMPORT_ERRORS;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#addStatusItem(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.StatusValue)
   */
  @UIControllerMethod
  public String addStatusItem(Screen screen, StatusValue statusValue)
  {
    if (statusValue != null) {
      try {
        StatusItem statusItem = new StatusItem(screen,
                                               new Date(),
                                               statusValue);
        logUserActivity("addStatusItem " + screen + " " + statusItem);
      }
      catch (DuplicateEntityException e) {
        showMessage("screens.duplicateEntity", "status item");
      }
      _screenViewer.setNewStatusValue(null);
    }
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deleteStatusItem(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.StatusItem)
   */
  @UIControllerMethod
  public String deleteStatusItem(Screen screen, StatusItem statusItem)
  {
    logUserActivity("deleteStatusItem " + screen + " " + statusItem);
    screen.getStatusItems().remove(statusItem);
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#addPublication(edu.harvard.med.screensaver.model.screens.Screen)
   */
  @UIControllerMethod
  public String addPublication(Screen screen)
  {
    try {
      Publication publication = new Publication(screen, "<new>", "", "", "");
      logUserActivity("addPublication " + screen + " " + publication);
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "publication");
    }
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deletePublication(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.Publication)
   */
  @UIControllerMethod
  public String deletePublication(Screen screen, Publication publication)
  {
    logUserActivity("deletePublication " + screen + " " + publication);
    screen.getPublications().remove(publication);
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#addLetterOfSupport(edu.harvard.med.screensaver.model.screens.Screen)
   */
  @UIControllerMethod
  public String addLetterOfSupport(Screen screen)
  {
    try {
      LetterOfSupport letterOfSupport = new LetterOfSupport(screen, new Date(), "");
      logUserActivity("addLetterOfSupport " + screen + " " + letterOfSupport);

    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "letter of support");
    }
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deleteLetterOfSupport(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.LetterOfSupport)
   */
  @UIControllerMethod
  public String deleteLetterOfSupport(Screen screen, LetterOfSupport letterOfSupport)
  {
    logUserActivity("deleteLetterOfSupport " + screen + " " + letterOfSupport);
    screen.getLettersOfSupport().remove(letterOfSupport);
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#addAttachedFile(edu.harvard.med.screensaver.model.screens.Screen)
   */
  @UIControllerMethod
  public String addAttachedFile(Screen screen)
  {
    try {
      AttachedFile attachedFile = new AttachedFile(screen, "<new>", "");
      logUserActivity("addAttachedFile " + screen + " " + attachedFile);
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "attached file");
    }
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deleteAttachedFile(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.AttachedFile)
   */
  @UIControllerMethod
  public String deleteAttachedFile(Screen screen, AttachedFile attachedFile)
  {
    logUserActivity("deleteAttachedFile " + screen + " " + attachedFile);
    screen.getAttachedFiles().remove(attachedFile);
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#addFundingSupport(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.FundingSupport)
   */
  @UIControllerMethod
  public String addFundingSupport(Screen screen, FundingSupport fundingSupport)
  {
    logUserActivity("addFundingSupport " + screen + " " + fundingSupport);
    if (fundingSupport != null) {
      if (!screen.addFundingSupport(fundingSupport)) {
        showMessage("screens.duplicateEntity", "funding support");
      }
      _screenViewer.setNewFundingSupport(null);
    }
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deleteFundingSupport(edu.harvard.med.screensaver.model.screens.Screen, edu.harvard.med.screensaver.model.screens.FundingSupport)
   */
  @UIControllerMethod
  public String deleteFundingSupport(Screen screen, FundingSupport fundingSupport)
  {
    logUserActivity("deleteFundingSupport " + screen + " " + fundingSupport);
    screen.getFundingSupports().remove(fundingSupport);
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#addKeyword(edu.harvard.med.screensaver.model.screens.Screen, java.lang.String)
   */
  @UIControllerMethod
  public String addKeyword(Screen screen, String keyword)
  {
    logUserActivity("addKeyword " + screen + " " + keyword);
    if (! screen.addKeyword(keyword)) {
      showMessage("screens.duplicateEntity", "keyword");
    }
    else {
      _screenViewer.setNewKeyword("");
    }
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#deleteKeyword(edu.harvard.med.screensaver.model.screens.Screen, java.lang.String)
   */
  @UIControllerMethod
  public String deleteKeyword(Screen screen, String keyword)
  {
    logUserActivity("deleteKeyword " + screen + " " + keyword);
    screen.getKeywords().remove(keyword);
    return VIEW_SCREEN;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#findScreen(java.lang.Integer)
   */
  public String findScreen(Integer screenNumber)
  {
    logUserActivity("findScreen " + screenNumber);
    if (screenNumber != null) {
      Screen screen = _dao.findEntityByProperty(Screen.class, 
                                                "hbnScreenNumber", 
                                                screenNumber);
      if (screen != null) {
        return viewScreen(screen, null);
      }
      else {
        showMessage("screens.noSuchEntity", 
                    "Screen " + screenNumber);
      }
    }
    else {
      showMessage("screens.screenNumberRequired", screenNumber);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String findCherryPickRequest(Integer cherryPickRequestNumber)
  {
    logUserActivity("findCherryPickRequest " + cherryPickRequestNumber);
    if (cherryPickRequestNumber != null) {
      CherryPickRequest cherryPickRequest = _dao.findCherryPickRequestByNumber(cherryPickRequestNumber);
      if (cherryPickRequest != null) {
        return viewCherryPickRequest(cherryPickRequest);
      }
      else {
        showMessage("screens.noSuchEntity", 
                    "Cherry Pick Request " + cherryPickRequestNumber);
      }
    }
    else {
      showMessage("cherryPickRequests.cherryPickRequestNumberRequired", 
                  cherryPickRequestNumber);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#importScreenResult(edu.harvard.med.screensaver.model.screens.Screen, org.apache.myfaces.custom.fileupload.UploadedFile, edu.harvard.med.screensaver.io.screenresults.ScreenResultParser)
   */
  @UIControllerMethod
  public String importScreenResult(final Screen screenIn,
                                   final UploadedFile uploadedFile,
                                   final ScreenResultParser parser)
  {
    logUserActivity("importScreenResult " + screenIn + " " + uploadedFile.getName() + " " + uploadedFile.getSize());
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(screenIn);
          log.info("starting import of ScreenResult for Screen " + screen);

          try {
            if (uploadedFile.getInputStream().available() > 0) {
              parser.parse(screen, 
                           "screen_result_" + screen.getScreenNumber(),
                           uploadedFile.getInputStream());
              if (parser.getErrors().size() > 0) {
                // these are data-related "user" errors, so we log at "info" level
                log.info("parse errors encountered during import of ScreenResult for Screen " + screenIn);
                throw new ScreenResultParseErrorsException("parse errors encountered");
              }
              else {
                log.info("successfully parsed ScreenResult for Screen " + screenIn);
              }
            }
          }
          catch (IOException e) {
            showMessage("systemError", e.getMessage());
            throw new DAOTransactionRollbackException("could not access uploaded file", e);
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (ScreenResultParseErrorsException e) {
      return viewScreenResultImportErrors();
    }
    screenSearchResultsChanged();
    return viewLastScreen();
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.ScreensController#downloadScreenResult(edu.harvard.med.screensaver.model.screenresults.ScreenResult)
   */
  @UIControllerMethod
  public String downloadScreenResult(final ScreenResult screenResultIn)
  {
    logUserActivity("downloadScreenResult " + screenResultIn);
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          ScreenResult screenResult = _dao.reloadEntity(screenResultIn);
          File exportedWorkbookFile = null;
          FileOutputStream out = null;
          try {
            if (screenResult != null) {
              HSSFWorkbook workbook = _screenResultExporter.build(screenResult);
              exportedWorkbookFile = File.createTempFile("screenResult" + screenResult.getScreen().getScreenNumber() + ".", 
              ".xls");
              out = new FileOutputStream(exportedWorkbookFile);
              workbook.write(out);
              out.close();
              JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
                                                     exportedWorkbookFile,
                                                     Workbook.MIME_TYPE);
            }
          }
          catch (IOException e)
          {
            reportApplicationError(e);
          }
          finally {
            IOUtils.closeQuietly(out);
            if (exportedWorkbookFile != null && exportedWorkbookFile.exists()) {
              exportedWorkbookFile.delete();
            }
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String downloadCherryPickRequestPlateMappingFiles(final CherryPickRequest cherryPickRequestIn,
                                                           final Set<CherryPickAssayPlate> plateNames)
  {
    logUserActivity("downloadScreenResult " + cherryPickRequestIn);

    if (plateNames.size() == 0) {
      showMessage("cherryPicks.noPlatesSelected", "assayPlatesTable");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn);
          try {
            if (cherryPickRequest != null) {
              InputStream zipStream = _cherryPickRequestPlateMapFilesBuilder.buildZip(cherryPickRequest, plateNames);  
              JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                                 zipStream,
                                                 "CherryPickRequest" + cherryPickRequest.getEntityId() + "_PlateMapFiles.zip",
              "application/zip");
            }
          }
          catch (IOException e)
          {
            reportApplicationError(e);
          }
          finally {
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String downloadCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    if (cherryPickRequest instanceof RNAiCherryPickRequest) {
      try {
        jxl.Workbook workbook = _cherryPickRequestExporter.exportRNAiCherryPickRequest((RNAiCherryPickRequest) cherryPickRequest);
        JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                           Workbook2Utils.toInputStream(workbook),
                                           cherryPickRequest.getClass().getSimpleName() + "-" + cherryPickRequest.getCherryPickRequestNumber() + ".xls",
                                           Workbook.MIME_TYPE);
      }
      catch (Exception e) {
        reportSystemError(e);
      }
    }
    else {
      showMessage("systemError", "downloading of compound cherry pick requests is not yet implemented");
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String createCherryPickRequest(final Screen screenIn)
  {
    logUserActivity("createCherryPickRequest " + screenIn);

    final CherryPickRequest[] result = new CherryPickRequest[1];
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(screenIn);
          result[0] =  screen.createCherryPickRequest();
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(result[0]);
  }

  @UIControllerMethod
  public String viewCherryPickRequest(final CherryPickRequest cherryPickRequestIn)
  {
    logUserActivity("viewCherryPickRequest " + cherryPickRequestIn);

    _cherryPickRequestViewer.setDao(_dao);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn);
          if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
            throw new UnsupportedOperationException("Sorry, but viewing compound cherry pick requests is not yet implemented.");
          }
          
          _dao.need(cherryPickRequest, 
                    "screen",
                    "screen.hbnLabHead",
                    "screen.hbnLeadScreener",
                    "screen.hbnCollaborators",
                    "hbnRequestedBy",
                    "cherryPickAssayPlates",
                    "cherryPickAssayPlates.hbnCherryPickLiquidTransfer");
          if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI)) {
            _dao.need(cherryPickRequest,
                      "screenerCherryPicks",
                      "screenerCherryPicks.labCherryPicks",
                      "screenerCherryPicks.screenedWell",
                      "screenerCherryPicks.screenedWell.hbnSilencingReagents",
                      "screenerCherryPicks.screenedWell.hbnSilencingReagents.gene",
                      "screenerCherryPicks.screenedWell.hbnSilencingReagents.gene.genbankAccessionNumbers");
            _dao.need(cherryPickRequest,
                      "labCherryPicks",
                      "labCherryPicks.sourceWell",
                      "labCherryPicks.sourceWell.hbnSilencingReagents",
                      "labCherryPicks.sourceWell.hbnSilencingReagents.gene",
                      "labCherryPicks.sourceWell.hbnSilencingReagents.gene.genbankAccessionNumbers");
          }
          else if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
            _dao.need(cherryPickRequest,
                      "screenCherryPicks",
                      "screenerCherryPicks.labCherryPicks",
                      "screenCherryPicks.screenedWell",
                      "screenCherryPicks.screenedWell.hbnCompounds");
            _dao.need(cherryPickRequest,
                      "labCherryPicks",
                      "labCherryPicks.sourceWell",
                      "labCherryPicks.sourceWell.hbnCompound");
          }
          _cherryPickRequestViewer.setCherryPickRequest(cherryPickRequest);
        }
      });
      return VIEW_CHERRY_PICK_REQUEST_ACTION_RESULT;
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (UnsupportedOperationException e) {
      reportApplicationError(e);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String addCherryPicksForWells(CherryPickRequest cherryPickRequest,
                                       final Set<Well> cherryPickWells)
  {
    logUserActivity("addCherryPicksForWells to " + cherryPickRequest);
    return doAddCherryPicksForWells(cherryPickRequest,
                                    cherryPickWells,
                                    false);
  }

  @UIControllerMethod
  public String addCherryPicksForPoolWells(final CherryPickRequest cherryPickRequestIn,
                                           final Set<Well> cherryPickPoolWells)
  {
    logUserActivity("addCherryPicksForPoolWells to " + cherryPickRequestIn);
    return doAddCherryPicksForWells(cherryPickRequestIn,
                                    cherryPickPoolWells,
                                    true);
  }

  @UIControllerMethod
  public String deleteAllScreenerCherryPicks(final CherryPickRequest cherryPickRequestIn)
  {
    logUserActivity("deleteAllCherryPicks from " + cherryPickRequestIn);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn);
          if (cherryPickRequest.isAllocated()) {
            throw new BusinessRuleViolationException("cherry picks cannot be deleted once a cherry pick request has been allocated");
          }
          Set<ScreenerCherryPick> cherryPicksToDelete = new HashSet<ScreenerCherryPick>(cherryPickRequest.getScreenerCherryPicks());
          for (ScreenerCherryPick cherryPick : cherryPicksToDelete) {
            _dao.deleteScreenerCherryPick(cherryPick);
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }

    return viewCherryPickRequest(cherryPickRequestIn);
  }

  @UIControllerMethod
  public String allocateCherryPicks(final CherryPickRequest cherryPickRequestIn)
  {
    logUserActivity("allocateCherryPicks for " + cherryPickRequestIn);

    if (cherryPickRequestIn.getMicroliterTransferVolumePerWellApproved() == null) {
      showMessage("cherryPicks.approvedCherryPickVolumeRequired");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    try {
      _cherryPickRequestAllocator.allocate(cherryPickRequestIn);
      if (!cherryPickRequestIn.isAllocated()) {
        showMessage("cherryPicks.allCherryPicksUnfulfillable");
      }
      else if (cherryPickRequestIn.isOnlyPartiallyAllocated()) {
        showMessage("cherryPicks.someCherryPicksUnfulfillable");
      }
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(cherryPickRequestIn);
  }

  @UIControllerMethod
  public String deallocateCherryPicks(final CherryPickRequest cherryPickRequestIn)
  {
    logUserActivity("deallocateCherryPicks for " + cherryPickRequestIn);

    try {
      _cherryPickRequestAllocator.deallocate(cherryPickRequestIn);
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(cherryPickRequestIn);
  }

  @UIControllerMethod
  public String cancelAndDeallocateCherryPicksByPlate(CherryPickRequest cherryPickRequest,
                                                      Set<CherryPickAssayPlate> assayPlates,
                                                      ScreensaverUser performedBy,
                                                      Date dateOfLiquidTransfer,
                                                      String comments)
  {
    logUserActivity("deallocateCherryPicksByPlate for " + cherryPickRequest + 
                    " for plates " + assayPlates);

    try {
      _cherryPickRequestAllocator.cancelAndDeallocateAssayPlates(cherryPickRequest,
                                                                 assayPlates,
                                                                 performedBy,
                                                                 dateOfLiquidTransfer,
                                                                 comments);
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(cherryPickRequest);
  }

  @UIControllerMethod
  public String plateMapCherryPicks(final CherryPickRequest cherryPickRequestIn)
  {
    logUserActivity("plateMapCherryPicks for " + cherryPickRequestIn);

    try {
      _cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequestIn);
    }
    catch (ConcurrencyFailureException e) {
      showMessage("concurrentModificationConflict");
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(cherryPickRequestIn);
  }


  @UIControllerMethod
  public String recordLiquidTransferForAssayPlates(final CherryPickRequest cherryPickRequestIn, 
                                                   final Set<CherryPickAssayPlate> selectedAssayPlates,
                                                   final ScreensaverUser performedByIn,
                                                   final Date dateOfLiquidTransfer,
                                                   final String comments)
  {
    logUserActivity("recordLiquidTransferForAssayPlates for " + cherryPickRequestIn);

    try {
      doRecordLiquidTransferForAssayPlates(cherryPickRequestIn,
                                           selectedAssayPlates,
                                           performedByIn,
                                           dateOfLiquidTransfer,
                                           comments,
                                           true);
      return viewCherryPickRequest(cherryPickRequestIn);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String createNewCherryPickRequestForUnfulfilledCherryPicks(final CherryPickRequest cherryPickRequestIn)
  {
    logUserActivity("createNewCherryPickRequestForUnfulfilledCherryPicks for " + cherryPickRequestIn);

    final CherryPickRequest[] result = new CherryPickRequest[1];
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
          CherryPickRequest newCherryPickRequest = cherryPickRequest.getScreen().createCherryPickRequest();
          newCherryPickRequest.setComments("Created for unfulfilled cherry picks in Cherry Pick Request " + 
                                           cherryPickRequest.getCherryPickRequestNumber());
          // TODO: this might be better done in a copy constructor
          newCherryPickRequest.setMicroliterTransferVolumePerWellApproved(cherryPickRequest.getMicroliterTransferVolumePerWellApproved());
          newCherryPickRequest.setMicroliterTransferVolumePerWellRequested(cherryPickRequest.getMicroliterTransferVolumePerWellRequested());
          newCherryPickRequest.setDateRequested(new Date());
          newCherryPickRequest.setRandomizedAssayPlateLayout(cherryPickRequest.isRandomizedAssayPlateLayout());
          newCherryPickRequest.setRequestedEmptyColumnsOnAssayPlate(new HashSet<Integer>(cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate()));
          newCherryPickRequest.setRequestedBy(cherryPickRequest.getRequestedBy());
          for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
            if (!labCherryPick.isAllocated() && !labCherryPick.isCanceled()) {
              ScreenerCherryPick newScreenerCherryPick = new ScreenerCherryPick(newCherryPickRequest,
                                                                                labCherryPick.getScreenerCherryPick().getScreenedWell());
              new LabCherryPick(newScreenerCherryPick, labCherryPick.getSourceWell());
            }
          }
          _dao.persistEntity(newCherryPickRequest);
          result[0] = newCherryPickRequest;
        }
      });
      return viewCherryPickRequest(result[0]);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

  @UIControllerMethod
  public String recordFailureOfAssayPlates(final CherryPickRequest cherryPickRequestIn, 
                                           final Set<CherryPickAssayPlate> selectedAssayPlates,
                                           final ScreensaverUser performedByIn,
                                           final Date dateOfLiquidTransfer,
                                           final String comments)
  {
    // create new assay plates, duplicating plate name, lab cherry picks with same layout but new copy selection, incrementing attempt ordinal
    logUserActivity("recordFailureOfAssayPlates for " + selectedAssayPlates);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          doRecordLiquidTransferForAssayPlates(cherryPickRequestIn,
                                               selectedAssayPlates,
                                               performedByIn,
                                               dateOfLiquidTransfer,
                                               comments,
                                               false);

          boolean someCherryPicksUnfulfillable = false;
          for (CherryPickAssayPlate assayPlate : selectedAssayPlates) {
            _dao.reattachEntity(assayPlate);

            // Construct a CherryPickAssayPlate from an existing one, preserving the
            // plate ordinal and plate type, but incrementing the attempt ordinal. The new
            // assay plate will have a new set of lab cherry picks that duplicate the
            // original plate's lab cherry picks, preserving their original well layout,
            // and allocated anew.
            // TODO: protect against race condition (should enforce at schema level)
            CherryPickAssayPlate newAssayPlate = (CherryPickAssayPlate) assayPlate.clone();
            for (LabCherryPick labCherryPick : assayPlate.getLabCherryPicks()) {
              LabCherryPick newLabCherryPick = new LabCherryPick(labCherryPick.getScreenerCherryPick(),
                                                                 labCherryPick.getSourceWell());
              _dao.persistEntity(newLabCherryPick);
              if (!_cherryPickRequestAllocator.allocate(newLabCherryPick)) {
                someCherryPicksUnfulfillable  = true;
              } else {
                newLabCherryPick.setMapped(newAssayPlate,
                                           labCherryPick.getAssayPlateRow(),
                                           labCherryPick.getAssayPlateColumn());
              }
            }
          }
          if (someCherryPicksUnfulfillable) {
            showMessage("cherryPicks.someCherryPicksUnfulfillable");
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    if (selectedAssayPlates.size() == 0) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    else {
      return viewCherryPickRequest(selectedAssayPlates.iterator().next().getCherryPickRequest());
    }
  }


  // private methods

  /**
   * Poor man's "event" (we don't have a real app event architecture, currently) to
   * be invoked when the ScreenSearchResults may have become stale.
   */
  private void screenSearchResultsChanged()
  {
    // TODO: should attempt to maintain search result position, sort order,
    // etc.; right now, we just clear the search result, causing it be recreated
    // entirely when browseScreens() is called
    _screensBrowser.setScreenSearchResults(null);
  }

  private void doRecordLiquidTransferForAssayPlates(final CherryPickRequest cherryPickRequestIn, 
                                                    final Set<CherryPickAssayPlate> selectedAssayPlates,
                                                    final ScreensaverUser performedByIn,
                                                    final Date dateOfLiquidTransfer,
                                                    final String comments,
                                                    final boolean success)
  {
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
        ScreensaverUser performedBy = _dao.reloadEntity(performedByIn);
        CherryPickLiquidTransfer liquidTransfer = new CherryPickLiquidTransfer(performedBy,
                                                                               new Date(),
                                                                               dateOfLiquidTransfer,
                                                                               cherryPickRequest,
                                                                               success ? CherryPickLiquidTransferStatus.SUCCESSFUL : CherryPickLiquidTransferStatus.FAILED);
        liquidTransfer.setComments(comments);
        for (CherryPickAssayPlate assayPlate : selectedAssayPlates) {
          if (!assayPlate.getCherryPickRequest().equals(cherryPickRequest)) {
            throw new IllegalArgumentException("all assay plates must be from the specified cherry pick request");
          }
          if (assayPlate.isPlated()) {
            throw new BusinessRuleViolationException("cannot record successful liquid transfer more than once for a cherry pick assay plate");
          }
          assayPlate.setCherryPickLiquidTransfer(liquidTransfer);
        }
        _dao.persistEntity(liquidTransfer); // necessary?
      }
    });
  }
  
  private String doAddCherryPicksForWells(final CherryPickRequest cherryPickRequestIn,
                                          final Set<Well> cherryPickWells,
                                          final boolean arePoolWells)
  {
    assert !arePoolWells || cherryPickRequestIn.getScreen().getScreenType().equals(ScreenType.RNAI);
                                                                                   
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
          if (cherryPickRequest.isAllocated()) {
            throw new BusinessRuleViolationException("cherry picks cannot be added to a cherry pick request that has already been allocated");
          }

          for (Well well : cherryPickWells) {
            well = _dao.reloadEntity(well);
            ScreenerCherryPick screenerCherryPick = new ScreenerCherryPick(cherryPickRequest, well);
            if (!arePoolWells) {
              new LabCherryPick(screenerCherryPick, well);
            }
          }
          if (arePoolWells) {
            _libraryPoolToDuplexWellMapper.createDuplexLabCherryPicksforPoolScreenerCherryPicks((RNAiCherryPickRequest) cherryPickRequest);
          }
          
        }
      });
      return viewCherryPickRequest(cherryPickRequestIn);
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (BusinessRuleViolationException e) {
      showMessage("businessError", e.getMessage());
    }
    catch (InvalidCherryPickWellException e) {
      showMessage("cherryPicks.invalidWell", e.getWell());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
}

