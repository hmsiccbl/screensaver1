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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.EntityKey;
import org.joda.time.LocalDate;

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
    /*CherryPickRequest cpr =*/ screen.createCherryPickRequest(
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

  /**
   * Tests that no problems occur when Hibernate applies cascades to
   * leadScreener and labHead relationships. Regression test for problems that
   * were occurring with these cascades.
   */
  public void testScreenToScreenerCascades()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Screen screen1a = MakeDummyEntities.makeDummyScreen(1);
    genericEntityDao.persistEntity(screen1a);
    Screen screen1b = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", new Integer(1), true, "labHead", "leadScreener");
    assertEquals(screen1a.getLabHead().getEntityId(), screen1b.getLabHead().getEntityId());
    assertEquals(screen1a.getLeadScreener().getEntityId(), screen1b.getLeadScreener().getEntityId());

    Screen screen2a = MakeDummyEntities.makeDummyScreen(2);
    screen2a.setLeadScreener(screen2a.getLabHead());
    genericEntityDao.persistEntity(screen2a);
    Screen screen2b = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", new Integer(2), true, "labHead", "leadScreener");
    assertEquals(screen2a.getLabHead().getEntityId(), screen2b.getLabHead().getEntityId());
    assertEquals(screen2a.getLabHead().getEntityId(), screen2b.getLeadScreener().getEntityId());
  }

  public void testCandidateStatusItems()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    Set<StatusValue> expected = new HashSet<StatusValue>(Arrays.asList(StatusValue.values()));
    assertEquals(expected, new HashSet<StatusValue>(screen.getCandidateStatusValues()));

    LocalDate today = new LocalDate();
    screen.createStatusItem(today, StatusValue.PENDING_ICCB);
    expected.remove(StatusValue.PENDING_LEGACY);
    expected.remove(StatusValue.PENDING_ICCB);
    expected.remove(StatusValue.PENDING_NSRB);
    assertEquals(expected, new HashSet<StatusValue>(screen.getCandidateStatusValues()));

    screen.createStatusItem(today, StatusValue.PILOTED);
    expected.remove(StatusValue.PILOTED);
    assertEquals(expected, new HashSet<StatusValue>(screen.getCandidateStatusValues()));

    screen.createStatusItem(today, StatusValue.ACCEPTED);
    expected.remove(StatusValue.ACCEPTED);
    assertEquals(expected, new HashSet<StatusValue>(screen.getCandidateStatusValues()));

    screen.createStatusItem(today, StatusValue.ONGOING);
    expected.remove(StatusValue.ONGOING);
    assertEquals(expected, new HashSet<StatusValue>(screen.getCandidateStatusValues()));

    screen.createStatusItem(today, StatusValue.COMPLETED);
    assertEquals(1, screen.getCandidateStatusValues().size());
    
    screen.createStatusItem(today, StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE);
    assertEquals(0, screen.getCandidateStatusValues().size());
  }

  public void testAddAnachronisticStatusItem()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);

    screen.createStatusItem(new LocalDate(2008, 6, 2), StatusValue.ONGOING);
    try {
      screen.createStatusItem(new LocalDate(2008, 6, 1), StatusValue.COMPLETED);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      assertEquals("date of new status item must not be before the date of the previous status item", e.getMessage());
    }

    try {
      screen.createStatusItem(new LocalDate(2008, 6, 3), StatusValue.ACCEPTED);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      assertEquals("date of new status item must not be after date of subsequent status item", e.getMessage());
    }
  }

  public void testAddConflictingStatusItem()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);

    screen.createStatusItem(new LocalDate(2008, 6, 2), StatusValue.PENDING_ICCB);
    try {
      screen.createStatusItem(new LocalDate(2008, 6, 2), StatusValue.PENDING_NSRB);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      assertEquals("status value Pending - NSRB is mutually exclusive with existing status item value Pending - ICCB-L",
                   e.getMessage());    
    }
  }

  public void testAddAndDeleteAttachedFiles() throws IOException
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    screen.createAttachedFile("file1.txt", AttachedFileType.SCREENER_CORRESPONDENCE, "file1 contents");
    genericEntityDao.persistEntity(screen);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1, true, "attachedFiles");
        assertEquals("add attached file to transient screen", 1, screen.getAttachedFiles().size());
        try {
          assertEquals("attached file contents accessible",
                       "file1 contents",
                       IOUtils.toString(screen.getAttachedFiles().iterator().next().getFileContents().getBinaryStream()));
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });

    Iterator<AttachedFile> iter = screen.getAttachedFiles().iterator();
    AttachedFile attachedFile = iter.next();
    screen.getAttachedFiles().remove(attachedFile);
    genericEntityDao.saveOrUpdateEntity(screen);
    screen = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", 1, true, "attachedFiles");
    assertEquals("delete attached file from detached screen", 0, screen.getAttachedFiles().size());
  }

}

