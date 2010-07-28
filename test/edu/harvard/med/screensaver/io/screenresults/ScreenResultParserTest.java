// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import jxl.BooleanFormulaCell;
import jxl.CellType;
import jxl.NumberFormulaCell;
import jxl.Sheet;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.StringUtils;

public class ScreenResultParserTest extends AbstractSpringTest
{

  private static final Logger log = Logger.getLogger(ScreenResultParserTest.class);

  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/screenresults");
  public static final String SCREEN_RESULT_115_TEST_WORKBOOK_FILE = "ScreenResultTest115.xls";
  public static final String SCREEN_RESULT_117_TEST_WORKBOOK_FILE = "ScreenResultTest117.xls";
  public static final String SCREEN_RESULT_115_30_DATA_COLUMNS_TEST_WORKBOOK_FILE = "ScreenResultTest115_30DataColumns.xls";
  public static final String SCREEN_RESULT_MISSING_DERIVED_FROM_WORKBOOK_FILE = "ScreenResultTest115-missing-derived-from.xls";
  public static final String ERRORS_TEST_WORKBOOK_FILE = "ScreenResultErrorsTest.xls";
  public static final String FORMULA_VALUE_TEST_WORKBOOK_FILE = "formula_value.xls";
  public static final String BLANK_ROWS_TEST_WORKBOOK_FILE = "ScreenResultTest115_blank_rows.xls";
  public static final String HIT_COUNT_TEST_WORKBOOK_FILE = "ScreenResultHitCountTest.xls";


  protected ScreenResultParser mockScreenResultParser;

  protected void onSetUp() throws Exception
  {
    super.onSetUp();
  }

  protected void onTearDown() throws Exception
  {
    // TODO: delete *.error.xls files
  }

  /**
   * This is the primary test of the ScreenResultParser.
   */
  public void testParseScreenResult() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
    Date now = new Date();
    
