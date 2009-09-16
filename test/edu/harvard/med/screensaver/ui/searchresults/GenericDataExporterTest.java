// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.accesspolicy.UnrestrictedDataAccessPolicy;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import org.apache.log4j.Logger;

public class GenericDataExporterTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(GenericDataExporterTest.class);

  public void testGenericDataExporter() throws Exception
  {
    final Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(library);

    WellSearchResults wellSearchResults = new WellSearchResults(genericEntityDao, new UnrestrictedDataAccessPolicy(), null, null, null, Collections.<DataExporter<?>>emptyList());
    GenericDataExporter<Well> exporter = (GenericDataExporter<Well>) wellSearchResults.getDataExporters().get(0);
    wellSearchResults.searchAllWells();
    wellSearchResults.searchCommandListener(null); // invoke search now (necessary when using searchAllWells(), above

    TableColumn<Well,String> wellColumn = (TableColumn<Well,String>) wellSearchResults.getColumnManager().getColumn("Well");
    wellColumn.addCriterion(new Criterion<String>(Operator.TEXT_STARTS_WITH, "B"));
    wellSearchResults.getColumnManager().setSortColumn(wellColumn);
    wellSearchResults.getColumnManager().setSortDirection(SortDirection.DESCENDING);
    wellSearchResults.getColumnManager().getColumn("Library").setVisible(false);
    wellSearchResults.getColumnManager().getColumn("Compound SMILES").setVisible(true);
    wellSearchResults.getColumnManager().getColumn("PubChem CIDs").setVisible(true);
    exporter.setTableColumns(wellSearchResults.getColumnManager().getVisibleColumns());
    InputStream exportedData = exporter.export(wellSearchResults.getDataTableModel());
    Workbook workbook = Workbook.getWorkbook(exportedData);
    Sheet sheet = workbook.getSheet(0);
    Cell[] row = sheet.getRow(0);
    assertEquals("row count", 24 + 1, sheet.getRows());
    assertEquals("column 0 header", "Plate", row[0].getContents());
    assertEquals("column 1 header", "Well", row[1].getContents());
    assertEquals("column 2 header", "Screen Type", row[2].getContents());
    assertEquals("column 3 header", "Library Well Type", row[3].getContents());
    assertEquals("column 4 header", "Vendor", row[4].getContents());
    assertEquals("column 5 header", "Vendor ID", row[5].getContents());
    assertEquals("column 6 header", "Compound SMILES", row[6].getContents());
    assertEquals("column 7 header", "PubChem CIDs", row[7].getContents());
    for (int rowIndex = 1; rowIndex <= 24; ++rowIndex) {
      assertEquals("filtered, sorted well column desc; rowIndex=" + rowIndex,
                   String.format("B%02d", 25 - rowIndex),
                   sheet.getCell(1, rowIndex).getContents());
    }
  }
  
  public void testMultipleSheetExport() throws Exception 
  {
    DataTableModel<String> model = new DataTableModel<String>() {
      private int _rowIndex;
      @Override public void fetch(List columns) {}
      @Override public void filter(List filterColumns) {}
      @Override public DataTableModelType getModelType() { return DataTableModelType.IN_MEMORY; }
      @Override public void sort(List sortColumns, SortDirection sortDirection) {}
      @Override public int getRowCount() { return (65536 - 1) * 3; }
      @Override public Object getRowData() { return Integer.toString(_rowIndex); }
      @Override public int getRowIndex() { return _rowIndex; }
      @Override public Object getWrappedData() { return null; }
      @Override public boolean isRowAvailable() { return _rowIndex >= 0 && _rowIndex < getRowCount(); }
      @Override public void setRowIndex(int rowIndex) { _rowIndex = rowIndex; }
      @Override public void setWrappedData(Object arg0) {}
    };
    GenericDataExporter<String> exporter = new GenericDataExporter<String>("test");
    class TestColumn extends TextColumn<String> {
      public TestColumn() { super("column", "", ""); }
      @Override
      public String getCellValue(String row)
      {
        return row;
      }
    };
    List<TableColumn<String,?>> columns = new ArrayList<TableColumn<String,?>>();
    columns.add(new TestColumn());
    exporter.setTableColumns(columns);
    InputStream exportedData = exporter.export(model);
    Workbook workbook = Workbook.getWorkbook(exportedData);
    assertEquals("sheet count", 3, workbook.getNumberOfSheets());
    assertEquals("sheet 1 row count", 65536, workbook.getSheet(0).getRows());
    assertEquals("sheet 2 row count", 65536, workbook.getSheet(1).getRows());
    assertEquals("sheet 3 row count", 65536, workbook.getSheet(2).getRows());
    assertEquals("smooth carry over to next sheet (no missing values)", 
                 Integer.parseInt(workbook.getSheet(0).getCell(0, 65535).getContents()) + 1,
                 Integer.parseInt(workbook.getSheet(1).getCell(0, 1).getContents()));
  }

}
