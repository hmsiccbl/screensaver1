// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 * The JSF backing bean for both the screenResultImporter subview and
 * screenResultImportErrors view.
 * 
 * @author ant
 */
public class ScreenResultImporterController extends AbstractController
{

  // static data
  
  private static final String ERRORS_XLS_FILE_EXTENSION = ".errors.xls";

  
  // instance data

  private ScreenResultParser _screenResultParser;
  private ScreenResultViewerController _screenResultViewer;
  private UploadedFile _uploadedFile;
  private Screen _screen;


  // backing bean property getter and setter methods

  public ScreenResultParser getScreenResultParser()
  {
    return _screenResultParser;
  }

  public void setScreenResultParser(ScreenResultParser screenResultParser)
  {
    _screenResultParser = screenResultParser;
  }

  public Screen getScreen()
  {
    return _screen;
  }

  public void setScreen(Screen screen)
  {
    _screen = screen;
  }

  public ScreenResultViewerController getScreenResultViewer()
  {
    return _screenResultViewer;
  }

  public void setScreenResultViewer(ScreenResultViewerController screenResultViewer)
  {
    _screenResultViewer = screenResultViewer;
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
  
  public String submit()
  {
    File tmpUploadedFile = null;
    try {
      if (_screen == null) {
        throw new IllegalStateException("no screen specified");
      }

      ScreenResult screenResult = null;

      if (_uploadedFile.getInputStream().available() > 0) {
        screenResult = _screenResultParser.parse(_screen, new File(_uploadedFile.getName()), _uploadedFile.getInputStream());
      }

      if (screenResult == null) {
        showMessage("badUploadedFile",
                   "uploadScreenResultFile");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      else if (_screenResultParser.getErrors().size() > 0) {
        return ERROR_ACTION_RESULT;
      }
      else {
        _screenResultViewer.setScreenResult(screenResult);
        return SUCCESS_ACTION_RESULT;
      }
    }
    catch (Exception e) {
      reportSystemError(e);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    finally {
      if (tmpUploadedFile != null) {
        tmpUploadedFile.delete();
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
