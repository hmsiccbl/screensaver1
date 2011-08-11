// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;

public class CherryPickRequestViewerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(CherryPickRequestViewerTest.class);
  
  @Autowired
  protected UsersDAO usersDao;
  @Autowired
  protected CherryPickRequestViewer cherryPickRequestViewer;
  @Autowired
  protected ActivityViewer activityViewer;
  @Autowired
  protected PlateUpdater _plateUpdater;

  private ScreeningRoomUser _screener;
  private CherryPickRequest _cpr;
  private Library _library;


  protected void setUp() throws Exception
  {
    super.setUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = genericEntityDao.mergeEntity(_admin);
        currentScreensaverUser.setScreensaverUser(_admin);
        _screener = new LabHead(_admin);
        _screener.setFirstName("Lab");
        _screener.setLastName("Head");
        genericEntityDao.persistEntity(_screener);
        _library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        Plate plate = _library.createCopy(_admin, CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "A").findPlate(1000).withWellVolume(new Volume(1000));
        genericEntityDao.persistEntity(_library);
        genericEntityDao.flush();
        _plateUpdater.updatePlateStatus(plate, PlateStatus.AVAILABLE, _admin, _admin, new LocalDate());
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
    assertEquals(ScreensaverConstants.BROWSE_CHERRY_PICK_REQUESTS, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity());
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
    List<SelectableRow<CherryPickAssayPlate>> cpapRows = (List<SelectableRow<CherryPickAssayPlate>>) activityViewer.getCherryPickPlatesDataModel().getWrappedData();
    assertEquals(1, cpapRows.size());
    assertEquals("Lab Head (1) CP" + _cpr.getEntityId() + "  Plate 01 of 1", cpapRows.get(0).getData().getName());
    activityViewer.getEntity().setDateOfActivity(new LocalDate(2010, 1, 1));
    assertEquals(ScreensaverConstants.BROWSE_CHERRY_PICK_REQUESTS, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity());
        assertTrue(cpr.getCherryPickAssayPlates().first().isPlatedAndScreened());
        List<CherryPickScreening> screenings = Lists.newArrayList(cpr.getCherryPickAssayPlates().first().getCherryPickScreenings());
        assertEquals(1, screenings.size());
        assertEquals(new LocalDate(2010, 1, 1), screenings.get(0).getDateOfActivity());
      }
    });
    
    // test multiple screening of a CPAP
    cherryPickRequestViewer.viewEntity(_cpr);
    cherryPickRequestViewer.selectAllAssayPlates();
    cherryPickRequestViewer.recordScreeningOfAssayPlates();
    activityViewer.getEntity().setDateOfActivity(new LocalDate(2011, 1, 1));
    assertEquals(ScreensaverConstants.BROWSE_CHERRY_PICK_REQUESTS, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity());
        assertTrue(cpr.getCherryPickAssayPlates().first().isPlatedAndScreened());
        List<CherryPickScreening> screenings = Lists.newArrayList(cpr.getCherryPickAssayPlates().first().getCherryPickScreenings());
        assertEquals(2, screenings.size());
        assertEquals(new LocalDate(2010, 1, 1), screenings.get(0).getDateOfActivity());
        assertEquals(new LocalDate(2011, 1, 1), screenings.get(1).getDateOfActivity());
      }
    });
    
  }

  public void testCherryPickPlatesCanceled()
  {
    initializeAssayPlates();
    cherryPickRequestViewer.selectAllAssayPlates();
    cherryPickRequestViewer.deallocateCherryPicksByPlate();
    assertEquals(ScreensaverConstants.BROWSE_CHERRY_PICK_REQUESTS, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity());
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
    assertEquals(ScreensaverConstants.BROWSE_CHERRY_PICK_REQUESTS, activityViewer.save());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        CherryPickRequest cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity());
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
    initializeLabCherryPicks();
    cherryPickRequestViewer.plateMapCherryPicks();
    CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr, true, CherryPickRequest.cherryPickAssayPlates);
    assertEquals(1, cpr.getCherryPickAssayPlates().size());
  }

  protected void initializeLabCherryPicks()
  {
    String input = Joiner.on("\n").appendTo(new StringBuilder(), Iterables.transform(_library.getWells(), Well.ToEntityId)).toString();
    log.info("cherry picks: " + input);
    cherryPickRequestViewer.viewEntity(_cpr);
    cherryPickRequestViewer.setCherryPicksInput(input);
    cherryPickRequestViewer.addCherryPicksForWells();
    CherryPickRequest cpr = genericEntityDao.reloadEntity(_cpr, true, CherryPickRequest.screenerCherryPicks);
    assertEquals(384, cpr.getScreenerCherryPicks().size());
    cherryPickRequestViewer.allocateCherryPicks();
    cpr = genericEntityDao.reloadEntity(_cpr, true, CherryPickRequest.labCherryPicks);
    assertEquals(384, cpr.getLabCherryPicks().size());
    assertEquals(0, cpr.getNumberUnfulfilledLabCherryPicks());
  }
  

  public void testEditLabCherryPickSourceCopy()
  {
    initializeLabCherryPicks();

    _library.createCopy(_admin, CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "B").findPlate(1000).withWellVolume(new Volume(500));
    _library.createCopy(_admin, CopyUsageType.LIBRARY_SCREENING_PLATES, "C").findPlate(1000).withWellVolume(new Volume(1000));
    _library = genericEntityDao.mergeEntity(_library);

    Iterator<LabCherryPick> iter = cherryPickRequestViewer.getEntity().getLabCherryPicks().iterator();
    LabCherryPick lcp1 = iter.next();
    LabCherryPick lcp2 = iter.next();
    

    cherryPickRequestViewer.getLabCherryPicksSearchResult().edit();
    assertEquals("A", genericEntityDao.reloadEntity(lcp1, true, LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy)).getSourceCopy().getName());
    ((TextEntityColumn<LabCherryPick>) cherryPickRequestViewer.getLabCherryPicksSearchResult().getColumnManager().getColumn("Source Copy")).setCellValue(lcp1, "B");
    cherryPickRequestViewer.getLabCherryPicksSearchResult().setLabCherryPickSourceCopyUpdateComments("update1");
    cherryPickRequestViewer.getLabCherryPicksSearchResult().save();
    assertEquals("B", genericEntityDao.reloadEntity(lcp1, true, LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy)).getSourceCopy().getName());
    CherryPickRequest cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity(), true, CherryPickRequest.updateActivities.castToSubtype(CherryPickRequest.class));
    assertEquals("updated source copy for lab cherry pick(s): " + lcp1.getSourceWell().getWellKey() + " from A to B",
                 cpr.getUpdateActivitiesOfType(AdministrativeActivityType.LAB_CHERRY_PICK_SOURCE_COPY_OVERRIDE).last().getComments());
    assertEquals("update1", cpr.getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT).last().getComments());

    cherryPickRequestViewer.getLabCherryPicksSearchResult().edit();
    ((TextEntityColumn<LabCherryPick>) cherryPickRequestViewer.getLabCherryPicksSearchResult().getColumnManager().getColumn("Source Copy")).setCellValue(lcp1, null);
    cherryPickRequestViewer.getLabCherryPicksSearchResult().setLabCherryPickSourceCopyUpdateComments("update2");
    cherryPickRequestViewer.getLabCherryPicksSearchResult().save();
    assertFalse(genericEntityDao.reloadEntity(lcp1, true, LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy)).isAllocated());
    cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity(), true, CherryPickRequest.updateActivities.castToSubtype(CherryPickRequest.class));
    assertEquals("updated source copy for lab cherry pick(s): " + lcp1.getSourceWell().getWellKey() + " from B to <none>",
                 cpr.getUpdateActivitiesOfType(AdministrativeActivityType.LAB_CHERRY_PICK_SOURCE_COPY_OVERRIDE).last().getComments());
    assertEquals("update2", cpr.getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT).last().getComments());

    cherryPickRequestViewer.getLabCherryPicksSearchResult().edit();
    ((TextEntityColumn<LabCherryPick>) cherryPickRequestViewer.getLabCherryPicksSearchResult().getColumnManager().getColumn("Source Copy")).setCellValue(lcp1, "A");
    ((TextEntityColumn<LabCherryPick>) cherryPickRequestViewer.getLabCherryPicksSearchResult().getColumnManager().getColumn("Source Copy")).setCellValue(lcp2, "B");
    cherryPickRequestViewer.getLabCherryPicksSearchResult().setLabCherryPickSourceCopyUpdateComments("update3");
    cherryPickRequestViewer.getLabCherryPicksSearchResult().save();
    assertEquals("A", genericEntityDao.reloadEntity(lcp1, true, LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy)).getSourceCopy().getName());
    assertEquals("B", genericEntityDao.reloadEntity(lcp2, true, LabCherryPick.wellVolumeAdjustments.to(WellVolumeAdjustment.copy)).getSourceCopy().getName());
    cpr = genericEntityDao.reloadEntity(cherryPickRequestViewer.getEntity(), true, CherryPickRequest.updateActivities.castToSubtype(CherryPickRequest.class));
    // TODO: the ordering of the lab cherry pick comments is not deterministic; need to update assertion to account for this (sometimes fails)
    assertEquals("updated source copy for lab cherry pick(s): " + lcp1.getSourceWell().getWellKey() + " from <none> to A, " +
                 lcp2.getSourceWell().getWellKey() + " from A to B",
                 cpr.getUpdateActivitiesOfType(AdministrativeActivityType.LAB_CHERRY_PICK_SOURCE_COPY_OVERRIDE).last().getComments());
    assertEquals("update3", cpr.getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT).last().getComments());

    cherryPickRequestViewer.getMessages().getQueuedMessages().clear();
    cherryPickRequestViewer.getLabCherryPicksSearchResult().edit();
    ((TextEntityColumn<LabCherryPick>) cherryPickRequestViewer.getLabCherryPicksSearchResult().getColumnManager().getColumn("Source Copy")).setCellValue(lcp1, "D");
    cherryPickRequestViewer.getLabCherryPicksSearchResult().save();
    assertEquals("No copy D for plate 1000", cherryPickRequestViewer.getMessages().getQueuedMessages().get(0).getSecond().getSummary());
  }
}
