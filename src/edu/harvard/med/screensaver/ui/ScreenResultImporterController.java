// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.io.screenresult.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.commons.io.IOUtils;
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


  // backing bean property getter and setter methods

  public ScreenResultParser getScreenResultParser()
  {
    return _screenResultParser;
  }

  public void setScreenResultParser(ScreenResultParser screenResultParser)
  {
    _screenResultParser = screenResultParser;
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
      ScreenResult screenResult = null;

      if (_uploadedFile.getInputStream().available() > 0) {
        tmpUploadedFile = File.createTempFile("screensaver.import.screenresult.", ".xls");
        IOUtils.copy(_uploadedFile.getInputStream(),
                     new FileOutputStream(tmpUploadedFile));
        screenResult = _screenResultParser.parse(tmpUploadedFile);
      }

      if (screenResult == null) {
        showMessage("badUploadedFile",
                   "uploadScreenResultFile");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      else if (_screenResultParser.getErrors().size() > 0) {
        return "screenresult-parse-errors";
      }
      else {
        _screenResultViewer.setScreenResult(screenResult);
        return SUCCESS_ACTION_RESULT;
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("IOException during parse of uploaded file: " + e.getMessage());
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
    try {
      Set<Workbook> workbooks = _screenResultParser.outputErrorsInAnnotatedWorkbooks(null,
                                                                                     ERRORS_XLS_FILE_EXTENSION);
      if (workbooks.size() != 1) {
        // TODO: output error message
        // should always be exactly 1, because we only accept all-in-one screen
        // result workbooks and we shouldn't be on this web page if no import
        // errors occurred
        return;
      }
      JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
                                             new File(workbooks.iterator().next().getWorkbookFile().getPath() + ERRORS_XLS_FILE_EXTENSION),
                                             Workbook.MIME_TYPE);
    }
    catch (IOException e) {
      e.printStackTrace();
      // TODO
    }
  }

}
