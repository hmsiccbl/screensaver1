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
import java.io.IOException;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

/**
 * The JSF backing bean for the rnaiLibraryContentsImporter subview.
 * 
 * @author ant
 */
public class RNAiLibraryContentsImporterController extends AbstractController
{

  // static data
  
  private static final String ERRORS_XLS_FILE_EXTENSION = ".errors.xls";

  
  // instance data

  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private LibraryViewerController _libraryViewer;
  private UploadedFile _uploadedFile;


  // backing bean property getter and setter methods

  public RNAiLibraryContentsParser getRnaiLibraryContentsParser()
  {
    return _rnaiLibraryContentsParser;
  }

  public void setRnaiLibraryContentsParser(RNAiLibraryContentsParser rnaiLibraryContentsParser)
  {
    _rnaiLibraryContentsParser = rnaiLibraryContentsParser;
  }

  public LibraryViewerController getLibraryViewer()
  {
    return _libraryViewer;
  }

  public void setLibraryViewer(LibraryViewerController libraryViewer)
  {
    _libraryViewer = libraryViewer;
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
    return new ListDataModel(_rnaiLibraryContentsParser.getErrors());
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
      Library library = null;

      if (_uploadedFile.getInputStream().available() > 0) {
        library = _rnaiLibraryContentsParser.parseLibraryContents(
          library,
          new File(_uploadedFile.getName()),
          _uploadedFile.getInputStream());
      }

      if (library == null) {
        showMessage("badUploadedFile", "uploadRNAiLibraryContentsFile");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      else if (_rnaiLibraryContentsParser.getHasErrors()) {
        return ERROR_ACTION_RESULT;
      }
      else {
        _libraryViewer.setLibrary(library);
        return SUCCESS_ACTION_RESULT;
      }
    }
    catch (IOException e) {
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
      Map<Workbook,File> workbook2File = null;
      //TODO _rnaiLibraryContentsParser.outputErrorsInAnnotatedWorkbooks(null,
             //TODO                                             ERRORS_XLS_FILE_EXTENSION);
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
