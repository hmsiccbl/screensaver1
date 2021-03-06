// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.policy.CherryPickPlateSourceWellMinimumVolumePolicy;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class CherryPickRequestAllocatorTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(CherryPickRequestAllocatorTest.class);

  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected CherryPickRequestDAO cherryPickRequestDao;
  @Autowired
  protected CherryPickRequestAllocator cherryPickRequestAllocator;
  @Autowired
  protected CherryPickRequestPlateMapper cherryPickRequestPlateMapper;

  private Volume _minimumSourceWellVolume = new Volume(5, VolumeUnit.MICROLITERS);


  /**
   * Test a single allocation request, focusing on testing the basic volume
   * comparison logic, using 3 virgin copies. Does not test the
   * "minimum copy count" logic, as only one cherry pick is created per plate.
   */
  public void testCherryPickRequestAllocatorSingle()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        // note: as a test-writing convenience, we create a library plate for
        // each assertion (below), since we can set the starting volume of each
        // plate independently

        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library1", 1, 6, PlateSize.WELLS_96);
        genericEntityDao.saveOrUpdateEntity(library);

        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "D");
        copy1.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(12).add(_minimumSourceWellVolume));
        copy1.getPlates().get(2).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(11).add(_minimumSourceWellVolume));
        copy1.getPlates().get(3).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        copy1.getPlates().get(4).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        copy1.getPlates().get(5).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        copy1.getPlates().get(6).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));

        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "E");
        copy2.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(22).add(_minimumSourceWellVolume));
        copy2.getPlates().get(2).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(22).add(_minimumSourceWellVolume));
        copy2.getPlates().get(3).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(12).add(_minimumSourceWellVolume));
        copy2.getPlates().get(4).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        copy2.getPlates().get(5).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        Plate retiredPlate =
          copy2.getPlates().get(6).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(22).add(_minimumSourceWellVolume));
        retiredPlate.setStatus(PlateStatus.RETIRED);

        Copy copy3 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "F");
        copy3.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(22).add(_minimumSourceWellVolume));
        copy3.getPlates().get(2).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(22).add(_minimumSourceWellVolume));
        copy3.getPlates().get(3).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(22).add(_minimumSourceWellVolume));
        copy3.getPlates().get(4).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(12).add(_minimumSourceWellVolume));
        copy3.getPlates().get(5).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        copy3.getPlates().get(6).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(1, new Volume(11));

        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick cherryPick1 = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick cherryPick2 = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(2, "A01")));
        LabCherryPick cherryPick3 = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(3, "A01")));
        LabCherryPick cherryPick4 = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(4, "A01")));
        LabCherryPick cherryPick5 = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(5, "A01")));
        LabCherryPick cherryPick6 = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(6, "A01")));
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();

        Set<LabCherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);

        assertEquals("sufficient volume in copy 1", "D", cherryPick1.getSourceCopy().getName());
        assertEquals("exact sufficient volume in copy 1", "D", cherryPick2.getSourceCopy().getName());
        assertEquals("sufficient volume in copy 2", "E", cherryPick3.getSourceCopy().getName());
        assertEquals("sufficient volume in copy 3", "F", cherryPick4.getSourceCopy().getName());
        assertFalse("insufficient volume in any copy; not allocated", cherryPick5.isAllocated());
        assertFalse("insufficient volume in any copy due to retired plate copy; not allocated", cherryPick6.isAllocated());

        Set<LabCherryPick> expectedUnfulfillableCherryPicks = new HashSet<LabCherryPick>();
        expectedUnfulfillableCherryPicks.add(cherryPick5);
        expectedUnfulfillableCherryPicks.add(cherryPick6);
        assertEquals("unfulfillable cherry picks", expectedUnfulfillableCherryPicks, unfulfillableCherryPicks);
      }
    });
  }

  /**
   * Test a single allocation request, focusing on testing the basic volume
   * comparison logic, using 3 virgin copies, and a minimum source well volume. Does not test the
   * "minimum copy count" logic, as only one cherry pick is created per plate.
   */
  public void testCherryPickRequestAllocatorWithNegativeMinimumVolume()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library1", 1, 2, PlateSize.WELLS_96);
        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C1");
        copy1.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(1, VolumeUnit.MICROLITERS).add(_minimumSourceWellVolume));
        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C2");
        copy2.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(3, VolumeUnit.MICROLITERS).add(_minimumSourceWellVolume));
        genericEntityDao.saveOrUpdateEntity(library);
      }
    });
    
    final CherryPickRequestAllocator allocator = new CherryPickRequestAllocator(genericEntityDao,
                                                                                librariesDao,
                                                                                cherryPickRequestDao,
                                                                                new CherryPickPlateSourceWellMinimumVolumePolicy() {

                                                                            @Override
                                                                            public Volume getMinimumVolumeAllowed(Well well)
                                                                            {
                                                                              return new Volume(-1, VolumeUnit.MICROLITERS);
                                                                            }
                                                                          });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(1, new Volume(2, VolumeUnit.MICROLITERS));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick lcp = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();
        Set<LabCherryPick> unfulfillableCherryPicks = allocator.allocate(cherryPickRequest);
        assertTrue(unfulfillableCherryPicks.isEmpty());
        assertEquals("sufficient volume in copy 1", "C1", lcp.getSourceCopy().getName());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(2, new Volume(2, VolumeUnit.MICROLITERS));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick lcp = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();
        Set<LabCherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);
        assertTrue(unfulfillableCherryPicks.isEmpty());
        assertEquals("sufficient volume in copy 2", "C2", lcp.getSourceCopy().getName());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(3, new Volume(2, VolumeUnit.MICROLITERS));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick lcp = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();
        Set<LabCherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);
        assertEquals(Sets.newHashSet(lcp), unfulfillableCherryPicks);
      }
    });

  }

  /**
   * Test multiple, sequential allocation requests, testing whether allocations
   * for each request are being recorded and considered in subsequent allocation
   * requests (volumes are cumulatively reduced).
   */
  public void testCherryPickRequestAllocatorMulti()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        genericEntityDao.saveOrUpdateEntity(library);

        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        copy1.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(10).add(_minimumSourceWellVolume));
        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "D");
        copy2.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(12).add(_minimumSourceWellVolume));
      }
    });


    Volume requestVolume = new Volume(6);
    doTestCherryPickRequestAllocation(1,
                                      requestVolume,
                                      new String[] {"A01"},
                                      new String[] {});
    doTestCherryPickRequestAllocation(2,
                                      requestVolume,
                                      new String[] {"A01", "B02"},
                                      new String[] {});
    doTestCherryPickRequestAllocation(3,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03"},
                                      new String[] {});
    doTestCherryPickRequestAllocation(4,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03", "D04"},
                                      new String[] {"A01"});
    doTestCherryPickRequestAllocation(5,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03", "D04", "E05"},
                                      new String[] {"A01", "B02"});
    doTestCherryPickRequestAllocation(6,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03", "D04", "E05", "F06"},
                                      new String[] {"A01", "B02", "C03"});
    doTestCherryPickRequestAllocation(7,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03", "D04", "E05", "F06"},
                                      new String[] {"A01", "B02", "C03", "D04"});
    doTestCherryPickRequestAllocation(8,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03", "D04", "E05", "F06"},
                                      new String[] {"A01", "B02", "C03", "D04", "E05"});
    doTestCherryPickRequestAllocation(9,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03", "D04", "E05", "F06"},
                                      new String[] {"A01", "B02", "C03", "D04", "E05", "F06"});

    // TODO: it would be good to test the case where allocation 1 is fulfilled
    // by copy 1, allocation 2 is only fullfillable by copy 2, but then
    // allocation 3 is fulfillable (again) by copy 1
  }

  /**
   * Test multiple, sequential allocation requests, testing whether manual well
   * volume adjustments considered in allocation requests.
   */
  public void testCherryPickRequestAllocatorWithAdjustedWellVolumesOnPlate()
  {
    final Volume requestVolume = new Volume(12);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        copy1.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(requestVolume.add(_minimumSourceWellVolume));
        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "D");
        copy2.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(requestVolume.add(_minimumSourceWellVolume));
        genericEntityDao.saveOrUpdateEntity(library);

        WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
          new WellVolumeCorrectionActivity(new AdministratorUser("Joe", "Admin"),
                                           new LocalDate());
        Set<WellVolumeAdjustment> wellVolumeAdjustments = wellVolumeCorrectionActivity.getWellVolumeAdjustments();
        Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
        Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
        Well wellC03 = genericEntityDao.findEntityById(Well.class, "00001:C03");
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy1, wellA01, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy2, wellA01, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy1, wellB02, new Volume(0)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy2, wellB02, new Volume(-1)));
        wellVolumeAdjustments.add(wellVolumeCorrectionActivity.createWellVolumeAdjustment(copy2, wellC03, requestVolume));
        genericEntityDao.saveOrUpdateEntity(wellVolumeCorrectionActivity);
      }
    });

    doTestCherryPickRequestAllocation(1,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03"},
                                      new String[] {"A01"});
    doTestCherryPickRequestAllocation(2,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03"},
                                      new String[] {"A01", "B02"});
    doTestCherryPickRequestAllocation(3,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03"},
                                      new String[] {"A01", "B02"});
    doTestCherryPickRequestAllocation(4,
                                      requestVolume,
                                      new String[] {"A01", "B02", "C03"},
                                      new String[] {"A01", "B02", "C03"});
  }

  /**
   * Tests the algorithm that selects the minimal number of copies that satisfies all cherry picks.
   */
  public void testAllocateWithMinimumCopiesPerPlate()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        genericEntityDao.saveOrUpdateEntity(library);

        Copy copy1 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        copy1.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(6).add(_minimumSourceWellVolume));
        Copy copy2 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "D");
        copy2.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(6).add(_minimumSourceWellVolume));
        Copy copy3 = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "E");
        copy3.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(new Volume(6).add(_minimumSourceWellVolume));
      }
    });

    // note: below operations are cumulative, so that previous operations affect
    // state being tested by subsequent operations

    Volume requestVolume = new Volume(6);
    RNAiCherryPickRequest cpr1 = 
      doTestCherryPickRequestAllocation(1,
                                        requestVolume,
                                        new String[] {"A01"},
                                        new String[] {});
    assertAllocationsArePreciselyFromCopies(cpr1, "C");

    RNAiCherryPickRequest cpr2 = 
      doTestCherryPickRequestAllocation(2,
                                        requestVolume,
                                        new String[] {"A01", "A02"},
                                        new String[] {});
    assertAllocationsArePreciselyFromCopies(cpr2, "D");
    
    RNAiCherryPickRequest cpr3 = 
      doTestCherryPickRequestAllocation(3,
                                        requestVolume,
                                        new String[] {"A02", "A03"},
                                        new String[] {});
    assertAllocationsArePreciselyFromCopies(cpr3, "C");

    RNAiCherryPickRequest cpr4 = 
      doTestCherryPickRequestAllocation(4,
                                        requestVolume,
                                        new String[] {"A02", "A03"},
                                        new String[] {});
    assertAllocationsArePreciselyFromCopies(cpr4, "E");
    
    RNAiCherryPickRequest cpr5 = 
      doTestCherryPickRequestAllocation(5,
                                        requestVolume,
                                        new String[] {"A01", "A02", "A03"},
                                        new String[] {"A02"}); // tests unfulfillable cherry pick
    assertAllocationsArePreciselyFromCopies(cpr5, "D", "E");
    
    // test worst case allocation (unique copy for each cherry pick)
    doTestCherryPickRequestAllocation(6,
                                      requestVolume,
                                      new String[] {"B01", "B05"},
                                      new String[] {});
    doTestCherryPickRequestAllocation(7,
                                      requestVolume,
                                      new String[] {"B01", "B03", "B05"},
                                      new String[] {});
    doTestCherryPickRequestAllocation(8,
                                      requestVolume,
                                      new String[] {"B02"},
                                      new String[] {});
    doTestCherryPickRequestAllocation(9,
                                      requestVolume,
                                      new String[] {"B02", "B03", "B05"},
                                      new String[] {});
    RNAiCherryPickRequest cpr11 = 
      doTestCherryPickRequestAllocation(11,
                                        requestVolume,
                                        new String[] {"B01", "B02", "B03"},
                                        new String[] {});
    assertAllocationsArePreciselyFromCopies(cpr11, "C", "D", "E");
  }

  private void assertAllocationsArePreciselyFromCopies(CherryPickRequest cpr, String... copyNamesArray)
  {
    Set<String> expectedcopyNames = Sets.newHashSet(copyNamesArray);
    Set<String> actualCopyNames = Sets.newHashSet();
    for (LabCherryPick lcp : cpr.getLabCherryPicks()) {
      Copy actualCopy = lcp.getSourceCopy();
      if (actualCopy != null) { 
        actualCopyNames.add(actualCopy.getName());
      }
    }
    assertEquals("allocations are from copies",
                 expectedcopyNames,
                 actualCopyNames);
  }

  public void testDeallocateAllCherryPicks()
  {
    final Volume requestVolume = new Volume(10);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        Copy copy = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        copy.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(requestVolume.add(_minimumSourceWellVolume));
        genericEntityDao.saveOrUpdateEntity(library);
      }
    });

    CherryPickRequest cpr1 = doTestCherryPickRequestAllocation(1,
                                                               requestVolume,
                                                               new String[] { "A01", "A02", "P23", "P24" },
                                                               new String[] {});
    assertTrue("CPR is allocated", cpr1.isAllocated());
    List<WellVolumeAdjustment> wellVolumeAdjustments1 = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
    assertEquals("wellVolumeAdjustment count after first CPR allocation", 4, wellVolumeAdjustments1.size());

    CherryPickRequest cpr2 = doTestCherryPickRequestAllocation(2,
                                                               requestVolume,
                                                               new String[] { "A01", "A02", "P23", "P24" },
                                                               new String[] { "A01", "A02", "P23", "P24" });
    List<WellVolumeAdjustment> wellVolumeAdjustments2 = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
    assertEquals("2nd CPR LCPs are unfulfillable", 4, wellVolumeAdjustments2.size());

    cpr1 = cherryPickRequestAllocator.deallocate(cpr1);
    List<WellVolumeAdjustment> wellVolumeAdjustments3 = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
    assertFalse("CPR is no longer allocated", cpr1.isAllocated());
    assertEquals("wellVolumeAdjustment count after CPR deallocation", 0, wellVolumeAdjustments3.size());
    assertEquals("number of unfulfilled lcps (persisted value)",
                 4,
                 cpr1.getNumberUnfulfilledLabCherryPicks());
  }

  public void testCancelAndDeallocateAssayPlates()
  {
    final Volume requestVolume = new Volume(10);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        Copy copy = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        copy.getPlates().get(1).withStatus(PlateStatus.AVAILABLE).withWellVolume(requestVolume.add(_minimumSourceWellVolume));
        genericEntityDao.saveOrUpdateEntity(library);
      }
    });
    final AdministratorUser adminUser = new AdministratorUser("Test", "Admin");
    genericEntityDao.persistEntity(adminUser);

    List<WellVolumeAdjustment> wvas = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
    assertTrue("no well volume adjustments, initially", wvas.isEmpty());
    final RNAiCherryPickRequest cpr = doTestCherryPickRequestAllocation(1,
                                                                        requestVolume,
                                                                        new String[] {"A01", "A02", "P23", "P24"},
                                                                        new String[] {});

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cpr2 = genericEntityDao.reloadEntity(cpr);
        assertTrue("CPR is allocated", cpr2.isAllocated());
        cherryPickRequestPlateMapper.generatePlateMapping(cpr2);
        List<WellVolumeAdjustment> wvas = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
        assertTrue("CPR is mapped", cpr2.isMapped());
        assertEquals("well volume adjustment counts, after CPR is allocated", 4, wvas.size());
        assertEquals("number of unfulfilled lcps (persisted value)", 
                     0,
                     cpr2.getNumberUnfulfilledLabCherryPicks());
      }
    });

    // note: we want detached assay plate entity instances, as the methods being tested need to handle this
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cpr2 = genericEntityDao.reloadEntity(cpr/*
                                                                   * , false,
                                                                   * "cherryPickAssayPlates.labCherryPicks.wellVolumeAdjustments"
                                                                   */);
        HashSet<CherryPickAssayPlate> assayPlatesToCancel = Sets.newHashSet(cpr2.getActiveCherryPickAssayPlates());
        assertEquals("assay plates to cancel count", 1, assayPlatesToCancel.size());
        CherryPickLiquidTransfer cplt = cpr2.getScreen().createCherryPickLiquidTransfer(adminUser, adminUser, new LocalDate(), CherryPickLiquidTransferStatus.CANCELED);
        cplt.addCherryPickAssayPlate(assayPlatesToCancel.iterator().next());
        genericEntityDao.saveOrUpdateEntity(cplt);
        cherryPickRequestAllocator.deallocateAssayPlates(assayPlatesToCancel);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cpr2 = genericEntityDao.reloadEntity(cpr);
        assertTrue("CPR is still allocated", cpr2.isAllocated());
        List<WellVolumeAdjustment> wvas = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
        assertEquals("well volume adjustment counts, after CPR assay plates are cancelled", 0, wvas.size());
