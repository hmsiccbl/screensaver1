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

public class QuantityTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(QuantityTest.class);


  // instance data members

  // public constructors and methods
  
  public void testDefaultUnits()
  {
    MolarConcentration c = 
      new MolarConcentration(1,MolarUnit.DEFAULT );
    assertEquals(MolarUnit.MILLIMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000000000"), c.getValue()); 
  }
  
  public void testExplicitUnits()
  {
    MolarConcentration c = new MolarConcentration(1, MolarUnit.MICROMOLAR);
    assertEquals(MolarUnit.MICROMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000000"), c.getValue()); 
    c = new MolarConcentration(1, MolarUnit.NANOMOLAR);
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000"), c.getValue()); 
  }
  
  public void testConvertToLargerUnits()
  {
    MolarConcentration c = new MolarConcentration(1000, MolarUnit.NANOMOLAR);
    MolarConcentration newc = (MolarConcentration) c.convert(MolarUnit.MICROMOLAR);
    assertEquals(MolarUnit.MICROMOLAR, newc.getUnits());
    assertEquals(new BigDecimal("1.000000"), newc.getValue());
  }

  public void testConvertToSmallerUnits()
  {
    MolarConcentration c = new MolarConcentration(1, MolarUnit.MICROMOLAR);
    MolarConcentration newc = c.convert(MolarUnit.NANOMOLAR);
    assertEquals(MolarUnit.NANOMOLAR, newc.getUnits());
    assertEquals(new BigDecimal("1000.000"), newc.getValue());
  }

  public void testConvertToReasonableUnits()
  {
    MolarConcentration c = new MolarConcentration("0.000001", MolarUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000"), c.getValue());

    c = new MolarConcentration("0.0000012", MolarUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.200"), c.getValue());

    c = new MolarConcentration("0.0001", MolarUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("100.000"), c.getValue());

    c = new MolarConcentration("0.000999", MolarUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("999.000"), c.getValue());

    c = new MolarConcentration("1.000001", MolarUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.MILLIMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000001000"), c.getValue());
    
    c = new MolarConcentration("0.1", MolarUnit.MICROMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("100.000"), c.getValue());

    c = new MolarConcentration("0.001", MolarUnit.MICROMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000"), c.getValue());

    c = new MolarConcentration("-5.0", MolarUnit.MICROMOLAR).convertToReasonableUnits();
    assertEquals(MolarUnit.MICROMOLAR, c.getUnits());
    assertEquals(new BigDecimal("-5.000000"), c.getValue());
  }
  
  public void testValueExceedsScale()
  {
    try {
      MolarConcentration c = new MolarConcentration("0.1", MolarUnit.PICOMOLAR);
      fail("expected exception: " + c);
    }
    catch (Exception e) {}
    try {
      MolarConcentration c = new MolarConcentration("1000000.1000001", MolarUnit.MICROMOLAR);
      fail("expected exception: " + c);
    }
    catch (Exception e) {}
  }
  
  public void testGetValueWithConversion()
  {
    assertEquals(new BigDecimal("1.000000"), new MolarConcentration(1, MolarUnit.MICROMOLAR).getValue(MolarUnit.MICROMOLAR));
    assertEquals(new BigDecimal("1.000"), new MolarConcentration(1, MolarUnit.NANOMOLAR).getValue(MolarUnit.NANOMOLAR));
    assertEquals(new BigDecimal("1.000"), new MolarConcentration(1, MolarUnit.NANOMOLAR).getValue(MolarUnit.NANOMOLAR));
    assertEquals(new BigDecimal("1.000000"), new MolarConcentration(1000, MolarUnit.NANOMOLAR).getValue(MolarUnit.MICROMOLAR));
    assertEquals(new BigDecimal("0.999000"), new MolarConcentration(999, MolarUnit.NANOMOLAR).getValue(MolarUnit.MICROMOLAR));
    assertEquals(new BigDecimal("1.001000"), new MolarConcentration(1001, MolarUnit.NANOMOLAR).getValue(MolarUnit.MICROMOLAR));
    assertEquals(new BigDecimal("1000.000"), new MolarConcentration(1, MolarUnit.MICROMOLAR).getValue(MolarUnit.NANOMOLAR));
    assertEquals(new BigDecimal("1001.000"), new MolarConcentration("1.001", MolarUnit.MICROMOLAR).getValue(MolarUnit.NANOMOLAR));

    assertEquals(new BigDecimal("0.000000001"), 
                 new MolarConcentration("1", MolarUnit.PICOMOLAR).getValue(MolarUnit.MILLIMOLAR));
    assertEquals(new BigDecimal("999.999999999"), 
                 new MolarConcentration("999999999999", MolarUnit.PICOMOLAR).getValue(MolarUnit.MILLIMOLAR));
    assertEquals(new BigDecimal("999999999999"), 
                 new MolarConcentration("999.999999999", MolarUnit.MILLIMOLAR).getValue(MolarUnit.PICOMOLAR));
  }
  
  public void testAdd()
  {
    assertEquals(new MolarConcentration(3, MolarUnit.DEFAULT), 
                 new MolarConcentration(1, MolarUnit.DEFAULT).
                 add(new MolarConcentration(2, MolarUnit.DEFAULT)));
    assertEquals(new MolarConcentration(3, MolarUnit.MICROMOLAR), 
                 new MolarConcentration(1, MolarUnit.MICROMOLAR).
                 add(new MolarConcentration(2, MolarUnit.MICROMOLAR)));
    assertEquals(new MolarConcentration(3, MolarUnit.NANOMOLAR), 
                 new MolarConcentration(1, MolarUnit.NANOMOLAR).
                 add(new MolarConcentration(2, MolarUnit.NANOMOLAR)));
    assertEquals(new MolarConcentration(2001, MolarUnit.NANOMOLAR), 
                 new MolarConcentration(1, MolarUnit.NANOMOLAR).
                 add(new MolarConcentration(2, MolarUnit.MICROMOLAR)));
    assertEquals(new MolarConcentration("2.001", MolarUnit.MICROMOLAR), 
                 new MolarConcentration(2, MolarUnit.MICROMOLAR).
                 add(new MolarConcentration(1, MolarUnit.NANOMOLAR)));
  }
  
  public void testSubtract()
  {
    assertEquals(new MolarConcentration(-1, MolarUnit.DEFAULT), 
                 new MolarConcentration(1, MolarUnit.DEFAULT).
                 subtract(new MolarConcentration(2, MolarUnit.DEFAULT)));
    assertEquals(new MolarConcentration(-1, MolarUnit.MICROMOLAR), 
                 new MolarConcentration(1, MolarUnit.MICROMOLAR).
                 subtract(new MolarConcentration(2, MolarUnit.MICROMOLAR)));
    assertEquals(new MolarConcentration(-1, MolarUnit.NANOMOLAR), 
                 new MolarConcentration(1, MolarUnit.NANOMOLAR).
                 subtract(new MolarConcentration(2, MolarUnit.NANOMOLAR)));
    assertEquals(new MolarConcentration(-1999, MolarUnit.NANOMOLAR), 
                 new MolarConcentration(1, MolarUnit.NANOMOLAR).
                 subtract(new MolarConcentration(2, MolarUnit.MICROMOLAR)));
    assertEquals(new MolarConcentration("1.999", MolarUnit.MICROMOLAR), 
                 new MolarConcentration(2, MolarUnit.MICROMOLAR).
                 subtract(new MolarConcentration(1, MolarUnit.NANOMOLAR)));
  }
  
  public void testNegate()
  {
    assertEquals(new MolarConcentration(-1, MolarUnit.DEFAULT), new MolarConcentration(1, MolarUnit.DEFAULT).negate());
    assertEquals(new MolarConcentration(1, MolarUnit.DEFAULT), new MolarConcentration(-1, MolarUnit.DEFAULT).negate());
    assertEquals(new MolarConcentration(0, MolarUnit.DEFAULT), new MolarConcentration(0, MolarUnit.DEFAULT).negate());
  }
  
  public void testEquals()
  {
    assertEquals(new MolarConcentration(0, MolarUnit.MICROMOLAR), new MolarConcentration(0, MolarUnit.NANOMOLAR));
    assertEquals(new MolarConcentration(1, MolarUnit.MICROMOLAR), new MolarConcentration(1000, MolarUnit.NANOMOLAR));
    assertEquals(new MolarConcentration("0.1", MolarUnit.MICROMOLAR), new MolarConcentration(100, MolarUnit.NANOMOLAR));
    assertEquals(new MolarConcentration("0.001", MolarUnit.MICROMOLAR), new MolarConcentration(1, MolarUnit.NANOMOLAR));
    assertEquals(new MolarConcentration(1000, MolarUnit.NANOMOLAR), new MolarConcentration(1, MolarUnit.MICROMOLAR));
    assertFalse(new MolarConcentration(1, MolarUnit.NANOMOLAR).equals(new MolarConcentration(1, MolarUnit.MICROMOLAR)));
    assertFalse(new MolarConcentration(1, MolarUnit.MICROMOLAR).equals(new MolarConcentration(1, MolarUnit.NANOMOLAR)));
  }
  
  public void testCompareEquals()
  {
    assertEquals(0, new MolarConcentration(0, MolarUnit.MICROMOLAR).
                 compareTo(new MolarConcentration(0, MolarUnit.MICROMOLAR)));
    assertEquals(0, new MolarConcentration(0, MolarUnit.MICROMOLAR).
                 compareTo(new MolarConcentration(0, MolarUnit.NANOMOLAR)));
    assertEquals(0, new MolarConcentration(0, MolarUnit.NANOMOLAR).
                 compareTo(new MolarConcentration(0, MolarUnit.MICROMOLAR)));
    assertEquals(-1, new MolarConcentration(1, MolarUnit.NANOMOLAR).
                 compareTo(new MolarConcentration(1, MolarUnit.MICROMOLAR)));
    assertEquals(1, new MolarConcentration(1, MolarUnit.MICROMOLAR).
                 compareTo(new MolarConcentration(1, MolarUnit.NANOMOLAR)));
    assertEquals(0, new MolarConcentration(1, MolarUnit.MICROMOLAR).
                 compareTo(new MolarConcentration(1000, MolarUnit.NANOMOLAR)));
    assertEquals(0, new MolarConcentration("0.001", MolarUnit.MICROMOLAR).
                 compareTo(new MolarConcentration(1, MolarUnit.NANOMOLAR)));
    assertEquals(0, new MolarConcentration(1, MolarUnit.NANOMOLAR).
                 compareTo(new MolarConcentration("0.001", MolarUnit.MICROMOLAR)));
  }    
  
  public void testToString()
  {
    assertEquals("0.1001 mM", new MolarConcentration("0.1001", MolarUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new MolarConcentration("10", MolarUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new MolarConcentration("10.0", MolarUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new MolarConcentration("10.0000", MolarUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new MolarConcentration("10.0", MolarUnit.MILLIMOLAR).toString());
    assertEquals("10.01 mM", new MolarConcentration("10.01", MolarUnit.MILLIMOLAR).toString());
    assertEquals("1 uM", new MolarConcentration("1", MolarUnit.MICROMOLAR).toString());
    assertEquals("1.001 uM", new MolarConcentration("1.001", MolarUnit.MICROMOLAR).toString());
    assertEquals("1.001 uM", new MolarConcentration("1.0010", MolarUnit.MICROMOLAR).toString());
    assertEquals("1.001 mM", new MolarConcentration("1.00100", MolarUnit.MILLIMOLAR).toString());
    assertEquals("1.00101 mM", new MolarConcentration("1.00101", MolarUnit.MILLIMOLAR).toString());
  }
  

  // private methods

}
