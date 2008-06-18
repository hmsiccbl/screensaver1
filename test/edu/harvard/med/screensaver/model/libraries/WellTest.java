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

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

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
        genericEntityDao.need(well, "molfileList");

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
      fail("failed to get a LazyInitException for well.molfileList");
    }
    catch (Exception e) {
    }
  }

  public void testResultValueMap()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(library);
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        /*final ScreenResult screenResult = */MakeDummyEntities.makeDummyScreenResult(screen, library);
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.persistEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1);
        ScreenResult screenResult = screen.getScreenResult();
        Well well = genericEntityDao.findEntityById(Well.class, screen.getScreenResult().getWells().first().getWellId());
        assertEquals("well.resultValues size",
                     screenResult.getResultValueTypes().size(),
                     well.getResultValues().size());

        ResultValueType rvt = genericEntityDao.findEntityByProperty(ResultValueType.class, "name", "numeric_repl1");
        ResultValue resultValue = well.getResultValues().get(rvt);
        assertNotNull(resultValue);
        assertEquals(rvt, resultValue.getResultValueType());
      }
    });
  }

  public void testDeprecation()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(library);
        Well well = genericEntityDao.findEntityById(Well.class, new WellKey(1000, "A01").toString());
        AdministratorUser admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
        AdministrativeActivity deprecationActivity =
          new AdministrativeActivity(admin,
                                     new LocalDate(),
                                     admin,
                                     new LocalDate(),
                                     AdministrativeActivityType.WELL_DEPRECATION);
        deprecationActivity.setComments("discontinued gene");
        well.setDeprecationActivity(deprecationActivity);
      }
    });

    Well well = genericEntityDao.findEntityById(Well.class,
                                                new WellKey(1000, "A01").toString(),
                                                true,
                                                "deprecationActivity");
    assertTrue(well.isDeprecated());
    assertEquals("discontinued gene", well.getDeprecationActivity().getComments());
  }

  @Override
  public Object getTestValueForType(Class type,
                                    AbstractEntity parentBean,
                                    boolean persistEntities)
  {
    if (type.equals(AdministrativeActivityType.class)) {
      return AdministrativeActivityType.WELL_DEPRECATION;
    }
    return super.getTestValueForType(type, parentBean, persistEntities);
  }

}

