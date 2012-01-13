// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.DataModelViolationException;

import junit.framework.TestCase;

public class ReagentVendorIdentifierTest extends TestCase
{
  // public constructors and methods

  public void testInstantiate()
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("vendor", "id");
    assertEquals(rvi.toString(), "vendor id");
    assertEquals(rvi.getVendorName(), "vendor");
    assertEquals(rvi.getVendorIdentifier(), "id");

    rvi = new ReagentVendorIdentifier("vendor", "id");
    assertEquals(rvi.toString(), "vendor id");
    assertEquals(rvi.getVendorName(), "vendor");
    assertEquals(rvi.getVendorIdentifier(), "id");

    try {
      new ReagentVendorIdentifier("vendor", "");
      fail("empty vendor identifier: expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    try {
      new ReagentVendorIdentifier("vendor", null);
      fail("null vendor identifier: expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    try {
      new ReagentVendorIdentifier(null, "id");
      fail("null vendor name: expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    try {
      new ReagentVendorIdentifier("", "id");
      fail("empty vendor name: expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
  }

  public void testToString()
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("vendor", "id");
    assertEquals(rvi.toString(), "vendor id");
  }

  public void testEquals()
  {
    ReagentVendorIdentifier rvi1 = new ReagentVendorIdentifier("vendor1", "id");
    ReagentVendorIdentifier rvi2 = new ReagentVendorIdentifier("vendor2", "id");
    assertFalse(rvi1.equals(rvi2));
    assertFalse(rvi2.equals(rvi1));

    rvi1 = new ReagentVendorIdentifier("vendor", "id1");
    rvi2 = new ReagentVendorIdentifier("vendor", "id2");
    assertFalse(rvi1.equals(rvi2));
    assertFalse(rvi2.equals(rvi1));

    rvi1 = new ReagentVendorIdentifier("vendor1", "id1");
    rvi2 = new ReagentVendorIdentifier("vendor1", "id1");
    assertTrue(rvi1.equals(rvi2));
    assertTrue(rvi2.equals(rvi1));
  }


  // private methods

}
