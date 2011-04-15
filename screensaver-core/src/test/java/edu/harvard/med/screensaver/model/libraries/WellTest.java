// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import junit.framework.TestSuite;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;

public class WellTest extends AbstractEntityInstanceTest<Well>
{
  public static TestSuite suite()
  {
    return buildTestSuite(WellTest.class, Well.class);
  }

  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected LibraryCreator libraryCreator;


  public WellTest()
  {
    super(Well.class);
  }


  public void testResultValueMap()
  {
    schemaUtil.truncateTables();
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
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
        ScreenResult screenResult = screen.getScreenResult();
        Well well = genericEntityDao.findEntityById(Well.class, screen.getScreenResult().getAssayWells().first().getLibraryWell().getWellId());
        assertEquals("well.resultValues size",
                     screenResult.getDataColumns().size(),
                     well.getResultValues().size());

        DataColumn col = genericEntityDao.findEntityByProperty(DataColumn.class, "name", "numeric_repl1");
        ResultValue resultValue = well.getResultValues().get(col);
        assertNotNull(resultValue);
        assertEquals(col, resultValue.getDataColumn());
      }
    });
  }

  public void testDeprecation()
  {
    schemaUtil.truncateTables();
    initDeprecatedWells();

    Well well = genericEntityDao.findEntityById(Well.class,
                                                new WellKey(1000, "A01").toString(),
                                                true,
                                                Well.deprecationActivity);
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
        AdministratorUser admin = new AdministratorUser("Admin2", "User");
        AdministrativeActivity deprecationActivity =
          new AdministrativeActivity(admin,
                                     new LocalDate(),
                                     AdministrativeActivityType.WELL_DEPRECATION);
        deprecationActivity.setComments("discontinued gene");
        well.setDeprecationActivity(deprecationActivity);
      }
    });
  }
  
  public void testWellEdge()
  {
    schemaUtil.truncateTables();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(1);
    library.setEndPlate(1);
    library.setPlateSize(PlateSize.WELLS_96);
    libraryCreator.createWells(library);
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
    schemaUtil.truncateTables();
    Well well = dataFactory.newInstance(Well.class);
    well.setLibraryWellType(LibraryWellType.UNDEFINED);
    well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
    well.setLibraryWellType(LibraryWellType.UNDEFINED);
    well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
    try {
      well.setLibraryWellType(LibraryWellType.DMSO);
      fail("expecting DataModelViolationException");
    }
    catch (DataModelViolationException e) {
      assertTrue(e.getMessage().contains("cannot change library well type"));
    }
  }
}

