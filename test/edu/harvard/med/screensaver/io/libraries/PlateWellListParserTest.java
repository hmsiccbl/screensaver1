// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

public class PlateWellListParserTest extends AbstractSpringTest
{
  private static Logger log = Logger.getLogger(PlateWellListParserTest.class);

  protected PlateWellListParser plateWellListParser;
  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected SchemaUtil schemaUtil;

  private Library _library1;

  private Library _library2;
  
  // AbstractDependencyInjectionSpringContextTests methods

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();

    schemaUtil.truncateTablesOrCreateSchema();

    _library1 = new Library("Test Library 1", "testlib1", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 10);
    genericEntityDao.persistEntity(_library1);
    librariesDao.loadOrCreateWellsForLibrary(_library1);
    _library2 = new Library("Test Library 2", "testlib2", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 21, 30);
    genericEntityDao.persistEntity(_library2);
    librariesDao.loadOrCreateWellsForLibrary(_library2);
  }
  
  public void testPlateWelListParserOnSingleLineInput()
  {
    PlateWellListParserResult result = plateWellListParser.lookupWellsFromPlateWellList("8 A2 B3");
    assertEquals("fatal errors size", 0, result.getFatalErrors().size());
    assertEquals("syntax errors size", 0, result.getSyntaxErrors().size());
    assertEquals("wells not found size", 0, result.getWellsNotFound().size());
    assertEquals("wells found size", 2, result.getWells().size());
    List<Well> wells = new ArrayList<Well>(result.getWells());
    assertEquals("wells found", new WellKey(8, 0, 1), wells.get(0).getWellKey());
    assertEquals("wells found", new WellKey(8, 1, 2), wells.get(1).getWellKey());
  }

  public void testPlateWelListParserOnMultiLineInput()
  {
    PlateWellListParserResult result = plateWellListParser.lookupWellsFromPlateWellList("8 A2 B3\n10 C4");
    assertEquals("fatal errors size", 0, result.getFatalErrors().size());
    assertEquals("syntax errors size", 0, result.getSyntaxErrors().size());
    assertEquals("wells not found size", 0, result.getWellsNotFound().size());
    assertEquals("wells found size", 3, result.getWells().size());
    List<Well> wells = new ArrayList<Well>(result.getWells());
    assertEquals("wells found", new WellKey(8, 0, 1), wells.get(0).getWellKey());
    assertEquals("wells found", new WellKey(8, 1, 2), wells.get(1).getWellKey());
    assertEquals("wells found", new WellKey(10, 2, 3), wells.get(2).getWellKey());
  }

  public void testPlateWelListParserWithErrors()
  {
    PlateWellListParserResult result = plateWellListParser.lookupWellsFromPlateWellList("8 A2 B3\n20 A2 B3\nPLL-2L\n21 Q20 P0\n11 A4\n1 P24");
    assertEquals("fatal errors", 0, result.getFatalErrors().size());
    List<Pair<Integer,String>> syntaxErrors = result.getSyntaxErrors();
    assertEquals("syntax errors size", 3, result.getSyntaxErrors().size());
    assertEquals("syntax errors", "invalid plate number PLL-2L", syntaxErrors.get(0).getSecond());
    assertEquals("syntax errors", "invalid well name Q20", syntaxErrors.get(1).getSecond());
    assertEquals("syntax errors", "invalid well name P0", syntaxErrors.get(2).getSecond());
    List<WellKey> wellsNotFound = new ArrayList<WellKey>(result.getWellsNotFound());
    assertEquals("wells not found size", 3, result.getWellsNotFound().size());
    assertEquals("wells not found", new WellKey(11, "A4"), wellsNotFound.get(0));
    assertEquals("wells not found", new WellKey(20, "A2"), wellsNotFound.get(1));
    assertEquals("wells not found", new WellKey(20, "B3"), wellsNotFound.get(2));
    List<Well> wells = new ArrayList<Well>(result.getWells());
    assertEquals("wells found size", 3, result.getWells().size());
    assertEquals("wells found", new WellKey(1, 15, 23), wells.get(0).getWellKey());
    assertEquals("wells found", new WellKey(8, 0, 1), wells.get(1).getWellKey());
    assertEquals("wells found", new WellKey(8, 1, 2), wells.get(2).getWellKey());
  }
}

