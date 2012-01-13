// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.hibernate.LazyInitializationException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestCherryPicksAdder;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestPlateMapper;
import edu.harvard.med.screensaver.service.screens.ScreenDerivedPropertiesUpdater;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class ScreenTest extends AbstractEntityInstanceTest<Screen>
{
  @Autowired
  protected CherryPickRequestAllocator cherryPickRequestAllocator;
  @Autowired
  protected CherryPickRequestPlateMapper cherryPickRequestPlateMapper;
  @Autowired
  protected CherryPickRequestCherryPicksAdder cherryPickRequestCherryPicksAdder;
  @Autowired
  protected ScreenDerivedPropertiesUpdater screenDerivedPropertiesUpdater;

  public static TestSuite suite()
  {
    return buildTestSuite(ScreenTest.class, Screen.class);
  }

  public ScreenTest()
  {
    super(Screen.class);
  }

  public void testGetLabActivities() throws Exception
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    AdministratorUser admin = new AdministratorUser("Admin", "User");
    LibraryScreening screening1 = screen.createLibraryScreening(admin,
                                                                screen.getLeadScreener(),
                                                                new LocalDate(2007, 3, 7));
    LibraryScreening screening2 = screen.createLibraryScreening(admin,
                                                                screen.getLeadScreener(),
                                                                new LocalDate(2007, 3, 8));
    /*CherryPickRequest cpr =*/ screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy(),
                                                               screen.getLeadScreener(),
                                                               new LocalDate(2007, 3, 9));
    CherryPickLiquidTransfer cplt = screen.createCherryPickLiquidTransfer(admin,
                                                                          MakeDummyEntities.makeDummyUser(screen.getFacilityId(), "Lab", "Guy"),
                                                                          new LocalDate(),
                                                                          CherryPickLiquidTransferStatus.SUCCESSFUL);

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
    schemaUtil.truncateTables();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(107);
        ScreenResult screenResult = screen.createScreenResult();
        screenResult.createDataColumn("Luminescence");
        screenResult.createDataColumn("FI");
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });

    Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "107");
    ScreenResult screenResult = screen.getScreenResult(); // should not cause LazyInitEx here
    try {
      screenResult.getDataColumns().iterator();
      fail("expected screenResult.dataColumns to be uninitialized");
    }
    catch (LazyInitializationException e) {}
  }

  /**
   * Tests that no problems occur when Hibernate applies cascades to
   * leadScreener and labHead relationships. Regression test for problems that
   * were occurring with these cascades.
   */
  public void testScreenToScreenerCascades()
  {
    schemaUtil.truncateTables();
    Screen screen1a = MakeDummyEntities.makeDummyScreen(1);
    genericEntityDao.persistEntity(screen1a);
    Screen screen1b = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1", true, Screen.labHead);
    assertEquals(screen1a.getLabHead().getEntityId(), screen1b.getLabHead().getEntityId());
    screen1b = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1", true, Screen.leadScreener);
    assertEquals(screen1a.getLeadScreener().getEntityId(), screen1b.getLeadScreener().getEntityId());

    Screen screen2a = MakeDummyEntities.makeDummyScreen(2);
    screen2a.setLeadScreener(screen2a.getLabHead());
    genericEntityDao.persistEntity(screen2a);
    Screen screen2b = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "2", true, Screen.labHead);
    assertEquals(screen2a.getLabHead().getEntityId(), screen2b.getLabHead().getEntityId());
    screen2b = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "2", true, Screen.leadScreener);
    assertEquals(screen2a.getLabHead().getEntityId(), screen2b.getLeadScreener().getEntityId());
  }

  public void testCandidateStatusItems()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    Set<ScreenStatus> expected = new HashSet<ScreenStatus>(Arrays.asList(ScreenStatus.values()));
    assertEquals(expected, new HashSet<ScreenStatus>(screen.getCandidateStatuses()));

    LocalDate today = new LocalDate();
    screen.createStatusItem(today, ScreenStatus.PENDING_ICCB);
    expected.remove(ScreenStatus.PENDING_LEGACY);
    expected.remove(ScreenStatus.PENDING_ICCB);
    expected.remove(ScreenStatus.PENDING_NSRB);
    assertEquals(expected, new HashSet<ScreenStatus>(screen.getCandidateStatuses()));

    screen.createStatusItem(today, ScreenStatus.PILOTED);
    expected.remove(ScreenStatus.PILOTED);
    assertEquals(expected, new HashSet<ScreenStatus>(screen.getCandidateStatuses()));

    screen.createStatusItem(today, ScreenStatus.ACCEPTED);
    expected.remove(ScreenStatus.ACCEPTED);
    assertEquals(expected, new HashSet<ScreenStatus>(screen.getCandidateStatuses()));

    screen.createStatusItem(today, ScreenStatus.ONGOING);
    expected.remove(ScreenStatus.ONGOING);
    assertEquals(expected, new HashSet<ScreenStatus>(screen.getCandidateStatuses()));

    screen.createStatusItem(today, ScreenStatus.COMPLETED);
    assertEquals(1, screen.getCandidateStatuses().size());
    
    screen.createStatusItem(today, ScreenStatus.TRANSFERRED_TO_BROAD_INSTITUTE);
    assertEquals(0, screen.getCandidateStatuses().size());
  }

  public void testAddAnachronisticStatusItem()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);

    screen.createStatusItem(new LocalDate(2008, 6, 2), ScreenStatus.ONGOING);
    try {
      screen.createStatusItem(new LocalDate(2008, 6, 1), ScreenStatus.COMPLETED);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      assertEquals("date of new screen status must not be before the date of the previous screen status", e.getMessage());
    }

    try {
      screen.createStatusItem(new LocalDate(2008, 6, 3), ScreenStatus.ACCEPTED);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      assertEquals("date of new screen status must not be after date of subsequent screen status", e.getMessage());
    }
  }

  public void testAddConflictingStatusItem()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);

    screen.createStatusItem(new LocalDate(2008, 6, 2), ScreenStatus.PENDING_ICCB);
    try {
      screen.createStatusItem(new LocalDate(2008, 6, 2), ScreenStatus.PENDING_NSRB);
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {
      assertEquals("screen status Pending - NSRB is mutually exclusive with existing screen status Pending - ICCB-L",
                   e.getMessage());    
    }
  }

  public void testHoldToCompletedStatus()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);

    screen.createStatusItem(new LocalDate(2008, 6, 1), ScreenStatus.ACCEPTED);
    screen.createStatusItem(new LocalDate(2008, 6, 2), ScreenStatus.ONGOING);
    screen = genericEntityDao.mergeEntity(screen);
    assertEquals(ScreenStatus.ONGOING, screen.getCurrentStatusItem().getStatus());
    screen.createStatusItem(new LocalDate(2008, 6, 3), ScreenStatus.HOLD);
    screen = genericEntityDao.mergeEntity(screen);
    StatusItem holdStatusItem = screen.getCurrentStatusItem();
    assertEquals(ScreenStatus.HOLD, holdStatusItem.getStatus());
    screen.getStatusItems().remove(holdStatusItem);
    screen.createStatusItem(new LocalDate(2008, 6, 3), ScreenStatus.COMPLETED);
    assertEquals(ScreenStatus.COMPLETED, screen.getCurrentStatusItem().getStatus());
    screen = genericEntityDao.mergeEntity(screen);
    assertEquals(ScreenStatus.COMPLETED, screen.getCurrentStatusItem().getStatus());
    assertEquals(Sets.newHashSet(ScreenStatus.ACCEPTED, ScreenStatus.ONGOING, ScreenStatus.COMPLETED),
                 Sets.newHashSet(Iterables.transform(screen.getStatusItems(),
                                                     new Function<StatusItem,ScreenStatus>() {
                                                       public ScreenStatus apply(StatusItem si)
                   {
                     return si.getStatus();
                   }
                                                     })));
  }

  public void testAddAndDeleteAttachedFiles() throws IOException
  {
    schemaUtil.truncateTables();
    final Screen screen = MakeDummyEntities.makeDummyScreen(1);
    ScreenAttachedFileType attachedFileType = new ScreenAttachedFileType("Screener Correspondence");
    genericEntityDao.persistEntity(attachedFileType);
    screen.createAttachedFile("file1.txt", attachedFileType, new LocalDate(), "file1 contents");
    genericEntityDao.persistEntity(screen);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1", true, Screen.attachedFiles);
        assertEquals("add attached file to transient screen", 1, screen.getAttachedFiles().size());
        try {
          assertEquals("attached file contents accessible",
                       "file1 contents",
                       IOUtils.toString(screen.getAttachedFiles().iterator().next().getFileContents()));
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen1 = genericEntityDao.mergeEntity(screen);
        Iterator<AttachedFile> iter = screen1.getAttachedFiles().iterator();
        AttachedFile attachedFile = iter.next();
        screen1.getAttachedFiles().remove(attachedFile);
      }
    });
    Screen screen2 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1", true, Screen.attachedFiles);
    assertEquals("delete attached file from detached screen", 0, screen2.getAttachedFiles().size());
  }
  
  public void testPinTransferApproved()
  {
    schemaUtil.truncateTables();
    AdministratorUser recorderAdmin = new AdministratorUser("Recorder", "Admin");
    AdministratorUser approverAdmin = new AdministratorUser("Approver", "Admin");
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    screen.setPinTransferApproved(recorderAdmin,
                                  approverAdmin,
                                  new LocalDate(2009, 1, 1),
                                  "comments");
    assertEquals("Recorder", screen.getPinTransferApprovalActivity().getCreatedBy().getFirstName());
    assertEquals("Approver", screen.getPinTransferApprovalActivity().getPerformedBy().getFirstName());
    assertEquals(new LocalDate(2009, 1, 1), screen.getPinTransferApprovalActivity().getDateOfActivity());
    assertEquals(new LocalDate(), screen.getPinTransferApprovalActivity().getDateCreated().toLocalDate());
    assertEquals("comments", screen.getPinTransferApprovalActivity().getComments());
  }

  public void testBillingItems() 
  {
    schemaUtil.truncateTables();
    Screen screen = dataFactory.newInstance(Screen.class);
    LocalDate date = new LocalDate(2000, 1, 1);
    screen.createBillingItem("item1", new BigDecimal("1.11"), date);
    screen.createBillingItem("item2", new BigDecimal("2.22"), date);
    screen.addCopyOfBillingItem(new BillingItem("item3", new BigDecimal("3.33"), date));
    genericEntityDao.mergeEntity(screen);
    
    Screen screen2 = genericEntityDao.findEntityById(Screen.class, screen.getEntityId(), true, Screen.billingItems);
    assertEquals(Lists.newArrayList(new BillingItem("item1", new BigDecimal("1.11"), date),
                                    new BillingItem("item2", new BigDecimal("2.22"), date),
                                    new BillingItem("item3", new BigDecimal("3.33"), date)),
                                    screen2.getBillingItems());
    
    screen2.getBillingItems().remove(screen2.getBillingItems().get(1));
    genericEntityDao.mergeEntity(screen2);
    
    Screen screen3 = genericEntityDao.findEntityById(Screen.class, screen.getEntityId(), true, Screen.billingItems);
    assertEquals(Lists.newArrayList(new BillingItem("item1", new BigDecimal("1.11"), date),
                                    new BillingItem("item3", new BigDecimal("3.33"), date)),
                 screen3.getBillingItems());
  }
  
  public void testScreeningStatusCounts()
  {
    schemaUtil.truncateTables();

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = MakeDummyEntities.makeDummyScreen(1);
        Screen screen2 = MakeDummyEntities.makeDummyScreen(2);
        genericEntityDao.persistEntity(screen1);
        genericEntityDao.persistEntity(screen2);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
        Screen screen2 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "2");
        AdministratorUser admin = new AdministratorUser("Admin", "User"); 
        ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User");
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 4);
        Copy copy = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Plate plate1000 = copy.findPlate(1000).withWellVolume(new Volume(0));
        Plate plate1001 = copy.findPlate(1001).withWellVolume(new Volume(0));
        Plate plate1002 = copy.findPlate(1002).withWellVolume(new Volume(0));
        Plate plate1003 = copy.findPlate(1003).withWellVolume(new Volume(0));
        genericEntityDao.persistEntity(library);

        LibraryScreening libraryScreening;
        libraryScreening = screen1.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        libraryScreening.addAssayPlatesScreened(plate1001);
        libraryScreening.addAssayPlatesScreened(plate1002);
        genericEntityDao.saveOrUpdateEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        libraryScreening = screen1.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1001);
        libraryScreening.addAssayPlatesScreened(plate1002);
        genericEntityDao.saveOrUpdateEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        libraryScreening = screen2.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        libraryScreening.addAssayPlatesScreened(plate1001);
        libraryScreening.addAssayPlatesScreened(plate1002);
        libraryScreening.addAssayPlatesScreened(plate1003);
        genericEntityDao.persistEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        screenDerivedPropertiesUpdater.updateScreeningStatistics(screen1);
      }
    });
    
    Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    assertEquals(384 * 5, screen.getScreenedExperimentalWellCount());
    assertEquals(384 * 3, screen.getUniqueScreenedExperimentalWellCount());
    assertEquals(5, screen.getAssayPlatesScreenedCount());
    assertEquals(3, screen.getLibraryPlatesScreenedCount());
    // TODO (test in screen result loader test)
    assertEquals(0, screen.getLibraryPlatesDataLoadedCount());
    assertEquals(0, screen.getLibraryPlatesDataAnalyzedCount());
    assertEquals(1, screen.getLibrariesScreenedCount());
    assertEquals(Integer.valueOf(1), screen.getMinScreenedReplicateCount());
    assertEquals(Integer.valueOf(1), screen.getMaxScreenedReplicateCount());
    assertEquals(null, screen.getMinDataLoadedReplicateCount());
    assertEquals(null, screen.getMaxDataLoadedReplicateCount());
    
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
        LibraryScreening libraryScreening = screen.getLabActivitiesOfType(LibraryScreening.class).first();
        assertTrue(libraryScreening.removeAssayPlatesScreened(libraryScreening.getAssayPlatesScreened().first().getPlateScreened()));
        screenDerivedPropertiesUpdater.updateScreeningStatistics(screen);
        assertEquals(384 * 4, screen.getScreenedExperimentalWellCount());
        assertEquals(384 * 2, screen.getUniqueScreenedExperimentalWellCount());
        assertEquals(4, screen.getAssayPlatesScreenedCount());
        assertEquals(2, screen.getLibraryPlatesScreenedCount());
        assertEquals(Integer.valueOf(1), screen.getMinScreenedReplicateCount());
        assertEquals(Integer.valueOf(1), screen.getMaxScreenedReplicateCount());
        assertEquals(null, screen.getMinDataLoadedReplicateCount());
        assertEquals(null, screen.getMaxDataLoadedReplicateCount());
      }
    });
  }
  
  public void testMinMaxReplicateCounts()
  {
    schemaUtil.truncateTables();

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        genericEntityDao.persistEntity(screen);
        genericEntityDao.flush();
        genericEntityDao.clear();
        screen = genericEntityDao.reloadEntity(screen);
        AdministratorUser admin = new AdministratorUser("Admin", "User"); 
        ScreeningRoomUser screener = new ScreeningRoomUser("Screener", "User");

        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 4);
        Copy copy = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
        Plate plate1000 = copy.findPlate(1000).withWellVolume(new Volume(0));
        Plate plate1001 = copy.findPlate(1001).withWellVolume(new Volume(0));
        Plate plate1002 = copy.findPlate(1002).withWellVolume(new Volume(0));
        Plate plate1003 = copy.findPlate(1003).withWellVolume(new Volume(0));
        genericEntityDao.persistEntity(library);

        LibraryScreening libraryScreening;
        libraryScreening = screen.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(1);
        libraryScreening.addAssayPlatesScreened(plate1000);
        genericEntityDao.persistEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        libraryScreening = screen.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(2);
        libraryScreening.addAssayPlatesScreened(plate1001);
        genericEntityDao.persistEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        libraryScreening = screen.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(2);
        libraryScreening.addAssayPlatesScreened(plate1001);
        genericEntityDao.persistEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        libraryScreening = screen.createLibraryScreening(admin, screener, new LocalDate());
        libraryScreening.setNumberOfReplicates(3);
        libraryScreening.addAssayPlatesScreened(plate1002);
        genericEntityDao.persistEntity(libraryScreening); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening
        
        ScreenResult screenResult = screen.createScreenResult();
        Map<Integer,Integer> plateNumbersLoadedWithMaxReplicates = ImmutableMap.of(1000, 2, 1001, 2, 1002, 3, 1003, 4);
        AdministrativeActivity dataLoading = screenResult.createScreenResultDataLoading(admin, plateNumbersLoadedWithMaxReplicates, "comments");
        genericEntityDao.persistEntity(dataLoading); // persist new LibraryScreening before non-cascaded AssayPlate.libraryScreening

        screenDerivedPropertiesUpdater.updateScreeningStatistics(screen);
      }
    });
    
    Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", "1", true, Screen.assayPlates);
    assertEquals(8, screen.getAssayPlatesScreenedCount());
    assertEquals(11, screen.getAssayPlatesDataLoaded().size());
    assertEquals(3, screen.getLibraryPlatesScreenedCount());
    assertEquals(4, screen.getLibraryPlatesDataLoadedCount());
    assertEquals(1, screen.getLibrariesScreenedCount());
    assertEquals(Integer.valueOf(1), screen.getMinScreenedReplicateCount());
    assertEquals(Integer.valueOf(3), screen.getMaxScreenedReplicateCount());
    assertEquals(Integer.valueOf(2), screen.getMinDataLoadedReplicateCount());
    assertEquals(Integer.valueOf(4), screen.getMaxDataLoadedReplicateCount());
  }
  
  public void testTotalPlatedLabCherryPicksCount()
  {
    doTestTotalPlatedLabCherryPicksCount(CherryPickLiquidTransferStatus.CANCELED, 0);
    doTestTotalPlatedLabCherryPicksCount(CherryPickLiquidTransferStatus.FAILED, 0);
    doTestTotalPlatedLabCherryPicksCount(CherryPickLiquidTransferStatus.SUCCESSFUL, 384);
  }

  private void doTestTotalPlatedLabCherryPicksCount(final CherryPickLiquidTransferStatus status,
                                                    int expectedTotalPlatedLabCherryPicksCount)
  {
    schemaUtil.truncateTables();
    final int[] ids = new int[3];

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "A").findPlate(1000).withWellVolume(new Volume(1000, VolumeUnit.DEFAULT)).withStatus(PlateStatus.AVAILABLE);
        AdministratorUser admin = new AdministratorUser("Admin", "User");
        ScreeningRoomUser screener = new LabHead(admin);
        
        screener.setFirstName("Lab");
        screener.setLastName("Head");
        
        CherryPickRequest cpr = screen.createCherryPickRequest(admin, screener, new LocalDate());
        cpr.setTransferVolumePerWellApproved(new Volume(1));
        
        genericEntityDao.persistEntity(screener);
        genericEntityDao.persistEntity(admin);
        genericEntityDao.persistEntity(library);
        genericEntityDao.persistEntity(screen);

        ids[0] = library.getLibraryId();
        ids[1] = cpr.getCherryPickRequestId();
        ids[2] = admin.getScreensaverUserId();
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen1 = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
        Library library = genericEntityDao.findEntityById(Library.class, ids[0]);
        CherryPickRequest cpr = genericEntityDao.findEntityById(CherryPickRequest.class, ids[1]);

        assertNotNull(cpr);
        assertNotNull(library);
        assertNotNull(screen1);

        String input = Joiner.on("\n").appendTo(new StringBuilder(), Iterables.transform(library.getWells(), Well.ToEntityId)).toString();
        // steps from CherryPickRequestViewerTest.initializeAssayPlates
        // 1. add screener CPs; map to lab CPs
        // cherryPickRequestViewer.viewEntity(cpr);
        // cherryPickRequestViewer.setCherryPicksInput(input);
        // cherryPickRequestViewer.addCherryPicksForWells();
        PlateWellListParserResult result = PlateWellListParser.parseWellsFromPlateWellList(input);
        cpr = cherryPickRequestCherryPicksAdder.addCherryPicksForWells(cpr, result.getParsedWellKeys(), false);

        assertNotNull(cpr);
        assertEquals(384, cpr.getScreenerCherryPicks().size());

        // 2. reserve reagent
        //    cherryPickRequestViewer.allocateCherryPicks();
        cherryPickRequestAllocator.allocate(cpr);
        assertEquals(384, cpr.getLabCherryPicks().size());
        assertEquals(0, cpr.getNumberUnfulfilledLabCherryPicks());
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
        CherryPickRequest cpr = genericEntityDao.findEntityById(CherryPickRequest.class, ids[1]);
        AdministratorUser admin = genericEntityDao.findEntityById(AdministratorUser.class, ids[2]);

        // steps from CherryPickRequestViewerTest.initializeAssayPlates
        //    cherryPickRequestViewer.plateMapCherryPicks();
        cherryPickRequestPlateMapper.generatePlateMapping(cpr);
        cpr = genericEntityDao.reloadEntity(cpr, true, CherryPickRequest.cherryPickAssayPlates);
        assertEquals(1, cpr.getCherryPickAssayPlates().size());

        // actions in CPRV.recordSuccessfulCreationOfAssayPlates()
        CherryPickLiquidTransfer cplt = screen.createCherryPickLiquidTransfer(admin,
                                                                              MakeDummyEntities.makeDummyUser(screen.getFacilityId(), "Lab", "Guy"),
                                                                              new LocalDate(),
                                                                              status);
        for (CherryPickAssayPlate cpap : cpr.getActiveCherryPickAssayPlates()) {
          cplt.addCherryPickAssayPlate(cpap);
        }

        screenDerivedPropertiesUpdater.updateTotalPlatedLabCherryPickCount(screen);
      }
    });
    Screen screen = genericEntityDao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), "1");
    assertEquals(expectedTotalPlatedLabCherryPicksCount, screen.getTotalPlatedLabCherryPicks());
  }
  
}

