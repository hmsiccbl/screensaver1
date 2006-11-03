// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.screenresults;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.view.screens.ScreenViewer;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 * The JSF backing bean for both the screenResultImporter subview and
 * screenResultImportErrors view.
 * 
 * @author ant
 */
public class ScreenResultImporter extends AbstractBackingBean
{

  // static data
  
  private static final String ERRORS_XLS_FILE_EXTENSION = ".errors.xls";
  private static final Logger log = Logger.getLogger(ScreenResultImporter.class);

  
  // instance data

  private DAO _dao;
  private ScreenViewer _screenViewer;
  private ScreenResultParser _screenResultParser;
  private ScreenResultViewer _screenResultViewer;
  private UploadedFile _uploadedFile;


  // backing bean property getter and setter methods

  public ScreenResultParser getScreenResultParser()
  {
    return _screenResultParser;
  }

  public void setScreenResultParser(ScreenResultParser screenResultParser)
  {
    _screenResultParser = screenResultParser;
  }

  public ScreenResultViewer getScreenResultViewer()
  {
    return _screenResultViewer;
  }

  public void setScreenResultViewer(ScreenResultViewer screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
  }

  public ScreenViewer getScreenViewer()
  {
    return _screenViewer;
  }

  public void setScreenViewer(ScreenViewer screenViewer)
  {
    _screenViewer = screenViewer;
  }

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    _uploadedFile = uploadedFile;
  }

  public UploadedFile getUploadedFile()
  {
    return _uploadedFile;
  }

  public DataModel getImportErrors()
  {
    // TODO: ultimately, we'll want to retrieve these errors from the database
    return new ListDataModel(_screenResultParser.getErrors());
  }


  // JSF application methods
  
  public String done()
  {
    return DONE_ACTION_RESULT;
  }
  
  // TODO: this method contains real business logic that should be moved to a non-ui package class; it also needs a unit test
  public String doImport()
  {
    boolean parseSuccessful = false;
    Screen screen = _screenViewer.getScreen();
    if (screen == null) {
      throw new IllegalStateException("screen viewer has not been initialized with a Screen");
    }
    ScreenResult existingScreenResult = screen.getScreenResult();
    try {
      log.info("starting import of ScreenResult for Screen " + screen);

      ScreenResult screenResult = null;
      if (_uploadedFile.getInputStream().available() > 0) {
        screenResult = _screenResultParser.parse(screen, new File(_uploadedFile.getName()), _uploadedFile.getInputStream());
      }

      if (screenResult == null) {
        // this is an unexpected, system error, so we log at "error" level
        log.error("fatal error during import of ScreenResult for Screen " + screen);
      }
      else if (_screenResultParser.getErrors().size() > 0) {
        // these are data-related "user" errors, so we log at "info" level
        log.info("parse errors encountered during import of ScreenResult for Screen " + screen);
      }
      else {
        _screenResultViewer.setScreenResult(screenResult);
        log.info("successfully imported " + screenResult + " for Screen " + screen);
        parseSuccessful = true;
      }
      if (parseSuccessful) {
        return SUCCESS_ACTION_RESULT;
      }
      else {
        return ERROR_ACTION_RESULT;
      }
    }
    catch (Exception e) {
      reportSystemError(e);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    finally {
      if (!parseSuccessful) {
        screen.setScreenResult(existingScreenResult);
      } 
      else if (existingScreenResult != null) {
        _dao.deleteEntity(existingScreenResult);
      }
    }
  }
  
  
  // JSF event handlers

  public void downloadErrorAnnotatedWorkbookListener(ActionEvent event)
  {
    File errorAnnotatedWorkbookFile = null;
    try {
      Map<Workbook,File> workbook2File = _screenResultParser.outputErrorsInAnnotatedWorkbooks(null,
                                                                                              ERRORS_XLS_FILE_EXTENSION);
      if (workbook2File.size() != 1) {
        reportSystemError("expected exactly 1 error-annotated workbook to be generated");
        return;
      }
      Workbook errorAnnotatedWorkbook = workbook2File.keySet().iterator().next();
      errorAnnotatedWorkbookFile = workbook2File.get(errorAnnotatedWorkbook);
      JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
                                             errorAnnotatedWorkbookFile,
                                             Workbook.MIME_TYPE);
    }
    catch (IOException e) {
      reportSystemError(e);
    }
    finally {
      if (errorAnnotatedWorkbookFile != null) {
        errorAnnotatedWorkbookFile.delete();
      }
    }
  }
  
}
