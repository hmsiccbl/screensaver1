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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;

public class ScreenResultExporterTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(ScreenResultExporterTest.class);
  
  protected ScreenResultParser mockScreenResultParser;
  

  /**
   * Tests the ScreenResultExporter by exporting to a file, parsing the exported
   * file, and comparing to the original, exported ScreenResult.
   * 
   * @throws Exception
   */
  public void testScreenResultExporter() throws Exception
  {
    ScreenResult originalScreenResult = 
      mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115), 
                                         new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                  ScreenResultParserTest.SCREEN_RESULT_115_TEST_WORKBOOK_FILE));
    addDummyCollaboratorsToScreen(originalScreenResult);
    
    ScreenResultExporter exporter = new ScreenResultExporter();
    HSSFWorkbook workbook = exporter.build(originalScreenResult);
    File exportedFile = File.createTempFile(ScreenResultParserTest.SCREEN_RESULT_115_TEST_WORKBOOK_FILE, ".exported.xls");
    workbook.write(new FileOutputStream(exportedFile));
    ScreenResult exportedScreenResult  = mockScreenResultParser.parse(MakeDummyEntities.makeDummyScreen(115), 
                                                                      exportedFile);
    if (mockScreenResultParser.getHasErrors()) {
      // okay, so I'm using our unit test to help with debugging...sue me!
      log.debug(mockScreenResultParser.getErrors());
    }
    assertFalse("parse errors on exported screen result", mockScreenResultParser.getHasErrors());
    // delete only after confirmation of no errors, to allow developers to inspect file if parse errors encountered
    exportedFile.deleteOnExit(); 
    
    // test screen info sheet 
    // TODO: make comprehensive
    assertEquals("screen number", 
                 115,
                 (int)
                 HSSFCellUtil.getCell(HSSFCellUtil.getRow(0, workbook.getSheetAt(0)), 1).getNumericCellValue());
    assertEquals("screen title", 
                 "Dummy screen",
                 HSSFCellUtil.getCell(HSSFCellUtil.getRow(1, workbook.getSheetAt(0)), 1).getStringCellValue());
    assertEquals("collaborators", 
                 "Chris Collaborator, Cindy Collaborator",
                 HSSFCellUtil.getCell(HSSFCellUtil.getRow(5, workbook.getSheetAt(0)), 1).getStringCellValue());
    assertEquals("email", 
                 "lead_screener_115@hms.harvard.edu",
                 HSSFCellUtil.getCell(HSSFCellUtil.getRow(8, workbook.getSheetAt(0)), 1).getStringCellValue());
    
    // test data sheets
    
    assertEquals("one data worksheet per assay plate: PL-00001", "PL-00001", workbook.getSheetName(2));
    HSSFSheet sheetPlate1 = workbook.getSheetAt(2);
    assertEquals(384, sheetPlate1.getLastRowNum());
    assertEquals("PL-00001", HSSFCellUtil.getCell(HSSFCellUtil.getRow(1,sheetPlate1),0).getStringCellValue());
    assertEquals("PL-00001", HSSFCellUtil.getCell(HSSFCellUtil.getRow(384,sheetPlate1),0).getStringCellValue());

    assertEquals("one data worksheet per assay plate: PL-00002", "PL-00002", workbook.getSheetName(3));
    HSSFSheet sheetPlate2 = workbook.getSheetAt(3);
    assertEquals(384, sheetPlate2.getLastRowNum());
    assertEquals("PL-00002", HSSFCellUtil.getCell(HSSFCellUtil.getRow(1,sheetPlate2),0).getStringCellValue());
    assertEquals("PL-00002", HSSFCellUtil.getCell(HSSFCellUtil.getRow(384,sheetPlate2),0).getStringCellValue());

    assertEquals("one data worksheet per assay plate: PL-00003", "PL-00003", workbook.getSheetName(4));
    HSSFSheet sheetPlate3 = workbook.getSheetAt(4);
    assertEquals(384, sheetPlate3.getLastRowNum());
    assertEquals("PL-00003", HSSFCellUtil.getCell(HSSFCellUtil.getRow(1,sheetPlate3),0).getStringCellValue());
    assertEquals("PL-00003", HSSFCellUtil.getCell(HSSFCellUtil.getRow(384,sheetPlate3),0).getStringCellValue());

    
    List<ResultValueType> expectedRvts = originalScreenResult.getResultValueTypesList();
    List<ResultValueType> actualRvts = exportedScreenResult.getResultValueTypesList();
    Iterator expectedIter = expectedRvts.iterator();
    Iterator actualIter = actualRvts.iterator(); 
    while (expectedIter.hasNext() && actualIter.hasNext()) {
      ResultValueType expectedRvt = (ResultValueType) expectedIter.next();
      ResultValueType actualRvt = (ResultValueType) actualIter.next();
      assertTrue(expectedRvt.isEquivalent(actualRvt));
      Map<WellKey,ResultValue> expectedResultValues = new HashMap<WellKey,ResultValue>(expectedRvt.getWellKeyToResultValueMap());
      Map<WellKey,ResultValue> actualResultValues = new HashMap<WellKey,ResultValue>(actualRvt.getWellKeyToResultValueMap());
      for (WellKey wellKey : expectedResultValues.keySet()) {
        ResultValue expectedRv = (ResultValue) expectedResultValues.get(wellKey);
        ResultValue actualRv = (ResultValue) actualResultValues.get(wellKey);
        assertNotNull("result value exists", actualRv);
        // TODO: reinstate string value comparison after exporter is made to set the numeric cell style
//        assertEquals("RVT " + expectedRvt.getName() + " well + " + wellKey + " result value (as string) ", 
//                     expectedRv.getValue(), 
//                     actualRv.getValue());
        assertEquals("RVT " + expectedRvt.getName() + " well + " + wellKey + " result value (typed value) ", 
                     ResultValue.getTypedValue(expectedRv, expectedRvt),
                     ResultValue.getTypedValue(actualRv, expectedRvt));
        actualResultValues.remove(wellKey);
      }
    }
  }


  private void addDummyCollaboratorsToScreen(ScreenResult screenResult)
  {
    ScreeningRoomUser collaborator1 = new ScreeningRoomUser(new Date(),
                                                            "Cindy",
                                                            "Collaborator",
                                                            "cindy_collaborator_"
                                                            + screenResult.getScreen().getScreenNumber()
                                                            + "@hms.harvard.edu",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            ScreeningRoomUserClassification.OTHER,
                                                            true);
    ScreeningRoomUser collaborator2 = new ScreeningRoomUser(new Date(),
                                                            "Chris",
                                                            "Collaborator",
                                                            "chris_collaborator_"
                                                            + screenResult.getScreen().getScreenNumber()
                                                            + "@hms.harvard.edu",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            ScreeningRoomUserClassification.OTHER,
                                                            true);

    screenResult.getScreen().addCollaborator(collaborator1);
    screenResult.getScreen().addCollaborator(collaborator2);
  }
}
