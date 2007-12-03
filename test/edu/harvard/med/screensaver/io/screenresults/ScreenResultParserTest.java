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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
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
import jxl.DateCell;
import jxl.NumberCell;
import jxl.NumberFormulaCell;
import jxl.Sheet;
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultWorkbookSpecification.ScreenInfoRow;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook2.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.util.DateUtil;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

/**
 */
public class ScreenResultParserTest extends AbstractSpringTest
{

  private static final Logger log = Logger.getLogger(ScreenResultParserTest.class);

  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/screenresults");
  public static final String SCREEN_RESULT_115_TEST_WORKBOOK_FILE = "ScreenResultTest115.xls";
  public static final String SCREEN_RESULT_116_TEST_WORKBOOK_FILE = "ScreenResultTest116.xls";
  public static final String SCREEN_RESULT_115_30_DATAHEADERS_TEST_WORKBOOK_FILE = "ScreenResultTest115_30DataHeaders.xls";
  public static final String SCREEN_RESULT_115_NO_DATE_TEST_WORKBOOK_FILE = "ScreenResultTest115-no-date.xls";
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
   * Tests basic usage of the JXL API to read an Excel spreadsheet, but does
   * not test Screensaver-related functionality. Basically, just a check that
   * the technology we're using actually works. Somewhat useful to keep around
   * in case we upgrade jar version, etc.
   */
  public void testReadExcelSpreadsheet() throws Exception
  {
    String[] expectedSheetNames = new String[] { "Screen Info", "Data Headers", "PL_00001", "PL_00003", "PL_00002" };
    String[] expectedHeaderRowValues = new String[] {
      "Stock Plate ID",
      "Well",
      "Type",
      "Exclude",
      "Luminescence",
      "Luminescence",
      "FI_A",
      "FI_B",
      "AssayIndicator1",
      "AssayIndicator2",
      "AssayIndicator3" };

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
    assertEquals("numeric data format precision",
                 4,
                 ((NumberFormulaCell) numericFormulaCell).getNumberFormat().getMaximumFractionDigits());

    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(TEST_INPUT_FILE_DIR, FORMULA_VALUE_TEST_WORKBOOK_FILE),
                                     errors);
    Cell.Factory cellFactory = new Cell.Factory(workbook, 0, errors);
    Cell cell = cellFactory.getCell((short) 3, (short) 1, false);
    assertNotNull(cell);
    Double parsedNumericValue = cell.getDouble();
    assertEquals("parse numeric value",
                 numericValue,
                 parsedNumericValue.doubleValue(),
                 0.0001);

    // test numeric precision (TODO: should probably be a separate unit test)
    Cell numericFormatFormulaCell = cellFactory.getCell((short) 3, (short) 1, false);
    assertEquals("precision of numeric format on formula cell", 4,
                 numericFormatFormulaCell.getDoublePrecision());
//    Cell generalFormatFormulaCell = cellFactory.getCell((short) 4, (short) 1);
//    assertEquals("precision of general format on formula cell", -1,
//                 generalFormatFormulaCell.getDoublePrecision());
//    Cell generalFormatNumericCell = cellFactory.getCell((short) 1, (short) 1);
//    assertEquals("precision of general format on numeric cell", -1,
//                 generalFormatNumericCell.getDoublePrecision());
    Cell numericFormatNumericCell = cellFactory.getCell((short) 2, (short) 1);
    assertEquals("precision of numeric format on numeric cell", 3,
                 numericFormatNumericCell.getDoublePrecision());
    Cell integerNumericFormatNumericCell = cellFactory.getCell((short) 5, (short) 1);
    assertEquals("precision of integer number format on numeric cell", 0,
                 integerNumericFormatNumericCell.getDoublePrecision());
    Cell percentageNumericCell = cellFactory.getCell((short) 6, (short) 1);
    assertEquals("precision of percentage number format on numeric cell", 3,
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
    assertFalse("screen result had no errors", mockScreenResultParser.getHasErrors());
    assertEquals("well count", 4, (int) mockScreenResultParser.getParsedScreenResult().getExperimentalWellCount());
  }

