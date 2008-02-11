// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.ui.searchresults.TextColumn;

import org.apache.log4j.Logger;

public class VisibleTableColumnModelTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(VisibleTableColumnModelTest.class);


  // instance data members

  public static class Row {
    String a;
    String b;
    String c;
  }

  private List<TableColumn<Row,?>> _columns;
  private VisibleTableColumnModel<Row> _columnModel;
  private Map<String,Boolean> _isVisible = new HashMap<String,Boolean>();

  // public constructors and methods

  @Override
  protected void setUp() throws Exception
  {
    _columns = new ArrayList<TableColumn<Row,?>>();
    _isVisible.put("A", true);
    _isVisible.put("B", true);
    _isVisible.put("C", true);
    _columns.add(new TextColumn<Row>("A", "") {
      @Override
      public String getCellValue(Row row) { return row.a; }
      public boolean isVisible() { return _isVisible.get(getName()); }
    });
    _columns.add(new TextColumn<Row>("B", "") {
      @Override
      public String getCellValue(Row row) { return row.b; }
      public boolean isVisible() { return _isVisible.get(getName()); }
    });
    _columns.add(new TextColumn<Row>("C", "") {
      @Override
      public String getCellValue(Row row) { return row.c; }
      public boolean isVisible() { return _isVisible.get(getName()); }
    });
    _columnModel = new VisibleTableColumnModel<Row>(_columns);
  }

  @SuppressWarnings("unchecked")
  public void testVisibleTableColumnModel()
  {
    assertEquals("all visible column count", 3, _columnModel.getRowCount());
    assertEquals("all visible column count", 3, ((List<TableColumn<Row,?>>) _columnModel.getWrappedData()).size());
    _columnModel.setRowIndex(0);
    assertEquals("all visible, column 0", _columns.get(0), _columnModel.getRowData());
    _columnModel.setRowIndex(1);
    assertEquals("all visible, column 1", _columns.get(1), _columnModel.getRowData());
    _columnModel.setRowIndex(2);
    assertEquals("all visible, column 3", _columns.get(2), _columnModel.getRowData());

    _isVisible.put("A", false);
    _columnModel.updateVisibleColumns();
    assertEquals("A invisible column count", 2, _columnModel.getRowCount());
    assertEquals("A invisible column count", 2, ((List<TableColumn<Row,?>>) _columnModel.getWrappedData()).size());
    assertEquals("A invisible row index", 1, _columnModel.getRowIndex());
    _columnModel.setRowIndex(0);
    assertEquals("A invisible, column 0", _columns.get(1), _columnModel.getRowData());
    _columnModel.setRowIndex(1);
    assertEquals("A invisible, column 1", _columns.get(2), _columnModel.getRowData());
    _columnModel.setRowIndex(2);
    assertFalse("A invisible, column 2 not available", _columnModel.isRowAvailable());

    _isVisible.put("B", false);
    _columnModel.updateVisibleColumns();
    assertEquals("A,B invisible column count", 1, _columnModel.getRowCount());
    assertEquals("A,B invisible column count", 1, ((List<TableColumn<Row,?>>) _columnModel.getWrappedData()).size());
    assertEquals("A,B invisible row index", 0, _columnModel.getRowIndex());
    assertEquals("A,B invisible, column 0", _columns.get(2), _columnModel.getRowData());
    _columnModel.setRowIndex(1);
    assertFalse("A,B invisible, column 1 not available", _columnModel.isRowAvailable());

    _isVisible.put("C", false);
    _columnModel.updateVisibleColumns();
    assertEquals("all invisible column count", 0, _columnModel.getRowCount());
    assertEquals("all invisible column count", 0, ((List<TableColumn<Row,?>>) _columnModel.getWrappedData()).size());
    assertEquals("all invisible row index", -1, _columnModel.getRowIndex());
    _columnModel.setRowIndex(1);
    assertFalse("all invisible, column 0 not available", _columnModel.isRowAvailable());
}

  // private methods

}

