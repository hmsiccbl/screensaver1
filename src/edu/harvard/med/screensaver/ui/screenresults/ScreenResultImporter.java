// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.IOException;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultLoader;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

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
  private ScreenResultLoader _screenResultLoader;

  private UploadedFile _uploadedFile;
  private List<? extends ParseError> _lastParseErrors;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultImporter()
  {
  }

  public ScreenResultImporter(GenericEntityDAO dao,
                              ScreenViewer screenViewer,
                              ScreenResultLoader screenResultLoader)
  {
    _dao = dao;
    _screenViewer = screenViewer;
    _screenResultLoader = screenResultLoader;
  }

  public Screen getScreen()
  {
    return _screenViewer.getEntity();
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
    return _lastParseErrors != null && ! _lastParseErrors.isEmpty();
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_lastParseErrors);
  }


  // JSF application methods

  @UICommand
  public String cancel()
  {
    return _screenViewer.reload();
  }

  @UICommand
  public String doImport() throws IOException
  {
    Screen screen = _screenViewer.getEntity();
    try {
      _screenResultLoader.parseAndLoad(new Workbook("Input Stream for screen: " + screen, _uploadedFile.getInputStream()),
                                       null, 
                                       screen, 
                                       true);
    }
    catch (ParseErrorsException e)
    {
      log.info("parse errors encountered during import of ScreenResult for Screen " + screen);
      _lastParseErrors = e.getErrors();
      return viewScreenResultImportErrors();
    }
    return _screenViewer.viewEntity(screen);
  }

  @UICommand
  public String viewScreenResultImportErrors()
  {
    return VIEW_SCREEN_RESULT_IMPORT_ERRORS;
  }
}
