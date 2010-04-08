// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.table.column.CompoundColumnComparator;
import edu.harvard.med.screensaver.ui.table.column.EnumColumn;
import edu.harvard.med.screensaver.ui.table.column.IntegerColumn;
import edu.harvard.med.screensaver.ui.table.column.RealColumn;
import edu.harvard.med.screensaver.ui.table.column.SelectableColumnTreeNode;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.TreeModel;

public class TableColumnManagerTest extends TestCase
{
  // static members

  private static final String BASIC_COLUMNS_GROUP = "Basic Columns";
  private static final String ADVANCED_COLUMNS_GROUP = "Advanced Columns";
  private static final String ADVANCED_SPECIAL_COLUMNS_GROUP = "Advanced Columns::Special";


  private static Logger log = Logger.getLogger(TableColumnManagerTest.class);


  // instance data members

  private TableColumnManager<RowItem> _columnManager;
  private List<TableColumn<RowItem,?>> _columns;
  private TableColumn<RowItem,?> _currentSortColumn;
  private SortDirection _currentSortDirection;

  private IntegerColumn<RowItem> _idCol;
  private TextColumn<RowItem> _nameCol;
  private EnumColumn<RowItem,Status> _statusCol;
  private RealColumn<RowItem> _valueCol;
  private TextColumn<RowItem> _urlCol;

  private List<RowItem> _unsortedData;
  private List<RowItem> _sortedData;


  // public constructors and methods

  @Override
  protected void setUp() throws Exception
  {
    _columns = makeColumns();
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
    _columnManager = new TableColumnManager<RowItem>(_columns, null, false);
    _columnManager.addObserver(new Observer() {
      @SuppressWarnings("unchecked")
      public void update(Observable o, Object obj)
      {
        if (obj instanceof SortChangedEvent) {
          SortChangedEvent<RowItem> event = (SortChangedEvent<RowItem>) obj;
          if (event.getColumn() != null) {
            _currentSortColumn = event.getColumn();
          }
          if (event.getDirection() != null) {
            _currentSortDirection = event.getDirection();
          }
          _sortedData = new ArrayList<RowItem>(_unsortedData);
          Collections.sort(_sortedData,
                           new CompoundColumnComparator<RowItem>(
                             _columnManager.getSortColumns(),
                             _columnManager.getSortDirection()));
        }
      }
    });

    _currentSortColumn = _columnManager.getSortColumn();
    _currentSortDirection = _columnManager.getSortDirection();

    _unsortedData = new ArrayList<RowItem>();
    // Note: test data rows must be initially unsorted by every column!
    _unsortedData.add(new RowItem(1, "A", Status.ACTIVE, 3.5, new URL("https://screensaver.med.harvard.edu/screensaver")));
    _unsortedData.add(new RowItem(2, "D", Status.DEAD, 2.0, new URL("http://www.java.sun.com")));
    _unsortedData.add(new RowItem(4, "B", Status.NEW, -3.0, new URL("http://www.hibernate.org")));
    _unsortedData.add(new RowItem(3, "C", Status.ACTIVE, 0.0, new URL("http://www.springframework.org")));

  }

