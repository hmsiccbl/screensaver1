// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.reports.icbg;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
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
    ICCBLPlateWellToINBioLQMapper mapper =
      new ICCBLPlateWellToINBioLQMapper();
    ICBGReportGenerator generator = new ICBGReportGenerator(
      dao,
      mapper,
      new ScreenDBProxy());
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
  private ICCBLPlateWellToINBioLQMapper _mapper;
  private ScreenDBProxy _screenDBProxy;
  private HSSFWorkbook _report;
  private int _currentBioactivityRow = 1;
  private int _currentProtocolRow = 1;
  
  
  // public constructor and instance methods
  
  public ICBGReportGenerator(
    GenericEntityDAO dao,
    ICCBLPlateWellToINBioLQMapper mapper,
    ScreenDBProxy screenDBProxy)
  {
    _dao = dao;
    _mapper = mapper;
    _screenDBProxy = screenDBProxy;
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
//    sheet = _report.createSheet("SITE");
//    row = sheet.createRow(0);
//    row.createCell((short) 0).setCellValue("SITE_ID");
//    row.createCell((short) 1).setCellValue("COLLECT_DATE");
//    row.createCell((short) 2).setCellValue("LATITUDE");
//    row.createCell((short) 3).setCellValue("LONGITUDE");
//    row.createCell((short) 4).setCellValue("COLLECTOR_ID");
//    row.createCell((short) 5).setCellValue("REGION");
//    row.createCell((short) 6).setCellValue("COUNTRY");
//    row.createCell((short) 7).setCellValue("ENTITY");
//    row.createCell((short) 8).setCellValue("LOCALE");
//    row.createCell((short) 9).setCellValue("ADMINISTRATION");
//    row.createCell((short) 10).setCellValue("BIOME");
//    row.createCell((short) 11).setCellValue("ENVIRONMENT");
//    row.createCell((short) 12).setCellValue("ZONE");
//    row.createCell((short) 13).setCellValue("SUBSTRATE");
//    row.createCell((short) 14).setCellValue("EXPOSURE");
//    row.createCell((short) 15).setCellValue("VERTICAL_M");
//    row.createCell((short) 16).setCellValue("TEMP_C");
//    row.createCell((short) 17).setCellValue("SEASON");
//    row.createCell((short) 18).setCellValue("ASPECT");
//    row.createCell((short) 19).setCellValue("NOTE");
//    sheet = _report.createSheet("SOURCE");
//    row = sheet.createRow(0);
//    row.createCell((short) 0).setCellValue("MICRO_SOURCE_ID");
//    row.createCell((short) 1).setCellValue("SITE_ID");
//    row.createCell((short) 2).setCellValue("MICRO_SOURCE_TYPE");
//    row.createCell((short) 3).setCellValue("MICRO_METHOD");
//    row.createCell((short) 4).setCellValue("MICRO_FOME");
//    row.createCell((short) 5).setCellValue("MICRO_QUALITY");
//    row.createCell((short) 6).setCellValue("M_GENNOTES");
//    row.createCell((short) 7).setCellValue("KINGDOM");
//    row.createCell((short) 8).setCellValue("PHYLUM");
//    row.createCell((short) 9).setCellValue("CLASS");
//    row.createCell((short) 10).setCellValue("ORDER_T");
//    row.createCell((short) 11).setCellValue("FAMILY");
//    row.createCell((short) 12).setCellValue("GENUS");
//    row.createCell((short) 13).setCellValue("SPECIES");
//    row.createCell((short) 14).setCellValue("PART_DESCR");
//    sheet = _report.createSheet("COLLECTION");
//    row = sheet.createRow(0);
//    row.createCell((short) 0).setCellValue("COLLECT_ID");
//    row.createCell((short) 1).setCellValue("SITE_ID");
//    row.createCell((short) 2).setCellValue("MICRO_SOURCE_ID");
//    row.createCell((short) 3).setCellValue("KINGDOM");
//    row.createCell((short) 4).setCellValue("PHYLUM");
//    row.createCell((short) 5).setCellValue("CLASS");
//    row.createCell((short) 6).setCellValue("ORDER");
//    row.createCell((short) 7).setCellValue("FAMILY");
//    row.createCell((short) 8).setCellValue("GENUS");
//    row.createCell((short) 9).setCellValue("SPECIES");
//    row.createCell((short) 10).setCellValue("VARIANT");
//    row.createCell((short) 11).setCellValue("AUTHORITY");
//    row.createCell((short) 12).setCellValue("VAUTHORITY");
//    row.createCell((short) 13).setCellValue("COMMON_NAME");
//    row.createCell((short) 14).setCellValue("DET");
//    row.createCell((short) 15).setCellValue("DET_DATE");
//    row.createCell((short) 16).setCellValue("MORPH_DESCR");
//    row.createCell((short) 17).setCellValue("ABUND_DESCR");
//    row.createCell((short) 18).setCellValue("ODOR_DESCR");
//    row.createCell((short) 19).setCellValue("COLOR");
//    row.createCell((short) 20).setCellValue("TYPE_NO (ACCESSION)");
//    row.createCell((short) 21).setCellValue("NO_VOUCHERS_TAKEN");
//    row.createCell((short) 22).setCellValue("RATIONALE");
//    row.createCell((short) 23).setCellValue("TAXONOMY");
//    row.createCell((short) 24).setCellValue("NOTE");
//    sheet = _report.createSheet("SAMPLE");
//    row = sheet.createRow(0);
//    row.createCell((short) 0).setCellValue("SAMPLE_ID");
//    row.createCell((short) 1).setCellValue("COLLECT_ID");
//    row.createCell((short) 2).setCellValue("SAMPLE_TYPE");
//    row.createCell((short) 3).setCellValue("MEDIA_ID");
//    row.createCell((short) 4).setCellValue("PRTOCOL_ID");
//    row.createCell((short) 5).setCellValue("HARVEST_DATE");
//    row.createCell((short) 6).setCellValue("PERSON_ID");
//    row.createCell((short) 7).setCellValue("COMMENTS");
//    sheet = _report.createSheet("EXTRACT");
//    row = sheet.createRow(0);
//    row.createCell((short) 0).setCellValue("MATERIAL_ID");
//    row.createCell((short) 1).setCellValue("MATERIAL_TYPE");
//    row.createCell((short) 2).setCellValue("SAMPLE_ID");
//    row.createCell((short) 3).setCellValue("PARENT_MATERIAL_ID_01");
//    row.createCell((short) 4).setCellValue("WT_G");
//    row.createCell((short) 5).setCellValue("PROTOCOL_ID");
//    row.createCell((short) 6).setCellValue("PROJECT_ID");
//    row.createCell((short) 7).setCellValue("NOTEBOOK");
//    row.createCell((short) 8).setCellValue("NOTE");
    sheet = _report.createSheet("BIOACTIVITY");
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
//    sheet = _report.createSheet("COMPOUND_PLUS");
//    row = sheet.createRow(0);
//    row.createCell((short) 0).setCellValue("COMPOUND_ID");
//    row.createCell((short) 1).setCellValue("MATERIAL_ID");
//    row.createCell((short) 2).setCellValue("MOLSTRUCTURE_FILE");
//    row.createCell((short) 3).setCellValue("SHORT_NAME");
//    row.createCell((short) 4).setCellValue("CHEMICAL_NAME");
//    row.createCell((short) 5).setCellValue("CHEMICAL_CLASS");
//    row.createCell((short) 6).setCellValue("CHEMICAL_FAMILY");
//    row.createCell((short) 7).setCellValue("UV_MAX");
//    row.createCell((short) 8).setCellValue("PH");
//    row.createCell((short) 9).setCellValue("PHYSICAL_APPEARANCE");
//    row.createCell((short) 10).setCellValue("MELTING_POINT");
//    row.createCell((short) 11).setCellValue("SOLUBILILTY");
//    row.createCell((short) 12).setCellValue("FORMULA");
//    row.createCell((short) 13).setCellValue("INVESTIGATOR");
//    row.createCell((short) 14).setCellValue("CL_EXTRA_1");
//    row.createCell((short) 15).setCellValue("PROJECT_ID");
//    row.createCell((short) 16).setCellValue("NOTE");
  }
  
  private void parseScreenResults()
  {
    _dao.doInTransaction(new DAOTransaction () {
      public void runTransaction() {
        List<ScreenResult> screenResults = _dao.findAllEntitiesOfType(ScreenResult.class);
        for (ScreenResult screenResult : screenResults) {
          parseScreenResult(screenResult);

          // this is not necessary, but helpful if you want early versions of the results
          log.info("writing report..");
          try {
            _report.write(new FileOutputStream(REPORT_FILENAME));
            log.info("report written.");
          }
          catch (Exception e) {
            e.printStackTrace();
            log.error("writing report generated error: " + e.getMessage());
          }
        }
      }
    });
  }
  
  private void parseScreenResult(ScreenResult screenResult)
  {
    Integer screenNumber = screenResult.getScreen().getScreenNumber();
    log.info("processing screen result for screen " + screenNumber); 
    AssayInfo assayInfo = _screenDBProxy.getAssayInfoForScreen(screenNumber);

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
    ResultValueType numericalRVT =
      findRightmostIndicatingNumericalRVT(screenResult);
    if (scaledOrBooleanRVT == null && numericalRVT == null) {
      log.info("no assay indicator for " + assayInfo.getAssayName());
      return false;
    }
    
    boolean printedBioactivityRow = false;
    Map<WellKey,ResultValue> scaledOrBooleanRVMap = (scaledOrBooleanRVT != null) ?
      scaledOrBooleanRVT.getResultValues() : null;
    Map<WellKey,ResultValue> numericalRVMap = (numericalRVT != null) ?
      numericalRVT.getResultValues() : null;

    for (Well well : screenResult.getWells()) {
      WellKey wellKey = well.getWellKey();
      ResultValue scaledOrBooleanRV = (scaledOrBooleanRVMap == null) ? null : 
        scaledOrBooleanRVMap.get(wellKey);
      ResultValue numericalRV = (numericalRVMap == null) ? null : 
        numericalRVMap.get(wellKey);
      if (printBioactivityRow(
            assayInfo,
            wellKey,
            scaledOrBooleanRVT,
            scaledOrBooleanRV,
            numericalRVT,
            numericalRV)) {
        printedBioactivityRow = true;
      }
    }
    return printedBioactivityRow;
  }
  
  private ResultValueType findRightmostIndicatingScaledOrBooleanRVT(
    ScreenResult screenResult)
  {
    ResultValueType rightmostScaledOrBoolean = null;
    SortedSet<ResultValueType> resultValueTypes =
      screenResult.getResultValueTypes();
    for (ResultValueType rvt : resultValueTypes) {
      if (! rvt.isActivityIndicator()) {
        continue;
      }
      ActivityIndicatorType indicatorType = rvt.getActivityIndicatorType();
      if (indicatorType.equals(ActivityIndicatorType.BOOLEAN) ||
          indicatorType.equals(ActivityIndicatorType.PARTITION)) {
        rightmostScaledOrBoolean = rvt;
      }
    }
    return rightmostScaledOrBoolean;
  }

  private ResultValueType findRightmostIndicatingNumericalRVT(
    ScreenResult screenResult)
  {
    ResultValueType rightmostNumerical = null;
    SortedSet<ResultValueType> resultValueTypes =
      screenResult.getResultValueTypes();
    for (ResultValueType rvt : resultValueTypes) {
      if (! rvt.isActivityIndicator()) {
        continue;
      }
      ActivityIndicatorType indicatorType = rvt.getActivityIndicatorType();
      if (indicatorType.equals(ActivityIndicatorType.NUMERICAL)) {
        rightmostNumerical = rvt;
      }
    }
    return rightmostNumerical;
  }
  
  private boolean printBioactivityRow(
    AssayInfo assayInfo,
    WellKey wellKey,
    ResultValueType scaledOrBooleanRVT,
    ResultValue scaledOrBooleanRV,
    ResultValueType numericalRVT,
    ResultValue numericalRV)
  {
    String lq = _mapper.getLQForWellKey(wellKey);
    if (lq == null) { return false; }
    String assayName = assayInfo.getAssayName();
    String plateName = "P" + wellKey.getPlateNumber();
    String wellName = wellKey.getWellName();
    
    HSSFSheet sheet = _report.getSheet("BIOACTIVITY");
    HSSFRow row = sheet.createRow(_currentBioactivityRow);

    row.createCell((short) 0).setCellValue(lq);
    row.createCell((short) 1).setCellValue(plateName);
    row.createCell((short) 2).setCellValue(wellName);
    row.createCell((short) 3).setCellValue(assayInfo.getAssayCategory());
    row.createCell((short) 4).setCellValue(assayName);
    if (numericalRV != null && numericalRV.getNumericValue() != null) {
      row.createCell((short) 5).setCellValue(numericalRV.getNumericValue());
      row.createCell((short) 6).setCellValue(numericalRVT.getDescription());
    }
    if (scaledOrBooleanRV != null) {
      if (scaledOrBooleanRVT.getActivityIndicatorType().equals(ActivityIndicatorType.BOOLEAN)) {
        if (scaledOrBooleanRV.getValue().equals("true")) {
          row.createCell((short) 7).setCellValue("A");
        }
        else if (scaledOrBooleanRV.getValue().equals("true")) {
          row.createCell((short) 7).setCellValue("I");          
        }
      }
      else if (scaledOrBooleanRVT.getActivityIndicatorType().equals(ActivityIndicatorType.PARTITION)) {
        if (scaledOrBooleanRV.getValue().toUpperCase().equals("S") ||
            scaledOrBooleanRV.getValue().toUpperCase().equals("M")) {
          row.createCell((short) 7).setCellValue("A");
        }
        else if (scaledOrBooleanRV.getValue().toUpperCase().equals("W")) {
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
    _currentBioactivityRow++;
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
