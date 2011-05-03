// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.io.StringWriter;

import junit.framework.TestCase;

public class CSVPrinterWriterTest extends TestCase
{
  public void testEscaping()
  {
    StringWriter w = new StringWriter();
    CSVPrintWriter pw = new CSVPrintWriter(w, "\n", ",");
    pw.print(true);
    pw.print("hello");
    pw.print("x,y,z");
    pw.print("x\"yz\"");
    pw.print("xy\nz");
    pw.println();
    pw.close();
    assertEquals("true,hello,\"x,y,z\",\"x\"\"yz\"\"\",\"xy\nz\"\n", w.toString());
  }
}
