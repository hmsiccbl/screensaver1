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

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.ScreensController;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

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

  private GenericEntityDAO _dao;
  private Screen _screen;
  private ScreensController _screensController;
  private UploadedFile _uploadedFile;
  private ScreenResultParser _screenResultParser;


  // backing bean property getter and setter methods

  public void setDao(GenericEntityDAO dao) {
    _dao = dao;
  }

  public void setScreensController(ScreensController screensController) {
    _screensController = screensController;
  }

  public void setScreen(Screen screen)
  {
    _screen = screen;
  }
  
  public Screen getScreen()
  {
    return _screen;
  }

  public void setScreenResultParser(ScreenResultParser screenResultParser) {
    _screenResultParser = screenResultParser;
  }

  public ScreenResultParser getScreenResultParser() 
  {
    return _screenResultParser;
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
    return new ListDataModel(_screenResultParser.getErrors());
  }


  // JSF application methods
  
  public String cancel()
  {
    return _screensController.viewLastScreen();
  }
  
  // TODO: this method contains real business logic that should be moved to a
  // non-ui package class; it also needs a unit test
  public String doImport()
  {
    return _screensController.importScreenResult(_screen,
                                                 _uploadedFile,
                                                 _screenResultParser);
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
      e.printStackTrace();
      reportSystemError(e);
    }
    finally {
      if (errorAnnotatedWorkbookFile != null) {
        errorAnnotatedWorkbookFile.delete();
      }
    }
  }
  
}
