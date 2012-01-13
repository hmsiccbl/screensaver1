// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class VolumeTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(VolumeTest.class);


  // instance data members

  // public constructors and methods
  
  public void testDefaultUnits()
  {
    Volume volume = new Volume(1);
    assertEquals(VolumeUnit.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000"), volume.getValue()); 
  }
  
  public void testExplicitUnits()
  {
    Volume volume = new Volume(1, VolumeUnit.MICROLITERS);
    assertEquals(VolumeUnit.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000"), volume.getValue()); 
    volume = new Volume(1, VolumeUnit.NANOLITERS);
    assertEquals(VolumeUnit.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1"), volume.getValue()); 
  }
  
  public void testConvertToLargerUnits()
  {
    Volume volume = new Volume(1000, VolumeUnit.NANOLITERS);
    Volume newVolume = volume.convert(VolumeUnit.MICROLITERS);
    assertEquals(VolumeUnit.MICROLITERS, newVolume.getUnits());
    assertEquals(new BigDecimal("1.000"), newVolume.getValue());
  }

  public void testConvertToSmallerUnits()
  {
    Volume volume = new Volume(1, VolumeUnit.MICROLITERS);
    Volume newVolume = volume.convert(VolumeUnit.NANOLITERS);
    assertEquals(VolumeUnit.NANOLITERS, newVolume.getUnits());
    assertEquals(new BigDecimal("1000"), newVolume.getValue());
  }

  public void testConvertToReasonableUnits()
  {
    Volume volume = new Volume("0.000001", VolumeUnit.LITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000"), volume.getValue());

    volume = new Volume("0.0000012", VolumeUnit.LITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.200"), volume.getValue());

    volume = new Volume("0.0000001", VolumeUnit.LITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("100"), volume.getValue());

    volume = new Volume("0.000000999", VolumeUnit.LITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("999"), volume.getValue());

    volume = new Volume("1.000001", VolumeUnit.LITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.LITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000001000"), volume.getValue());
    
    volume = new Volume("0.1", VolumeUnit.MICROLITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("100"), volume.getValue());

    volume = new Volume("0.001", VolumeUnit.MICROLITERS).convertToReasonableUnits();
    assertEquals(VolumeUnit.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1"), volume.getValue());
}
  
  public void testValueExceedsScale()
  {
    try {
      Volume volume = new Volume("1000.1", VolumeUnit.NANOLITERS);
      fail("expected exception: " + volume);
    }
    catch (Exception e) {}
    try {
      Volume volume = new Volume("1000000.1001", VolumeUnit.MICROLITERS);
      fail("expected exception: " + volume);
    }
    catch (Exception e) {}
  }
  
  public void testGetValueWithConversion()
  {
    assertEquals(new BigDecimal("1.000"), new Volume(1, VolumeUnit.MICROLITERS).getValue(VolumeUnit.MICROLITERS));
    assertEquals(new BigDecimal("0.000001000"), new Volume(1, VolumeUnit.MICROLITERS).getValue(VolumeUnit.LITERS));
    assertEquals(new BigDecimal("1.000"), new Volume(1000, VolumeUnit.NANOLITERS).getValue(VolumeUnit.MICROLITERS));
    assertEquals(new BigDecimal("0.999"), new Volume(999, VolumeUnit.NANOLITERS).getValue(VolumeUnit.MICROLITERS));
    assertEquals(new BigDecimal("1.001"), new Volume(1001, VolumeUnit.NANOLITERS).getValue(VolumeUnit.MICROLITERS));
    assertEquals(new BigDecimal("1000"), new Volume(1, VolumeUnit.MICROLITERS).getValue(VolumeUnit.NANOLITERS));
    assertEquals(new BigDecimal("1001"), new Volume("1.001", VolumeUnit.MICROLITERS).getValue(VolumeUnit.NANOLITERS));
  }
  
  public void testAdd()
  {
    assertEquals(new Volume(3), new Volume(1).add(new Volume(2)));
    assertEquals(new Volume(3, VolumeUnit.MICROLITERS), new Volume(1, VolumeUnit.MICROLITERS).add(new Volume(2, VolumeUnit.MICROLITERS)));
    assertEquals(new Volume(3, VolumeUnit.NANOLITERS), new Volume(1, VolumeUnit.NANOLITERS).add(new Volume(2, VolumeUnit.NANOLITERS)));
    assertEquals(new Volume(2001, VolumeUnit.NANOLITERS), new Volume(1, VolumeUnit.NANOLITERS).add(new Volume(2, VolumeUnit.MICROLITERS)));
    assertEquals(new Volume("2.001", VolumeUnit.MICROLITERS), new Volume(2, VolumeUnit.MICROLITERS).add(new Volume(1, VolumeUnit.NANOLITERS)));
  }
  
  public void testSubtract()
  {
    assertEquals(new Volume(-1), new Volume(1).subtract(new Volume(2)));
    assertEquals(new Volume(-1, VolumeUnit.MICROLITERS), new Volume(1, VolumeUnit.MICROLITERS).subtract(new Volume(2, VolumeUnit.MICROLITERS)));
    assertEquals(new Volume(-1, VolumeUnit.NANOLITERS), new Volume(1, VolumeUnit.NANOLITERS).subtract(new Volume(2, VolumeUnit.NANOLITERS)));
    assertEquals(new Volume(-1999, VolumeUnit.NANOLITERS), new Volume(1, VolumeUnit.NANOLITERS).subtract(new Volume(2, VolumeUnit.MICROLITERS)));
    assertEquals(new Volume("1.999", VolumeUnit.MICROLITERS), new Volume(2, VolumeUnit.MICROLITERS).subtract(new Volume(1, VolumeUnit.NANOLITERS)));
  }
  
  public void testNegate()
  {
    assertEquals(new Volume(-1), new Volume(1).negate());
    assertEquals(new Volume(1), new Volume(-1).negate());
    assertEquals(new Volume(0), new Volume(0).negate());
  }
  
  public void testEquals()
  {
    assertEquals(new Volume(0, VolumeUnit.MICROLITERS), new Volume(0, VolumeUnit.NANOLITERS));
    assertEquals(new Volume(1, VolumeUnit.MICROLITERS), new Volume(1000, VolumeUnit.NANOLITERS));
    assertEquals(new Volume("0.1", VolumeUnit.MICROLITERS), new Volume(100, VolumeUnit.NANOLITERS));
    assertEquals(new Volume("0.001", VolumeUnit.MICROLITERS), new Volume(1, VolumeUnit.NANOLITERS));
    assertEquals(new Volume(1000, VolumeUnit.NANOLITERS), new Volume(1, VolumeUnit.MICROLITERS));
    assertFalse(new Volume(1, VolumeUnit.NANOLITERS).equals(new Volume(1, VolumeUnit.MICROLITERS)));
    assertFalse(new Volume(1, VolumeUnit.MICROLITERS).equals(new Volume(1, VolumeUnit.NANOLITERS)));
  }
  
  public void testCompareEquals()
  {
    assertEquals(0, new Volume(0, VolumeUnit.MICROLITERS).compareTo(new Volume(0, VolumeUnit.MICROLITERS)));
    assertEquals(0, new Volume(0, VolumeUnit.MICROLITERS).compareTo(new Volume(0, VolumeUnit.NANOLITERS)));
    assertEquals(0, new Volume(0, VolumeUnit.NANOLITERS).compareTo(new Volume(0, VolumeUnit.MICROLITERS)));
    assertEquals(-1, new Volume(1, VolumeUnit.NANOLITERS).compareTo(new Volume(1, VolumeUnit.MICROLITERS)));
    assertEquals(1, new Volume(1, VolumeUnit.MICROLITERS).compareTo(new Volume(1, VolumeUnit.NANOLITERS)));
    assertEquals(0, new Volume(1, VolumeUnit.MICROLITERS).compareTo(new Volume(1000, VolumeUnit.NANOLITERS)));
    assertEquals(0, new Volume("0.001", VolumeUnit.MICROLITERS).compareTo(new Volume(1, VolumeUnit.NANOLITERS)));
    assertEquals(0, new Volume(1, VolumeUnit.NANOLITERS).compareTo(new Volume("0.001", VolumeUnit.MICROLITERS)));
  }    
  
  public void testToString()
  {
    assertEquals("0 L", new Volume("0", VolumeUnit.LITERS).toString());
    assertEquals("0 L", new Volume("0.0", VolumeUnit.LITERS).toString());
    assertEquals("0.1 L", new Volume("0.1", VolumeUnit.LITERS).toString());
    assertEquals("0.1 L", new Volume("0.100", VolumeUnit.LITERS).toString());
    assertEquals("0.1001 L", new Volume("0.1001", VolumeUnit.LITERS).toString());
    assertEquals("10 L", new Volume("10", VolumeUnit.LITERS).toString());
    assertEquals("10 L", new Volume("10.0", VolumeUnit.LITERS).toString());
    assertEquals("10 L", new Volume("10.0000", VolumeUnit.LITERS).toString());
    assertEquals("10 L", new Volume("10.0", VolumeUnit.LITERS).toString());
    assertEquals("10.01 L", new Volume("10.01", VolumeUnit.LITERS).toString());
    assertEquals("1 uL", new Volume("1", VolumeUnit.MICROLITERS).toString());
    assertEquals("1.001 uL", new Volume("1.001", VolumeUnit.MICROLITERS).toString());
    assertEquals("1.001 uL", new Volume("1.0010", VolumeUnit.MICROLITERS).toString());
    assertEquals("1.001 mL", new Volume("1.00100", VolumeUnit.MILLILITERS).toString());
    assertEquals("1.00101 mL", new Volume("1.00101", VolumeUnit.MILLILITERS).toString());
  }
  

  // private methods

}
