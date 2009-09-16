//$HeadURL$
// $Id$
//
//Copyright 2006 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.cellhts2.CellHts2Annotator;
import edu.harvard.med.screensaver.ui.AbstractEditableBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.screenresults.cellhts2.CellHTS2Runner;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.EditableViewer;
import edu.harvard.med.screensaver.util.DeleteDir;

import org.apache.log4j.Logger;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@SuppressWarnings("unchecked")
public class ScreenResultViewer extends AbstractEditableBackingBean
{

  // static data members

  private static Logger log = Logger.getLogger(ScreenResultViewer.class);


  // instance data members

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private ScreenSearchResults _screensBrowser;
  private ScreenViewer _screenViewer;
  private EditableViewer _screenDetailViewer;
  private ResultValueTypesTable _resultValueTypesTable;
  private WellSearchResults _wellSearchResults;
  private CellHts2Annotator _cellHts2Annotator;

  private ScreenResult _screenResult;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private boolean _isWellSearchResultsInitialized;

  private String _cellHTS2ReportFilePath;
  private CellHTS2Runner _cellHTS2Runner;
  
  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultViewer()
  {
  }

  public ScreenResultViewer(GenericEntityDAO dao,
                            ScreenResultsDAO screenResultsDao,
                            ScreenSearchResults screensBrowser,
                            ScreenViewer screenViewer,
                            EditableViewer screenDetailViewer,
                            ResultValueTypesTable resultValueTypesTable,
                            WellSearchResults wellSearchResults,
                            CellHts2Annotator cellHts2Annotator,
                            CellHTS2Runner cellHTS2Runner)

  {
    super(ScreensaverUserRole.SCREEN_RESULTS_ADMIN);
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _screensBrowser = screensBrowser;
    _screenViewer = screenViewer;
    _screenDetailViewer = screenDetailViewer;
    _resultValueTypesTable = resultValueTypesTable;
    _wellSearchResults = wellSearchResults;
    _cellHts2Annotator = cellHts2Annotator;
    _cellHTS2Runner = cellHTS2Runner;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenResultSummary", false);
    _isPanelCollapsedMap.put("dataHeadersTable", true);
    _isPanelCollapsedMap.put("dataTable", true);
    _isPanelCollapsedMap.put("heatMaps", true);
  }


  // public methods

  public AbstractEntity getEntity()
  {
    return getScreenResult();
  }

  /**
   * @param screenResult can be null
   */
  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    setEditMode(false);

    // lazy initialization of _wellSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    _wellSearchResults.searchWellsForScreenResult(null);
    _isWellSearchResultsInitialized = false;

    // open viewer with dataTable panel closed, to avoid expense of initializing unless user explicitly requests to view it, while scrolling through multiple screen results
    _isPanelCollapsedMap.put("dataTable", true);
    _isPanelCollapsedMap.put("heatMaps", true);

    if (screenResult == null) {
      _resultValueTypesTable.initialize(Collections.<ResultValueType>emptyList());
    }
    else {
      _resultValueTypesTable.initialize(screenResult.getResultValueTypesList());
      _cellHTS2ReportFilePath = ScreensaverProperties.getProperty("cellHTS2report.filepath.base") + 
         ScreensaverProperties.getProperty("cellHTS2report.filepath.prefix") + 
         screenResult.getScreenResultId();
    }
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public Map<?,?> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  public ResultValueTypesTable getDataHeadersTable()
  {
    return _resultValueTypesTable;
  }

  public WellSearchResults getResultValueTable()
  {
      // lazy initialization of _wellSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    if (!_isWellSearchResultsInitialized && !_isPanelCollapsedMap.get("dataTable")) {
      _wellSearchResults.searchWellsForScreenResult(_screenResult);
      _isWellSearchResultsInitialized = true;
    }
    return _wellSearchResults;
  }


  // JSF application methods

  @UIControllerMethod
  public String delete()
  {
    if (_screenResult != null) {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          ScreenResult screenResult = _dao.reattachEntity(_screenResult);
          _screenResultsDao.deleteScreenResult(screenResult);
        }
      });
      
      // Delete directory and all files under it
      DeleteDir.deleteDirectory(new File(_cellHTS2ReportFilePath));
      log.info("Screen result file deleted");
      
      return _screenViewer.viewScreen(_screenResult.getScreen());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String saveScreenResult()
  {
    // note: saving the parent screen will save its screenResult; assumes
    // ScreenViewer and ScreenResultViewer are in sync (showing data for same
    // screen)
    return _screenDetailViewer.save();
  }

   public boolean isCellHTS2Enabled()
  {
     return Boolean.parseBoolean(getApplicationProperties().get(ScreensaverConstants.CELLHTS_ENABLED_PROPERTY).toString());
  }

  public boolean isCellHTS2ReportFileExists() 
  {
	  File file = new File(_cellHTS2ReportFilePath);
	  return file.exists();
  }
  
  public String getReportURL() {
	  String contextRoot = WEBAPP_ROOT.substring(WEBAPP_ROOT.lastIndexOf("/", WEBAPP_ROOT.length()-2), WEBAPP_ROOT.length());
	  return contextRoot + ScreensaverProperties.getProperty("cellHTS2report.filepath.prefix") + _screenResult.getScreenResultId()+"/index.html";
  }
  
  @UIControllerMethod
  public String viewCellHTS2Runner()
  {
	  return _cellHTS2Runner.viewCellHTS2Runner(_screenResult);
  }
  
  @UIControllerMethod
  public String cancel()
  {
    setEditMode(false);
    if (_screenResult.getEntityId() == null) {
      return VIEW_MAIN;
    }
    return _screenViewer.viewScreen(_screenResult.getScreen());
  }

  @UIControllerMethod
  public String edit()
  {
    setEditMode(true);
    return VIEW_SCREEN_RESULT_EDITOR;
  }

  @UIControllerMethod
  public String save()
  {
    setEditMode(false);

    _dao.reattachEntity(_screenResult.getScreen());
    _dao.flush();

    return _screenViewer.viewScreen(_screenResult.getScreen());
  }

}
