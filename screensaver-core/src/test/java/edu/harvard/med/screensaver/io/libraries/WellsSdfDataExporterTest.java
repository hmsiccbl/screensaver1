// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;


public class WellsSdfDataExporterTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(WellsSdfDataExporterTest.class);

  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected edu.harvard.med.screensaver.service.libraries.LibraryCreator libraryCreator;

  private WellListDAOTransaction _wellListDAOTransaction;

  class WellListDAOTransaction implements DAOTransaction
  {

    public void runTransaction()
    {
      Library library = dataFactory.newInstance(Library.class);
      library.setScreenType(ScreenType.SMALL_MOLECULE);
      library.setStartPlate(1);
      library.setEndPlate(1);
      libraryCreator.createWells(library);
      LibraryContentsVersion lcv1 = library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
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
      library.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) library.getLatestContentsVersion().getLoadingActivity().getPerformedBy(),
                                                                            new LocalDate(),
                                                                            AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
      
      LibraryContentsVersion lcv2 = library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
      for (Well well : wellSet) {
        well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor2", well.getWellKey().toString()), 
                                        "molfile " + lcv2.getVersionNumber() + " for well " + well.getWellKey(),
                                        "",
                                        "",
                                        null,
                                        null,
                                        null);
      }
      genericEntityDao.persistEntity(library);
    }
  }
  
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    _wellListDAOTransaction = new WellListDAOTransaction();
    genericEntityDao.doInTransaction(_wellListDAOTransaction);
  }

  public void testExportWellsDataToSDF() throws IOException
  {
    LibraryContentsVersionReference lcvRef = new LibraryContentsVersionReference();
    WellsSdfDataExporter wellsDataExporter = new WellsSdfDataExporter(genericEntityDao, lcvRef);
    
    Set<String> wellKeys = new HashSet<String>();
    wellKeys.add("00001:A01");
    wellKeys.add("00001:A02");
    wellKeys.add("00001:A03");

    LibraryContentsVersion lcv1 = genericEntityDao.findEntityByProperty(LibraryContentsVersion.class, "versionNumber", Integer.valueOf(1));
    LibraryContentsVersion lcv2 = genericEntityDao.findEntityByProperty(LibraryContentsVersion.class, "versionNumber", Integer.valueOf(2));

    InputStream exportedData = wellsDataExporter.export(wellKeys.iterator());
    verifyExpectedWellsExported(wellKeys, lcv1, exportedData);
    
    lcvRef.setValue(lcv1);
    exportedData = wellsDataExporter.export(wellKeys.iterator());
    verifyExpectedWellsExported(wellKeys, lcv1, exportedData);
    
    lcvRef.setValue(lcv2);
    exportedData = wellsDataExporter.export(wellKeys.iterator());
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

