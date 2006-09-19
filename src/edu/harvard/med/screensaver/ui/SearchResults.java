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
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

/**
 * A sortable, paging search result of {@link AbstractEntity model entities}.
 * 
 * <p>
 * 
 * Currently, this class has some {@link Library}-specific code in it. I will be generalizing
 * that part of the code after I have gotten paging and sorting worked out to my liking.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
abstract public class SearchResults<E extends AbstractEntity>
implements ScreensaverConstants
{
  
  // public static final data
  
  private static final Logger log = Logger.getLogger(SearchResults.class);
  public static final int [] PAGESIZES = { 10, 20, 50, 100 };
  public static final int DEFAULT_PAGESIZE = 10;
  

  // private instance data
  
  private List<E> _unsortedResults;
  private List<E> _currentResults;
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
    _currentResults = _unsortedResults;
    _resultsSize = _currentResults.size();
    _dataModel = new ListDataModel(_currentResults);
  }

  
  // public getters and setters
  
  /**
   * @return the data table
   */
  public UIData getDataTable()
  {
    return _dataTable;
  }

  /**
   * @param dataTable the data table
   */
  public void setDataTable(UIData dataTable)
  {
    _dataTable = dataTable;
  }
  
  /**
   * @return the dataModel
   */
  public DataModel getDataModel()
  {
    return _dataModel;
  }

  /**
   * @param dataModel the dataModel
   */
  public void setDataModel(DataModel dataModel)
  {
    _dataModel = dataModel;
  }

  /**
   * @return the dataHeaderColumnModel
   */
  public DataModel getDataHeaderColumnModel()
  {
    return _dataHeaderColumnModel;
  }

  /**
   * @param dataHeaderColumnModel the dataHeaderColumnModel
   */
  public void setDataHeaderColumnModel(DataModel dataHeaderColumnModel)
  {
    _dataHeaderColumnModel = dataHeaderColumnModel;
  }

  
  // public instance methods
  
  @SuppressWarnings("unchecked")
  public E getEntity()
  {
    return (E) getDataModel().getRowData();
  }
  
  public String getColumnName()
  {
    return (String) getDataHeaderColumnModel().getRowData();
  }
  
  public boolean getIsCommandLink()
  {
    return isCommandLink(getColumnName());
  }
  
  @SuppressWarnings("unchecked")
  public Object getCellValue()
  {
    return getCellValue(getEntity(), getColumnName());
  }

  @SuppressWarnings("unchecked")
  public Object getCellAction()
  {
    return getCellAction(getEntity(), getColumnName());
  }
  
  public int getFirstIndex()
  {
    return _currentIndex * _itemsPerPage + 1;
  }
  
  public int getLastIndex()
  {
    int lastIndex = (_currentIndex + 1) * _itemsPerPage;
    if (lastIndex > _resultsSize) {
      lastIndex = _resultsSize;
    }
    return lastIndex;
  }
  
  public int getResultsSize()
  {
    return _resultsSize;
  }
  
  public int getItemsPerPage()
  {
    return _itemsPerPage;
  }
  
  public String firstPage()
  {
    _currentIndex = 0;
    getDataTable().setFirst(_currentIndex * _itemsPerPage);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String nextPage()
  {
    if ((_currentIndex + 1) * _itemsPerPage <= _resultsSize) {
      _currentIndex ++;
      getDataTable().setFirst(_currentIndex * _itemsPerPage);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String prevPage()
  {
    if (_currentIndex > 0) {
      _currentIndex --;
      getDataTable().setFirst(_currentIndex * _itemsPerPage);      
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String lastPage()
  {
    _currentIndex = _resultsSize / _itemsPerPage;
    getDataTable().setFirst(_currentIndex * _itemsPerPage);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public List<SelectItem> getItemsPerPageSelections()
  {
    List<Integer> selections = new ArrayList<Integer>();
    for (int i : PAGESIZES) {
      selections.add(i);
    }
    return JSFUtils.createUISelectItems(selections);
  }
  
  public void itemsPerPageListener(ValueChangeEvent event)
  {
    _itemsPerPage = (Integer) event.getNewValue();
    getDataTable().setRows(_itemsPerPage);
    _currentIndex = 0;
    getDataTable().setFirst(0);
  }
  
  public String updateItemsPerPage()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // private instance methods

  abstract protected DataModel createDataHeaderColumnModel();
  abstract protected boolean isCommandLink(String columnName);
  abstract protected Object getCellValue(E entity, String columnName);
  abstract protected Object getCellAction(E entity, String columnName);
}
