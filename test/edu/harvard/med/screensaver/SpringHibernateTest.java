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

package edu.harvard.med.screensaver;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.db.LabDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.Child;
import edu.harvard.med.screensaver.model.Parent;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Log4jConfigurer;

// TODO: break this test class apart into 2 classes one for Spring+Hibernate
// tests and one for entity model tests

/**
 * Tests Spring/Hibernate integration. This is one of the rare cases where
 * testing must be Spring-aware, and so we use
 * <code>AbstractDependencyInjectionSpringContextTests</code> to have Spring
 * inject our persistence-related objects into our test class.
 * 
 * @author andrew tolopko
 */
public class SpringHibernateTest
  extends AbstractDependencyInjectionSpringContextTests
{

  /**
   * Spring configuration will be loaded from the configuration file(s)
   * specified in this constant.
   */
   private static final String[] SPRING_CONFIG_FILES = new String[] {"spring-context-persistence.xml"};

  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected LabDAO labDAO;

  /**
   * Bean property, for executing programmatic transactions.
   */
  protected HibernateTransactionManager txnManager;
  
  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;
  
  protected HibernateTemplate hibernateTemplate;
  
//  /**
//   * For schema-related test setup tasks. Not provided via Dependency Injection,
//   * due to AOP proxy-related class mismatch problems.
//   */
//  private LocalSessionFactoryBean _hibernateSessionFactory;

  static {
    try {
      Log4jConfigurer.initLogging("classpath:log4j.properties");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public SpringHibernateTest() {
    // have AbstractDependencyInjectionSpringContextTests inject the properties
    // we need into protected data members that share the same name as beans in
    // our Spring configuration files.
    setPopulateProtectedVariables(true);
  }
  
  // bean property setter/getter methods

  public void setLabDAO(LabDAO labDAO) {
    this.labDAO = labDAO;
  }

  public void setTransactionManager(HibernateTransactionManager txnManager) {
    this.txnManager = txnManager;
  }
  
  @Override
  /**
   * Provides the Spring framework with the configuration files we need loaded
   * in order to execute our tests.
   */
  protected String[] getConfigLocations()
  {
    return SPRING_CONFIG_FILES;
  }
  
  // AbstractDependencyInjectionSpringContextTests methods

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.recreateSchema();
  }

  // JUnit test methods 
  
  public void testParentChildRelationship() {
    Parent parent = new Parent();
    parent.addChild(new Child("a"));
    parent.addChild(new Child("b"));
    hibernateTemplate.save(parent);
    
    Session session = hibernateTemplate.getSessionFactory().openSession();
    Parent loadedParent = (Parent) session.load(Parent.class, parent.getId());
    assertNotSame("distinct parent objects for save and load operations", parent, loadedParent);
    Set<Child> loadedChildren = loadedParent.getChildren();
    assertNotSame("distinct children set objects for save and load operations", parent.getChildren(), loadedChildren);
    assertEquals(parent, loadedParent);
    assertEquals(parent.getChildren(), loadedChildren);
    
    // now test whether we can add another child to our Parent that was loaded from the database
    Child childC = new Child("c");
    loadedParent.addChild(childC);
    assertTrue("child added to loaded parent", loadedParent.getChildren().contains(childC));
    session.flush();
    session.close();
    Session session2 = hibernateTemplate.getSessionFactory().openSession();
    Parent loadedParent2 = (Parent) session2.load(Parent.class, parent.getId());
    assertTrue("child added to re-loaded parent", loadedParent2.getChildren().contains(childC));
    session2.close();
  }
  
  public void testCreateAndModifyCompound()
  {
    // create a new compound
    new TransactionTemplate(txnManager).execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        labDAO.defineCompound("compound P", "P");
      }
    });

    new TransactionTemplate(txnManager).execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        // // look up a compound and modify it
        Compound compound = labDAO.findCompoundByName("compound P");
        assertNotNull("compound exists", compound );
        compound.setSmiles("P'");
      }
    });
     
    new TransactionTemplate(txnManager).execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Compound compound = labDAO.findCompoundByName("compound P");
        assertEquals("compound modified", "P'", compound.getSmiles());
      }
    });
  }
  
  public void testCreateLibraryWellCompound()
  {
    // create a new well, add compound p to it
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Library library = labDAO.defineLibrary("library Q", "Q", LibraryType.KNOWN_BIOACTIVES, 1, 2);
        Compound compound = labDAO.defineCompound("compound P", "P");
        Well well = labDAO.defineLibraryWell(library, 27, "A01");
        labDAO.associateCompoundWithWell(well, compound);
      }
     });
    
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Library library = labDAO.findLibraryByName("library Q");
        assertEquals("Library's Well count", 1, library.getWells().size());
        assertEquals("library has type", LibraryType.KNOWN_BIOACTIVES, library.getLibraryType());
        Well well = library.getWells().iterator().next();
        Compound compound = labDAO.findCompoundByName("compound P");
        assertEquals("library has well", "A01", well.getWellName());
        assertEquals("Well's Compound count", 1, well.getCompounds().size());
        assertEquals("Compound's Well count", 1, compound.getWells().size());
        assertEquals("Well-Compound association", "compound P", well.getCompounds().iterator().next().getCompoundName());
        assertEquals("Compound-Well association", "A01", compound.getWells().iterator().next().getWellName());
      }
    });

    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        // // iterate over compounds
        Iterator<Compound> compoundsItr = labDAO.findAllCompounds().iterator();
        System.out.println("compounds:");
        while (compoundsItr.hasNext()) {
          displayCompound(compoundsItr.next());
          System.out.println();
        }
      }
    });
  }
  
  
  /**
   * Tests whether a Well's comopunds can be modified after it has been loaded
   * from the database. (This is more a test of Hibernate than of our
   * application.)
   */
  public void testCreateWellModifyLater() {
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Library library = labDAO.defineLibrary("library Q", "Q", LibraryType.KNOWN_BIOACTIVES, 1, 2);
        labDAO.defineLibraryWell(library, 27, "A01");
      }
     });
    
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Well well = labDAO.findAllLibraryWells("library Q").iterator().next();
        well.addCompound(labDAO.defineCompound("compound P", "P"));
      }
    });
    
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Well well = labDAO.findAllLibraryWells("library Q").iterator().next();
        well.getCompounds().contains(new Compound("compound P"));
      }
    });
    
    
  }

  public void testSpringHibernateTransactionRollback()
  {
    try {
      List<String> wellNames = new ArrayList<String>();
      wellNames.add("B01");
      wellNames.add("B02");
      wellNames.add("B03");
      wellNames.add("C01"); // triggers exception and causes rollback
      Library library = labDAO.defineLibrary("library R", "R", LibraryType.KNOWN_BIOACTIVES, 1, 4);
      Compound compound = labDAO.defineCompound("compound Q", "Q");
      labDAO.defineLibraryPlateWells(28, wellNames, library, compound);
    } 
    catch (Exception e) {
      Set<Well> wells = labDAO.findAllLibraryWells("library R");
      assertEquals("rollback of all Wells", 0, wells.size());
    }
  }    

  public void testSpringHibernateTransactionCommit()
  {
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        List<String> wellNames = new ArrayList<String>();
        wellNames.add("B01");
        wellNames.add("B02");
        wellNames.add("B03");
        Library library = labDAO.defineLibrary("library R", "R", LibraryType.KNOWN_BIOACTIVES, 1, 4);
        Compound compound = labDAO.defineCompound("compound Q", "Q");
        labDAO.defineLibraryPlateWells(28, wellNames, library, compound);
      }
    });

    Set<Well> wells = labDAO.findAllLibraryWells("library R");
    assertEquals("commit of all Wells", 3, wells.size());

  }    
  
  
  public void testScreenResults() {
    
    final int replicates = 2;
    
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        ScreenResult screenResult = new ScreenResult();
        screenResult.setShareable(false);
        screenResult.setDateCreated(Calendar.getInstance().getTime());
        screenResult.setReplicateCount(replicates);
        
        ResultValueType[] rvt = new ResultValueType[replicates];
        for (int i = 0; i < replicates; i++) {
          rvt[i] = new ResultValueType();
          rvt[i].setAssayReadoutType(i % 2 == 0 ? AssayReadoutType.PHOSPHORESCENCE : AssayReadoutType.FLOURESCENCE);
          rvt[i].setActivityIndicatorType(i % 2 == 0 ? ActivityIndicatorType.BOOLEAN: ActivityIndicatorType.SCALED);
          rvt[i].setIndicatorDirection(i % 2 == 0 ? IndicatorDirection.LOW_VALUES_INDICATE : IndicatorDirection.HIGH_VALUES_INDICATE);
          rvt[i].setAssayPhenotype("human");
          rvt[i].setScreenResult(screenResult);
        }
        
        Library library = labDAO.defineLibrary("library with results", 
                                               "lwr", 
                                               LibraryType.COMMERCIAL,
                                               1, 
                                               1);
        Well[] wells = new Well[3];
        for (int iWell = 0; iWell < wells.length; ++iWell) {
          wells[iWell] = labDAO.defineLibraryWell(library,
                                                  1,
                                                  "well" + iWell);
          for (int iResultValue = 0; iResultValue < rvt.length; ++iResultValue) {
            ResultValue rv = new ResultValue();
            rv.setValue("value " + iWell + "," + iResultValue);
            rv.setWell(wells[iWell]);
            rv.setResultValueType(rvt[iResultValue]);
          }
        }
        labDAO.persistEntity(screenResult);
      }
    });
       
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        Set<Well> wells = labDAO.findAllLibraryWells("library with results");
        ScreenResult screenResult = labDAO.loadAllScreenResults().iterator().next();
        int iResultValue = 0;
        SortedSet<ResultValueType> resultValueTypes = screenResult.getResultValueTypes();
        assertEquals(2, replicates);
        for (ResultValueType rvt : resultValueTypes) {
          assertEquals(screenResult,
                       rvt.getScreenResult());
          assertEquals(iResultValue % 2 == 0 ? AssayReadoutType.PHOSPHORESCENCE : AssayReadoutType.FLOURESCENCE,
                       rvt.getAssayReadoutType());
          assertEquals(iResultValue % 2 == 0 ? ActivityIndicatorType.BOOLEAN: ActivityIndicatorType.SCALED,
                       rvt.getActivityIndicatorType());
          assertEquals(iResultValue % 2 == 0 ? IndicatorDirection.LOW_VALUES_INDICATE : IndicatorDirection.HIGH_VALUES_INDICATE,
                       rvt.getIndicatorDirection());
          assertEquals("human",
                       rvt.getAssayPhenotype());
          
            int iWell = 0;
          for (ResultValue rv : rvt.getResultValues()) {
            assertEquals(rvt,
                         rv.getResultValueType());
            assertTrue(wells.contains(rv.getWell()));
            // note that our naming scheme is testing the ordering of the
            // ResultValueType and ResultValue entities (within their parent
            // sets)
            assertEquals("value " + iWell + "," + iResultValue,
                         rv.getValue());
            iWell++;
          }
          iResultValue++;
        }
      }
    });
    

  }
  
  public void testDerivedScreenResults() {
    final int replicates = 3;
    final SortedSet<ResultValueType> derivedRvtSet1 = new TreeSet<ResultValueType>();
    final SortedSet<ResultValueType> derivedRvtSet2 = new TreeSet<ResultValueType>();
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        ScreenResult screenResult = new ScreenResult();
        screenResult.setShareable(false);
        screenResult.setDateCreated(Calendar.getInstance().getTime());
        screenResult.setReplicateCount(replicates);
        
        for (int i = 0; i < replicates; i++) {
          ResultValueType rvt = new ResultValueType();
          rvt.setAssayPhenotype("human");
          rvt.setScreenResult(screenResult);
          derivedRvtSet1.add(rvt);
          if (i % 2 == 0) {
            derivedRvtSet2.add(rvt);
          }
        }
        ResultValueType derivedRvt1 = new ResultValueType();
        derivedRvt1.setAssayPhenotype("human");
        derivedRvt1.setScreenResult(screenResult);
        derivedRvt1.setDerivedFrom(derivedRvtSet1);

        ResultValueType derivedRvt2 = new ResultValueType();
        derivedRvt2.setAssayPhenotype("human");
        derivedRvt2.setScreenResult(screenResult);
        derivedRvt2.setDerivedFrom(derivedRvtSet2);
        
        labDAO.persistEntity(screenResult);
      }
    });
    
    new TransactionTemplate(txnManager).execute(new
                                                TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus status)
      {
        ScreenResult screenResult = labDAO.loadAllScreenResults().iterator().next();
        @SuppressWarnings("unchecked")
        SortedSet<ResultValueType> resultValueTypes = new TreeSet(screenResult.getResultValueTypes());

        ResultValueType derivedRvt = resultValueTypes.last();
        Set<ResultValueType> derivedFromSet = derivedRvt.getDerivedFrom();
        assertEquals(derivedRvtSet2, derivedFromSet);
        
        resultValueTypes.remove(derivedRvt);
        derivedRvt = resultValueTypes.last();
        derivedFromSet = derivedRvt.getDerivedFrom();
        assertEquals(derivedRvtSet1, derivedFromSet);
      }
    });
    
  }
        
  private void displayCompound(Compound compound) {
    Well well;
    Iterator<Well> wells;
    System.out.println("compoundId:   " + compound.getCompoundId());
    System.out.println("compoundName: " + compound.getCompoundName());
    System.out.println("smiles:       " + compound.getSmiles());
    wells = compound.getWells()
                    .iterator();
    while (wells.hasNext()) {
      well = wells.next();
      displayWell(well);
    }
  }

  private void displayWell(Well well) {
    System.out.println("well plate:   " + well.getPlateNumber());
    System.out.println("well well:    " + well.getWellName());
  }


}
