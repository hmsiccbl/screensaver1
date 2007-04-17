// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
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
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

public class CherryPickRequestAllocatorTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestAllocatorTest.class);

  
  // instance data members
  
  protected CherryPickRequestAllocator cherryPickRequestAllocator;
  

  // public constructors and methods
  
  public void testCherryPickRequestAllocatorSingle()
  {
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        // note: as a test-writing convenience, we create a library plate for
        // each assertion (below), since we can set the starting volume of each
        // plate independently
        
        Library library = makeRNAiDuplexLibrary("library1", 1, 6, 1);
        dao.persistEntity(library);

        Copy copy1 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        new CopyInfo(copy1, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy1, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(11).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy1, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy1, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy1, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy1, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        
        Copy copy2 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        new CopyInfo(copy2, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy2, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy2, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy2, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy2, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        CopyInfo retiredPlateCopyInfo = 
        new CopyInfo(copy2, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        retiredPlateCopyInfo.setDateRetired(new Date());

        Copy copy3 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "F");
        new CopyInfo(copy3, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy3, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy3, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(22).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy3, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy3, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        new CopyInfo(copy3, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cherryPickRequest = createCherryPickRequest(1, 11);

        ScreenerCherryPick dummyScreenerCherryPick = new ScreenerCherryPick(cherryPickRequest, dao.findWell(new WellKey(1, "A01")));
        LabCherryPick cherryPick1 = new LabCherryPick(dummyScreenerCherryPick, dao.findWell(new WellKey(1, "A01")));
        LabCherryPick cherryPick2 = new LabCherryPick(dummyScreenerCherryPick, dao.findWell(new WellKey(2, "A01")));
        LabCherryPick cherryPick3 = new LabCherryPick(dummyScreenerCherryPick, dao.findWell(new WellKey(3, "A01")));
        LabCherryPick cherryPick4 = new LabCherryPick(dummyScreenerCherryPick, dao.findWell(new WellKey(4, "A01")));
        LabCherryPick cherryPick5 = new LabCherryPick(dummyScreenerCherryPick, dao.findWell(new WellKey(5, "A01")));
        LabCherryPick cherryPick6 = new LabCherryPick(dummyScreenerCherryPick, dao.findWell(new WellKey(6, "A01")));
        dao.persistEntity(cherryPickRequest.getScreen());

        Set<LabCherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);

        assertEquals("sufficient volume in copy 1", "D", cherryPick1.getSourceCopy().getName());
        assertEquals("exact sufficient volume in copy 1", "D", cherryPick2.getSourceCopy().getName());
        assertEquals("sufficient volume in copy 2", "E", cherryPick3.getSourceCopy().getName());
        assertEquals("sufficient volume in copy 3", "F", cherryPick4.getSourceCopy().getName());
        assertFalse("insufficient volume in any copy; not allocated", cherryPick5.isAllocated());
        assertFalse("insufficient volume in any copy due to retired plate; not allocated", cherryPick6.isAllocated());
        
        Set<LabCherryPick> expectedUnfulfillableCherryPicks = new HashSet<LabCherryPick>();
        expectedUnfulfillableCherryPicks.add(cherryPick5);
        expectedUnfulfillableCherryPicks.add(cherryPick6);
        assertEquals("unfulfillable cherry picks", expectedUnfulfillableCherryPicks, unfulfillableCherryPicks);
      }
    });
  }

  public void testCherryPickRequestAllocatorMulti()
  {
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = makeRNAiDuplexLibrary("library", 1, 1, 384);
        dao.persistEntity(library);

        Copy copy1 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        new CopyInfo(copy1, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(10).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
        Copy copy2 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        new CopyInfo(copy2, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(12).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
      }
    });
    
    doTestCherryPickRequestAllocation(1, 
                                     new String[] {"A01"}, 
                                     new String[] {});
    doTestCherryPickRequestAllocation(2,
                                     new String[] {"A01", "B02"}, 
                                     new String[] {});
    doTestCherryPickRequestAllocation(3,
                                     new String[] {"A01", "B02", "C03"}, 
                                     new String[] {});
    doTestCherryPickRequestAllocation(4,
                                     new String[] {"A01", "B02", "C03", "D04"}, 
                                     new String[] {"A01"});
    doTestCherryPickRequestAllocation(5,
                                     new String[] {"A01", "B02", "C03", "D04", "E05"}, 
                                     new String[] {"A01", "B02"});
    doTestCherryPickRequestAllocation(6,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03"});
    doTestCherryPickRequestAllocation(7,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04"});
    doTestCherryPickRequestAllocation(8,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04", "E05"});
    doTestCherryPickRequestAllocation(9,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"});
   
    // TODO: it would be good to test the case where allocation 1 is fulfilled
    // by copy 1, allocation 2 is only fullfillable by copy 2, but then
    // allocation 3 is fulfillable (again) by copy 1
  }
  
  public void testAllocateSingleLabCherryPick()
  {
    fail("not implemented");
    // cherryPickRequestAllocator.allocate(labCherryPick);
  }
  
  public void testDeallocate()
  {
    fail("not implemented");
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

  static CherryPickRequest createCherryPickRequest(int screenNumber, int volume)
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(screenNumber);
    screen.setScreenType(ScreenType.RNAI);
    // Note: if we use screen.getLeadScreener() as requestor, Hibernate complains!
    ScreeningRoomUser cherryPickRequestor =
      MockDaoForScreenResultImporter.makeDummyUser(screenNumber, "Cherry", "Picker");
    RNAiCherryPickRequest cherryPickRequest = new RNAiCherryPickRequest(screen,
                                                                        cherryPickRequestor,
                                                                        new Date());
    cherryPickRequest.setMicroliterTransferVolumePerWellApproved(new BigDecimal(volume));
    return cherryPickRequest;
  }
  
  public static Well makeRNAiWell(Library library, int plateNumber, WellName wellName)
  {
    Gene gene = new Gene("gene" + plateNumber + wellName,
                         new WellKey(plateNumber, wellName).hashCode(),
                         "entrezGeneSymbol" + wellName,
                         "Human");
    SilencingReagent silencingReagent = new SilencingReagent(gene,
                         SilencingReagentType.SIRNA,
                         "ATCG");
    return makeRNAiWell(library, plateNumber, wellName, silencingReagent);
  }
  
  private static Well makeRNAiWell(Library library, int plateNumber, WellName wellName, SilencingReagent siReagent)
  {
    Well well1 = new Well(library, new WellKey(plateNumber, wellName), WellType.EXPERIMENTAL);
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

  private void doTestCherryPickRequestAllocation(final int screenNumber,
                                                 final String[] cherryPickWellNames, 
                                                 final String[] expectedUnfillableCherryPickWellNames)
  {
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = createCherryPickRequest(screenNumber, 6);
        Set<LabCherryPick> expectedUnfulfillableCherryPicks = new HashSet<LabCherryPick>();
        Set<String> expectedUnfillableCherryPickWellNamesSet = new HashSet<String>(Arrays.asList(expectedUnfillableCherryPickWellNames));
        ScreenerCherryPick dummyScreenerCherryPick = new ScreenerCherryPick(cherryPickRequest, dao.findWell(new WellKey(1, new WellName(cherryPickWellNames[0]))));
        for (String cherryPickWellName : cherryPickWellNames) {
          LabCherryPick labCherryPick = new LabCherryPick(dummyScreenerCherryPick, 
                                                          dao.findWell(new WellKey(1, 
                                                                                   new WellName(cherryPickWellName))));
          if (expectedUnfillableCherryPickWellNamesSet.contains(labCherryPick.getSourceWell().getWellName())) {
            expectedUnfulfillableCherryPicks.add(labCherryPick);
          }
        }
        dao.persistEntity(cherryPickRequest.getScreen());
        Set<LabCherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);
        assertEquals("unfulfillable cherry picks for requested " + Arrays.asList(cherryPickWellNames),
                     expectedUnfulfillableCherryPicks,
                     unfulfillableCherryPicks);
      }
    });
  }
}

