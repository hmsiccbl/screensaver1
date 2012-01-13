// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.ui.arch.datatable.DataTable;
import edu.harvard.med.screensaver.ui.arch.datatable.column.EnumColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.RealColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TextColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryDataModel;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;

public class DataTableTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(DataTableTest.class);
  

  // instance data members

  private DataTable<RowItem> _dataTable;
  private DataFetcher<RowItem,Integer,String> _dataFetcher;
  private int _fetchCount;
  private int _sortCount;
  private int _filterCount;
  private List<RowItem> _data;
  private List<TableColumn<RowItem,?>> _columns;
  private IntegerColumn<RowItem> _idCol;
  private TextColumn<RowItem> _nameCol;
  private EnumColumn<RowItem,Status> _statusCol;
  private RealColumn<RowItem> _valueCol;
  private TextColumn<RowItem> _urlCol;

  
  // public constructors and methods
  
  @Override
  public void setUp() 
  {
    _columns = TableColumnManagerTest.makeColumns();
    _idCol = (IntegerColumn<RowItem>) _columns.get(0);
    assertEquals("ID", _idCol.getName());
    _nameCol = (TextColumn<RowItem>) _columns.get(1);
    assertEquals("Name", _nameCol.getName());
    _statusCol = (EnumColumn<RowItem,Status>) _columns.get(2);
    assertEquals("Status", _statusCol.getName());
    _valueCol = (RealColumn<RowItem>) _columns.get(3);
    assertEquals("Value", _valueCol.getName());
    _urlCol = (TextColumn<RowItem>) _columns.get(4);
    assertEquals("URL", _urlCol.getName());

    _data = new ArrayList<RowItem>();
    try {
      _data.add(new RowItem(1, "a", Status.NEW, 1.00, new URL("http://1.com")));
      _data.add(new RowItem(2, "b", Status.ACTIVE, 2.00, new URL("http://2.com")));
      _data.add(new RowItem(3, "c", Status.DEAD, 3.00, new URL("http://3.com")));
    }
    catch (MalformedURLException e) {
      fail(e.getMessage());
    }
    
    _dataFetcher = new DataFetcher<RowItem,Integer,String>() {
      public List<RowItem> fetchAllData()
      {
        ++_fetchCount;
        return _data;
      }

      public Map<Integer,RowItem> fetchData(Set<Integer> keys)
      {
        Map<Integer,RowItem> result = new HashMap<Integer,RowItem>();
        for (RowItem rowitem : _data) {
          if (keys.contains(rowitem.getId())) {
            result.put(rowitem.getId(), rowitem);
          }
        }
        return result;
      }

      public List<Integer> findAllKeys()
      {
        List<Integer> result = new ArrayList<Integer>();
        for (RowItem rowitem : _data) {
          result.add(rowitem.getId());
        }
        return result;
      }

      public void setFilteringCriteria(Map<String,List<? extends Criterion<?>>> criteria)
      {
        throw new UnsupportedOperationException("this DataFetcher expected to be used by InMemoryDataModel only");
      }

      public void setOrderBy(List<String> orderByProperties)
      {
        throw new UnsupportedOperationException("this DataFetcher expected to be used by InMemoryDataModel only");
      }

      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
      }

      @Override
      public void setPropertiesToFetch(List<String> properties)
      {
      }
    };

    _dataTable = new DataTable<RowItem>();
    _dataTable.initialize(new InMemoryDataModel<RowItem>(_dataFetcher) {
      @Override
      public void sort(List<? extends TableColumn<RowItem,?>> sortColumns,
                       SortDirection sortDirection)
      {
        ++_sortCount;
        super.sort(sortColumns, sortDirection);
      }
      
      @Override
      public void filter(List<? extends TableColumn<RowItem,?>> columns)
      {
        ++_filterCount;
        super.filter(columns);
      }
    },
                          _columns,
                          new UISelectOneBean<Integer>(Arrays.asList(1, 2, 4), 2),
                          false);
  }
  
  public void testRefetchOnColumnVisibilityChange()
  {
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    int prevFetchCount = _fetchCount;
    int prevSortCount = _sortCount;
    int prevFilterCount = _filterCount;
    _statusCol.setVisible(false);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after hiding column", prevFetchCount, _fetchCount);
    assertEquals("no data sort after hiding column", prevSortCount, _sortCount);
    assertEquals("no data filter after hiding column", prevFilterCount, _filterCount);
    _statusCol.setVisible(true);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("data fetch after unhiding column", prevFetchCount + 1, _fetchCount);
    // for InMemoryDatModel:
    // when "raw" data is re-fetched, sort and filter need to be reapplied
    assertEquals("data sort after unhiding column", prevSortCount + 1, _sortCount);
    assertEquals("data filter after unhiding column", prevFilterCount + 1, _filterCount);
  }
  
  public void testResortOnSortColumnChange()
  {
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    int prevFetchCount = _fetchCount;
    int prevSortCount = _sortCount;
    int prevFilterCount = _filterCount;
    _dataTable.getColumnManager().setSortColumn(_idCol);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after setting same sort column", prevFetchCount, _fetchCount);
    assertEquals("no data sort after setting same sort column", prevSortCount, _sortCount);
    assertEquals("no data filter after setting same sort column", prevFilterCount, _filterCount);
    _dataTable.getColumnManager().setSortColumn(_statusCol);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after setting different sort column", prevFetchCount, _fetchCount);
    assertEquals("data sort after setting different sort column", prevSortCount + 1, _sortCount);
    assertEquals("no data filter after setting different sort column", prevFilterCount, _filterCount);
    _dataTable.getColumnManager().setSortDirection(SortDirection.DESCENDING);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after setting different sort dir", prevFetchCount, _fetchCount);
    assertEquals("data sort after setting same different dir", prevSortCount + 2, _sortCount);
    assertEquals("no data filter after setting same different dir", prevFilterCount, _filterCount);
    _dataTable.getColumnManager().setSortColumn(_urlCol);
    _dataTable.getColumnManager().setSortDirection(SortDirection.ASCENDING);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after setting different sort col & dir", prevFetchCount, _fetchCount);
    assertEquals("(single) data sort after setting same different col & dir", prevSortCount + 3, _sortCount);
    assertEquals("no data filter after setting same different col & dir", prevFilterCount, _filterCount);
  }
  
  public void testRefilterOnCriterionChange()
  {
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    int prevFetchCount = _fetchCount;
    int prevSortCount = _sortCount;
    int prevFilterCount = _filterCount;
    _statusCol.clearCriteria().addCriterion(new Criterion<Status>(Operator.EQUAL, Status.DEAD));
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after adding criterion", prevFetchCount, _fetchCount);
    assertEquals("data filter after adding criterion", prevFilterCount + 1, _filterCount);
    // for InMemoryDataModel: sort must occur after filter occurs
    assertEquals("data sort after filter", prevSortCount + 1, _sortCount);  
    _statusCol.getCriterion().setValue(Status.NEW);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after changing existing criterion value", prevFetchCount, _fetchCount);
    assertEquals("data filter after changing existing criterion value", prevFilterCount + 2, _filterCount);
    // for InMemoryDataModel: sort must occur after filter occurs
    assertEquals("data sort after changing existing criterion value", prevSortCount + 2, _sortCount);
    
    _statusCol.setVisible(false);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("no data fetch after criterion column hidden", prevFetchCount, _fetchCount);
    assertEquals("data filter after criterion column hidden", prevFilterCount + 3, _filterCount);
    // for InMemoryDataModel: sort must occur after filter occurs
    assertEquals("data sort after criterion column hidden", prevSortCount + 3, _sortCount);
    
    _statusCol.setVisible(true);
    _dataTable.getDataTableModel().setRowIndex(0);
    _dataTable.getRowData();
    assertEquals("data fetch after criterion column unhidden", prevFetchCount + 1, _fetchCount);
    // for InMemoryDatModel:
    // when "raw" data is re-fetched, sort and filter need to be reapplied
    // re-filter also necessary since criterion column may have changed filter result
    assertEquals("data sort after criterion column unhidden", prevSortCount + 4, _sortCount);
    assertEquals("data filter after criterion column unhidden", prevFilterCount + 4, _filterCount);
  }
  
  public void testSortColumnChangedIfHidden()
  {
    _dataTable.getColumnManager().setSortColumn(_nameCol);
    _dataTable.getColumnManager().setSortDirection(SortDirection.DESCENDING);
    _dataTable.getDataTableModel().setRowIndex(0);
    RowItem rowData = _dataTable.getRowData();
    assertEquals("originally sorted desc on name column", _nameCol, _dataTable.getColumnManager().getSortColumn());
    assertEquals("originally sorted desc on name column", new Integer(3), rowData.getId());
    _nameCol.setVisible(false);
    _dataTable.getDataTableModel().setRowIndex(0);
    rowData = _dataTable.getRowData();
    assertEquals("sorted on id column after hiding original sort column", _idCol, _dataTable.getColumnManager().getSortColumn());
    assertEquals("sorted on id column after hiding original sort column", new Integer(3), rowData.getId());
  }
  
  public void testRowsPerPageSelector()
  {
    assertEquals(2, _dataTable.getRowsPerPage());
    
    _dataTable.getRowsPerPageSelector().setSelection(1);
    assertEquals(1, _dataTable.getRowsPerPage());
    assertTrue(_dataTable.isMultiPaged());
    
    _dataTable.getRowsPerPageSelector().setSelection(4);
    assertEquals(4, _dataTable.getRowsPerPage());
    assertFalse(_dataTable.isMultiPaged());
  }
  
  public void testResetFilter()
  {
    _dataTable.getColumnManager().setSortColumn(_valueCol);
    _valueCol.getCriterion().setOperatorAndValue(Operator.GREATER_THAN_EQUAL, 2.0);
    assertEquals("filtered prior to reset", 2, _dataTable.getRowCount());

    _dataTable.resetFilter();
    assertEquals("filter reset", 3, _dataTable.getRowCount());
  }

  // private methods

}
