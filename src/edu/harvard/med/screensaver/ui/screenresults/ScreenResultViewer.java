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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultExporter;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.dao.DataAccessException;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@SuppressWarnings("unchecked")
public class ScreenResultViewer extends AbstractBackingBean
{

  // static data members

  private static Logger log = Logger.getLogger(ScreenResultViewer.class);

  public static final Integer DATA_TABLE_FILTER_SHOW_ALL = -2;
  public static final Integer DATA_TABLE_FILTER_SHOW_POSITIVES = -1;


  // instance data members

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private ScreenSearchResults _screensBrowser;
  private ScreenViewer _screenViewer;
  private ScreenDetailViewer _screenDetailViewer;
  private ResultValueTypesTable _resultValueTypesTable;
  private ScreenResultExporter _screenResultExporter;
  private WellSearchResults _wellSearchResults;

  private ScreenResult _screenResult;
  private Map<String,Boolean> _isPanelCollapsedMap;

  private UISelectOneBean<Integer> _dataFilter;
  private UISelectOneBean<ResultValueType> _showPositivesOnlyForDataHeader;



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
                            ScreenDetailViewer screenDetailViewer,
                            ResultValueTypesTable resultValueTypesTable,
                            ScreenResultExporter screenResultExporter,
                            WellSearchResults wellSearchResults)

  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _screensBrowser = screensBrowser;
    _screenViewer = screenViewer;
    _screenDetailViewer = screenDetailViewer;
    _resultValueTypesTable = resultValueTypesTable;
    _screenResultExporter = screenResultExporter;
    _wellSearchResults = wellSearchResults;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenResultSummary", false);
    _isPanelCollapsedMap.put("dataHeadersTable", true);
    _isPanelCollapsedMap.put("dataTable", true);
    _isPanelCollapsedMap.put("heatMaps", true);
  }


  // public methods

  /**
   * @param screenResult can be null
   */
  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    _dataFilter = null;
    _showPositivesOnlyForDataHeader = null;
    _wellSearchResults.searchWellsForScreenResult(screenResult);
    if (screenResult == null) {
      _resultValueTypesTable.initialize(Collections.<ResultValueType>emptyList());
    } 
    else {
      _resultValueTypesTable.initialize(screenResult.getResultValueTypesList());
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
    return _wellSearchResults;
  }


  // JSF application methods

  @UIControllerMethod
  public String download()
  {
    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = _dao.reloadEntity(_screenResult,
                                                        true,
          "resultValueTypes");
          // note: we eager fetch the resultValues for each ResultValueType
          // individually, since fetching all with a single needReadOnly() call
          // would generate an SQL result cross-product for all RVTs+RVs that
          // would include a considerable amount of redundant data
          // for the (denormalized) RVT fields
          for (ResultValueType rvt : screenResult.getResultValueTypes()) {
            _dao.needReadOnly(rvt, "resultValues");
          }
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

  @UIControllerMethod
  public String delete()
  {
    if (_screenResult != null) {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          ScreenResult screenResult = _dao.reattachEntity(_screenResult);
          _screenResultsDao.deleteScreenResult(screenResult);
          _screensBrowser.refetch();
        }
      });
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
    return _screenDetailViewer.saveScreen();
  }


  // result value data table filtering methods

  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.SCREEN_RESULTS_ADMIN;
  }


  // private methods

}