    ScreenResult screenResult = mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115),
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    doTestScreenResult115ParseResult(screenResult);
  }
  
  /**
   * Tests basic usage of the JXL API to read an Excel spreadsheet, but does
   * not test Screensaver-related functionality. Basically, just a check that
   * the technology we're using actually works. Somewhat useful to keep around
   * in case we upgrade jar version, etc.
   */
  public void testReadExcelSpreadsheet() throws Exception
  {
    String[] expectedSheetNames = new String[] { "Screen Info", "Data Columns", "PL_00001", "PL_00003", "PL_00002" };
    String[] expectedHeaderRowValues = new String[] {
      "Plate",
      "Well",
      "Type",
      "Exclude",
      "Luminescence",
      "Luminescence",
      "FI_A",
      "FI_B",
      "Average",
      "PositiveIndicator2",
      "PositiveIndicator3" };

    InputStream xlsInStream = ScreenResultParserTest.class.getResourceAsStream(SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
    jxl.Workbook wb = jxl.Workbook.getWorkbook(xlsInStream);
    int nSheets = wb.getNumberOfSheets();
    assertEquals("worksheet count", expectedSheetNames.length, nSheets);
    for (int i = 0; i < nSheets; i++) {
      String sheetName = wb.getSheet(i).getName();
      assertEquals("worksheet " + i + " name", expectedSheetNames[i], sheetName);
      jxl.Sheet sheet = wb.getSheet(i);
      if (i >= 2) {
        for (int iCell = 0; iCell < sheet.getRow(0).length; ++iCell) {
          jxl.Cell cell = sheet.getCell(iCell, 0);
          assertEquals(expectedHeaderRowValues[iCell], cell.getContents());
        }
      }
    }
  }

  public void testParseNumericFormulaCellValue() throws Exception
  {
    // TODO: warning! getResourceAsStream() is looking in .eclipse.classes,
    // while our Workbook object is given a file in test/. Make sure the file is
    // the same in both places!
    InputStream xlsInStream =
      ScreenResultParserTest.class.getResourceAsStream(FORMULA_VALUE_TEST_WORKBOOK_FILE);
    jxl.Workbook wb = jxl.Workbook.getWorkbook(xlsInStream);
    Sheet sheet = wb.getSheet(0);
    jxl.Cell numericFormulaCell = sheet.getCell(3, 1);
    assertEquals("cell type",
                 CellType.NUMBER_FORMULA,
                 numericFormulaCell.getType());
    double numericValue = ((NumberFormulaCell) numericFormulaCell).getValue();
    assertEquals("numeric value", 2.133, numericValue, 0.0001);
    String formula = ((NumberFormulaCell) numericFormulaCell).getFormula();
    assertEquals("formula", "B2+C2", formula);
    assertEquals("numeric decimal places",
                 4,
                 ((NumberFormulaCell) numericFormulaCell).getNumberFormat().getMaximumFractionDigits());

//    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(TEST_INPUT_FILE_DIR, FORMULA_VALUE_TEST_WORKBOOK_FILE));
    Worksheet worksheet = workbook.getWorksheet(0);
//    Cell.Factory cellFactory = new Cell.Factory(workbook, 0, errors);
    Cell cell = worksheet.getCell(3,1, false);
    assertTrue(!cell.isEmpty());
    Double parsedNumericValue = cell.getDouble();
    assertEquals("parse numeric value",
                 numericValue,
                 parsedNumericValue.doubleValue(),
                 0.0001);

    // test numeric decimal places (TODO: should probably be a separate unit test)
    Cell numericFormatFormulaCell = worksheet.getCell(3,1, false);
    assertEquals("decimal places of numeric format on formula cell", 4,
                 numericFormatFormulaCell.getDoublePrecision());
//    Cell generalFormatFormulaCell = cellFactory.getCell((short) 4, (short) 1);
//    assertEquals("precision of general format on formula cell", -1,
//                 generalFormatFormulaCell.getDoublePrecision());
//    Cell generalFormatNumericCell = cellFactory.getCell((short) 1, (short) 1);
//    assertEquals("precision of general format on numeric cell", -1,
//                 generalFormatNumericCell.getDoublePrecision());
    Cell numericFormatNumericCell = worksheet.getCell(2, 1);
    assertEquals("decimal places of numeric format on numeric cell", 3,
                 numericFormatNumericCell.getDoublePrecision());
    Cell integerNumericFormatNumericCell = worksheet.getCell(5, 1);
    assertEquals("decimal places of integer number format on numeric cell", 0,
                 integerNumericFormatNumericCell.getDoublePrecision());
    Cell percentageNumericCell = worksheet.getCell(6, 1);
    assertEquals("decimal places of percentage number format on numeric cell", 3,
                 percentageNumericCell.getDoublePrecision());
  }

  public void testReadUndefinedCell() throws BiffException, IOException
  {
    InputStream xlsInStream =
      ScreenResultParserTest.class.getResourceAsStream(FORMULA_VALUE_TEST_WORKBOOK_FILE);
    jxl.Workbook wb = jxl.Workbook.getWorkbook(xlsInStream);
    Sheet sheet = wb.getSheet(0);
    try {
      sheet.getCell(0, 2);
      fail("expected ArrayIndexOutOfBoundsException");
    }
    catch (ArrayIndexOutOfBoundsException e) {}
    try {
      sheet.getCell(10, 0);
      fail("expected ArrayIndexOutOfBoundsException");
    }
    catch (ArrayIndexOutOfBoundsException e) {}
  }

  public void testDetectEmptyRow() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, BLANK_ROWS_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115), workbookFile);
    if (mockScreenResultParser.getHasErrors()) {
      log.debug("parse errors:\n" + StringUtils.makeListString(mockScreenResultParser.getErrors(), "\n"));
    }
    assertFalse("screen result had errors: " + mockScreenResultParser.getErrors(),
                mockScreenResultParser.getHasErrors());
    assertEquals("assay well count", 4, (int) mockScreenResultParser.getParsedScreenResult().getAssayWells().size());
  }

  // Note: this test was added to highlight a failure of the POI/HSSF library
  // design, which did not allow you to read a boolean-typed formula cell value. 
  // We now longer use this library, but it can't hurt to retain the test.
  public void testReadBooleanFormulaCellValue() throws Exception
  {
    InputStream xlsInStream =
      ScreenResultParserTest.class.getResourceAsStream(FORMULA_VALUE_TEST_WORKBOOK_FILE);
    jxl.Workbook wb = jxl.Workbook.getWorkbook(xlsInStream);
    Sheet sheet = wb.getSheet(0);

    // test boolean formula cell w/'general' format
    jxl.Cell booleanFormulaCell = sheet.getCell(7, 1);
    assertEquals("cell type",
                 CellType.BOOLEAN_FORMULA,
                 booleanFormulaCell.getType());
    boolean booleanValue = ((BooleanFormulaCell) booleanFormulaCell).getValue();
    assertEquals("boolean value", true, booleanValue);
    String formula = ((BooleanFormulaCell) booleanFormulaCell).getFormula();
    assertEquals("formula", "G2>0.01", formula);

    // test boolean formula cell w/explicit 'boolean' format
    booleanFormulaCell = sheet.getCell(8, 1);
    assertEquals("cell type",
                 CellType.BOOLEAN_FORMULA,
                 booleanFormulaCell.getType());
    booleanValue = ((BooleanFormulaCell) booleanFormulaCell).getValue();
    assertEquals("boolean value", true, booleanValue);
    formula = ((BooleanFormulaCell) booleanFormulaCell).getFormula();
    assertEquals("formula", "G2>0.01", formula);

    // test boolean formula cell, read via our own Cell class
//    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(TEST_INPUT_FILE_DIR,
                                              FORMULA_VALUE_TEST_WORKBOOK_FILE));
    Worksheet worksheet = workbook.getWorksheet(0);