//        assertEquals("number of unfulfilled lcps (persisted value)", 
//                     4,
//                     cpr2.getNumberUnfulfilledLabCherryPicks());
        for (WellVolumeAdjustment wva : wvas) {
          assertTrue("lab cherry pick is cancelled", wva.getLabCherryPick().isCancelled());
        }
      }
    });
  }
  
  private RNAiCherryPickRequest doTestCherryPickRequestAllocation(final int screenFacilityId,
                                                                  final Volume requestVolume,
                                                                  final String[] cherryPickWellNames,
                                                                  final String[] expectedUnfillableCherryPickWellNames)
  {
    final RNAiCherryPickRequest result[] = new RNAiCherryPickRequest[1];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        RNAiCherryPickRequest cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(screenFacilityId, requestVolume);
        Set<LabCherryPick> expectedUnfulfillableCherryPicks = new HashSet<LabCherryPick>();
        Set<String> expectedUnfillableCherryPickWellNamesSet = new HashSet<String>(Arrays.asList(expectedUnfillableCherryPickWellNames));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, new WellName(cherryPickWellNames[0]))));
        for (String cherryPickWellName : cherryPickWellNames) {
          LabCherryPick labCherryPick = dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(1, new WellName(cherryPickWellName))));
          if (expectedUnfillableCherryPickWellNamesSet.contains(labCherryPick.getSourceWell().getWellName())) {
            expectedUnfulfillableCherryPicks.add(labCherryPick);
          }
        }
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();
        
        Set<LabCherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);
        assertEquals("unfulfillable cherry picks for requested " + Arrays.asList(cherryPickWellNames),
                     expectedUnfulfillableCherryPicks,
                     unfulfillableCherryPicks);
        assertEquals("number of unfulfilled lcps (persisted value)", 
                     expectedUnfillableCherryPickWellNames.length, 
                     cherryPickRequest.getNumberUnfulfilledLabCherryPicks());
        result[0] = cherryPickRequest;
      }
    });
    return result[0];
  }
}

