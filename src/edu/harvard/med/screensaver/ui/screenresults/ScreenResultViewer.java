//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;

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
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.dao.ConcurrencyFailureException;
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
public class ScreenResultViewer extends AbstractBackingBean implements Observer
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
  private ScreenResultExporter _screenResultExporter;
  private ScreenResultDataTable _screenResultDataTable;
  private FullScreenResultDataTable _fullScreenResultDataTable;
  private PositivesOnlyScreenResultDataTable _positivesOnlyScreenResultDataTable;
  private SinglePlateScreenResultDataTable _singlePlateScreenResultDataTable;

  private ScreenResult _screenResult;
  private Map<String,Boolean> _isPanelCollapsedMap;

  private ResultValueTypeTable _rvtTable;
  private UISelectOneBean<Integer> _dataFilter;
  private UISelectOneBean<ResultValueType> _showPositivesOnlyForDataHeader;
  private int _screenResultSize;


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
                            ScreenResultExporter screenResultExporter,
                            ResultValueTypeTable rvtTable,
                            FullScreenResultDataTable fullScreenResultDataTable,
                            PositivesOnlyScreenResultDataTable positivesOnlyScreenResultDataTable,
                            SinglePlateScreenResultDataTable singlePlateScreenResultDataTable)

  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _screensBrowser = screensBrowser;
    _screenViewer = screenViewer;
    _screenResultExporter = screenResultExporter;
    _rvtTable = rvtTable;
    _fullScreenResultDataTable = fullScreenResultDataTable;
    _positivesOnlyScreenResultDataTable = positivesOnlyScreenResultDataTable;
    _singlePlateScreenResultDataTable = singlePlateScreenResultDataTable;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("screenSummary", false);
    _isPanelCollapsedMap.put("annotations", true);
    _isPanelCollapsedMap.put("annotationTypes", true);
    _isPanelCollapsedMap.put("annotationValues", true);
    _isPanelCollapsedMap.put("screenResultSummary", false);
    _isPanelCollapsedMap.put("dataHeadersTable", true);
    _isPanelCollapsedMap.put("dataTable", true);
    _isPanelCollapsedMap.put("heatMaps", true);
  }


  // public methods

  public void setScreenResult(ScreenResult screenResult)
  {
    resetView();
    _screenResult = screenResult;
    getDataHeadersTable().initialize(getResultValueTypes(), this);
    updateDataTable();
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public Map<?,?> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  /**
   * @motivation Each of 3 backing bean objects need a reference to the
   *             UIComponent, but the active one will only be swapped in after
   *             JSF binds the reference (i.e. calls the
   *             DataTable.setRowsPerPageUIComponent() method), allowing only
   *             the previous active backing bean to have the correct reference
   *             to the UIComponent
   */
  public void setSharedRowsPerPageUIComponent(UIInput rowsPerPageUIComponent)
  {
    _fullScreenResultDataTable.setRowsPerPageUIComponent(rowsPerPageUIComponent);
    _positivesOnlyScreenResultDataTable.setRowsPerPageUIComponent(rowsPerPageUIComponent);
    _singlePlateScreenResultDataTable.setRowsPerPageUIComponent(rowsPerPageUIComponent);
  }

  public UIInput getSharedRowsPerPageUIComponent()
  {
    assert _fullScreenResultDataTable.getRowsPerPageUIComponent() == _positivesOnlyScreenResultDataTable.getRowsPerPageUIComponent() &&
    _fullScreenResultDataTable.getRowsPerPageUIComponent() == _singlePlateScreenResultDataTable.getRowsPerPageUIComponent();
    return _fullScreenResultDataTable.getRowsPerPageUIComponent();
  }

  /**
   * @motivation Each of 3 backing bean objects need a reference to the
   *             UIComponent, but the active one will only be swapped in after
   *             JSF binds the reference (i.e. calls the
   *             DataTable.setDataTableUIComponent() method), allowing only the
   *             previous active backing bean to have the correct reference to
   *             the UIComponent
   */
  public void setSharedDataTableUIComponent(UIData dataTableUIComponent)
  {
    _fullScreenResultDataTable.setDataTableUIComponent(dataTableUIComponent);
    _positivesOnlyScreenResultDataTable.setDataTableUIComponent(dataTableUIComponent);
    _singlePlateScreenResultDataTable.setDataTableUIComponent(dataTableUIComponent);
  }

  public UIData getSharedDataTableUIComponent()
  {
    assert _fullScreenResultDataTable.getDataTableUIComponent() == _positivesOnlyScreenResultDataTable.getDataTableUIComponent() &&
    _fullScreenResultDataTable.getDataTableUIComponent() == _singlePlateScreenResultDataTable.getDataTableUIComponent();
    return _fullScreenResultDataTable.getDataTableUIComponent();
  }

  public UISelectManyBean<ResultValueType> getDataHeaderSelections()
  {
    return getDataHeadersTable().getSelections();
  }

  public ResultValueTypeTable getDataHeadersTable()
  {
    return _rvtTable;
  }

  public ScreenResultDataTable getResultValueTable()
  {
    return _screenResultDataTable;
  }

  public void update(Observable observable, Object o)
  {
    // data header selections changed
    // TODO: make use of TableSortManager.getColumnModel().updateVisibleColumns(), instead of rebuilding data table backing bean wholesale
    updateDataTable();
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
          "hbnResultValueTypes");
          // note: we eager fetch the resultValues for each ResultValueType
          // individually, since fetching all with a single needReadOnly() call
          // would generate an SQL result cross-product for all RVTs+RVs that
          // would include a considerable amount of redundant data
          // for the (denormalized) RVT fields
          for (ResultValueType rvt : screenResult.getResultValueTypes()) {
            // note: requesting the iterator generates an SQL statement that
            // only includes the result_value_type_result_values table, whereas
            // the needReadOnly() call's SQL statement joins to the
            // result_value_type table as well, which is slower
            rvt.getResultValues().keySet().iterator();
            //_dao.needReadOnly(rvt, "resultValues");
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
      try {
        _screenResultsDao.deleteScreenResult(_screenResult);
        _screensBrowser.invalidateSearchResult();
        return _screenViewer.viewScreen(_screenResult.getScreen());
      }
      catch (ConcurrencyFailureException e) {
        showMessage("concurrentModificationConflict");
      }
      catch (DataAccessException e) {
        showMessage("databaseOperationFailed", e.getMessage());
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String saveScreenResult()
  {
    // note: saving the parent screen will save its screenResult; assumes
    // ScreenViewer and ScreenResultViewer are in sync (showing data for same
    // screen)
    return _screenViewer.saveScreen();
  }


  // result value data table filtering methods

  public UISelectOneBean<ResultValueType> getShowPositivesOnlyForDataHeader()
  {
    if (_showPositivesOnlyForDataHeader == null) {
      updateDataHeaderSelectionsForShowPositives();
    }
    return _showPositivesOnlyForDataHeader;
  }

  private void updateDataHeaderSelectionsForShowPositives()
  {
    log.debug("updating data header selections for show positives");
    List<ResultValueType> resultValueTypes = new ArrayList<ResultValueType>();
    resultValueTypes.addAll(getResultValueTypes());
    for (Iterator<ResultValueType> iter = resultValueTypes.iterator(); iter.hasNext();) {
      ResultValueType rvt = iter.next();
      if (!rvt.isPositiveIndicator()) {
        iter.remove();
      }
    }
    _showPositivesOnlyForDataHeader = new UISelectOneBean<ResultValueType>(resultValueTypes) {
      @Override
      protected String getLabel(ResultValueType t)
      {
        return t.getName();
      }
    };
  }

  // result value data table methods

  public UISelectOneBean<Integer> getDataFilter/*TODO: Selections*/()
  {
    if (_dataFilter == null) {
      lazyBuildDataFilterSelections();
    }
    return _dataFilter;
  }

  /**
   * Called when the set of rows to be displayed in the table needs to be changed (row filtering).
   */
  public void dataTableFilterListener(ValueChangeEvent event)
  {
    log.debug("dataTableFilter changed to " + event.getNewValue());
    getDataFilter().setValue((String) event.getNewValue());
    updateDataTable();
    getFacesContext().renderResponse();
  }


  /**
   * Called when the set of rows to be displayed in the table needs to be changed (row filtering).
   */
  public void showPositivesForDataHeaderListener(ValueChangeEvent event)
  {
    log.debug("showPositivesForDataHeader changed to " + event.getNewValue());
    getShowPositivesOnlyForDataHeader().setValue((String) event.getNewValue());
    updateDataTable();
    getFacesContext().renderResponse();
  }

  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.SCREEN_RESULTS_ADMIN;
  }


  // private methods

  /**
   * Get ResultValueTypes set, safely, handling case that no screen result
   * exists.
   */
  private List<ResultValueType> getResultValueTypes()
  {
    List<ResultValueType> rvts = new ArrayList<ResultValueType>();
    if (getScreenResult() != null) {
      rvts.addAll(getScreenResult().getResultValueTypes());
    }
    return rvts;
  }

  private void updateDataTable()
  {
    log.debug("updating data table content");
    if (_screenResult == null) {
      _screenResultDataTable = new EmptyScreenResultDataTable();
    }
    else if (getDataFilter().getSelection().equals(DATA_TABLE_FILTER_SHOW_ALL)) {
      int screenResultSize = 0;
      if (getResultValueTypes().size() > 0) {
        screenResultSize = getResultValueTypes().get(0).getResultValues().size();
      }
      _fullScreenResultDataTable.setScreenResultSize(screenResultSize);
      _screenResultDataTable = _fullScreenResultDataTable;
    }
    else if (getDataFilter().getSelection().equals(DATA_TABLE_FILTER_SHOW_POSITIVES)) {
      _positivesOnlyScreenResultDataTable.setPositivesDataHeader(getShowPositivesOnlyForDataHeader().getSelection());
      _screenResultDataTable = _positivesOnlyScreenResultDataTable;
    }
    else {
      _singlePlateScreenResultDataTable.setPlateNumber(getDataFilter().getSelection());
      _screenResultDataTable = _singlePlateScreenResultDataTable;
    }
    _screenResultDataTable.setResultValueTypes(getDataHeaderSelections().getSelections());
  }

  private void lazyBuildDataFilterSelections()
  {
    if (_screenResult == null) {
      _dataFilter = new UISelectOneBean<Integer>();
      return;
    }

    log.debug("updating data table filter selections");

    SortedSet<Integer> filters = new TreeSet<Integer>(_screenResult.getPlateNumbers());
    filters.add(DATA_TABLE_FILTER_SHOW_ALL);
    if (getShowPositivesOnlyForDataHeader().getSize() > 0) {
      filters.add(DATA_TABLE_FILTER_SHOW_POSITIVES);
    }
    _dataFilter =
      new UISelectOneBean<Integer>(filters, DATA_TABLE_FILTER_SHOW_ALL) {
      @Override
      protected String getLabel(Integer val)
      {
        if (val == DATA_TABLE_FILTER_SHOW_ALL) {
          return "All";
        }
        if (val == DATA_TABLE_FILTER_SHOW_POSITIVES) {
          return "Positives";
        }
        return super.getLabel(val);
      }
    };
  }
  private void resetView()
  {
    _dataFilter = null;
    _showPositivesOnlyForDataHeader = null;
  }

}
