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
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
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
    final UserViewer userViewer = new UserViewer(null, null, genericEntityDao, usersDao, null, null, null);
    userViewer.setCurrentScreensaverUser(_currentScreensaverUser);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        genericEntityDao.persistEntity(new ChecklistItem(1, "trained", false));
        genericEntityDao.persistEntity(new ChecklistItem(2, "ID assigned", true));
        genericEntityDao.persistEntity(new ChecklistItem(3, "lab access", true));
        ScreeningRoomUser user = new ScreeningRoomUser("Test", "User", "test_user@hms.harvard.edu");
        genericEntityDao.persistEntity(user);
        genericEntityDao.flush();
        userViewer.setUser(user);
      }
    });
    DataModel checklistItemDataModel = userViewer.getChecklistItemsDataModel();
    assertEquals(3, checklistItemDataModel.getRowCount());
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> data = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModel.getWrappedData();
    assertEquals("trained", data.get(0).getKey().getItemName());
    assertNull("trained", data.get(0).getValue());
    assertEquals("ID assigned", data.get(1).getKey().getItemName());
    assertNull("ID assigned", data.get(1).getValue());
    assertEquals("trained", data.get(0).getKey().getItemName());
    assertNull("trained", data.get(0).getValue());
    assertEquals("lab access", data.get(2).getKey().getItemName());
    assertNull("lab access", data.get(2).getValue());
  }
  
  public void testPopulatedChecklistItemsDataModel()
  {    
    final LocalDate today = new LocalDate();
    final UserViewer userViewer = new UserViewer(null, null, genericEntityDao, usersDao, null, null, null);
    userViewer.setCurrentScreensaverUser(_currentScreensaverUser);
    genericEntityDao.doInTransaction(new DAOTransaction() {

      public void runTransaction()
      {
        ScreeningRoomUser user = new ScreeningRoomUser("Test", "User", "test_user@hms.harvard.edu");
        genericEntityDao.persistEntity(user);
        ChecklistItem checklistItemType = new ChecklistItem(2, "ID assigned", true);
        genericEntityDao.persistEntity(checklistItemType);
        genericEntityDao.persistEntity(new ChecklistItem(1, "trained", false));
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
    DataModel checklistItemDataModel = userViewer.getChecklistItemsDataModel();
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> data = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModel.getWrappedData();
    assertEquals("trained", data.get(0).getKey().getItemName());
    assertNull("trained", data.get(0).getValue());
    assertEquals("ID assigned", data.get(1).getKey().getItemName());
    assertEquals(today, data.get(1).getValue().getDatePerformed());
    assertEquals("Admin User", data.get(1).getValue().getEntryActivity().getPerformedBy().getFullNameFirstLast());
  }

  // private methods

}