  public static List<TableColumn<RowItem,?>> makeColumns()
  {
    List<TableColumn<RowItem,?>> columns = new ArrayList<TableColumn<RowItem,?>>();
    TableColumn<RowItem,?> idCol = new IntegerColumn<RowItem>(
      "ID", "The row's ID", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(RowItem row) { return row.getId(); }
    };
    TableColumn<RowItem,?> nameCol = new TextColumn<RowItem>(
      "Name", "The row's name", BASIC_COLUMNS_GROUP) {
      @Override
      public String getCellValue(RowItem row) { return row.getName(); }
    };
    TableColumn<RowItem,?> statusCol = new EnumColumn<RowItem,Status>(
      "Status", "The row's status",
      ADVANCED_COLUMNS_GROUP,
      Status.values()) {
      @Override
      public Status getCellValue(RowItem row) { return row.getStatus(); }
      @Override
      protected Comparator<RowItem> getAscendingComparator()
      {
        return new Comparator<RowItem>() {
          public int compare(RowItem row1, RowItem row2)
          {
            Integer orderedStatus1 = row1.getStatus().equals(Status.NEW) ? 0 : row1.getStatus().equals(Status.ACTIVE) ? 1 : 2;
            Integer orderedStatus2 = row2.getStatus().equals(Status.NEW) ? 0 : row2.getStatus().equals(Status.ACTIVE) ? 1 : 2;
            return orderedStatus1.compareTo(orderedStatus2);
          }
        };
      }
    };
    TableColumn<RowItem,?> valueCol = new RealColumn<RowItem>(
      "Value", "The row's value", ADVANCED_COLUMNS_GROUP, 3) {
      @Override
      public Double getCellValue(RowItem row) { return row.getValue(); }
    };
    TableColumn<RowItem,?> urlCol = new TextColumn<RowItem>(
      "URL", "The row's URL", ADVANCED_SPECIAL_COLUMNS_GROUP) {
      @Override
      public String getCellValue(RowItem row) { return row.getUrl().toString(); }

      @Override
      protected Comparator<RowItem> getAscendingComparator()
      {
        return new Comparator<RowItem>() {
          public int compare(RowItem row1, RowItem row2)
          {
            return row1.getUrl().toString().compareTo(row2.getUrl().toString());
          }
        };
      }

    };
    columns.add(idCol);
    columns.add(nameCol);
    columns.add(statusCol);
    columns.add(valueCol);
    columns.add(urlCol);
    return columns;
  }


  /**
   * Tests resetting TableColumnManager with an entirely new set of columns
   */
  public void testSetColumns()
  {
    assertTrue("initial columns",
               _columns.equals(_columnManager.getAllColumns()));
    assertTrue("initial visible columns",
               _columns.equals(_columnManager.getVisibleColumns()));

    List<TableColumn<RowItem,?>> subList = _columns.subList(1, _columns.size() - 1);

    _columnManager.setColumns(subList);
    assertTrue("new columns",
               subList.equals(_columnManager.getAllColumns()));
    assertTrue("new visible",
               subList.equals(_columnManager.getVisibleColumns()));
   }

  public void testColumnVisibility()
  {
    assertTrue("all columns initially visible",
               _columns.equals(_columnManager.getVisibleColumns()));

    _idCol.setVisible(false);
    assertTrue("column hidden, via column object update",
                !_columnManager.getVisibleColumns().contains(_idCol) &&
                _columnManager.getVisibleColumns().size() == _columns.size() - 1);

    _idCol.setVisible(true);
    assertTrue("column unhidden, via column object update",
                _columnManager.getVisibleColumns().contains(_idCol) &&
                _columns.size() == _columnManager.getVisibleColumns().size());

    TreeModel model = _columnManager.getColumnsTreeModel();
    SelectableColumnTreeNode node = (SelectableColumnTreeNode) model.getNodeById("0:0");
    assertEquals("using idCol node", node.getColumn(), _idCol);
    node.setChecked(false);
    assertTrue("column hidden, via columns selector",
                !_columnManager.getVisibleColumns().contains(_idCol) &&
                _columnManager.getVisibleColumns().size() == _columns.size() - 1);

    node.setChecked(true);
    assertTrue("column unhidden, via columns selector",
                _columnManager.getVisibleColumns().contains(_idCol) &&
                _columns.size() == _columnManager.getVisibleColumns().size());

    // TODO: test that hidden column does not remain the sort column
  }

