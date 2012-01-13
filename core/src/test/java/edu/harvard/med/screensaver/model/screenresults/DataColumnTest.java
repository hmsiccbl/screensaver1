// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class DataColumnTest extends AbstractEntityInstanceTest<DataColumn>
{
  public static TestSuite suite()
  {
    return buildTestSuite(DataColumnTest.class, DataColumn.class);
  }

  @Autowired
  protected ScreenResultParser screenResultParser;
  @Autowired
  protected LibrariesDAO librariesDao;

  public DataColumnTest()
  {
    super(DataColumn.class);
  }

  public void testDeleteDataColumnTest()
  {
    schemaUtil.truncateTables();

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        Library library = MakeDummyEntities.makeDummyLibrary(1, screen.getScreenType(), 1);
        MakeDummyEntities.makeDummyScreenResult(screen, library);
        genericEntityDao.persistEntity(library);
        genericEntityDao.persistEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreenResult screenResult =
          genericEntityDao.findEntityByProperty(Screen.class,
                                                Screen.facilityId.getPropertyName(),
                                                "1",
                                                true,
                                                Screen.screenResult).getScreenResult();
        assertEquals("pre-delete col count", 8, screenResult.getDataColumns().size());

        try {
          screenResult.deleteDataColumn(screenResult.getDataColumnsList().get(1));
          fail("expected DataModelViolationException when deleting DataColumn that is derived from");
        }
        catch (DataModelViolationException e) {}
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreenResult screenResult =
          genericEntityDao.findEntityByProperty(Screen.class,
                                                Screen.facilityId.getPropertyName(),
                                                "1",
                                                true,
                                                Screen.screenResult).getScreenResult();
        screenResult.deleteDataColumn(screenResult.getDataColumnsList().get(6));
        screenResult.deleteDataColumn(screenResult.getDataColumnsList().get(5));
        screenResult.deleteDataColumn(screenResult.getDataColumnsList().get(3));
        screenResult.deleteDataColumn(screenResult.getDataColumnsList().get(1));
        assertEquals("post-delete col count", 4, screenResult.getDataColumns().size());
      }
    });

    ScreenResult screenResult =
      genericEntityDao.findEntityByProperty(Screen.class,
                                            Screen.facilityId.getPropertyName(),
                                            "1",
                                            true,
                                            Screen.screenResult.to(ScreenResult.dataColumns))
                                            .getScreenResult();
    assertEquals("post-delete, post-persist col count", 4, screenResult.getDataColumns().size());

  }
}

