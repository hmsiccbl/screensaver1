// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CherryPickRequestPlateMapperTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestPlateMapperTest.class);


  // instance data members

  protected LibrariesDAO librariesDao;
  protected CherryPickRequestPlateMapper cherryPickRequestPlateMapper;
  protected CherryPickRequestAllocator cherryPickRequestAllocator;


  // public constructors and methods

  public void testCherryPickPlateMapper()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Library duplexLibrary = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes library", 1, 6, PlateSize.WELLS_384);
        makeLibraryCopy(duplexLibrary, "C", 10);
        makeLibraryCopy(duplexLibrary, "D", 10);
        genericEntityDao.persistEntity(duplexLibrary);
        genericEntityDao.flush(); // needed since cprAllocator.allocate() calls reload for each well, which must be in db (not just session)

        // create and allocate a cherry pick request, to force next cherry pick
        // request to allocate some cherry picks from alternate plate copies;
        // we need to allocate from multiple copies to test that sets of picks from
        // different copies are kept together on same cherry pick plate
        {
          CherryPickRequest earlierCherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(10));
          ScreenerCherryPick dummyScreenerCherryPick = earlierCherryPickRequest.createScreenerCherryPick(
            librariesDao.findWell(new WellKey(1, "A01")));
          // these cherry picks will force subsequent picks from the same well to be allocated from copy D
          Copy copyC = duplexLibrary.getCopy("C");
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(5, "A01"))).setAllocated(copyC);
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(5, "A02"))).setAllocated(copyC);
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(5, "A03"))).setAllocated(copyC);
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(6, "A01"))).setAllocated(copyC);
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(6, "A02"))).setAllocated(copyC);
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(6, "A03"))).setAllocated(copyC);
          // due to the "minimal copy usage" allocation feature, we need force
          // at least one D copy well on library plate 5 to be depleted, so that all cherry picks
          // (in the next cpr) are not allocated exclusively from copy D; the
          // effect will be that future picks from the above wells will be
          // allocated from copy D, and all others from copy C.
          Copy copyD = duplexLibrary.getCopy("D");
          dummyScreenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(5, "A24"))).setAllocated(copyD);

          genericEntityDao.saveOrUpdateEntity(earlierCherryPickRequest.getScreen());
          genericEntityDao.flush(); // needed to make sure above allocations are in the database; allocate(), below, will query db (not just session) for existing well volume reservations
        }

        CherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(2, new Volume(10));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(
          librariesDao.findWell(new WellKey(1, "A01")));
        cherryPickRequest.setRandomizedAssayPlateLayout(false);
        Set<WellName> emptyWells = makeEmptyWellsFromColumnsAndRows(Arrays.asList(/*2,*/ 3, 4, 5, 6, /*7,*/ 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 21),
                                                                    Arrays.asList(3), 
                                                                    duplexLibrary);
        cherryPickRequest.addEmptyWellsOnAssayPlate(emptyWells);
        // cherry picks intended for plate 1
        addLabCherryPicks(dummyScreenerCherryPick, 1, "A01", "A14"); // to assay plate 1, col 3 (fully) and 8 (partially)
        addLabCherryPicks(dummyScreenerCherryPick, 2, "A01", "A08"); // to assay plate 1, col 8 (leaving 1 available)
        // cherry picks intended for plate 2
        addLabCherryPicks(dummyScreenerCherryPick, 3, "A01", "A16"); // to assay plate 2
        addLabCherryPicks(dummyScreenerCherryPick, 4, "A01", "A06"); // to assay plate 2 (exactly full)
        // cherry picks intended for plate 3
        addLabCherryPicks(dummyScreenerCherryPick, 5, "A01", "A24"); // C copies (21) to assay plate 3, D copies (3) to assay plate 4
        // cherry picks intended for plate 4
        addLabCherryPicks(dummyScreenerCherryPick, 6, "A01", "A08"); // both C (5) and D copies (3) to assay plate 4
        assertEquals(76, cherryPickRequest.getLabCherryPicks().size());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();
        
        cherryPickRequestAllocator.allocate(cherryPickRequest);
        cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequest);

        assertEquals("assay plates count", 4, cherryPickRequest.getCherryPickAssayPlates().size());
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 0, 21, 1);
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 22, 43, 2);
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 44, 64, 3);
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 65, 75, 4);

        CherryPickAssayPlate lastPlate = cherryPickRequest.getCherryPickAssayPlates().last();
        for (int iCol = 0; iCol < duplexLibrary.getPlateSize().getColumns(); iCol++) {
          if (iCol == 2) {
            assertColumnIsFull(cherryPickRequest, lastPlate, iCol);
          }
          else  {
            assertColumnIsEmpty(cherryPickRequest, lastPlate, iCol);
          }
        }

        assertTrue(cherryPickRequestPlateMapper.getAssayPlatesRequiringSourcePlateReload(cherryPickRequest).isEmpty());
      }
    });
  }

  /**
   * Tests the case where the wells from a single source plate are too numerous
   * to fit on even an empty assay plate (in which case we must map these wells
   * to two or more assay plates)
   */
  public void testTooManySourcePlateWellsForAssayPlate()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Library duplexLibrary = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes library", 1, 1, PlateSize.WELLS_384);
        makeLibraryCopy(duplexLibrary, "C", 10);
        genericEntityDao.saveOrUpdateEntity(duplexLibrary);
        genericEntityDao.flush(); // needed since cprAllocator.allocate() calls reload for each well, which must be in db (not just session)

        CherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(10));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(
          librariesDao.findWell(new WellKey(1, "A01")));
        cherryPickRequest.setRandomizedAssayPlateLayout(false);
        Set<Integer> emptyColumns = new HashSet<Integer>();
        emptyColumns.addAll(Arrays.asList(/*2,*/ 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 21));
        cherryPickRequest.addEmptyWellsOnAssayPlate(makeEmptyWellsFromColumnsAndRows(emptyColumns,
                                                                                     Lists.<Integer>newArrayList(),
                                                                                     duplexLibrary));
        addLabCherryPicks(dummyScreenerCherryPick, 1, "A01", "B04"); // enough to fill 2 assay plates completely, plus a 3rd, partially
        assertEquals(28, cherryPickRequest.getLabCherryPicks().size());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();

        cherryPickRequestAllocator.allocate(cherryPickRequest);
        cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequest);

        assertEquals("assay plates count", 3, cherryPickRequest.getCherryPickAssayPlates().size());
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 0, 11, 1);
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 12, 23, 2);
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 24, 27, 3);

        assertFalse(cherryPickRequestPlateMapper.getAssayPlatesRequiringSourcePlateReload(cherryPickRequest).isEmpty());
      }
    });
  }

  public void testRandomizedPlateMappingIsLeftConstrained()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("library", 1, 2, PlateSize.WELLS_384);
        makeLibraryCopy(library, "C", 10);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush(); // needed since cprAllocator.allocate() calls reload for each well, which must be in db (not just session)

        CherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(10));
        ScreenerCherryPick dummyScreenerCherryPick1 = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        ScreenerCherryPick dummyScreenerCherryPick2 = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(2, "A01")));
        cherryPickRequest.setRandomizedAssayPlateLayout(true);
        Set<Integer> emptyColumns = Sets.newHashSet(3);
        cherryPickRequest.addEmptyWellsOnAssayPlate(makeEmptyWellsFromColumnsAndRows(emptyColumns, Sets.<Integer>newHashSet(), library));
        addLabCherryPicks(dummyScreenerCherryPick1, 1, "A01", "C24"); // create 72 cherry picks, to fill exactly 6 left-most available columns
        addLabCherryPicks(dummyScreenerCherryPick2, 2, "A01", "J12"); // create 228 cherry picks, to create an indivisible block of cherry picks that must be mapped to next plate
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();

        cherryPickRequestAllocator.allocate(cherryPickRequest);
        cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequest);

        assertEquals("assay plates count", 2, cherryPickRequest.getCherryPickAssayPlates().size());
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 0, 71, 1);
        assertColumnIsEmpty(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 0);
        assertColumnIsEmpty(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 1);
        assertColumnIsFull(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 2);
        assertColumnIsEmpty(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 3);
        assertColumnIsFull(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 4);
        assertColumnIsFull(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 5);
        assertColumnIsFull(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 6);
        assertColumnIsFull(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 7);
        assertColumnIsFull(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), 8);
        for (int iCol = 9; iCol < library.getPlateSize().getColumns(); ++iCol) {
          assertColumnIsEmpty(cherryPickRequest, cherryPickRequest.getCherryPickAssayPlates().first(), iCol);
        }
      }
    });
  }

  /**
   * Test that all available wells on a cherry pick plate are being assigned to
   * exactly once. By "available wells" we mean any well that is not in the
   * "required empty columns", "required empty rows", "screener-requested
   * empty columns", and "screener-requested empty rows" sets.
   */
  public void testCherryPickPlateIsFullyUtilized()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        makeLibraryCopy(library, "C", 10);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush(); // needed since cprAllocator.allocate() calls reload for each well, which must be in db (not just session)

        RNAiCherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(10));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        cherryPickRequest.setRandomizedAssayPlateLayout(true);
        Set<Integer> emptyColumns = Sets.newHashSet();
        emptyColumns.add(2);
        Set<Integer> emptyRows = Sets.newHashSet();
        emptyRows.add(2);
        cherryPickRequest.addEmptyWellsOnAssayPlate(makeEmptyWellsFromColumnsAndRows(emptyColumns, emptyRows, library));
        addLabCherryPicks(dummyScreenerCherryPick, 1, "A01", "I17"); // create 209 cherry picks, to fill all available wells on cherry pick plate
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();

        cherryPickRequestAllocator.allocate(cherryPickRequest);
        cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequest);


        TreeSet<LabCherryPick> sortedLabCherryPicks = new TreeSet<LabCherryPick>(new Comparator<LabCherryPick>() {
          public int compare(LabCherryPick o1, LabCherryPick o2) {
            return o1.getAssayPlateWellName().getName().compareTo(o2.getAssayPlateWellName().getName());
          }});
        sortedLabCherryPicks.addAll(cherryPickRequest.getLabCherryPicks());

        assertEquals("assay plates count", 1, cherryPickRequest.getCherryPickAssayPlates().size());
        assertLabCherryPicksOnAssayPlate(cherryPickRequest, 0, 209 - 1, 1);
        Set<WellName> usedWellNames = new HashSet<WellName>();
        for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
          log.debug("testing " + labCherryPick);
          assertEquals("lab cherry pick assigned to assay plate 0",
                       0,
                       labCherryPick.getAssayPlate().getPlateOrdinal().intValue());
          assertNotNull("lab cherry pick assigned to row",
                        labCherryPick.getAssayPlateRow());
          assertNotNull("lab cherry pick assigned to column",
                        labCherryPick.getAssayPlateColumn());
          assertFalse("lab cherry pick not assigned to a requested empty well",
                      cherryPickRequest.getEmptyWellsOnAssayPlate().contains(new WellName(labCherryPick.getAssayPlateRow(),
                                                                                          labCherryPick.getAssayPlateColumn())));
          assertNotNull("lab cherry pick assigned to unused well",
                        usedWellNames.contains(labCherryPick.getAssayPlateWellName()));
          usedWellNames.add(labCherryPick.getAssayPlateWellName());
        }
        // test again, for good measure, via alternate method
        CherryPickAssayPlate plate = cherryPickRequest.getCherryPickAssayPlates().first();
        for (int colIndex = 3; colIndex <= 21; colIndex++) {
          assertColumnIsFull(cherryPickRequest, plate, colIndex);
        }
        assertColumnIsEmpty(cherryPickRequest, plate, 0);
        assertColumnIsEmpty(cherryPickRequest, plate, 1);
        assertColumnIsEmpty(cherryPickRequest, plate, 2);
        assertColumnIsEmpty(cherryPickRequest, plate, 22);
        assertColumnIsEmpty(cherryPickRequest, plate, 23);
      }
    });
  }
  
  public void testCherryPickPlatesWithOnlyEmptyWells()
  {
    final CherryPickRequest[] cherryPickRequestOut = new CherryPickRequest[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("library", 1, 1, PlateSize.WELLS_384);
        makeLibraryCopy(library, "C", 10);
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.flush(); // needed since cprAllocator.allocate() calls reload for each well, which must be in db (not just session)

        RNAiCherryPickRequest cherryPickRequest = CherryPickRequestAllocatorTest.createRNAiCherryPickRequest(1, new Volume(10));
        ScreenerCherryPick dummyScreenerCherryPick = cherryPickRequest.createScreenerCherryPick(librariesDao.findWell(new WellKey(1, "A01")));
        cherryPickRequest.addEmptyWellsOnAssayPlate(makeEmptyWellsFromColumnsAndRows(Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21), null, library));
        addLabCherryPicks(dummyScreenerCherryPick, 1, "A01", "A01"); // create 1 cherry pick
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();

        cherryPickRequestAllocator.allocate(cherryPickRequest);
        cherryPickRequestOut[0] = cherryPickRequest;
      }
    });
    
    try {
      cherryPickRequestPlateMapper.generatePlateMapping(cherryPickRequestOut[0]);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      // success!
    }
  }

  private void assertColumnIsEmpty(CherryPickRequest cherryPickRequest, CherryPickAssayPlate assayPlate, int columnIndex)
  {
    for (LabCherryPick cherryPick : assayPlate.getLabCherryPicks()) {
      if (cherryPick.getAssayPlateColumn() == columnIndex) {
        fail("column " + columnIndex + " is empty on plate " + assayPlate.getName());
      }
    }
  }

  private void assertColumnIsFull(CherryPickRequest cherryPickRequest, CherryPickAssayPlate assayPlate, int columnIndex)
  {
    Set<WellName> expectedUsedWellNames = new HashSet<WellName>();
    for (int rowIndex = 0; rowIndex < assayPlate.getAssayPlateType().getPlateSize().getRows(); ++rowIndex) {
      WellName wellName = new WellName(rowIndex, columnIndex);
      if (!cherryPickRequest.getEmptyWellsOnAssayPlate().contains(wellName)) {
        expectedUsedWellNames.add(wellName);
      }
    }

    for (LabCherryPick labCherryPick : assayPlate.getLabCherryPicks()) {
      expectedUsedWellNames.remove(labCherryPick.getAssayPlateWellName());
    }

    assertEquals("column " + columnIndex + " is full on plate " + assayPlate.getName(),
               Collections.<WellName>emptySet(),
               expectedUsedWellNames);
  }

  private void addLabCherryPicks(ScreenerCherryPick screenerCherryPick,
                                 int libraryPlateNumber,
                                 String firstWellNameStr,
                                 String lastWellNameStr)
  {
    WellName firstWellName = new WellName(firstWellNameStr);
    WellName lastWellName = new WellName(lastWellNameStr);
    PlateSize plateSize = screenerCherryPick.getScreenedWell().getLibrary().getPlateSize();
    for (int iRow = firstWellName.getRowIndex(); iRow <= lastWellName.getRowIndex(); ++iRow) {
      int iColFirst;
      if (iRow == firstWellName.getRowIndex()) {
        iColFirst = firstWellName.getColumnIndex();
      }
      else {
        iColFirst = 0;
      }
      int iColLast;
      if (iRow == lastWellName.getRowIndex()) {
        iColLast = lastWellName.getColumnIndex();
      }
      else {
        iColLast = plateSize.getColumns() - 1;
      }
      for (int iCol = iColFirst; iCol <= iColLast; ++iCol) {
        screenerCherryPick.createLabCherryPick(librariesDao.findWell(new WellKey(libraryPlateNumber, new WellName(iRow, iCol))));
      }
    }
  }


  // private methods

  /**
   * @param expectedAssayPlateNumber 1-based plate number
   */
  private void assertLabCherryPicksOnAssayPlate(CherryPickRequest cherryPickRequest,
                                                int firstIndex,
                                                int lastIndex,
                                                int expectedAssayPlateNumber)
  {
    TreeSet<LabCherryPick> sortedCherryPicks = new TreeSet<LabCherryPick>(PlateMappingCherryPickComparator.getInstance());
    sortedCherryPicks.addAll(cherryPickRequest.getLabCherryPicks());
    List<LabCherryPick> indexedCherryPicks = new ArrayList<LabCherryPick>(sortedCherryPicks);
    for (int index = firstIndex; index <= lastIndex; index++) {
      String expectedAssayPlateName = String.format("Cherry Picker_%d (%d) CP%d  Plate %02d of %d",
                                                    cherryPickRequest.getScreen().getScreenNumber(),
                                                    cherryPickRequest.getScreen().getScreenNumber(),
                                                    cherryPickRequest.getEntityId(),
                                                    expectedAssayPlateNumber,
                                                    cherryPickRequest.getCherryPickAssayPlates().size());
      LabCherryPick cherryPick = indexedCherryPicks.get(index);
      if (log.isDebugEnabled()) {
        log.debug("labCherryPick #" + index +
                  " source_copy=" + cherryPick.getSourceCopy().getName() +
                  " source_well=" + cherryPick.getSourceWell() +
                  " plate=" + cherryPick.getAssayPlate().getPlateOrdinal() +
                  " well=" + cherryPick.getAssayPlateWellName());
      }
      assertEquals("cherry pick #" + index + " assay plate name",
                   expectedAssayPlateName,
                   cherryPick.getAssayPlate().getName());
    }
  }

  /**
   * Note: adds CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME to volume!
   * @param library
   * @param copyName
   * @param volume
   */
  private void makeLibraryCopy(Library library, String copyName, int volume)
  {
    Copy copy = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, copyName);
    for (int plateNumber = library.getStartPlate(); plateNumber <= library.getEndPlate(); plateNumber++) {
      copy.createCopyInfo(plateNumber, "<loc>", PlateType.EPPENDORF, new Volume(volume).add(CherryPickRequestAllocator.MINIMUM_SOURCE_WELL_VOLUME));
    }
  }

  private Set<WellName> makeEmptyWellsFromColumnsAndRows(Collection<Integer> emptyColumns,
                                                         Collection<Integer> emptyRows, 
                                                         Library library)
  {
    Set<WellName> emptyWells = Sets.newHashSet(PlateSize.WELLS_384.getEdgeWellNames(2));
    if (emptyColumns != null) {
      for (Integer emptyColumn : emptyColumns) {
        for (int emptyRow = 0; emptyRow <= library.getPlateSize().getRows(); ++emptyRow) {
          emptyWells.add(new WellName(emptyRow, emptyColumn));
        }
      }
    }
    if (emptyRows != null) {
      for (int emptyColumn = 0; emptyColumn <= library.getPlateSize().getColumns(); ++emptyColumn) {
        for (Integer emptyRow : emptyRows) {
          emptyWells.add(new WellName(emptyRow, emptyColumn));
        }
      }
    }
    return emptyWells;
  }

}

