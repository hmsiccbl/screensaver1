// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.File;
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.log4j.Logger;

public class RestrictedAccessDAOTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(RestrictedAccessDAOTest.class);


  // instance data members
  
  protected DAO dao;
  protected SchemaUtil schemaUtil;
  protected DataAccessPolicy dataAccessPolicy;
  protected ScreenResultParser screenResultParser;

  // public constructors and methods
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  public void testScreensaverUserAccountPermissions()
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[4];
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        users[0] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        users[1] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        users[2] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        users[3] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        users[1].setLabHead(users[0]);
        users[2].setLabHead(users[0]);
      }
    } );
    
    dataAccessPolicy.setScreensaverUser(users[1]);
    List<ScreeningRoomUser> filterUsers = dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    assertTrue("user can view own account ", filterUsers.contains(users[1]));
    assertTrue("user can view account of user in same lab", filterUsers.contains(users[2]));
    assertTrue("user can view account of lab head", filterUsers.contains(users[0]));
    assertFalse("user cannot view account of user not in same lab", filterUsers.contains(users[3]));
    
    dataAccessPolicy.setScreensaverUser(users[0]);
    filterUsers = dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    assertTrue("lab head can view own account", filterUsers.contains(users[0]));
    assertTrue("lab head can view account of user in same lab", filterUsers.contains(users[1]));
    assertTrue("lab head can view account of user in same lab", filterUsers.contains(users[2]));
    assertFalse("lab head cannot view account of user not in same lab", filterUsers.contains(users[3]));
  }

  public void testScreenResultUserPermissionsByUserType()
  {
    schemaUtil.initializeDatabase();

    ScreensaverUser rnaiUser = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
    doTestScreenResultUserPermissions(ScreenType.RNAI, rnaiUser);
    
    ScreensaverUser compoundUser = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
    doTestScreenResultUserPermissions(ScreenType.SMALL_MOLECULE, compoundUser);
  }
  
  public void testSharedScreenResultPermissions()
  {
    schemaUtil.initializeDatabase();

    final ScreeningRoomUser[] users = new ScreeningRoomUser[3];
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        users[0] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        users[1] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        users[2] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);
        screen.setLeadScreener(users[0]);
        screen.addCollaborator(users[1]);
        
        screenResultParser.parse(screen, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "NewFormatTest.xls"));
        assertEquals("screenresult import successful", 0, screenResultParser.getErrors().size());
      }
    } );
    

    Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);

    dataAccessPolicy.setScreensaverUser(users[0]);
    ScreenResult screenResult1 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNotNull("lead screener can view screen result", screenResult1);

    dataAccessPolicy.setScreensaverUser(users[1]);
    ScreenResult screenResult2 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNotNull("screen collobarator can view screen result", screenResult2);
    
    dataAccessPolicy.setScreensaverUser(users[2]);
    ScreenResult screenResult3 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNull("non-lead screener and non-collaborator cannot view screen result", screenResult3);
  }


  // private methods
  
  private void doTestScreenResultUserPermissions(ScreenType screenType, ScreensaverUser user)
  {
    dataAccessPolicy.setScreensaverUser(user);
    List<Screen> screens = dao.findAllEntitiesWithType(Screen.class);
    assertTrue("non-empty screens list", screens.size() > 0);
    for (Screen screen : screens) {
      assertEquals("user access to only " + screenType + " screens", screenType, screen.getScreenType());
    }
  }
  
  private ScreeningRoomUser makeUserWithRoles(ScreensaverUserRole... roles)
  {
    Date created = new Date();
    Object object = new Object();
    ScreeningRoomUser user = new ScreeningRoomUser(created,
                                                   "first",
                                                   "last",
                                                   "email" + object.hashCode() 
                                                     + "@hms.harvard.edu",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   "",
                                                   ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                                   true);
    for (ScreensaverUserRole role : roles) {
      user.addScreensaverUserRole(role);
    }
    dao.persistEntity(user);
    return user;
  }
  


}

