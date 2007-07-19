// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.faces.component.UIData;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.screenresults.DataTableRowsPerPageUISelectOneBean;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.TableSortManager;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;


/**
 * A sortable, paging search result of {@link AbstractEntity model entities}.
 * Supports two modes of operation: "summary" and "detail" modes, corresponding
 * to UI browsers and UI viewers, respectively. Maintains the current page for
 * summary mode and the current entity for detail mode, allowing each of these
 * to be scrolled independently. The current sort column and sort order,
 * however, are shared between the two modes.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class SearchResults<E> extends AbstractBackingBean
{
  
  // public static final data
  
  private static final Logger log = Logger.getLogger(SearchResults.class);
  
  private static final List<Integer> PAGE_SIZE_SELECTIONS =
    Arrays.asList(10, 20, 50, 100, DataTableRowsPerPageUISelectOneBean.SHOW_ALL_VALUE);
  private static final Integer DEFAULT_PAGESIZE = PAGE_SIZE_SELECTIONS.get(1);

  /**
   * Workaround for JSF suckiness. Two things: first, I need to use the returning a Map trick to
   * get around the problem that JSF EL doesn't allow parameterized methods. Second, I gotta
   * escape the backslashes in the f:param, since the JSF EL is evaluating that backslash as an
   * escape character somewhere.
   */
  private final Map<String,String> _backslashEscaper = new HashMap<String,String>() {
    private static final long serialVersionUID = 1L;
    public String get(Object key)
    {
      if (key instanceof String) {
        return ((String) key).replace("\\", "\\\\");
      }
      else {
        return key.toString();
      }
    }
  };
  

  // private instance data
  
  private Collection<E> _unsortedResults;
  private List<E> _currentSort;
  private Pair<TableColumn<E>,SortDirection> _currentSortType;
  private int _resultsSize;
  private int _currentPageIndex = 0;
  private int _currentEntityIndex = 0;
  private DataTableRowsPerPageUISelectOneBean _rowsPerPage;
  private UIData _dataTable;
  private DataModel _dataModel;
  private TableSortManager<E> _sortManager;
  private UISelectOneBean<DataExporter<E>> _dataExporterSelector;
  private boolean _editMode;
  private boolean _hasEditableColumns;

  
  // public constructor
  
  /**
   * Construct a new <code>SearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public SearchResults(Collection<E> unsortedResults)
  {
    _unsortedResults = unsortedResults;
    _resultsSize = unsortedResults.size();
    _rowsPerPage = new DataTableRowsPerPageUISelectOneBean(PAGE_SIZE_SELECTIONS, 
                                                           DEFAULT_PAGESIZE);
    _rowsPerPage.setAllRowsValue(_resultsSize);
  }

  
  // public getters and setters - used by searchResults.jspf
  
  public TableSortManager<E> getSortManager()
  {
    if (_sortManager == null) {
      List<TableColumn<E>> columns = getColumns();
      initializeTableSortManager(columns);
      initializeCompoundSortColumns();
      initializeHasEditableColumns(columns);
    }
    
    return _sortManager;
  }
  
  protected List<Integer[]> getCompoundSorts()
  {
    return new ArrayList<Integer[]>();
  }

  public Collection<E> getContents()
  {
    return _unsortedResults;
  }
  
  /**
   * Get the data table.
   * @return the data table
   */
  public UIData getDataTable()
  {
    return _dataTable;
  }

  /**
   * Set the data table.
   * @param dataTable the new data table
   */
  public void setDataTable(UIData dataTable)
  {
    _dataTable = dataTable;
  }
  
  /**
   * Get the data model.
   * @return the data model
   */
  public DataModel getDataModel()
  {
    if (_dataModel == null) {
      doSort();
    }
    return _dataModel;
  }

  /**
   * Set the data model.
   * @param dataModel the new data model
   */
  public void setDataModel(DataModel dataModel)
  {
    _dataModel = dataModel;
  }
  
  /**
   * Get the (1-based) index of the first item displayed on the current page.
   * @return the (1-based) index of the first item displayed on the current page
   */
  public int getFirstIndex()
  {
    if (_resultsSize == 0) {    // special case if results are empty
      return 0;
    }
    return (_currentPageIndex * _rowsPerPage.getSelection()) + 1;
  }
  
  /**
   * Get the (1-based) index of the last item displayed on the current page.
   * @return the (1-based) index of the last item displayed on the current page
   */
  public int getLastIndex()
  {
    int lastIndex = (_currentPageIndex + 1) * _rowsPerPage.getSelection();
    if (lastIndex > _resultsSize) {
      lastIndex = _resultsSize;
    }
    return lastIndex;
  }
  
  /**
   * Get the (1-based) index of the current item displayed on the current page.
   * @return the (1-based) index of the current item displayed on the current page
   */
  public int getCurrentIndex()
  {
    if (_resultsSize == 0) {    // special case if results are empty
      return 0;
    }
    return _currentEntityIndex + 1;
  }
  
  /**
   * Get the data object associated with the current row.
   * @return the data object associated with the current row
   */
  public E getCurrentRowDataObject()
  {
    if (_resultsSize == 0) {
      return null;
    }
    return getCurrentSort().get(getCurrentIndex() - 1);
  }
  
  /**
   * Get the total size of the search results.
   * @return the total size of the search results
   */
  public int getResultsSize()
  {
    return _resultsSize;
  }
  
  /**
   * Get the number of items currently being displayed on a page.
   * @return the number of items currently being displayed on a page
   */
  public DataTableRowsPerPageUISelectOneBean getRowsPerPageSelector()
  {
    return _rowsPerPage;
  }
  
  
  // public action command methods & action listeners

  /**
   * Resort the results according to the current column, as selected by the user
   * in a drop-down list (in the UI), and redisplay the page. Sort direction is
   * determined by last call to {@link #setSortDirection(SortDirection)}.
   * Cache any newly computed sorts of the results for reuse.
   * 
   * @return the navigation rule to redisplay the current page
   */
  public Object sortOnSelectedColumn()
  {
    Object currentEntity = _currentSort.get(_currentEntityIndex);
    doSort();
    _currentEntityIndex = _currentSort.indexOf(currentEntity);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  /**
   * Get the value to be displayed for the current cell.
   * @return the value to be displayed for the current cell
   */
  public Object getCellValue()
  {
    return getCurrentColumn().getCellValue(getEntity());
  }
  
  public void setCellValue(Object value)
  {
    if (log.isDebugEnabled()) {
      log.debug("setting value on " + getEntity() + " from column " + getCurrentColumn().getName() + ": " + value);
    }
    getCurrentColumn().setCellValue(getEntity(), value);
  }

  /**
   * Perform the action for clicking on the current cell. Return the navigation rule to go
   * along with the action for clicking on the current cell. This method is only called when
   * {@link #getIsCommandLink()} is true.
   * 
   * @return the navigation rule to go along with the action for clicking on the current cell 
   */
  @SuppressWarnings("unchecked")
  public Object cellAction()
  {
    return getCurrentColumn().cellAction(getEntity());
  }

  /**
   * Reset the state of the search results to display the first page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String firstPage()
  {
    if (getViewMode().equals(SearchResultsViewMode.SUMMARY)) {
      _currentPageIndex = 0;
    }
    else {
      _currentEntityIndex = 0;
    }
    return gotoCurrentIndex();
  }

  /**
   * Reset the state of the search results to display the previous page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String prevPage()
  {
    if (getViewMode().equals(SearchResultsViewMode.SUMMARY)) {
      _currentPageIndex = Math.max(0,
                                   _currentPageIndex - 1);
    }
    else {
      _currentEntityIndex = Math.max(0,
                                     _currentEntityIndex - 1);
    }
    return gotoCurrentIndex();
  }

  /**
   * Reset the state of the search results to display the next page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String nextPage()
  {
    if (getViewMode().equals(SearchResultsViewMode.SUMMARY)) {
      _currentPageIndex = Math.min(_currentPageIndex + 1,
                                   Math.max(0, _resultsSize - 1) / _rowsPerPage.getSelection());
    }
    else {
      _currentEntityIndex = Math.min(_currentEntityIndex + 1,
                                     _resultsSize - 1);
    }
    
    return gotoCurrentIndex();
  }

  /**
   * Reset the state of the search results to display the last page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String lastPage()
  {
    if (getViewMode().equals(SearchResultsViewMode.SUMMARY)) {
      _currentPageIndex = Math.max(0, _resultsSize - 1) / _rowsPerPage.getSelection();
    }
    else {
      _currentEntityIndex = _resultsSize - 1;
    }
    return gotoCurrentIndex();
  }
  
  /**
   * Get the current view mode.
   * @return the current view mode as a {@link SearchResultsViewMode} object
   */
  public SearchResultsViewMode getViewMode()
  {
    // HACK: is there a better way of determining the context in which this controller is being used???
    if (
      getFacesContext().getViewRoot().getViewId().contains("Browser") ||
      getFacesContext().getViewRoot().getViewId().contains("SearchResults")
    ) {
      return SearchResultsViewMode.SUMMARY;
    }
    return SearchResultsViewMode.DETAIL;
  }
  
  /**
   * Returns whether the current view mode is a "summary" view.
   * 
   * @return <code>true</code> iff the view mode is
   *         SearchResultsViewMode.SUMMARY, else <code>false</code>
   */
  public boolean isSummaryView()
  {
    return getViewMode().equals(SearchResultsViewMode.SUMMARY);
  }
  
  public boolean isEditMode()
  {
    return _editMode;
  }
  
  public boolean getHasEditableColumns()
  {
    return _hasEditableColumns;
  }
  
  /**
   * Update the number of items displayed per page, based on the user selecting
   * a new value in the selection input for items per page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String updateRowsPerPage()
  {
    getDataTable().setFirst(0);
    _currentPageIndex = 0;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  final public String edit()
  {
    setEditMode(true);
    doEdit();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  protected void doEdit() {}

  final public String save()
  {
    setEditMode(false);
    doSave();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  protected void doSave() {}
  
  final public String cancel()
  {
    setEditMode(false);
    doCancel();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  protected void doCancel() {}


  public UISelectOneBean<DataExporter<E>> getDataExporterSelector()
  {
    if (_dataExporterSelector == null) {
      _dataExporterSelector = new UISelectOneBean<DataExporter<E>>(getDataExporters()) {
        @Override
        protected String getLabel(DataExporter<E> dataExporter)
        {
          return dataExporter.getFormatName(); 
        }
      };
    }
    return _dataExporterSelector;
  }
  
  @SuppressWarnings("unchecked")
  final public String downloadSearchResults()
  {
    DataExporter dataExporter = getDataExporterSelector().getSelection();
    InputStream inputStream = dataExporter.export(getContents());
    try {
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         inputStream,
                                         dataExporter.getFileName(),
                                         dataExporter.getMimeType());
    }
    catch (IOException e) {
      reportApplicationError(e.toString());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public Map<String,String> getEscapeBackslashes()
  {
    return _backslashEscaper;
  }

  
  // abstract public and private methods

  /**
   * Return the string action to show the summary view
   * @return the summary view page
   */
  abstract public String showSummaryView();
  
  /**
   * Create and return a list of the column header values.
   * 
   * @return a list of the column headers
   */
  abstract protected List<TableColumn<E>> getColumns();
  
  /**
   * Set the entity to be displayed in detail mode.
   * @param entity the entity to be displayed in detail mode
   */
  abstract protected void setEntityToView(E entity);
  
  abstract protected List<DataExporter<E>> getDataExporters();
  
    
  // protected instance methods
  
  final protected TableColumn<E> getCurrentColumn()
  {
    return getSortManager().getCurrentColumn();
  }

  /**
   * Get the entity in the current cell.
   * @return the entity in the current cell
   */
  @SuppressWarnings("unchecked")  
  final protected E getEntity()
  {
    return (E) getDataModel().getRowData();
  }
  
  final protected List<E> getCurrentSort()
  {
    doSort();
    return _currentSort;
  }


  // private instance methods
  

  private void initializeTableSortManager(List<TableColumn<E>> columns)
  {
    _sortManager = new TableSortManager<E>(columns);
    _sortManager.addObserver(new Observer() {
      public void update(Observable o, Object obj)
      {
        doSort();
      }
    });
  }

  private void initializeHasEditableColumns(List<TableColumn<E>> columns)
  {
    for (TableColumn<E> column : columns) {
      if (column.isEditable()) {
        _hasEditableColumns = true;
        break;
      }
    }
  }

  private void initializeCompoundSortColumns()
  {
    List<Integer[]> compoundSorts = getCompoundSorts();
    for (Integer[] compoundSort : compoundSorts) {
      List<TableColumn<E>> compoundSortColumns = new ArrayList<TableColumn<E>>();
      for (Integer colIndex : compoundSort) {
        compoundSortColumns.add(_sortManager.getColumn(colIndex));
      }
      _sortManager.addCompoundSortColumns(compoundSortColumns);
    }
  }

  /**
   * Update the search browser's data table, or the search viewer's current
   * entity, depending upon the current SearchResultsViewMode.
   */
  private String gotoCurrentIndex()
  {
    if (getViewMode().equals(SearchResultsViewMode.SUMMARY)) {
      // update the search results summary table
      getDataTable().setFirst(_currentPageIndex * _rowsPerPage.getSelection());
    }
    else {
      // update the entity viewer
      if (_resultsSize > 0) {
        setEntityToView(_currentSort.get(_currentEntityIndex));
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Internal method for performing and caching sorted results, by both sort
   * column and direction.
   */
  private void doSort()
  {
    // TODO: reinstate cached sort orders by column & direction
    Pair<TableColumn<E>,SortDirection> newSortType = 
      new Pair<TableColumn<E>,SortDirection>(getSortManager().getSortColumn(), getSortManager().getSortDirection());
    if (!newSortType.equals(_currentSortType)) {
      _currentSort = new ArrayList<E>(_unsortedResults);
      Collections.sort(_currentSort, getSortManager().getSortColumnComparator());
      _dataModel = new ListDataModel(_currentSort);
      _currentSortType = newSortType;
    }
  }

  private void setEditMode(boolean isEditMode)
  {
    _editMode = isEditMode;
    getSortManager().getColumnModel().updateVisibleColumns();
  }
}