  // Note: this test cannot pass because the POI/HSSF library is crappy and does
  // not allow you to read a boolean-typed formula cell value!
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
    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(TEST_INPUT_FILE_DIR,
                                              FORMULA_VALUE_TEST_WORKBOOK_FILE), errors);
    Cell.Factory cellFactory = new Cell.Factory(workbook, 0, errors);
    Cell cell = cellFactory.getCell((short) 7, (short) 1, false);
    assertNotNull(cell);
    assertEquals("parse boolean value",
                 booleanValue,
                 cell.getBoolean().booleanValue());
  }

  /**
   * Tests that screen result errors are saved to a new set of workbooks.
   * @throws IOException
   * @throws BiffException
   * @throws WriteException
   */
  public void testErrorAnnotatedWorkbook() throws IOException, BiffException, WriteException
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115), workbookFile);
    WritableWorkbook errorAnnotatedWorkbook = mockScreenResultParser.getErrorAnnotatedWorkbook();
    File file = File.createTempFile(ERRORS_TEST_WORKBOOK_FILE, ".xls");
    errorAnnotatedWorkbook.setOutputFile(file);
    errorAnnotatedWorkbook.write();
    errorAnnotatedWorkbook.close();

    assertTrue("error-annotated workbook file exists", file.exists());

    // test error-annotated workbook contents
    jxl.Workbook errorAnnotatedWorkbook2 = jxl.Workbook.getWorkbook(file);

    // note: data sheets w/o errors are not exported
    assertEquals("number of sheets", 2,
                 errorAnnotatedWorkbook2.getNumberOfSheets());

    Sheet sheet0 = errorAnnotatedWorkbook2.getSheet(0);

    int i = ScreenResultWorkbookSpecification.SCREENINFO_FIRST_DATA_ROW_INDEX;
    for (ScreenInfoRow screenInfoRow : ScreenInfoRow.values()) {
      assertEquals("row " + i + " label",
                   screenInfoRow.getDisplayText(),
                   sheet0.getCell(ScreenResultWorkbookSpecification.SCREENINFO_ROW_HEADER_COLUMN_INDEX, i).getContents());
      if (screenInfoRow.equals(ScreenInfoRow.ID)) {
        assertEquals(screenInfoRow.name() + " value",
                     screenResult.getScreen().getScreenNumber(),
                     new Integer((int) ((NumberCell) sheet0.getCell(ScreenResultWorkbookSpecification.SCREENINFO_VALUE_COLUMN_INDEX, i)).getValue()));
      }
      if (screenInfoRow.equals(ScreenInfoRow.DATE_FIRST_LIBRARY_SCREENING)) {
        assertEquals(screenInfoRow.name() + " value",
                     screenResult.getDateCreated(),
                     DateUtils.truncate(Cell.convertGmtDateToLocalTimeZone(((DateCell) sheet0.getCell(ScreenResultWorkbookSpecification.SCREENINFO_VALUE_COLUMN_INDEX, i)).getDate()), Calendar.DATE));
      }
      ++i;
    }

    Sheet sheet1 = errorAnnotatedWorkbook2.getSheet(1);
    assertEquals("SushiRaw: unparseable value \"sushiraw\" (expected one of [, derived, raw])",
                 sheet1.getCell('C' - 'A', 6).getContents());
    assertEquals("B: invalid Data Header column reference 'B' (expected one of [E, F])",
                 sheet1.getCell('D' - 'A', 8).getContents());
    assertEquals("H: invalid Data Header column reference 'H' (expected one of [E, F, G])",
                 sheet1.getCell('E' - 'A', 8).getContents());
    assertEquals("D,E: invalid Data Header column reference 'D' (expected one of [E, F, G, H])",
                 sheet1.getCell('F' - 'A', 8).getContents());
    assertEquals("unparseable value \"\" (expected one of [<, >])",
                 sheet1.getCell('F' - 'A', 11).getContents());
    assertEquals("Follow-up: unparseable value \"follow-up\" (expected one of [, follow up, primary])",
                 sheet1.getCell('E' - 'A', 13).getContents());
    assertEquals("Baloonean: unparseable value \"baloonean\" (expected one of [Boolean, Numeric, Numerical, Partition, Partitioned])",
                 sheet1.getCell('H' - 'A', 10).getContents());
    assertEquals("unparseable value \"\" (expected one of [<, >])",
                 sheet1.getCell('H' - 'A', 11).getContents());
    assertEquals("value required",
                 sheet1.getCell('H' - 'A', 12).getContents());
  }

  /**
   * Tests that Cells are cloned when needed (a single Cell is generally
   * recycled, as an optimization). Note that this test assumes that the test
   * errorAnnotatedWorkbooks do not have more than 1 error per cell, which is a possibility in
   * the real world, but would break our naive test. (I suppose we could also
   * test simply that at least some of our ParseErrors' cells were different.)
   */
  public void testRecycledCellUsage()
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
    ScreenResult result1 = mockScreenResultParser.parse(screen, workbookFile);
    List<WorkbookParseError> errors1 = mockScreenResultParser.getErrors();
    assertNotNull("1st parse returns a result", result1);
    ScreenResult result2 = mockScreenResultParser.parse(screen, workbookFile);
    List<WorkbookParseError> errors2 = mockScreenResultParser.getErrors();
    assertNotNull("2nd parse returns a result", result2);
    assertNotSame("parses returned different ScreenResult objects", result1, result2);
    assertTrue(errors1.size() > 0);
    assertTrue(errors2.size() > 0);
    assertEquals("errors not accumulating across multiple parse() calls", errors1, errors2);
    // allow GC
    result1 = null;
    result2 = null;
    System.gc();

    // now test reading yet another spreadsheet, for which we can test the parsed result
    testParseScreenResult();
   }

  /**
   * Test that if a screen result file has no "Date of First Screening Room
   * Activity" value, the screen result's "date created" property should default
   * to the Screen's first visit date.
   */
  public void testScreenResultDateCreated() throws Exception
  {

    Screen screen = MakeDummyEntities.makeDummyScreen(115);
    try {
      screen.createLibraryScreening(
        screen.getLeadScreener(),
        DateUtil.makeDate(2007, 1, 1),
        DateUtil.makeDate(2007, 2, 2));
    }
    catch (DuplicateEntityException e) {
      e.printStackTrace();
    }

    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_NO_DATE_TEST_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(screen,
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    Date expectedDate = DateUtil.makeDate(2007, 2, 2);
    ScreenResult expectedScreenResult = makeScreenResult(expectedDate);
    assertEquals("dateCreated",
                 expectedScreenResult.getDateCreated(),
                 screenResult.getDateCreated());
  }

    /**
   * Tests parsing of the new ScreenResult workbook format, which is an
   * "all-in-one" format, and has significant structural changes.
   *
   * @throws Exception
   */
  public void testParseScreenResult() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115),
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    Date expectedDate = DateUtil.makeDate(2006, 1, 1);
    ScreenResult expectedScreenResult = makeScreenResult(expectedDate);
    assertEquals("date",
                 expectedDate,
                 screenResult.getDateCreated());
    assertEquals("replicate count", 2, screenResult.getReplicateCount().intValue());

    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt;

    rvt = expectedScreenResult.createResultValueType("Luminescence");
    rvt.setDescription("Desc1");
    rvt.setReplicateOrdinal(1);
    rvt.setTimePoint("0:10");
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setComments("None");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(0, rvt);

    rvt = expectedScreenResult.createResultValueType("Luminescence");
    rvt.setDescription("Desc2");
    rvt.setReplicateOrdinal(2);
    rvt.setTimePoint("0:10");
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setFollowUpData(true);
    rvt.setComments("None");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(1, rvt);

    rvt = expectedScreenResult.createResultValueType("FI");
    rvt.setDescription("Fold Induction");
    rvt.setReplicateOrdinal(1);
    rvt.setDerived(true);
    rvt.setHowDerived("Divide compound well by plate median");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(0));
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(2, rvt);

    rvt = expectedScreenResult.createResultValueType("FI");
    rvt.setDescription("Fold Induction");
    rvt.setReplicateOrdinal(2);
    rvt.setDerived(true);
    rvt.setHowDerived("Divide compound well by plate median");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(1));
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setFollowUpData(true);
    rvt.setNumeric(true);
    expectedResultValueTypes.put(3, rvt);

    rvt = expectedScreenResult.createResultValueType("AssayIndicator1", null, true, true, false, "Phenotype1");
    rvt.setHowDerived("Average");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(2));
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(3));
    rvt.setPositiveIndicatorType(PositiveIndicatorType.NUMERICAL);
    rvt.setPositiveIndicatorDirection(PositiveIndicatorDirection.HIGH_VALUES_INDICATE);
    rvt.setPositiveIndicatorCutoff(1.5);
    rvt.setNumeric(true);
    expectedResultValueTypes.put(4, rvt);

    rvt = expectedScreenResult.createResultValueType("AssayIndicator2", null, true, true, false, "Phenotype1");
    rvt.setHowDerived("W<=1.6, M<=1.7, S<=1.8");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(4));
    rvt.setPositiveIndicatorType(PositiveIndicatorType.PARTITION);
    rvt.setNumeric(false);
    expectedResultValueTypes.put(5, rvt);

    rvt = expectedScreenResult.createResultValueType("AssayIndicator3", null, true, true, false, "Phenotype1");
    rvt.setHowDerived("AssayIndicator2 is S");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(5));
    rvt.setPositiveIndicatorType(PositiveIndicatorType.BOOLEAN);
    rvt.setNumeric(false);
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
          { 1174576., 1469296., null, 5.8, 5.800, "3", "true" },
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
        setDefaultValues(expectedRvt);
        setDefaultValues(actualRvt);
        log.info("expectedRvt = " + expectedRvt.getName());
        log.info("actualRvt = " + actualRvt.getName());
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(actualRvt));

        // compare result values
        assertEquals(960, actualRvt.getResultValues().size());
        int iWell = 0;
        Map<Well,ResultValue> resultValues = actualRvt.getResultValues();
        for (Well well: new TreeSet<Well>(resultValues.keySet())) {
          ResultValue rv = resultValues.get(well);
          assertEquals("rvt " + iRvt + " well #" + iWell + " plate name",
                       expectedPlateNumbers[iWell],
                       new Integer(well.getPlateNumber()));
          assertEquals("rvt " + iRvt + " well #" + iWell + " well name",
                       expectedWellNames[iWell],
                       well.getWellName());
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

  public void testHitCounts() throws Exception
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
    List<Integer> expectedHitCount = Arrays.asList(4, 6, 3);
    List<Double> expectedHitRatio = Arrays.asList(expectedHitCount.get(0) / (double) nonExcludedResultValues,
                                                  expectedHitCount.get(1) / (double) nonExcludedResultValues,
                                                  expectedHitCount.get(2) / (double) nonExcludedResultValues);

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

  public void testIllegalScreenNumber()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(999);
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(screen,
                                 workbookFile);
    assertEquals("screen result data file is for screen number 115, expected 999",
                 mockScreenResultParser.getErrors().get(0).getErrorMessage());
  }

  public void testMultiCharColumnLabels()
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
      Map<Well,ResultValue> resultValues = rvt.getResultValues();
      assertEquals(rvt.getName() + " result value 0", 1000.0 + i, resultValues.get(new WellKey(1, "A01")).getNumericValue());
      assertEquals(rvt.getName() + " result value 1", 2000.0 + i, resultValues.get(new WellKey(1, "A02")).getNumericValue());
      assertEquals(rvt.getName() + " result value 2", 3000.0 + i, resultValues.get(new WellKey(1, "A03")).getNumericValue());
    }
    assertTrue("last is not derived from any", resultValueTypes.get(30 - 1).getDerivedTypes().isEmpty());
  }

  private void setDefaultValues(ResultValueType rvt)
  {
    if (rvt.getAssayPhenotype() == null) {
      rvt.setAssayPhenotype("");
    }
    if (rvt.getComments() == null) {
      rvt.setComments("");
    }
    if (rvt.getDescription() == null) {
      rvt.setDescription("");
    }
    if (rvt.getHowDerived() == null) {
      rvt.setHowDerived("");
    }
    if (rvt.getName() == null) {
      rvt.setName("");
    }
    if (rvt.getTimePoint() == null) {
      rvt.setTimePoint("");
    }
  }


  // testing  utility methods

  public static ScreenResult makeScreenResult(Date date)
  {
    Screen screen = makeScreen(date);
    ScreenResult screenResult = screen.createScreenResult(date);
    return screenResult;
  }

  private static Screen makeScreen(Date date)
  {
    ScreeningRoomUser screener = new ScreeningRoomUser(date,
                                                       "first",
                                                       "last",
                                                       "first_last@hms.harvard.edu",
                                                       "",
                                                       "",
                                                       "",
                                                       "",
                                                       "",
                                                       ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                       false);
    Screen screen = new Screen(screener,
                               screener,
                               1,
                               date,
                               ScreenType.SMALL_MOLECULE,
                               "test screen");
    return screen;
  }

}
