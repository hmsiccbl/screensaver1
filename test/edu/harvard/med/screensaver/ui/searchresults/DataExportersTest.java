// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.policy.DefaultEntityViewPolicy;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TextColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.BooleanTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.FetchPaths;
import edu.harvard.med.screensaver.ui.table.column.entity.HasFetchPaths;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerSetTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerTupleColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextTupleColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

public class DataExportersTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(DataExportersTest.class);
  private List<TableColumn<Tuple<String>,?>> _columns;
  private List<Tuple<String>> _expectedData;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();

    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(library);

    _columns = Lists.newArrayList();
    RelationshipPath<Well> relPath = Well.latestReleasedReagent;
    _columns.add(new IntegerTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("plateNumber"), "Plate", "", ""));
    _columns.add(new TextTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("wellName"), "Well", "", ""));
    _columns.add(new EnumTupleColumn<Well,String,ScreenType>(Well.library.toProperty("screenType"), "Screen Type", "", "", ScreenType.values()));
    _columns.add(new EnumTupleColumn<Well,String,LibraryWellType>(RelationshipPath.from(Well.class).toProperty("libraryWellType"), "Library Well Type", "", "", LibraryWellType.values()));
    _columns.add(new TextTupleColumn<Well,String>(relPath.to(Reagent.vendorName), "Reagent Vendor", "", ""));
    _columns.add(new TextTupleColumn<Well,String>(relPath.to(Reagent.vendorIdentifier), "Reagent ID", "", ""));
    _columns.add(new TextTupleColumn<Well,String>(relPath.toProperty("smiles"), "Compound SMILES", "", ""));
    _columns.add(new IntegerSetTupleColumn<Well,String>(relPath.to(SmallMoleculeReagent.pubchemCids), "PubChem CIDs", "", ""));
    _columns.add(new BooleanTupleColumn<Well,String>(RelationshipPath.from(Well.class).toProperty("deprecated"), "Is Deprecated", "", ""));
    TextTupleColumn<Well,String> wellColumn = (TextTupleColumn<Well,String>) _columns.get(1);

    TupleDataFetcher<Well,String> dataFetcher = new TupleDataFetcher<Well,String>(Well.class, genericEntityDao);
    dataFetcher.setPropertiesToFetch(FetchPaths.<Well,Tuple<String>>getPropertyPaths(_columns));
    List<Criterion<String>> criteria = ImmutableList.of(new Criterion<String>(Operator.TEXT_STARTS_WITH, "B"));
    Map<PropertyPath<Well>,List<? extends Criterion<?>>> map = ImmutableMap.<PropertyPath<Well>,List<? extends Criterion<?>>>of(wellColumn.getPropertyPath(), criteria);
    dataFetcher.setFilteringCriteria(map);
    dataFetcher.setOrderBy(ImmutableList.of(wellColumn.getPropertyPath()));
    
    List<String> keys = dataFetcher.findAllKeys();
    Map<String,Tuple<String>> data = dataFetcher.fetchData(Sets.newHashSet(keys));
    _expectedData = Lists.newArrayList();
    Collections.reverse(keys);
    for (String string : keys) {
      _expectedData.add(data.get(string));
    }
  }

  public void testExcelWorkbookDataExporter() throws Exception
  {
    ExcelWorkbookDataExporter<Tuple<String>> dataExporter = new ExcelWorkbookDataExporter<Tuple<String>>("test");
    dataExporter.setTableColumns(_columns);

    Iterator<Tuple<String>> wellTuples = _expectedData.iterator();
    InputStream exportedData = dataExporter.export(wellTuples);
    Workbook workbook = Workbook.getWorkbook(exportedData);
    Sheet sheet = workbook.getSheet(0);
    Cell[] row = sheet.getRow(0);
    assertEquals("row count", 24 + 1, sheet.getRows());
    assertEquals("column 0 header", "Plate", row[0].getContents());
    assertEquals("column 1 header", "Well", row[1].getContents());
    assertEquals("column 2 header", "Screen Type", row[2].getContents());
    assertEquals("column 3 header", "Library Well Type", row[3].getContents());
    assertEquals("column 4 header", "Reagent Vendor", row[4].getContents());
    assertEquals("column 5 header", "Reagent ID", row[5].getContents());
    assertEquals("column 6 header", "Compound SMILES", row[6].getContents());
    assertEquals("column 7 header", "PubChem CIDs", row[7].getContents());
    assertEquals("column 8 header", "Is Deprecated", row[8].getContents());
    for (int rowIndex = 1; rowIndex <= 24; ++rowIndex) {
      for (int colIndex = 0; colIndex < 5; ++colIndex) {
        assertEquals("filtered, sorted well column desc; rowIndex=" + rowIndex + ", column=" + _columns.get(colIndex).getName(),
                     _expectedData.get(rowIndex - 1).getProperty(TupleDataFetcher.makePropertyKey(((HasFetchPaths) _columns.get(colIndex)).getPropertyPath())).toString(),
                     sheet.getCell(colIndex, rowIndex).getContents().toString());
      }
    }
  }
  
  public void testCsvDataExporter() throws Exception
  {
    CsvDataExporter<Tuple<String>> dataExporter = new CsvDataExporter<Tuple<String>>("test");
    dataExporter.setTableColumns(_columns);

    Iterator<Tuple<String>> wellTuples = _expectedData.iterator();
    InputStream exportedData = dataExporter.export(wellTuples);
    BufferedInputStream bis = new BufferedInputStream(exportedData);
    BufferedReader in = new BufferedReader(new InputStreamReader(bis));
    String[] row = nextRow(in);
    assertEquals("column 0 header", "Plate", row[0]);
    assertEquals("column 1 header", "Well", row[1]);
    assertEquals("column 2 header", "Screen Type", row[2]);
    assertEquals("column 3 header", "Library Well Type", row[3]);
    assertEquals("column 4 header", "Reagent Vendor", row[4]);
    assertEquals("column 5 header", "Reagent ID", row[5]);
    assertEquals("column 6 header", "Compound SMILES", row[6]);
    assertEquals("column 7 header", "PubChem CIDs", row[7]);
    assertEquals("column 8 header", "Is Deprecated", row[8]);

    int rowIndex = 0;
    while ((row = nextRow(in)) != null) {
      for (int colIndex = 0; colIndex < 5; ++colIndex) {
        assertEquals("filtered, sorted well column desc; rowIndex=" + rowIndex + ", column=" + _columns.get(colIndex).getName(),
                     _expectedData.get(Integer.valueOf(rowIndex)).getProperty(TupleDataFetcher.makePropertyKey(((HasFetchPaths) _columns.get(colIndex)).getPropertyPath())).toString(),
                     row[colIndex]);
      }
      ++rowIndex;
    }
  }

  private String[] nextRow(BufferedReader in) throws IOException
  {
    String line = in.readLine();
    if (line == null) { return null; }
    return line.split(",");
  }

  public void testExcelWorkbookExporterMultipleSheetExport() throws Exception
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
      @Override public Iterator<String> iterator() { return new Iterator<String>() {
        private int row = 0;

        @Override
        public boolean hasNext()
        {
          return row < getRowCount();
        }

        @Override
        public String next()
        {
          setRowIndex(row++);
          return (String) getRowData();
        }

        @Override
        public void remove()
        {
          throw new UnsupportedOperationException();
        }}; 
      }
    };
    ExcelWorkbookDataExporter<String> exporter = new ExcelWorkbookDataExporter<String>("test");
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
    InputStream exportedData = exporter.export(model.iterator());
    Workbook workbook = Workbook.getWorkbook(exportedData);
    assertEquals("sheet count", 3, workbook.getNumberOfSheets());
    assertEquals("sheet 1 row count", 65536, workbook.getSheet(0).getRows());
    assertEquals("sheet 2 row count", 65536, workbook.getSheet(1).getRows());
    assertEquals("sheet 3 row count", 65536, workbook.getSheet(2).getRows());
    assertEquals("smooth carry over to next sheet (no missing values)", 
                 Integer.parseInt(workbook.getSheet(0).getCell(0, 65535).getContents()) + 1,
                 Integer.parseInt(workbook.getSheet(1).getCell(0, 1).getContents()));
  }

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws Exception
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    
    app.addCommandLineOption(OptionBuilder.isRequired().hasArg().withArgName("output workbook file").create("f"));
    if (!app.processOptions(true, false, true)) {
      System.exit(1);
    }
    GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    WellSearchResults searchResults = new WellSearchResults(dao,
                                                            null,
                                                            new DefaultEntityViewPolicy(),
                                                            null, null, null, null,
                                                            Lists.<DataExporter<Tuple<String>>>newArrayList(new ExcelWorkbookDataExporter<Tuple<String>>("wells")));
    ScreenResult screenResult = dao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), 974, true, Screen.screenResult.to(ScreenResult.dataColumns).getPath()).getScreenResult();
    searchResults.searchWellsForScreenResult(screenResult);
    searchResults.getRowCount(); // force initial data fetch

    ExcelWorkbookDataExporter<Tuple<String>> dataExporter = (ExcelWorkbookDataExporter<Tuple<String>>) searchResults.getDataExporterSelector().getDefaultSelection();

    log.debug("starting exporting data for download");
    dataExporter.setTableColumns(searchResults.getColumnManager().getVisibleColumns());
    InputStream inputStream = dataExporter.export(searchResults.getDataTableModel().iterator());
    log.debug("finished exporting data for download");

    File file = app.getCommandLineOptionValue("f", File.class);

    OutputStream outputStream = new FileOutputStream(file);
    IOUtils.copy(inputStream, outputStream);
    outputStream.close();
    log.info("export completed");
  }

}
