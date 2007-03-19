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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
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
        // each assertion (below), since we can set the starting volume each
        // plate independently
        
        Library library = new Library("library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 1, 6);
        for (int plateNumber = library.getStartPlate(); plateNumber <= library.getEndPlate(); plateNumber++) {
          makeRNAiWell(library, plateNumber, "A01");
        }
        dao.persistEntity(library);

        Copy copy1 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        new CopyInfo(copy1, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(12.0));
        new CopyInfo(copy1, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(11.0));
        new CopyInfo(copy1, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        
        Copy copy2 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        new CopyInfo(copy2, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        new CopyInfo(copy2, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        new CopyInfo(copy2, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(12.0));
        new CopyInfo(copy2, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy2, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        CopyInfo retiredPlateCopyInfo = 
        new CopyInfo(copy2, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        retiredPlateCopyInfo.setDateRetired(new Date());

        Copy copy3 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "F");
        new CopyInfo(copy3, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        new CopyInfo(copy3, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        new CopyInfo(copy3, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        new CopyInfo(copy3, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(12.0));
        new CopyInfo(copy3, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy3, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        CherryPickRequest cherryPickRequest = createCherryPickRequest(1, 11);

        CherryPick cherryPick1 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(1, "A01")));
        CherryPick cherryPick2 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(2, "A01")));
        CherryPick cherryPick3 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(3, "A01")));
        CherryPick cherryPick4 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(4, "A01")));
        CherryPick cherryPick5 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(5, "A01")));
        CherryPick cherryPick6 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(6, "A01")));
        dao.persistEntity(cherryPickRequest.getScreen());

        Set<CherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);

        assertEquals("sufficient volume in copy 1", "D", cherryPick1.getSourceCopy().getName());
        assertEquals("exact sufficient volume in copy 1", "D", cherryPick2.getSourceCopy().getName());
        assertEquals("sufficient volume in copy 2", "E", cherryPick3.getSourceCopy().getName());
        assertEquals("sufficient volume in copy 3", "F", cherryPick4.getSourceCopy().getName());
        assertFalse("insufficient volume in any copy; not allocated", cherryPick5.isAllocated());
        assertFalse("insufficient volume in any copy due to retired plate; not allocated", cherryPick6.isAllocated());
        
        Set<CherryPick> expectedUnfulfillableCherryPicks = new HashSet<CherryPick>();
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
        Library library = new Library("library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 1, 1);
        for (int i = 0; i < 6; ++i) {
          makeRNAiWell(library, 1, String.format("%c%02d", 'A' + i, i + 1));
        }
        dao.persistEntity(library);

        Copy copy1 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        new CopyInfo(copy1, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        Copy copy2 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        new CopyInfo(copy2, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(12.0));
      }
    });
    
    doTestCheryPickRequestAllocation(1, 
                                     new String[] {"A01"}, 
                                     new String[] {});
    doTestCheryPickRequestAllocation(2,
                                     new String[] {"A01", "B02"}, 
                                     new String[] {});
    doTestCheryPickRequestAllocation(3,
                                     new String[] {"A01", "B02", "C03"}, 
                                     new String[] {});
    doTestCheryPickRequestAllocation(4,
                                     new String[] {"A01", "B02", "C03", "D04"}, 
                                     new String[] {"A01"});
    doTestCheryPickRequestAllocation(5,
                                     new String[] {"A01", "B02", "C03", "D04", "E05"}, 
                                     new String[] {"A01", "B02"});
    doTestCheryPickRequestAllocation(6,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03"});
    doTestCheryPickRequestAllocation(7,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04"});
    doTestCheryPickRequestAllocation(8,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04", "E05"});
    doTestCheryPickRequestAllocation(9,
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"});
    
  }
  
  
  // private methods
  
  private CherryPickRequest createCherryPickRequest(int screenNumber, int volume)
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

  private void doTestCheryPickRequestAllocation(final int screenNumber,
                                                final String[] cherryPickWellNames, 
                                                final String[] expectedUnfillableCherryPickWellNames)
  {
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        CherryPickRequest cherryPickRequest = createCherryPickRequest(screenNumber, 6);
        Set<CherryPick> cherryPicks = new HashSet<CherryPick>();
        Set<CherryPick> expectedUnfulfillableCherryPicks = new HashSet<CherryPick>();
        Set<String> expectedUnfillableCherryPickWellNamesSet = new HashSet<String>(Arrays.asList(expectedUnfillableCherryPickWellNames));
        for (String cherryPickWellName : cherryPickWellNames) {
          CherryPick cherryPick = new CherryPick(cherryPickRequest,
                                                 dao.findWell(new WellKey(1,
                                                                          cherryPickWellName)));
          cherryPicks.add(cherryPick);
          if (expectedUnfillableCherryPickWellNamesSet.contains(cherryPickWellName)) {
            expectedUnfulfillableCherryPicks.add(cherryPick);
          }
        }
        dao.persistEntity(cherryPickRequest.getScreen());
        Set<CherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);
        assertEquals("unfulfillable cherry picks for requested " + Arrays.asList(cherryPickWellNames),
                     expectedUnfulfillableCherryPicks,
                     unfulfillableCherryPicks);
      }
    });
  }
  
  private void makeRNAiWell(Library library, int plateNumber, String wellName)
  {
    Well well = new Well(library, new WellKey(plateNumber, wellName), WellType.EXPERIMENTAL);
    Gene gene = new Gene("gene" + plateNumber + wellName,
                         new WellKey(plateNumber, wellName).hashCode(),
                         "entrezGeneSymbol" + wellName,
                         "Human");
    well.addSilencingReagent(new SilencingReagent(gene,
                                                  SilencingReagentType.SIRNA,
                                                  "ATCG"));
  }
  
}