  public void testColumnGroups()
  {
    TreeModel model = _columnManager.getColumnsTreeModel();
    assertEquals("root node name", "Columns", model.getNodeById("0").getDescription());
    assertEquals("id column", _idCol.getName(), model.getNodeById("0:0").getDescription());
    assertEquals("basic group", BASIC_COLUMNS_GROUP, model.getNodeById("0:1").getDescription());
    assertEquals("name column", _nameCol.getName(), model.getNodeById("0:1:0").getDescription());
    assertEquals("advanced", ADVANCED_COLUMNS_GROUP, model.getNodeById("0:2").getDescription());
    assertEquals("status column", _statusCol.getName(), model.getNodeById("0:2:0").getDescription());
    assertEquals("value column", _valueCol.getName(), model.getNodeById("0:2:1").getDescription());
    assertEquals("advanced:special", "Special", model.getNodeById("0:2:2").getDescription());
    assertEquals("url column", _urlCol.getName(), model.getNodeById("0:2:2:0").getDescription());
  }

  public void testSortColumnSelection()
  {
    assertEquals("initial sort column", _idCol, _columnManager.getSortColumn());
    assertEquals("initial sort column via UISelectBean", _idCol, _columnManager.getSortColumnSelector().getSelection());
    assertEquals("initial sort direction", SortDirection.ASCENDING, _columnManager.getSortDirection());
    assertEquals("initial sort direction via UISelectBean", SortDirection.ASCENDING, _columnManager.getSortDirectionSelector().getSelection());
    _columnManager.setSortColumn(_valueCol);
    assertEquals("new sort column", _valueCol, _columnManager.getSortColumn());
    assertEquals("new sort column in UISelectBean", _valueCol, _columnManager.getSortColumnSelector().getSelection());
    assertEquals("new sort column in callback", _valueCol, _currentSortColumn);
    assertEquals("unchanged sort direction", SortDirection.ASCENDING, _columnManager.getSortDirection());
    assertEquals("unchanged sort direction in UISelectBean", SortDirection.ASCENDING, _columnManager.getSortDirectionSelector().getSelection());
    assertEquals("unchanged sort direction in callback", SortDirection.ASCENDING, _currentSortDirection);
    _columnManager.setSortDirection(SortDirection.DESCENDING);
    assertEquals("unchanged sort column", _valueCol, _columnManager.getSortColumn());
    assertEquals("unchanged sort column in UISelectBean", _valueCol, _columnManager.getSortColumnSelector().getSelection());
    assertEquals("unchanged sort column in callback", _valueCol, _currentSortColumn);
    assertEquals("new sort direction", SortDirection.DESCENDING, _columnManager.getSortDirection());
    assertEquals("new sort direction in UISelectBean", SortDirection.DESCENDING, _columnManager.getSortDirectionSelector().getSelection());
    assertEquals("new sort direction in callback", SortDirection.DESCENDING, _currentSortDirection);
    _columnManager.getSortColumnSelector().setSelection(_nameCol);
    assertEquals("new sort column via selector update", _nameCol, _columnManager.getSortColumn());
    assertEquals("new sort column in UISelectBean via selector update", _nameCol, _columnManager.getSortColumnSelector().getSelection());
    assertEquals("new sort column in callback via selector update", _nameCol, _currentSortColumn);
    assertEquals("unchanged sort direction via selector update", SortDirection.DESCENDING, _columnManager.getSortDirection());
    assertEquals("unchanged sort direction in UISelectBean via selector update", SortDirection.DESCENDING, _columnManager.getSortDirectionSelector().getSelection());
    assertEquals("unchanged sort direction in callback via selector update", SortDirection.DESCENDING, _currentSortDirection);
    _columnManager.getSortDirectionSelector().setSelection(SortDirection.ASCENDING);
    assertEquals("unchanged sort column via selector update", _nameCol, _columnManager.getSortColumn());
    assertEquals("unchanged sort column in UISelectBean via selector update", _nameCol, _columnManager.getSortColumnSelector().getSelection());
    assertEquals("unchanged sort column in callback via selector update", _nameCol, _currentSortColumn);
    assertEquals("new sort direction via selector update", SortDirection.ASCENDING, _columnManager.getSortDirection());
    assertEquals("new sort direction in UISelectBean via selector update", SortDirection.ASCENDING, _columnManager.getSortDirectionSelector().getSelection());
    assertEquals("new sort direction in callback via selector update", SortDirection.ASCENDING, _currentSortDirection);
  }

