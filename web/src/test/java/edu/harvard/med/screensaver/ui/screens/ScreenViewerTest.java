// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.io.IOException;

import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.ScreenStatus;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.screens.ScreenGenerator;
import edu.harvard.med.screensaver.ui.activities.ActivityViewer;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;


public class ScreenViewerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(ScreenViewerTest.class);
  
  @Autowired
  protected ScreenViewer screenViewer;
  @Autowired
  protected ScreenDetailViewer screenDetailViewer;
  @Autowired
  protected ScreenGenerator screenGenerator;
  @Autowired
  protected ActivityViewer activityViewer;

  private Screen _screen;
  private ScreeningRoomUser _screener;
  private LabHead _labHead;

  private AttachedFileType _genericAttachedFileType;
  
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();

    genericEntityDao.persistEntity(new ScreenAttachedFileType("Publication"));
    _genericAttachedFileType = genericEntityDao.mergeEntity(new ScreenAttachedFileType("Generic"));

    _screener = new ScreeningRoomUser(_admin);
    _screener.setFirstName("A");
    _screener.setLastName("B");
    _screener.setUserClassification(ScreeningRoomUserClassification.UNASSIGNED);
    _labHead = new LabHead(_admin);
    _labHead.setFirstName("X");
    _labHead.setLastName("Z");
    _screener = genericEntityDao.mergeEntity(_screener);
    _labHead = genericEntityDao.mergeEntity(_labHead);

    _screen = screenGenerator.createPrimaryScreen(_admin, null, ScreenType.SMALL_MOLECULE);
  }
  
  public void testNewScreen()
  {
    doNewScreen();
  }

  public void testAddRelatedScreen()
  {
    _screen.setProjectId("project1");
    doNewScreen();
    assertEquals(ScreensaverConstants.EDIT_SCREEN, screenDetailViewer.addRelatedScreen());
    screenDetailViewer.getEntity().setFacilityId("screen2");
    screenDetailViewer.getEntity().setProjectPhase(ProjectPhase.FOLLOW_UP_SCREEN);
    assertEquals(ScreensaverConstants.VIEW_SCREEN, screenDetailViewer.save());
    assertEquals("screen2", screenViewer.getEntity().getFacilityId());
    assertEquals(ScreenType.SMALL_MOLECULE, screenViewer.getEntity().getScreenType());
    assertEquals(_labHead, screenViewer.getEntity().getLabHead());
    assertEquals(_screener, screenViewer.getEntity().getLeadScreener());
    assertEquals(_screen.getTitle(), screenViewer.getEntity().getTitle());
    assertEquals("project1", screenViewer.getEntity().getProjectId());
    assertEquals(ProjectPhase.FOLLOW_UP_SCREEN, screenViewer.getEntity().getProjectPhase());
  }

  public void testAddLibraryScreening()
  {
    _admin = new AdministratorUser("Admin2", "User");
    _admin.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    _admin = genericEntityDao.mergeEntity(_admin);
    currentScreensaverUser.setScreensaverUser(_admin);

    doNewScreen();
    assertEquals(ScreensaverConstants.VIEW_ACTIVITY, screenDetailViewer.addLibraryScreening());
    // note: we're not going to test the LabActivityViewer in-depth here, since we're just trying to test the addLibraryScreening operation
    activityViewer.getEntity().setDateOfActivity(new LocalDate(2011, 2, 8));
    assertEquals(ScreensaverConstants.BROWSE_SCREENS, activityViewer.save());
    assertEquals(1, screenViewer.getEntity().getLabActivitiesOfType(LibraryScreening.class).size());
  }

  public void testAddDeleteStatusItems()
  {
    doNewScreen();

    assertEquals(ScreensaverConstants.EDIT_SCREEN, screenDetailViewer.edit());
    screenDetailViewer.setNewStatusItemDate(new LocalDate(2011, 1, 1));
    screenDetailViewer.getNewStatusItemValue().setSelection(ScreenStatus.ACCEPTED);
    screenDetailViewer.addStatusItem();
    assertEquals(ScreensaverConstants.BROWSE_SCREENS, screenDetailViewer.save());
    _screen = genericEntityDao.reloadEntity(_screen, true, Screen.statusItems);
    assertEquals(ScreenStatus.ACCEPTED, _screen.getStatusItems().first().getStatus());

    assertEquals(ScreensaverConstants.EDIT_SCREEN, screenDetailViewer.edit());
    screenDetailViewer.getEntity().getStatusItems().remove(_screen.getStatusItems().first());
    assertEquals(ScreensaverConstants.BROWSE_SCREENS, screenDetailViewer.save());
    _screen = genericEntityDao.reloadEntity(_screen, true, Screen.statusItems);
    assertTrue(_screen.getStatusItems().isEmpty());
  }

  public void testAddDeleteAttachedFile() throws IOException
  {
    doNewScreen();

    assertEquals(ScreensaverConstants.EDIT_SCREEN, screenDetailViewer.edit());
    screenDetailViewer.getAttachedFiles().setNewAttachedFileName("data.txt");
    screenDetailViewer.getAttachedFiles().setNewAttachedFileContents("data, data");
    screenDetailViewer.getAttachedFiles().getNewAttachedFileType().setSelection(_genericAttachedFileType);
    screenDetailViewer.getAttachedFiles().addAttachedFile();
    assertEquals(ScreensaverConstants.BROWSE_SCREENS, screenDetailViewer.save());
    _screen = genericEntityDao.reloadEntity(_screen, true, Screen.attachedFiles.to(AttachedFile.fileType));
    AttachedFile attachedFile = Iterables.get(_screen.getAttachedFiles(), 0);
    assertEquals("data.txt", attachedFile.getFilename());
    assertEquals("data, data", IOUtils.toString(attachedFile.getFileContents()));
    assertEquals("Generic", attachedFile.getFileType().getValue());
    
    assertEquals(ScreensaverConstants.EDIT_SCREEN, screenDetailViewer.edit());
    AttachedFile attachedFile2 = Iterables.get(screenDetailViewer.getEntity().getAttachedFiles(), 0);
    screenDetailViewer.getEntity().removeAttachedFile(attachedFile2);
    assertEquals(ScreensaverConstants.BROWSE_SCREENS, screenDetailViewer.save());
    _screen = genericEntityDao.reloadEntity(_screen, true, Screen.attachedFiles);
    assertTrue(_screen.getAttachedFiles().isEmpty());
  }

  private void doNewScreen()
  {
    assertEquals(ScreensaverConstants.EDIT_SCREEN, screenDetailViewer.editNewEntity(_screen));
    screenDetailViewer.getEntity().setFacilityId("screen1");
    screenDetailViewer.getEntity().setTitle("title");
    screenDetailViewer.getLabName().setSelection(_labHead);
    screenDetailViewer.getLeadScreener().setSelection(_screener);
    assertEquals(ScreensaverConstants.BROWSE_SCREENS, screenDetailViewer.save());
    _screen = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", "screen1");
    assertNotNull(_screen);
  }
}
