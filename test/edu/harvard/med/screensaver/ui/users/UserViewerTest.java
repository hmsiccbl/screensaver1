// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;

import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

public class UserViewerTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(UserViewerTest.class);
  
  protected UsersDAO usersDao;

  // instance data members

  private ScreensaverUser _admin;
  private CurrentScreensaverUser _currentScreensaverUser;
  
  
  // public constructors and methods
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
    genericEntityDao.persistEntity(_admin);
    _admin = genericEntityDao.reloadEntity(_admin, false, "activitiesPerformed");
    _currentScreensaverUser = new CurrentScreensaverUser();
    _currentScreensaverUser.setScreensaverUser(_admin);
  }
  
  public void testVirginChecklistItemsDataModel()
  {
    final UserViewer userViewer = new UserViewer(null, null, genericEntityDao, usersDao, null, null, null, null);
    userViewer.setCurrentScreensaverUser(_currentScreensaverUser);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        genericEntityDao.persistEntity(new ChecklistItem("lab access", true, ChecklistItemGroup.FORMS, 1));
        genericEntityDao.persistEntity(new ChecklistItem("trained", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 1));
        genericEntityDao.persistEntity(new ChecklistItem("ID assigned", true, ChecklistItemGroup.NON_HARVARD_SCREENERS, 2));
        genericEntityDao.persistEntity(new ChecklistItem("legacy", true, ChecklistItemGroup.LEGACY, 1));
        ScreeningRoomUser user = new ScreeningRoomUser("Test", "User", "test_user@hms.harvard.edu");
        genericEntityDao.persistEntity(user);
        genericEntityDao.flush();
        userViewer.setUser(user);
      }
    });
    Map<ChecklistItemGroup,DataModel> checklistItemDataModels = userViewer.getChecklistItemsDataModelMap();
    assertEquals(4, checklistItemDataModels.size());
    assertFalse(checklistItemDataModels.containsKey(ChecklistItemGroup.LEGACY));
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> group1 = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModels.get(ChecklistItemGroup.FORMS).getWrappedData();
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> group2 = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModels.get(ChecklistItemGroup.NON_HARVARD_SCREENERS).getWrappedData();
    assertNotNull(group1);
    assertNotNull(group2);
    assertEquals("lab access", group1.get(0).getKey().getItemName());
    assertNull("lab access", group1.get(0).getValue());
    assertEquals("trained", group2.get(0).getKey().getItemName());
    assertNull("trained", group2.get(0).getValue());
    assertEquals("ID assigned", group2.get(1).getKey().getItemName());
    assertNull("ID assigned", group2.get(1).getValue());
  }
  
  public void testPopulatedChecklistItemsDataModel()
  {    
    final LocalDate today = new LocalDate();
    final UserViewer userViewer = new UserViewer(null, null, genericEntityDao, usersDao, null, null, null, null);
    userViewer.setCurrentScreensaverUser(_currentScreensaverUser);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreeningRoomUser user = new ScreeningRoomUser("Test", "User", "test_user@hms.harvard.edu");
        genericEntityDao.persistEntity(user);
        ChecklistItem checklistItemType = new ChecklistItem("ID assigned", true, ChecklistItemGroup.NON_HARVARD_SCREENERS, 2);
        genericEntityDao.persistEntity(checklistItemType);
        genericEntityDao.persistEntity(new ChecklistItem("trained", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 1));
        ChecklistItemEvent item = 
          user.createChecklistItemActivationEvent(checklistItemType,
                                             today,
                                             new AdministrativeActivity(_admin, 
                                                                        today, 
                                                                        AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
        genericEntityDao.saveOrUpdateEntity(item);
        genericEntityDao.flush();
        userViewer.setUser(user);
      }
    });
    Map<ChecklistItemGroup,DataModel> checklistItemDataModels = userViewer.getChecklistItemsDataModelMap();
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> group = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModels.get(ChecklistItemGroup.NON_HARVARD_SCREENERS).getWrappedData();
    assertEquals("trained", group.get(0).getKey().getItemName());
    assertNull("trained", group.get(0).getValue());
    assertEquals("ID assigned", group.get(1).getKey().getItemName());
    assertEquals(today, group.get(1).getValue().getDatePerformed());
    assertEquals("Admin User", group.get(1).getValue().getEntryActivity().getPerformedBy().getFullNameFirstLast());
    assertFalse(group.get(1).getValue().isExpiration());
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ChecklistItemEvent activation = genericEntityDao.findAllEntitiesOfType(ChecklistItemEvent.class).get(0);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        ChecklistItemEvent expirationEvent = activation.createChecklistItemExpirationEvent(today,  
                                                      new AdministrativeActivity(_admin, 
                                                                                 today, 
                                                                                 AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
        genericEntityDao.saveOrUpdateEntity(expirationEvent);
        genericEntityDao.flush();
        userViewer.setUser(activation.getScreeningRoomUser());
      }
    });
    Map<ChecklistItemGroup,DataModel> checklistItemDataModels2 = userViewer.getChecklistItemsDataModelMap();
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> group2 = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModels2.get(ChecklistItemGroup.NON_HARVARD_SCREENERS).getWrappedData();
    assertEquals("ID assigned", group2.get(1).getKey().getItemName());
    assertEquals(today, group2.get(1).getValue().getDatePerformed());
    assertEquals("Admin User", group2.get(1).getValue().getEntryActivity().getPerformedBy().getFullNameFirstLast());
    assertTrue(group2.get(1).getValue().isExpiration());
  }

  // private methods

}
