// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class ScreenTest extends AbstractEntityInstanceTest<Screen>
{
  // static members

  private static Logger log = Logger.getLogger(ScreenTest.class);


  // instance data members

  
  // public constructors and methods

  public ScreenTest() throws IntrospectionException
  {
    super(Screen.class);
  }
  

  public void testGetLabActivities() throws Exception
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    LibraryScreening screening1 = screen.createLibraryScreening(
      screen.getLeadScreener(),
      new LocalDate(2007, 3, 7));
    LibraryScreening screening2 = screen.createLibraryScreening(
      screen.getLeadScreener(),
      new LocalDate(2007, 3, 8));
    CherryPickRequest cpr = screen.createCherryPickRequest(
      screen.getLeadScreener(),
      new LocalDate(2007, 3, 9));
    CherryPickLiquidTransfer cplt = screen.createCherryPickLiquidTransfer(
      MakeDummyEntities.makeDummyUser(1, "Lab", "Guy"),
      new LocalDate());

    Set<LibraryScreening> libraryScreenings =
      screen.getLabActivitiesOfType(LibraryScreening.class);
    assertEquals("library screening activities",
                 new TreeSet<LibraryScreening>(Arrays.asList(screening1, screening2)),
                 libraryScreenings);

    Set<CherryPickLiquidTransfer> cherryPickLiquidTransfers =
      screen.getLabActivitiesOfType(CherryPickLiquidTransfer.class);
    assertEquals("cherry pick liquid transfer activities",
               new TreeSet<CherryPickLiquidTransfer>(Arrays.asList(cplt)),
               cherryPickLiquidTransfers);

    Set<LabActivity> activities =
      screen.getLabActivitiesOfType(LabActivity.class);
    assertEquals("cherry pick liquid transfer activities",
                 new TreeSet<LabActivity>(Arrays.asList(screening1, screening2, cplt)),
                 activities);
  }

  /**
   * Test that our Hibernate mapping is set properly to lazy load
   * Screen->ScreenResult. This is an ancient test that was implemented during
   * the learning of Hibernate (and before we "trusted" particular mappings were
   * doing what we expected), but it we might as well keep it around.
   */
  public void testScreenToScreenResultIsLazy()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(107);
        ScreenResult screenResult = screen.createScreenResult();
        screenResult.createResultValueType("Luminescence");
        screenResult.createResultValueType("FI");
        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
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
  
}

