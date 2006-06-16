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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 */
public class ScreenResultParserTest extends TestCase
{

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
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
  // ScreenResultParser parser = new ScreenResultParser(null, null);
  // Class cellValueParserClass =
  // Arrays.asList(parser.getClass().getDeclaredClasses();
  // Constructor constructor = cellValueParserClass.getConstructor(Map.class,
  // String.class);
  // }

  public void testParseScreenResult() throws Exception {
    InputStream metadataIn = ScreenResultParserTest.class.getResourceAsStream("115MetaData.xls");
    InputStream dataIn = ScreenResultParserTest.class.getResourceAsStream("115_CBDivE_1to51.xls");
    ScreenResultParser parser = new ScreenResultParser(metadataIn, dataIn);
    ScreenResult screenResult = parser.parse();
    assertEquals(Collections.EMPTY_LIST, parser.getErrors());

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

    SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
    int i = 0;
    for (ResultValueType rvt : resultValueTypes) {
      ResultValueType expectedRvt = expectedResultValueTypes.get(i);
      if (expectedRvt != null) {
        setDefaultValues(expectedRvt);
        setDefaultValues(rvt);
        assertTrue("ResultValueType " + i, expectedRvt.isEquivalent(rvt));
      }
      ++i;
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
