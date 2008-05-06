// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

public class CherryPickRequestAllocatorTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestAllocatorTest.class);


  // instance data members

  protected LibrariesDAO librariesDao;
  protected CherryPickRequestAllocator cherryPickRequestAllocator;
  protected CherryPickRequestPlateMapper cherryPickRequestPlateMapper;


  // public constructors and methods

  public void testCherryPickRequestAllocatorSingle()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        // note: as a test-writing convenience, we create a library plate for
        // each assertion (below), since we can set the starting volume of each
        // plate independently

        Library library = makeRNAiDuplexLibrary("library1", 1, 6, 1);
        genericEntityDao.saveOrUpdateEntity(library);

        Copy copy1 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        copy1.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy1.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(11).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy1.createCopyInfo(3, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy1.createCopyInfo(4, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy1.createCopyInfo(5, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy1.createCopyInfo(6, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));

        Copy copy2 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        copy2.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy2.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy2.createCopyInfo(3, "loc1", PlateType.EPPENDORF, new Volume(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy2.createCopyInfo(4, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy2.createCopyInfo(5, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        CopyInfo retiredPlateCopyInfo =
          copy2.createCopyInfo(6, "loc1", PlateType.EPPENDORF, new Volume(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        retiredPlateCopyInfo.setDateRetired(new Date());

        Copy copy3 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "F");
        copy3.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy3.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy3.createCopyInfo(3, "loc1", PlateType.EPPENDORF, new Volume(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy3.createCopyInfo(4, "loc1", PlateType.EPPENDORF, new Volume(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy3.createCopyInfo(5, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        copy3.createCopyInfo(6, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cherryPickRequest = createRNAiCherryPickRequest(1, new Volume(11));

        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick cherryPick1 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, librariesDao.findWell(new WellKey(1, "A01")));
        LabCherryPick cherryPick2 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, librariesDao.findWell(new WellKey(2, "A01")));
        LabCherryPick cherryPick3 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, librariesDao.findWell(new WellKey(3, "A01")));
        LabCherryPick cherryPick4 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, librariesDao.findWell(new WellKey(4, "A01")));
        LabCherryPick cherryPick5 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, librariesDao.findWell(new WellKey(5, "A01")));
        LabCherryPick cherryPick6 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, librariesDao.findWell(new WellKey(6, "A01")));
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());

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

  public void testCherryPickRequestAllocatorMulti()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = makeRNAiDuplexLibrary("library", 1, 1, 384);
        genericEntityDao.saveOrUpdateEntity(library);

        Copy copy1 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copy1.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        Copy copy2 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        copy2.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
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

  public void testCherryPickRequestAllocatorWithAdjustedWellVolumesOnPlate()
  {
    final Volume requestVolume = new Volume(12);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = makeRNAiDuplexLibrary("library", 1, 1, 384);
        Copy copy1 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copy1.createCopyInfo(1, "loc1", PlateType.EPPENDORF, requestVolume.add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        Copy copy2 = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        copy2.createCopyInfo(1, "loc1", PlateType.EPPENDORF, requestVolume.add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        genericEntityDao.saveOrUpdateEntity(library);

        WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
          new WellVolumeCorrectionActivity(new AdministratorUser("Joe", "Admin", "joe_admin@hms.harvard.edu", "", "", "", "", ""),
                                           new Date());
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

  public void testAllocateSingleLabCherryPick()
  {
    // TODO: implement
    // cherryPickRequestAllocator.allocate(labCherryPick);
  }

  public void testDeallocateAllCherryPicks()
  {
    final Volume requestVolume = new Volume(10);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = makeRNAiDuplexLibrary("library", 1, 1, 384);
        Copy copy = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copy.createCopyInfo(1, "loc1", PlateType.EPPENDORF, requestVolume.add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        genericEntityDao.saveOrUpdateEntity(library);
      }
    });

    final RNAiCherryPickRequest cpr = doTestCherryPickRequestAllocation(1,
                                                                        requestVolume,
                                                                        new String[] {"A01", "A02", "P23", "P24"},
                                                                        new String[] {});
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        assertTrue("CPR is allocated", cpr.isAllocated());
        List<WellVolumeAdjustment> wellVolumeAdjustments1 = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
        assertEquals("wellVolumeAdjustment count after first CPR allocation", 4, wellVolumeAdjustments1.size());
      }
    });

    doTestCherryPickRequestAllocation(2,
                                      requestVolume,
                                      new String[] {"A01", "A02", "P23", "P24"},
                                      new String[] {"A01", "A02", "P23", "P24"});
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        List<WellVolumeAdjustment> wellVolumeAdjustments2 = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
        assertEquals("wellVolumeAdjustment count after second CPR allocation", 4, wellVolumeAdjustments2.size());
      }
    });

    cherryPickRequestAllocator.deallocate(cpr);
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        List<WellVolumeAdjustment> wellVolumeAdjustments3 = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
        assertFalse("CPR is no longer allocated", cpr.isAllocated());
        assertEquals("wellVolumeAdjustment count after CPR deallocation", 0, wellVolumeAdjustments3.size());
        assertEquals("number of unfulfilled lcps (persisted value)", 
                     4,
                     cpr.getNumberUnfulfilledLabCherryPicks());
      }
    });
  }

  public void testCancelAndDeallocateAssayPlates()
  {
    final Volume requestVolume = new Volume(10);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = makeRNAiDuplexLibrary("library", 1, 1, 384);
        Copy copy = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copy.createCopyInfo(1, "loc1", PlateType.EPPENDORF, requestVolume.add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        genericEntityDao.saveOrUpdateEntity(library);
      }
    });
    final AdministratorUser adminUser = new AdministratorUser("Test", "Admin", "", "", "", "", "", "");
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

    // note: we want detached assay plate entity instances, as the method being test needs to handle this
    final HashSet<CherryPickAssayPlate> assayPlatesToCancel = new HashSet<CherryPickAssayPlate>(genericEntityDao.reloadEntity(cpr, false, "cherryPickAssayPlates").getActiveCherryPickAssayPlates());
    assertEquals("assay plates to cancel count", 1, assayPlatesToCancel.size());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cpr2 = genericEntityDao.reloadEntity(cpr);
        cherryPickRequestAllocator.cancelAndDeallocateAssayPlates(cpr2, assayPlatesToCancel, adminUser, new Date(), "test comment");
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cpr2 = genericEntityDao.reloadEntity(cpr);
        assertTrue("CPR is still allocated", cpr2.isAllocated());
        List<WellVolumeAdjustment> wvas = genericEntityDao.findAllEntitiesOfType(WellVolumeAdjustment.class);
        assertEquals("well volume adjustment counts, after CPR assay plates are cancelled", 0, wvas.size());
        assertEquals("number of unfulfilled lcps (persisted value)", 
                     4,
                     cpr2.getNumberUnfulfilledLabCherryPicks());
      }
    });
  }


  // static util methods

  public static Library makeRNAiDuplexLibrary(String name, int startPlate, int endPlate, int wellsPerPlate)
  {
    Library library = new Library(name, name, ScreenType.RNAI, LibraryType.COMMERCIAL, startPlate, endPlate);
    NEXT_PLATE:
    for (int plateNumber = startPlate; plateNumber <= endPlate; plateNumber++) {
      int wellsToCreateOnPlate = wellsPerPlate;
      for (int iRow = 0; iRow < Well.PLATE_ROWS; iRow++) {
        for (int iCol = 0; iCol < Well.PLATE_COLUMNS; iCol++) {
          makeRNAiWell(library, plateNumber, new WellName(iRow, iCol));
          if (--wellsToCreateOnPlate <= 0) {
            continue NEXT_PLATE;
          }
        }
      }
    }
    return library;
  }

  public static RNAiCherryPickRequest createRNAiCherryPickRequest(int screenNumber, Volume volume)
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(screenNumber, ScreenType.RNAI);
    // Note: if we use screen.getLeadScreener() as requestor, Hibernate complains!
    ScreeningRoomUser cherryPickRequestor =
      MakeDummyEntities.makeDummyUser(screenNumber, "Cherry", "Picker");
    RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest)
      screen.createCherryPickRequest(cherryPickRequestor, new Date());
    cherryPickRequest.setTransferVolumePerWellApproved(volume);
    return cherryPickRequest;
  }

  public static Well makeRNAiWell(Library library, int plateNumber, WellName wellName)
  {
    Gene gene = new Gene("gene" + plateNumber + wellName,
                         new WellKey(plateNumber, wellName).hashCode(),
                         "entrezGeneSymbol" + wellName,
                         "Human");
    SilencingReagent silencingReagent = gene.createSilencingReagent(
                         SilencingReagentType.SIRNA,
                         "ATCG");
    return makeRNAiWell(library, plateNumber, wellName, silencingReagent);
  }

  private static Well makeRNAiWell(Library library, int plateNumber, WellName wellName, SilencingReagent siReagent)
  {
    Well well1 = library.createWell(new WellKey(plateNumber, wellName), WellType.EXPERIMENTAL);
    well1.addSilencingReagent(siReagent);
    return well1;
  }

  public static Set<Well> makeRNAiDuplexWellsForPoolWell(Library duplexLibrary, Well poolWell, int plateNumber, WellName wellName)
  {
    Set<Well> duplexWells = new HashSet<Well>();
    for (SilencingReagent silencingReagent : poolWell.getGene().getSilencingReagents()) {
      duplexWells.add(makeRNAiWell(duplexLibrary,
                                   plateNumber++,
                                   wellName,
                                   silencingReagent));
    }
    return duplexWells;
  }


  // private methods

  private RNAiCherryPickRequest doTestCherryPickRequestAllocation(final int screenNumber,
                                                                  final Volume requestVolume,
                                                                  final String[] cherryPickWellNames,
                                                                  final String[] expectedUnfillableCherryPickWellNames)
  {
    final RNAiCherryPickRequest result[] = new RNAiCherryPickRequest[1];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        RNAiCherryPickRequest cherryPickRequest = createRNAiCherryPickRequest(screenNumber, requestVolume);
        Set<LabCherryPick> expectedUnfulfillableCherryPicks = new HashSet<LabCherryPick>();
        Set<String> expectedUnfillableCherryPickWellNamesSet = new HashSet<String>(Arrays.asList(expectedUnfillableCherryPickWellNames));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, new WellName(cherryPickWellNames[0]))));
        for (String cherryPickWellName : cherryPickWellNames) {
          LabCherryPick labCherryPick = cherryPickRequest.createLabCherryPick(
            dummyScreenerCherryPick,
            librariesDao.findWell(new WellKey(1, new WellName(cherryPickWellName))));
          if (expectedUnfillableCherryPickWellNamesSet.contains(labCherryPick.getSourceWell().getWellName())) {
            expectedUnfulfillableCherryPicks.add(labCherryPick);
          }
        }
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
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

