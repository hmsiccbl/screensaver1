// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.reports.icbg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;


/**
 * Generates the ICCB-L portion of the ICBG Napis report.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ICBGReportGenerator extends CommandLineApplication
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
   * @throws IOException
   * @throws WriteException
   * @throws FileNotFoundException
   * @throws RowsExceededException
   * @throws BiffException
   */
  public static void main(String[] args) throws RowsExceededException, FileNotFoundException, WriteException, IOException, BiffException
  {

   final ICBGReportGenerator generator = new ICBGReportGenerator( args);
   generator.execute();
  }
  
  private void execute()
  {
    _dao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        try {
          WritableWorkbook report = produceReport();
          log.info("writing report..");
          report.write();
          report.close();
          log.info("report written." );
        }
        catch (Exception e) {
          e.printStackTrace();
          log.error(e.getMessage());
        }
      }
    });
  }
  
  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDAO;
  private ICCBLPlateWellToINBioLQMapper _mapper;
  private AssayInfoProducer _assayInfoProducer;
  private WritableWorkbook _report;
  private int _currentProtocolRow = 1;
  private int _cbsIndex = 1;
  private WritableSheet _protocolSheet;
  
  public ICBGReportGenerator( String[] args) throws BiffException, IOException
  {
     super(args);

    processOptions(true,false);
    
    _dao = (GenericEntityDAO) getSpringBean("genericEntityDao");
    _screenResultsDAO = (ScreenResultsDAO) getSpringBean("screenResultsDao");
    _mapper = new ICCBLPlateWellToINBioLQMapper();
    _assayInfoProducer = new AssayInfoProducer();
  }
  
  public WritableWorkbook produceReport() throws RowsExceededException, FileNotFoundException, WriteException, IOException
  {
    initializeReport();
    parseScreenResults();
    return _report;
  }
  
  private void initializeReport() throws FileNotFoundException, IOException, RowsExceededException, WriteException
  {
    _report = jxl.Workbook.createWorkbook(new FileOutputStream(REPORT_FILENAME));
    _protocolSheet = _report.createSheet("PROTOCOL", 0);
    _protocolSheet.addCell(new Label(0, 0, "PROTOCOL_ID"));
    _protocolSheet.addCell(new Label(1, 0, "PROTOCOL_TYPE"));
    _protocolSheet.addCell(new Label(2, 0, "PROTOCOL_DESCR"));
    _protocolSheet.addCell(new Label(3, 0, "P_NOTE"));
    WritableSheet compoundSheet = _report.createSheet("COMPOUND", 1);
    compoundSheet.addCell(new Label(0, 0, "COMPOUND_ID"));
    compoundSheet.addCell(new Label(1, 0, "MATERIAL_ID"));
    compoundSheet.addCell(new Label(2, 0, "CL_EXTRA_1"));
    compoundSheet.addCell(new Label(3, 0, "MOLSTRUCTURE_FILE"));
  }
  
  private WritableSheet createBioactivitySheet() throws RowsExceededException, WriteException
  {
          ++_cbsIndex;

    WritableSheet sheet = _report.createSheet(
                                              "BIOACTIVITY" + _cbsIndex, 
                                              _cbsIndex + 2);
    sheet.addCell(new Label(0, 0, "MATERIAL_ID"));
    sheet.addCell(new Label(1, 0, "PLATE_ID"));
    sheet.addCell(new Label(2, 0, "WELL_ID"));
    sheet.addCell(new Label(3, 0, "ASSAY_CATEGORY"));
    sheet.addCell(new Label(4, 0, "ASSAY_NAME"));
    sheet.addCell(new Label(5, 0, "ACTIVITY"));
    sheet.addCell(new Label(6, 0, "UNITS"));
    sheet.addCell(new Label(7, 0, "ASSAY_QUAL_RESULT"));
    sheet.addCell(new Label(8, 0, "ASSAY_DATE"));
    sheet.addCell(new Label(9, 0, "PROJECT_ID"));
    sheet.addCell(new Label(10, 0, "DEPARTMENT_ID"));
    sheet.addCell(new Label(11, 0, "CONCENTRATION"));
    sheet.addCell(new Label(12, 0, "CONC_UNITS"));
    sheet.addCell(new Label(13, 0, "ADMIN"));
    sheet.addCell(new Label(14, 0, "INVESTIGATOR"));
    sheet.addCell(new Label(15, 0, "NOTEBOOK"));
    sheet.addCell(new Label(16, 0, "COMMENTS"));
    sheet.addCell(new Label(17, 0, "EXTRA"));
   return sheet;
}
  
  private void parseScreenResults() throws RowsExceededException, WriteException
  {
    List<ScreenResult> screenResults = _dao.findAllEntitiesOfType(ScreenResult.class);
    int row = 1;
    WritableSheet sheet = createBioactivitySheet();
    for (ScreenResult screenResult : screenResults) {
      log.info("processing screen result for screen " + screenResult.getScreen().getFacilityId());
      AssayInfo assayInfo = _assayInfoProducer.getAssayInfoForScreen(screenResult.getScreen());

      // TODO: printBioactivityRows should be called once for each assay phenotype
      DataColumn scaledOrBooleanDataColumn =
        findRightmostIndicatingScaledOrBooleanDataColumn(screenResult);

      if (scaledOrBooleanDataColumn == null) {
        log.info("no assay indicator for " + assayInfo.getAssayName());
      }
      else {
        boolean printedBioactivityRow = false;

        for (Integer plateNumber : _mapper.getMappedPlates()) {
          for(Map.Entry<WellKey,ResultValue> entry :
            _screenResultsDAO.findResultValuesByPlate(plateNumber, scaledOrBooleanDataColumn).entrySet())
          {
            WellKey wellKey = entry.getKey();
            ResultValue scaledOrBooleanRV = entry.getValue();
            String lq = _mapper.getLQForWellKey(wellKey);
            if (lq != null) {
              printedBioactivityRow = true;
              String assayName = assayInfo.getAssayName();
              String plateName = "P" + wellKey.getPlateNumber();
              String wellName = wellKey.getWellName();

              sheet.addCell(new Label(0, row, lq));
              sheet.addCell(new Label(1, row, plateName));
              sheet.addCell(new Label(2, row, wellName));
              sheet.addCell(new Label(3, row, assayInfo.getAssayCategory()));
              sheet.addCell(new Label(4, row, assayName));
              // note: cells 5 and 6 used to be used for "numerical assay indicator", which no longer exists
              if (scaledOrBooleanRV != null) {
                if (scaledOrBooleanDataColumn.isBooleanPositiveIndicator()) {
                  if (scaledOrBooleanRV.getValue().equals("true")) {
                    sheet.addCell(new Label(7, row, "A"));
                  }
                  else if (scaledOrBooleanRV.getValue().equals("false")) {
                    sheet.addCell(new Label(7, row, "I"));
                  }
                }
                else if (scaledOrBooleanDataColumn.isPartitionPositiveIndicator()) {
                  String partitionValue = scaledOrBooleanRV.getValue();
                  if (partitionValue == null) {
                    log.info("no partition value for well key " + wellKey);
                    partitionValue = "";
                  }
                  if (partitionValue.equals("2") || partitionValue.equals("3")) {
                    sheet.addCell(new Label(7, row, "A"));
                  }
                  else if (partitionValue.equals("1")) {
                    sheet.addCell(new Label(7, row, "Q"));
                  }
                  else {
                    sheet.addCell(new Label(7, row, "I"));
                  }
                }
              }
              sheet.addCell(new Label(8, row, assayInfo.getAssayDate()));
              sheet.addCell(new Label(13, row, assayName + plateName + wellName));
              sheet.addCell(new Label(14, row, assayInfo.getInvestigator()));

              if (row++ % MAX_ROWS_PER_SHEET == 0) {
                sheet = createBioactivitySheet();
                row = 1;
              }
            } // well is mapped to inBioLQ
          } // result values
        } // mapped plates
        if (printedBioactivityRow) {
          printProtocolRow(assayInfo);
        }
      } // assay indicator found
    } // screenresults
  }

  private DataColumn findRightmostIndicatingScaledOrBooleanDataColumn(
    ScreenResult screenResult)
  {
    DataColumn rightmostScaledOrBoolean = null;
    for (DataColumn col : screenResult.getDataColumns()) {
      if (col.isPositiveIndicator()) {
        rightmostScaledOrBoolean = col;
      }
    }
    return rightmostScaledOrBoolean;
  }

  private void printProtocolRow(AssayInfo assayInfo) throws RowsExceededException, WriteException
  {
    _protocolSheet.addCell(new Label(0, _currentProtocolRow, assayInfo.getAssayName()));
    _protocolSheet.addCell(new Label(1, _currentProtocolRow, "B"));
    _protocolSheet.addCell(new Label(2, _currentProtocolRow, assayInfo.getProtocolDescription()));
    _protocolSheet.addCell(new Label(3, _currentProtocolRow, assayInfo.getPNote()));
    _currentProtocolRow++;
  }
  
