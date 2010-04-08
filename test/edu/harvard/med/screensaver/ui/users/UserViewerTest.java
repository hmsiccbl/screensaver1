// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.searchresults.UserSearchResults;
import edu.harvard.med.screensaver.ui.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.util.ChecklistItems;
import edu.harvard.med.screensaver.ui.util.Messages;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.record.BeginRecord;
import org.joda.time.LocalDate;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class UserViewerTest extends AbstractTransactionalSpringContextTests
{
  private static Logger log = Logger.getLogger(UserViewerTest.class);
  

  private AdministratorUser _admin;
  private CurrentScreensaverUser _currentScreensaverUser;
  
  protected UsersDAO usersDao;
  protected GenericEntityDAO genericEntityDao;
  protected SchemaUtil schemaUtil;
  protected Messages messages;


  private UserViewer _userViewer;


  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test.xml" };
  }

  public UserViewerTest() 
  {
    setPopulateProtectedVariables(true);
  }

  @Override
  protected void onSetUpBeforeTransaction() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  protected void onSetUpInTransaction() throws Exception 
  {
    _admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
    genericEntityDao.persistEntity(_admin);
    _currentScreensaverUser = new CurrentScreensaverUser();
    _currentScreensaverUser.setScreensaverUser(_admin);

    _userViewer = new UserViewer(null, null, genericEntityDao, usersDao, null, null, null, new AttachedFiles(genericEntityDao), new ChecklistItems(null, null, genericEntityDao));
    _userViewer.setCurrentScreensaverUser(_currentScreensaverUser);
    _userViewer.getChecklistItems().setCurrentScreensaverUser(_currentScreensaverUser);
  }
  
  public void testVirginChecklistItemsDataModel()
  {
    genericEntityDao.persistEntity(new ChecklistItem("lab access", true, ChecklistItemGroup.FORMS, 1));
    genericEntityDao.persistEntity(new ChecklistItem("trained", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 1));
    genericEntityDao.persistEntity(new ChecklistItem("ID assigned", true, ChecklistItemGroup.NON_HARVARD_SCREENERS, 2));
    genericEntityDao.persistEntity(new ChecklistItem("legacy", true, ChecklistItemGroup.LEGACY, 1));
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
    genericEntityDao.persistEntity(user);

    setComplete();
    endTransaction();

    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    _userViewer.setEntity(user);
    Map<ChecklistItemGroup,DataModel> checklistItemDataModels = _userViewer.getChecklistItems().getChecklistItemsDataModelMap();
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

    ScreeningRoomUser user = new ScreeningRoomUser("Test", "User");
    genericEntityDao.persistEntity(user);
    ChecklistItem checklistItemType = new ChecklistItem("ID assigned", true, ChecklistItemGroup.NON_HARVARD_SCREENERS, 2);
    genericEntityDao.persistEntity(checklistItemType);
    genericEntityDao.persistEntity(new ChecklistItem("trained", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 1));
    ChecklistItemEvent item = 
      user.createChecklistItemActivationEvent(checklistItemType,
                                              today,
                                              _admin);
    genericEntityDao.saveOrUpdateEntity(item);
    setComplete();
    endTransaction();

    startNewTransaction();
    user = genericEntityDao.reloadEntity(user);
    _userViewer.setEntity(user);

    Map<ChecklistItemGroup,DataModel> checklistItemDataModels = _userViewer.getChecklistItems().getChecklistItemsDataModelMap();
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> group = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModels.get(ChecklistItemGroup.NON_HARVARD_SCREENERS).getWrappedData();
    assertEquals("trained", group.get(0).getKey().getItemName());
    assertNull("trained", group.get(0).getValue());
    assertEquals("ID assigned", group.get(1).getKey().getItemName());
    assertEquals(today, group.get(1).getValue().getDatePerformed());
    assertEquals("Admin User", group.get(1).getValue().getCreatedBy().getFullNameFirstLast());
    assertFalse(group.get(1).getValue().isExpiration());
    
    endTransaction();

    startNewTransaction();
    
    ChecklistItemEvent activation = genericEntityDao.findAllEntitiesOfType(ChecklistItemEvent.class).get(0);
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
    ChecklistItemEvent expirationEvent = activation.createChecklistItemExpirationEvent(today, _admin); 
    genericEntityDao.saveOrUpdateEntity(expirationEvent);
    genericEntityDao.flush();
    _userViewer.setEntity(activation.getScreeningRoomUser());

    Map<ChecklistItemGroup,DataModel> checklistItemDataModels2 = _userViewer.getChecklistItems().getChecklistItemsDataModelMap();
    final List<Map.Entry<ChecklistItem,ChecklistItemEvent>> group2 = (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) checklistItemDataModels2.get(ChecklistItemGroup.NON_HARVARD_SCREENERS).getWrappedData();
    assertEquals("ID assigned", group2.get(1).getKey().getItemName());
    assertEquals(today, group2.get(1).getValue().getDatePerformed());
    assertEquals("Admin User", group2.get(1).getValue().getCreatedBy().getFullNameFirstLast());
    assertTrue(group2.get(1).getValue().isExpiration());
  }
}
