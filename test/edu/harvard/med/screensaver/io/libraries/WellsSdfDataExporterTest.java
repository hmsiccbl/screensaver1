// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;


public class WellsSdfDataExporterTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(WellsSdfDataExporterTest.class);

  protected LibrariesDAO librariesDao;

  private WellListDAOTransaction _wellListDAOTransaction;

  class WellListDAOTransaction implements DAOTransaction
  {
    public void runTransaction()
    {
      Library library = new Library(
        "dummy",
        "shortDummy",
        ScreenType.SMALL_MOLECULE,
        LibraryType.COMMERCIAL,
        1,
        1);
      genericEntityDao.saveOrUpdateEntity(library);
      librariesDao.loadOrCreateWellsForLibrary(library);
      Set<Well> wellSet = library.getWells();
      for (Well well : wellSet) {
        well.setMolfile("molfile for well " + well.getWellKey());
      }
    }
  }
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _wellListDAOTransaction = new WellListDAOTransaction();
    genericEntityDao.doInTransaction(_wellListDAOTransaction);
  }

  public void testExportWellsDataToSDF() throws IOException
  {
    WellsSdfDataExporter wellsDataExporter =
      new WellsSdfDataExporter(genericEntityDao);
    
    Set<String> wellKeys = new HashSet<String>();
    wellKeys.add("00001:A01");
    wellKeys.add("00001:A02");
    wellKeys.add("00001:A03");
    InputStream exportedData = wellsDataExporter.export(wellKeys);
    
    verifyExpectedWellsExported(wellKeys, exportedData);
  }

  public void testExportWellDataToSDF() throws IOException 
  {
    WellSdfDataExporter exporter = new WellSdfDataExporter(genericEntityDao);
    InputStream exportedData = exporter.export(genericEntityDao.findEntityById(Well.class, new WellKey("00001:A02").toString()));
    Set<String> expectedWellKeys = new HashSet<String>();
    expectedWellKeys.add("00001:A02");
    verifyExpectedWellsExported(expectedWellKeys, exportedData);
  }

  private void verifyExpectedWellsExported(Set<String> wellKeys,
                                           InputStream exportedData)
    throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(exportedData));
    String line;
    Pattern p = Pattern.compile("molfile for well (.+)");
    Set<String> actualWellKeys = new HashSet<String>();
    while ((line = reader.readLine()) != null) {
      Matcher m = p.matcher(line);
      if (m.matches()) {
        actualWellKeys.add(m.group(1));
      }
    }
    reader.close();
    assertEquals("exported well molfiles", wellKeys, actualWellKeys);
  }
}

