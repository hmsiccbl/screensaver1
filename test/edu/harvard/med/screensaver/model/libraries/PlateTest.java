// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;
import java.math.BigDecimal;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.TestDataFactory;

public class PlateTest extends AbstractEntityInstanceTest<Plate>
{
  public static TestSuite suite()
  {
    return buildTestSuite(PlateTest.class, Plate.class);
  }

  public PlateTest()
  {
    super(Plate.class);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    dataFactory.addBuilder(new TestDataFactory.AbstractBuilder<BigDecimal>(BigDecimal.class,
                                                                           "testEntityProperty:mgMlConcentration") {
      @Override
      public BigDecimal newInstance(String callStack)
      {
        return new BigDecimal("1.1");
      }
    });
  }
}

