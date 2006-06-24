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

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.Child;
import edu.harvard.med.screensaver.model.Parent;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link SimpleDAOTest}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ComplexDAOTest extends AbstractSpringTest
{
  
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

  
  // AbstractDependencyInjectionSpringContextTests methods

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.recreateSchema();
  }

  
  // JUnit test methods 
  
  public void testParentChildRelationship()
  {
    Parent parent = new Parent("parent1");
    new Child("a", parent);
    new Child("b", parent);
    dao.persistEntity(parent);
    
    Parent loadedParent = dao.findEntityById(Parent.class, parent.getParentId());
    assertNotSame("distinct parent objects for save and load operations", parent, loadedParent);
    Set<Child> loadedChildren = loadedParent.getChildren();
    assertNotSame("distinct children set objects for save and load operations", parent.getChildren(), loadedChildren);
    assertEquals(parent, loadedParent);
    assertEquals(parent.getChildren(), loadedChildren);
    
    // now test whether we can add another child to our Parent that was loaded from the database
    Child childC = new Child("c", loadedParent);
    assertTrue("child added to loaded parent", loadedParent.getChildren().contains(childC));
    dao.persistEntity(loadedParent);
    
    Parent loadedParent2 = dao.findEntityById(Parent.class, parent.getParentId());
    assertTrue("child added to re-loaded parent", loadedParent2.getChildren().contains(childC));
  }
  
  public void testCreateAndModifyCompound()
  {
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Compound compound = dao.defineEntity(Compound.class, "compound P");
          compound.setSmiles("P");
        }
      });

    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = dao.findEntityByProperty(
            Compound.class,
            "compoundName",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("compound smiles", "P", compound.getSmiles());
          compound.setSmiles("P'");
        }
      });

    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          // look up a compound and modify it
          Compound compound = dao.findEntityByProperty(
            Compound.class,
            "compoundName",
            "compound P");
          assertNotNull("compound exists", compound);
          assertEquals("compound modified", "P'", compound.getSmiles());
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
          Library library = dao.defineEntity(
            Library.class,
            "library Q",
            "Q",
            LibraryType.KNOWN_BIOACTIVES,
            1,
            2);
          Compound compound = dao.defineEntity(
            Compound.class,
            "compound P");
          compound.setSmiles("P");
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
            "compoundName",
            "compound P");
          assertEquals("library has well", "A01", well.getWellName());
          assertEquals("Well's Compound count", 1, well.getCompounds().size());
          assertEquals("Compound's Well count", 1, compound.getWells().size());
          assertEquals("Well-Compound association", "compound P", well.getCompounds().iterator().next().getCompoundName());
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
          Library library = dao.defineEntity(
            Library.class,
            "library Q",
            "Q",
            LibraryType.KNOWN_BIOACTIVES,
            1,
            2);
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
          compound.setSmiles("P");
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
          }
        });
    }
    catch (Exception e) {
      fail("unexpected exception e");
    }
    Library library = dao.findEntityByProperty(Library.class, "libraryName", "library Q");
    assertEquals("commit of all Wells", 3, library.getWells().size());
  }    
  
  
  public void testScreenResults() 
  {
    final int replicates = 2;
    
    dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          ScreenResult screenResult = new ScreenResult(new Date());
          
          ResultValueType[] rvt = new ResultValueType[replicates];
          for (int i = 0; i < replicates; i++) {
            rvt[i] = new ResultValueType(
              screenResult,
              "rvt" + i,
              i + 1,
              false,
              false,
              false,
              "human",
              false);
            rvt[i].setAssayReadoutType(i % 2 == 0 ? AssayReadoutType.PHOTOMETRY: AssayReadoutType.FLOURESCENCE_INTENSITY);
            rvt[i].setActivityIndicatorType(i % 2 == 0 ? ActivityIndicatorType.BOOLEAN: ActivityIndicatorType.SCALED);
            rvt[i].setIndicatorDirection(i % 2 == 0 ? IndicatorDirection.LOW_VALUES_INDICATE : IndicatorDirection.HIGH_VALUES_INDICATE);
          }
          
          Library library = dao.defineEntity(
            Library.class,
            "library with results", 
            "lwr", 
            LibraryType.COMMERCIAL,
            1, 
            1);
          Well[] wells = new Well[3];
          for (int iWell = 0; iWell < wells.length; ++iWell) {
            wells[iWell] = dao.defineEntity(
              Well.class,
              library,
              1,
              "well" + iWell);
            for (int iResultValue = 0; iResultValue < rvt.length; ++iResultValue) {
              ResultValue rv = new ResultValue(rvt[iResultValue],
                                               wells[iWell],
                                               "value " + iWell + "," + iResultValue);
              rv.setExclude(iWell % 2 == 1);
            }
          }

          // test the calculation of replicateCount from child ResultValueTypes,
          // before setReplicate() is called by anyone
          assertEquals(replicates,screenResult.getReplicateCount().intValue());
          
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
              iResultValue % 2 == 0 ? AssayReadoutType.PHOTOMETRY : AssayReadoutType.FLOURESCENCE_INTENSITY,
              rvt.getAssayReadoutType());
            assertEquals(
              iResultValue % 2 == 0 ? ActivityIndicatorType.BOOLEAN: ActivityIndicatorType.SCALED,
              rvt.getActivityIndicatorType());
            assertEquals(
              iResultValue % 2 == 0 ? IndicatorDirection.LOW_VALUES_INDICATE : IndicatorDirection.HIGH_VALUES_INDICATE,
              rvt.getIndicatorDirection());
            assertEquals(
              "human",
              rvt.getAssayPhenotype());
            
            int iWell = 0;
            for (ResultValue rv : rvt.getResultValues()) {
              assertEquals(rvt, rv.getResultValueType());
              assertTrue(wells.contains(rv.getWell()));
              // note that our naming scheme is testing the ordering of the
              // ResultValueType and ResultValue entities (within their parent
              // sets)
              assertEquals(
                "value " + iWell + "," + iResultValue,
                rv.getValue());
              assertEquals(iWell % 2 == 1,
                           rv.isExclude());
              iWell++;
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
          ScreenResult screenResult = new ScreenResult(new Date());
          
          for (int i = 0; i < replicates; i++) {
            ResultValueType rvt = new ResultValueType(
              screenResult,
              "rvt" + i,
              1,
              false,
              false,
              false,
              "human",
              false);
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
            "human",
            false);
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
            "human",
            false);
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
}
