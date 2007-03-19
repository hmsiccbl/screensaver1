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
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.DateUtil;

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
        new CopyInfo(copy1, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy1, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        
        Copy copy2 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        new CopyInfo(copy2, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
        new CopyInfo(copy2, 2, "loc1", PlateType.EPPENDORF, new BigDecimal(11.0));
        new CopyInfo(copy2, 3, "loc1", PlateType.EPPENDORF, new BigDecimal(12.0));
        new CopyInfo(copy2, 4, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        new CopyInfo(copy2, 5, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        CopyInfo retiredPlateCopyInfo = new CopyInfo(copy1, 6, "loc1", PlateType.EPPENDORF, new BigDecimal(22.0));
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
        Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
        screen.setScreenType(ScreenType.RNAI);
        RNAiCherryPickRequest cherryPickRequest = new RNAiCherryPickRequest(screen, screen.getLeadScreener(), new Date());
        cherryPickRequest.setMicroliterTransferVolumePerWellApproved(new BigDecimal(11));
        CherryPick cherryPick1 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(1, "A01")));
        CherryPick cherryPick2 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(2, "A01")));
        CherryPick cherryPick3 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(3, "A01")));
        CherryPick cherryPick4 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(5, "A01")));
        CherryPick cherryPick5 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(5, "A01")));
        CherryPick cherryPick6 = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(6, "A01")));
        Set<CherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);

        assertEquals("sufficient volume in copy 1", "D", cherryPick1.getSourceCopy());
        assertEquals("exact sufficient volume in copy 1", "D", cherryPick2.getSourceCopy());
        assertEquals("sufficient volume in copy 2", "E", cherryPick3.getSourceCopy());
        assertEquals("sufficient volume in copy 3", "E", cherryPick4.getSourceCopy());
        assertFalse("insufficient volume in any copy; not allocated", cherryPick5.isAllocated());
        assertFalse("insufficient volume in any copy due to retrired plate; not allocated", cherryPick6.isAllocated());
        
        Set<CherryPick> expectedUnfulfillableCherryPicks = new HashSet<CherryPick>();
        expectedUnfulfillableCherryPicks.add(cherryPick5);
        expectedUnfulfillableCherryPicks.add(cherryPick6);
        assertEquals("unfulfillabe cherry picks", expectedUnfulfillableCherryPicks, unfulfillableCherryPicks);
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

        Copy copy1 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        new CopyInfo(copy1, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(10.0));
        Copy copy2 = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        new CopyInfo(copy2, 1, "loc1", PlateType.EPPENDORF, new BigDecimal(12.0));
      }
    });
    
    doTestCheryPickRequestAllocation(new String[] {"A01"}, 
                                     new String[] {});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02"}, 
                                     new String[] {});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03"}, 
                                     new String[] {});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03", "D04"}, 
                                     new String[] {"A01"});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03", "D04", "E05"}, 
                                     new String[] {"A01", "B02"});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03"});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04"});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04", "E05"});
    doTestCheryPickRequestAllocation(new String[] {"A01", "B02", "C03", "D04", "E05", "F06"}, 
                                     new String[] {"A01", "B02", "C03", "D04", "E05", "F06"});
    
  }
  
  
  // private methods

  private void doTestCheryPickRequestAllocation(final String[] cherryPickWellNames, 
                                                final String[] expectedUnfillableCherryPickWellNames)
  {
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
        screen.setScreenType(ScreenType.RNAI);
        RNAiCherryPickRequest cherryPickRequest = new RNAiCherryPickRequest(screen, screen.getLeadScreener(), DateUtil.makeDate(2007, 1, 1));
        cherryPickRequest.setMicroliterTransferVolumePerWellApproved(new BigDecimal(6));
        Set<CherryPick> cherryPicks = new HashSet<CherryPick>();
        Set<CherryPick> expectedUnfulfillableCherryPicks = new HashSet<CherryPick>();
        Set<String> expectedUnfillableCherryPickWellNamesSet = new HashSet<String>(Arrays.asList(expectedUnfillableCherryPickWellNames));
        for (String cherryPickWellName : cherryPickWellNames) {
          CherryPick cherryPick = new CherryPick(cherryPickRequest, dao.findWell(new WellKey(1, cherryPickWellName)));
          cherryPicks.add(cherryPick);
          if (expectedUnfillableCherryPickWellNamesSet.contains(cherryPickWellName)) {
            expectedUnfulfillableCherryPicks.add(cherryPick);
          }
        }
        Set<CherryPick> unfulfillableCherryPicks = cherryPickRequestAllocator.allocate(cherryPickRequest);
        assertEquals("unfulfillabe cherry picks", expectedUnfulfillableCherryPicks, unfulfillableCherryPicks);
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

