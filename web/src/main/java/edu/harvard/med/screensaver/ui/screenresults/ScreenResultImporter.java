// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.IOException;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultLoader;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

/**
 * The JSF backing bean for the screenResultImporter view.
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
  private String _comments;
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

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
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
      ScreenResult screenResult = 
        _screenResultLoader.parseAndLoad(screen,
                                         new Workbook("Input Stream for screen: " + screen, _uploadedFile.getInputStream()),
                                         (AdministratorUser) getScreensaverUser(), 
                                         _comments,
                                         null,
                                         true);
      screenResult = _dao.reloadEntity(screenResult, true, ScreenResult.screen.to(AuditedAbstractEntity.updateActivities.castToSubtype(Screen.class)));
      showMessage("screens.screenResultDataLoaded", 
                  screenResult.getLastDataLoadingActivity().getComments());
      screen = _dao.reloadEntity(screen, true, Screen.assayPlates);
      int assayPlatesCreated = Sets.difference(screen.getAssayPlatesDataLoaded(), screen.getAssayPlatesScreened()).size();
      if (assayPlatesCreated > 0) {
        showMessage("screens.assayPlatesCreatedForLoadedData", assayPlatesCreated);
      }
    }
    catch (ParseErrorsException e) {
      log.info("parse errors encountered during import of ScreenResult for Screen " + screen);
      _lastParseErrors = e.getErrors();
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _screenViewer.viewEntity(screen);
  }

  @UICommand
  public String importScreenResultData()
  {
    return IMPORT_SCREEN_RESULT_DATA;
  }
}
