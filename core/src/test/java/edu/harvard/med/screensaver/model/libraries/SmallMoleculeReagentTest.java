// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.TestSuite;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.test.TestDataFactory.PostCreateHook;

public class SmallMoleculeReagentTest extends AbstractEntityInstanceTest<SmallMoleculeReagent>
{
  @Autowired
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  @Autowired
  protected LibrariesDAO librariesDao;

  public static TestSuite suite()
  {
    return buildTestSuite(SmallMoleculeReagentTest.class, SmallMoleculeReagent.class);
  }

  public SmallMoleculeReagentTest()
  {
    super(SmallMoleculeReagent.class);
  }
  
  public void testCollections()
  {
    schemaUtil.truncateTables();
    SmallMoleculeReagent reagent = dataFactory.newInstance(SmallMoleculeReagent.class);
    reagent.getCompoundNames().add("compound1");
    reagent.getCompoundNames().add("compound2");
    reagent.getPubchemCids().add(1);
    reagent.getPubchemCids().add(2);
    reagent.getChembankIds().add(10);
    reagent.getChembankIds().add(11);
    reagent = genericEntityDao.mergeEntity(reagent);
    
    Reagent reagent2 = genericEntityDao.findEntityById(SmallMoleculeReagent.class, reagent.getReagentId());
    assertNotNull(reagent2);
    assertEquals(Lists.newArrayList("compound1", "compound2"), reagent.getCompoundNames());
    assertEquals("compound1", reagent.getPrimaryCompoundName());
    assertEquals(Sets.newHashSet(1, 2), reagent.getPubchemCids());
    assertEquals(Sets.newHashSet(10, 11), reagent.getChembankIds());
  }

  /**
   * Test the special-case molfile property, which is implemented as a set of
   * strings (with size 0 or 1).
   */
  public void testMolfile()
  {
    schemaUtil.truncateTables();
    
    final AdministratorUser releaseAdmin = dataFactory.newInstance(AdministratorUser.class);
    releaseAdmin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(releaseAdmin);
    
    final Library library = dataFactory.newInstance(Library.class);
    library.setLibraryName("library");
    library.setScreenType(ScreenType.SMALL_MOLECULE);
    LibraryContentsVersion contentsVersion = library.createContentsVersion(releaseAdmin);
    for (int i = 0; i < 3; ++i) {
      Well well = library.createWell(new WellKey(library.getStartPlate(), 0, i), LibraryWellType.EXPERIMENTAL);
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
        Well well = librariesDao.findWell(new WellKey(library.getStartPlate(), 0, 0));
        assertEquals("molfile1", ((SmallMoleculeReagent) well.getLatestReleasedReagent()).getMolfile());

        // set up to test that we can eager load the molfile via our dao methods, and access after session is closed
        wellWithMolfileLoadedEagerly = librariesDao.findWell(new WellKey(library.getStartPlate(), 0, 1));
        genericEntityDao.need(wellWithMolfileLoadedEagerly, 
                              Well.latestReleasedReagent.to(SmallMoleculeReagent.molfileList));

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

  public void testOptionalImmutableProperties()
  {
    dataFactory.addPostCreateHook(SmallMoleculeReagent.class, new PostCreateHook<SmallMoleculeReagent>() {
      @Override
      public void postCreate(String callStack, SmallMoleculeReagent smr)
      {
        if (callStack.endsWith(getName())) {
            //          smr.forFacilityBatchId(1).forSaltFormId(2).forVendorBatchId("batchId");
          smr.forSaltFormId(2).forFacilityBatchId(1).forVendorBatchId("batchId");
        }
      }
    });
    SmallMoleculeReagent smallMoleculeReagent = dataFactory.newInstance(SmallMoleculeReagent.class, getName());
    assertEquals(Integer.valueOf(1), smallMoleculeReagent.getFacilityBatchId());
    assertEquals(Integer.valueOf(2), smallMoleculeReagent.getSaltFormId());
    assertEquals("batchId", smallMoleculeReagent.getVendorBatchId());
  }
}

