// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class POIMemoryTest
{
  // static members

  private static Logger log = Logger.getLogger(POIMemoryTest.class);


  public static void main(String[] args) throws Exception
  {
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(args[0])));
    POIFSFileSystem dataFs = new POIFSFileSystem(bis);
    HSSFWorkbook workbook = new HSSFWorkbook(dataFs, false);
    int sheets = workbook.getNumberOfSheets();
    for (int i = 2; i < sheets; ++i) {
      HSSFSheet sheet = workbook.getSheetAt(i);
      log.info("sheet " + workbook.getSheetName(i) + " has " + sheet.getLastRowNum() + " rows");
      for (int iRow = sheet.getFirstRowNum(); iRow < sheet.getLastRowNum(); ++iRow) {
        HSSFRow row = sheet.getRow(iRow);
        Iterator<?> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
          HSSFCell cell = (HSSFCell) cellIterator.next();
          switch (cell.getCellType()) {
          case HSSFCell.CELL_TYPE_BOOLEAN: cell.getBooleanCellValue(); break;
          case HSSFCell.CELL_TYPE_NUMERIC: cell.getNumericCellValue(); break;
          default: cell.getStringCellValue(); break;
          }
        }
      }
      sheet.releaseData();
      Runtime.getRuntime().gc();
    }
  }

  // instance data members

  // public constructors and methods

  // private methods

}

