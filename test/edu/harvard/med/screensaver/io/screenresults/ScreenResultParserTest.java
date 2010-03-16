// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
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

import jxl.BooleanFormulaCell;
import jxl.CellType;
import jxl.NumberFormulaCell;
import jxl.Sheet;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;

public class ScreenResultParserTest extends AbstractSpringTest
{

  private static final Logger log = Logger.getLogger(ScreenResultParserTest.class);

  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/screenresults");
  public static final String SCREEN_RESULT_115_TEST_WORKBOOK_FILE = "ScreenResultTest115.xls";
  public static final String SCREEN_RESULT_117_TEST_WORKBOOK_FILE = "ScreenResultTest117.xls";
  public static final String SCREEN_RESULT_115_30_DATAHEADERS_TEST_WORKBOOK_FILE = "ScreenResultTest115_30DataHeaders.xls";
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
    
    // also test that the parse time is correct
    assertTrue("Screen parse time is incorrect: " + screenResult.getDateLastImported() +
               ", should be after: " + now,
               screenResult.getDateLastImported().getMillis() > now.getTime() );
    
  }
  
  /**
   * Tests basic usage of the JXL API to read an Excel spreadsheet, but does
   * not test Screensaver-related functionality. Basically, just a check that
   * the technology we're using actually works. Somewhat useful to keep around
   * in case we upgrade jar version, etc.
   */
  public void testReadExcelSpreadsheet() throws Exception
  {
    String[] expectedSheetNames = new String[] { "Screen Info", "Data Headers", "PL_00001", "PL_00003", "PL_00002" };
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
    assertEquals("well count", 4, (int) mockScreenResultParser.getParsedScreenResult().getExperimentalWellCount());
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
    assertTrue(errors.contains(new ParseError("value required", "Data Headers:(B,3)")));
    assertTrue(errors.contains(new ParseError("illegal value", "Data Headers:(E,4)")));
    assertTrue(errors.contains(new ParseError("unparseable value \"paradiso positive indicator\" (expected one of [Boolean Positive Indicator, Numeric, Partition Positive Indicator, Text])", "Data Headers:(G,3)")));
    assertTrue(errors.contains(new ParseError("unparseable value \"sushiraw\" (expected one of [, derived, raw])", "Data Headers:(C,9)")));
    assertTrue(errors.contains(new ParseError("invalid Data Header column reference 'B' (expected one of [E, F])", "Data Headers:(D,11)")));
    assertTrue(errors.contains(new ParseError("invalid Data Header column reference 'H' (expected one of [E, F, G])", "Data Headers:(E,11)")));
    assertTrue(errors.contains(new ParseError("invalid Data Header column reference 'D' (expected one of [E, F, G, H])", "Data Headers:(F,11)")));
    assertTrue(errors.contains(new ParseError("unparseable value \"follow-up\" (expected one of [, follow up, primary])", "Data Headers:(E,12)")));
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
    ScreenResult screenResult = mockScreenResultParser.parse(screen,
                                 workbookFile,
                                 new IntRange(1, 2), 
                                 false);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());
    DateTime firstParseTime = screenResult.getDateLastImported();

    screenResult = mockScreenResultParser.parse(screen,
                                 workbookFile,
                                 new IntRange(3, 3),
                                 false);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());
    
    doTestScreenResult115ParseResult(screen.getScreenResult());
    
    // also test that the parse time has been updated
    assertTrue("Screen parse time is incorrect: " + screenResult.getDateLastImported() +
               ", should be after: " + firstParseTime,
               screenResult.getDateLastImported().getMillis() > firstParseTime.getMillis() );
    
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
                 screenResult.getResultValueTypesList().get(0).getResultValues().size());
    int nonExcludedResultValues = resultValues - 1;
    List<Integer> expectedHitCount = Arrays.asList(6, 3);
    List<Double> expectedHitRatio = Arrays.asList(expectedHitCount.get(0) / (double) nonExcludedResultValues,
                                                  expectedHitCount.get(1) / (double) nonExcludedResultValues);

    int iPositiveIndicatorRvt = 0;
    for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
      if (rvt.isPositiveIndicator()) {
        assertEquals("hit count", expectedHitCount.get(iPositiveIndicatorRvt), rvt.getPositivesCount());
        assertEquals("hit ratio",
                     expectedHitRatio.get(iPositiveIndicatorRvt).doubleValue(),
                     rvt.getPositivesRatio().doubleValue(),
                     0.01);
        ++iPositiveIndicatorRvt;
      }
    }
  }

  public void testMultiCharColumnLabels() throws FileNotFoundException
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_30_DATAHEADERS_TEST_WORKBOOK_FILE);
    ScreenResult result = mockScreenResultParser.parse(screen,workbookFile);
    if (mockScreenResultParser.getHasErrors()) {
      log.debug("parse errors: " + mockScreenResultParser.getErrors());
    }
    assertFalse("screen result had no errors", mockScreenResultParser.getHasErrors());
    List<ResultValueType> resultValueTypes = result.getResultValueTypesList();
    assertEquals("ResultValueType count", 30, resultValueTypes.size());
    for (int i = 0; i < 30 - 1; ++i) {
      ResultValueType rvt = resultValueTypes.get(i);
      assertEquals("is derived from next", resultValueTypes.get(i+1), rvt.getDerivedTypes().first());
      Map<WellKey,ResultValue> resultValues = rvt.getWellKeyToResultValueMap();
      assertEquals(rvt.getName() + " result value 0", 1000.0 + i, resultValues.get(new WellKey(1, "A01")).getNumericValue());
      assertEquals(rvt.getName() + " result value 1", 2000.0 + i, resultValues.get(new WellKey(1, "A02")).getNumericValue());
      assertEquals(rvt.getName() + " result value 2", 3000.0 + i, resultValues.get(new WellKey(1, "A03")).getNumericValue());
    }
    assertTrue("last is not derived from any", resultValueTypes.get(30 - 1).getDerivedTypes().isEmpty());
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
    ResultValueType rvt0 = screenResult.getResultValueTypesList().get(0);
    assertTrue("rvt 0 is derived", rvt0.isDerived());
    assertEquals("rvt 0 'derived from' is empty", 0, rvt0.getTypesDerivedFrom().size());
    
    // regression test that normal derived from values work as expected
    ResultValueType rvt1 = screenResult.getResultValueTypesList().get(1);
    assertTrue("rvt 1 is derived", rvt1.isDerived());
    assertTrue("rvt 1 'types derived from' is not empty", rvt1.getTypesDerivedFrom().contains(rvt0));
    assertTrue("rvt 0 'derived types' is not empty", rvt0.getDerivedTypes().contains(rvt1));
  }
  
  // private methods
  
  private void doTestScreenResult115ParseResult(ScreenResult screenResult)
  {
    assertEquals("replicate count", 2, screenResult.getReplicateCount().intValue());

    ScreenResult expectedScreenResult = makeScreenResult();
    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt;

    rvt = expectedScreenResult.createResultValueType("Luminescence1");
    rvt.setDescription("Desc1");
    rvt.forReplicate(1);
    rvt.forTimePoint("0:10");
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    rvt.forPhenotype("Phenotype1");
    rvt.setComments("None");
    rvt.makeNumeric(0);
    expectedResultValueTypes.put(0, rvt);

    rvt = expectedScreenResult.createResultValueType("Luminescence2");
    rvt.setDescription("Desc2");
    rvt.forReplicate(2);
    rvt.forTimePoint("0:10");
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    rvt.forPhenotype("Phenotype1");
    rvt.setFollowUpData(true);
    rvt.setComments("None");
    rvt.makeNumeric(0);
    expectedResultValueTypes.put(1, rvt);

    rvt = expectedScreenResult.createResultValueType("FI1");
    rvt.setDescription("Fold Induction");
    rvt.forReplicate(1);
    rvt.makeDerived("Divide compound well by plate median", Sets.newHashSet(expectedResultValueTypes.get(0)));
    rvt.forPhenotype("Phenotype1");
    rvt.makeNumeric(2);
    expectedResultValueTypes.put(2, rvt);

    rvt = expectedScreenResult.createResultValueType("FI2");
    rvt.setDescription("Fold Induction");
    rvt.forReplicate(2);
    rvt.makeDerived("Divide compound well by plate median", Sets.newHashSet(expectedResultValueTypes.get(1)));
    rvt.forPhenotype("Phenotype1");
    rvt.setFollowUpData(true);
    rvt.makeNumeric(2);
    expectedResultValueTypes.put(3, rvt);

    rvt = expectedScreenResult.createResultValueType("Average");
    rvt.makeDerived("Average", Sets.newHashSet(expectedResultValueTypes.get(2), expectedResultValueTypes.get(3)));
    rvt.makeNumeric(2);
    expectedResultValueTypes.put(4, rvt);

    rvt = expectedScreenResult.createResultValueType("PositiveIndicator2");
    rvt.makeDerived("W<=1.6, M<=1.7, S<=1.8", Sets.newHashSet(expectedResultValueTypes.get(4)));
    rvt.makePartitionPositiveIndicator();
    expectedResultValueTypes.put(5, rvt);

    rvt = expectedScreenResult.createResultValueType("PositiveIndicator3");
    rvt.makeDerived("PositiveIndicator2 is S", Sets.newHashSet(expectedResultValueTypes.get(5)));
    rvt.makeBooleanPositiveIndicator();
    expectedResultValueTypes.put(6, rvt);


    Integer[] expectedPlateNumbers = {1, 1, 1, 1, 1, 1, 1, 1};

    String[] expectedWellNames = {"A01", "A02", "A03", "A04", "A05", "A06", "A07", "A08"};

    AssayWellType[] expectedAssayWellTypes = {
      AssayWellType.ASSAY_POSITIVE_CONTROL,
      AssayWellType.EXPERIMENTAL,
      AssayWellType.EXPERIMENTAL,
      AssayWellType.ASSAY_CONTROL,
      AssayWellType.LIBRARY_CONTROL,
      AssayWellType.BUFFER,
      AssayWellType.OTHER,
      AssayWellType.EMPTY};

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
          { 1071894., 1196906., 0.98, 1.11, 1.045, "0", "true" },
          { 1174576., 1469296., null, 5.8,  5.80, "3", "true" },
          { 1294182., 1280934., 1.18, 1.19, 1.185, "0", "false" },
          { 1158888., 1458878., 1.06, 1.35, 1.205, "1", "false" },
          { 1385142., 1383446., 1.26, 1.28, 1.270, "1", "false" },
          { null, null, null, null, null, "0", "false" },
          { 1666646., 1154436., 1.52, 1.07, 1.295, "1", "false" },
          { null, null, null, null, null, "0", "false" } };

    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int iRvt = 0;
    for (ResultValueType actualRvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(iRvt);
      if (expectedRvt != null) {
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(actualRvt));

        // compare result values
        assertEquals(960, actualRvt.getResultValues().size());
        int iWell = 0;
        Map<WellKey,ResultValue> resultValues = actualRvt.getWellKeyToResultValueMap();
        for (WellKey wellKey : new TreeSet<WellKey>(resultValues.keySet())) {
          ResultValue rv = resultValues.get(wellKey);
          assertEquals("rvt " + iRvt + " well #" + iWell + " plate name",
                       expectedPlateNumbers[iWell],
                       new Integer(wellKey.getPlateNumber()));
          assertEquals("rvt " + iRvt + " well #" + iWell + " well name",
                       expectedWellNames[iWell],
                       wellKey.getWellName());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well type",
                       expectedAssayWellTypes[iWell],
                       rv.getAssayWellType());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well type",
                       expectedExcludeValues[iWell][iRvt],
                       rv.isExclude());
          if (expectedValues[iWell][iRvt] == null) {
            assertTrue("rvt " + iRvt + " well #" + iWell + " result value is null",
                       rv.isNull());
          }
          else {
            if (expectedRvt.isNumeric()) {
              double expectedNumericValue = (Double) expectedValues[iWell][iRvt];
              assertEquals("rvt " + iRvt + " well #" + iWell + " result value (numeric)",
                           expectedNumericValue,
                           rv.getNumericValue(),
                           0.001);
            }
            else {
              assertEquals("rvt " + iRvt + " well #" + iWell + " result value (non-numeric)",
                           expectedValues[iWell][iRvt].toString(),
                           rv.getValue());
            }
          }
          ++iWell;
          if (iWell == expectedPlateNumbers.length) { break; }
        }
      }
      ++iRvt;
    }
  }
 
  private void doTestScreenResult117ParseResult(ScreenResult screenResult)
  {
    assertEquals("replicate count", 2, screenResult.getReplicateCount().intValue());

    ScreenResult expectedScreenResult = makeScreenResult();
    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt;

    rvt = expectedScreenResult.createResultValueType("r1c1").
    makeNumeric(0).
    forReplicate(1).
    forChannel(1).
    forTimePointOrdinal(1).
    forZdepthOrdinal(4);
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedResultValueTypes.put(0, rvt);

    rvt = expectedScreenResult.createResultValueType("r1c2").
    makeNumeric(0).
    forReplicate(1).
    forChannel(2).
    forTimePointOrdinal(2).
    forZdepthOrdinal(3);
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedResultValueTypes.put(1, rvt);
    
    rvt = expectedScreenResult.createResultValueType("r2c1").
    makeNumeric(0).
    forReplicate(2).
    forChannel(1).
    forTimePointOrdinal(3).
    forZdepthOrdinal(2);
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedResultValueTypes.put(2, rvt);
    
    rvt = expectedScreenResult.createResultValueType("r2c2").
    makeNumeric(0).
    forReplicate(2).
    forChannel(2).
    forTimePointOrdinal(4).
    forZdepthOrdinal(1);
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    expectedResultValueTypes.put(3, rvt);

    //check the second result value type
    Integer[] expectedPlateNumbers = {1,1,1,2,2,2};
//
    String[] expectedWellNames = {"A01", "A02", "A03","A01", "A02", "A03"};
//
    AssayWellType[] expectedAssayWellTypes = {
      AssayWellType.ASSAY_POSITIVE_CONTROL,
      AssayWellType.EXPERIMENTAL,
      AssayWellType.EXPERIMENTAL,
      AssayWellType.ASSAY_CONTROL_SHARED,
      AssayWellType.EXPERIMENTAL,
      AssayWellType.EXPERIMENTAL};

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

    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int iRvt = 0;
    for (ResultValueType actualRvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(iRvt);
      if (expectedRvt != null) {
//        setDefaultValues(expectedRvt);
//        setDefaultValues(actualRvt);
        log.info("expectedRvt = " + expectedRvt.getName());
        log.info("actualRvt = " + actualRvt.getName());
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(actualRvt));

        // TODO compare result values
       assertEquals(6, actualRvt.getResultValues().size());
        int iWell = 0;
        Map<WellKey,ResultValue> resultValues = actualRvt.getWellKeyToResultValueMap();
        for (WellKey wellKey : new TreeSet<WellKey>(resultValues.keySet())) {
          ResultValue rv = resultValues.get(wellKey);
         assertEquals("rvt " + iRvt + " well #" + iWell + " plate name",
                       expectedPlateNumbers[iWell],
                       new Integer(wellKey.getPlateNumber()));
          assertEquals("rvt " + iRvt + " well #" + iWell + " well name",
                       expectedWellNames[iWell],
                       wellKey.getWellName());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well type",
                       expectedAssayWellTypes[iWell],
                       rv.getAssayWellType());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well type",
                       expectedExcludeValues[iWell][iRvt],
                       rv.isExclude());
        if (expectedValues[iWell][iRvt] == null) {
            assertTrue("rvt " + iRvt + " well #" + iWell + " result value is null",
                       rv.isNull());
          }
          else {
            if (expectedRvt.isNumeric()) {
              double expectedNumericValue = (Double) expectedValues[iWell][iRvt];
              assertEquals("rvt " + iRvt + " well #" + iWell + " result value (numeric)",
                           expectedNumericValue,
                           rv.getNumericValue(),
                           Math.pow(1, -rvt.getDecimalPlaces()));
            }
            else {
              assertEquals("rvt " + iRvt + " well #" + iWell + " result value (non-numeric)",
                           expectedValues[iWell][iRvt].toString(),
                           rv.getValue());
            }
          }
        ++iWell;
          if (iWell == expectedPlateNumbers.length) { break; }
        }
      }
      ++iRvt; 
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
