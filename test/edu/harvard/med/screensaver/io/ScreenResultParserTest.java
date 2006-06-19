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
  public void testReadExcelSpreadsheet() throws Exception {
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

  public void testParseScreenResult() throws Exception {
    InputStream metadataIn = ScreenResultParserTest.class.getResourceAsStream("/edu/harvard/med/screensaver/io/115MetaData.xls");
    InputStream dataIn = ScreenResultParserTest.class.getResourceAsStream("/edu/harvard/med/screensaver/io/115_CBDivE_1to51.xls");
    assertNotNull(metadataIn);
    assertNotNull(dataIn);
    
    ScreenResult screenResult = screenResultParser.parse(metadataIn,
                                                         dataIn);
    assertEquals(Collections.EMPTY_LIST, screenResultParser.getErrors());
    
    Calendar expectedDate = Calendar.getInstance();
    expectedDate.set(2000, 7 - 1, 7, 0, 0, 0);
    expectedDate.set(Calendar.MILLISECOND, 0);
    ScreenResult expectedScreenResult = new ScreenResult(expectedDate.getTime());
    assertEquals("date", expectedScreenResult.getDateCreated(), screenResult.getDateCreated());
    assertEquals("replicate count", 4, screenResult.getReplicateCount().intValue());

    Map<Integer,ResultValueType> expectedResultValueTypes = new HashMap<Integer,ResultValueType>();

    ResultValueType rvt0 = new ResultValueType(
      expectedScreenResult,
      "Luminescence");
    rvt0.setDescription("Luminescence");
    rvt0.setReplicateOrdinal(1);

    ResultValueType rvt1 = new ResultValueType(
      expectedScreenResult,
      "Luminescence");
    rvt1.setDescription("Luminescence");
    rvt1.setReplicateOrdinal(2);

    @SuppressWarnings("unused") ResultValueType rvt2 = new ResultValueType(
      expectedScreenResult,
      "filler");

    @SuppressWarnings("unused") ResultValueType rvt3 = new ResultValueType(
      expectedScreenResult,
      "filler");

    ResultValueType rvt4 = new ResultValueType(
      expectedScreenResult,
      "FI");
    rvt4.setDescription("Fold Induction");
    rvt4.setReplicateOrdinal(1);
    rvt4.setHowDerived("Divide compound well by plate median");
    SortedSet<ResultValueType> rvt4DerivedFrom = new TreeSet<ResultValueType>();
    rvt4DerivedFrom.add(rvt0);
    rvt4.setDerivedFrom(rvt4DerivedFrom);

    ResultValueType rvt5 = new ResultValueType(
      expectedScreenResult,
      "FI");
    rvt5.setDescription("Fold Induction");
    rvt5.setReplicateOrdinal(2);
    rvt5.setHowDerived("Divide compound well by plate median");
    SortedSet<ResultValueType> rvt5DerivedFrom = new TreeSet<ResultValueType>();
    rvt5DerivedFrom.add(rvt1);
    rvt5.setDerivedFrom(rvt5DerivedFrom);

    expectedResultValueTypes.put(0, rvt0);
    expectedResultValueTypes.put(1, rvt1);
    expectedResultValueTypes.put(4, rvt4);
    expectedResultValueTypes.put(5, rvt5);
    
    Integer[] expectedInitialPlateNumbers = {
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1,
      1};

    String[] expectedInitialWellNames = {
      "A01",
      "A02",
      "A03",
      "A04",
      "A05",
      "A06",
      "A07",
      "A08",
      "A09",
      "A10"};
    
    double[][] expectedInitialResultValues = {
      {1071894, 1196906, 1033498, 1433458, 0.98, 1.11, 1.33, 2.28},
      {1174576, 1469296, 1076012, 1154580, 1.07, 1.36, 1.39, 1.83},
      {1294182, 1280934,  728706,  688634, 1.18, 1.19, 0.94, 1.09},
      {1158888, 1458878, 1086796,  726618, 1.06, 1.35, 1.4,  1.15},
      {1385142, 1383446, 1266540, 1002392, 1.26, 1.28, 1.63, 1.59},
      {1139144, 1481892,  959056,  775942, 1.04, 1.37, 1.24, 1.23},
      {1666646, 1154436, 1152980, 1098754, 1.52, 1.07, 1.49, 1.74},
      {1371892, 1521110, 1177946,  928598, 1.25, 1.41, 1.52, 1.47},
      {1285342, 1337506, 1080422,  947894, 1.17, 1.24, 1.39, 1.5},
      {1389320, 1354286, 1051664,  796924, 1.27, 1.25, 1.35, 1.27}};
    
    Integer[] expectedFinalPlateNumbers = {
      51,
      51,
      51};

    String[] expectedFinalWellNames = {
      "P18",
      "P19",
      "P20"};
    
    double [][] expectedFinalResultValues = {
      {1141108, 1271406, 0.0, 921460, 0.94, 1.08, 1.11},
      {1133060, 1159632, 0.0, 833304, 0.94, 0.98, 1.01},
      {1200236, 1169434, 0.0, 846736, 0.99, 0.99, 1.02}};
      

    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int iRvt = 0;
    for (ResultValueType rvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(iRvt);
      if (expectedRvt != null) {
        setDefaultValues(expectedRvt);
        setDefaultValues(rvt);
        assertTrue("ResultValueType " + iRvt, expectedRvt.isEquivalent(rvt));
        
        // compare result values
        assertEquals(16320, rvt.getResultValues().size());
        int iWell = 0;
        for (ResultValue rv : rvt.getResultValues() ) {
          assertEquals("rvt " + iRvt + " well #" + iWell + " plate name",
                       expectedInitialPlateNumbers[iWell],
                       rv.getWell().getPlateNumber());
          assertEquals("rvt " + iRvt + " well #" + iWell + " well name",
                       expectedInitialWellNames[iWell],
                       rv.getWell().getWellName());
          assertEquals("rvt " + iRvt + " well #" + iWell + " result value",
                       expectedInitialResultValues[iWell][iRvt],
                       Double.parseDouble(rv.getValue()));
          ++iWell;
          if (iWell == expectedInitialResultValues.length) {
            // done testing the initial rows of data, now jump to testing the final rows of data
            break;
          }
        }
        List<ResultValue> listOfResultValues = new ArrayList<ResultValue>(rvt.getResultValues());
        int startIndex = rvt.getResultValues().size() - 3;
        iWell = 0;
        for (Iterator<ResultValue> iter = listOfResultValues.listIterator(startIndex); iter.hasNext();) {
          ResultValue rv = iter.next();
          assertEquals("rvt " + iRvt + " well #" + (iWell + startIndex) + " plate name",
                       expectedFinalPlateNumbers[iWell],
                       rv.getWell().getPlateNumber());
          assertEquals("rvt " + iRvt + " well #" + (iWell + startIndex) + " well name",
                       expectedFinalWellNames[iWell],
                       rv.getWell().getWellName());
          assertEquals("rvt " + iRvt + " well #" + (iWell + startIndex) + " result value",
                       expectedFinalResultValues[iWell][iRvt],
                       Double.parseDouble(rv.getValue()));
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
