// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("vendor:id");
    assertEquals(rvi.getReagentId(), "vendor:id");
    assertEquals(rvi.getVendorName(), "vendor");
    assertEquals(rvi.getVendorIdentifier(), "id");

    rvi = new ReagentVendorIdentifier("vendor", "id");
    assertEquals(rvi.getReagentId(), "vendor:id");
    assertEquals(rvi.getVendorName(), "vendor");
    assertEquals(rvi.getVendorIdentifier(), "id");

    try {
      rvi = new ReagentVendorIdentifier("vendor", "");
      fail("empty vendor identifier: expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    try {
      rvi = new ReagentVendorIdentifier("vendor", null);
      fail("null vendor identifier: expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    // this is valid usage until ticket #11048 is resolved
    rvi = new ReagentVendorIdentifier(null, "id");
    assertEquals(rvi.getReagentId(), ":id");
    assertEquals(rvi.getVendorName(), "");
    assertEquals(rvi.getVendorIdentifier(), "id");
  }

  public void testToString()
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("vendor:id");
    assertEquals(rvi.toString(), "vendor id");
  }

  public void testEquals()
  {
    ReagentVendorIdentifier rvi1 = new ReagentVendorIdentifier("vendor1:id");
    ReagentVendorIdentifier rvi2 = new ReagentVendorIdentifier("vendor2:id");
    assertFalse(rvi1.equals(rvi2));
    assertFalse(rvi2.equals(rvi1));

    rvi1 = new ReagentVendorIdentifier("vendor:id1");
    rvi2 = new ReagentVendorIdentifier("vendor:id2");
    assertFalse(rvi1.equals(rvi2));
    assertFalse(rvi2.equals(rvi1));

    rvi1 = new ReagentVendorIdentifier("vendor1:id1");
    rvi2 = new ReagentVendorIdentifier("vendor1:id1");
    assertTrue(rvi1.equals(rvi2));
    assertTrue(rvi2.equals(rvi1));
  }

  public void testInstantiateWithIllegalVendorName()
  {
    try {
      new ReagentVendorIdentifier("vendorPart1:vendorPart2", "id");
      fail("expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {
    }
  }

  // private methods

}
