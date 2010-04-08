// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
    Concentration c = 
      new Concentration(1,ConcentrationUnit.DEFAULT );
    assertEquals(ConcentrationUnit.MILLIMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000000000"), c.getValue()); 
  }
  
  public void testExplicitUnits()
  {
    Concentration c = new Concentration(1, ConcentrationUnit.MICROMOLAR);
    assertEquals(ConcentrationUnit.MICROMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000000"), c.getValue()); 
    c = new Concentration(1, ConcentrationUnit.NANOMOLAR);
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000"), c.getValue()); 
  }
  
  public void testConvertToLargerUnits()
  {
    Concentration c = new Concentration(1000, ConcentrationUnit.NANOMOLAR);
    Concentration newc = (Concentration) c.convert(ConcentrationUnit.MICROMOLAR);
    assertEquals(ConcentrationUnit.MICROMOLAR, newc.getUnits());
    assertEquals(new BigDecimal("1.000000"), newc.getValue());
  }

  public void testConvertToSmallerUnits()
  {
    Concentration c = new Concentration(1, ConcentrationUnit.MICROMOLAR);
    Concentration newc = c.convert(ConcentrationUnit.NANOMOLAR);
    assertEquals(ConcentrationUnit.NANOMOLAR, newc.getUnits());
    assertEquals(new BigDecimal("1000.000"), newc.getValue());
  }

  public void testConvertToReasonableUnits()
  {
    Concentration c = new Concentration("0.000001", ConcentrationUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000"), c.getValue());

    c = new Concentration("0.0000012", ConcentrationUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.200"), c.getValue());

    c = new Concentration("0.0001", ConcentrationUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("100.000"), c.getValue());

    c = new Concentration("0.000999", ConcentrationUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("999.000"), c.getValue());

    c = new Concentration("1.000001", ConcentrationUnit.MILLIMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.MILLIMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000001000"), c.getValue());
    
    c = new Concentration("0.1", ConcentrationUnit.MICROMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("100.000"), c.getValue());

    c = new Concentration("0.001", ConcentrationUnit.MICROMOLAR).convertToReasonableUnits();
    assertEquals(ConcentrationUnit.NANOMOLAR, c.getUnits());
    assertEquals(new BigDecimal("1.000"), c.getValue());
}
  
  public void testValueExceedsScale()
  {
    try {
      Concentration c = new Concentration("0.1", ConcentrationUnit.PICOMOLAR);
      fail("expected exception: " + c);
    }
    catch (Exception e) {}
    try {
      Concentration c = new Concentration("1000000.1000001", ConcentrationUnit.MICROMOLAR);
      fail("expected exception: " + c);
    }
    catch (Exception e) {}
  }
  
  public void testGetValueWithConversion()
  {
    assertEquals(new BigDecimal("1.000000"), new Concentration(1, ConcentrationUnit.MICROMOLAR).getValue(ConcentrationUnit.MICROMOLAR));
    assertEquals(new BigDecimal("1.000"), new Concentration(1, ConcentrationUnit.NANOMOLAR).getValue(ConcentrationUnit.NANOMOLAR));
    assertEquals(new BigDecimal("1.000"), new Concentration(1, ConcentrationUnit.NANOMOLAR).getValue(ConcentrationUnit.NANOMOLAR));
    assertEquals(new BigDecimal("1.000000"), new Concentration(1000, ConcentrationUnit.NANOMOLAR).getValue(ConcentrationUnit.MICROMOLAR));
    assertEquals(new BigDecimal("0.999000"), new Concentration(999, ConcentrationUnit.NANOMOLAR).getValue(ConcentrationUnit.MICROMOLAR));
    assertEquals(new BigDecimal("1.001000"), new Concentration(1001, ConcentrationUnit.NANOMOLAR).getValue(ConcentrationUnit.MICROMOLAR));
    assertEquals(new BigDecimal("1000.000"), new Concentration(1, ConcentrationUnit.MICROMOLAR).getValue(ConcentrationUnit.NANOMOLAR));
    assertEquals(new BigDecimal("1001.000"), new Concentration("1.001", ConcentrationUnit.MICROMOLAR).getValue(ConcentrationUnit.NANOMOLAR));

    assertEquals(new BigDecimal("0.000000001"), 
                 new Concentration("1", ConcentrationUnit.PICOMOLAR).getValue(ConcentrationUnit.MILLIMOLAR));
    assertEquals(new BigDecimal("999.999999999"), 
                 new Concentration("999999999999", ConcentrationUnit.PICOMOLAR).getValue(ConcentrationUnit.MILLIMOLAR));
    assertEquals(new BigDecimal("999999999999"), 
                 new Concentration("999.999999999", ConcentrationUnit.MILLIMOLAR).getValue(ConcentrationUnit.PICOMOLAR));
  }
  
  public void testAdd()
  {
    assertEquals(new Concentration(3, ConcentrationUnit.DEFAULT), 
                 new Concentration(1, ConcentrationUnit.DEFAULT).
                 add(new Concentration(2, ConcentrationUnit.DEFAULT)));
    assertEquals(new Concentration(3, ConcentrationUnit.MICROMOLAR), 
                 new Concentration(1, ConcentrationUnit.MICROMOLAR).
                 add(new Concentration(2, ConcentrationUnit.MICROMOLAR)));
    assertEquals(new Concentration(3, ConcentrationUnit.NANOMOLAR), 
                 new Concentration(1, ConcentrationUnit.NANOMOLAR).
                 add(new Concentration(2, ConcentrationUnit.NANOMOLAR)));
    assertEquals(new Concentration(2001, ConcentrationUnit.NANOMOLAR), 
                 new Concentration(1, ConcentrationUnit.NANOMOLAR).
                 add(new Concentration(2, ConcentrationUnit.MICROMOLAR)));
    assertEquals(new Concentration("2.001", ConcentrationUnit.MICROMOLAR), 
                 new Concentration(2, ConcentrationUnit.MICROMOLAR).
                 add(new Concentration(1, ConcentrationUnit.NANOMOLAR)));
  }
  
  public void testSubtract()
  {
    assertEquals(new Concentration(-1, ConcentrationUnit.DEFAULT), 
                 new Concentration(1, ConcentrationUnit.DEFAULT).
                 subtract(new Concentration(2, ConcentrationUnit.DEFAULT)));
    assertEquals(new Concentration(-1, ConcentrationUnit.MICROMOLAR), 
                 new Concentration(1, ConcentrationUnit.MICROMOLAR).
                 subtract(new Concentration(2, ConcentrationUnit.MICROMOLAR)));
    assertEquals(new Concentration(-1, ConcentrationUnit.NANOMOLAR), 
                 new Concentration(1, ConcentrationUnit.NANOMOLAR).
                 subtract(new Concentration(2, ConcentrationUnit.NANOMOLAR)));
    assertEquals(new Concentration(-1999, ConcentrationUnit.NANOMOLAR), 
                 new Concentration(1, ConcentrationUnit.NANOMOLAR).
                 subtract(new Concentration(2, ConcentrationUnit.MICROMOLAR)));
    assertEquals(new Concentration("1.999", ConcentrationUnit.MICROMOLAR), 
                 new Concentration(2, ConcentrationUnit.MICROMOLAR).
                 subtract(new Concentration(1, ConcentrationUnit.NANOMOLAR)));
  }
  
  public void testNegate()
  {
    assertEquals(new Concentration(-1, ConcentrationUnit.DEFAULT), new Concentration(1, ConcentrationUnit.DEFAULT).negate());
    assertEquals(new Concentration(1, ConcentrationUnit.DEFAULT), new Concentration(-1, ConcentrationUnit.DEFAULT).negate());
    assertEquals(new Concentration(0, ConcentrationUnit.DEFAULT), new Concentration(0, ConcentrationUnit.DEFAULT).negate());
  }
  
  public void testEquals()
  {
    assertEquals(new Concentration(0, ConcentrationUnit.MICROMOLAR), new Concentration(0, ConcentrationUnit.NANOMOLAR));
    assertEquals(new Concentration(1, ConcentrationUnit.MICROMOLAR), new Concentration(1000, ConcentrationUnit.NANOMOLAR));
    assertEquals(new Concentration("0.1", ConcentrationUnit.MICROMOLAR), new Concentration(100, ConcentrationUnit.NANOMOLAR));
    assertEquals(new Concentration("0.001", ConcentrationUnit.MICROMOLAR), new Concentration(1, ConcentrationUnit.NANOMOLAR));
    assertEquals(new Concentration(1000, ConcentrationUnit.NANOMOLAR), new Concentration(1, ConcentrationUnit.MICROMOLAR));
    assertFalse(new Concentration(1, ConcentrationUnit.NANOMOLAR).equals(new Concentration(1, ConcentrationUnit.MICROMOLAR)));
    assertFalse(new Concentration(1, ConcentrationUnit.MICROMOLAR).equals(new Concentration(1, ConcentrationUnit.NANOMOLAR)));
  }
  
  public void testCompareEquals()
  {
    assertEquals(0, new Concentration(0, ConcentrationUnit.MICROMOLAR).
                 compareTo(new Concentration(0, ConcentrationUnit.MICROMOLAR)));
    assertEquals(0, new Concentration(0, ConcentrationUnit.MICROMOLAR).
                 compareTo(new Concentration(0, ConcentrationUnit.NANOMOLAR)));
    assertEquals(0, new Concentration(0, ConcentrationUnit.NANOMOLAR).
                 compareTo(new Concentration(0, ConcentrationUnit.MICROMOLAR)));
    assertEquals(-1, new Concentration(1, ConcentrationUnit.NANOMOLAR).
                 compareTo(new Concentration(1, ConcentrationUnit.MICROMOLAR)));
    assertEquals(1, new Concentration(1, ConcentrationUnit.MICROMOLAR).
                 compareTo(new Concentration(1, ConcentrationUnit.NANOMOLAR)));
    assertEquals(0, new Concentration(1, ConcentrationUnit.MICROMOLAR).
                 compareTo(new Concentration(1000, ConcentrationUnit.NANOMOLAR)));
    assertEquals(0, new Concentration("0.001", ConcentrationUnit.MICROMOLAR).
                 compareTo(new Concentration(1, ConcentrationUnit.NANOMOLAR)));
    assertEquals(0, new Concentration(1, ConcentrationUnit.NANOMOLAR).
                 compareTo(new Concentration("0.001", ConcentrationUnit.MICROMOLAR)));
  }    
  
  public void testToString()
  {
    assertEquals("0.1001 mM", new Concentration("0.1001", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new Concentration("10", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new Concentration("10.0", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new Concentration("10.0000", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("10 mM", new Concentration("10.0", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("10.01 mM", new Concentration("10.01", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("1 uM", new Concentration("1", ConcentrationUnit.MICROMOLAR).toString());
    assertEquals("1.001 uM", new Concentration("1.001", ConcentrationUnit.MICROMOLAR).toString());
    assertEquals("1.001 uM", new Concentration("1.0010", ConcentrationUnit.MICROMOLAR).toString());
    assertEquals("1.001 mM", new Concentration("1.00100", ConcentrationUnit.MILLIMOLAR).toString());
    assertEquals("1.00101 mM", new Concentration("1.00101", ConcentrationUnit.MILLIMOLAR).toString());
  }
  

  // private methods

}
