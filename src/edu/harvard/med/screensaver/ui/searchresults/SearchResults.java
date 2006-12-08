// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


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
abstract public class SearchResults<E extends AbstractEntity> extends AbstractBackingBean
{
  
  // public static final data
  
  private static final Logger log = Logger.getLogger(SearchResults.class);
  
  private static final int [] PAGESIZES = { 10, 20, 50, 100 };
  private static final int DEFAULT_PAGESIZE = PAGESIZES[0];
  
  private static final String SD_FILE = "SDFile";
  private static final String EXCEL_FILE = "Excel Spreadsheet";
  private static final String [] DOWNLOAD_FORMATS = { "", EXCEL_FILE, SD_FILE };

  private static final List<String> PAGE_SIZE_SELECTIONS =
    Arrays.asList("10", "20", "50", "100", "All");
  

  // private instance data
  
  private List<E> _unsortedResults;
  protected List<E> _currentSort;
  private String _currentSortColumnName;
  private SortDirection  _currentSortDirection = SortDirection.ASCENDING;
  private Map<String,List<E>> _forwardSorts = new HashMap<String,List<E>>();
  private Map<String,List<E>> _reverseSorts = new HashMap<String,List<E>>();
  private int _resultsSize;
  private int _currentPageIndex = 0;
  private int _currentEntityIndex = 0;
  private UISelectOneBean<String> _itemsPerPage = new UISelectOneBean<String>(PAGE_SIZE_SELECTIONS);
  private String _downloadFormat = "";
  
  private UIData _dataTable;
  private DataModel _dataModel;
  private DataModel _dataHeaderColumnModel = new ListDataModel(getColumnHeaders());

  
  // public constructor
  
  /**
   * Construct a new <code>SearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public SearchResults(List<E> unsortedResults)
  {
    _unsortedResults = unsortedResults;
    _resultsSize = unsortedResults.size();
    doSort(getColumnHeaders().get(0), SortDirection.ASCENDING);
    _dataModel = new ListDataModel(_currentSort);
  }

  
  // public getters and setters - used by searchResults.jspf
  
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
   * Get the data header column model.
   * @return the data header column model
   */
  public DataModel getDataHeaderColumnModel()
  {
    return _dataHeaderColumnModel;
  }

  /**
   * Set the data header column model.
   * @param dataHeaderColumnModel the data header column model
   */
  public void setDataHeaderColumnModel(DataModel dataHeaderColumnModel)
  {
    _dataHeaderColumnModel = dataHeaderColumnModel;
  }

  /**
   * Get the name of the current column.
   * @return the name of the current column
   */
  public String getColumnName()
  {
    return (String) getDataHeaderColumnModel().getRowData();
  }
  
  /**
   * Return true whenever the cell values for the current column should be a hyperlink.
   * @return true whenever the cell values for the current column should be a hyperlink
   */
  public boolean getIsCommandLink()
  {
    return isCommandLink(getColumnName());
  }
  
  /**
   * Return true whenever the cell values for the column with the specified name should
   * be a semicolon-separated list of hyperlinks. In this situation, {@link
   * #getCellValue()} returns an array of values, and {@link #cellAction()} is
   * called with a <code>commandValue</code> parameter equal to the results of
   * {@link #getCellValue(AbstractEntity, String)}.
   * 
   * @return true whenever the cell values for the current column should be a list
   * of hyperlinks
   */
  public boolean getIsCommandLinkList()
  {
    return isCommandLinkList(getColumnName());
  }
  
  /**
   * Get the value to be displayed for the current cell.
   * @return the value to be displayed for the current cell
   */
  @SuppressWarnings("unchecked")
  public Object getCellValue()
  {
    return getCellValue(getEntity(), getColumnName());
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
    return (_currentPageIndex * getNumItemsPerPage()) + 1;
  }
  