  public void testSingleColumnSort()
  {
    // ensure that first update to sort order forces a change; TableSortManager
    // only invokes sortChanged() callback if sort column or direction changes
    List<TableColumn<RowItem,?>> columnsTestOrder = new ArrayList<TableColumn<RowItem,?>>(_columns);
    Collections.rotate(columnsTestOrder, 1);

    int i = 0;
    for (TableColumn<RowItem,?> col : columnsTestOrder) {
      // ensure sortDir always changes from previous sort
      SortDirection sortDir = i++ % 2 == 0 ? SortDirection.DESCENDING : SortDirection.ASCENDING;
      doTestSort(col, sortDir);
    }
  }

  public void testCompoundColumnSort()
  {
    List<TableColumn<RowItem,?>> compoundSort = new ArrayList<TableColumn<RowItem,?>>();
    compoundSort.add(_statusCol);
    compoundSort.add(_nameCol);
    _columnManager.addCompoundSortColumns(compoundSort);

    _sortedData = null;
    TableColumn<RowItem,?> primarySortCol = _statusCol;
    _columnManager.setSortColumn(primarySortCol);
    _columnManager.setSortDirection(SortDirection.ASCENDING);
    assertNotNull("compound sort occurred on " + primarySortCol.getName() + " " + SortDirection.ASCENDING, _sortedData);
    assertEquals("compound sort row 0", 4, _sortedData.get(0).getId().intValue());
    assertEquals("compound sort row 1", 1, _sortedData.get(1).getId().intValue());
    assertEquals("compound sort row 2", 3, _sortedData.get(2).getId().intValue());
    assertEquals("compound sort row 3", 2, _sortedData.get(3).getId().intValue());

    _sortedData = null;
    _columnManager.setSortDirection(SortDirection.DESCENDING);
    assertNotNull("compound sort occurred on " + primarySortCol.getName() + " " + SortDirection.DESCENDING, _sortedData);
    assertEquals("compound sort row 0", 2, _sortedData.get(0).getId().intValue());
    assertEquals("compound sort row 1", 1, _sortedData.get(1).getId().intValue());
    assertEquals("compound sort row 2", 3, _sortedData.get(2).getId().intValue());
    assertEquals("compound sort row 3", 4, _sortedData.get(3).getId().intValue());
  }


  // private methods

  private void doTestSort(TableColumn<RowItem,?> sortCol, SortDirection sortDir)
  {
    _sortedData = null;
    _columnManager.setSortColumn(sortCol);
    assertSorted(sortCol, _currentSortDirection);
    _sortedData = null;
    _columnManager.setSortDirection(sortDir);
    assertSorted(sortCol, sortDir);
  }

  @SuppressWarnings("unchecked")
  private void assertSorted(TableColumn<RowItem,?> sortCol, SortDirection sortDir)
  {
    assertNotNull("sort occurred on " + sortCol.getName() + " " + sortDir, _sortedData);
    for (int i = 0; i < _sortedData.size(); ++i) {
      log.debug("row " + i +" : " + _sortedData.get(i));
    }
    for (int i = 0; i < _sortedData.size() - 1; ++i) {
      assertRowPairSorted(i, sortCol, sortDir);
    }
  }

  private void assertRowPairSorted(int i, TableColumn<RowItem,?> sortCol, SortDirection sortDir)
  {
    RowItem prevRow = _sortedData.get(i);
    RowItem currRow = _sortedData.get(i + 1);
    int cmpResult = sortCol.getComparator(sortDir).compare(prevRow, currRow);
    assertTrue("row " + i + " sorted by " + sortCol.getName() + " " + sortDir,
               cmpResult <= 0);
  }
}