//  private void parseScreenResults() throws RowsExceededException, WriteException
//  {
//    List<ScreenResult> screenResults = _dao.findAllEntitiesOfType(ScreenResult.class);
//    for (ScreenResult screenResult : screenResults) {
//      parseScreenResult(screenResult);
//  // DEBUG ONLY!
///*
//    if (_currentBioactivitySheetIndex > 1) {
//    break;
//    }
//*/
//    }
//  }
//  
//  private void parseScreenResult(ScreenResult screenResult) throws RowsExceededException, WriteException
//  {
//    log.info("processing screen result for screen " + screenResult.getScreen().getFacilityId());
//    AssayInfo assayInfo = _assayInfoProducer.getAssayInfoForScreen(screenResult.getScreen());
//
//    // TODO: printBioactivityRows should be called once for each assay phenotype
//    if (printBioactivityRows(assayInfo, screenResult)) {
//      log.info("printed bioactivity rows.");
//      printProtocolRow(assayInfo);  
//    }
//  }
//
//  /**
//   * Print bioactivity rows for this screen result.
//   * 
//   * @param assayInfo
//   * @param screenResult
//   * @return true iff bioactivity rows were printed
//   * @throws WriteException
//   * @throws RowsExceededException
//   */
//  private boolean printBioactivityRows(AssayInfo assayInfo, ScreenResult screenResult) throws RowsExceededException, WriteException
//  {
//    DataColumn scaledOrBooleanDataColumn =
//      findRightmostIndicatingScaledOrBooleanDataColumn(screenResult);
//    if (scaledOrBooleanDataColumn == null) {
//      log.info("no assay indicator for " + assayInfo.getAssayName());
//      return false;
//    }
//    
//    boolean printedBioactivityRow = false;
//    
//    List<DataColumn> cols = new ArrayList<DataColumn>();
//    if (scaledOrBooleanDataColumn != null) {
//      cols.add(scaledOrBooleanDataColumn);
//    }
//
//    _currentBioactivitySheet = createBioactivitySheet();
//    for (Integer plateNumber : _mapper.getMappedPlates()) {
//      Set<WellKey> mappedKeys = new HashSet<WellKey>();
//      Map<WellKey,ResultValue> scaledOrBooleanRVMap = null;
//      if (scaledOrBooleanDataColumn != null) {
//        scaledOrBooleanRVMap =
//          _screenResultsDAO.findResultValuesByPlate(plateNumber, scaledOrBooleanDataColumn);
//        mappedKeys.addAll(scaledOrBooleanRVMap.keySet());
//      }
//      Map<WellKey,ResultValue> numericalRVMap = null;
//      for (WellKey wellKey : mappedKeys) {
///*		// DEBUG ONLY!
//		if (_currentBioactivityRow > 20) break;
//*/
//		
//        ResultValue scaledOrBooleanRV = (scaledOrBooleanDataColumn == null) ? null :
//          scaledOrBooleanRVMap.get(wellKey);
//        if (printBioactivityRow(
//          assayInfo,
//          wellKey,
//          scaledOrBooleanDataColumn,
//          scaledOrBooleanRV)) {
//          printedBioactivityRow = true;
//        }
//      }
//    }
//    if(!printedBioactivityRow) 
//      _removeSheets.add(_currentBioactivitySheetIndex + 2);
//    else
//      log.info("---- wrote sheet: " +(_currentBioactivitySheetIndex +2));
//    return printedBioactivityRow;
//  }
//  
//
//  private boolean printBioactivityRow(
//                                      AssayInfo assayInfo,
//                                      WellKey wellKey,
//                                      DataColumn scaledOrBooleanDataColumn,
//                                      ResultValue scaledOrBooleanRV) throws RowsExceededException, WriteException
//  {
//    String lq = _mapper.getLQForWellKey(wellKey);
//    if (lq == null) {
//      return false;
//    }
//    String assayName = assayInfo.getAssayName();
//    String plateName = "P" + wellKey.getPlateNumber();
//    String wellName = wellKey.getWellName();
//
//    WritableSheet sheet = _currentBioactivitySheet;
//
//    //sheet.addCell(new Label(0, _currentBioactivityRow, lq));
//    sheet.addCell(new Label(0, _currentBioactivityRow, "row is: " + _currentBioactivityRow));
//    
//    sheet.addCell(new Label(1, _currentBioactivityRow, plateName));
//    sheet.addCell(new Label(2, _currentBioactivityRow, wellName));
//    sheet.addCell(new Label(3, _currentBioactivityRow, assayInfo.getAssayCategory()));
//    sheet.addCell(new Label(4, _currentBioactivityRow, assayName));
//    // note: cells 5 and 6 used to be used for "numerical assay indicator", which no longer exists
//    if (scaledOrBooleanRV != null) {
//      if (scaledOrBooleanDataColumn.isBooleanPositiveIndicator()) {
//        if (scaledOrBooleanRV.getValue().equals("true")) {
//          sheet.addCell(new Label(7, _currentBioactivityRow, "A"));
//        }
//        else if (scaledOrBooleanRV.getValue().equals("false")) {
//          sheet.addCell(new Label(7, _currentBioactivityRow, "I"));
//        }
//      }
//      else if (scaledOrBooleanDataColumn.isPartitionPositiveIndicator()) {
//        String partitionValue = scaledOrBooleanRV.getValue();
//        if (partitionValue == null) {
//          log.info("no partition value for well key " + wellKey);
//          partitionValue = "";
//        }
//        if (partitionValue.equals("2") || partitionValue.equals("3")) {
//          sheet.addCell(new Label(7, _currentBioactivityRow, "A"));
//        }
//        else if (partitionValue.equals("1")) {
//          sheet.addCell(new Label(7, _currentBioactivityRow, "Q"));
//        }
//        else {
//          sheet.addCell(new Label(7, _currentBioactivityRow, "I"));
//        }
//      }
//    }
//    sheet.addCell(new Label(8, _currentBioactivityRow, assayInfo.getAssayDate()));
////    sheet.addCell(new Label(9, 0, "ICBG-CLARDY"));
////    sheet.addCell(new Label(10, 0, "ICCBL"));
//    //sheet.addCell(new Label(11, 0, "CONCENTRATION"));
//    //sheet.addCell(new Label(12, 0, "CONC_UNITS"));
//    sheet.addCell(new Label(13, _currentBioactivityRow, assayName + plateName + wellName));
//    sheet.addCell(new Label(14, _currentBioactivityRow, assayInfo.getInvestigator()));
//    //sheet.addCell(new Label(15, 0, "NOTEBOOK"));
//    //sheet.addCell(new Label(16, 0, "COMMENTS"));
//    //sheet.addCell(new Label(17, 0, "EXTRA"));
//    
//    
//    if (_currentBioactivityRow++ % MAX_ROWS_PER_SHEET == 0) {
//      _currentBioactivitySheet = createBioactivitySheet();
//      _currentBioactivityRow = 1;
//    }
//    return true;
//  }

}
