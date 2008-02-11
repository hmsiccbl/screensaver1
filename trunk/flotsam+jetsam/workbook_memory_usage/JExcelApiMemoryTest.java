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

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import org.apache.log4j.Logger;

public class JExcelApiMemoryTest
{
  // static members

  private static Logger log = Logger.getLogger(JExcelApiMemoryTest.class);

  public static void main(String[] args) throws Exception
  {
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(args[0])));
    WorkbookSettings WORKBOOK_SETTINGS = new WorkbookSettings();
    Workbook workbook = Workbook.getWorkbook(bis, WORKBOOK_SETTINGS);
    int sheets = workbook.getNumberOfSheets();
    for (int i = 2; i < sheets; ++i) {
      Sheet sheet = workbook.getSheet(i);
      log.info("sheet " + sheet.getName() + " has " + sheet.getColumns() + " columns and " + sheet.getRows() + " rows");
      for (int iRow = 0; iRow < sheet.getRows(); ++iRow) {
        Cell[] row = sheet.getRow(iRow);
        for (int iCell = 0; iCell < row.length; iCell++) {
          Cell cell = row[iCell];
          cell.getContents();
        }
      }
    }
    Runtime.getRuntime().gc();
  }

  // instance data members

  // public constructors and methods

  // private methods

}

