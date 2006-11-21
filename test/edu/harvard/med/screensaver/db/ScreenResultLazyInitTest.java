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
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class ScreenResultLazyInitTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(ScreenResultLazyInitTest.class);


  // instance data members
  
  protected DAO dao;
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
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = ScreenResultParser.makeDummyScreen(107);
        ScreenResult screenResult = new ScreenResult(screen, new Date());
        new ResultValueType(screenResult, "Luminescence");
        new ResultValueType(screenResult, "FI");
        dao.persistEntity(screen);
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Session session = hibernateTemplate.getSessionFactory().getCurrentSession();
        assertEquals("session initially empty", 0, session.getStatistics().getEntityCount());
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 107);
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
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = ScreenResultParser.makeDummyScreen(107);
        ScreenResult screenResult = new ScreenResult(screen, new Date());
        ResultValueType rvt = new ResultValueType(screenResult, "Luminescence");
        Library library = new Library("library 1", "lib1", LibraryType.COMMERCIAL, 1, 1);
        for (int i = 1; i < 10; ++i) {
          Well well = new Well(library, i, "A01");
          ResultValue resultValue = new ResultValue(rvt, well, Integer.toString(i));
          dao.persistEntity(resultValue);
        }
        dao.persistEntity(screen);
      }
    });

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Session session = hibernateTemplate.getSessionFactory().getCurrentSession();
        assertEquals("session initially empty", 0, session.getStatistics().getEntityCount());
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 107);
        List<ResultValueType> rvts = screen.getScreenResult().getResultValueTypesList();
        ResultValueType rvt = rvts.get(0);
        
        for (Object key : session.getStatistics().getEntityKeys()) {
          EntityKey entityKey = (EntityKey) key;
          log.debug(entityKey);
          assertFalse("no resultValue entities in session",
                      entityKey.getEntityName().endsWith("ResultValue"));
        }
        
        int n = session.getStatistics().getEntityKeys().size();
        List<ResultValue> resultValues = rvt.getResultValues();
        log.debug("resultValue count=" + resultValues.size());
        ++n; // to account for the single library that will be loaded when the first result value is loaded
//      for (ResultValue rv : resultValues) { // can't do this, as the implicit iterator() call will load the full resultValues collection at once!
        for (int i = 0; i < resultValues.size(); ++i) {
          ResultValue rv = resultValues.get(i);
          n += 2;
          rv.getValue();
          log.debug("entity key count=" + session.getStatistics().getEntityKeys().size());
          assertEquals("single resultValue (and well) loaded",
                       n,
                       session.getStatistics().getEntityKeys().size());
        }
      }
    });
  }

  // private methods

}

