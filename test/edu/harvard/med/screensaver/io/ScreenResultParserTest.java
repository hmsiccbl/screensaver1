// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 */
public class ScreenResultParserTest extends AbstractSpringTest
{

  protected ScreenResultParser screenResultParser;

  protected void onSetUp() throws Exception {}

  protected void onTearDown() throws Exception {}
  
  @Override
  protected String[] getConfigLocations()
  {
    return new String[] {"spring-context-services.xml", "spring-context-screenresultparser-test.xml" };
  }

  /**
   * Tests basic usage of the HSSF API to read an Excel spreadsheet, but does
   * not Screensaver functionality. Basically, just a check that the technology
   * we're using actually works. Somewhat useful to keep around in case we
   * upgrade jar version, etc.
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

  public void testParseScreenResult() throws Exception
  {
    InputStream metadataIn = ScreenResultParserTest.class.getResourceAsStream("/edu/harvard/med/screensaver/io/258MetaData.xls");
    InputStream dataIn = ScreenResultParserTest.class.getResourceAsStream("/edu/harvard/med/screensaver/io/258_CBMicro1.xls");
    assertNotNull(metadataIn);
    assertNotNull(dataIn);

    ScreenResult screenResult = screenResultParser.parse(metadataIn, dataIn);
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
    SortedSet<ResultValueType> rvt2DerivedFrom = new TreeSet<ResultValueType>();
    rvt2DerivedFrom.add(rvt1);
    rvt2.setDerivedFrom(rvt2DerivedFrom);
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
    SortedSet<ResultValueType> rvt4DerivedFrom = new TreeSet<ResultValueType>();
    rvt4DerivedFrom.add(rvt1);
    rvt4DerivedFrom.add(rvt3);
    rvt4.setDerivedFrom(rvt4DerivedFrom);
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

    // 115
    // multiple replicates
    // multiple plates in single tab
    // 142
    // one tab per stock plate
    // has both raw and followup spreadsheets, both defined in metadata file
    // "meta" tab is 3rd

    // 126
    // seems like standard case
    // 180
    // multiple tabs in metadata file, 1 per raw data file
  }

  private void setDefaultValues(ResultValueType rvt) {
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
  
  // TODO: test errors
}
