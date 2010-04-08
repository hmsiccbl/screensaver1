// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.io.IOUtils;

public class Workbook2UtilsTest extends TestCase
{
  /** Requires manual inspection to verify success */
  public void testImageExport() throws Exception
  {
    OutputStream out = new FileOutputStream("image-export-text.xls") ;
    WritableWorkbook workbook = Workbook.createWorkbook(out);
    WritableSheet sheet = workbook.createSheet("sheet1", 0);
    InputStream imageIn = getClass().getResourceAsStream("/images/arrow-first.png");
    byte[] imageData = IOUtils.toByteArray(imageIn);
    Workbook2Utils.writeCell(sheet, 1, 0, "image:");
    Workbook2Utils.writeImage(sheet, 1, 1, imageData);
    workbook.write();
    workbook.close();
  }
}
