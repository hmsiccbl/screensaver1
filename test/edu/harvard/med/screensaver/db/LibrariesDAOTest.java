// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.UnexpectedRollbackException;

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
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;


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
  protected LibraryCreator libraryCreator;
  
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
        Copy copyC = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        copyC.findPlate(1).withWellVolume(new Volume(10)).withStatus(PlateStatus.AVAILABLE);
        copyC.findPlate(2).withWellVolume(new Volume(100)).withStatus(PlateStatus.AVAILABLE); // should be ignored
        Copy copyD = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "D");
        copyD.findPlate(1).withWellVolume(new Volume(10)).withStatus(PlateStatus.AVAILABLE);
        copyD.findPlate(2).withWellVolume(new Volume(100)).withStatus(PlateStatus.AVAILABLE); // should be ignored
        Copy copyE = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "E");
        copyE.findPlate(1).withWellVolume(new Volume(10)).withStatus(PlateStatus.AVAILABLE);
        copyE.findPlate(2).withWellVolume(new Volume(100)).withStatus(PlateStatus.AVAILABLE); // should be ignored
        Copy copyF = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "F");
        copyF.findPlate(1).withWellVolume(new Volume(10)).withStatus(PlateStatus.AVAILABLE);
        copyF.findPlate(2).withWellVolume(new Volume(100)).withStatus(PlateStatus.AVAILABLE); // should be ignored
        Copy copyG = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "G"); // retired copies should be ignored
        copyG.findPlate(1).withWellVolume(new Volume(10)).withStatus(PlateStatus.RETIRED);
        Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A"); // library screening copies should be ignored
        copyA.findPlate(1).withWellVolume(new Volume(10)).withStatus(PlateStatus.AVAILABLE);

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

    Copy copyC = genericEntityDao.findEntityByProperty(Copy.class, "name", "C");
    Copy copyD = genericEntityDao.findEntityByProperty(Copy.class, "name", "D");
    Copy copyE = genericEntityDao.findEntityByProperty(Copy.class, "name", "E");
    Copy copyF = genericEntityDao.findEntityByProperty(Copy.class, "name", "F");
    Copy copyG = genericEntityDao.findEntityByProperty(Copy.class, "name", "G");
    Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
    Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
    Well wellC03 = genericEntityDao.findEntityById(Well.class, "00001:C03");

    assertEquals("C:A01", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellA01, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyC));
    assertEquals("C:B02", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellB02, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyC));
    assertEquals("C:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyC));
    assertEquals("D:A01", new Volume(9), librariesDao.findRemainingVolumesInWellCopies(wellA01, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyD));
    assertEquals("D:B02", new Volume(9), librariesDao.findRemainingVolumesInWellCopies(wellB02, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyD));
    assertEquals("D:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyD));
    assertEquals("E:A01", new Volume(8), librariesDao.findRemainingVolumesInWellCopies(wellA01, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyE));
    assertEquals("E:B02", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellB02, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyE));
    assertEquals("E:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyE));
    assertEquals("F:A01", new Volume(9), librariesDao.findRemainingVolumesInWellCopies(wellA01, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyF));
    assertEquals("F:B02", new Volume(7), librariesDao.findRemainingVolumesInWellCopies(wellB02, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyF));
    assertEquals("F:C03", new Volume(10), librariesDao.findRemainingVolumesInWellCopies(wellC03, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyF));
    // note: copy G is retired
    assertEquals("G:A01", null, librariesDao.findRemainingVolumesInWellCopies(wellA01, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyG));
    assertEquals("G:B02", null, librariesDao.findRemainingVolumesInWellCopies(wellB02, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyG));
    assertEquals("G:C03", null, librariesDao.findRemainingVolumesInWellCopies(wellC03, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).get(copyG));
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
    Library resultLibrary = genericEntityDao.findEntityById(Library.class, Integer.valueOf(library.getLibraryId()));
    ScreeningRoomUser resultOwner = resultLibrary.getOwner();
    resultOwner.equals(owner);
  }
  
  public void testFindPlate()
  {
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(102);
    final Copy copyC = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
    final Copy copyD = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "D");
    copyC.findPlate(100).withWellVolume(new Volume(0));
    copyC.findPlate(101).withWellVolume(new Volume(0));
    copyD.findPlate(100).withWellVolume(new Volume(0));
    copyD.findPlate(102).withWellVolume(new Volume(0));
    genericEntityDao.saveOrUpdateEntity(library);

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate plate = librariesDao.findPlate(100, copyC.getName());
        assertEquals(100, plate.getPlateNumber());
        assertEquals("C", plate.getCopy().getName());
        
        plate = librariesDao.findPlate(102, copyD.getName());
        assertEquals(102, plate.getPlateNumber());
        assertEquals("D", plate.getCopy().getName());
        
        assertNull(librariesDao.findPlate(103, copyD.getName()));
      }
    });
  }

  public void testScreenTypesForWellsAndReagents()
  {
    Library rnaiLib = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    Library smLib = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(rnaiLib);
    genericEntityDao.persistEntity(smLib);
    WellKey rnaiWellId = rnaiLib.getWells().iterator().next().getWellKey();
    WellKey smWellId = smLib.getWells().iterator().next().getWellKey();
    ReagentVendorIdentifier rnaiReagentId = rnaiLib.getWells().iterator().next().getReagents().values().iterator().next().getVendorId();
    ReagentVendorIdentifier smReagentId = smLib.getWells().iterator().next().getReagents().values().iterator().next().getVendorId();
    
    assertEquals(Sets.newHashSet(ScreenType.SMALL_MOLECULE, ScreenType.RNAI),
                 librariesDao.findScreenTypesForWells(Sets.newHashSet(rnaiWellId, smWellId)));
    assertEquals(Sets.newHashSet(ScreenType.SMALL_MOLECULE),
                 librariesDao.findScreenTypesForWells(Sets.newHashSet(smWellId)));
    assertEquals(Sets.newHashSet(ScreenType.RNAI),
                 librariesDao.findScreenTypesForWells(Sets.newHashSet(rnaiWellId)));

    assertEquals(Sets.newHashSet(ScreenType.SMALL_MOLECULE, ScreenType.RNAI),
                 librariesDao.findScreenTypesForReagents(Sets.newHashSet(rnaiReagentId.getVendorIdentifier(), smReagentId.getVendorIdentifier())));
    assertEquals(Sets.newHashSet(ScreenType.SMALL_MOLECULE),
                 librariesDao.findScreenTypesForReagents(Sets.newHashSet(smReagentId.getVendorIdentifier())));
    assertEquals(Sets.newHashSet(ScreenType.RNAI),
                 librariesDao.findScreenTypesForReagents(Sets.newHashSet(rnaiReagentId.getVendorIdentifier())));

  }

  public void testLibraryCopyDeleteIsScreened()
  {
    // this test should show that if plate has an associated AssayPlate, then the copy delete will fail when 
    // it cascades to the plate delete;
    // also, this will verify that the same delete will fail if there is a LabCherryPick associated
    // with the copy via the AssayPlate.
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library1 = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 2);
        Library library2 = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 2);
        Copy lib1Copy = library1.createCopy((AdministratorUser) library1.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Copy lib2Copy = library2.createCopy((AdministratorUser) library2.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
        Plate plate1000 = lib1Copy.findPlate(1000).withWellVolume(new Volume(0));
        Plate plate1001 = lib1Copy.findPlate(1001).withWellVolume(new Volume(0));
        genericEntityDao.saveOrUpdateEntity(library1);
        genericEntityDao.saveOrUpdateEntity(library2);
        genericEntityDao.flush();
        
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        ScreeningRoomUser user = new ScreeningRoomUser("Screener", "User");
        LibraryScreening libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        genericEntityDao.saveOrUpdateEntity(screen);
        
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        libraryScreening.addAssayPlatesScreened(plate1001);
        libraryScreening.update();
      }
    });

    Copy copy1A = genericEntityDao.findEntityByProperty(Copy.class, "name", "A");
    assertNotNull(copy1A);
    Copy copy2A = genericEntityDao.findEntityByProperty(Copy.class, "name", "B");
    assertNotNull(copy2A);

    boolean fail = false;
    try {
      genericEntityDao.deleteEntity(copy1A);
    }
    catch (DataIntegrityViolationException e) {
      fail = true;
      SQLException se = getRootException(e);
      if (se != null) {
        log.error("SQL Exception: ", se);
      }
      else {
        log.error("e: ", e);
      }
    }
    assertTrue("Copy delete should fail due to associated LibraryScreening", fail);

    // include a regular delete, to verify
    genericEntityDao.deleteEntity(copy2A);
  }

  public void testLibraryCopyDeleteHasWVA()
  {
    // TODO: for [#2527] delete Copy command:
    // The Copy viewer should allow the copy to be deleted, iff the copy has not been
    // used (screened, cherry picked, or has well volume adjustments).  Deletion
    // should cascade to all of its constituent Plates.
    schemaUtil.truncateTablesOrCreateSchema();

    // 1. create some WVA's
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        TestDataFactory dataFactory = new TestDataFactory();
        Library library = dataFactory.newInstance(Library.class);
        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Well well = dataFactory.newInstance(Well.class);
        Volume volume = dataFactory.getTestValueForType(Volume.class);
        WellVolumeCorrectionActivity wellVolumeCorrectionActivity = dataFactory.newInstance(WellVolumeCorrectionActivity.class);
        WellVolumeAdjustment wellVolumeAdjustment = wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy1, well, volume);
        new EntityNetworkPersister(genericEntityDao, wellVolumeAdjustment).persistEntityNetwork();

        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
        genericEntityDao.persistEntity(copy2);
      }
    });
    
    Copy copy1A = genericEntityDao.findEntityByProperty(Copy.class, "name", "A");
    assertNotNull(copy1A);
    Copy copy2A = genericEntityDao.findEntityByProperty(Copy.class, "name", "B");
    assertNotNull(copy2A);

    boolean fail = false;
    try {
      genericEntityDao.deleteEntity(copy1A);
    }
    catch (DataIntegrityViolationException e) {
      fail = true;
      SQLException se = getRootException(e);
      if (se != null) {
        log.error("SQL Exception: ", se);
      }
      else {
        log.error("e: ", e);
      }
    }
    assertTrue("Copy delete should fail due to Well Volume Adjustments", fail);

    // include a regular delete, to verify
    genericEntityDao.deleteEntity(copy2A);
  }

  private SQLException getRootException(DataIntegrityViolationException e)
  {
    if (e.getCause() instanceof ConstraintViolationException) {
      SQLException se = ((ConstraintViolationException) e.getCause()).getSQLException();
      while (se.getNextException() != null)
        se = se.getNextException();
      return se;
    }
    return null;
  }
}
