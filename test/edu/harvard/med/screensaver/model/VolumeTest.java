// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.Volume.Units;

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
    assertEquals(Units.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000"), volume.getValue()); 
  }
  
  public void testExplicitUnits()
  {
    Volume volume = new Volume(1, Units.MICROLITERS);
    assertEquals(Units.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000"), volume.getValue()); 
    volume = new Volume(1, Units.NANOLITERS);
    assertEquals(Units.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1"), volume.getValue()); 
  }
  
  public void testConvertToLargerUnits()
  {
    Volume volume = new Volume(1000, Units.NANOLITERS);
    Volume newVolume = volume.convert(Units.MICROLITERS);
    assertEquals(Units.MICROLITERS, newVolume.getUnits());
    assertEquals(new BigDecimal("1.000"), newVolume.getValue());
  }

  public void testConvertToSmallerUnits()
  {
    Volume volume = new Volume(1, Units.MICROLITERS);
    Volume newVolume = volume.convert(Units.NANOLITERS);
    assertEquals(Units.NANOLITERS, newVolume.getUnits());
    assertEquals(new BigDecimal("1000"), newVolume.getValue());
  }

  public void testConvertToReasonableUnits()
  {
    Volume volume = new Volume("0.000001", Units.LITERS).convertToReasonableUnits();
    assertEquals(Units.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000"), volume.getValue());

    volume = new Volume("0.0000012", Units.LITERS).convertToReasonableUnits();
    assertEquals(Units.MICROLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.200"), volume.getValue());

    volume = new Volume("0.0000001", Units.LITERS).convertToReasonableUnits();
    assertEquals(Units.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("100"), volume.getValue());

    volume = new Volume("0.000000999", Units.LITERS).convertToReasonableUnits();
    assertEquals(Units.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("999"), volume.getValue());

    volume = new Volume("1.000001", Units.LITERS).convertToReasonableUnits();
    assertEquals(Units.LITERS, volume.getUnits());
    assertEquals(new BigDecimal("1.000001000"), volume.getValue());
    
    volume = new Volume("0.1", Units.MICROLITERS).convertToReasonableUnits();
    assertEquals(Units.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("100"), volume.getValue());

    volume = new Volume("0.001", Units.MICROLITERS).convertToReasonableUnits();
    assertEquals(Units.NANOLITERS, volume.getUnits());
    assertEquals(new BigDecimal("1"), volume.getValue());
}
  
  public void testValueExceedsScale()
  {
    try {
      Volume volume = new Volume("1000.1", Units.NANOLITERS);
      fail("expected exception: " + volume);
    }
    catch (Exception e) {}
    try {
      Volume volume = new Volume("1000000.1001", Units.MICROLITERS);
      fail("expected exception: " + volume);
    }
    catch (Exception e) {}
  }
  
  public void testGetValueWithConversion()
  {
    assertEquals(new BigDecimal("1.000"), new Volume(1, Units.MICROLITERS).getValue(Units.MICROLITERS));
    assertEquals(new BigDecimal("0.000001000"), new Volume(1, Units.MICROLITERS).getValue(Units.LITERS));
    assertEquals(new BigDecimal("1.000"), new Volume(1000, Units.NANOLITERS).getValue(Units.MICROLITERS));
    assertEquals(new BigDecimal("0.999"), new Volume(999, Units.NANOLITERS).getValue(Units.MICROLITERS));
    assertEquals(new BigDecimal("1.001"), new Volume(1001, Units.NANOLITERS).getValue(Units.MICROLITERS));
    assertEquals(new BigDecimal("1000"), new Volume(1, Units.MICROLITERS).getValue(Units.NANOLITERS));
    assertEquals(new BigDecimal("1001"), new Volume("1.001", Units.MICROLITERS).getValue(Units.NANOLITERS));
  }
  
  public void testAdd()
  {
    assertEquals(new Volume(3), new Volume(1).add(new Volume(2)));
    assertEquals(new Volume(3, Units.MICROLITERS), new Volume(1, Units.MICROLITERS).add(new Volume(2, Units.MICROLITERS)));
    assertEquals(new Volume(3, Units.NANOLITERS), new Volume(1, Units.NANOLITERS).add(new Volume(2, Units.NANOLITERS)));
    assertEquals(new Volume(2001, Units.NANOLITERS), new Volume(1, Units.NANOLITERS).add(new Volume(2, Units.MICROLITERS)));
    assertEquals(new Volume("2.001", Units.MICROLITERS), new Volume(2, Units.MICROLITERS).add(new Volume(1, Units.NANOLITERS)));
  }
  
  public void testSubtract()
  {
    assertEquals(new Volume(-1), new Volume(1).subtract(new Volume(2)));
    assertEquals(new Volume(-1, Units.MICROLITERS), new Volume(1, Units.MICROLITERS).subtract(new Volume(2, Units.MICROLITERS)));
    assertEquals(new Volume(-1, Units.NANOLITERS), new Volume(1, Units.NANOLITERS).subtract(new Volume(2, Units.NANOLITERS)));
    assertEquals(new Volume(-1999, Units.NANOLITERS), new Volume(1, Units.NANOLITERS).subtract(new Volume(2, Units.MICROLITERS)));
    assertEquals(new Volume("1.999", Units.MICROLITERS), new Volume(2, Units.MICROLITERS).subtract(new Volume(1, Units.NANOLITERS)));
  }
  
  public void testNegate()
  {
    assertEquals(new Volume(-1), new Volume(1).negate());
    assertEquals(new Volume(1), new Volume(-1).negate());
    assertEquals(new Volume(0), new Volume(0).negate());
  }
  
  public void testEquals()
  {
    assertEquals(new Volume(0, Units.MICROLITERS), new Volume(0, Units.NANOLITERS));
    assertEquals(new Volume(1, Units.MICROLITERS), new Volume(1000, Units.NANOLITERS));
    assertEquals(new Volume("0.1", Units.MICROLITERS), new Volume(100, Units.NANOLITERS));
    assertEquals(new Volume("0.001", Units.MICROLITERS), new Volume(1, Units.NANOLITERS));
    assertEquals(new Volume(1000, Units.NANOLITERS), new Volume(1, Units.MICROLITERS));
    assertFalse(new Volume(1, Units.NANOLITERS).equals(new Volume(1, Units.MICROLITERS)));
    assertFalse(new Volume(1, Units.MICROLITERS).equals(new Volume(1, Units.NANOLITERS)));
  }
  
  public void testCompareEquals()
  {
    assertEquals(0, new Volume(0, Units.MICROLITERS).compareTo(new Volume(0, Units.MICROLITERS)));
    assertEquals(0, new Volume(0, Units.MICROLITERS).compareTo(new Volume(0, Units.NANOLITERS)));
    assertEquals(0, new Volume(0, Units.NANOLITERS).compareTo(new Volume(0, Units.MICROLITERS)));
    assertEquals(-1, new Volume(1, Units.NANOLITERS).compareTo(new Volume(1, Units.MICROLITERS)));
    assertEquals(1, new Volume(1, Units.MICROLITERS).compareTo(new Volume(1, Units.NANOLITERS)));
    assertEquals(0, new Volume(1, Units.MICROLITERS).compareTo(new Volume(1000, Units.NANOLITERS)));
    assertEquals(0, new Volume("0.001", Units.MICROLITERS).compareTo(new Volume(1, Units.NANOLITERS)));
    assertEquals(0, new Volume(1, Units.NANOLITERS).compareTo(new Volume("0.001", Units.MICROLITERS)));
  }    
  
  public void testToString()
  {
    assertEquals("0L", new Volume("0", Units.LITERS).toString());
    assertEquals("0L", new Volume("0.0", Units.LITERS).toString());
    assertEquals("0.1L", new Volume("0.1", Units.LITERS).toString());
    assertEquals("0.1L", new Volume("0.100", Units.LITERS).toString());
    assertEquals("0.1001L", new Volume("0.1001", Units.LITERS).toString());
    assertEquals("10L", new Volume("10", Units.LITERS).toString());
    assertEquals("10L", new Volume("10.0", Units.LITERS).toString());
    assertEquals("10L", new Volume("10.0000", Units.LITERS).toString());
    assertEquals("10L", new Volume("10.0", Units.LITERS).toString());
    assertEquals("10.01L", new Volume("10.01", Units.LITERS).toString());
    assertEquals("1uL", new Volume("1", Units.MICROLITERS).toString());
    assertEquals("1.001uL", new Volume("1.001", Units.MICROLITERS).toString());
    assertEquals("1.001uL", new Volume("1.0010", Units.MICROLITERS).toString());
    assertEquals("1.001mL", new Volume("1.00100", Units.MILLILITERS).toString());
    assertEquals("1.00101mL", new Volume("1.00101", Units.MILLILITERS).toString());
  }
  

  // private methods

}
