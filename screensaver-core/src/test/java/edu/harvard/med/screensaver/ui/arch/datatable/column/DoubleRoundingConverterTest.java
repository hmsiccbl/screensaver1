// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

import javax.faces.convert.Converter;

import edu.harvard.med.screensaver.ui.arch.util.converter.DoubleRoundingConverter;

import junit.framework.TestCase;

public class DoubleRoundingConverterTest extends TestCase
{
  public void testDoubleRoundingConverter()
  {
    Converter converter;

    converter = DoubleRoundingConverter.getInstance(0);
    assertEquals("10", converter.getAsString(null, null, new Double(10)));
    assertEquals("10", converter.getAsString(null, null, new Double(10.1101)));
    assertEquals("10", converter.getAsString(null, null, new Double(10.5)));
    assertEquals("12", converter.getAsString(null, null, new Double(11.5)));
    assertEquals("11", converter.getAsString(null, null, new Double(10.99999)));

    converter = DoubleRoundingConverter.getInstance(1);
    assertEquals("10.0", converter.getAsString(null, null, new Double(10)));
    assertEquals("10.1", converter.getAsString(null, null, new Double(10.11)));
    assertEquals("10.2", converter.getAsString(null, null, new Double(10.19)));
    assertEquals("10.5", converter.getAsString(null, null, new Double(10.5)));
    assertEquals("11.0", converter.getAsString(null, null, new Double(10.99999)));

    converter = DoubleRoundingConverter.getInstance(3);
    assertEquals("10.000", converter.getAsString(null, null, new Double(10)));
    assertEquals("10.110", converter.getAsString(null, null, new Double(10.1101)));
    assertEquals("10.111", converter.getAsString(null, null, new Double(10.1109)));
    assertEquals("10.500", converter.getAsString(null, null, new Double(10.5)));
    assertEquals("11.000", converter.getAsString(null, null, new Double(10.99999)));

    converter = DoubleRoundingConverter.getInstance(-1);
    assertEquals("10.0", converter.getAsString(null, null, new Double(10)));
    assertEquals("10.5", converter.getAsString(null, null, new Double(10.5)));
    assertEquals("10.1101", converter.getAsString(null, null, new Double(10.1101)));
    assertEquals("10.1109", converter.getAsString(null, null, new Double(10.1109)));
    assertEquals("10.99999", converter.getAsString(null, null, new Double(10.99999)));
  }

}
