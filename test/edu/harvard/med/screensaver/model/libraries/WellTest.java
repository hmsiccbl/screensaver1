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

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.EntityNetworkPersister;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.joda.time.LocalDate;

public class WellTest extends AbstractEntityInstanceTest<Well>
{
  public static TestSuite suite()
  {
    return buildTestSuite(WellTest.class, Well.class);
  }

  protected LibrariesDAO librariesDao;

  public WellTest() throws IntrospectionException
  {
    super(Well.class);
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
    initDeprecatedWells();

    Well well = genericEntityDao.findEntityById(Well.class,
                                                new WellKey(1000, "A01").toString(),
                                                true,
                                                "deprecationActivity");
    assertTrue(well.isDeprecated());
    assertEquals("discontinued gene", well.getDeprecationActivity().getComments());
  }

//  public void testIsDeprecatedDerivedPropertyFormula()
//  {
//    schemaUtil.truncateTablesOrCreateSchema();
//    initDeprecatedWells();
//    
//    String hql = "from Well where deprecated = true";
//    List<Well> result = genericEntityDao.findEntitiesByHql(Well.class, hql);
//    assertEquals(1, result.size());
//    assertEquals(new WellKey(1000, "A01"), result.get(0).getWellKey());
//    assertTrue(result.get(0).isDeprecated());
//  }

//  @Override
//  public Object getTestValueForType(Class type, AbstractEntity parentBean)
//  {
//    if (type.equals(AdministrativeActivityType.class)) {
//      return AdministrativeActivityType.WELL_DEPRECATION;
//    }
//    return dataFactory.getTestValueForType(type, parentBean);
//  }

  private void initDeprecatedWells()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(library);
        Well well = genericEntityDao.findEntityById(Well.class, new WellKey(1000, "A01").toString());
        AdministratorUser admin = new AdministratorUser("Admin2", "User", "admin_user@hms.harvard.edu", "", "", "", "admin2", "");
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
  }
  
  public void testWellEdge()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(1);
    library.setEndPlate(1);
    library.setPlateSize(PlateSize.WELLS_96);
    librariesDao.loadOrCreateWellsForLibrary(library);
    for (Well well : library.getWells()) {
        assertEquals("is edge @ " + well.getWellName(),
                     well.isEdgeWell(),
                     well.getWellKey().getRow() == 0 || 
                     well.getWellKey().getRow() == library.getPlateSize().getRows() - 1 || 
                     well.getWellKey().getColumn() == 0 || 
                     well.getWellKey().getColumn() == library.getPlateSize().getColumns() - 1);
    }
  }
  
  public void testLibraryWellTypeMutability()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Well well = dataFactory.newInstance(Well.class);
    well.setLibraryWellType(LibraryWellType.UNDEFINED);
    well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
    well.setLibraryWellType(LibraryWellType.UNDEFINED);
    new EntityNetworkPersister(genericEntityDao, well).persistEntityNetwork();
    well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
    new EntityNetworkPersister(genericEntityDao, well).persistEntityNetwork();
    try {
      well.setLibraryWellType(LibraryWellType.DMSO);
      fail("expecting DataModelViolationException");
    }
    catch (DataModelViolationException e) {
      assertTrue(e.getMessage().contains("cannot change library well type"));
    }
  }
}

