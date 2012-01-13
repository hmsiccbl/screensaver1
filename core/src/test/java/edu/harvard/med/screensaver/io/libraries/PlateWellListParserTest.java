// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.util.Pair;

public class PlateWellListParserTest extends TestCase
{
  public void testPlateWelListParserOnSingleLineInput()
  {
    PlateWellListParserResult result = PlateWellListParser.parseWellsFromPlateWellList("8 A2 B3");
    assertEquals("syntax errors size", 0, result.getErrors().size());
    List<WellKey> wellKeys = new ArrayList<WellKey>(result.getParsedWellKeys());
    assertEquals("wells parsed", new WellKey(8, 0, 1), wellKeys.get(0));
    assertEquals("wells parsed", new WellKey(8, 1, 2), wellKeys.get(1));
  }

  public void testPlateWelListParserOnMultiLineInput()
  {
    PlateWellListParserResult result = PlateWellListParser.parseWellsFromPlateWellList("8 A2 B3\n10 C4");
    assertEquals("syntax errors size", 0, result.getErrors().size());
    List<WellKey> wells = new ArrayList<WellKey>(result.getParsedWellKeys());
    assertEquals("wells found", new WellKey(8, 0, 1), wells.get(0));
    assertEquals("wells found", new WellKey(8, 1, 2), wells.get(1));
    assertEquals("wells found", new WellKey(10, 2, 3), wells.get(2));
  }

  public void testPlateWelListParserWithErrors()
  {
    PlateWellListParserResult result = PlateWellListParser.parseWellsFromPlateWellList("8 A2 B3\n20 A2 B3\nPLL-2L\n21 Q20 P0\n11 A4\n1 P24");
    List<Pair<Integer,String>> syntaxErrors = result.getErrors();
    assertEquals("syntax errors size", 2, result.getErrors().size());
    assertEquals("syntax errors", "invalid plate number PLL-2L", syntaxErrors.get(0).getSecond());
    assertEquals("syntax errors", "invalid well name P0 (plate 21)", syntaxErrors.get(1).getSecond());
  }
}

