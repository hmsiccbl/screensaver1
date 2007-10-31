// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ScreenResultLazyInitTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultLazyInitTest.class);


  // instance data members

  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  //protected HibernateSessionFactory hibernateSessionFactory;
  protected HibernateTemplate hibernateTemplate;

  // public constructors and methods

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }

  public void testScreenToScreenResultLazyInit()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(107);
        ScreenResult screenResult = screen.createScreenResult(new Date());
        screenResult.createResultValueType("Luminescence");
        screenResult.createResultValueType("FI");
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Session session = hibernateTemplate.getSessionFactory().getCurrentSession();
        assertEquals("session initially empty", 0, session.getStatistics().getEntityCount());
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 107);
        assertNotNull("screen in session", screen);
        for (Object key : session.getStatistics().getEntityKeys()) {
          EntityKey entityKey = (EntityKey) key;
          assertFalse("no resultValueType entities in session",
                      entityKey.getEntityName().contains("ResultValueType"));
        }
      }
    });
  }

  public void testResultValueTypeToResultValueExtraLazyInit()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(107);
        ScreenResult screenResult = screen.createScreenResult(new Date());
        ResultValueType rvt = screenResult.createResultValueType("Luminescence");
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          1);
        for (int i = 1; i < 10; ++i) {
          Well well = library.createWell(new WellKey(i, "A01"), WellType.EMPTY);
          genericEntityDao.saveOrUpdateEntity(well);
          rvt.createResultValue(well, Integer.toString(i));
        }
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Session session = hibernateTemplate.getSessionFactory().getCurrentSession();
        assertEquals("session initially empty", 0, session.getStatistics().getEntityCount());
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 107);
        List<ResultValueType> rvts = screen.getScreenResult().getResultValueTypesList();
        ResultValueType rvt = rvts.get(0);

        for (Object key : session.getStatistics().getEntityKeys()) {
          EntityKey entityKey = (EntityKey) key;
          log.debug(entityKey);
          assertFalse("no resultValue entities in session",
                      entityKey.getEntityName().endsWith("ResultValue"));
        }

        Set<Well> wells = rvt.getScreenResult().getWells();
        Map<WellKey,ResultValue> resultValues = rvt.getWellKeyToResultValueMap();
        log.debug("resultValue count=" + resultValues.size());
        Iterator iter = wells.iterator();
        int n = session.getStatistics().getEntityKeys().size();
        log.debug("entity key count (pre-iteration)=" + n);
        while (iter.hasNext()) {
          Well well = (Well) iter.next();
          ResultValue rv = resultValues.get(well.getWellKey());
          rv.getValue();
          log.debug("entity key count=" + session.getStatistics().getEntityKeys().size());
          assertEquals("Hibernate session cache did not grow in size",
                       n,
                       session.getStatistics().getEntityKeys().size());
        }
      }
    });
  }

  // private methods

}

