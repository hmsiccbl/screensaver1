//$HeadURL$
//$Id$

//Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;

/**
 * @author serickson
 */
public class LibraryCreatorTest extends AbstractSpringPersistenceTest
{
  private static final String RNAI_LIBRARY_CONTENTS_TEST_FILE = "rnaiLibraryContentsFile.xls";
  private static Logger log = Logger.getLogger(LibraryCreatorTest.class);

  @Autowired
  protected LibraryCreator libraryCreator;
  @Autowired
  protected LibrariesDAO librariesDao;
  
  public void testCreateLibrary() throws ParseErrorsException, IOException
  {
    AdministratorUser adminUser = new AdministratorUser("Admin", "User");
    final Library library = new Library(adminUser,
                                        "Human1",
                                        "Human1",
                                        ScreenType.RNAI,
                                        LibraryType.SIRNA,
                                        50439,
                                        50439,
                                        PlateSize.WELLS_384);
    library.setProvider("Dharmacon");
    library.setDescription("test library");

    try {
      libraryCreator.createLibrary(library);

      assertNotNull("library was assigned ID", library.getLibraryId());

      Library library2 = genericEntityDao.findEntityById(Library.class, library.getEntityId(), true, Library.wells);
      assertNotNull("library was persisted", library2);
      Well firstWell = librariesDao.findWell(new WellKey(50439, "A1"));
      Well lastWell = librariesDao.findWell(new WellKey(50439, "P24"));
      assertNotNull("library wells created (check first empty well)", firstWell);
      assertNotNull("library wells created (check last empty well)", lastWell);
    }
    catch (Exception e) {
      log.warn("fail",e);
      fail(e.getMessage());
    }

    // TODO: do these need to be in DAOTransaction to be valid? - sde4
    // or should we just check for UnexpectedRollbackException?
    // get this exception when in a tx block
    //org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only
    //  at org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:672)
    //  at org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:321)
    //  at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:116)
    //  at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:171)
    //  at org.springframework.aop.framework.Cglib2AopProxy$DynamicAdvisedInterceptor.intercept(Cglib2AopProxy.java:635)
    //  at edu.harvard.med.screensaver.db.GenericEntityDAOImpl$$EnhancerByCGLIB$$d60f3cb6.doInTransaction(<generated>)
    //  at edu.harvard.med.screensaver.service.libraries.LibraryCreatorTest.testCreateLibrary(LibraryCreatorTest.java:118)
 
    Library library4 = new Library(adminUser, "library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 50439, 50439, PlateSize.WELLS_384);
    try {
      libraryCreator.createLibrary(library4);
      fail("expected failure on redundant library create");
    }
    catch (DataModelViolationException e) 
    {
      log.info("expected when trying to create a library with a plate range reuse violation: ", e);
    }
    catch (Exception e)
    {
      log.warn("fail",e);
      fail(e.getMessage());
    }
    
    Library library5 = new Library(adminUser, "library5", "lib5", ScreenType.RNAI, LibraryType.COMMERCIAL, 50438, 50441, PlateSize.WELLS_384);
    try {
      libraryCreator.createLibrary(library5);
      fail("expected failure on conflicting plate range");
    }
    catch (DataModelViolationException e) {}
    catch (Exception e)
    {
      log.warn("fail",e);
      fail(e.getMessage());
    }
    

// TODO: this is no longer an exception? (since library versions are what are being created) - sde4 
//    try {
//      libraryCreator.createLibrary(library);
//      fail("expected failure on redundant library create");
//    }
//    catch (IllegalArgumentException e) {}

  }

  // TODO: implement
//  /**
//   * Test the service.libraries.LibraryCreator.createLibrary does not suffer
//   * from a race condition on library plate range allocation
//   */
//  public void testConcurrentLibraryCreate()
//  {
//    fail("not yet implemented");
//  }
}
