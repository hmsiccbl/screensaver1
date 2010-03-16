// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/reports/icbg/ICBGReportGenerator.java $
// $Id: ICBGReportGenerator.java 2587 2008-07-21 12:00:03Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.reports.icbg;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;


/**
 * Generates the ICCB-L portion of the ICBG Napis report.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ICBGReportGenerator
{

  // static fields
  
  private static Logger log = Logger.getLogger(ICBGReportGenerator.class);
  private static final String REPORT_FILENAME = "report.xls";
  
  private static final int MAX_ROWS_PER_SHEET = /*10 DEBUG ONLY!*/ 65000;
  
  
  // public static methods
  
  /**
   * Generate the report.
   * 
   * @param args unused
   */
  public static void main(String[] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    GenericEntityDAO dao = (GenericEntityDAO) context.getBean("genericEntityDao");
    ScreenResultsDAO screenResultsDAO = (ScreenResultsDAO) context.getBean("screenResultsDao");
    ICCBLPlateWellToINBioLQMapper mapper = new ICCBLPlateWellToINBioLQMapper();
    ICBGReportGenerator generator = new ICBGReportGenerator(
      dao,
      screenResultsDAO,
      mapper,
      new AssayInfoProducer());
    HSSFWorkbook report = generator.produceReport();
    
    log.info("writing report..");
    try {
      report.write(new FileOutputStream(REPORT_FILENAME));
      log.info("report written.");
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error("writing report generated error: " + e.getMessage());
    }
  }
  
  
  // instance fields

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDAO;
  private ICCBLPlateWellToINBioLQMapper _mapper;
  private AssayInfoProducer _assayInfoProducer;
  private HSSFWorkbook _report;
  private int _currentBioactivityRow = 1;
  private int _currentProtocolRow = 1;
  private int _currentBioactivitySheetIndex = 1;
  private HSSFSheet _currentBioactivitySheet;
  
  
  // public constructor and instance methods
  
  public ICBGReportGenerator(
    GenericEntityDAO dao,
    ScreenResultsDAO screenResultsDAO,
    ICCBLPlateWellToINBioLQMapper mapper,
    AssayInfoProducer assayInfoProducer)
  {
    _dao = dao;
    _screenResultsDAO = screenResultsDAO;
    _mapper = mapper;
    _assayInfoProducer = assayInfoProducer;
  }
  
  public HSSFWorkbook produceReport()
  {
    initializeReport();
    parseScreenResults();
    return _report;
  }
  
  
  // private instance methods
  
  private void initializeReport()
  {
    _report = new HSSFWorkbook();
    HSSFSheet sheet;
    HSSFRow row;
    sheet = _report.createSheet("PROTOCOL");
    row = sheet.createRow(0);
    row.createCell((short) 0).setCellValue("PROTOCOL_ID");
    row.createCell((short) 1).setCellValue("PROTOCOL_TYPE");
    row.createCell((short) 2).setCellValue("PROTOCOL_DESCR");
    row.createCell((short) 3).setCellValue("P_NOTE");
    sheet = _report.createSheet("COMPOUND");
    row = sheet.createRow(0);
    row.createCell((short) 0).setCellValue("COMPOUND_ID");
    row.createCell((short) 1).setCellValue("MATERIAL_ID");
    row.createCell((short) 2).setCellValue("CL_EXTRA_1");
    row.createCell((short) 3).setCellValue("MOLSTRUCTURE_FILE");
  }
  
  private HSSFSheet createBioactivitySheet()
  {
   HSSFSheet sheet;
    HSSFRow row;
    sheet = _report.createSheet("BIOACTIVITY" + _currentBioactivitySheetIndex);
    row = sheet.createRow(0);
    row.createCell((short) 0).setCellValue("MATERIAL_ID");
    row.createCell((short) 1).setCellValue("PLATE_ID");
    row.createCell((short) 2).setCellValue("WELL_ID");
    row.createCell((short) 3).setCellValue("ASSAY_CATEGORY");
    row.createCell((short) 4).setCellValue("ASSAY_NAME");
    row.createCell((short) 5).setCellValue("ACTIVITY");
    row.createCell((short) 6).setCellValue("UNITS");
    row.createCell((short) 7).setCellValue("ASSAY_QUAL_RESULT");
    row.createCell((short) 8).setCellValue("ASSAY_DATE");
    row.createCell((short) 9).setCellValue("PROJECT_ID");
    row.createCell((short) 10).setCellValue("DEPARTMENT_ID");
    row.createCell((short) 11).setCellValue("CONCENTRATION");
    row.createCell((short) 12).setCellValue("CONC_UNITS");
    row.createCell((short) 13).setCellValue("ADMIN");
    row.createCell((short) 14).setCellValue("INVESTIGATOR");
    row.createCell((short) 15).setCellValue("NOTEBOOK");
    row.createCell((short) 16).setCellValue("COMMENTS");
    row.createCell((short) 17).setCellValue("EXTRA");
   return sheet;
}
  
  private void parseScreenResults()
  {
    _dao.doInTransaction(new DAOTransaction () {
      public void runTransaction() {
        List<ScreenResult> screenResults = _dao.findAllEntitiesOfType(ScreenResult.class);
        for (ScreenResult screenResult : screenResults) {
          parseScreenResult(screenResult);
	// DEBUG ONLY!
/*
	  if (_currentBioactivitySheetIndex > 1) {
		break;
	  }
*/
        }
      }
    });
  }
  
  private void parseScreenResult(ScreenResult screenResult)
  {
    log.info("processing screen result for screen " + screenResult.getScreen().getScreenNumber());
    AssayInfo assayInfo = _assayInfoProducer.getAssayInfoForScreen(screenResult.getScreen());

    // TODO: printBioactivityRows should be called once for each assay phenotype
    if (printBioactivityRows(assayInfo, screenResult)) {
      log.info("printed bioactivity rows.");
      printProtocolRow(assayInfo);  
    }
  }
  
  /**
   * Print bioactivity rows for this screen result.
   * @param assayInfo
   * @param screenResult
   * @return true iff bioactivity rows were printed
   */
  private boolean printBioactivityRows(AssayInfo assayInfo, ScreenResult screenResult)
  {
    ResultValueType scaledOrBooleanRVT =
      findRightmostIndicatingScaledOrBooleanRVT(screenResult);
    if (scaledOrBooleanRVT == null) {
      log.info("no assay indicator for " + assayInfo.getAssayName());
      return false;
    }
    
    boolean printedBioactivityRow = false;
    
    List<ResultValueType> rvts = new ArrayList<ResultValueType>();
    if (scaledOrBooleanRVT != null) {
      rvts.add(scaledOrBooleanRVT);
    }

    _currentBioactivitySheet = createBioactivitySheet();
    for (Integer plateNumber : _mapper.getMappedPlates()) {
      Set<WellKey> mappedKeys = new HashSet<WellKey>();
      Map<WellKey,ResultValue> scaledOrBooleanRVMap = null;
      if (scaledOrBooleanRVT != null) {
        scaledOrBooleanRVMap =
          _screenResultsDAO.findResultValuesByPlate(plateNumber, scaledOrBooleanRVT);
        mappedKeys.addAll(scaledOrBooleanRVMap.keySet());
      }
      Map<WellKey,ResultValue> numericalRVMap = null;
      for (WellKey wellKey : mappedKeys) {
/*		// DEBUG ONLY!
		if (_currentBioactivityRow > 20) break;
*/
		
        ResultValue scaledOrBooleanRV = (scaledOrBooleanRVT == null) ? null :
          scaledOrBooleanRVMap.get(wellKey);
        if (printBioactivityRow(
          assayInfo,
          wellKey,
          scaledOrBooleanRVT,
          scaledOrBooleanRV)) {
          printedBioactivityRow = true;
        }
      }
    }
    return printedBioactivityRow;
  }
  
  private ResultValueType findRightmostIndicatingScaledOrBooleanRVT(
    ScreenResult screenResult)
  {
    ResultValueType rightmostScaledOrBoolean = null;
    for (ResultValueType rvt : screenResult.getResultValueTypes()) {
      if (rvt.isPositiveIndicator()) {
        rightmostScaledOrBoolean = rvt;
      }
    }
    return rightmostScaledOrBoolean;
  }

  private boolean printBioactivityRow(
    AssayInfo assayInfo,
    WellKey wellKey,
    ResultValueType scaledOrBooleanRVT,
    ResultValue scaledOrBooleanRV)
  {
    String lq = _mapper.getLQForWellKey(wellKey);
    if (lq == null) { return false; }
    String assayName = assayInfo.getAssayName();
    String plateName = "P" + wellKey.getPlateNumber();
    String wellName = wellKey.getWellName();
    
    HSSFSheet sheet = _currentBioactivitySheet;
    HSSFRow row = sheet.createRow(_currentBioactivityRow);

    row.createCell((short) 0).setCellValue(lq);
    row.createCell((short) 1).setCellValue(plateName);
    row.createCell((short) 2).setCellValue(wellName);
    row.createCell((short) 3).setCellValue(assayInfo.getAssayCategory());
    row.createCell((short) 4).setCellValue(assayName);
    // note: cells 5 and 6 used to be used for "numerical assay indicator", which no longer exists
    if (scaledOrBooleanRV != null) {
      if (scaledOrBooleanRVT.isBooleanPositiveIndicator()) {
        if (scaledOrBooleanRV.getValue().equals("true")) {
          row.createCell((short) 7).setCellValue("A");
        }
        else if (scaledOrBooleanRV.getValue().equals("false")) {
          row.createCell((short) 7).setCellValue("I");          
        }
      }
      else if (scaledOrBooleanRVT.isPartitionPositiveIndicator()) {
        String partitionValue = scaledOrBooleanRV.getValue();
        if (partitionValue == null) {
          log.info("no partition value for well key " + wellKey);
          partitionValue = "";
        }
        if (partitionValue.equals("2") || partitionValue.equals("3")) {
          row.createCell((short) 7).setCellValue("A");
        }
        else if (partitionValue.equals("1")) {
          row.createCell((short) 7).setCellValue("Q");
        }
        else {
          row.createCell((short) 7).setCellValue("I");
        }
      }
    }
    row.createCell((short) 8).setCellValue(assayInfo.getAssayDate());
    row.createCell((short) 9).setCellValue("ICBG-CLARDY");
    row.createCell((short) 10).setCellValue("ICCBL");
    //row.createCell((short) 11).setCellValue("CONCENTRATION");
    //row.createCell((short) 12).setCellValue("CONC_UNITS");
    row.createCell((short) 13).setCellValue(assayName + plateName + wellName);
    row.createCell((short) 14).setCellValue(assayInfo.getInvestigator());
    //row.createCell((short) 15).setCellValue("NOTEBOOK");
    //row.createCell((short) 16).setCellValue("COMMENTS");
    //row.createCell((short) 17).setCellValue("EXTRA");
   	if (_currentBioactivityRow++ % MAX_ROWS_PER_SHEET == 0) {
	  ++_currentBioactivitySheetIndex;
	  _currentBioactivitySheet = createBioactivitySheet();
	  _currentBioactivityRow = 1;
	}
	return true;
  }

  private void printProtocolRow(AssayInfo assayInfo)
  {
    HSSFSheet sheet = _report.getSheet("PROTOCOL");
    HSSFRow row = sheet.createRow(_currentProtocolRow);
    row.createCell((short) 0).setCellValue(assayInfo.getAssayName());
    row.createCell((short) 1).setCellValue("B");
    row.createCell((short) 2).setCellValue(assayInfo.getProtocolDescription());
    row.createCell((short) 3).setCellValue(assayInfo.getPNote());
    _currentProtocolRow++;
  }
}
