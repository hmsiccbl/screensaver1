// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;

import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

public class SmallMoleculeReagentTest extends AbstractEntityInstanceTest<SmallMoleculeReagent>
{
  protected LibraryContentsVersionManager libraryContentsVersionManager;

  public static TestSuite suite()
  {
    return buildTestSuite(SmallMoleculeReagentTest.class, SmallMoleculeReagent.class);
  }

  public SmallMoleculeReagentTest() throws IntrospectionException
  {
    super(SmallMoleculeReagent.class);
  }
  
  public void testCollections()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    SmallMoleculeReagent reagent = dataFactory.newInstance(SmallMoleculeReagent.class);
    reagent.getCompoundNames().add("compound1");
    reagent.getCompoundNames().add("compound2");
    reagent.getPubchemCids().add(1);
    reagent.getPubchemCids().add(2);
    reagent.getChembankIds().add(10);
    reagent.getChembankIds().add(11);
    persistEntityNetwork(reagent);
    
    Reagent reagent2 = genericEntityDao.findEntityById(SmallMoleculeReagent.class, reagent.getReagentId());
    assertNotNull(reagent2);
    assertEquals(Sets.newHashSet("compound1", "compound2"), reagent.getCompoundNames());
    assertEquals(Sets.newHashSet(1, 2), reagent.getPubchemCids());
    assertEquals(Sets.newHashSet(10, 11), reagent.getChembankIds());
  }

  /**
   * Test the special-case molfile property, which is implemented as a set of
   * strings (with size 0 or 1).
   */
  public void testMolfile()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    
    final AdministratorUser releaseAdmin = dataFactory.newInstance(AdministratorUser.class);
    releaseAdmin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(releaseAdmin);
    
    Library library = dataFactory.newInstance(Library.class);
    library.setLibraryName("library");
    library.setScreenType(ScreenType.SMALL_MOLECULE);
    LibraryContentsVersion contentsVersion = library.createContentsVersion(new AdministrativeActivity(releaseAdmin, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
    for (int i = 0; i < 3; ++i) {
      Well well = library.createWell(new WellKey(1, 0, i), LibraryWellType.EXPERIMENTAL);
      well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", Integer.toString(i + 1)),
                                      "molfile" + (i + 1), 
                                      "", "", null, null, null);
    }
    contentsVersion.release(new AdministrativeActivity(releaseAdmin, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
    genericEntityDao.saveOrUpdateEntity(library);

    class WellPocketDAOTransaction implements DAOTransaction
    {
      Well wellWithMolfileLoadedEagerly;
      Well wellWithMolfileNotLoaded;
      public void runTransaction()
      {
        // test that we can load the molfile on demand, within session
        Well well = genericEntityDao.findEntityById(Well.class, "00001:A01");
        assertEquals("molfile1", ((SmallMoleculeReagent) well.getLatestReleasedReagent()).getMolfile());

        // set up to test that we can eager load the molfile via our dao methods, and access after session is closed
        wellWithMolfileLoadedEagerly = genericEntityDao.findEntityById(Well.class, "00001:A02");
        genericEntityDao.need(wellWithMolfileLoadedEagerly, 
                              Well.latestReleasedReagent.to(SmallMoleculeReagent.molfileList).getPath());

        // set up to test that a molfile not loaded is not accessible after session is closed
        wellWithMolfileNotLoaded = genericEntityDao.findEntityById(Well.class, "00001:A03");
      }
    }
    WellPocketDAOTransaction wellPocketDAOTransaction = new WellPocketDAOTransaction();
    genericEntityDao.doInTransaction(wellPocketDAOTransaction);

    assertEquals("molfile2",
                 wellPocketDAOTransaction.wellWithMolfileLoadedEagerly.<SmallMoleculeReagent>getLatestReleasedReagent().getMolfile());

    // this should cause an error because well.molfile should be lazily loaded
    try {
      wellPocketDAOTransaction.wellWithMolfileNotLoaded.<SmallMoleculeReagent>getLatestReleasedReagent().getMolfile();
      fail("failed to get a LazyInitException for well.molfileList");
    }
    catch (Exception e) {
    }
  }
}

