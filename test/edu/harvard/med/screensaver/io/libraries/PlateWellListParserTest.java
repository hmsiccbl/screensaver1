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
    genericEntityDao.saveOrUpdateEntity(_library1);
    librariesDao.loadOrCreateWellsForLibrary(_library1);
    _library2 = new Library("Test Library 2", "testlib2", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 21, 30);
    genericEntityDao.saveOrUpdateEntity(_library2);
    librariesDao.loadOrCreateWellsForLibrary(_library2);
  }
  
  public void testPlateWelListParserOnSingleLineInput()
  {
    PlateWellListParserResult result = plateWellListParser.parseWellsFromPlateWellList("8 A2 B3");
    assertEquals("syntax errors size", 0, result.getErrors().size());
    List<WellKey> wellKeys = new ArrayList<WellKey>(result.getParsedWellKeys());
    assertEquals("wells parsed", new WellKey(8, 0, 1), wellKeys.get(0));
    assertEquals("wells parsed", new WellKey(8, 1, 2), wellKeys.get(1));
  }

  public void testPlateWelListParserOnMultiLineInput()
  {
    PlateWellListParserResult result = plateWellListParser.parseWellsFromPlateWellList("8 A2 B3\n10 C4");
    assertEquals("syntax errors size", 0, result.getErrors().size());
    List<WellKey> wells = new ArrayList<WellKey>(result.getParsedWellKeys());
    assertEquals("wells found", new WellKey(8, 0, 1), wells.get(0));
    assertEquals("wells found", new WellKey(8, 1, 2), wells.get(1));
    assertEquals("wells found", new WellKey(10, 2, 3), wells.get(2));
  }

  public void testPlateWelListParserWithErrors()
  {
    PlateWellListParserResult result = plateWellListParser.parseWellsFromPlateWellList("8 A2 B3\n20 A2 B3\nPLL-2L\n21 Q20 P0\n11 A4\n1 P24");
    List<Pair<Integer,String>> syntaxErrors = result.getErrors();
    assertEquals("syntax errors size", 3, result.getErrors().size());
    assertEquals("syntax errors", "invalid plate number PLL-2L", syntaxErrors.get(0).getSecond());
    assertEquals("syntax errors", "invalid well name Q20 (plate 21)", syntaxErrors.get(1).getSecond());
    assertEquals("syntax errors", "invalid well name P0 (plate 21)", syntaxErrors.get(2).getSecond());
  }
}

