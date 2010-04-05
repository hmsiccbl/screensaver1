// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 2359 2008-05-09 21:16:57Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.EntityNetworkPersister;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.UnexpectedRollbackException;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link GenericEntityDAOTest}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrariesDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(LibrariesDAOTest.class);

  protected LibrariesDAO librariesDao;
  
  public void testFindReagent()
  {
    TestDataFactory dataFactory = new TestDataFactory();
    SilencingReagent silencingReagent = dataFactory.newInstance(SilencingReagent.class);
    new EntityNetworkPersister(genericEntityDao, silencingReagent).persistEntityNetwork();
    
    Set<Reagent> reagents = librariesDao.findReagents(silencingReagent.getVendorId(), false);
    assertEquals(1, reagents.size());
    assertEquals(silencingReagent.getVendorId(), reagents.iterator().next().getVendorId());
    assertTrue(reagents.iterator().next() instanceof SilencingReagent);
    
    reagents = librariesDao.findReagents(silencingReagent.getVendorId(), true);
    assertEquals(0, reagents.size());
   
    Library library = silencingReagent.getWell().getLibrary();
    LibraryContentsVersion lcv = library.getContentsVersions().first();
    lcv.release(new AdministrativeActivity((AdministratorUser) lcv.getLoadingActivity().getPerformedBy(),
                                           new LocalDate(),
                                           AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
    LibraryContentsVersion lcv2 = library.createContentsVersion(new AdministrativeActivity((AdministratorUser) lcv.getLoadingActivity().getPerformedBy(),
                                                                                           new LocalDate(),
                                                                                           AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
    silencingReagent.getWell().createSilencingReagent(silencingReagent.getVendorId(), SilencingReagentType.SIRNA, "AAAC");
    genericEntityDao.saveOrUpdateEntity(lcv2.getLibrary());
    reagents = librariesDao.findReagents(silencingReagent.getVendorId(), true);    
    assertEquals(1, reagents.size());
  }

  public void testRemainingWellVolume()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("library", 1, 2, PlateSize.WELLS_384);
        Copy copyC = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copyC.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyC.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyD = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        copyD.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyD.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyE = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        copyE.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyE.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyF = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "F");
        copyF.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyF.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyG = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "G");
        copyG.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10)).setDateRetired(new LocalDate());

        genericEntityDao.saveOrUpdateEntity(library);

        WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
          new WellVolumeCorrectionActivity(new AdministratorUser("Joe", "Admin", "joe_admin@hms.harvard.edu", "", "", "", "", ""),
                                           new LocalDate());
        Set<WellVolumeAdjustment> wellVolumeAdjustments = wellVolumeCorrectionActivity.getWellVolumeAdjustments();
        Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
        Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
        /*Well wellC03 =*/ genericEntityDao.findEntityById(Well.class, "00001:C03");
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyD, wellA01, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyF, wellA01, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyD, wellB02, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copyF, wellB02, new Volume(-1)));
        genericEntityDao.saveOrUpdateEntity(wellVolumeCorrectionActivity);

        RNAiCherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(2));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(wellA01);
        LabCherryPick labCherryPick1 = dummyScreenerCherryPick.createLabCherryPick(wellA01);
        labCherryPick1.setAllocated(copyE);
        LabCherryPick labCherryPick2 = dummyScreenerCherryPick.createLabCherryPick(wellB02);
        labCherryPick2.setAllocated(copyF);
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
      }
    });

    Copy copyC = genericEntityDao.findEntityById(Copy.class, "library:C");
    Copy copyD = genericEntityDao.findEntityById(Copy.class, "library:D");
    Copy copyE = genericEntityDao.findEntityById(Copy.class, "library:E");
    Copy copyF = genericEntityDao.findEntityById(Copy.class, "library:F");
    Copy copyG = genericEntityDao.findEntityById(Copy.class, "library:G");
    Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
    Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
    Well wellC03 = genericEntityDao.findEntityById(Well.class, "00001:C03");

    assertEquals("C:A01", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyC));
    assertEquals("C:B02", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyC));
    assertEquals("C:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyC));
    assertEquals("D:A01", new Volume(9),  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyD));
    assertEquals("D:B02", new Volume(9),  librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyD));
    assertEquals("D:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyD));
    assertEquals("E:A01", new Volume(8),  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyE));
    assertEquals("E:B02", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyE));
    assertEquals("E:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyE));
    assertEquals("F:A01", new Volume(9),  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyF));
    assertEquals("F:B02", new Volume(7),  librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyF));
    assertEquals("F:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyF));
    // note: copy G is retired
    assertEquals("G:A01", null,  librariesDao.findRemainingVolumesInWellCopies(wellA01).get(copyG));
    assertEquals("G:B02", null,  librariesDao.findRemainingVolumesInWellCopies(wellB02).get(copyG));
    assertEquals("G:C03", null,  librariesDao.findRemainingVolumesInWellCopies(wellC03).get(copyG));
  }
  
  public void testDeleteLibraryContentsVersion()
  {
    final TestDataFactory dataFactory = new TestDataFactory();
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = dataFactory.newInstance(Library.class);
        library.setScreenType(ScreenType.RNAI);
        librariesDao.loadOrCreateWellsForLibrary(library);
        /*LibraryContentsVersion lcv1 = */dataFactory.newInstance(LibraryContentsVersion.class, library);
        for (Well well : library.getWells()) {
          if (well.getWellKey().getColumn() == 0) {
            well.setLibraryWellType(LibraryWellType.EMPTY);
          }
          else {
            well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
            int x = well.getWellKey().hashCode();
            SilencingReagent reagent = well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "rvi" + x),
                                                                   SilencingReagentType.SIRNA,
                                                                   "ACTG");
            reagent.getFacilityGene().withEntrezgeneId(x).withGeneName("gene" + x).withGenbankAccessionNumber("gb" + x).withSpeciesName("species");
            reagent.getVendorGene().withEntrezgeneId(x).withGeneName("gene" + x).withGenbankAccessionNumber("gb" + x).withSpeciesName("species");
          }
        }

        /*LibraryContentsVersion lcv2 = */dataFactory.newInstance(LibraryContentsVersion.class, library);
        for (Well well : library.getWells()) {
          if (well.getWellKey().getColumn() == 0) {
            well.setLibraryWellType(LibraryWellType.EMPTY);
          }
          else {
            well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
            int x = well.getWellKey().hashCode() * 17;
            SilencingReagent reagent = well.createSilencingReagent(new ReagentVendorIdentifier("vendor", "rvi" + x),
                                                                   SilencingReagentType.SIRNA,
                                                                   "ACTG");
            reagent.getFacilityGene().withEntrezgeneId(x).withGeneName("gene" + x).withGenbankAccessionNumber("gb" + x).withSpeciesName("species");
            reagent.getVendorGene().withEntrezgeneId(x).withGeneName("gene" + x).withGenbankAccessionNumber("gb" + x).withSpeciesName("species");
          }
        }
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findAllEntitiesOfType(Library.class).get(0);
        LibraryContentsVersion lcv2 = library.getLatestContentsVersion();
        assertEquals(Integer.valueOf(2), lcv2.getVersionNumber());
        librariesDao.deleteLibraryContentsVersion(lcv2);
        // test in-memory representation updated properly
        assertEquals(1, library.getContentsVersions().size());
        LibraryContentsVersion lcv1 = library.getLatestContentsVersion();
        assertEquals(1, lcv1.getVersionNumber().intValue());
        for (Well well : library.getWells()) {
          if (well.getColumn() != 0) {
            assertEquals(1, well.getReagents().size());
            assertNull(well.getReagents().get(lcv2));
            assertNotNull(well.getReagents().get(lcv1));
          }
        }
      }
    });
    // test in-database representation updated properly
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findAllEntitiesOfType(Library.class).get(0);
        assertEquals(1, library.getContentsVersions().size());
        LibraryContentsVersion lcv1 = library.getLatestContentsVersion();
        assertEquals(1, lcv1.getVersionNumber().intValue());
        for (Well well : library.getWells()) {
          if (well.getColumn() != 0) {
            assertEquals(1, well.getReagents().size());
            assertNotNull(well.getReagents().get(lcv1));
          }
        }
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findAllEntitiesOfType(Library.class).get(0);
        LibraryContentsVersion lcv1 = library.getLatestContentsVersion();
        assertEquals(Integer.valueOf(1), lcv1.getVersionNumber());
        librariesDao.deleteLibraryContentsVersion(lcv1);
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findAllEntitiesOfType(Library.class).get(0);
        assertEquals(0, library.getContentsVersions().size());
        assertNull(library.getLatestContentsVersion());
        for (Well well : library.getWells()) {
          assertEquals(0, well.getReagents().size());
          assertEquals(LibraryWellType.UNDEFINED, well.getLibraryWellType());
        }
      }
    });

    // sanity check
    assertTrue(genericEntityDao.findAllEntitiesOfType(LibraryContentsVersion.class).isEmpty());
    assertTrue(genericEntityDao.findAllEntitiesOfType(Reagent.class).isEmpty());
    
    // verify that released contents versions cannot be deleted
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = genericEntityDao.findAllEntitiesOfType(Library.class).get(0);
        LibraryContentsVersion lcv1 = dataFactory.newInstance(LibraryContentsVersion.class, library);
        AdministrativeActivity releaseAdminActivity = new AdministrativeActivity((AdministratorUser) lcv1.getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE);
        lcv1.release(releaseAdminActivity);
      }
    });
    try {
      genericEntityDao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          Library library = genericEntityDao.findAllEntitiesOfType(Library.class).get(0);
          LibraryContentsVersion releasedContentsVersion = library.getLatestReleasedContentsVersion();
          assertNotNull(releasedContentsVersion);
          assertTrue(releasedContentsVersion.isReleased());
          try {
            librariesDao.deleteLibraryContentsVersion(releasedContentsVersion);
            fail("expecting BusinessRuleViolationException when deleting released library contents version");
          }
          catch (Exception e) {}
        }
      });
    }
    catch (UnexpectedRollbackException e) { /* why are we getting this? */}
  }
  
  public void testCountExperimentalWells()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 3);
    genericEntityDao.persistEntity(library);
    
    assertEquals(384 * 3, librariesDao.countExperimentalWells(1000, 1003));
    assertEquals(384, librariesDao.countExperimentalWells(1002, 1002));
  }

  public void testCreateRNaiLibraryContentsInclOwner()
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 2);
    ScreeningRoomUser owner = new ScreeningRoomUser("A", "B");
    genericEntityDao.saveOrUpdateEntity(owner);
       
    library.setOwner(owner);

    genericEntityDao.saveOrUpdateEntity(library);
    Library resultLibrary = genericEntityDao.findEntityById(Library.class,new Integer(library.getLibraryId()));
    ScreeningRoomUser resultOwner = resultLibrary.getOwner();
    resultOwner.equals(owner);
  }
}
