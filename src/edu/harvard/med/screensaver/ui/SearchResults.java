// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.util.JSFUtils;


/**
 * A sortable, paging search result of {@link AbstractEntity model entities}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
abstract public class SearchResults<E extends AbstractEntity>
implements ScreensaverConstants
{
  
  // public static final data
  
  private static final Logger log = Logger.getLogger(SearchResults.class);
  public static final int [] PAGESIZES = { 10, 20, 50, 100 };
  public static final int DEFAULT_PAGESIZE = PAGESIZES[0];
  

  // private instance data
  
  private List<E> _unsortedResults;
  private List<E> _currentSort;
  private String _currentSortColumnName;
  private boolean _isCurrentSortForward;
  private Map<String,List<E>> _forwardSorts = new HashMap<String,List<E>>();
  private Map<String,List<E>> _reverseSorts = new HashMap<String,List<E>>();
  private int _resultsSize;
  private int _currentIndex = 0;
  private int _itemsPerPage = DEFAULT_PAGESIZE;
  
  private UIData _dataTable;
  private DataModel _dataModel;
  private DataModel _dataHeaderColumnModel = createDataHeaderColumnModel();

  
  // public constructor
  
  /**
   * Construct a new <code>SearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public SearchResults(List<E> unsortedResults)
  {
    _unsortedResults = unsortedResults;
    _currentSort = _unsortedResults;
    _resultsSize = _currentSort.size();
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
    return _currentIndex * _itemsPerPage + 1;
  }
  
  /**
   * Get the (1-based) index of the last item displayed on the current page.
   * @return the (1-based) index of the last item displayed on the current page
   */
  public int getLastIndex()
  {
    int lastIndex = (_currentIndex + 1) * _itemsPerPage;
    if (lastIndex > _resultsSize) {
      lastIndex = _resultsSize;
    }
    return lastIndex;
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
  public int getItemsPerPage()
  {
    return _itemsPerPage;
  }

  /**
   * Get the contents of the list selection box for the number of items per page.
   * @return the contents of the list selection box for the number of items per page
   */
  public List<SelectItem> getItemsPerPageSelections()
  {
    List<Integer> selections = new ArrayList<Integer>();
    for (int i : PAGESIZES) {
      selections.add(i);
    }
    return JSFUtils.createUISelectItems(selections);
  }


  // public action command methods & action listeners

  /**
   * Resort the results according to the current column, and redisplay the page. Sort
   * descending if the previous sort order was ascending and on the same column. Otherwise,
   * sort descending. Cache any newly computed sorts of the results for reuse.
   * 
   * @return the navigation rule to redisplay the current page
   */
  public Object sortOnColumn()
  {
    String sortColumnName = getColumnName();
    boolean isSortForward =
      sortColumnName.equals(_currentSortColumnName) ? ! _isCurrentSortForward : true;
    
    // get the forward sort for the specified column, computing it if needed
    List<E> forwardSort = _forwardSorts.get(sortColumnName);
    if (forwardSort == null) {
      forwardSort = new ArrayList<E>(_unsortedResults);
      Collections.sort(forwardSort, getComparatorForColumnName(sortColumnName));
      _forwardSorts.put(sortColumnName, forwardSort);
    }
    
    // set the _currentSort variable appropriately
    if (isSortForward) {
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
    _isCurrentSortForward = isSortForward;
    _dataModel = new ListDataModel(_currentSort);
    
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
  public Object getCellAction()
  {
    return getCellAction(getEntity(), getColumnName());
  }

  /**
   * Reset the state of the search results to display the first page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String firstPage()
  {
    _currentIndex = 0;
    getDataTable().setFirst(_currentIndex * _itemsPerPage);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Reset the state of the search results to display the previous page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String prevPage()
  {
    if (_currentIndex > 0) {
      _currentIndex --;
      getDataTable().setFirst(_currentIndex * _itemsPerPage);      
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Reset the state of the search results to display the next page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String nextPage()
  {
    if ((_currentIndex + 1) * _itemsPerPage <= _resultsSize) {
      _currentIndex ++;
      getDataTable().setFirst(_currentIndex * _itemsPerPage);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  /**
   * Reset the state of the search results to display the last page.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String lastPage()
  {
    _currentIndex = _resultsSize / _itemsPerPage;
    getDataTable().setFirst(_currentIndex * _itemsPerPage);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  /**
   * Update the number of items displayed per page, based on the user selecting a new value
   * in the selection input for items per page.
   * 
   * @param event the event generated by the user selecting a new value in the selection
   * input for items per page
   */
  public void itemsPerPageListener(ValueChangeEvent event)
  {
    _itemsPerPage = (Integer) event.getNewValue();
    getDataTable().setRows(_itemsPerPage);
    _currentIndex = 0;
    getDataTable().setFirst(0);
  }
  
  /**
   * Perform the necessary command action for when the items per page changes. This only
   * requires refreshing the current page, since the necessary changes to the search
   * results state have already been made by {@link #itemsPerPageListener(ValueChangeEvent)}.
   * 
   * @return the navigation rule to redisplay the search results
   */
  public String updateItemsPerPage()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // abstract protected instance methods
  
  /**
   * Create and return a model for the column headers. This basically consists of a list
   * of the column header values.
   * 
   * @return a model for the column headers
   */
  abstract protected DataModel createDataHeaderColumnModel();
  
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
  abstract protected Object getCellAction(E entity, String columnName);
  
  /**
   * Get a comparator for sorting the entities according to the specified column.
   * 
   * @param columnName the name of the column
   * @return a comparator for sorting the entities according to the specified column
   */
  abstract protected Comparator<E> getComparatorForColumnName(String columnName);
  
  
  // private instance methods
  
  /**
   * Get the entity in the current cell.
   * @return the entity in the current cell
   */
  @SuppressWarnings("unchecked")
  private E getEntity()
  {
    return (E) getDataModel().getRowData();
  }
}
