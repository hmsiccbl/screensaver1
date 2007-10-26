// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class WellTest extends AbstractEntityInstanceTest<Well>
{
  private static Logger log = Logger.getLogger(WellTest.class);

  protected LibrariesDAO librariesDao;

  public WellTest() throws IntrospectionException
  {
    super(Well.class);
  }

  /**
   * Test the special-case molfile property, which is implemented as a set of
   * strings (with size 0 or 1).
   */
  public void testMolfile()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = new Library(
          "dummy",
          "shortDummy",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        genericEntityDao.saveOrUpdateEntity(library);
        librariesDao.loadOrCreateWellsForLibrary(library);
        for (Well well : library.getWells()) {
          well.setMolfile("molfile:" + well.getEntityId());
        }
      }
    });
    class WellPocketDAOTransaction implements DAOTransaction
    {
      Well wellWithMolfileLoadedEagerly;
      Well wellWithMolfileNotLoaded;
      public void runTransaction()
      {
        // test that we can load the molfile on demand, within session
        Well well = genericEntityDao.findEntityById(Well.class, "00001:A01");
        assertEquals("molfile:00001:A01", well.getMolfile());

        // test that we can eager load the molfile via our dao methods, and access after session is closed
        wellWithMolfileLoadedEagerly = genericEntityDao.findEntityById(Well.class, "00001:A02");
        genericEntityDao.need(well, "molfileSet");

        // test that a molfile not loaded is not accessible after session is closed
        assertEquals("molfile:00001:A01", well.getMolfile());
        wellWithMolfileNotLoaded = genericEntityDao.findEntityById(Well.class, "00001:A01");
      }
    }
    WellPocketDAOTransaction wellPocketDAOTransaction = new WellPocketDAOTransaction();
    genericEntityDao.doInTransaction(wellPocketDAOTransaction);

    // this should cause an error because well.molfile should be lazily loaded
    try {
      assertEquals("molfile:00001A02",
                   wellPocketDAOTransaction.wellWithMolfileLoadedEagerly.getMolfile());
      wellPocketDAOTransaction.wellWithMolfileNotLoaded.getMolfile();
      fail("failed to get a LazyInitException for well.molfile");
    }
    catch (Exception e) {
    }
  }
}

