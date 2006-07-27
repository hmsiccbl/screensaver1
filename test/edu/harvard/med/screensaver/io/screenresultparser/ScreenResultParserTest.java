// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresultparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresult.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseError;
import edu.harvard.med.screensaver.io.workbook.Workbook;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 */
public class ScreenResultParserTest extends AbstractSpringTest
{

  public static final File TEST_INPUT_FILE_DIR = new File("test/edu/harvard/med/screensaver/io/screenresultparser");
  
  protected ScreenResultParser screenResultParser;

  protected void onSetUp() throws Exception {}
  protected void onTearDown() throws Exception {
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
    String[] expectedSheetNames = new String[] { "PL-0500", "PL-0501", "PL-0502", "PL-0503" };
    String[] expectedHeaderRowValues = new String[] { "Stock ID", "Row", "Type", "Exclude", "Intensity_A", "Intensity_B", "Positive" };

    InputStream xlsInStream = ScreenResultParserTest.class.getResourceAsStream("324_500-503.xls");
    POIFSFileSystem fs = new POIFSFileSystem(xlsInStream);
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    int nSheets = wb.getNumberOfSheets();
    assertEquals("worksheet count", expectedSheetNames.length, nSheets);
    for (int i = 0; i < nSheets; i++) {
      String sheetName = wb.getSheetName(i);
      assertEquals("worksheet " + i + " name", expectedSheetNames[i], sheetName);
      HSSFSheet sheet = wb.getSheetAt(i);
      HSSFRow row = sheet.getRow(0);
      for (short iCell = 0; iCell < row.getPhysicalNumberOfCells(); ++iCell) {
        HSSFCell cell = row.getCell(iCell);
        assertEquals(expectedHeaderRowValues[iCell], cell.getStringCellValue());
      }
    }
  }
  
  // TODO: how do we instantiate a non-static inner class via reflection?
  // public void testCellParser() throws Exception {
  // ScreenResultParser screenResultParser = new ScreenResultParser(null, null);
  // Class cellValueParserClass =
  // Arrays.asList(screenResultParser.getClass().getDeclaredClasses();
  // Constructor constructor = cellValueParserClass.getConstructor(Map.class,
  // String.class);
  // }

