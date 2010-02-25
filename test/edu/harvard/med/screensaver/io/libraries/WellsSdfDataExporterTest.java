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
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;


public class WellsSdfDataExporterTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(WellsSdfDataExporterTest.class);

  protected LibrariesDAO librariesDao;

  private WellListDAOTransaction _wellListDAOTransaction;

  class WellListDAOTransaction implements DAOTransaction
  {

    public void runTransaction()
    {
      TestDataFactory dataFactory = new TestDataFactory();
      Library library = new Library(
        "dummy",
        "shortDummy",
        ScreenType.SMALL_MOLECULE,
        LibraryType.COMMERCIAL,
        1,
        1);
      librariesDao.loadOrCreateWellsForLibrary(library);
      LibraryContentsVersion lcv1 = dataFactory.newInstance(LibraryContentsVersion.class, library);
      Set<Well> wellSet = library.getWells();
      for (Well well : wellSet) {
        well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", well.getWellKey().toString()), 
                                        "molfile " + lcv1.getVersionNumber() + " for well " + well.getWellKey(),
                                        "",
                                        "",
                                        null,
                                        null,
                                        null);
      }
      library.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) library.getLatestContentsVersion().getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
      
      LibraryContentsVersion lcv2 = dataFactory.newInstance(LibraryContentsVersion.class, library);
      for (Well well : wellSet) {
        well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor2", well.getWellKey().toString()), 
                                        "molfile " + lcv2.getVersionNumber() + " for well " + well.getWellKey(),
                                        "",
                                        "",
                                        null,
                                        null,
                                        null);
        genericEntityDao.saveOrUpdateEntity(library);
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
    wellsDataExporter.setLibraryContentsVersion(null);
    
    Set<String> wellKeys = new HashSet<String>();
    wellKeys.add("00001:A01");
    wellKeys.add("00001:A02");
    wellKeys.add("00001:A03");

    LibraryContentsVersion lcv1 = genericEntityDao.findEntityByProperty(LibraryContentsVersion.class, "versionNumber", Integer.valueOf(1));
    LibraryContentsVersion lcv2 = genericEntityDao.findEntityByProperty(LibraryContentsVersion.class, "versionNumber", Integer.valueOf(2));

    InputStream exportedData = wellsDataExporter.export(wellKeys);
    verifyExpectedWellsExported(wellKeys, lcv1, exportedData);
    
    wellsDataExporter.setLibraryContentsVersion(lcv1);
    exportedData = wellsDataExporter.export(wellKeys);
    verifyExpectedWellsExported(wellKeys, lcv1, exportedData);
    
    wellsDataExporter.setLibraryContentsVersion(lcv2);
    exportedData = wellsDataExporter.export(wellKeys);
    verifyExpectedWellsExported(wellKeys, lcv2, exportedData);
  }

  private void verifyExpectedWellsExported(Set<String> wellKeys,
                                           LibraryContentsVersion lcv,
                                           InputStream exportedData)
    throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(exportedData));
    String line;
    Pattern p = Pattern.compile("molfile (.+) for well (.+)");
    Set<String> actualVersionNumbers = new HashSet<String>();
    Set<String> actualWellKeys = new HashSet<String>();
    while ((line = reader.readLine()) != null) {
      Matcher m = p.matcher(line);
      if (m.matches()) {
        actualVersionNumbers.add(m.group(1));
        actualWellKeys.add(m.group(2));
      }
    }
    reader.close();
    assertEquals("library contents version numbers", Sets.newHashSet(lcv.getVersionNumber().toString()), actualVersionNumbers); 
    assertEquals("exported well molfiles", wellKeys, actualWellKeys);
  }
}

