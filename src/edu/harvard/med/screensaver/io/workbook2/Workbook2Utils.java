// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import jxl.Workbook;
import jxl.biff.EmptyCell;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

public class Workbook2Utils
{
  // static members

  private static Logger log = Logger.getLogger(Workbook2Utils.class);


  // instance data members

  // public constructors and methods

  public static void writeRow(WritableSheet sheet, int iRow, Object... fieldValues) throws RowsExceededException, WriteException
  {
    int iCol = 0;
    for (Object object : fieldValues) {
      writeCell(sheet, iRow, iCol++, object);
    }
  }

  public static void writeCell(WritableSheet sheet, int iRow, int iCol, Object fieldValue) throws WriteException, RowsExceededException
  {
    WritableCell cell = null;
    if (fieldValue == null) {
      cell = new EmptyCell(iCol, iRow);
    }
    else if (fieldValue instanceof Integer) {
      cell = new Number(iCol, iRow, ((java.lang.Number) fieldValue).intValue());
    }
    else if (fieldValue instanceof java.lang.Number) {
      cell = new Number(iCol, iRow, ((java.lang.Number) fieldValue).doubleValue());
    }
    else if (fieldValue instanceof Collection) {
      cell = new Label(iCol, iRow, StringUtils.makeListString((Collection<?>) fieldValue, "; "));
    }
    else {
      cell = new Label(iCol, iRow, fieldValue.toString());
    }
    sheet.addCell(cell);
  }

  public static InputStream toInputStream(Workbook workbook) throws IOException, WriteException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    WritableWorkbook workbook2 = jxl.Workbook.createWorkbook(out, workbook);
    workbook2.write();
    workbook2.close();
    return new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
  }


  // private methods

}

