// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import junit.framework.TestCase;

public class WellNameTest extends TestCase
{
  public void testWellName()
  {
    WellName wellName = new WellName("A01");
    assertEquals(0, wellName.getRowIndex());
    assertEquals(0, wellName.getColumnIndex());
    assertEquals("A", wellName.getRowLabel());
    assertEquals("01", wellName.getColumnLabel());
    assertEquals(new WellName(0, 0), wellName);

    wellName = new WellName("A1");
    assertEquals(0, wellName.getRowIndex());
    assertEquals(0, wellName.getColumnIndex());
    assertEquals("A", wellName.getRowLabel());
    assertEquals("01", wellName.getColumnLabel());
    assertEquals(new WellName(0, 0), wellName);
    
    wellName = new WellName("a1");
    assertEquals(0, wellName.getRowIndex());
    assertEquals(0, wellName.getColumnIndex());
    assertEquals("A", wellName.getRowLabel());
    assertEquals("01", wellName.getColumnLabel());
    assertEquals(new WellName(0, 0), wellName);

    wellName = new WellName("P24");
    assertEquals(15, wellName.getRowIndex());
    assertEquals(23, wellName.getColumnIndex());
    assertEquals("P", wellName.getRowLabel());
    assertEquals("24", wellName.getColumnLabel());
    assertEquals(new WellName(15, 23), wellName);
    
    wellName = new WellName("AA1");
    assertEquals(26, wellName.getRowIndex());
    assertEquals(0, wellName.getColumnIndex());
    assertEquals("AA", wellName.getRowLabel());
    assertEquals("01", wellName.getColumnLabel());
    assertEquals(new WellName(26, 0), wellName);

    wellName = new WellName("AF48");
    assertEquals(31, wellName.getRowIndex());
    assertEquals(47, wellName.getColumnIndex());
    assertEquals("AF", wellName.getRowLabel());
    assertEquals("48", wellName.getColumnLabel());
    assertEquals(new WellName(31, 47), wellName);
    
    try {
      new WellName("G0");
      fail("expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
    }
  }

}

