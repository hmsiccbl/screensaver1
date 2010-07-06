// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;

public class CherryPickRequestViewerTest extends AbstractSpringPersistenceTest
{
  private static Logger log = Logger.getLogger(CherryPickRequestViewerTest.class);
  
  private AdministratorUser _admin;
  private ScreeningRoomUser _screener;
  private CherryPickRequest _cpr;

  protected CurrentScreensaverUser currentScreensaverUser;
  protected UsersDAO usersDao;
  protected CherryPickRequestViewer cherryPickRequestViewer;
  protected ActivityViewer activityViewer;

  private Library _library;


  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
        currentScreensaverUser.setScreensaverUser(_admin);
        genericEntityDao.persistEntity(_admin);
        _screener = new LabHead(_admin);
        _screener.setFirstName("Lab");
        _screener.setLastName("Head");
        genericEntityDao.persistEntity(_screener);
        _library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        _library.createCopy(CopyUsageType.FOR_CHERRY_PICK_SCREENING, "A").createCopyInfo(1000, "", PlateType.ABGENE, new Volume(1000));
        genericEntityDao.persistEntity(_library);
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        _cpr = screen.createCherryPickRequest(_admin, _screener, new LocalDate());
        _cpr.setTransferVolumePerWellApproved(new Volume(1));
        genericEntityDao.persistEntity(screen);
      }
    });
  }
  
  public void testCherryPickPlatesPlatedAndScreened()
  {
    initializeAssayPlates();
    cherryPickRequestViewer.selectAllAssayPlates();
    cherryPickRequestViewer.recordSuccessfulCreationOfAssayPlates();
    assertEquals(ScreensaverConstants.VIEW_CHERRY_PICK_REQUEST, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr);
        assertTrue(cpr.getCherryPickAssayPlates().first().isPlated());
        assertTrue(Iterables.all(cpr.getLabCherryPicks(), new Predicate<LabCherryPick>() {
          public boolean apply(LabCherryPick lcp)
          {
            return lcp.isPlated();
          };
        }));
      }
    });

    cherryPickRequestViewer.viewEntity(_cpr);
    cherryPickRequestViewer.selectAllAssayPlates();
    cherryPickRequestViewer.recordScreeningOfAssayPlates();
    assertEquals(ScreensaverConstants.VIEW_CHERRY_PICK_REQUEST, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr);
        assertTrue(cpr.getCherryPickAssayPlates().first().isPlatedAndScreened());
      }
    });
  }

  public void testCherryPickPlatesCanceled()
  {
    initializeAssayPlates();
    cherryPickRequestViewer.selectAllAssayPlates();
    cherryPickRequestViewer.deallocateCherryPicksByPlate();
    assertEquals(ScreensaverConstants.VIEW_CHERRY_PICK_REQUEST, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr);
        assertTrue(cpr.getCherryPickAssayPlates().first().isCancelled());
        assertTrue(Iterables.all(cpr.getLabCherryPicks(), new Predicate<LabCherryPick>() {
          public boolean apply(LabCherryPick lcp)
          {
            return lcp.isCancelled() && lcp.getWellVolumeAdjustments().isEmpty();
          };
        }));
      }
    });
  }

  public void testCherryPickPlatesFailedAndRecreated()
  {
    initializeAssayPlates();
    final int lastAssayPlateCount = cherryPickRequestViewer.getEntity().getCherryPickAssayPlates().size();
    final int lastLabCherryPickCount = cherryPickRequestViewer.getEntity().getLabCherryPicks().size();

    cherryPickRequestViewer.selectAllAssayPlates();
    cherryPickRequestViewer.recordFailedCreationOfAssayPlates();
    assertEquals(ScreensaverConstants.VIEW_CHERRY_PICK_REQUEST, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr);
        assertEquals(lastAssayPlateCount * 2, cpr.getCherryPickAssayPlates().size());
        assertEquals(lastLabCherryPickCount * 2, cpr.getLabCherryPicks().size());
        assertTrue(cpr.getCherryPickAssayPlates().first().isFailed());
        assertEquals(Integer.valueOf(0), cpr.getCherryPickAssayPlates().first().getPlateOrdinal());
        assertEquals(Integer.valueOf(0), cpr.getCherryPickAssayPlates().first().getAttemptOrdinal());
        assertTrue(Iterables.all(cpr.getCherryPickAssayPlates().first().getLabCherryPicks(), new Predicate<LabCherryPick>() {
          public boolean apply(LabCherryPick lcp)
          {
            return lcp.isFailed();
          };
        }));
        assertFalse(cpr.getCherryPickAssayPlates().last().isPlated());
        assertEquals(Integer.valueOf(0), cpr.getCherryPickAssayPlates().last().getPlateOrdinal());
        assertEquals(Integer.valueOf(1), cpr.getCherryPickAssayPlates().last().getAttemptOrdinal());
        assertTrue(Iterables.all(cpr.getCherryPickAssayPlates().last().getLabCherryPicks(), new Predicate<LabCherryPick>() {
          public boolean apply(LabCherryPick lcp)
          {
            return lcp.isMapped();
          };
        }));
      }
    });
  }

  private void initializeAssayPlates()
  {
    String input = Joiner.on("\n").appendTo(new StringBuilder(), Iterables.transform(_library.getWells(), Well.ToEntityId)).toString();
    log.info("cherry picks: " + input);
    cherryPickRequestViewer.viewEntity(_cpr);
    cherryPickRequestViewer.setCherryPicksInput(input);
    cherryPickRequestViewer.addCherryPicksForWells();
    CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr, true, CherryPickRequest.screenerCherryPicks.getPath());
    assertEquals(384, cpr.getScreenerCherryPicks().size());
    cherryPickRequestViewer.allocateCherryPicks();
    cpr = genericEntityDao.reloadEntity(_cpr, true, CherryPickRequest.labCherryPicks.getPath());
    assertEquals(384, cpr.getLabCherryPicks().size());
    assertEquals(0, cpr.getNumberUnfulfilledLabCherryPicks());
    cherryPickRequestViewer.plateMapCherryPicks();
    cpr = genericEntityDao.reloadEntity(_cpr, true, CherryPickRequest.cherryPickAssayPlates.getPath());
    assertEquals(1, cpr.getCherryPickAssayPlates().size());
  }
  
}
