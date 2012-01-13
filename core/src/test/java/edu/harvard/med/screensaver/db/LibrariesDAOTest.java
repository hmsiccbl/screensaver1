// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.service.libraries.LibraryScreeningDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.test.TestDataFactory.PostCreateHook;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link GenericEntityDAOTest}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrariesDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(LibrariesDAOTest.class);

  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected LibraryCreator libraryCreator;
  @Autowired
  protected LibraryScreeningDerivedPropertiesUpdater libraryScreeningDerivedPropertiesUpdater;
  
  public void testFindReagent()
  {
    SilencingReagent silencingReagent = dataFactory.newInstance(SilencingReagent.class);
    
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
    library.createContentsVersion((AdministratorUser) lcv.getLoadingActivity().getPerformedBy());
    silencingReagent.getWell().createSilencingReagent(silencingReagent.getVendorId(), SilencingReagentType.SIRNA, "AAAC");
    genericEntityDao.mergeEntity(library);
    reagents = librariesDao.findReagents(silencingReagent.getVendorId(), true);    
    assertEquals(1, reagents.size());
  }

  public void testRemainingWellVolume()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 2, PlateSize.WELLS_384);
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
          new WellVolumeCorrectionActivity(new AdministratorUser("Joe", "Admin"),
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

        RNAiCherryPickRequest cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(1, new Volume(2));
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
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = dataFactory.newInstance(Library.class);
        library.setScreenType(ScreenType.RNAI);
        libraryCreator.createWells(library);
        library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));

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

        library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));

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
        LibraryContentsVersion lcv1 = library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
        AdministrativeActivity releaseAdminActivity = new AdministrativeActivity((AdministratorUser) lcv1.getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE);
        lcv1.release(releaseAdminActivity);
      }
    });
    Library library = genericEntityDao.findAllEntitiesOfType(Library.class, true, Library.latestReleasedContentsVersion).get(0);
    LibraryContentsVersion releasedContentsVersion = library.getLatestReleasedContentsVersion();
    assertNotNull(releasedContentsVersion);
    assertTrue(releasedContentsVersion.isReleased());
    try {
      librariesDao.deleteLibraryContentsVersion(releasedContentsVersion);
      fail("expecting BusinessRuleViolationException when deleting released library contents version");
    }
    catch (BusinessRuleViolationException e) {}
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

  /**
   * Test that if plate has an associated AssayPlate, then the copy delete will fail when
   * it cascades to the plate delete;
   * also, this will verify that the same delete will fail if there is a LabCherryPick associated
   * with the copy via the AssayPlate.
   */
  public void testLibraryCopyDeleteIsScreened()
  {
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
        genericEntityDao.persistEntity(library1);
        genericEntityDao.persistEntity(library2);
        genericEntityDao.flush();
        
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        AdministratorUser admin = new AdministratorUser("Admin", "User");
        ScreeningRoomUser user = new ScreeningRoomUser("Screener", "User");
        LibraryScreening libraryScreening = screen.createLibraryScreening(admin, user, new LocalDate());
        
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        libraryScreening.addAssayPlatesScreened(plate1001);

        genericEntityDao.persistEntity(screen);
        genericEntityDao.flush();

        libraryScreeningDerivedPropertiesUpdater.updateScreeningStatistics(libraryScreening);
      }
    });

    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Copy copy1A = genericEntityDao.findEntityByProperty(Copy.class, "name", "A");
          assertNotNull(copy1A);
          genericEntityDao.deleteEntity(copy1A);
        }
      });
      fail("Copy delete should fail due to Well Volume Adjustments");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    assertNotNull(genericEntityDao.findEntityByProperty(Copy.class, "name", "A"));

    // verify that delete works when copy has no associated entities
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Copy copy2A = genericEntityDao.findEntityByProperty(Copy.class, "name", "B");
        assertNotNull(copy2A);
        genericEntityDao.deleteEntity(copy2A);
      }
    });
    assertNull(genericEntityDao.findEntityByProperty(Copy.class, "name", "B"));
  }

  public void testLibraryCopyDeleteHasWVA()
  {
    // TODO: for [#2527] delete Copy command:
    // The Copy viewer should allow the copy to be deleted, iff the copy has not been
    // used (screened, cherry picked, or has well volume adjustments).  Deletion
    // should cascade to all of its constituent Plates.
    schemaUtil.truncateTables();

    // 1. create some WVA's
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = dataFactory.newInstance(Library.class);
        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Well well = dataFactory.newInstance(Well.class);
        Volume volume = dataFactory.newInstance(Volume.class);
        WellVolumeCorrectionActivity wellVolumeCorrectionActivity = dataFactory.newInstance(WellVolumeCorrectionActivity.class);
        WellVolumeAdjustment wellVolumeAdjustment = wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy1, well, volume);
        genericEntityDao.persistEntity(wellVolumeAdjustment);

        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
        genericEntityDao.persistEntity(copy2);
      }
    });
    
    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Copy copy1A = genericEntityDao.findEntityByProperty(Copy.class, "name", "A");
          assertNotNull(copy1A);
          genericEntityDao.deleteEntity(copy1A);
        }
      });
      fail("Copy delete should fail due to Well Volume Adjustments");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    assertNotNull(genericEntityDao.findEntityByProperty(Copy.class, "name", "A"));

    // verify that delete works when copy has no associated entities
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Copy copy2A = genericEntityDao.findEntityByProperty(Copy.class, "name", "B");
        assertNotNull(copy2A);
        genericEntityDao.deleteEntity(copy2A);
      }
    });
    assertNull(genericEntityDao.findEntityByProperty(Copy.class, "name", "B"));
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

  public void testCalculatePlateVolumeStatistics()
  {
    dataFactory.addPostCreateHook(Plate.class, new PostCreateHook<Plate>() {
      @Override
      public void postCreate(String callStack, Plate p)
      {
        if (callStack.endsWith(getName())) {
          p.setWellVolume(new Volume(10, VolumeUnit.MICROLITERS));
        }
      } 
    });
    dataFactory.addPostCreateHook(LibraryScreening.class, new PostCreateHook<LibraryScreening>() {
      @Override
      public void postCreate(String callStack, LibraryScreening ls) {
        if (callStack.endsWith(getName())) {
          ls.setVolumeTransferredPerWellFromLibraryPlates(new Volume("3.0", VolumeUnit.MICROLITERS));
          ls.setVolumeTransferredPerWellToAssayPlates(new Volume("1.5", VolumeUnit.MICROLITERS));
          ls.setNumberOfReplicates(2);
        }
      } 
    });
    Plate plate1 = dataFactory.newInstance(Plate.class, getName());
    Plate plate2 = dataFactory.newInstance(Plate.class, getName());
    Plate plate3 = dataFactory.newInstance(Plate.class, getName());
    Plate plate4 = dataFactory.newInstance(Plate.class, getName());
    LibraryScreening libScreening1 = dataFactory.newInstance(LibraryScreening.class, getName());
    LibraryScreening libScreening2 = dataFactory.newInstance(LibraryScreening.class, getName());
    LibraryScreening libScreening3 = dataFactory.newInstance(LibraryScreening.class, getName());
    libScreening1.addAssayPlatesScreened(plate1);
    libScreening1.addAssayPlatesScreened(plate2);
    libScreening1.addAssayPlatesScreened(plate3);
    libScreening2.addAssayPlatesScreened(plate1);
    libScreening2.addAssayPlatesScreened(plate2);
    libScreening3.addAssayPlatesScreened(plate1);
    libScreening1 = genericEntityDao.mergeEntity(libScreening1);
    libScreening2 = genericEntityDao.mergeEntity(libScreening2);
    libScreening3 = genericEntityDao.mergeEntity(libScreening3);

    Set<Plate> plates = Sets.newHashSet(plate1, plate2, plate3, plate4);
    librariesDao.calculatePlateVolumeStatistics(plates);
    assertEquals(new Volume(1, VolumeUnit.MICROLITERS), plate1.getVolumeStatistics().getAverageRemaining());
    assertEquals(new Volume(4, VolumeUnit.MICROLITERS), plate2.getVolumeStatistics().getAverageRemaining());
    assertEquals(new Volume(7, VolumeUnit.MICROLITERS), plate3.getVolumeStatistics().getAverageRemaining());
    assertEquals(new Volume(10, VolumeUnit.MICROLITERS), plate4.getVolumeStatistics().getAverageRemaining());
  }
  
  public void testCalculateCopyVolumeStatistics()
  {
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(1);
    library.setEndPlate(3);
    Copy copy1 = library.createCopy(dataFactory.newInstance(AdministratorUser.class), CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
    Copy copy2 = library.createCopy(dataFactory.newInstance(AdministratorUser.class), CopyUsageType.LIBRARY_SCREENING_PLATES, "D");
    Copy copy3 = library.createCopy(dataFactory.newInstance(AdministratorUser.class), CopyUsageType.LIBRARY_SCREENING_PLATES, "E");
    for (Plate p : copy1.getPlates().values()) {
      p.setWellVolume(new Volume(10, VolumeUnit.MICROLITERS));
    }
    for (Plate p : copy2.getPlates().values()) {
      p.setWellVolume(new Volume(10, VolumeUnit.MICROLITERS));
    }
    for (Plate p : copy3.getPlates().values()) {
      p.setWellVolume(new Volume(10, VolumeUnit.MICROLITERS));
    }
    library = genericEntityDao.mergeEntity(library);
    copy1 = library.getCopy("C");
    copy2 = library.getCopy("D");
    copy3 = library.getCopy("E");

    LibraryScreening libScreening1 = newLibraryScreening(new Volume("2.0", VolumeUnit.MICROLITERS));
    LibraryScreening libScreening2 = newLibraryScreening(new Volume("3.0", VolumeUnit.MICROLITERS));
    LibraryScreening libScreening3 = newLibraryScreening(new Volume("4.0", VolumeUnit.MICROLITERS));
    LibraryScreening libScreening4 = newLibraryScreening(new Volume("5.0", VolumeUnit.MICROLITERS));
    LibraryScreening libScreening5 = newLibraryScreening(new Volume("4.0", VolumeUnit.MICROLITERS));
    LibraryScreening libScreening6 = newLibraryScreening(new Volume("4.0", VolumeUnit.MICROLITERS));
    
    libScreening1.addAssayPlatesScreened(copy1.findPlate(1));
    libScreening1.addAssayPlatesScreened(copy1.findPlate(2));
    libScreening1.addAssayPlatesScreened(copy1.findPlate(3));
    libScreening2.addAssayPlatesScreened(copy1.findPlate(1));
    libScreening2.addAssayPlatesScreened(copy1.findPlate(2));
    libScreening2.addAssayPlatesScreened(copy1.findPlate(3));

    libScreening3.addAssayPlatesScreened(copy2.findPlate(1));
    libScreening3.addAssayPlatesScreened(copy2.findPlate(2));
    libScreening3.addAssayPlatesScreened(copy2.findPlate(3));
    libScreening4.addAssayPlatesScreened(copy2.findPlate(1));
    libScreening4.addAssayPlatesScreened(copy2.findPlate(2));
    libScreening4.addAssayPlatesScreened(copy2.findPlate(3));

    libScreening5.addAssayPlatesScreened(copy3.findPlate(1));
    libScreening5.addAssayPlatesScreened(copy3.findPlate(2));
    libScreening5.addAssayPlatesScreened(copy3.findPlate(3));
    libScreening6.addAssayPlatesScreened(copy3.findPlate(1)); // subset of copy's plates screened 

    genericEntityDao.mergeEntity(libScreening1);
    genericEntityDao.mergeEntity(libScreening2);
    genericEntityDao.mergeEntity(libScreening3);
    genericEntityDao.mergeEntity(libScreening4);
    genericEntityDao.mergeEntity(libScreening5);
    genericEntityDao.mergeEntity(libScreening6);

    Set<Copy> copies = Sets.newHashSet(copy1, copy2, copy3);
    librariesDao.calculateCopyVolumeStatistics(copies);
    assertEquals(new Volume(5, VolumeUnit.MICROLITERS), copy1.getVolumeStatistics().getAverageRemaining());
    assertEquals(new Volume(5, VolumeUnit.MICROLITERS), copy1.getVolumeStatistics().getMinRemaining());
    assertEquals(new Volume(5, VolumeUnit.MICROLITERS), copy1.getVolumeStatistics().getMaxRemaining());
    assertEquals(new Volume(1, VolumeUnit.MICROLITERS), copy2.getVolumeStatistics().getAverageRemaining());
    assertEquals(new Volume(1, VolumeUnit.MICROLITERS), copy2.getVolumeStatistics().getMinRemaining());
    assertEquals(new Volume(1, VolumeUnit.MICROLITERS), copy2.getVolumeStatistics().getMaxRemaining());
    assertEquals(new Volume("4.667", VolumeUnit.MICROLITERS), copy3.getVolumeStatistics().getAverageRemaining());
    assertEquals(new Volume(2, VolumeUnit.MICROLITERS), copy3.getVolumeStatistics().getMinRemaining());
    assertEquals(new Volume(6, VolumeUnit.MICROLITERS), copy3.getVolumeStatistics().getMaxRemaining());
  }

  public void testCalculateCopyVolumeStatisticsEmptyCopies()
  {
    librariesDao.calculateCopyVolumeStatistics(ImmutableSet.<Copy>of());
  }

  private LibraryScreening newLibraryScreening(Volume volume)
  {
    LibraryScreening ls = dataFactory.newInstance(LibraryScreening.class, getName());
    ls.setVolumeTransferredPerWellFromLibraryPlates(volume);
    ls.setVolumeTransferredPerWellToAssayPlates(volume);
    ls.setNumberOfReplicates(1);
    return ls;
  }
    /**
   * NOTE: this is a LINCS-only feature
   */
  public void testFindWellsForCompoundName()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        AdministratorUser adminUser = new AdministratorUser("Admin", "User");
        Library library = new Library(adminUser, "Small Molecule Library", "smLib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 2, PlateSize.WELLS_384);
        library.createContentsVersion(adminUser);
        PlateSize plateSize = library.getPlateSize();

        int plate = 1, row = 0, col = 0;
        Well well = library.createWell(new WellKey(plate, row, col),
                                       LibraryWellType.UNDEFINED);

         /* SmallMoleculeReagent reagent = */
        SmallMoleculeReagent r = well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", well.getWellKey().toString()),
                                              "molfile",
                                              "smiles",
                                              "inchi",
                                              new BigDecimal("1.000"),
                                              new BigDecimal("1.002"),
                                              new MolecularFormula("M1F2"));
        r.getCompoundNames().add("xyzNAME");
        r.getCompoundNames().add("abc");

        row = 1;
        col = 0;
        well = library.createWell(new WellKey(plate, row, col),
                                       LibraryWellType.UNDEFINED);
        /* SmallMoleculeReagent reagent = */
        r = well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", well.getWellKey().toString()),
                                              "molfile",
                                              "smiles",
                                              "inchi",
                                              new BigDecimal("1.000"),
                                              new BigDecimal("1.002"),
                                              new MolecularFormula("M1F2"));
        r.getCompoundNames().add("XYZname1");
        r.getCompoundNames().add("def");

        row = 2;
        col = 0;
        well = library.createWell(new WellKey(plate, row, col),
                                       LibraryWellType.UNDEFINED);
        /* SmallMoleculeReagent reagent = */
        r = well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", well.getWellKey().toString()),
                                              "molfile",
                                              "smiles",
                                              "inchi",
                                              new BigDecimal("1.000"),
                                              new BigDecimal("1.002"),
                                              new MolecularFormula("M1F2"));
        r.getCompoundNames().add("ABCname1");
        r.getCompoundNames().add("def");
        genericEntityDao.persistEntity(adminUser);
        genericEntityDao.persistEntity(library);
      }
    });

    Set<Library> inLibraries = Sets.newHashSet(genericEntityDao.findAllEntitiesOfType(Library.class));

    Set<WellKey> wellKeys = librariesDao.findWellKeysForCompoundName("xyz");
    assertFalse(wellKeys.isEmpty());
    assertEquals(2, wellKeys.size());

    wellKeys = librariesDao.findWellKeysForCompoundName("XYZ");
    assertFalse(wellKeys.isEmpty());
    assertEquals(2, wellKeys.size());

    wellKeys = librariesDao.findWellKeysForCompoundName("naME");
    assertFalse(wellKeys.isEmpty());
    assertEquals(3, wellKeys.size());

    wellKeys = librariesDao.findWellKeysForCompoundName("jkl");
    assertTrue(wellKeys.isEmpty());
  }

  public void testFindCanonicalReagent()
  {
    dataFactory.addPostCreateHook(SmallMoleculeReagent.class, new PostCreateHook<SmallMoleculeReagent>() {
      @Override
      public void postCreate(String callStack, SmallMoleculeReagent smr)
      {
        if (callStack.endsWith("csmr")) {
          smr.getWell().setFacilityId("ID1");
          smr.forFacilityBatchId(1);
          smr.forSaltFormId(1);
        }
        else if (callStack.endsWith("smr1")) {
          smr.getWell().setFacilityId("ID1");
          smr.forFacilityBatchId(2);
          smr.forSaltFormId(1);
        }
        else if (callStack.endsWith("smr2")) {
          smr.getWell().setFacilityId("ID2");
          smr.forFacilityBatchId(1);
          smr.forSaltFormId(1);
        }
      }
    });
    SmallMoleculeReagent smr1 = dataFactory.newInstance(SmallMoleculeReagent.class, "smr1");
    SmallMoleculeReagent smr2 = dataFactory.newInstance(SmallMoleculeReagent.class, "smr2");
    SmallMoleculeReagent canonicalSmr = dataFactory.newInstance(SmallMoleculeReagent.class, "csmr");
    canonicalSmr.getWell().getLibrary().setShortName("R-Lib");
    canonicalSmr.getWell().getLibrary().getLatestContentsVersion().release(null);
    smr1.getWell().getLibrary().getLatestContentsVersion().release(null);
    smr2.getWell().getLibrary().getLatestContentsVersion().release(null);
    genericEntityDao.mergeEntity(smr1.getWell().getLibrary());
    genericEntityDao.mergeEntity(smr2.getWell().getLibrary());
    genericEntityDao.mergeEntity(canonicalSmr.getWell().getLibrary());
    
    assertEquals(ImmutableSet.of(canonicalSmr.getWell().getWellId()),
                 librariesDao.findCanonicalReagentWellIds(ImmutableSet.of(smr1.getWell().getEntityId())));
    assertTrue(librariesDao.findCanonicalReagentWellIds(ImmutableSet.of(smr2.getWell().getEntityId())).isEmpty());
  }
}
