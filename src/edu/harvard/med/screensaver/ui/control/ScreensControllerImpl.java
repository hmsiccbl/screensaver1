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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.cherrypicks.CherryPickRequestExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
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
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolume;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.screenresults.HeatMapViewer;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screens.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.screens.ScreensBrowser;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.StringUtils;

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

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private LibrariesDAO _librariesDao;
  private CherryPickRequestDAO _cherryPickRequestDao;
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

  public void setGenericEntityDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public void setScreenResultsDao(ScreenResultsDAO screenResultsDao)
  {
    _screenResultsDao = screenResultsDao;
  }

  public void setLibrariesDao(LibrariesDAO libariesDao)
  {
    _librariesDao = libariesDao;
  }

  public void setCherryPickRequestDao(CherryPickRequestDAO cherryPickRequestDao)
  {
    _cherryPickRequestDao = cherryPickRequestDao;
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
        List<Screen> screens = _dao.findAllEntitiesOfType(Screen.class, 
                                                          true, 
                                                          "screenResult", 
                                                          "hbnLabHead", 
                                                          "hbnLeadScreener", 
                                                          "billingInformation",
                                                          "statusItems");
        for (Iterator iter = screens.iterator(); iter.hasNext();) {
          Screen screen = (Screen) iter.next();
          if (screen.isRestricted()) {
            iter.remove();
          }
        }
        _screensBrowser.setSearchResults(new ScreenSearchResults(screens,
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
                _dao.needReadOnly(screen, "screenResult");
              }
            }
            _screensBrowser.setSearchResults(new ScreenSearchResults(new ArrayList<Screen>(screens),
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
  public String viewScreen(final Screen screenIn, SearchResults<Screen> screenSearchResults)
  {
    // TODO: implement as aspect
    if (screenIn.isRestricted()) {
      showMessage("restrictedEntity", "Screen " + screenIn.getScreenNumber());
      logUserActivity("viewScreen " + screenIn + " (unauthorized)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    logUserActivity("viewScreen " + screenIn);
    _screenResultImporter.setScreenResultParser(new ScreenResultParser(_librariesDao));
    _screenResultViewer.setScreenResultExporter(_screenResultExporter);
    _screenResultViewer.setLibrariesController(_librariesController);
    _screensBrowser.setSearchResults(screenSearchResults);
    _heatMapViewer.setLibrariesController(_librariesController);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          Screen screen = _currentScreen = _dao.reloadEntity(screenIn,
                                                             true,
                                                             "hbnLabHead.hbnLabMembers",
                                                             "hbnLeadScreener",
                                                             "billingInformation");
          _dao.needReadOnly(screen, "hbnCollaborators.hbnLabAffiliation");
          _dao.needReadOnly(screen, "screeningRoomActivities");
          _dao.needReadOnly(screen, "abaseTestsets", "attachedFiles", "fundingSupports", "keywords", "lettersOfSupport", "publications");
          _dao.needReadOnly(screen, "statusItems");
          _dao.needReadOnly(screen, "cherryPickRequests");
          _dao.needReadOnly(screen, "hbnCollaborators");
          _dao.needReadOnly(screen.getScreenResult(), "plateNumbers");
          _dao.needReadOnly(screen.getScreenResult(), 
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
        _screenResultsDao.deleteScreenResult(screenResult);
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
        _cherryPickRequestDao.deleteCherryPickRequest(cherryPickRequest);
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
                      _screensBrowser.getSearchResults());
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
        showMessage("noSuchEntity", 
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
      CherryPickRequest cherryPickRequest = _cherryPickRequestDao.findCherryPickRequestByNumber(cherryPickRequestNumber);
      if (cherryPickRequest != null) {
        return viewCherryPickRequest(cherryPickRequest);
      }
      else {
        showMessage("noSuchEntity", 
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
          Screen screen = _dao.reloadEntity(screenIn); // TODO: this should be reattachEntity, since we're editing it
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
          ScreenResult screenResult = _dao.reloadEntity(screenResultIn, 
                                                        true,
                                                        "hbnResultValueTypes");
          // note: we eager fetch the resultValues for each ResultValueType
          // individually, since fetching all with a single needReadOnly() call
          // would generate an SQL result cross-product for all RVTs+RVs that
          // would include a considerable amount of redundant data
          // for the (denormalized) RVT fields
          for (ResultValueType rvt : screenResult.getResultValueTypes()) {
            // note: requesting the iterator generates an SQL statement that
            // only includes the result_value_type_result_values table, whereas
            // the needReadOnly() call's SQL statement joins to the
            // result_value_type table as well, which is slower
            rvt.getResultValues().keySet().iterator();
            //_dao.needReadOnly(rvt, "resultValues");
          }
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
    logUserActivity("downloadCherryPickRequestPlateMappingFiles " + cherryPickRequestIn);

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
    
    if (!screenIn.getScreenType().equals(ScreenType.RNAI)) {
      showMessage("applicationError", "Cherry Pick Requests can only be created for RNAi screens, currently");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

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
    // TODO: implement as aspect
    if (cherryPickRequestIn.isRestricted()) {
      showMessage("restrictedEntity", "Cherry Pick Request " + cherryPickRequestIn.getCherryPickRequestNumber());
      logUserActivity("viewCherryPickRequest " + cherryPickRequestIn + " (unauthorized)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    logUserActivity("viewCherryPickRequest " + cherryPickRequestIn);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn, 
                                                                  true, 
                                                                  "hbnRequestedBy",
                                                                  "screen.hbnLabHead", 
                                                                  "screen.hbnLeadScreener",
                                                                  "screen.hbnCollaborators");
          if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
            throw new UnsupportedOperationException("Sorry, but viewing compound cherry pick requests is not yet implemented.");
          }
          
          _dao.needReadOnly(cherryPickRequest, 
                            "cherryPickAssayPlates.hbnCherryPickLiquidTransfer",
                            "cherryPickAssayPlates.labCherryPicks.sourceWell");
          if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI)) {
            _dao.needReadOnly(cherryPickRequest,
                              "labCherryPicks.sourceWell.hbnSilencingReagents.gene.genbankAccessionNumbers");
            _dao.needReadOnly(cherryPickRequest,
                              "screenerCherryPicks.screenedWell.hbnSilencingReagents.gene.genbankAccessionNumbers",
                              "screenerCherryPicks.RNAiKnockdownConfirmation");
            _dao.needReadOnly(cherryPickRequest,
                              "screenerCherryPicks.labCherryPicks.wellVolumeAdjustments");
          }
//          else if (cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
//            // TODO: inflate, as needed
//            _dao.needReadOnly(cherryPickRequest,
//                              "screenerCherryPicks",
//                              "screenerCherryPicks.labCherryPicks");
//          }
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
                                       final Set<WellKey> cherryPickWellKeys)
  {
    logUserActivity("addCherryPicksForWells to " + cherryPickRequest);
    return doAddCherryPicksForWells(cherryPickRequest,
                                    cherryPickWellKeys,
                                    false);
  }

  @UIControllerMethod
  public String addCherryPicksForPoolWells(final CherryPickRequest cherryPickRequestIn,
                                           final Set<WellKey> cherryPickPoolWellKeys)
  {
    logUserActivity("addCherryPicksForPoolWells to " + cherryPickRequestIn);
    return doAddCherryPicksForWells(cherryPickRequestIn,
                                    cherryPickPoolWellKeys,
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
          CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn,
                                                                  false,
                                                                  "labCherryPicks.sourceWell");
          _dao.need(cherryPickRequest,
                    "screenerCherryPicks.screenedWell",
                    "screenerCherryPicks.RNAiKnockdownConfirmation");
          if (cherryPickRequest.isAllocated()) {
            throw new BusinessRuleViolationException("cherry picks cannot be deleted once a cherry pick request has been allocated");
          }
          Set<ScreenerCherryPick> cherryPicksToDelete = new HashSet<ScreenerCherryPick>(cherryPickRequest.getScreenerCherryPicks());
          for (ScreenerCherryPick cherryPick : cherryPicksToDelete) {
            _cherryPickRequestDao.deleteScreenerCherryPick(cherryPick);
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
  public String viewCherryPickRequestWellVolumes(CherryPickRequest cherryPickRequest,
                                                 boolean forUnfufilledLabCherryPicksOnly)
  {
    logUserActivity("viewCherryPickRequestWellVolumes for " + cherryPickRequest);
    
    Collection<WellCopyVolume> wellCopyVolumes = _librariesDao.findWellCopyVolumes(cherryPickRequest, 
                                                                                   forUnfufilledLabCherryPicksOnly);
    WellCopyVolumeSearchResults wellCopyVolumeSearchResults = new WellCopyVolumeSearchResults(wellCopyVolumes, 
                                                                                              _librariesController,
                                                                                              this,
                                                                                              _dao,
                                                                                              getMessages());
    return _librariesController.viewWellCopyVolumeSearchResults(wellCopyVolumeSearchResults);
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
      Set<LabCherryPick> unfulfillable = _cherryPickRequestAllocator.allocate(cherryPickRequestIn);
      if (unfulfillable.size() == cherryPickRequestIn.getLabCherryPicks().size()) {
        showMessage("cherryPicks.allCherryPicksUnfulfillable");
      }
      else if (unfulfillable.size() > 0) {
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
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    return viewCherryPickRequest(cherryPickRequestIn);
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
          newCherryPickRequest.setVolumeApprovedBy(cherryPickRequest.getVolumeApprovedBy());
          newCherryPickRequest.setDateVolumeApproved(cherryPickRequest.getDateVolumeApproved());
          newCherryPickRequest.setDateRequested(new Date());
          newCherryPickRequest.setRandomizedAssayPlateLayout(cherryPickRequest.isRandomizedAssayPlateLayout());
          newCherryPickRequest.setRequestedEmptyColumnsOnAssayPlate(new HashSet<Integer>(cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate()));
          newCherryPickRequest.setRequestedBy(cherryPickRequest.getRequestedBy());
          // note: we can only instantiate one new ScreenerCherryPick per *set*
          // of LabCherryPicks from the same screenedWell, otherwise we'll
          // (approriately) get a DuplicateEntityException
          for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
            ScreenerCherryPick newScreenerCherryPick = null;
            for (LabCherryPick labCherryPick : screenerCherryPick.getLabCherryPicks()) {
              if (!labCherryPick.isAllocated() && !labCherryPick.isCanceled()) {
                if (newScreenerCherryPick == null) {
                  newScreenerCherryPick = new ScreenerCherryPick(newCherryPickRequest,
                                                                 labCherryPick.getScreenerCherryPick().getScreenedWell());
                }
                new LabCherryPick(newScreenerCherryPick, labCherryPick.getSourceWell());
              }
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
                someCherryPicksUnfulfillable = true;
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
    }
    return viewCherryPickRequest(cherryPickRequestIn);
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
    _screensBrowser.setSearchResults(null);
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
                                          final Set<WellKey> cherryPickWellKeys,
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

          for (WellKey wellKey : cherryPickWellKeys) {
            Well well = _dao.findEntityById(Well.class,
                                            wellKey.toString(),
                                            true,
                                            // needed by libraryPoolToDuplexWellMapper, below
                                            "hbnSilencingReagents.hbnWells.hbnSilencingReagents.gene",
                                            "hbnSilencingReagents.gene");
            if (well == null) {
              throw new InvalidCherryPickWellException(wellKey, "no such well");
            } 
            else {
              ScreenerCherryPick screenerCherryPick = new ScreenerCherryPick(cherryPickRequest, well);
              if (!arePoolWells) {
                new LabCherryPick(screenerCherryPick, well);
              }
            }
          }
          
          if (arePoolWells) {
            _libraryPoolToDuplexWellMapper.createDuplexLabCherryPicksforPoolScreenerCherryPicks((RNAiCherryPickRequest) cherryPickRequest);
          }
          
          
        }
      });
      
      doWarnOnInvalidPoolWellScreenerCherryPicks(cherryPickRequestIn);
      doWarnOnDuplicateScreenerCherryPicks(cherryPickRequestIn);
      
      
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (InvalidCherryPickWellException e) {
      showMessage("cherryPicks.invalidWell", e.getWellKey());
    }
    catch (BusinessRuleViolationException e) {
      showMessage("businessError", e.getMessage());
    }
    return viewCherryPickRequest(cherryPickRequestIn);
  }

  private void doWarnOnInvalidPoolWellScreenerCherryPicks(CherryPickRequest cherryPickRequestIn)
  {
    int n = 0;
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequestIn.getScreenerCherryPicks()) {
      if (screenerCherryPick.getLabCherryPicks().size() == 0) {
        ++n;
      }
    }
    if (n > 0) {
      showMessage("cherryPicks.poolWellsWithoutDuplexWells", Integer.toString(n));
    }
  }

  private void doWarnOnDuplicateScreenerCherryPicks(final CherryPickRequest cherryPickRequestIn)
  {
    Map<WellKey,Number> duplicateScreenerCherryPickWellKeysMap = _cherryPickRequestDao.findDuplicateCherryPicksForScreen(cherryPickRequestIn.getScreen());
    Set<WellKey> duplicateScreenerCherryPickWellKeys = duplicateScreenerCherryPickWellKeysMap.keySet();
    Set<WellKey> ourScreenerCherryPickWellsKeys = new HashSet<WellKey>();
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequestIn.getScreenerCherryPicks()) {
      ourScreenerCherryPickWellsKeys.add(screenerCherryPick.getScreenedWell().getWellKey());
    }
    duplicateScreenerCherryPickWellKeys.retainAll(ourScreenerCherryPickWellsKeys);
    if (duplicateScreenerCherryPickWellKeysMap.size() > 0) {
      String duplicateWellsList = StringUtils.makeListString(duplicateScreenerCherryPickWellKeys, ", ");
      showMessage("cherryPicks.duplicateCherryPicksInScreen", cherryPickRequestIn.getScreen().getScreenNumber(), duplicateWellsList);
    }
  }
}

