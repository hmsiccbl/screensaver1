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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.util.DateUtil;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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
  public static final String ERRORS_TEST_WORKBOOK_FILE = "NewFormatErrorsTest.xls";
  public static final String FORMULA_VALUE_TEST_WORKBOOK_FILE = "formula_value.xls";
  public static final String BLANK_ROWS_TEST_WORKBOOK_FILE = "ScreenResultTest115_blank_rows.xls";
  
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
   * Tests basic usage of the HSSF API to read an Excel spreadsheet, but does
   * not test Screensaver-related functionality. Basically, just a check that
   * the technology we're using actually works. Somewhat useful to keep around
   * in case we upgrade jar version, etc.
   */
  public void testReadExcelSpreadsheet() throws Exception 
  {
    // TODO: for sheet names (at least) underscores appear to converted to dashes by HSSF
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
    POIFSFileSystem fs = new POIFSFileSystem(xlsInStream);
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    int nSheets = wb.getNumberOfSheets();
    assertEquals("worksheet count", expectedSheetNames.length, nSheets);
    for (int i = 0; i < nSheets; i++) {
      String sheetName = wb.getSheetName(i);
      assertEquals("worksheet " + i + " name", expectedSheetNames[i], sheetName);
      HSSFSheet sheet = wb.getSheetAt(i);
      HSSFRow row = sheet.getRow(0);
      if (i >= 2) {
        for (short iCell = 0; iCell < row.getPhysicalNumberOfCells(); ++iCell) {
          HSSFCell cell = row.getCell(iCell);
          assertEquals(expectedHeaderRowValues[iCell], cell.getStringCellValue());
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
    POIFSFileSystem fs = new POIFSFileSystem(xlsInStream);
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    HSSFSheet sheet = wb.getSheetAt(0);
    HSSFRow row = sheet.getRow(0);
    HSSFCell hssfNumericFormulaCell = row.getCell((short) 2);
    assertEquals("HSSF cell type",
                 HSSFCell.CELL_TYPE_FORMULA,
                 hssfNumericFormulaCell.getCellType());
    double hssfNumericValue = hssfNumericFormulaCell.getNumericCellValue();
    assertEquals("HSSF numeric value", 2.133, hssfNumericValue, 0.0001);
    String hssfFormula = hssfNumericFormulaCell.getCellFormula();
    assertEquals("HSSF formula", "A1+B1", hssfFormula);
    HSSFDataFormat dataFormat = wb.createDataFormat();
    String format = dataFormat.getFormat(hssfNumericFormulaCell.getCellStyle().getDataFormat());
    assertEquals("HSSF numeric data format precision", "0.0000", format);

    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(TEST_INPUT_FILE_DIR, 
                                              FORMULA_VALUE_TEST_WORKBOOK_FILE), errors);
    Cell.Factory cellFactory = new Cell.Factory(workbook, 0, errors);
    Cell cell = cellFactory.getCell((short) 2, (short) 0, false);
    assertNotNull(cell);
    Double parsedNumericValue = cell.getDouble();
    assertEquals("parse numeric value",
                 hssfNumericValue,
                 parsedNumericValue.doubleValue(),
                 0.0001);

    // test numeric precision (TODO: should probably be a separate unit test)
    Cell numericFormatFormulaCell = cellFactory.getCell((short) 2, (short) 0, false);
    assertEquals("precision of numeric format on formula cell", 4, 
                 numericFormatFormulaCell.getDoublePrecision());
    Cell generalFormatFormulaCell = cellFactory.getCell((short) 3, (short) 0);
    assertEquals("precision of general format on formula cell", -1, 
                 generalFormatFormulaCell.getDoublePrecision());
    Cell generalFormatNumericCell = cellFactory.getCell((short) 0, (short) 0);
    assertEquals("precision of general format on numeric cell", -1, 
                 generalFormatNumericCell.getDoublePrecision());
    Cell numericFormatNumericCell = cellFactory.getCell((short) 1, (short) 0);
    assertEquals("precision of numeric format on numeric cell", 3, 
                 numericFormatNumericCell.getDoublePrecision());
    Cell integerNumericFormatNumericCell = cellFactory.getCell((short) 4, (short) 0);
    assertEquals("precision of integer number format on numeric cell", 0, 
                 integerNumericFormatNumericCell.getDoublePrecision());
    Cell percentageNumericCell = cellFactory.getCell((short) 5, (short) 0);
    assertEquals("precision of percentage number format on numeric cell", 3, 
                 percentageNumericCell.getDoublePrecision());
  }
  
  public void testDetectEmptyRow() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, BLANK_ROWS_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(MockDaoForScreenResultImporter.makeDummyScreen(115), workbookFile);
    assertFalse("screen result had no errors", mockScreenResultParser.getHasErrors());
    assertEquals("well count", 4, mockScreenResultParser.getParsedScreenResult().getExperimentalWellCount());
  }
  
  // Note: this test cannot pass because the POI/HSSF library is crappy and does
  // not allow you to read a boolean-typed formula cell value!
  public void testReadBooleanFormulaCellValue() throws Exception
  {
    InputStream xlsInStream = 
      ScreenResultParserTest.class.getResourceAsStream(FORMULA_VALUE_TEST_WORKBOOK_FILE);
    POIFSFileSystem fs = new POIFSFileSystem(xlsInStream);
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    HSSFSheet sheet = wb.getSheetAt(0);
    HSSFRow row = sheet.getRow(0);
    HSSFCell hssfBooleanFormulaCell = row.getCell((short) 3);
    assertEquals("HSSF cell type",
                 HSSFCell.CELL_TYPE_FORMULA,
                 hssfBooleanFormulaCell.getCellType());
    boolean hssfBooleanValue = hssfBooleanFormulaCell.getBooleanCellValue();
    assertEquals("HSSF numeric value", true, hssfBooleanValue);
    String hssfFormula = hssfBooleanFormulaCell.getCellFormula();
    assertEquals("HSSF formula", "C1=2", hssfFormula);
    
    ParseErrorManager errors = new ParseErrorManager();
    Workbook workbook = new Workbook(new File(TEST_INPUT_FILE_DIR, 
                                              FORMULA_VALUE_TEST_WORKBOOK_FILE), errors);
    Cell.Factory cellFactory = new Cell.Factory(workbook, 0, errors);
    Cell cell = cellFactory.getCell((short) 3, (short) 0, false);
    assertNotNull(cell);
    assertEquals("parse boolean value",
                 hssfBooleanValue,
                 cell.getBoolean().booleanValue());
    
  }
  
  /**
   * Tests that screen result errors are saved to a new set of workbooks.
   * @throws IOException 
   */
  public void testSaveScreenResultErrors() throws IOException
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(MockDaoForScreenResultImporter.makeDummyScreen(115), workbookFile);
    String extension = "errors.xls";
    Map<Workbook,File> workbook2File =
      mockScreenResultParser.outputErrorsInAnnotatedWorkbooks(new File(System.getProperty("java.io.tmpdir")),
                                                          extension);

    for (Workbook workbook : workbook2File.keySet()) {
      if (workbook.getWorkbookFile().equals(workbookFile))
      {
        // test metadata workbook
        assertEquals(5,
                     workbook.getWorkbook().getNumberOfSheets());
//        HSSFSheet sheet0 = workbook.getWorkbook().getSheetAt(0);
//        HSSFSheet sheet1 = workbook.getWorkbook().getSheetAt(1);
//        HSSFSheet sheet3 = workbook.getWorkbook().getSheetAt(3);
        
        // TODO: implement asserts
//        assertEquals("ERROR: value required",
//                     HSSFCellUtil.getCell(sheet0.getRow(8),'F' - 'A').getStringCellValue());
//        assertEquals("ERROR: value required; unparseable value \"\" (expected one of [E])",
//                     HSSFCellUtil.getCell(sheet0.getRow(9),'F' - 'A').getStringCellValue());
//        assertEquals("ERROR: unparseable value \"maybe\" (expected one of [, 0, 1, false, n, no, true, y, yes])",
//                     HSSFCellUtil.getCell(sheet0.getRow(16),'F' - 'A').getStringCellValue());
//        assertEquals("ERROR: invalid cell type (expected a date)",
//                     HSSFCellUtil.getCell(sheet1.getRow(7),'B' - 'A').getStringCellValue());
//        assertEquals("Parse Errors",
//                     workbook.getWorkbook().getSheetName(3));
//        assertEquals("ERROR: unparseable plate number 'PLL-0001'",
//                     HSSFCellUtil.getCell(sheet0.getRow(3),'A' - 'A').getStringCellValue());
      }
    }
  }
  
  /**
   * Tests that Cells are cloned when needed (a single Cell is generally
   * recycled, as an optimization). Note that this test assumes that the test
   * workbooks do not have more than 1 error per cell, which is a possibility in
   * the real world, but would break our naive test. (I suppose we could also
   * test simply that at least some of our ParseErrors' cells were different.)
   */
  public void testRecycledCellUsage() 
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(MockDaoForScreenResultImporter.makeDummyScreen(115), workbookFile);
    Set<Cell> cellsWithErrors = new HashSet<Cell>();
    List<ParseError> errors = mockScreenResultParser.getErrors();
    for (ParseError error : errors) {
      assertFalse("every error assigned to distinct cell",
                  cellsWithErrors.contains(error.getCell()));
      cellsWithErrors.add(error.getCell());
    }
  }
  
  public void testParserReuse() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, ERRORS_TEST_WORKBOOK_FILE);
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(115);
    ScreenResult result1 = mockScreenResultParser.parse(screen, workbookFile);
    List<ParseError> errors1 = mockScreenResultParser.getErrors();
    assertNotNull("1st parse returns a result", result1);
    ScreenResult result2 = mockScreenResultParser.parse(screen, workbookFile);
    List<ParseError> errors2 = mockScreenResultParser.getErrors();
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

    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(115);
    try {
      new LibraryScreening(screen,
                           screen.getLeadScreener(),
                           DateUtil.makeDate(2007, 1, 1),
                           AssayProtocolType.PRELIMINARY);
    }
    catch (DuplicateEntityException e) {
      e.printStackTrace();
    }
    
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_NO_DATE_TEST_WORKBOOK_FILE);
    ScreenResult screenResult = mockScreenResultParser.parse(screen,
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    Date expectedDate = DateUtil.makeDate(2007, 1, 1);
    ScreenResult expectedScreenResult = makeScreenResult(expectedDate);
    assertEquals("date",
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
    ScreenResult screenResult = mockScreenResultParser.parse(MockDaoForScreenResultImporter.makeDummyScreen(115), 
                                                             workbookFile);
    assertEquals(Collections.EMPTY_LIST, mockScreenResultParser.getErrors());

    Date expectedDate = DateUtil.makeDate(2006, 1, 1);
    ScreenResult expectedScreenResult = makeScreenResult(expectedDate);
    assertEquals("date",
                 expectedScreenResult.getDateCreated(),
                 screenResult.getDateCreated());
    assertEquals("replicate count", 2, screenResult.getReplicateCount().intValue());

    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt;
    
    rvt = new ResultValueType(expectedScreenResult, "Luminescence");
    rvt.setDescription("Desc1");
    rvt.setReplicateOrdinal(1);
    rvt.setTimePoint("0:10");
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    rvt.setActivityIndicator(false);
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setComments("None");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(0, rvt);

    rvt = new ResultValueType(expectedScreenResult, "Luminescence");
    rvt.setDescription("Desc2");
    rvt.setReplicateOrdinal(2);
    rvt.setTimePoint("0:10");
    rvt.setAssayReadoutType(AssayReadoutType.LUMINESCENCE);
    rvt.setActivityIndicator(false);
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setFollowUpData(true);
    rvt.setComments("None");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(1, rvt);

    rvt = new ResultValueType(expectedScreenResult, "FI");
    rvt.setDescription("Fold Induction");
    rvt.setReplicateOrdinal(1);
    rvt.setDerived(true);
    rvt.setHowDerived("Divide compound well by plate median");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(0));
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(2, rvt);

    rvt = new ResultValueType(expectedScreenResult, "FI");
    rvt.setDescription("Fold Induction");
    rvt.setReplicateOrdinal(2);
    rvt.setDerived(true);
    rvt.setHowDerived("Divide compound well by plate median");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(1));
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setFollowUpData(true);
    rvt.setNumeric(true);
    expectedResultValueTypes.put(3, rvt);

    rvt = new ResultValueType(expectedScreenResult, "AssayIndicator1");
    rvt.setDerived(true);
    rvt.setHowDerived("Average");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(2));
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(3));
    rvt.setActivityIndicator(true);
    rvt.setActivityIndicatorType(ActivityIndicatorType.NUMERICAL);
    rvt.setIndicatorDirection(IndicatorDirection.HIGH_VALUES_INDICATE);
    rvt.setIndicatorCutoff(1.5);
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setNumeric(true);
    expectedResultValueTypes.put(4, rvt);

    rvt = new ResultValueType(expectedScreenResult, "AssayIndicator2");
    rvt.setDerived(true);
    rvt.setHowDerived("W<=1.6, M<=1.7, S<=1.8");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(4));
    rvt.setActivityIndicator(true);
    rvt.setActivityIndicatorType(ActivityIndicatorType.PARTITION);
    rvt.setAssayPhenotype("Phenotype1");
    rvt.setNumeric(false);
    expectedResultValueTypes.put(5, rvt);

    rvt = new ResultValueType(expectedScreenResult, "AssayIndicator3");
    rvt.setDerived(true);
    rvt.setHowDerived("AssayIndicator2 is S");
    rvt.addTypeDerivedFrom(expectedResultValueTypes.get(5));
    rvt.setActivityIndicator(true);
    rvt.setActivityIndicatorType(ActivityIndicatorType.BOOLEAN);
    rvt.setAssayPhenotype("Phenotype1");
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
          { 1071894., 1196906., 0.98, 1.11, 1.045, "", "true" },
          { 1174576., 1469296., null, 5.8, 5.800, "S", "true" },
          { 1294182., 1280934., 1.18, 1.19, 1.185, "", "false" },
          { 1158888., 1458878., 1.06, 1.35, 1.205, "W", "false" },
          { 1385142., 1383446., 1.26, 1.28, 1.270, "W", "false" },
          { null, null, null, null, null, "", false },
          { 1666646., 1154436., 1.52, 1.07, 1.295, "W", "false" },
          { null, null, null, null, null, "", false } };

    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int iRvt = 0;
    for (ResultValueType actualRvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(iRvt);
      if (expectedRvt != null) {
        setDefaultValues(expectedRvt);
        setDefaultValues(actualRvt);
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(actualRvt));

        // compare result values
        assertEquals(960, actualRvt.getResultValues().size());
        int iWell = 0;
        Map<WellKey,ResultValue> resultValues = actualRvt.getResultValues();
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
  
  public void testIllegalScreenNumber()
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(999);
    File workbookFile = new File(TEST_INPUT_FILE_DIR, SCREEN_RESULT_115_TEST_WORKBOOK_FILE);
    mockScreenResultParser.parse(screen,
                                 workbookFile);
    assertEquals("screen result data file is for screen number 115, expected 999",
                 mockScreenResultParser.getErrors().get(0).getMessage());
  }
    
  public void testMultiCharColumnLabels()
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(115);
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
      Map<WellKey,ResultValue> resultValues = rvt.getResultValues();
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
    ScreenResult screenResult = new ScreenResult(screen, date);
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
