// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.AttachedFile;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LetterOfSupport;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.ui.screenresults.HeatMapViewer;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultImporter;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenFinder;
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
public class ScreensController extends AbstractUIController
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(ScreensController.class);
  private static final String BROWSE_SCREENS = "browseScreens";
  private static final String VIEW_SCREEN = "viewScreen";
  private static String VIEW_SCREEN_RESULT_IMPORT_ERRORS = "viewScreenResultImportErrors";
  private static String VIEW_SCREEN_RESULT = "viewScreenResult";

  
  // private instance fields
  
  private DAO _dao;
  private LibrariesController _librariesController;
  private ScreensBrowser _screensBrowser;
  private ScreenViewer _screenViewer;
  private ScreenResultViewer _screenResultViewer;
  private ScreenFinder _screenFinder;
  private HeatMapViewer _heatMapViewer;
  private ScreenResultImporter _screenResultImporter;
  private ScreenResultExporter _screenResultExporter;
  private Screen _currentScreen;
  private ScreenSearchResults _currentScreenSearchResults;
  

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
    _screensBrowser.setScreensController(this);
  }
  
  public void setScreenViewer(ScreenViewer screenViewer)
  {
    _screenViewer = screenViewer;
    _screenViewer.setScreensController(this);
  }
  
  public void setScreenResultViewer(ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
    _screenResultViewer.setScreensController(this);
  }

  public void setScreenFinder(ScreenFinder screenFinder)
  {
    _screenFinder = screenFinder;
    _screenFinder.setScreensController(this);
  }

  public void setHeatMapViewer(HeatMapViewer heatMapViewer) 
  {
    _heatMapViewer = heatMapViewer;
  }

  public void setScreenResultImporter(ScreenResultImporter screenResultImporter) 
  {
    _screenResultImporter = screenResultImporter;
    _screenResultImporter.setScreensController(this);
  }

  public void setScreenResultExporter(ScreenResultExporter screenResultExporter) 
  {
    _screenResultExporter = screenResultExporter;
  }

 
  // public controller methods

  @UIControllerMethod
  public String browseScreens()
  {
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction()
      {
        if (_screensBrowser.getScreenSearchResults() == null) {
          List<Screen> screens = _dao.findAllEntitiesWithType(Screen.class);
          for (Screen screen : screens) {
            _dao.need(screen, "screenResult");
          }
          _screensBrowser.setScreenSearchResults(new ScreenSearchResults(screens, 
                                                                         ScreensController.this, 
                                                                         _dao));
        }
      }
    });
    return BROWSE_SCREENS;
  }
  
  @UIControllerMethod
  public String viewScreen(final Screen screenIn, ScreenSearchResults screenSearchResults)
  {
    _screenViewer.setDao(_dao);

    _screenResultImporter.setDao(_dao);
    _screenResultImporter.setMessages(getMessages());
    _screenResultImporter.setScreenResultParser(new ScreenResultParser(_dao));

    _screenResultViewer.setDao(_dao);
    _screenResultViewer.setMessages(getMessages());
    _screenResultViewer.setScreenResultExporter(_screenResultExporter);
    _screenResultViewer.setLibrariesController(_librariesController);
    _screenResultViewer.setScreenSearchResults(screenSearchResults);

    _heatMapViewer.setDao(_dao);
    _heatMapViewer.setLibrariesController(_librariesController);

    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          Screen screen = _currentScreen = (Screen) _dao.reloadEntity(screenIn);
          _dao.need(screen, 
                    "abaseTestsets",
                    "attachedFiles",
                    "billingInformation",
                    "fundingSupports",
                    "keywords",
                    "lettersOfSupport",
                    "publications",
                    "statusItems",
                    "visits",
                    "hbnCollaborators",
                    "hbnLabHead",
                    "hbnLabHead.hbnLabMembers",
                    "hbnLeadScreener",
                    "screenResult.plateNumbers",
                    "screenResult.hbnResultValueTypes",
                    "screenResult.hbnResultValueTypes.hbnDerivedTypes",
                    "screenResult.hbnResultValueTypes.hbnTypesDerivedFrom");
          
          ScreenResult permissionsAwareScreenResult = 
            _dao.findEntityById(ScreenResult.class, 
                                screen.getScreenResult() == null ? -1 : 
                                  screen.getScreenResult().getEntityId());

          _screenViewer.setScreen(screen);
          _screenResultImporter.setScreen(screen);
          _screenResultViewer.setScreen(screen);
          _heatMapViewer.setScreenResult(permissionsAwareScreenResult);
          _screenResultViewer.setScreenResult(permissionsAwareScreenResult);
          if (permissionsAwareScreenResult != null &&
            permissionsAwareScreenResult.getResultValueTypes().size() > 0) {
            _screenResultViewer.setScreenResultSize(permissionsAwareScreenResult.getResultValueTypesList().get(0).getResultValues().size());
          }
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
      
    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String editScreen(final Screen screen)
  {
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
    return viewLastScreen(); // reload
  }

  @UIControllerMethod
  public String saveScreen(final Screen screen, final DAOTransaction updater)
  {
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
    return VIEW_SCREEN;
  }

  @UIControllerMethod
  public String deleteScreenResult(ScreenResult screenResult)
  {
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
    return viewLastScreen();
  }

  
  @UIControllerMethod
  public String viewLastScreen()
  {
    return viewScreen(_currentScreen, _currentScreenSearchResults);
  }
    
  @UIControllerMethod
  public String viewScreenResultImportErrors()
  {
    return VIEW_SCREEN_RESULT_IMPORT_ERRORS;
  }

  @UIControllerMethod
  public String addStatusItem(Screen screen, StatusValue statusValue)
  {
    if (statusValue != null) {
      try {
        new StatusItem(screen,
                       new Date(),
                       statusValue);
      }
      catch (DuplicateEntityException e) {
        showMessage("screens.duplicateEntity", "status item");
      }
      _screenViewer.setNewStatusValue(null);
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteStatusItem(Screen screen, StatusItem statusItem)
  {
    screen.getStatusItems().remove(statusItem);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addPublication(Screen screen)
  {
    try {
      new Publication(screen, "<new>", "", "", "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "publication");
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deletePublication(Screen screen, Publication publication)
  {
    screen.getPublications().remove(publication);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addLetterOfSupport(Screen screen)
  {
    try {
      new LetterOfSupport(screen, new Date(), "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "letter of support");
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteLetterOfSupport(Screen screen, LetterOfSupport letterOfSupport)
  {
    screen.getLettersOfSupport().remove(letterOfSupport);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addAttachedFile(Screen screen)
  {
    try {
      new AttachedFile(screen, "<new>", "");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "attached file");
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteAttachedFile(Screen screen, AttachedFile attachedFile)
  {
    screen.getAttachedFiles().remove(attachedFile);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addFundingSupport(Screen screen, FundingSupport fundingSupport)
  {
    if (fundingSupport != null) {
      if (!screen.addFundingSupport(fundingSupport)) {
        showMessage("screens.duplicateEntity", "funding support");
      }
      _screenViewer.setNewFundingSupport(null);
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteFundingSupport(Screen screen, FundingSupport fundingSupport)
  {
    screen.getFundingSupports().remove(fundingSupport);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addAbaseTestset(Screen screen)
  {
    try {
      new AbaseTestset(screen, "<new>");
    }
    catch (DuplicateEntityException e) {
      showMessage("screens.duplicateEntity", "abase testset");
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteAbaseTestset(Screen screen, AbaseTestset abaseTestset)
  {
    screen.getAbaseTestsets().remove(abaseTestset);
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String addKeyword(Screen screen, String keyword)
  {
    if (! screen.addKeyword(keyword)) {
      showMessage("screens.duplicateEntity", "keyword");
    }
    else {
      _screenViewer.setNewKeyword("");
    }
    return VIEW_SCREEN;
  }
  
  @UIControllerMethod
  public String deleteKeyword(Screen screen, String keyword)
  {
    screen.getKeywords().remove(keyword);
    return VIEW_SCREEN;
  }

  public String findScreen(Integer screenNumber)
  {
    if (screenNumber != null) {
      Screen screen = _dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
      if (screen != null) {
        return viewScreen(screen, null);
      }
      else {
        showMessage("screens.noSuchScreenNumber", screenNumber);
      }
    }
    else {
      showMessage("screens.screenNumberRequired", screenNumber);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String importScreenResult(final Screen screenIn,
                                   final UploadedFile uploadedFile,
                                   final ScreenResultParser parser)
  {
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          Screen screen = (Screen) _dao.reloadEntity(screenIn);
          log.info("starting import of ScreenResult for Screen " + screen);

          try {
            if (uploadedFile.getInputStream().available() > 0) {
              parser.parse(screen, 
                           new File("screen_result_" + screen.getScreenNumber()),
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
      return viewLastScreen();
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
    }
    catch (ScreenResultParseErrorsException e) {
      return viewScreenResultImportErrors();
    }
    return viewLastScreen();
  }

  @UIControllerMethod
  public String downloadScreenResult(final ScreenResult screenResultIn)
  {
    try {
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          ScreenResult screenResult = (ScreenResult) _dao.reloadEntity(screenResultIn);
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


  
  // private methods

}

