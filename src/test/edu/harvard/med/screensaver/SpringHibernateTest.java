// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: TestHibernate.java 83 2006-05-11 20:52:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.beans.libraries.Compound;
import edu.harvard.med.screensaver.beans.libraries.Library;
import edu.harvard.med.screensaver.beans.libraries.Well;
import edu.harvard.med.screensaver.db.LabDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;

import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Log4jConfigurer;

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
  
//  /**
//   * For schema-related test setup tasks. Not provided via Dependency Injection,
//   * due to AOP proxy-related class mismatch problems.
//   */
//  private LocalSessionFactoryBean _hibernateSessionFactory;

  static {
    try {
      Log4jConfigurer.initLogging("classpath:log4j-testing.properties");
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
//    _hibernateSessionFactory = (LocalSessionFactoryBean) applicationContext.getBean("&hibernateSessionFactory");
//    _hibernateSessionFactory.dropDatabaseSchema();
//    _hibernateSessionFactory.createDatabaseSchema();
    schemaUtil.recreateSchema();
  }

  // JUnit test methods 
  
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
        Library library = labDAO.defineLibrary("library Q", "Q", "DOS", 1, 2);
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
        Well well = library.getWells().iterator().next();
        assertEquals("library has well", "A01", well.getWellName());
        assertEquals("Well's Compound count", 1, well.getCompounds().size());
        assertEquals("Well-Compound association", "compound P", well.getCompounds().iterator().next().getCompoundName());
      }
    });

   // TODO: throwing exceptions!?
  //  // // iterate over compounds
  //  Iterator<Compound> compoundsItr = labDAO.findAllCompounds().iterator();
  //  System.out.println("compounds:");
  //  while (compoundsItr.hasNext()) {
  //    displayCompound(compoundsItr.next());
  //    System.out.println();
  //  }
  }

  public void testSpringHibernateTransactionRollback()
  {
    try {
      List<String> wellNames = new ArrayList<String>();
      wellNames.add("B01");
      wellNames.add("B02");
      wellNames.add("B03");
      wellNames.add("C01"); // triggers exception and causes rollback
      Library library = labDAO.defineLibrary("library R", "R", "DOS", 1, 4);
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
        Library library = labDAO.defineLibrary("library R", "R", "DOS", 1, 4);
        Compound compound = labDAO.defineCompound("compound Q", "Q");
        labDAO.defineLibraryPlateWells(28, wellNames, library, compound);
      }
    });

    Set<Well> wells = labDAO.findAllLibraryWells("library R");
    assertEquals("commit of all Wells", 3, wells.size());

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

//  /**
//   * Non-JUnit means for executing this test class within Spring.
//   * 
//   * @motivation When running as JUnit test, the labDAO property does not appear
//   *             to be injected with an transaction-capapble AOP proxy for
//   *             labDAO. Hoping that by removing JUnit (and
//   *             AbstractDependencyInjectionSpringContextTests) from this mix,
//   *             we'll be testing the configuration in a more kosher fashion.
//   */
//  public static void main(String[] args) {
//    ApplicationContext appCtx = new ClassPathXmlApplicationContext("spring-context-persistence.xml");
//
//    LocalSessionFactoryBean hibernateSessionFactory = (LocalSessionFactoryBean) appCtx.getBean("&hibernateSessionFactory");
//    hibernateSessionFactory.dropDatabaseSchema();
//    hibernateSessionFactory.createDatabaseSchema();
//    
//    SpringHibernateTest tester = (SpringHibernateTest) appCtx.getBean("labDAOTest");
//    tester.testDatabaseAccess();
//  }

}
