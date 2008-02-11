// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import edu.harvard.med.screensaver.ui.searchresults.EnumColumn;
import edu.harvard.med.screensaver.ui.searchresults.IntegerColumn;
import edu.harvard.med.screensaver.ui.searchresults.RealColumn;
import edu.harvard.med.screensaver.ui.searchresults.TextColumn;

import org.apache.log4j.Logger;

public class TableSortManagerTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(TableSortManagerTest.class);


  // instance data members

  private TableSortManager<RowItem> _sortManager;
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
    _columns = new ArrayList<TableColumn<RowItem,?>>();
    _idCol = new IntegerColumn<RowItem>("ID", "The row's ID") {
      @Override
      public Integer getCellValue(RowItem row) { return row.getId(); }
    };
    _nameCol = new TextColumn<RowItem>("Name", "The row's name") {
      @Override
      public String getCellValue(RowItem row) { return row.getName(); }
    };
    _statusCol = new EnumColumn<RowItem,Status>("Status", "The row's status", Status.values()) {
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
    _valueCol = new RealColumn<RowItem>("Value", "The row's value") {
      @Override
      public Double getCellValue(RowItem row) { return row.getValue(); }
    };
    _urlCol = new TextColumn<RowItem>("URL", "The row's URL") {
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
    _columns.add(_idCol);
    _columns.add(_nameCol);
    _columns.add(_statusCol);
    _columns.add(_valueCol);
    _columns.add(_urlCol);
    _sortManager = new TableSortManager<RowItem>(_columns);
    _sortManager.addObserver(new Observer() {
      @SuppressWarnings("unchecked")
      public void update(Observable o, Object obj)
      {
        SortChangedEvent<RowItem> event = (SortChangedEvent<RowItem>) obj;
        if (event.getColumn() != null) {
          _currentSortColumn = event.getColumn();
        }
        if (event.getDirection() != null) {
          _currentSortDirection = event.getDirection();
        }
        _sortedData = new ArrayList<RowItem>(_unsortedData);
        Collections.sort(_sortedData, _sortManager.getSortColumnComparator());
      }
    });

    _currentSortColumn = _sortManager.getSortColumn();
    _currentSortDirection = _sortManager.getSortDirection();

    _unsortedData = new ArrayList<RowItem>();
    // Note: test data rows must be initially unsorted by every column!
    _unsortedData.add(new RowItem(1, "A", Status.ACTIVE, 3.5, new URL("https://screensaver.med.harvard.edu/screensaver")));
    _unsortedData.add(new RowItem(2, "D", Status.DEAD, 2.0, new URL("http://www.java.sun.com")));
    _unsortedData.add(new RowItem(4, "B", Status.NEW, -3.0, new URL("http://www.hibernate.org")));
    _unsortedData.add(new RowItem(3, "C", Status.ACTIVE, 0.0, new URL("http://www.springframework.org")));

  }

  public void testCurrentSortColumn()
  {
    assertEquals("initial sort column", _idCol, _sortManager.getSortColumn());
    assertEquals("initial sort column via UISelectBean", _idCol, _sortManager.getSortColumnSelector().getSelection());
    assertEquals("initial sort direction", SortDirection.ASCENDING, _sortManager.getSortDirection());
    assertEquals("initial sort direction via UISelectBean", SortDirection.ASCENDING, _sortManager.getSortDirectionSelector().getSelection());
    _sortManager.setSortColumn(_valueCol);
    assertEquals("new sort column", _valueCol, _sortManager.getSortColumn());
    assertEquals("new sort column in UISelectBean", _valueCol, _sortManager.getSortColumnSelector().getSelection());
    assertEquals("new sort column in callback", _valueCol, _currentSortColumn);
    assertEquals("unchanged sort direction", SortDirection.ASCENDING, _sortManager.getSortDirection());
    assertEquals("unchanged sort direction in UISelectBean", SortDirection.ASCENDING, _sortManager.getSortDirectionSelector().getSelection());
    assertEquals("unchanged sort direction in callback", SortDirection.ASCENDING, _currentSortDirection);
    _sortManager.setSortDirection(SortDirection.DESCENDING);
    assertEquals("unchanged sort column", _valueCol, _sortManager.getSortColumn());
    assertEquals("unchanged sort column in UISelectBean", _valueCol, _sortManager.getSortColumnSelector().getSelection());
    assertEquals("unchanged sort column in callback", _valueCol, _currentSortColumn);
    assertEquals("new sort direction", SortDirection.DESCENDING, _sortManager.getSortDirection());
    assertEquals("new sort direction in UISelectBean", SortDirection.DESCENDING, _sortManager.getSortDirectionSelector().getSelection());
    assertEquals("new sort direction in callback", SortDirection.DESCENDING, _currentSortDirection);
    _sortManager.getSortColumnSelector().setSelection(_nameCol);
    assertEquals("new sort column via selector update", _nameCol, _sortManager.getSortColumn());
    assertEquals("new sort column in UISelectBean via selector update", _nameCol, _sortManager.getSortColumnSelector().getSelection());
    assertEquals("new sort column in callback via selector update", _nameCol, _currentSortColumn);
    assertEquals("unchanged sort direction via selector update", SortDirection.DESCENDING, _sortManager.getSortDirection());
    assertEquals("unchanged sort direction in UISelectBean via selector update", SortDirection.DESCENDING, _sortManager.getSortDirectionSelector().getSelection());
    assertEquals("unchanged sort direction in callback via selector update", SortDirection.DESCENDING, _currentSortDirection);
    _sortManager.getSortDirectionSelector().setSelection(SortDirection.ASCENDING);
    assertEquals("unchanged sort column via selector update", _nameCol, _sortManager.getSortColumn());
    assertEquals("unchanged sort column in UISelectBean via selector update", _nameCol, _sortManager.getSortColumnSelector().getSelection());
    assertEquals("unchanged sort column in callback via selector update", _nameCol, _currentSortColumn);
    assertEquals("new sort direction via selector update", SortDirection.ASCENDING, _sortManager.getSortDirection());
    assertEquals("new sort direction in UISelectBean via selector update", SortDirection.ASCENDING, _sortManager.getSortDirectionSelector().getSelection());
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
    _sortManager.addCompoundSortColumns(compoundSort);

    _sortedData = null;
    TableColumn<RowItem,?> primarySortCol = _statusCol;
    _sortManager.setSortColumn(primarySortCol);
    _sortManager.setSortDirection(SortDirection.ASCENDING);
    assertNotNull("compound sort occurred on " + primarySortCol.getName() + " " + SortDirection.ASCENDING, _sortedData);
    assertEquals("compound sort row 0", 4, _sortedData.get(0).getId().intValue());
    assertEquals("compound sort row 1", 1, _sortedData.get(1).getId().intValue());
    assertEquals("compound sort row 2", 3, _sortedData.get(2).getId().intValue());
    assertEquals("compound sort row 3", 2, _sortedData.get(3).getId().intValue());

    _sortedData = null;
    _sortManager.setSortDirection(SortDirection.DESCENDING);
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
    _sortManager.setSortColumn(sortCol);
    assertSorted(sortCol, _currentSortDirection);
    _sortedData = null;
    _sortManager.setSortDirection(sortDir);
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
    int cmpResult = sortCol.getAscendingComparator().compare(prevRow, currRow);
    assertTrue("row " + i + " sorted by " + sortCol.getName() + " " + sortDir,
               sortDir.equals(SortDirection.ASCENDING) ? (cmpResult <= 0) : (cmpResult >= 0));
  }
}

