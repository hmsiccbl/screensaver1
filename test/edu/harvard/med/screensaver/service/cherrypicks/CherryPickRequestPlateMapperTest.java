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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;

import org.apache.log4j.Logger;

public class CherryPickRequestPlateMapperTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestPlateMapperTest.class);

  
  // instance data members
  
  protected CherryPickRequestPlateMapper cherryPickRequestPlateMapper;
  protected CherryPickRequestAllocator cherryPickRequestAllocator;
  

  // public constructors and methods
  
  public void testCherryPickPlateMapper()
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiLibrary("library", 1, 6, 384);
        makeLibraryCopy(library, "C", 10);
        makeLibraryCopy(library, "D", 10);
        dao.persistEntity(library);

        // create and allocate a cherry pick request, to cause next cherry pick request to draw from multiple plates
        CherryPickRequest earlierCherryPickRequest = CherryPickRequestAllocatorTest.createCherryPickRequest(1, 10);
        new CherryPick(earlierCherryPickRequest, dao.findWell(new WellKey(5, "A01")));
        new CherryPick(earlierCherryPickRequest, dao.findWell(new WellKey(5, "A02")));
        new CherryPick(earlierCherryPickRequest, dao.findWell(new WellKey(5, "A03")));
        new CherryPick(earlierCherryPickRequest, dao.findWell(new WellKey(6, "A01")));
        new CherryPick(earlierCherryPickRequest, dao.findWell(new WellKey(6, "A02")));
        new CherryPick(earlierCherryPickRequest, dao.findWell(new WellKey(6, "A03")));
        dao.persistEntity(earlierCherryPickRequest.getScreen());
        cherryPickRequestAllocator.allocate(earlierCherryPickRequest);

        CherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createCherryPickRequest(2, 10);
        cherryPickRequest.setRandomizedAssayPlateLayout(false);
        Set<Integer> emptyColumns = new HashSet<Integer>();
        emptyColumns.addAll(Arrays.asList(4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22));
        cherryPickRequest.setEmptyColumnsOnAssayPlate(emptyColumns);
        addCherryPicks(cherryPickRequest, 1, "A01", "A14"); // to assay plate 1, col 3 and 8 (partially)
        addCherryPicks(cherryPickRequest, 2, "A01", "A08"); // to assay plate 1, col 8 (leaving 2 available)
        addCherryPicks(cherryPickRequest, 3, "A01", "A16"); // to assay plate 2
        addCherryPicks(cherryPickRequest, 4, "A01", "A08"); // to assay plate 2 (exactly full)
        addCherryPicks(cherryPickRequest, 5, "A01", "B02"); // C copies (23) to assay plate 3, D copies (3) to assay plate 4
        addCherryPicks(cherryPickRequest, 6, "A01", "A10"); // both C (3) and D copies (7) to assay plate 4
        assertEquals(82, cherryPickRequest.getCherryPicks().size());
        dao.persistEntity(cherryPickRequest.getScreen());
        cherryPickRequestAllocator.allocate(cherryPickRequest);
        cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequest);

        assertEquals("assay plates count", 4, cherryPickRequest.getAssayPlates().size());
        assertCherryPicksOnAssayPlate(cherryPickRequest, 0, 21, 0);
        assertCherryPicksOnAssayPlate(cherryPickRequest, 22, 45, 1);
        assertCherryPicksOnAssayPlate(cherryPickRequest, 46, 68, 2);
        assertCherryPicksOnAssayPlate(cherryPickRequest, 69, 81, 3);
      }
    });
  }

  private void addCherryPicks(CherryPickRequest cherryPickRequest, 
                              int libraryPlateNumber, 
                              String firstWellNameStr, 
                              String lastWellNameStr)
  {
    WellName firstWellName = new WellName(firstWellNameStr);
    WellName lastWellName = new WellName(lastWellNameStr);
    for (int iRow = firstWellName.getRowIndex(); iRow <= lastWellName.getRowIndex(); ++iRow) {
      int iColFirst;
      if (iRow == firstWellName.getRowIndex()) {
        iColFirst = firstWellName.getColumnIndex();
      } 
      else {
        iColFirst = Well.MIN_WELL_COLUMN - 1;
      }
      int iColLast;
      if (iRow == lastWellName.getRowIndex()) {
        iColLast = lastWellName.getColumnIndex();
      } 
      else {
        iColLast = Well.MAX_WELL_COLUMN - 1;
      }
      for (int iCol = iColFirst; iCol <= iColLast; ++iCol) {
        new CherryPick(cherryPickRequest, dao.findWell(new WellKey(libraryPlateNumber, new WellName(iRow, iCol))));
      }
    }
  }


  // private methods

  private void assertCherryPicksOnAssayPlate(CherryPickRequest cherryPickRequest, 
                                             int firstIndex, 
                                             int lastIndex, 
                                             int expectedAssayPlateIndex)
  {
    TreeSet<CherryPick> sortedCherryPicks = new TreeSet<CherryPick>(PlateMappingCherryPickComparator.getInstance());
    sortedCherryPicks.addAll(cherryPickRequest.getCherryPicks());
    List<CherryPick> indexedCherryPicks = new ArrayList<CherryPick>(sortedCherryPicks);
    for (int index = firstIndex; index <= lastIndex; index++) {
      String expectedAssayPlateName = String.format("Cherry Picker (%d) CP%d  Plate %02d of %d",
                                                    cherryPickRequest.getScreen().getScreenNumber(),
                                                    cherryPickRequest.getOrdinal(),
                                                    expectedAssayPlateIndex + 1,
                                                    cherryPickRequest.getAssayPlates().size());
      CherryPick cherryPick = indexedCherryPicks.get(index);
      assertEquals("cherry pick #" + index + " assay plate name",
                   expectedAssayPlateName,
                   cherryPick.getAssayPlateName());
    }
  }

  private void makeLibraryCopy(Library library, String copyName, int volume)
  {
    Copy copy = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, copyName);
    for (int plateNumber = library.getStartPlate(); plateNumber <= library.getEndPlate(); plateNumber++) {
      new CopyInfo(copy, plateNumber, "<loc>", PlateType.EPPENDORF, new BigDecimal(volume));      
    }
  }


}

