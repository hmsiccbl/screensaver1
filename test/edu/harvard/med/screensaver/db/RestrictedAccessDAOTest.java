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

/**
 * Tests DataAccessPolicy implementation, as well as Spring AOP configuration
 * for wrapping our DAO methods with "restricted access" interceptors. Does not
 * comprehenisvely test all DAO methods, but what we test here is reasonable.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
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
  
  public void testScreenResultPermissions()
  {
    schemaUtil.initializeDatabase();

    final ScreeningRoomUser[] users = new ScreeningRoomUser[5];
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        users[0] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER); // lead screener for screen 115
        users[1] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER); // collaborator for screen 115
        users[2] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER); // lab member with previous users, but not associated with screen 115
        users[3] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER); // lab head of previous users, not otherwise associated with screen 115
        users[4] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER); // unaffiliated with previous users
        
        users[3].addLabMember(users[0]);
        users[3].addLabMember(users[1]);
        users[3].addLabMember(users[2]);

        Screen screen115 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);
        screenResultParser.parse(screen115, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "NewFormatTest.xls"));
        assertEquals("screenresult import successful", 0, screenResultParser.getErrors().size());
        screen115.setLeadScreener(users[0]);
        screen115.addCollaborator(users[1]);
        
        Screen screen116 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 116);
        screenResultParser.parse(screen116, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, "NewFormatTest2.xls"));
        screen116.getScreenResult().setShareable(true);
        assertEquals("screenresult import successful", 0, screenResultParser.getErrors().size());
        screen116.setLeadScreener(users[4]);
      }
    } );
    

    Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);

    dataAccessPolicy.setScreensaverUser(users[0]);
    ScreenResult screenResult1 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNotNull("lead screener can view private screen result", screenResult1);

    dataAccessPolicy.setScreensaverUser(users[1]);
    ScreenResult screenResult2 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNotNull("screen collobarator can view private screen result", screenResult2);
    
    dataAccessPolicy.setScreensaverUser(users[2]);
    ScreenResult screenResult3 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNull("lab member cannot view private screen result, if not also lead screener or collaborator", screenResult3);

    dataAccessPolicy.setScreensaverUser(users[3]);
    ScreenResult screenResult4 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNull("lab head cannot view private screen result, if not also lead screener or collaborator", screenResult4);

    Screen screen116 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 116);

    dataAccessPolicy.setScreensaverUser(users[0]);
    ScreenResult screenResult5 = dao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertNotNull("compound screener with deposited data can view shareable screen result", screenResult5);
    
    dataAccessPolicy.setScreensaverUser(users[2]);
    ScreenResult screenResult6 = dao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertNull("compound screener without deposited data cannot view shareable screen result", screenResult6);
    
    dataAccessPolicy.setScreensaverUser(users[4]);
    ScreenResult screenResult7 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertNull("compound screener without deposited data cannot view private screen result", screenResult7);
  }
  
  public void testVisitsPermissions() 
  {
    // TODO
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

