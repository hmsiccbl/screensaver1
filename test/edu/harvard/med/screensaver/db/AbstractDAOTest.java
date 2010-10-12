// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;


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
  private Library _library;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _library = new Library(null,
                           "library Q",
                           "Q",
                           ScreenType.SMALL_MOLECULE,
                           LibraryType.KNOWN_BIOACTIVES,
                           1,
                           2,
                           PlateSize.WELLS_96);
    _library.createWell(new WellKey(27, "A01"), LibraryWellType.EXPERIMENTAL);
    _library.createWell(new WellKey(27, "A02"), LibraryWellType.EXPERIMENTAL);
    _library.createWell(new WellKey(27, "A03"), LibraryWellType.EXPERIMENTAL);
  }

  // protected instance fields

  public void testTransactionRollback()
  {
    try {
      genericEntityDao.doInTransaction(new DAOTransaction()
        {
          public void runTransaction()
          {
            genericEntityDao.saveOrUpdateEntity(_library);
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
            genericEntityDao.saveOrUpdateEntity(_library);
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