  /**
   * Tests legacy file format, as well as testing most parsing cases (field
   * types and values). This is the most comprehensive test of low-level parsing
   * functionality.
   */
  public void testParseLegacyScreenResult() throws Exception
  {
    ScreenResult screenResult = screenResultParser.parse(new File(TEST_INPUT_FILE_DIR, "258MetaData.xls"));
    assertEquals(Collections.EMPTY_LIST, screenResultParser.getErrors());

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(2003, 3 - 1, 5, 0, 0, 0);
    expectedDate.set(Calendar.MILLISECOND, 0);
    ScreenResult expectedScreenResult = new ScreenResult(expectedDate.getTime());
    assertEquals("date",
                 expectedScreenResult.getDateCreated(),
                 screenResult.getDateCreated());
    assertEquals("replicate count", 2, screenResult.getReplicateCount()
                                                   .intValue());

    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt0 = new ResultValueType(expectedScreenResult,
                                               "Cherry Pick");
    rvt0.setDescription("Cherry Pick");
    rvt0.setCherryPick(true);
    rvt0.setActivityIndicator(true);
    rvt0.setActivityIndicatorType(ActivityIndicatorType.BOOLEAN);
    rvt0.setComments("Use this to determine cherry picks");

    ResultValueType rvt1 = new ResultValueType(expectedScreenResult,
                                               "Absorbance");
    rvt1.setDescription("Absorbance");
    rvt1.setReplicateOrdinal(1);
    rvt1.setTimePoint("0:10");
    rvt1.setAssayPhenotype("Human");

    ResultValueType rvt2 = new ResultValueType(expectedScreenResult, "FI");
    rvt2.setDescription("Fold Induction");
    rvt2.setReplicateOrdinal(1);
    rvt2.setDerived(true);
    rvt2.setHowDerived("Divide compound well by plate median");
    rvt2.addTypeDerivedFrom(rvt1);
    rvt2.setActivityIndicator(true);
    rvt2.setActivityIndicatorType(ActivityIndicatorType.NUMERICAL);
    rvt2.setIndicatorDirection(IndicatorDirection.LOW_VALUES_INDICATE);
    rvt2.setIndicatorCutoff(1.5);

    ResultValueType rvt3 = new ResultValueType(expectedScreenResult,
                                               "Absorbance");
    rvt3.setDescription("Absorbance");
    rvt3.setReplicateOrdinal(2);
    rvt3.setTimePoint("0:15");
    rvt3.setFollowUpData(true);
    rvt3.setAssayPhenotype("Mouse");
    rvt3.setComments("Generated during return visit");

    ResultValueType rvt4 = new ResultValueType(expectedScreenResult, "FI");
    rvt4.setDescription("Fold Induction");
    rvt4.setReplicateOrdinal(2);
    rvt4.setDerived(true);
    rvt4.setHowDerived("Divide compound well by plate median");
    rvt4.addTypeDerivedFrom(rvt1);
    rvt4.addTypeDerivedFrom(rvt3);
    rvt4.setActivityIndicator(true);
    rvt4.setActivityIndicatorType(ActivityIndicatorType.NUMERICAL);
    rvt4.setIndicatorDirection(IndicatorDirection.HIGH_VALUES_INDICATE);
    rvt4.setIndicatorCutoff(1.4);

    expectedResultValueTypes.put(0, rvt0);
    expectedResultValueTypes.put(1, rvt1);
    expectedResultValueTypes.put(2, rvt2);
    expectedResultValueTypes.put(3, rvt3);
    expectedResultValueTypes.put(4, rvt4);

    Integer[] expectedInitialPlateNumbers = { 686, 686, 686 };

    String[] expectedInitialWellNames = { "A03", "A04", "A05" };

    Object[][] expectedInitialResultValues = {
      {false, 1.05300000,  2.27922078, 0.88100000,  2.12801932},
      {true, 0.53100000,  1.14935065,  0.49600000,  1.19806763},
      {false, 0.56800000,  1.22943723,  0.45000000,  1.08695652}};

    Integer[] expectedFinalPlateNumbers = { 842, 842, 842 };

    String[] expectedFinalWellNames = { "P20", "P21", "P22" };

    Object[][] expectedFinalResultValues = {
      {false, 0.29600000,  1.07832423,  0.28600000,  1.00000000},
      {true, 0.30100000,  1.09653916,  0.29200000,  1.02097902},
      {false, 0.28000000,  1.02003643,  0.29900000,  1.04545455}};

    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int iRvt = 0;
    for (ResultValueType rvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(iRvt);
      if (expectedRvt != null) {
        setDefaultValues(expectedRvt);
        setDefaultValues(rvt);
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(rvt));

        // compare result values
        assertEquals(50240, rvt.getResultValues().size());
        int iWell = 0;
        for (ResultValue rv : rvt.getResultValues()) {
          assertEquals("rvt " + iRvt + " well #" + iWell + " plate name",
                       expectedInitialPlateNumbers[iWell],
                       rv.getWell()
                         .getPlateNumber());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well name",
                       expectedInitialWellNames[iWell],
                       rv.getWell()
                         .getWellName());
          if (iRvt == 0) {
            assertEquals("rvt " + iRvt + " well #" + iWell + " result value",
                         expectedInitialResultValues[iWell][iRvt].toString(),
                         rv.getValue().toString());
          }
          else {
            assertEquals("rvt " + iRvt + " well #" + iWell + " result value",
                         ((Double) expectedInitialResultValues[iWell][iRvt]).doubleValue(),
                         Double.parseDouble(rv.getValue()),
                         0.0001);
          }
          ++iWell;
          if (iWell == expectedInitialResultValues.length) {
            // done testing the initial rows of data, now jump to testing the
            // final rows of data
            break;
          }
        }
        List<ResultValue> listOfResultValues = new ArrayList<ResultValue>(rvt.getResultValues());
        int startIndex = rvt.getResultValues()
                            .size() - 3;
        iWell = 0;
        for (Iterator<ResultValue> iter = listOfResultValues.listIterator(startIndex); iter.hasNext();) {
          ResultValue rv = iter.next();
          assertEquals("rvt " + iRvt + " well #" + (iWell + startIndex) + " plate name",
                       expectedFinalPlateNumbers[iWell],
                       rv.getWell()
                         .getPlateNumber());
          assertEquals("rvt " + iRvt + " well #" + (iWell + startIndex) + " well name",
                       expectedFinalWellNames[iWell],
                       rv.getWell()
                         .getWellName());
          if (iRvt == 0) {
            assertEquals("rvt " + iRvt + " well #" + iWell + " result value",
                         expectedFinalResultValues[iWell][iRvt].toString(),
                         rv.getValue().toString());
          } 
          else {
            assertEquals("rvt " + iRvt + " well #" + (iWell + startIndex) + " result value",
                         ((Double) expectedFinalResultValues[iWell][iRvt]).doubleValue(),
                         Double.parseDouble(rv.getValue()),
                         0.0001);
          }
          ++iWell;
          if (iWell == expectedFinalResultValues.length) {
            break;
          }
        }
      }
      ++iRvt;
    }
  }
  
  /**
   * Test that ScreenResultParser can handle raw data from multiple workbooks,
   * where each workbook also uses multiple worksheets.
   */
  public void testParseLegacyMultiWorkbookMultiWorksheet()
  {
    ScreenResult screenResult = screenResultParser.parse(new File(TEST_INPUT_FILE_DIR, "464MetaData.xls"));
    assertEquals(Collections.EMPTY_LIST, screenResultParser.getErrors());
    Integer[] expectedPlateNumbers = { 1409, 1410, 1369, 1370, 1371, 1453, 1454 };
    Set<Integer> expectedPlateNumbersSet = new HashSet<Integer>(Arrays.asList(expectedPlateNumbers));
    Set<Integer> actualPlateNumbersSet = new HashSet<Integer>();
    // note: must generate actualPlateNumbersSet from *all* ResultValueTypes,
    // in case some have ResultValues that represent only a subset of all the
    // plate/wells.
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      for (ResultValue value : rvt.getResultValues()) {
        actualPlateNumbersSet.add(value.getWell().getPlateNumber());
      }
    }
    assertEquals(expectedPlateNumbersSet,
                 actualPlateNumbersSet);
  }
  

  /**
   * Tests that screen result errors are saved to a new set of workbooks.
   * @throws IOException 
   */
  public void testSaveScreenResultErrors() throws IOException
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, "metadata_with_errors.xls");
    screenResultParser.parse(workbookFile);
    String extension = "errors.xls";
    Set<Workbook> errorAnnotatedWorkbookFiles = screenResultParser.outputErrorsInAnnotatedWorkbooks(extension);

    for (Workbook workbook : errorAnnotatedWorkbookFiles) {
      if (workbook.getWorkbookFile().equals(workbookFile))
      {
        // test metadata workbook
        assertEquals(4,
                     workbook.getWorkbook().getNumberOfSheets());
        HSSFSheet sheet0 = workbook.getWorkbook().getSheetAt(0);
        HSSFSheet sheet1 = workbook.getWorkbook().getSheetAt(1);
        HSSFSheet sheet3 = workbook.getWorkbook().getSheetAt(3);
        assertEquals("ERROR: value required",
                     HSSFCellUtil.getCell(sheet0.getRow(8),'F' - 'A').getStringCellValue());
        assertEquals("ERROR: value required; unparseable value \"\" (expected one of [E])",
                     HSSFCellUtil.getCell(sheet0.getRow(9),'F' - 'A').getStringCellValue());
        assertEquals("ERROR: unparseable value \"maybe\" (expected one of [, 0, 1, false, n, no, true, y, yes])",
                     HSSFCellUtil.getCell(sheet0.getRow(16),'F' - 'A').getStringCellValue());
        assertEquals("ERROR: invalid cell type (expected a date)",
                     HSSFCellUtil.getCell(sheet1.getRow(7),'B' - 'A').getStringCellValue());
        assertEquals("Parse Errors",
                     workbook.getWorkbook().getSheetName(3));
        assertTrue(HSSFCellUtil.getCell(sheet3.getRow(0),'A' - 'A').getStringCellValue().
                   matches("could not read workbook '.*': test/edu/harvard/med/screensaver/io/screenresultparser/nonextant\\.xls \\(No such file or directory\\)"));
        
      }
      else if (workbook.getWorkbookFile().getName().equals("rawdata_with_errors.xls")) {
        // test raw data workbook
        assertEquals(1,
                     workbook.getWorkbook().getNumberOfSheets());
        HSSFSheet sheet0 = workbook.getWorkbook().getSheetAt(0);
        assertEquals("ERROR: unparseable plate number 'PLL-0001'",
                     HSSFCellUtil.getCell(sheet0.getRow(3),'A' - 'A').getStringCellValue());
      } 
      else {
        fail("workbook " + workbook.getWorkbookFile() + " contained errors, but should not have");
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
    File workbookFile = new File(TEST_INPUT_FILE_DIR, "metadata_with_errors.xls");
    screenResultParser.parse(workbookFile);
    Set<Cell> cellsWithErrors = new HashSet<Cell>();
    List<ParseError> errors = screenResultParser.getErrors();
    for (ParseError error : errors) {
      assertFalse("every error assigned to distinct cell",
                  cellsWithErrors.contains(error.getCell()));
      cellsWithErrors.add(error.getCell());
    }
  }
  
  /*
   * In fact, Spring provides us with the same parser instance for each test
   * (singleton="true"), but good to have an explicit test for parser reuse in
   * case this assumption changes.
   */
  public void testParserReuse() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, "metadata_with_errors.xls");
    ScreenResult result1 = screenResultParser.parse(workbookFile);
    List<ParseError> errors1 = screenResultParser.getErrors();
    ScreenResult result2 = screenResultParser.parse(workbookFile);
    List<ParseError> errors2 = screenResultParser.getErrors();
    assertNotNull("2nd parse returned a result", result2);
    assertNotSame("parses returned different ScreenResult objects", result1, result2);
    assertTrue(errors1.size() > 0);
    assertTrue(errors2.size() > 0);
    assertEquals("errors not accumulating across multiple parse() calls", errors1, errors2);
    // allow GC
    result1 = null;
    result2 = null;
    System.gc();

    // now test reading yet another spreadsheet, for which we can test the parsed result
    testParseLegacyScreenResult();
   }
  
  /**
   * Tests parsing of the new ScreenResult workbook format, which is an
   * "all-in-one" format, and has significant structural changes.
   * 
   * @throws Exception
   */
  public void testParseNewScreenResult() throws Exception
  {
    File workbookFile = new File(TEST_INPUT_FILE_DIR, "all-in-one.xls");
    ScreenResult screenResult = screenResultParser.parse(workbookFile);
    assertEquals(Collections.EMPTY_LIST, screenResultParser.getErrors());

    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(2006, 1 - 1, 1, 0, 0, 0);
    expectedDate.set(Calendar.MILLISECOND, 0);
    ScreenResult expectedScreenResult = new ScreenResult(expectedDate.getTime());
    assertEquals("date",
                 expectedScreenResult.getDateCreated(),
                 screenResult.getDateCreated());
    assertEquals("replicate count", 1, screenResult.getReplicateCount()
                                                   .intValue());

    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt0 = new ResultValueType(expectedScreenResult,
                                               "Luminescence");
    rvt0.setDescription("Luminescence");
    rvt0.setReplicateOrdinal(1);
    rvt0.setActivityIndicator(true);
    rvt0.setActivityIndicatorType(ActivityIndicatorType.NUMERICAL);
    rvt0.setIndicatorDirection(IndicatorDirection.HIGH_VALUES_INDICATE);
    rvt0.setIndicatorCutoff(1070000.0);
    rvt0.setAssayPhenotype("Human");
    rvt0.setComments("None");

    ResultValueType rvt1 = new ResultValueType(expectedScreenResult,
                                               "Cherry Pick");
    rvt1.setDescription("Cherry Pick");
    rvt1.setDerived(true);
    rvt1.setHowDerived("E > 1070000");
    rvt1.addTypeDerivedFrom(rvt0);
    rvt1.setActivityIndicator(true);
    rvt1.setActivityIndicatorType(ActivityIndicatorType.BOOLEAN);
    rvt1.setCherryPick(true);

    expectedResultValueTypes.put(0, rvt0);
    expectedResultValueTypes.put(1, rvt1);

    Integer[] expectedInitialPlateNumbers = {1, 1, 1, 2, 2, 2};

    String[] expectedInitialWellNames = {"A01", "A02", "A03", "A01", "A02", "A03"};

    Object[][] expectedInitialResultValues = {
      {1071894.0, true},
      {1071894.0, true},
      {1174576.0, false},
      {1089391.0, true},
      {1030000.0, false},
      {1020000.0, false}};
    
    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int iRvt = 0;
    for (ResultValueType rvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(iRvt);
      if (expectedRvt != null) {
        setDefaultValues(expectedRvt);
        setDefaultValues(rvt);
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(rvt));

        // compare result values
        assertEquals(6, rvt.getResultValues().size());
        int iWell = 0;
        for (ResultValue rv : rvt.getResultValues()) {
          assertEquals("rvt " + iRvt + " well #" + iWell + " plate name",
                       expectedInitialPlateNumbers[iWell],
                       rv.getWell()
                         .getPlateNumber());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well name",
                       expectedInitialWellNames[iWell],
                       rv.getWell()
                         .getWellName());
          if (iRvt == 1) {
            assertEquals("rvt " + iRvt + " well #" + iWell + " result value",
                         expectedInitialResultValues[iWell][iRvt].toString(),
                         rv.getValue());
          }
          else {
            assertEquals("rvt " + iRvt + " well #" + iWell + " result value",
                         ((Double) expectedInitialResultValues[iWell][iRvt]).doubleValue(),
                         Double.parseDouble(rv.getValue()),
                         0.0001);
          }
          ++iWell;
        }
      }
      ++iRvt;
    }
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

}
