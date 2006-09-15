// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.List;

import javax.faces.component.UIData;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;

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
  
  public static final int DEFAULT_PAGESIZE = 10;
  

  // private instance data
  
  private List<E> _unsortedResults;
  private List<E> _currentResults;
  private int _resultsSize;
  private int _currentIndex = 0;
  private int _pageSize = DEFAULT_PAGESIZE;
  
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
  public Object getRawDataCellValue()
  {
    DataModel dataModel = getDataModel();
    E entity = (E) dataModel.getRowData();
    DataModel columnModel = getDataHeaderColumnModel();
    String columnName = (String) columnModel.getRowData();
    return getColumnValue(entity, columnName);
  }
  
  public String firstPage()
  {
    _currentIndex = 0;
    getDataTable().setFirst(_currentIndex * _pageSize);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String nextPage()
  {
    if ((_currentIndex + 1) * _pageSize <= _resultsSize) {
      _currentIndex ++;
      getDataTable().setFirst(_currentIndex * _pageSize);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String prevPage()
  {
    if (_currentIndex > 0) {
      _currentIndex --;
      getDataTable().setFirst(_currentIndex * _pageSize);      
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String lastPage()
  {
    _currentIndex = _resultsSize / _pageSize;
    getDataTable().setFirst(_currentIndex * _pageSize);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  
  // private instance methods

  abstract protected DataModel createDataHeaderColumnModel();
  abstract protected Object getColumnValue(E entity, String columnName);
}
