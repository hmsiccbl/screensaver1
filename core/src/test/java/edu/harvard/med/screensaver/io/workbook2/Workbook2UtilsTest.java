// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.3.1-dev/test/edu/harvard/med/screensaver/io/workbook2/Workbook2UtilsTest.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.workbook2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Workbook2UtilsTest extends TestCase
{
  private static final Logger log = Logger.getLogger(Workbook2UtilsTest.class);

  /** Requires manual inspection to verify success */
  public void testImageExport() throws Exception
  {
    try {
			File file = File.createTempFile("testImageExport", ".xls");
			OutputStream out = new FileOutputStream(file);
			WritableWorkbook workbook = Workbook.createWorkbook(out);
			WritableSheet sheet = workbook.createSheet("sheet1", 0);
			InputStream imageIn = Workbook2UtilsTest.class.getResourceAsStream("arrow-first.png");
			byte[] imageData = IOUtils.toByteArray(imageIn);
			Workbook2Utils.writeCell(sheet, 1, 0, "image:");
			Workbook2Utils.writeImage(sheet, 1, 1, imageData);
			workbook.write();
			workbook.close();
			log.warn("must manually verify that image was exported to workbook " + file);
		} catch (Exception e) {
			// prefer not to maintain this type of test, so allow the error to go to console -sde4
			e.printStackTrace();
		}
  }
}