  /**
   * Get the (1-based) index of the last item displayed on the current page.
   * @return the (1-based) index of the last item displayed on the current page
   */
  public int getLastIndex()
  {
    int lastIndex = (_currentPageIndex + 1) * getNumItemsPerPage();
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
  public UISelectOneBean<String> getItemsPerPageSelector()
  {
    return _itemsPerPage;
  }
  
  public int getNumItemsPerPage()
  {
    String itemsPerPage = _itemsPerPage.getSelection();
    if (itemsPerPage.equals("All")) {
      return _resultsSize;
    }
    return new Integer(itemsPerPage);
  }

  /**
   * Get the current sort column name.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public String getCurrentSortColumnName()
  {
    return _currentSortColumnName;
  }

  /**
   * Set the current sort column name.
   * 
   * @motivation allow sort column to be set from a drop-down list UI component
   *             (in addition to clicking on table column headers)
   * @param currentSortColumnName the new current sort column name
   */
  public void setCurrentSortColumnName(String currentSortColumnName)
  {
    _currentSortColumnName = currentSortColumnName;
  }

  /**
   * Get the current sort direction.
   * 
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @return the current sort column name
   */
  public SortDirection getCurrentSortDirection()
  {
    return _currentSortDirection;
  }

  /**
   * Set the current sort direction.
   * 
   * @motivation allow sort direction to be set from a drop-down list UI
   *             component (in addition to clicking on table column headers)
   * @param currentSortDirection the new current sort direction
   */
  public void setCurrentSortDirection(SortDirection currentSortDirection)
  {
    _currentSortDirection = currentSortDirection;
  }

  /**
   * Get a list of SelectItem objects for the set of columns that can be sorted
   * on.
   * 
   * @return list of SelectItem objects for the set of columns that can be
   *         sorted on
   */
  public List<SelectItem> getSortColumnSelections()
  {
    List<String> selections = new ArrayList<String>();
    for (String columnName : getColumnHeaders()) {
      selections.add(columnName);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  /**
   * Get a list of SelectItem objects for the set of sort directions (ascending,
   * descending).
   * 
   * @return list of SelectItem objects for the set of sort directions
   *         (ascending, descending)
   */
  public List<SelectItem> getSortDirectionSelections()
  {
    List<SelectItem> selections = new ArrayList<SelectItem>();
    for (SortDirection sortOrder : SortDirection.values()) {
      selections.add(new SelectItem(sortOrder,
                                    sortOrder.toString()));
    }
    return selections;
  }
  
  
  // public action command methods & action listeners

  /**
   * Resort the results according to the column most recently selected by the
   * user (via the UI), and redisplay the page. Sort descending if the previous
   * sort order was ascending and on the same column. Otherwise, sort
   * descending. Cache any newly computed sorts of the results for reuse.
   * 
   * @return the navigation rule to redisplay the current page
   */
  public Object sortOnColumn()
  {
    String sortColumnName = getColumnName();

    // toggle sort order
    SortDirection sortDirection = sortColumnName.equals(_currentSortColumnName) ? 
      _currentSortDirection.equals(SortDirection.ASCENDING) ? 
        SortDirection.DESCENDING : SortDirection.ASCENDING : 
          SortDirection.ASCENDING;

    doSort(sortColumnName, sortDirection);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Resort the results according to the current column, as selected by the user
   * in a drop-down list (in the UI), and redisplay the page. Sort direction is
   * determined by last call to {@link #setCurrentSortDirection(SortDirection)}.
   * Cache any newly computed sorts of the results for reuse.
   * 
   * @return the navigation rule to redisplay the current page
   */
  public Object sortOnSelectedColumn()
  {
    Object currentEntity = _currentSort.get(_currentEntityIndex);
    doSort(_currentSortColumnName, _currentSortDirection);
    _currentEntityIndex = _currentSort.indexOf(currentEntity);
    return REDISPLAY_PAGE_ACTION_RESULT;
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
    _currentEntityIndex = _dataModel.getRowIndex();
    return cellAction(getEntity(), getColumnName());
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
                                   Math.max(0, _resultsSize - 1) / getNumItemsPerPage());
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
      _currentPageIndex = Math.max(0, _resultsSize - 1) / getNumItemsPerPage();
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
      getFacesContext().getViewRoot().getViewId().contains("wellSearchResults")
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
  
  /**
   * Update the number of items displayed per page, based on the user selecting
   * a new value in the selection input for items per page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String updateItemsPerPage()
  {
    getDataTable().setRows(getNumItemsPerPage());
    getDataTable().setFirst(0);
    _currentPageIndex = 0;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Return true whenever the search results are downloadable. For the time being, the only
   * downloadable search results are {@link
   * edu.harvard.med.screensaver.ui.searchresults.WellSearchResults}.
   * 
   * @return true whenever the search results are downloadable
   */
  public boolean getIsDownloadable()
  {
    return false;
  }
  
  public String getDownloadFormat()
  {
    return _downloadFormat;
  }


  public void setDownloadFormat(String downloadFormat)
  {
    _downloadFormat = downloadFormat;
  }

  public List<SelectItem> getDownloadFormatSelections()
  {
    List<String> selections = new ArrayList<String>();
    for (String i : DOWNLOAD_FORMATS) {
      selections.add(i);
    }
    return JSFUtils.createUISelectItems(selections);
  }
  
  public String downloadSearchResults()
  {
    File searchResultsFile = null;
    PrintWriter searchResultsPrintWriter = null;
    FileOutputStream searchResultsFileOutputStream = null;
    try {
      searchResultsFile = File.createTempFile(
        "searchResults.",
        _downloadFormat.equals(SD_FILE) ? ".sdf" : ".xls");
      if (_downloadFormat.equals(SD_FILE)) {
        searchResultsPrintWriter = new PrintWriter(searchResultsFile);
        writeSDFileSearchResults(searchResultsPrintWriter);
        searchResultsPrintWriter.close();
      }
      else {
        HSSFWorkbook searchResultsWorkbook = new HSSFWorkbook();
        writeExcelFileSearchResults(searchResultsWorkbook);
        searchResultsFileOutputStream = new FileOutputStream(searchResultsFile);
        searchResultsWorkbook.write(searchResultsFileOutputStream);
        searchResultsFileOutputStream.close();
      }
      JSFUtils.handleUserFileDownloadRequest(
        getFacesContext(),
        searchResultsFile,
        _downloadFormat.equals(SD_FILE) ? "chemical/x-mdl-sdfile" : Workbook.MIME_TYPE);
    }
    catch (IOException e)
    {
      showMessage("systemError");
      log.error(e.getMessage());
    }
    finally {
      IOUtils.closeQuietly(searchResultsPrintWriter);
      IOUtils.closeQuietly(searchResultsFileOutputStream);
      if (searchResultsFile != null && searchResultsFile.exists()) {
        searchResultsFile.delete();
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
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
  abstract protected List<String> getColumnHeaders();
  
  /**
   * Return true whenever the cell values for the column with the specified name should
   * be a hyperlink.
   * 
   * @param columnName the name of the column
   * @return true whenever the cell values for the column with the specified name should
   * be a hyperlink.
   */
  abstract protected boolean isCommandLink(String columnName);
  
  /**
   * Return true whenever the cell values for the column with the specified name should
   * be a semicolon-separated list of hyperlinks. In this situation, {@link
   * #getCellValue()} returns an array of values, and {@link #cellAction()} is
   * called with a <code>commandValue</code> parameter equal to the results of
   * {@link #getCellValue(AbstractEntity, String)}.
   * 
   * @param columnName the name of the column
   * @return true whenever the cell values for the column with the specified name should
   * be a list of hyperlinks.
   */
  abstract protected boolean isCommandLinkList(String columnName);
  
  /**
   * Get the value to be displayed for the current cell.
   * 
   * @param entity the entity displayed in the current cell (the row index)
   * @param columnName the name of the column for the current cell (the column index)
   * @return the value to be displayed for the current cell
   */
  abstract protected Object getCellValue(E entity, String columnName);
  
  /**
   * Perform the action for clicking on the current cell. Return the navigation rule to go
   * along with the action for clicking on the current cell. This method is only called when
   * {@link #isCommandLink} is true.
   * 
   * @param entity the entity displayed in the current cell (the row index)
   * @param columnName the name of the column for the current cell (the column index)
   * @return the navigation rule to go along with the action for clicking on the current cell 
   */
  abstract protected Object cellAction(E entity, String columnName);
  
  /**
   * Get a comparator for sorting the entities according to the specified column.
   * 
   * @param columnName the name of the column
   * @return a comparator for sorting the entities according to the specified column
   */
  abstract protected Comparator<E> getComparatorForColumnName(String columnName);
  
  /**
   * Set the entity to be displayed in detail mode.
   * @param entity the entity to be displayed in detail mode
   */
  abstract protected void setEntityToView(E entity);
  
  
  // protected instance methods
  
  /**
   * Get the entity in the current cell.
   * @return the entity in the current cell
   */
  @SuppressWarnings("unchecked")  
  protected E getEntity()
  {
    return (E) getDataModel().getRowData();
  }
  
  /**
   * Write the search results as an SD File to the print writer. Subclasses need to
   * override this method to implement writing to SD File.
   * @param searchResultsPrintWriter the print writer to write the search results to
   */
  protected void writeSDFileSearchResults(PrintWriter searchResultsPrintWriter)
  {
    throw new UnsupportedOperationException(
      "This SearchResults (" + this + ") does not know how to write itself as an SD File.");
  }
  
  /**
   * Write the search results as an Excel File to the HSSFWorkbook. Subclasses need to
   * override this method to implement writing to Excel File.
   * @param searchResultsWorkbook the workbook to write the search results to
   */
  protected void writeExcelFileSearchResults(HSSFWorkbook searchResultsWorkbook)
  {
    throw new UnsupportedOperationException(
      "This SearchResults (" + this + ") does not know how to write itself as an Excel File.");    
  }


  
  
  // private instance methods
  
  /**
   * Update the search browser's data table, or the search viewer's current
   * entity, depending upon the current SearchResultsViewMode.
   */
  private String gotoCurrentIndex()
  {
    if (getViewMode().equals(SearchResultsViewMode.SUMMARY)) {
      // update the search results summary table
      getDataTable().setFirst(_currentPageIndex * getNumItemsPerPage());
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
   * Internal method for peforming and caching sorted results, by both sort
   * column and direction.
   * 
   * @param sortColumnName
   * @param sortDirection
   */
  private void doSort(String sortColumnName, SortDirection sortDirection)
  {
    // get the forward sort for the specified column, computing it if needed
    List<E> forwardSort = _forwardSorts.get(sortColumnName);
    if (forwardSort == null) {
      forwardSort = new ArrayList<E>(_unsortedResults);
      Collections.sort(forwardSort, getComparatorForColumnName(sortColumnName));
      _forwardSorts.put(sortColumnName, forwardSort);
    }
    
    // set the _currentSort variable appropriately
    if (sortDirection.equals(SortDirection.ASCENDING)) {
      _currentSort = forwardSort;
    }
    else {
      
      // get the reverse sort for the specified column, computing it if needed
      List<E> reverseSort = _reverseSorts.get(sortColumnName);
      if (reverseSort == null) {
        reverseSort = new ArrayList<E>(forwardSort);
        Collections.reverse(reverseSort);
        _reverseSorts.put(sortColumnName, reverseSort);
      }
      
      _currentSort = reverseSort;
    }
    
    // update other instance fields that need to be set to track the current sort
    _currentSortColumnName = sortColumnName;
    _currentSortDirection = sortDirection;
    _dataModel = new ListDataModel(_currentSort);
  }
}
