// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import javax.faces.convert.ConverterException;

import edu.harvard.med.screensaver.model.libraries.LibraryType;

import junit.framework.TestCase;

public class EnumTypeConverterTest extends TestCase
{
  public void testGetAsObject()
  {
    TestEnumTypeConverter converter = new TestEnumTypeConverter();
    for (LibraryType libraryType : LibraryType.values()) {
      assertEquals(libraryType,
                   converter.getAsObject(null, null, libraryType.getValue()));
    }
    assertEquals(LibraryType.COMMERCIAL,
                 converter.getAsObject(null, null, "Commercial"));
    assertEquals(LibraryType.COMMERCIAL,
                 converter.getAsObject(null, null, "COMMERCIAL"));
    assertEquals(LibraryType.COMMERCIAL,
                 converter.getAsObject(null, null, "Commercial "));
    assertEquals(LibraryType.COMMERCIAL,
                 converter.getAsObject(null, null, "CoMMercial "));
    assertEquals(LibraryType.COMMERCIAL,
                 converter.getAsObject(null, null, " Commercial,"));
    try {
      converter.getAsObject(null, null, "invalid");
      fail("expected ConverterException");
    }
    catch (ConverterException e) {
    }
    catch (Exception e)
    {
      fail("expected ConverterException");
    }
  }

  public void testGetAsString()
  {
    TestEnumTypeConverter converter = new TestEnumTypeConverter();
    String s = converter.getAsString(null, null, LibraryType.COMMERCIAL);
    assertEquals(s, LibraryType.COMMERCIAL.getValue());
  }
}
