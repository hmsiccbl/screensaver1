// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/infrastructure-upgrade/test/edu/harvard/med/screensaver/ui/users/UserViewerTest.java $
// $Id: UserViewerTest.java 5202 2011-01-21 22:16:57Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.List;

import javax.faces.model.SelectItem;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivityType;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.screens.ScreenGenerator;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;
import edu.harvard.med.screensaver.ui.screens.ScreenDetailViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;

public class ActivityViewerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(ActivityViewerTest.class);
  
  @Autowired
  protected ScreenViewer screenViewer;
  @Autowired
  protected ScreenDetailViewer screenDetailViewer;
  @Autowired
  protected ScreenGenerator screenGenerator;
  @Autowired
  protected ActivityViewer activityViewer;
  @Autowired
  protected UserViewer userViewer;

  private Screen _screen;
  private ScreeningRoomUser _screener;
  private LabHead _labHead;
  private LibraryScreening _libraryScreening;
  private Library _library1;
  private Library _library2;
  
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();

    // for testServiceActivity
    _admin.addScreensaverUserRole(ScreensaverUserRole.SERVICE_ACTIVITY_ADMIN);
    _admin = genericEntityDao.mergeEntity(_admin);
    currentScreensaverUser.setScreensaverUser(_admin);

    _screener = new ScreeningRoomUser(_admin);
    _screener.setFirstName("A");
    _screener.setLastName("B");
    _screener.setUserClassification(ScreeningRoomUserClassification.UNASSIGNED);
    _screener = genericEntityDao.mergeEntity(_screener);

    _library1 = dataFactory.newInstance(Library.class);
    _library1.setStartPlate(1);
    _library1.setEndPlate(2);
    _library1.createCopy(_admin, CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    _library1 = genericEntityDao.mergeEntity(_library1);

    _library2 = dataFactory.newInstance(Library.class);
    _library2.setStartPlate(3);
    _library2.setEndPlate(4);
    _library2.createCopy(_admin, CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    _library2 = genericEntityDao.mergeEntity(_library2);

    _screen = screenGenerator.createPrimaryScreen(_admin, null, ScreenType.SMALL_MOLECULE);
    _screen.setFacilityId("1");
    _screen.setTitle("screen");
    _screen.setLeadScreener(_screener);
    _libraryScreening = _screen.createLibraryScreening(_admin, _screener, new LocalDate());
    _screen = genericEntityDao.mergeEntity(_screen);
    _libraryScreening = (LibraryScreening) _screen.getLabActivities().first();
  }
  
  public void testAddDeleteAssayPlates()
  {
    activityViewer.viewEntity(_libraryScreening);
    ((LibraryScreening) activityViewer.getEntity()).setNumberOfReplicates(2);
    activityViewer.getEntity().setDateOfActivity(new LocalDate());
    activityViewer.setNewPlateRangeScreenedStartPlate(1);
    activityViewer.setNewPlateRangeScreenedEndPlate(2);
    activityViewer.getNewPlateRangeScreenedCopy().setSelection("A");
    activityViewer.addNewPlateRangeScreened();
    activityViewer.setNewPlateRangeScreenedStartPlate(4);
    activityViewer.setNewPlateRangeScreenedEndPlate(4);
    activityViewer.getNewPlateRangeScreenedCopy().setSelection("A");
    activityViewer.addNewPlateRangeScreened();
    activityViewer.save();

    _libraryScreening = genericEntityDao.reloadEntity((LibraryScreening) activityViewer.getEntity(), true, LibraryScreening.assayPlatesScreened);
    assertEquals(Sets.newHashSet(1, 2, 4),
                 Sets.newHashSet(Iterables.transform(_libraryScreening.getAssayPlatesScreened(), AssayPlate.ToPlateNumber)));

    activityViewer.viewEntity(_libraryScreening);
    activityViewer.getPlatesScreenedDataModel().setRowIndex(1);
    activityViewer.deletePlateRange();
    activityViewer.save();

    _libraryScreening = genericEntityDao.reloadEntity((LibraryScreening) activityViewer.getEntity(), true, LibraryScreening.assayPlatesScreened);
    assertEquals(Sets.newHashSet(1, 2),
                 Sets.newHashSet(Iterables.transform(_libraryScreening.getAssayPlatesScreened(), AssayPlate.ToPlateNumber)));

    activityViewer.viewEntity(_libraryScreening);
    activityViewer.getPlatesScreenedDataModel().setRowIndex(0);
    activityViewer.deletePlateRange();
    activityViewer.setNewPlateRangeScreenedStartPlate(2);
    activityViewer.setNewPlateRangeScreenedEndPlate(2);
    activityViewer.getNewPlateRangeScreenedCopy().setSelection("A");
    activityViewer.addNewPlateRangeScreened();
    activityViewer.setNewPlateRangeScreenedStartPlate(3);
    activityViewer.setNewPlateRangeScreenedEndPlate(4);
    activityViewer.getNewPlateRangeScreenedCopy().setSelection("A");
    activityViewer.addNewPlateRangeScreened();
    activityViewer.save();

    _libraryScreening = genericEntityDao.reloadEntity((LibraryScreening) activityViewer.getEntity(), true, LibraryScreening.assayPlatesScreened);
    assertEquals(Sets.newHashSet(2, 3, 4),
                 Sets.newHashSet(Iterables.transform(_libraryScreening.getAssayPlatesScreened(), AssayPlate.ToPlateNumber)));
  }

  public void testServiceActivity()
  {
    userViewer.viewEntity(_screener);
    assertEquals(ScreensaverConstants.VIEW_ACTIVITY, userViewer.addServiceActivity());
    List<SelectItem> selectItems = activityViewer.getServicedScreen().getSelectItems();
    assertEquals("", selectItems.get(0).getValue());
    assertEquals("<none>", selectItems.get(0).getLabel());
    assertEquals(_screen.getScreenId().toString(), selectItems.get(1).getValue());
    assertEquals(_screen.getFacilityId() + ": " + _screen.getTitle(), selectItems.get(1).getLabel());
    activityViewer.getServicedScreen().setValue(_screen.getScreenId().toString());
    activityViewer.getEntity().setDateOfActivity(new LocalDate(2011, 1, 1));
    ((ServiceActivity) activityViewer.getEntity()).setType(ServiceActivityType.MEDCHEM_CONSULT);
    // note: this assertion also initializes the performedBy UISelectOneBean, which is required for save() to succeed
    assertEquals(_admin, activityViewer.getPerformedBy().getSelection());
    assertEquals(ScreensaverConstants.BROWSE_SCREENERS, activityViewer.save());
    assertTrue(userViewer.getContextualSearchResults().isEntityView());
    assertEquals(2, userViewer.getUserActivitiesCount());
    ScreeningRoomUser screener2 = new EntityInflator<ScreeningRoomUser>(genericEntityDao, _screener, true).
      need(ScreeningRoomUser.serviceActivities.to(Activity.performedBy)).
      need(ScreeningRoomUser.serviceActivities.to(ServiceActivity.servicedUser)).
      need(ScreeningRoomUser.serviceActivities.to(ServiceActivity.servicedScreen)).
      inflate();
    assertEquals(1, screener2.getServiceActivities().size());
    ServiceActivity serviceActivity = screener2.getServiceActivities().first();
    assertEquals(new LocalDate(2011, 1, 1), serviceActivity.getDateOfActivity());
    assertEquals(_admin, serviceActivity.getPerformedBy());
    assertEquals(_screener, serviceActivity.getServicedUser());
    assertEquals(genericEntityDao.reloadEntity(_screen), serviceActivity.getServicedScreen());

    assertEquals(ScreensaverConstants.BROWSE_ACTIVITIES, userViewer.browseUserActivities());
    EntitySearchResults<Activity,Activity,?> activitiesBrowser = activityViewer.getContextualSearchResults();
    assertEquals(2, activitiesBrowser.getRowCount());
    activitiesBrowser.getDataTableModel().setRowIndex(1);
    activitiesBrowser.getColumnManager().getVisibleColumnModel().setRowIndex(0);
    assertEquals(ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT, activitiesBrowser.cellAction());
    assertEquals(serviceActivity, activityViewer.getEntity());
    activityViewer.edit();
    ((ServiceActivity) activityViewer.getEntity()).setType(ServiceActivityType.AUTOMATION);
    assertEquals(ScreensaverConstants.BROWSE_ACTIVITIES, activityViewer.save());
    assertEquals(ServiceActivityType.AUTOMATION, genericEntityDao.reloadEntity(serviceActivity).getType());
  }
}