//    Cell.Factory cellFactory = new Cell.Factory(workbook, 0, errors);
    Cell cell = worksheet.getCell(7, 1, false);
    assertNotNull(cell);
    assertEquals("parse boolean value",
                 booleanValue,
                 cell.getBoolean().booleanValue());
  }

  /**
   * Tests that screen result errors are saved to an annotated error workbook.
   */
  public void testErrorReporting() throws IOException, BiffException, WriteException
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115), workbookFile);
    List<WorkbookParseError> errors = mockScreenResultParser.getErrors();
    assertTrue(errors.contains(new ParseError("value required", "Data Columns:(B,3)")));
    assertTrue(errors.contains(new ParseError("illegal value", "Data Columns:(E,4)")));
    assertTrue(errors.contains(new ParseError("unparseable value \"paradiso positive indicator\" (expected one of [Boolean Positive Indicator, Numeric, Partition Positive Indicator, Text])", "Data Columns:(G,3)")));
    assertTrue(errors.contains(new ParseError("invalid Data Column worksheet column label 'B' (expected one of [E, F])", "Data Columns:(D,10)")));
    assertTrue(errors.contains(new ParseError("invalid Data Column worksheet column label 'H' (expected one of [E, F, G])", "Data Columns:(E,10)")));
    assertTrue(errors.contains(new ParseError("invalid Data Column worksheet column label 'D' (expected one of [E, F, G, H])", "Data Columns:(F,10)")));
    assertTrue(errors.contains(new ParseError("unparseable value \"follow-up\" (expected one of [, follow up, primary])", "Data Columns:(E,11)")));
  }

  /**
   * Tests that Cells are cloned when needed (a single Cell is generally
   * recycled, as an optimization). Note that this test assumes that the test
   * errorAnnotatedWorkbooks do not have more than 1 error per cell, which is a possibility in
   * the real world, but would break our naive test. (I suppose we could also
   * test simply that at least some of our ParseErrors' cells were different.)
   * @throws FileNotFoundException 
   */
  public void testRecycledCellUsage() throws FileNotFoundException
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115), workbookFile);
    Set<Cell> cellsWithErrors = new HashSet<Cell>();
    List<WorkbookParseError> errors = mockScreenResultParser.getErrors();
    for (WorkbookParseError error : errors) {
      assertFalse("every error assigned to distinct cell",
                  cellsWithErrors.contains(error.getCell()));
      cellsWithErrors.add(error.getCell());
    }
  }

  public void testParserReuse() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    mockScreenResultParser.parse(screen, workbookFile);
    List<WorkbookParseError> errors1 = mockScreenResultParser.getErrors();
    /*Screen*/ screen = MakeDummyEntities.makeDummyScreen(115);
    mockScreenResultParser.parse(screen, workbookFile);
    List<WorkbookParseError> errors2 = mockScreenResultParser.getErrors();
    assertTrue(errors1.size() > 0);
    assertTrue(errors2.size() > 0);
    assertEquals("errors not accumulating across multiple parse() calls", errors1, errors2);

    // now test reading yet another spreadsheet, for which we can test the parsed result
    testParseScreenResult();
   }

  public void testParseScreenResultIncremental() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    mockScreenResultParser.parse(screen,
                                 workbookFile,
                                 new IntRange(1, 2), 
                                 false);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());
    assertEquals(640, screen.getScreenResult().getAssayWells().size());
    mockScreenResultParser.parse(screen,
                                 workbookFile,
                                 new IntRange(3, 3),
                                 false);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());
    assertEquals(960, screen.getScreenResult().getAssayWells().size());
    
    doTestScreenResult115ParseResult(screen.getScreenResult());
    assertEquals(960, screen.getScreenResult().getAssayWells().size());
  }

  public void testPositivesCount() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, HIT_COUNT_TEST_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115),
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    int resultValues = 10;
    assertEquals("result value count",
                 resultValues,
                 screenResult.getDataColumnsList().get(0).getResultValues().size());
    int experimentalWells = 7;
    List<Integer> expectedHitCount = Arrays.asList(4, 2); // only experimental wells and non-excluded wells are considered
    List<Double> expectedHitRatio = Arrays.asList(expectedHitCount.get(0) / (double) experimentalWells,
                                                  expectedHitCount.get(1) / (double) experimentalWells);

    int iPositiveIndicatorCol = 0;
    for (DataColumn col : screenResult.getDataColumnsList()) {
      if (col.isPositiveIndicator()) {
        assertEquals("hit count", expectedHitCount.get(iPositiveIndicatorCol), col.getPositivesCount());
        assertEquals("hit ratio",
                     expectedHitRatio.get(iPositiveIndicatorCol).doubleValue(),
                     col.getPositivesRatio().doubleValue(),
                     0.01);
        ++iPositiveIndicatorCol;
      }
    }
  }

  public void testMultiCharColumnLabels() throws FileNotFoundException
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_30_DATA_COLUMNS_TEST_WORKBOOK_FILE);
    ScreenResult result = mockScreenResultParser.parse(screen,workbookFile);
    if (mockScreenResultParser.getHasErrors()) {
      log.debug("parse errors: " + mockScreenResultParser.getErrors());
    }
    assertFalse("screen result had no errors", mockScreenResultParser.getHasErrors());
    List<DataColumn> dataColumns = result.getDataColumnsList();
    assertEquals("DataColumn count", 30, dataColumns.size());
    for (int i = 0; i < 30 - 1; ++i) {
      DataColumn col = dataColumns.get(i);
      assertEquals("is derived from next", dataColumns.get(i+1), col.getDerivedTypes().first());
      Map<WellKey,ResultValue> resultValues = col.getWellKeyToResultValueMap();
      assertEquals(col.getName() + " result value 0", 1000.0 + i, resultValues.get(new WellKey(1, "A01")).getNumericValue());
      assertEquals(col.getName() + " result value 1", 2000.0 + i, resultValues.get(new WellKey(1, "A02")).getNumericValue());
      assertEquals(col.getName() + " result value 2", 3000.0 + i, resultValues.get(new WellKey(1, "A03")).getNumericValue());
    }
    assertTrue("last is not derived from any", dataColumns.get(30 - 1).getDerivedTypes().isEmpty());
  }
  
  public void testParseScreenResultWithChannels() throws Exception
  {
    /* including test for use "S", "time point ordinal" and "zdepth_ordinal" */
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_117_TEST_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(117),
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    doTestScreenResult117ParseResult(screenResult);
  }
  
  public void testMissingDerivedFrom() throws FileNotFoundException
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_MISSING_DERIVED_FROM_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(screen, workbookFile);
    assertEquals("screen result had no errors", 
                 Collections.<ParseError>emptyList(),
                 mockScreenResultParser.getErrors());
    DataColumn col0 = screenResult.getDataColumnsList().get(0);
    assertTrue("col 0 is derived", col0.isDerived());
    assertEquals("col 0 'derived from' is empty", 0, col0.getTypesDerivedFrom().size());
    
    // regression test that normal derived from values work as expected
    DataColumn col1 = screenResult.getDataColumnsList().get(1);
    assertTrue("col 1 is derived", col1.isDerived());
    assertTrue("col 1 'types derived from' is not empty", col1.getTypesDerivedFrom().contains(col0));
    assertTrue("col 0 'derived types' is not empty", col0.getDerivedTypes().contains(col1));
  }
  
  // private methods
  
  private void doTestScreenResult115ParseResult(ScreenResult screenResult)
  {
    assertEquals("replicate count", 2, screenResult.getReplicateCount().intValue());

    ScreenResult expectedScreenResult = makeScreenResult();
    Map<Integer,DataColumn> expectedDataColumns = new HashMap<Integer,DataColumn>();

    DataColumn dataColumn;

    dataColumn = expectedScreenResult.createDataColumn("Luminescence1");
    dataColumn.setDescription("Desc1");
    dataColumn.forReplicate(1);
    dataColumn.forTimePoint("0:10");
    dataColumn.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    dataColumn.forPhenotype("Phenotype1");
    dataColumn.setComments("None");
    dataColumn.makeNumeric(0);
    expectedDataColumns.put(0, dataColumn);

    dataColumn = expectedScreenResult.createDataColumn("Luminescence2");
    dataColumn.setDescription("Desc2");
    dataColumn.forReplicate(2);
    dataColumn.forTimePoint("0:10");
    dataColumn.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    dataColumn.forPhenotype("Phenotype1");
    dataColumn.setFollowUpData(true);
    dataColumn.setComments("None");
    dataColumn.makeNumeric(0);
    expectedDataColumns.put(1, dataColumn);

    dataColumn = expectedScreenResult.createDataColumn("FI1");
    dataColumn.setDescription("Fold Induction");
    dataColumn.forReplicate(1);
    dataColumn.makeDerived("Divide compound well by plate median", Sets.newHashSet(expectedDataColumns.get(0)));
    dataColumn.forPhenotype("Phenotype1");
    dataColumn.makeNumeric(2);
    expectedDataColumns.put(2, dataColumn);

    dataColumn = expectedScreenResult.createDataColumn("FI2");
    dataColumn.setDescription("Fold Induction");
    dataColumn.forReplicate(2);
    dataColumn.makeDerived("Divide compound well by plate median", Sets.newHashSet(expectedDataColumns.get(1)));
    dataColumn.forPhenotype("Phenotype1");
    dataColumn.setFollowUpData(true);
    dataColumn.makeNumeric(2);
    expectedDataColumns.put(3, dataColumn);

    dataColumn = expectedScreenResult.createDataColumn("Average");
    dataColumn.makeDerived("Average", Sets.newHashSet(expectedDataColumns.get(2), expectedDataColumns.get(3)));
    dataColumn.makeNumeric(2);
    expectedDataColumns.put(4, dataColumn);

    dataColumn = expectedScreenResult.createDataColumn("PositiveIndicator2");
    dataColumn.makeDerived("W<=1.6, M<=1.7, S<=1.8", Sets.newHashSet(expectedDataColumns.get(4)));
    dataColumn.makePartitionPositiveIndicator();
    expectedDataColumns.put(5, dataColumn);

    dataColumn = expectedScreenResult.createDataColumn("PositiveIndicator3");
    dataColumn.makeDerived("PositiveIndicator2 is S", Sets.newHashSet(expectedDataColumns.get(5)));
    dataColumn.makeBooleanPositiveIndicator();
    expectedDataColumns.put(6, dataColumn);


    Integer[] expectedPlateNumbers = {1, 1, 1, 1, 1, 1, 1, 1};

    String[] expectedWellNames = {"A01", "A02", "A03", "A04", "A05", "A06", "A07", "A08"};

    AssayWellControlType[] expectedAssayWellControlTypes = {
      AssayWellControlType.ASSAY_POSITIVE_CONTROL,
      null,
      null,
      AssayWellControlType.ASSAY_CONTROL,
      null,
      null,
      AssayWellControlType.OTHER_CONTROL,
      null
      };

    boolean[][] expectedExcludeValues = {
      {false, false, false, false, false, false, false},
      {false, false, false, false, false, false, false},
      {false, true,  false, true,  false, false, false},
      {false, false, false, false, false, false, false},
      {false, false, false, false, false, false, false},
      {false, false, false, false, false, false, false},
      {true,  true,  true,  true,  true,  true,  true},
      {false, false, false, false, false, false, false}};

    Object[][] expectedValues = {
          { 1071894., 1196906., 0.98, 1.11, 1.045, PartitionedValue.NOT_POSITIVE, Boolean.TRUE },
          { 1174576., 1469296., null, 5.8, 5.80, PartitionedValue.STRONG, Boolean.TRUE },
          { 1294182., 1280934., 1.18, 1.19, 1.185, PartitionedValue.NOT_POSITIVE, Boolean.FALSE },
          { 1158888., 1458878., 1.06, 1.35, 1.205, PartitionedValue.WEAK, Boolean.FALSE },
          { 1385142., 1383446., 1.26, 1.28, 1.270, PartitionedValue.WEAK, Boolean.FALSE },
          { null, null, null, null, null, PartitionedValue.NOT_POSITIVE, Boolean.FALSE },
          { 1666646., 1154436., 1.52, 1.07, 1.295, PartitionedValue.WEAK, Boolean.FALSE },
          { null, null, null, null, null, PartitionedValue.NOT_POSITIVE, Boolean.FALSE } };

    SortedSet<DataColumn> dataColumns = screenResult.getDataColumns();
    int iCol = 0;
    for (DataColumn actualCol : dataColumns) {
      DataColumn expectedCol = expectedDataColumns.get(iCol);
      if (expectedCol != null) {
        assertTrue("DataColumn " + iCol, expectedCol.isEquivalent(actualCol));

        // compare result values
        assertEquals(960, actualCol.getResultValues().size());
        int iWell = 0;
        Map<WellKey,ResultValue> resultValues = actualCol.getWellKeyToResultValueMap();
        for (WellKey wellKey : new TreeSet<WellKey>(resultValues.keySet())) {
          ResultValue rv = resultValues.get(wellKey);
          assertEquals("col " + iCol + " well #" + iWell + " plate name",
                       expectedPlateNumbers[iWell],
                       new Integer(wellKey.getPlateNumber()));
          assertEquals("col " + iCol + " well #" + iWell + " well name",
                       expectedWellNames[iWell],
                       wellKey.getWellName());
          assertEquals("col " + iCol + " well #" + iWell + " well type",
                       expectedAssayWellControlTypes[iWell],
                       rv.getAssayWellControlType());
          assertEquals("col " + iCol + " well #" + iWell + " well type",
                       expectedExcludeValues[iWell][iCol],
                       rv.isExclude());
          if (expectedValues[iWell][iCol] == null) {
            assertTrue("col " + iCol + " well #" + iWell + " result value is null",
                       rv.isNull());
          }
          else {
            assertEquals("col " + iCol + " well #" + iWell + " result value", expectedValues[iWell][iCol], rv.getTypedValue());
          }
          ++iWell;
          if (iWell == expectedPlateNumbers.length) { break; }
        }
      }
      ++iCol;
    }
  }
 
  private void doTestScreenResult117ParseResult(ScreenResult screenResult)
  {
    assertEquals("replicate count", 2, screenResult.getReplicateCount().intValue());

    ScreenResult expectedScreenResult = makeScreenResult();
    Map<Integer,DataColumn> expectedDataColumns = new HashMap<Integer,DataColumn>();

    DataColumn col;

    col = expectedScreenResult.createDataColumn("r1c1").
    makeNumeric(0).
    forReplicate(1).
    forChannel(1).
    forTimePointOrdinal(1).
    forZdepthOrdinal(4);
    col.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedDataColumns.put(0, col);

    col = expectedScreenResult.createDataColumn("r1c2").
    makeNumeric(0).
    forReplicate(1).
    forChannel(2).
    forTimePointOrdinal(2).
    forZdepthOrdinal(3);
    col.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedDataColumns.put(1, col);
    
    col = expectedScreenResult.createDataColumn("r2c1").
    makeNumeric(0).
    forReplicate(2).
    forChannel(1).
    forTimePointOrdinal(3).
    forZdepthOrdinal(2);
    col.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedDataColumns.put(2, col);
    
    col = expectedScreenResult.createDataColumn("r2c2").
    makeNumeric(0).
    forReplicate(2).
    forChannel(2).
    forTimePointOrdinal(4).
    forZdepthOrdinal(1);
    col.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedDataColumns.put(3, col);

    //check the second data column
    Integer[] expectedPlateNumbers = {1,1,1,2,2,2};
//
    String[] expectedWellNames = {"A01", "A02", "A03","A01", "A02", "A03"};
//
    AssayWellControlType[] expectedAssayWellTypes = {
      AssayWellControlType.ASSAY_POSITIVE_CONTROL,
      null,
      null,
      AssayWellControlType.ASSAY_CONTROL_SHARED,
      null,
      null };

    boolean[][] expectedExcludeValues = {
      {false, false, false, false},
      {false, false, false, false},
      {false, false, false, false},
      {false, false, false, false},
      {false, false, true, false},
      {false, false, false, false}
    };

    Object[][] expectedValues = {
          {1071894.,100.,7654321.,90.},
          {1234567.,110.,6543210.,120.},
          {1174576.,120.,5432109.,80.},
          {1071896.,101.,7654320.,89.},
          {1234563.,113.,6543217.,126.},
          {1174572.,125.,5432105.,89.}
    };

    SortedSet<DataColumn> dataColumns = screenResult.getDataColumns();
    int iCol = 0;
    for (DataColumn actualCol : dataColumns) {
      DataColumn expectedCol = expectedDataColumns.get(iCol);
      if (expectedCol != null) {
//        setDefaultValues(expectedCol);
//        setDefaultValues(actualCol);
        log.info("expectedCol = " + expectedCol.getName());
        log.info("actualCol = " + actualCol.getName());
        assertTrue("DataColumn " + iCol, expectedCol.isEquivalent(actualCol));

        // TODO compare result values
       assertEquals(6, actualCol.getResultValues().size());
        int iWell = 0;
        Map<WellKey,ResultValue> resultValues = actualCol.getWellKeyToResultValueMap();
        for (WellKey wellKey : new TreeSet<WellKey>(resultValues.keySet())) {
          ResultValue rv = resultValues.get(wellKey);
         assertEquals("col " + iCol + " well #" + iWell + " plate name",
                       expectedPlateNumbers[iWell],
                       new Integer(wellKey.getPlateNumber()));
          assertEquals("col " + iCol + " well #" + iWell + " well name",
                       expectedWellNames[iWell],
                       wellKey.getWellName());
          assertEquals("col " + iCol + " well #" + iWell + " well type",
                       expectedAssayWellTypes[iWell],
                       rv.getAssayWellControlType());
          assertEquals("col " + iCol + " well #" + iWell + " well type",
                       expectedExcludeValues[iWell][iCol],
                       rv.isExclude());
        if (expectedValues[iWell][iCol] == null) {
            assertTrue("col " + iCol + " well #" + iWell + " result value is null",
                       rv.isNull());
          }
          else {
            if (expectedCol.isNumeric()) {
              double expectedNumericValue = (Double) expectedValues[iWell][iCol];
              assertEquals("col " + iCol + " well #" + iWell + " result value (numeric)",
                           expectedNumericValue,
                           rv.getNumericValue(),
                           Math.pow(1, -col.getDecimalPlaces()));
            }
            else {
              assertEquals("col " + iCol + " well #" + iWell + " result value (non-numeric)",
                           expectedValues[iWell][iCol].toString(),
                           rv.getValue());
            }
          }
        ++iWell;
          if (iWell == expectedPlateNumbers.length) { break; }
        }
      }
      ++iCol; 
    }  
  }
  
  

  // testing  utility methods

  public static ScreenResult makeScreenResult()
  {
    Screen screen = makeScreen();
    ScreenResult screenResult = screen.createScreenResult();
    return screenResult;
  }

  private static Screen makeScreen()
  {
    return MakeDummyEntities.makeDummyScreen(1);
  }

}
