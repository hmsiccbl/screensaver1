// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;

public class ScreenResultExporterTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(ScreenResultExporterTest.class);
  
  protected ScreenResultParser screenResultParser;
    
  /**
   * Tests the ScreenResultExporter by exporting to a file, parsing the exported
   * file, and comparing to the original, exported ScreenResult.
   * 
   * @throws Exception
   */
  public void testScreenResultExporter() throws Exception
  {
    ScreenResult originalScreenResult  = screenResultParser.parseLegacy(new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "LegacyTestAllInOne.xls"), false);
    ScreenResultExporter exporter = new ScreenResultExporter();
    HSSFWorkbook workbook = exporter.build(originalScreenResult);
    File exportedFile = File.createTempFile("LegacyTestAllInOne", ".exported.xls");
    workbook.write(new FileOutputStream(exportedFile));
    ScreenResult exportedScreenResult  = screenResultParser.parse(exportedFile); // parse with "new" format
    if (screenResultParser.getHasErrors()) {
      // okay, so I'm using our unit test to help with debugging...sue me!
      log.debug(screenResultParser.getErrors());
    }
    assertFalse("parse errors on exported screen result", screenResultParser.getHasErrors());
    exportedFile.deleteOnExit(); // delete only after confirmation of no errors, to allow developers to inspect file if parse errors encountered
    
    assertEquals("one data worksheet per assay plate: PL-00001", "PL-00001", workbook.getSheetName(2));
    HSSFSheet sheetPlate1 = workbook.getSheetAt(2);
    assertEquals(320, sheetPlate1.getLastRowNum());
    assertEquals("PL-00001", HSSFCellUtil.getCell(HSSFCellUtil.getRow(1,sheetPlate1),0).getStringCellValue());
    assertEquals("PL-00001", HSSFCellUtil.getCell(HSSFCellUtil.getRow(320,sheetPlate1),0).getStringCellValue());

    assertEquals("one data worksheet per assay plate: PL-00002", "PL-00002", workbook.getSheetName(3));
    HSSFSheet sheetPlate2 = workbook.getSheetAt(3);
    assertEquals(320, sheetPlate2.getLastRowNum());
    assertEquals("PL-00002", HSSFCellUtil.getCell(HSSFCellUtil.getRow(1,sheetPlate2),0).getStringCellValue());
    assertEquals("PL-00002", HSSFCellUtil.getCell(HSSFCellUtil.getRow(320,sheetPlate2),0).getStringCellValue());

    assertEquals("one data worksheet per assay plate: PL-00003", "PL-00003", workbook.getSheetName(4));
    HSSFSheet sheetPlate3 = workbook.getSheetAt(4);
    assertEquals(320, sheetPlate3.getLastRowNum());
    assertEquals("PL-00003", HSSFCellUtil.getCell(HSSFCellUtil.getRow(1,sheetPlate3),0).getStringCellValue());
    assertEquals("PL-00003", HSSFCellUtil.getCell(HSSFCellUtil.getRow(320,sheetPlate3),0).getStringCellValue());

    
    List<ResultValueType> expectedRvts = originalScreenResult.generateResultValueTypesList();
    List<ResultValueType> actualRvts = exportedScreenResult.generateResultValueTypesList();
    Iterator expectedIter = expectedRvts.iterator();
    Iterator actualIter = actualRvts.iterator(); 
    while (expectedIter.hasNext() && actualIter.hasNext()) {
      ResultValueType expectedRvt = (ResultValueType) expectedIter.next();
      ResultValueType actualRvt = (ResultValueType) actualIter.next();
      assertTrue(expectedRvt.isEquivalent(actualRvt));
      SortedSet<ResultValue> expectedResultValues = expectedRvt.getResultValues();
      SortedSet<ResultValue> actualResultValues = actualRvt.getResultValues();
      Iterator expectedIter2 = expectedResultValues.iterator();
      Iterator actualIter2 = actualResultValues.iterator(); 
      int i = 0;
      while (expectedIter2.hasNext() && actualIter2.hasNext()) {
        ResultValue expectedRv = (ResultValue) expectedIter2.next();
        ResultValue actualRv = (ResultValue) actualIter2.next();
        assertTrue("RVT " + expectedRvt.getName() + " result value " + i,
                   expectedRv.isEquivalent(actualRv));
        ++i;
      }
    }
  }
}
