// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 2359 2008-05-09 21:16:57Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link GenericEntityDAOTest}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AbstractDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(AbstractDAOTest.class);


  // protected instance fields

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
}
