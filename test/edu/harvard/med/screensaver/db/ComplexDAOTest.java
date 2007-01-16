// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.screendb.ScreenDBDataImporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link SimpleDAOTest}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ComplexDAOTest extends AbstractSpringTest
{
  
  private static final Logger log = Logger.getLogger(ComplexDAOTest.class);
  
  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ComplexDAOTest.class);
  }

  
  // protected instance fields
  
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected DAO dao;
  
  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  protected ScreenDBDataImporter screenDBDataImporter;
  
  // AbstractDependencyInjectionSpringContextTests methods

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }

  
  // JUnit test methods 
  
  public void testCreateAndModifyCompound()
  {
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Compound compound = dao.defineEntity(Compound.class, "compound P");
          compound.setChembankId("P");
        }
      });

    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = dao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("chembank id", "P", compound.getChembankId());
          compound.setChembankId("P'");
        }
      });

    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = dao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("chembank id modified", "P'", compound.getChembankId());
        }
      });
  }
  
  public void testCreateLibraryWellCompound()
  {
    // create a new well, add compound p to it
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = new Library(
            "library Q",
            "Q",
            ScreenType.SMALL_MOLECULE,
            LibraryType.KNOWN_BIOACTIVES,
            1,
            2);
          Compound compound = dao.defineEntity(
            Compound.class,
            "compound P");
          compound.setChembankId("P");
          Well well = dao.defineEntity(
            Well.class,
            library,
            27,
            "A01");
          well.addCompound(compound);
        }
      });
    
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = dao.findEntityByProperty(
            Library.class,
            "libraryName",
            "library Q");
          assertEquals("Library's Well count", 1, library.getWells().size());
          assertEquals("library has type", LibraryType.KNOWN_BIOACTIVES, library.getLibraryType());
          Well well = library.getWells().iterator().next();
          Compound compound = dao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertEquals("library has well", "A01", well.getWellName());
          assertEquals("Well's Compound count", 1, well.getCompounds().size());
          assertEquals("Compound's Well count", 1, compound.getWells().size());
          assertEquals("Well-Compound association", "compound P", well.getCompounds().iterator().next().getSmiles());
          assertEquals("Compound-Well association", "A01", compound.getWells().iterator().next().getWellName());
      }
    });
  }
  
  /**
   * Tests whether a Well's compounds can be modified after it has been loaded
   * from the database. (This is more a test of Hibernate than of our
   * application.)
   */
  public void testCreateWellModifyLater() 
  {
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = new Library(
            "library Q",
            "Q",
            ScreenType.SMALL_MOLECULE,
            LibraryType.KNOWN_BIOACTIVES,
            1,
            2);
          dao.persistEntity(library);
          dao.defineEntity(Well.class, library, 27, "A01");
        }
      });
    
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = dao.findEntityByProperty(Library.class, "libraryName", "library Q");
          Well well = library.getWells().iterator().next();
          Compound compound = dao.defineEntity(Compound.class, "compound P");
          compound.setChembankId("P");
          well.addCompound(compound);
        }
      });
    
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = dao.findEntityByProperty(Library.class, "libraryName", "library Q");
          Well well = library.getWells().iterator().next();
          assertTrue(well.getCompounds().contains(new Compound("compound P")));
        }
      });
  }

  public void testTransactionRollback()
  {
    try {
      dao.doInTransaction(new DAOTransaction()
        {
          public void runTransaction()
          {
            Library library = dao.defineEntity(
              Library.class,
              "library Q",
              "Q",
              LibraryType.KNOWN_BIOACTIVES,
              1,
              2);
            dao.defineEntity(Well.class, library, 27, "A01");
            dao.defineEntity(Well.class, library, 27, "A02");
            dao.defineEntity(Well.class, library, 27, "A03");
            throw new RuntimeException("fooled ya!");
          }
        });
      fail("exception thrown from transaction didnt come thru");
    }
    catch (Exception e) {
    }
    assertNull(dao.findEntityByProperty(Library.class, "libraryName", "library Q"));
  }    

  public void testTransactionCommit()
  {
    try {
      dao.doInTransaction(new DAOTransaction()
        {
          public void runTransaction()
          {
            Library library = new Library(
              "library Q",
              "Q",
              ScreenType.SMALL_MOLECULE,
              LibraryType.KNOWN_BIOACTIVES,
              1,
              2);
            dao.persistEntity(library);
            dao.defineEntity(Well.class, library, 27, "A01");
            dao.defineEntity(Well.class, library, 27, "A02");
            dao.defineEntity(Well.class, library, 27, "A03");
          }
        });
    }
    catch (Exception e) {
      fail("unexpected exception e");
    }
    
    try {
      dao.doInTransaction(new DAOTransaction()
        {
          public void runTransaction()
          {
            Library library = dao.findEntityByProperty(
              Library.class,
              "libraryName",
              "library Q");
            assertEquals("commit of all Wells", 3, library.getWells().size());
          }
        });
    }
    catch (Exception e) {
      fail("unexpected exception e");
    }
  }
  
  
  public void testScreenResults() 
  {
    final int replicates = 2;
    
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult(new Date());
          ResultValueType[] rvt = new ResultValueType[replicates];
          for (int i = 0; i < replicates; i++) {
            rvt[i] = new ResultValueType(
              screenResult,
              "rvt" + i,
              i + 1,
              false,
              false,
              false,
              "human");
            rvt[i].setAssayReadoutType(i % 2 == 0 ? AssayReadoutType.PHOTOMETRY: AssayReadoutType.FLUORESCENCE_INTENSITY);
            rvt[i].setActivityIndicatorType(i % 2 == 0 ? ActivityIndicatorType.BOOLEAN: ActivityIndicatorType.PARTITION);
            rvt[i].setIndicatorDirection(i % 2 == 0 ? IndicatorDirection.LOW_VALUES_INDICATE : IndicatorDirection.HIGH_VALUES_INDICATE);
          }
          
          Library library = new Library(
            "library with results", 
            "lwr", 
            ScreenType.SMALL_MOLECULE,
            LibraryType.COMMERCIAL,
            1, 
            1);
          dao.persistEntity(library);
          Well[] wells = new Well[3];
          for (int iWell = 0; iWell < wells.length; ++iWell) {
            wells[iWell] = dao.defineEntity(
              Well.class,
              library,
              ( iWell / 2 ) + 1,
              String.format("%c%02d", 
                            Well.MIN_WELL_ROW + ((iWell / Well.PLATE_ROWS) + 1), 
                            (iWell % Well.PLATE_COLUMNS) + 1));
            for (int iResultValue = 0; iResultValue < rvt.length; ++iResultValue) {
              rvt[iResultValue].addResultValue(wells[iWell], 
                                               AssayWellType.EXPERIMENTAL, 
                                               "value " + iWell + "," + iResultValue, 
                                               iWell % 2 == 1);
            }
          }

          // test the calculation of replicateCount from child ResultValueTypes,
          // before setReplicate() is called by anyone
          assertEquals(replicates, screenResult.getReplicateCount().intValue());
          
          SortedSet<Integer> expectedPlateNumbers = new TreeSet<Integer>();
          expectedPlateNumbers.add(1);
          expectedPlateNumbers.add(2);
          assertEquals(expectedPlateNumbers, screenResult.getPlateNumbers());
          
          dao.persistEntity(screenResult);
        }

      });

    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = dao.findEntityByProperty(
            Library.class,
            "libraryName",
            "library with results");
          Set<Well> wells = library.getWells();
          Set<WellKey> wellKeys = new HashSet<WellKey>();
          for (Well well : wells) {
            wellKeys.add(well.getWellKey());
          }
          ScreenResult screenResult =
            dao.findAllEntitiesWithType(ScreenResult.class).get(0);
          assertEquals(replicates,screenResult.getReplicateCount().intValue());
          int iResultValue = 0;
          SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
          assertEquals(2, replicates);
          for (ResultValueType rvt : resultValueTypes) {
            assertEquals(
              screenResult,
              rvt.getScreenResult());
            assertEquals(
              iResultValue % 2 == 0 ? AssayReadoutType.PHOTOMETRY : AssayReadoutType.FLUORESCENCE_INTENSITY,
              rvt.getAssayReadoutType());
            assertEquals(
              iResultValue % 2 == 0 ? ActivityIndicatorType.BOOLEAN: ActivityIndicatorType.PARTITION,
              rvt.getActivityIndicatorType());
            assertEquals(
              iResultValue % 2 == 0 ? IndicatorDirection.LOW_VALUES_INDICATE : IndicatorDirection.HIGH_VALUES_INDICATE,
              rvt.getIndicatorDirection());
            assertEquals(
              "human",
              rvt.getAssayPhenotype());
            
            Map<WellKey,ResultValue> resultValues = rvt.getResultValues();
            for (WellKey wellKey : resultValues.keySet()) {
              assertTrue(wellKeys.contains(wellKey));
              // note that our naming scheme is testing the ordering of the
              // ResultValueType and ResultValue entities (within their parent
              // sets)
              ResultValue rv = resultValues.get(wellKey);
              assertEquals("value " + wellKey.getColumn() + "," + iResultValue, rv.getValue());
              assertEquals(wellKey.getColumn() % 2 == 1, rv.isExclude());
            }
            iResultValue++;
          }
        }
      });
  }
  
  public void testDerivedScreenResults() 
  {
    final int replicates = 3;
    final SortedSet<ResultValueType> derivedRvtSet1 = new TreeSet<ResultValueType>();
    final SortedSet<ResultValueType> derivedRvtSet2 = new TreeSet<ResultValueType>();
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult(new Date());
          
          for (int i = 0; i < replicates; i++) {
            ResultValueType rvt = new ResultValueType(
              screenResult,
              "rvt" + i,
              1,
              false,
              false,
              false,
              "human");
            derivedRvtSet1.add(rvt);
            if (i % 2 == 0) {
              derivedRvtSet2.add(rvt);
            }
          }
          ResultValueType derivedRvt1 = new ResultValueType(
            screenResult,
            "derivedRvt1",
            1,
            false,
            false,
            false,
            "human");
          for (ResultValueType resultValueType : derivedRvtSet1) {
            derivedRvt1.addTypeDerivedFrom(resultValueType);
          }
          
          ResultValueType derivedRvt2 = new ResultValueType(
            screenResult,
            "derivedRvt2",
            1,
            false,
            false,
            false,
            "human");
          for (ResultValueType resultValueType : derivedRvtSet2) {
            derivedRvt2.addTypeDerivedFrom(resultValueType);
          }
          
          dao.persistEntity(screenResult);
      }
    });
    
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          List<ScreenResult> screenResults = dao.findAllEntitiesWithType(ScreenResult.class); 
          ScreenResult screenResult = screenResults.get(0);
          SortedSet<ResultValueType> resultValueTypes =
            new TreeSet<ResultValueType>(screenResult.getResultValueTypes());
          
          ResultValueType derivedRvt = resultValueTypes.last();
          Set<ResultValueType> derivedFromSet = derivedRvt.getTypesDerivedFrom();
          assertEquals(derivedRvtSet2, derivedFromSet);
          
          resultValueTypes.remove(derivedRvt);
          derivedRvt = resultValueTypes.last();
          derivedFromSet = derivedRvt.getTypesDerivedFrom();
          assertEquals(derivedRvtSet1, derivedFromSet);
        }
      });
  }
  
  public void testFindLabHeads()
  {
    final Collection<ScreeningRoomUser> expectedLabHeads = 
      new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreeningRoomUser user1 = dao.defineEntity(ScreeningRoomUser.class,
                                                   new Date(),
                                                   "first1",
                                                   "last1",
                                                   "email1@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCB_FELLOW,
                                                   false);
        ScreeningRoomUser user2 = dao.defineEntity(ScreeningRoomUser.class,
                                                   new Date(),
                                                   "first2",
                                                   "last2",
                                                   "email2@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCB_FELLOW,
                                                   false);
        ScreeningRoomUser user3 = dao.defineEntity(ScreeningRoomUser.class,
                                                   new Date(),
                                                   "first3",
                                                   "last3",
                                                   "email3@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCB_FELLOW,
                                                   false);
        ScreeningRoomUser user4 = dao.defineEntity(ScreeningRoomUser.class,
                                                   new Date(),
                                                   "first4",
                                                   "last4",
                                                   "email4@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCB_FELLOW,
                                                   false);
        ScreeningRoomUser user5 = dao.defineEntity(ScreeningRoomUser.class,
                                                   new Date(),
                                                   "first5",
                                                   "last5",
                                                   "email5@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCB_FELLOW,
                                                   false);
        ScreeningRoomUser user6 = dao.defineEntity(ScreeningRoomUser.class,
                                                   new Date(),
                                                   "first6",
                                                   "last6",
                                                   "email6@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCB_FELLOW,
                                                   false);
        user2.setLabHead(user1);
        user3.setLabHead(user1);
        user5.setLabHead(user4);
        expectedLabHeads.add(user1);
        expectedLabHeads.add(user4);
        expectedLabHeads.add(user6);
      }
    });

    List<ScreeningRoomUser> actualLabHeads = dao.findAllLabHeads();
    assertTrue(expectedLabHeads.containsAll(actualLabHeads) && actualLabHeads.containsAll(expectedLabHeads));
  }
  
  // TODO: this test needs to be updated to include an *imported* screen result,
  // with ResultValueTypes and ResultValues, which are linked to Wells.
  // Otherwise we're testing fluff!
  public void testDeleteScreenResult()
  {
    final int[] screenResultIds = new int[1];

    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = ScreenResultParser.makeDummyScreen(1); 
        new ScreenResult(screen1, new Date());
        dao.persistEntity(screen1);
      }
    });

    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        assertNotNull("screen1 has screen result initially", screen1.getScreenResult());
        screenResultIds[0] = screen1.getScreenResult().getEntityId();
        dao.deleteScreenResult(screen1.getScreenResult());
        assertNull("screen1 has no screen result after delete from screen, but before commit", screen1.getScreenResult());
      }
    });
    
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        assertNull("screen1 has no screen result after delete and commit", screen1.getScreenResult());

        ScreenResult screenResult1 = dao.findEntityById(ScreenResult.class, screenResultIds[0]);
        assertNull("screenResult1 was deleted from database", screenResult1);
      }
    });
  }
  
  /**
   * A ScreenResult's plateNumbers, wells, experimentWellCount, and hits
   * properties should be updated when a ResultValue is added to a
   * ScreenResult's ResultValueType.
   */
  public void testScreenResultUpdates()
  {
    final SortedSet<Integer> expectedPlateNumbers = new TreeSet<Integer>();
    final SortedSet<Well> expectedWells = new TreeSet<Well>();
    final int[] expectedExperimentalWellCount = new int[1];
    final int[] expectedHits = new int[1];
    final double indicatorCutoff = 5.0;
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = ScreenResultParser.makeDummyScreen(1); 
        ScreenResult screenResult = new ScreenResult(screen, new Date());
        ResultValueType rvt = new ResultValueType(screenResult, "Raw Value");
        rvt.setActivityIndicator(true);
        rvt.setActivityIndicatorType(ActivityIndicatorType.NUMERICAL);
        rvt.setIndicatorCutoff(indicatorCutoff);
        rvt.setIndicatorDirection(IndicatorDirection.HIGH_VALUES_INDICATE);
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        for (int i = 1; i <= 10; ++i) {
          int plateNumber = i;
          expectedPlateNumbers.add(i);
          Well well = new Well(library, plateNumber, "A01");
          expectedWells.add(well);
          AssayWellType assayWellType = i % 2 == 0 ? AssayWellType.EXPERIMENTAL : AssayWellType.ASSAY_POSITIVE_CONTROL;
          boolean exclude = i % 4 == 0;
          rvt.addResultValue(well, assayWellType, (double) i, 3, false);
          if (assayWellType.equals(AssayWellType.EXPERIMENTAL)) {
            expectedExperimentalWellCount[0]++;
            if (!exclude && i >= indicatorCutoff) {
              ++expectedHits[0];
            }
          }

        }
        dao.persistEntity(screen);
      }
    });
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        assertEquals("plate numbers", expectedPlateNumbers, screen.getScreenResult().getPlateNumbers());
        assertEquals("wells", expectedWells, screen.getScreenResult().getWells());
        assertEquals("experimental well count", expectedExperimentalWellCount[0], screen.getScreenResult().getExperimentalWellCount());
      }
    });
  }
  
  public void testFindSortedResultValueTableByRange()
  {
    final Screen screen = ScreenResultParser.makeDummyScreen(1); 
    ScreenResult screenResult = new ScreenResult(screen, new Date());
    ResultValueType rvt1 = new ResultValueType(screenResult, "Raw Value");
    ResultValueType rvt2 = new ResultValueType(screenResult, "Derived Value");
    rvt2.setActivityIndicator(true);
    rvt2.setActivityIndicatorType(ActivityIndicatorType.PARTITION);
    rvt2.setDerived(true);
    rvt2.setHowDerived("even wells are 'S', otherwise 'W'");
    rvt2.addTypeDerivedFrom(rvt1);
    Library library = new Library(
      "library 1",
      "lib1",
      ScreenType.SMALL_MOLECULE,
      LibraryType.COMMERCIAL,
      1,
      1);
    for (int iPlate = 1; iPlate <= 10; ++iPlate) {
      int plateNumber = iPlate;
      for (int iWell = 1; iWell <= 10; ++iWell) {
        Well well = new Well(library, plateNumber, "A" + iWell);
        AssayWellType assayWellType = iPlate == 10 ? AssayWellType.LIBRARY_CONTROL : AssayWellType.EXPERIMENTAL;
        rvt1.addResultValue(well, assayWellType, (double) iWell, 0, false);
        rvt2.addResultValue(well, assayWellType, iWell % 2 == 0 ? "S" : "W", false);
      }
    }
    dao.persistEntity(screen);

    // test sort by 2nd RVT, ascending
    Map<WellKey,List<ResultValue>> result = 
      dao.findSortedResultValueTableByRange(Arrays.asList(rvt1, rvt2), 
                                            1, 
                                            SortDirection.ASCENDING, 
                                            10, 
                                            80,
                                            null);
    for (Map.Entry<WellKey,List<ResultValue>> entry : result.entrySet()) {
      log.debug(entry.getKey() + " => " + entry.getValue());
    }
    assertEquals("result size", 80, result.size());
    int iWell = 10;
    for (Map.Entry<WellKey,List<ResultValue>> entry : result.entrySet()) {
      assertEquals("sorted result values for " + entry.getKey() + " at sort index " + iWell, 
                   iWell < 50 ? "S" : "W", 
                   entry.getValue().get(1).getValue());
      assertEquals("associated result value for " + entry.getKey(), 
                   Integer.toString(entry.getKey().getColumn() + 1), 
                   entry.getValue().get(0).getValue());
      ++iWell;
    }
    
    // test sort by wellname, plate number ascending
    result = 
      dao.findSortedResultValueTableByRange(Arrays.asList(rvt1, rvt2),
                                            DAO.SORT_BY_WELL_PLATE,
                                            SortDirection.ASCENDING, 
                                            0, 
                                            100,
                                            null);
    iWell = 0;
    for (Map.Entry<WellKey,List<ResultValue>> entry : result.entrySet()) {
      int expectedWellColumn = (iWell / 10);
      assertEquals("sort by well, plate ascending: well", 
                   expectedWellColumn,
                   entry.getKey().getColumn());
      int expectedPlateNumber = (iWell % 10) + 1;
      assertEquals("sort by well, plate ascending: plate", 
                   expectedPlateNumber,
                   entry.getKey().getPlateNumber());
      ++iWell;
    }
    
    // test sort by assayWellType descending
    result = 
      dao.findSortedResultValueTableByRange(Arrays.asList(rvt1, rvt2),
                                            DAO.SORT_BY_ASSAY_WELL_TYPE, 
                                            SortDirection.DESCENDING, 
                                            0, 
                                            11,
                                            null);
    for (Map.Entry<WellKey,List<ResultValue>> entry : result.entrySet()) {
      log.debug(entry.getKey() + " " + entry.getValue().get(0).getAssayWellType());
    }
    iWell = 0;
    for (Map.Entry<WellKey,List<ResultValue>> entry : result.entrySet()) {
      assertEquals("sort by assayWellType", 
                   iWell < 10 ? AssayWellType.LIBRARY_CONTROL : AssayWellType.EXPERIMENTAL,
                   entry.getValue().get(0).getAssayWellType());
      ++iWell;
    }
    
  }
  
  public void testFindResultValuesByPlate()
  {
    final Screen screen = ScreenResultParser.makeDummyScreen(1); 
    ScreenResult screenResult = new ScreenResult(screen, new Date());
    ResultValueType rvt = new ResultValueType(screenResult, "Raw Value");
    Library library = new Library(
      "library 1",
      "lib1",
      ScreenType.SMALL_MOLECULE,
      LibraryType.COMMERCIAL,
      1,
      1);
    for (int iPlate = 1; iPlate <= 3; ++iPlate) {
      int plateNumber = iPlate;
      for (int iWell = 0; iWell < 10; ++iWell) {
        Well well = new Well(library, plateNumber, "A" + (iWell + 1));
        rvt.addResultValue(well, Integer.toString(iWell));
      }
    }
    dao.persistEntity(screen);

    Map<WellKey,ResultValue> resultValues = dao.findResultValuesByPlate(2, rvt);
    assertEquals("result values size", 10, resultValues.size());
    for (int iWell = 0; iWell < 10; ++iWell) {
      ResultValue rv = resultValues.get(new WellKey(2, 0, iWell));
      assertEquals("rv.value", Integer.toString(iWell), rv.getValue());
    }
  }
  
  public void testEntityInflation()
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = ScreenResultParser.makeDummyScreen(1); 
        ScreeningRoomUser labMember = new ScreeningRoomUser(new Date(),
                                                            "Lab",
                                                            "Member",
                                                            "lab_member@hms.harvard.edu",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            "",
                                                            ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                            false);
        screen.getLabHead().addLabMember(labMember);
        screen.addKeyword("keyword1");
        screen.addKeyword("keyword2");
        dao.persistEntity(screen);
      }
    });

    final Screen[] screenOut = new Screen[1];
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        dao.need(screen, 
                 "keywords", 
                 "hbnLabHead.hbnLabMembers",
                 "hbnCollaborators.hbnLabHead"); // tests DAOImpl.verifyEntityRelationshipExists()
        screenOut[0] = screen;
      }
    });
    
    // note: the Hibernate session/txn *must* be closed before we can make our assertions
    Screen screen = screenOut[0];
    try {
      assertEquals("keywords size", 2, screen.getKeywords().size());
      assertEquals("labHead last name", "Screener", screen.getLabHead().getLastName());
      assertEquals("labHead.labMembers size", 1, screen.getLabHead().getLabMembers().size());
      assertEquals("labHead.labMembers[0].lastName", "Member", screen.getLabHead().getLabMembers().iterator().next().getLastName());
    }
    catch (LazyInitializationException e) {
      e.printStackTrace();
      fail("screen relationships were not initialized by dao.need(AbstractEntity, String...)");
    }
    try {
      screen.getCollaborators().iterator().next();
      fail("expected LazyInitializationException for screen.collaborators access");
    }
    catch (LazyInitializationException e) {}
  }
  
  public void testEntityInflationInvalidRelationship()
  {
    // TODO: implement, but requires that DAOImpl.need() throws an exception or assertion failure on an invalid relationship request
  }
  
  public void testRelationshipSize()
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = ScreenResultParser.makeDummyScreen(1); 
        try {
          new Publication(screen, "1", "2007", "authro1", "Title1");
          new Publication(screen, "2", "2007", "author2", "Title2");
        }
        catch (DuplicateEntityException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
        ScreeningRoomUser collab1 = new ScreeningRoomUser(new Date(),
                                                          "Col",
                                                          "Laborator",
                                                          "collab1@hms.harvard.edu",
                                                          "",
                                                          "",
                                                          "",
                                                          "",
                                                          "",
                                                          ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                          false);
        ScreeningRoomUser collab2 = new ScreeningRoomUser(new Date(),
                                                          "Col",
                                                          "Laborator",
                                                          "collab2@hms.harvard.edu",
                                                          "",
                                                          "",
                                                          "",
                                                          "",
                                                          "",
                                                          ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                          false);
        dao.persistEntity(collab1);
        dao.persistEntity(collab2);
        screen.addCollaborator(collab1);
        screen.addCollaborator(collab2);
        dao.persistEntity(screen);
      }
    });

    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        assertEquals("keywords size", 2, dao.relationshipSize(screen, "publications"));
        assertEquals("collaborators size", 2, dao.relationshipSize(screen.getHbnCollaborators()));
      }
    });
  }

}

