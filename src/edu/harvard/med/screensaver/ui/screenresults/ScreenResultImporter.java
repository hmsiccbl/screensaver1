// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.io.IOException;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jxl.write.WritableWorkbook;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.springframework.dao.DataAccessException;

/**
 * The JSF backing bean for both the screenResultImporter subview and
 * screenResultImportErrors view.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenResultImporter extends AbstractBackingBean
{

  // static data

  private static final String ERRORS_XLS_FILE_EXTENSION = ".errors.xls";
  private static final Logger log = Logger.getLogger(ScreenResultImporter.class);


  // instance data

  private GenericEntityDAO _dao;
  private ScreenViewer _screenViewer;
  private ScreenSearchResults _screensBrowser;
  private ScreenResultParser _screenResultParser;

  private Screen _screen;
  private UploadedFile _uploadedFile;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultImporter()
  {
  }

  public ScreenResultImporter(GenericEntityDAO dao,
                              ScreenViewer screenViewer,
                              ScreenSearchResults screensBrowser,
                              ScreenResultParser screenResultParser)
  {
    _dao = dao;
    _screenViewer = screenViewer;
    _screensBrowser = screensBrowser;
    _screenResultParser = screenResultParser;
  }

  // backing bean property getter and setter methods

  public void setScreen(Screen screen)
  {
    _screen = screen;
  }

  public Screen getScreen()
  {
    return _screen;
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    _uploadedFile = uploadedFile;
  }

  public UploadedFile getUploadedFile()
  {
    return _uploadedFile;
  }

  public boolean getHasErrors()
  {
    return _screenResultParser.getHasErrors();
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_screenResultParser.getErrors());
  }


  // JSF application methods

  @UIControllerMethod
  public String cancel()
  {
    return _screenViewer.viewLastScreen();
  }

  @UIControllerMethod
  public String doImport()
  {
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Screen screen = _dao.reloadEntity(_screen); // TODO: this should be reattachEntity, since we're editing it
          log.info("starting import of ScreenResult for Screen " + screen);

          try {
            if (_uploadedFile.getInputStream().available() > 0) {
              _screenResultParser.parse(screen,
                           "screen_result_" + screen.getScreenNumber(),
                           _uploadedFile.getInputStream());
              if (_screenResultParser.getErrors().size() > 0) {
                // these are data-related "user" errors, so we log at "info" level
                log.info("parse errors encountered during import of ScreenResult for Screen " + _screen);
                throw new ScreenResultParseErrorsException("parse errors encountered");
              }
              else {
                log.info("successfully parsed ScreenResult for Screen " + _screen);
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
    _screensBrowser.invalidateSearchResult();
    return _screenViewer.viewLastScreen();
  }

  @UIControllerMethod
  public String viewScreenResultImportErrors()
  {
    return VIEW_SCREEN_RESULT_IMPORT_ERRORS;
  }


  // JSF event handlers

  public void downloadErrorAnnotatedWorkbookListener(ActionEvent event)
  {
    File errorAnnotatedWorkbookFile = null;
    try {
      WritableWorkbook errorAnnotatedWorkbook = _screenResultParser.getErrorAnnotatedWorkbook();
      errorAnnotatedWorkbookFile = File.createTempFile(_uploadedFile.getName(), ERRORS_XLS_FILE_EXTENSION);
      errorAnnotatedWorkbook.setOutputFile(errorAnnotatedWorkbookFile);
      errorAnnotatedWorkbook.write();
      errorAnnotatedWorkbook.close();
      JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
                                             errorAnnotatedWorkbookFile,
                                             Workbook.MIME_TYPE);
    }
    catch (Exception e) {
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
