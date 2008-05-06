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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;


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
  protected GenericEntityDAO genericEntityDao;
  protected ScreenResultsDAO screenResultsDao;
  protected CherryPickRequestDAO cherryPickRequestDao;
  protected UsersDAO usersDao;
  protected LibrariesDAO librariesDao;
  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;


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
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Compound compound = new Compound("compound P", "inchi");
          compound.addChembankId("P");
          genericEntityDao.saveOrUpdateEntity(compound);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = genericEntityDao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("chembank id", "P", compound.getChembankIds().iterator().next());
          compound.removeChembankId("P");
          compound.addChembankId("P'");
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = genericEntityDao.findEntityByProperty(
            Compound.class,
            "smiles",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("chembank id modified", "P'", compound.getChembankIds().iterator().next());
        }
      });
  }

  public void testCreateLibraryWellCompound()
  {
    // create a new well, add compound p to it
    genericEntityDao.doInTransaction(new DAOTransaction()
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
          Compound compound = new Compound("compound P", "inchi");
          Well well = library.createWell(new WellKey(27, "A01"), WellType.EXPERIMENTAL);
          well.addCompound(compound);
          genericEntityDao.saveOrUpdateEntity(library);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(
            Library.class,
            "libraryName",
            "library Q");
          assertEquals("Library's Well count", 1, library.getWells().size());
          assertEquals("library has type", LibraryType.KNOWN_BIOACTIVES, library.getLibraryType());
          Well well = library.getWells().iterator().next();
          Compound compound = genericEntityDao.findEntityByProperty(
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
    genericEntityDao.doInTransaction(new DAOTransaction()
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
          library.createWell(new WellKey(27, "A01"), WellType.EXPERIMENTAL);
          genericEntityDao.saveOrUpdateEntity(library);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library Q");
          Well well = library.getWells().iterator().next();
          Compound compound = new Compound("compound P", "inchi");
          well.addCompound(compound);
        }
      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library Q");
          Well well = library.getWells().iterator().next();
          assertTrue(well.getCompounds().contains(new Compound("compound P", "inchi P")));
        }
      });
  }

  public void testTransactionRollback()
  {
    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
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
            library.createWell(new WellKey(27, "A01"), WellType.EXPERIMENTAL);
            library.createWell(new WellKey(27, "A02"), WellType.EXPERIMENTAL);
            library.createWell(new WellKey(27, "A03"), WellType.EXPERIMENTAL);
            genericEntityDao.saveOrUpdateEntity(library);
            throw new RuntimeException("fooled ya!");
          }
        });
      fail("exception thrown from transaction didnt come thru");
    }
    catch (Exception e) {
    }
    assertNull(genericEntityDao.findEntityByProperty(Library.class, "libraryName", "library Q"));
  }

  public void testTransactionCommit()
  {
    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
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
            library.createWell(new WellKey(27, "A01"), WellType.EXPERIMENTAL);
            library.createWell(new WellKey(27, "A02"), WellType.EXPERIMENTAL);
            library.createWell(new WellKey(27, "A03"), WellType.EXPERIMENTAL);
            genericEntityDao.saveOrUpdateEntity(library);
          }
        });
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("unexpected exception e");
    }

    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
        {
          public void runTransaction()
          {
            Library library = genericEntityDao.findEntityByProperty(
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

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult(new Date());
          ResultValueType[] rvt = new ResultValueType[replicates];
          for (int i = 0; i < replicates; i++) {
            rvt[i] = screenResult.createResultValueType(
              "rvt" + i,
              i + 1,
              false,
              false,
              false,
              "human");
            rvt[i].setAssayReadoutType(i % 2 == 0 ? AssayReadoutType.PHOTOMETRY: AssayReadoutType.FLUORESCENCE_INTENSITY);
            rvt[i].setPositiveIndicatorType(i % 2 == 0 ? PositiveIndicatorType.BOOLEAN: PositiveIndicatorType.PARTITION);
            rvt[i].setPositiveIndicatorDirection(i % 2 == 0 ? PositiveIndicatorDirection.LOW_VALUES_INDICATE : PositiveIndicatorDirection.HIGH_VALUES_INDICATE);
          }

          Library library = new Library(
            "library with results",
            "lwr",
            ScreenType.SMALL_MOLECULE,
            LibraryType.COMMERCIAL,
            1,
            1);
          Well[] wells = new Well[3];
          for (int iWell = 0; iWell < wells.length; ++iWell) {
            WellKey wellKey = new WellKey(( iWell / 2 ) + 1,
                                          String.format("%c%02d",
                                                        Well.MIN_WELL_ROW + ((iWell / Well.PLATE_ROWS) + 1),
                                                        (iWell % Well.PLATE_COLUMNS) + 1));

            wells[iWell] = library.createWell(wellKey, WellType.EXPERIMENTAL);
            for (int iResultValue = 0; iResultValue < rvt.length; ++iResultValue) {
              rvt[iResultValue].createResultValue(wells[iWell],
                                               AssayWellType.EXPERIMENTAL,
                                               "value " + iWell + "," + iResultValue,
                                               iWell % 2 == 1);
            }
          }
          genericEntityDao.saveOrUpdateEntity(library);

          // test the calculation of replicateCount from child ResultValueTypes,
          // before setReplicate() is called by anyone
          assertEquals(replicates, screenResult.getReplicateCount().intValue());

          SortedSet<Integer> expectedPlateNumbers = new TreeSet<Integer>();
          expectedPlateNumbers.add(1);
          expectedPlateNumbers.add(2);
          assertEquals(expectedPlateNumbers, screenResult.getPlateNumbers());

          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());
        }

      });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Library library = genericEntityDao.findEntityByProperty(
            Library.class,
            "libraryName",
            "library with results");
          Set<Well> wells = library.getWells();
          Set<WellKey> wellKeys = new HashSet<WellKey>();
          for (Well well : wells) {
            wellKeys.add(well.getWellKey());
          }
          ScreenResult screenResult =
            genericEntityDao.findAllEntitiesOfType(ScreenResult.class).get(0);
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
              iResultValue % 2 == 0 ? PositiveIndicatorType.BOOLEAN: PositiveIndicatorType.PARTITION,
              rvt.getPositiveIndicatorType());
            assertEquals(
              iResultValue % 2 == 0 ? PositiveIndicatorDirection.LOW_VALUES_INDICATE : PositiveIndicatorDirection.HIGH_VALUES_INDICATE,
              rvt.getPositiveIndicatorDirection());
            assertEquals(
              "human",
              rvt.getAssayPhenotype());

            Map<WellKey,ResultValue> resultValues = rvt.getWellKeyToResultValueMap();
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
    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = ScreenResultParserTest.makeScreenResult(new Date());

          for (int i = 0; i < replicates; i++) {
            ResultValueType rvt = screenResult.createResultValueType(
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
          ResultValueType derivedRvt1 = screenResult.createResultValueType(
            "derivedRvt1",
            1,
            false,
            false,
            false,
            "human");
          for (ResultValueType resultValueType : derivedRvtSet1) {
            derivedRvt1.addTypeDerivedFrom(resultValueType);
          }

          ResultValueType derivedRvt2 = screenResult.createResultValueType(
            "derivedRvt2",
            1,
            false,
            false,
            false,
            "human");
          for (ResultValueType resultValueType : derivedRvtSet2) {
            derivedRvt2.addTypeDerivedFrom(resultValueType);
          }

          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLeadScreener());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen().getLabHead());
          genericEntityDao.saveOrUpdateEntity(screenResult.getScreen());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          List<ScreenResult> screenResults = genericEntityDao.findAllEntitiesOfType(ScreenResult.class);
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
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreeningRoomUser user1 = new ScreeningRoomUser (
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
        genericEntityDao.saveOrUpdateEntity(user1);
        ScreeningRoomUser user2 = new ScreeningRoomUser (
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
        genericEntityDao.saveOrUpdateEntity(user2);
        ScreeningRoomUser user3 = new ScreeningRoomUser (
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
        genericEntityDao.saveOrUpdateEntity(user3);
        ScreeningRoomUser user4 = new ScreeningRoomUser (
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
        genericEntityDao.saveOrUpdateEntity(user4);
        ScreeningRoomUser user5 = new ScreeningRoomUser (
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
        genericEntityDao.saveOrUpdateEntity(user5);
        ScreeningRoomUser user6 = new ScreeningRoomUser (
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
        genericEntityDao.saveOrUpdateEntity(user6);
        user2.setLabHead(user1);
        user3.setLabHead(user1);
        user5.setLabHead(user4);
        expectedLabHeads.add(user1);
        expectedLabHeads.add(user4);
        expectedLabHeads.add(user6);
      }
    });

    Set<ScreeningRoomUser> actualLabHeads = usersDao.findAllLabHeads();
    assertTrue(expectedLabHeads.containsAll(actualLabHeads) && actualLabHeads.containsAll(expectedLabHeads));
  }

  public void testDeleteScreenResult()
  {
    final int[] screenResultIds = new int[1];

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.saveOrUpdateEntity(library);
        Screen screen1 = MakeDummyEntities.makeDummyScreen(1);
        MakeDummyEntities.makeDummyScreenResult(screen1, library);
        genericEntityDao.saveOrUpdateEntity(screen1.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen1.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen1);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertNotNull("screen1 has screen result initially", screen1.getScreenResult());
        screenResultIds[0] = screen1.getScreenResult().getEntityId();
        screenResultsDao.deleteScreenResult(screen1.getScreenResult());
        assertNull("screen1 has no screen result after delete from screen, but before commit", screen1.getScreenResult());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertNull("screen1 has no screen result after delete and commit", screen1.getScreenResult());

        ScreenResult screenResult1 = genericEntityDao.findEntityById(ScreenResult.class, screenResultIds[0]);
        assertNull("screenResult1 was deleted from database", screenResult1);
      }
    });
  }

  public void testDeleteCherryPickRequest()
  {
    final int screenNumber = 1;
    final CherryPickRequest cherryPickRequest = makeCherryPickRequest(screenNumber);
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
        assertEquals("screen has 1 cherry pick request before deleting cherry pick request",
                     1,
                     screen.getCherryPickRequests().size());
      }
    });

    // note: we reload to test under condition of having an entity that has not
    // had any of its lazy relationships initialized (e.g. UI reloads the
    // cherryPickRequest anew when navigating to the CherryPickRequestViewer,
    // and so screen.cherryPickRequests collection is not initialized, but is
    // needed by deleteCherryPickRequest()).
    CherryPickRequest reloadedCherryPickRequest = (CherryPickRequest) genericEntityDao.reloadEntity(cherryPickRequest);
    cherryPickRequestDao.deleteCherryPickRequest(reloadedCherryPickRequest);

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
        assertEquals("screen has no cherry pick requests", 0, screen.getCherryPickRequests().size());
        assertNull("cherry pick request deleted",
                   genericEntityDao.findEntityById(CherryPickRequest.class, cherryPickRequest.getEntityId()));
      }
    });
  }

  private CherryPickRequest makeCherryPickRequest(final int screenNumber)
  {
    final CherryPickRequest[] result = new CherryPickRequest[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library poolLibrary = new Library("Pools library 1",
                                          "poollib1",
                                          ScreenType.RNAI,
                                          LibraryType.COMMERCIAL,
                                          1,
                                          2);
        Well poolWell1 = CherryPickRequestAllocatorTest.makeRNAiWell(poolLibrary, 1, new WellName("A01"));
        Well poolWell2 = CherryPickRequestAllocatorTest.makeRNAiWell(poolLibrary, 2, new WellName("P24"));
        genericEntityDao.saveOrUpdateEntity(poolLibrary);

        Library duplexLibrary = new Library("Duplexes library 1",
                                            "duplib1",
                                            ScreenType.RNAI,
                                            LibraryType.COMMERCIAL,
                                            3,
                                            4);
        Set<Well> pool1DuplexWells = CherryPickRequestAllocatorTest.makeRNAiDuplexWellsForPoolWell(duplexLibrary, poolWell1, 3, new WellName("A01"));
        Set<Well> pool2DuplexWells = CherryPickRequestAllocatorTest.makeRNAiDuplexWellsForPoolWell(duplexLibrary, poolWell2, 4, new WellName("P24"));
        genericEntityDao.saveOrUpdateEntity(duplexLibrary);

        Screen screen = MakeDummyEntities.makeDummyScreen(screenNumber, ScreenType.RNAI);
        CherryPickRequest cherryPickRequest = screen.createCherryPickRequest();
        cherryPickRequest.createLabCherryPick(cherryPickRequest.createScreenerCherryPick(poolWell1), pool1DuplexWells.iterator().next());
        cherryPickRequest.createLabCherryPick(cherryPickRequest.createScreenerCherryPick(poolWell2), pool2DuplexWells.iterator().next());
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
        result[0] = cherryPickRequest;
      }
    });
    return result[0];
  }

  // TODO: test case where deletion not allowed
  public void testDeleteScreenerCherryPick()
  {
    final int screenNumber = 1;
    makeCherryPickRequest(screenNumber);
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
        CherryPickRequest cherryPickRequest = screen.getCherryPickRequests().iterator().next();
        assertEquals("screener cherry picks exist before delete",
                     2,
                     cherryPickRequest.getScreenerCherryPicks().size());
        assertEquals("screener cherry picks exist for well1 before delete", 1, cherryPickRequestDao.findScreenerCherryPicksForWell(librariesDao.findWell(new WellKey(1, "A01"))).size());
        assertEquals("screener cherry picks exist for well2 before delete", 1, cherryPickRequestDao.findScreenerCherryPicksForWell(librariesDao.findWell(new WellKey(2, "P24"))).size());
        Set<ScreenerCherryPick> cherryPicksToDelete = new HashSet<ScreenerCherryPick>(cherryPickRequest.getScreenerCherryPicks());
        for (ScreenerCherryPick cherryPick : cherryPicksToDelete) {
          cherryPickRequestDao.deleteScreenerCherryPick(cherryPick);
        }
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
        CherryPickRequest cherryPickRequest = screen.getCherryPickRequests().iterator().next();
        assertEquals("screener cherry picks deleted from cherry pick request", 0, cherryPickRequest.getScreenerCherryPicks().size());
        assertEquals("screener cherry picks deleted from well1", 0, cherryPickRequestDao.findScreenerCherryPicksForWell(librariesDao.findWell(new WellKey(1, "A01"))).size());
        assertEquals("screener cherry picks deleted from well2", 0, cherryPickRequestDao.findScreenerCherryPicksForWell(librariesDao.findWell(new WellKey(2, "P24"))).size());
      }
    });
  }

  // TODO: test case where deletion not allowed
  public void testDeleteLabCherryPick()
  {
    final int screenNumber = 1;
    makeCherryPickRequest(screenNumber);
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
        CherryPickRequest cherryPickRequest = screen.getCherryPickRequests().iterator().next();
        assertEquals("lab cherry picks exist before delete",
                     2,
                     cherryPickRequest.getLabCherryPicks().size());
        assertEquals("lab cherry picks exist in well1 before delete", 1, cherryPickRequestDao.findLabCherryPicksForWell(librariesDao.findWell(new WellKey(3, "A01"))).size());
        assertEquals("lab cherry picks exist in well2 before delete", 1, cherryPickRequestDao.findLabCherryPicksForWell(librariesDao.findWell(new WellKey(4, "P24"))).size());
        Set<LabCherryPick> cherryPicksToDelete = new HashSet<LabCherryPick>(cherryPickRequest.getLabCherryPicks());
        for (LabCherryPick cherryPick : cherryPicksToDelete) {
          cherryPickRequestDao.deleteLabCherryPick(cherryPick);
        }
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", screenNumber);
        CherryPickRequest cherryPickRequest = screen.getCherryPickRequests().iterator().next();
        assertEquals("lab cherry picks deleted from cherry pick request", 0, cherryPickRequest.getLabCherryPicks().size());
        assertEquals("lab cherry picks deleted from well1", 0, cherryPickRequestDao.findLabCherryPicksForWell(librariesDao.findWell(new WellKey(3, "A01"))).size());
        assertEquals("lab cherry picks deleted from well2", 0, cherryPickRequestDao.findLabCherryPicksForWell(librariesDao.findWell(new WellKey(4, "P24"))).size());
      }
    });
  }

  /**
   * A ScreenResult's plateNumbers, wells, experimentWellCount, and positives
   * properties should be updated when a ResultValue is added to a
   * ScreenResult's ResultValueType.
   */
  public void testScreenResultDerivedPersistentValues()
  {
    final SortedSet<Integer> expectedPlateNumbers = new TreeSet<Integer>();
    final SortedSet<Well> expectedWells = new TreeSet<Well>();
    final int[] expectedExperimentalWellCount = new int[1];
    final int[] expectedPositives = new int[1];
    final double indicatorCutoff = 5.0;
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        ScreenResult screenResult = screen.createScreenResult(new Date());
        ResultValueType rvt1 = screenResult.createResultValueType("RVT1", null, false, true, false, "");
        rvt1.setPositiveIndicatorType(PositiveIndicatorType.NUMERICAL);
        rvt1.setPositiveIndicatorCutoff(indicatorCutoff);
        rvt1.setPositiveIndicatorDirection(PositiveIndicatorDirection.HIGH_VALUES_INDICATE);
        rvt1.setNumeric(true);
        ResultValueType rvt2 = screenResult.createResultValueType("RVT2", null, false, true, false, "");
        rvt2.setPositiveIndicatorType(PositiveIndicatorType.BOOLEAN);
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
          Well well = library.createWell(new WellKey(plateNumber, "A01"), WellType.EXPERIMENTAL);
          expectedWells.add(well);
          AssayWellType assayWellType = i % 2 == 0 ? AssayWellType.EXPERIMENTAL : AssayWellType.ASSAY_POSITIVE_CONTROL;
          boolean exclude = i % 4 == 0;
          double rvt1Value = (double) i;
          rvt1.createResultValue(well, assayWellType, rvt1Value, 3, exclude);
          rvt2.createResultValue(well, assayWellType, "false", false);
          if (assayWellType.equals(AssayWellType.EXPERIMENTAL)) {
            expectedExperimentalWellCount[0]++;
            if (!exclude && rvt1Value >= indicatorCutoff) {
              log.debug("result value " + rvt1Value + " is deemed a positive by this test");
              ++expectedPositives[0];
            }
          }
        }
        genericEntityDao.saveOrUpdateEntity(library);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertEquals("plate numbers", expectedPlateNumbers, screen.getScreenResult().getPlateNumbers());
        assertEquals("wells", expectedWells, screen.getScreenResult().getWells());
        assertEquals("experimental well count", expectedExperimentalWellCount[0], screen.getScreenResult().getExperimentalWellCount().intValue());
        assertEquals("positives", expectedPositives[0], screen.getScreenResult().getResultValueTypesList().get(0).getPositivesCount().intValue());
        assertEquals("0 positives (but not null)", 0, screen.getScreenResult().getResultValueTypesList().get(1).getPositivesCount().intValue());
      }
    });
  }


  public void testFindResultValuesByPlate()
  {
    final Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenResult screenResult = screen.createScreenResult(new Date());
    ResultValueType rvt1 = screenResult.createResultValueType("Raw Value");
    ResultValueType rvt2 = screenResult.createResultValueType("Derived Value");
    rvt1.setNumeric(true);
    rvt2.setNumeric(true);
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
        Well well = library.createWell(new WellKey(plateNumber, "A" + (iWell + 1)), WellType.EXPERIMENTAL);
        rvt1.createResultValue(well, (double) iWell, 3);
        rvt2.createResultValue(well, iWell + 10.0, 3);
      }
    }
    genericEntityDao.saveOrUpdateEntity(library);
    genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
    genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
    genericEntityDao.saveOrUpdateEntity(screen);

    // test findResultValuesByPlate(Integer, RVT)
    Map<WellKey,ResultValue> resultValues1 = screenResultsDao.findResultValuesByPlate(2, rvt1);
    assertEquals("result values size", 10, resultValues1.size());
    for (int iWell = 0; iWell < 10; ++iWell) {
      ResultValue rv = resultValues1.get(new WellKey(2, 0, iWell));
      assertEquals("rv.value", new Double(iWell), rv.getNumericValue());
    }
  }

  public void testIsPlateRangeAvaiable()
  {
    Library library1 = new Library("library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  4);
    Library library2 = new Library("library 2",
                                  "lib2",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  10,
                                  10);
    genericEntityDao.persistEntity(library1);
    genericEntityDao.persistEntity(library2);

    assertFalse(librariesDao.isPlateRangeAvailable(-1, -1));
    assertFalse(librariesDao.isPlateRangeAvailable(-1, 0));
    assertFalse(librariesDao.isPlateRangeAvailable(0, 1));

    assertFalse(librariesDao.isPlateRangeAvailable(1, 1));
    assertFalse(librariesDao.isPlateRangeAvailable(2, 3));
    assertFalse(librariesDao.isPlateRangeAvailable(1, 4));
    assertFalse(librariesDao.isPlateRangeAvailable(3, 4));
    assertFalse(librariesDao.isPlateRangeAvailable(4, 4));
    assertFalse(librariesDao.isPlateRangeAvailable(4, 9));
    assertFalse(librariesDao.isPlateRangeAvailable(5, 10));
    assertFalse(librariesDao.isPlateRangeAvailable(4, 10));
    assertFalse(librariesDao.isPlateRangeAvailable(10, 10));
    assertFalse(librariesDao.isPlateRangeAvailable(9, 11));
    assertFalse(librariesDao.isPlateRangeAvailable(10, 12));
    assertFalse(librariesDao.isPlateRangeAvailable(9, 4));

    assertTrue(librariesDao.isPlateRangeAvailable(5, 5));
    assertTrue(librariesDao.isPlateRangeAvailable(9, 9));
    assertTrue(librariesDao.isPlateRangeAvailable(5, 9));
    assertTrue(librariesDao.isPlateRangeAvailable(9, 5));
    assertTrue(librariesDao.isPlateRangeAvailable(6, 8));
    assertTrue(librariesDao.isPlateRangeAvailable(11, 11));
    assertTrue(librariesDao.isPlateRangeAvailable(11, 100000));
  }

  public void testEntityInflation()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
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
        genericEntityDao.saveOrUpdateEntity(labMember);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    final Screen[] screenOut = new Screen[1];
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        genericEntityDao.need(screen,
                              "keywords",
                              "labHead.labMembers");
        screenOut[0] = screen;
      }
    });

    // note: the Hibernate session/txn *must* be closed before we can make our assertions
    Screen screen = screenOut[0];
    try {
      assertEquals("keywords size", 2, screen.getKeywords().size());
      assertEquals("labHead last name", "Screener_1", screen.getLabHead().getLastName());
      assertEquals("labHead.LabMembers size", 1, screen.getLabHead().getLabMembers().size());
      assertEquals("labHead.LabMembers[0].lastName", "Member", screen.getLabHead().getLabMembers().iterator().next().getLastName());
    }
    catch (LazyInitializationException e) {
      e.printStackTrace();
      fail("screen relationships were not initialized by genericEntityDao.need(AbstractEntity, String...)");
    }
    try {
      screen.getCollaborators().iterator().next();
      fail("expected LazyInitializationException for screen.collaborators access");
    }
    catch (LazyInitializationException e) {}
  }

  public void testEntityInflationInvalidRelationship()
  {
    Library library = new Library("library 1",
                                  "lib1",
                                  ScreenType.SMALL_MOLECULE,
                                  LibraryType.COMMERCIAL,
                                  1,
                                  1);
    library.createWell(new WellKey(1, "A01"), WellType.EXPERIMENTAL);
    genericEntityDao.saveOrUpdateEntity(library);
    try {
        // oops...should've been "wells"!
      genericEntityDao.need(library, "hbnWells");
      fail("invalid relationship name was not detected!");
    }
    catch (Exception e) {}
  }

  public void testRelationshipSize()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        try {
          screen.createPublication("1", "2007", "authro1", "Title1");
          screen.createPublication("2", "2007", "author2", "Title2");
        }
        catch (DuplicateEntityException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
        ScreeningRoomUser collab1 = new ScreeningRoomUser(new Date(),
                                                          "Col",
                                                          "Laborator1",
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
                                                          "Laborator2",
                                                          "collab2@hms.harvard.edu",
                                                          "",
                                                          "",
                                                          "",
                                                          "",
                                                          "",
                                                          ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                          false);
        genericEntityDao.saveOrUpdateEntity(collab1);
        genericEntityDao.saveOrUpdateEntity(collab2);
        screen.addCollaborator(collab1);
        screen.addCollaborator(collab2);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        assertEquals("publications size", 2, genericEntityDao.relationshipSize(screen.getPublications()));
        assertEquals("collaborators size", 2, genericEntityDao.relationshipSize(screen.getCollaborators()));
        // TODO: test that this relationshipSize() method is invocable outside of a Hibernate session
        assertEquals("publications size", 2, genericEntityDao.relationshipSize(screen, "publications"));
        assertEquals("collaborators size w/criteria",
                     1,
                     genericEntityDao.relationshipSize(screen, "collaborators", "lastName", "Laborator2"));
      }
    });
  }

  public void testFindDuplicateCherryPicksForScreen()
  {
    CherryPickRequest cherryPickRequest1 = makeCherryPickRequest(1);
    Screen screen = cherryPickRequest1.getScreen();

    CherryPickRequest cherryPickRequest2 = screen.createCherryPickRequest();
    Iterator<ScreenerCherryPick> scpIter = cherryPickRequest1.getScreenerCherryPicks().iterator();
    ScreenerCherryPick duplicateScreenerCherryPick1 = scpIter.next();
    cherryPickRequest2.createLabCherryPick(
      cherryPickRequest2.createScreenerCherryPick(duplicateScreenerCherryPick1.getScreenedWell()),
      duplicateScreenerCherryPick1.getLabCherryPicks().iterator().next().getSourceWell());
    genericEntityDao.saveOrUpdateEntity(screen);
    Map<WellKey,Number> duplicateCherryPickWells = cherryPickRequestDao.findDuplicateCherryPicksForScreen(screen);
    assertEquals("duplicate cherry picks count", 1, duplicateCherryPickWells.size());
    assertEquals("duplicate cherry pick well keys",
                 new HashSet<WellKey>(Arrays.asList(duplicateScreenerCherryPick1.getScreenedWell().getWellKey())),
                 duplicateCherryPickWells.keySet());

    CherryPickRequest cherryPickRequest3 = screen.createCherryPickRequest();
    ScreenerCherryPick duplicateScreenerCherryPick2 = scpIter.next();
    cherryPickRequest3.createLabCherryPick(
      cherryPickRequest3.createScreenerCherryPick(duplicateScreenerCherryPick2.getScreenedWell()),
      duplicateScreenerCherryPick2.getLabCherryPicks().iterator().next().getSourceWell());
    genericEntityDao.saveOrUpdateEntity(screen);
    duplicateCherryPickWells = cherryPickRequestDao.findDuplicateCherryPicksForScreen(screen);
    assertEquals("duplicate cherry picks count", 2, duplicateCherryPickWells.size());
    assertEquals("duplicate cherry pick well keys",
                 new HashSet<WellKey>(Arrays.asList(duplicateScreenerCherryPick1.getScreenedWell().getWellKey(), duplicateScreenerCherryPick2.getScreenedWell().getWellKey())),
                 duplicateCherryPickWells.keySet());

  }

  public void testRemainingWellVolume()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("library", 1, 2, 384);
        Copy copyC = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "C");
        copyC.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyC.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyD = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "D");
        copyD.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyD.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyE = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "E");
        copyE.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyE.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyF = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "F");
        copyF.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10));
        copyF.createCopyInfo(2, "loc1", PlateType.EPPENDORF, new Volume(100)); // should be ignored
        Copy copyG = library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "G");
        copyG.createCopyInfo(1, "loc1", PlateType.EPPENDORF, new Volume(10)).setDateRetired(new Date());

        genericEntityDao.saveOrUpdateEntity(library);

        WellVolumeCorrectionActivity wellVolumeCorrectionActivity =
          new WellVolumeCorrectionActivity(new AdministratorUser("Joe", "Admin", "joe_admin@hms.harvard.edu", "", "", "", "", ""),
                                           new Date());
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
        LabCherryPick labCherryPick1 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, wellA01);
        labCherryPick1.setAllocated(copyE);
        LabCherryPick labCherryPick2 = cherryPickRequest.createLabCherryPick(dummyScreenerCherryPick, wellB02);
        labCherryPick2.setAllocated(copyF);
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest.getScreen());
      }
    });

    Copy copyC = genericEntityDao.findEntityById(Copy.class, "library:C");
    Copy copyD = genericEntityDao.findEntityById(Copy.class, "library:D");
    Copy copyE = genericEntityDao.findEntityById(Copy.class, "library:E");
    Copy copyF = genericEntityDao.findEntityById(Copy.class, "library:F");
    Copy copyG = genericEntityDao.findEntityById(Copy.class, "library:G");
    Well wellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
    Well wellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
    Well wellC03 = genericEntityDao.findEntityById(Well.class, "00001:C03");

    assertEquals("C:A01", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellA01, copyC));
    assertEquals("C:B02", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellB02, copyC));
    assertEquals("C:C03", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellC03, copyC));
    assertEquals("D:A01", new Volume(9),  librariesDao.findRemainingVolumeInWellCopy(wellA01, copyD));
    assertEquals("D:B02", new Volume(9),  librariesDao.findRemainingVolumeInWellCopy(wellB02, copyD));
    assertEquals("D:C03", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellC03, copyD));
    assertEquals("E:A01", new Volume(8),  librariesDao.findRemainingVolumeInWellCopy(wellA01, copyE));
    assertEquals("E:B02", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellB02, copyE));
    assertEquals("E:C03", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellC03, copyE));
    assertEquals("F:A01", new Volume(9),  librariesDao.findRemainingVolumeInWellCopy(wellA01, copyF));
    assertEquals("F:B02", new Volume(7),  librariesDao.findRemainingVolumeInWellCopy(wellB02, copyF));
    assertEquals("F:C03", new Volume(10), librariesDao.findRemainingVolumeInWellCopy(wellC03, copyF));
    assertEquals("G:A01", new Volume(0),  librariesDao.findRemainingVolumeInWellCopy(wellA01, copyG));
    assertEquals("G:B02", new Volume(0),  librariesDao.findRemainingVolumeInWellCopy(wellB02, copyG));
    assertEquals("G:C03", new Volume(0),  librariesDao.findRemainingVolumeInWellCopy(wellC03, copyG));
  }

}
