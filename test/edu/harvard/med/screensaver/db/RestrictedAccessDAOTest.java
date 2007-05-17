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
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

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
  protected CurrentScreensaverUser currentScreensaverUser;
  protected ScreenResultParser screenResultParser;
  
  // public constructors and methods
  
  @Override
  protected String[] getConfigLocations()
  {
    return new String[] { "spring-context-test-security.xml" };
  }
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    schemaUtil.truncateTablesOrCreateSchema();
  }
  
  @Override
  protected void onTearDown() throws Exception
  {
    // must clear the "current" user, so that subsequent tests are not affected
    // by data access permissions
    currentScreensaverUser.setScreensaverUser(null);
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
    
    currentScreensaverUser.setScreensaverUser(users[1]);
    List<ScreeningRoomUser> filteredUsers = dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    for (Iterator iter = filteredUsers.iterator(); iter.hasNext();) {
      ScreeningRoomUser user = (ScreeningRoomUser) iter.next();
      if (user.isRestricted()) {
        iter.remove();
      }
    }
    assertTrue("user can view own account ", filteredUsers.contains(users[1]));
    assertTrue("user can view account of user in same lab", filteredUsers.contains(users[2]));
    assertTrue("user can view account of lab head", filteredUsers.contains(users[0]));
    assertFalse("user cannot view account of user not in same lab", filteredUsers.contains(users[3]));
    
    currentScreensaverUser.setScreensaverUser(users[0]);
    filteredUsers = dao.findAllEntitiesWithType(ScreeningRoomUser.class);
    for (Iterator iter = filteredUsers.iterator(); iter.hasNext();) {
      ScreeningRoomUser user = (ScreeningRoomUser) iter.next();
      if (user.isRestricted()) {
        iter.remove();
      }
    }
    assertTrue("lab head can view own account", filteredUsers.contains(users[0]));
    assertTrue("lab head can view account of user in same lab", filteredUsers.contains(users[1]));
    assertTrue("lab head can view account of user in same lab", filteredUsers.contains(users[2]));
    assertFalse("lab head cannot view account of user not in same lab", filteredUsers.contains(users[3]));
  }

  public void testScreenResultUserPermissionsByUserType()
  {
    ScreeningRoomUser rnaiUser = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
    ScreeningRoomUser compoundUser = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);

    Screen rnaiScreen = MockDaoForScreenResultImporter.makeDummyScreen(1);
    rnaiScreen.setScreenType(ScreenType.RNAI);
    ScreenResult screenResult1 = new ScreenResult(rnaiScreen, new Date());
    screenResult1.setShareable(true);
    Screen compoundScreen = MockDaoForScreenResultImporter.makeDummyScreen(2);
    compoundScreen.setScreenType(ScreenType.RNAI);
    ScreenResult screenResult2 = new ScreenResult(compoundScreen, new Date());
    screenResult2.setShareable(true);
    
    rnaiScreen.setLeadScreener(rnaiUser);
    compoundScreen.setLeadScreener(compoundUser);
    
    dao.persistEntity(rnaiScreen);
    dao.persistEntity(compoundScreen);

    List<Screen> screens = dao.findAllEntitiesWithType(Screen.class);
    assertEquals("screens count", 2, screens.size());
    for (Screen screen : screens) {
      if (screen.getScreenType().equals(ScreenType.RNAI)) {
        currentScreensaverUser.setScreensaverUser(rnaiUser);
        assertTrue("rnai user is not restricted from rnai screen results", !screen.isRestricted());
        currentScreensaverUser.setScreensaverUser(compoundUser);
        assertTrue("compound user is restricted from rnai screen results", screen.isRestricted());
      } 
      else if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        currentScreensaverUser.setScreensaverUser(rnaiUser);
        assertTrue("rnai user is restricted from compound screen results", screen.isRestricted());
        currentScreensaverUser.setScreensaverUser(compoundUser);
        assertTrue("compound user is not restricted from compound screen results", !screen.isRestricted());
      }
      else {
        fail("unknown screen type" + screen.getScreenType());
      }
    }
  }
  
  public void testRestrictedEntityTest()
  {
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

        // define the library and wells needed to import ScreenResultTest115.xls
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.SMALL_MOLECULE,
          LibraryType.COMMERCIAL,
          1,
          3);
        dao.loadOrCreateWellsForLibrary(library);
        dao.persistEntity(library);
        dao.flush();

        Screen screen115 = MockDaoForScreenResultImporter.makeDummyScreen(115);
        screenResultParser.parse(screen115, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                     ScreenResultParserTest.SCREEN_RESULT_115_TEST_WORKBOOK_FILE));

        if (screenResultParser.getHasErrors()) {
          log.error(screenResultParser.getErrors());
        }
        assertFalse("screenresult import successful", screenResultParser.getHasErrors());
       
        screen115.setLeadScreener(users[0]);
        screen115.addCollaborator(users[1]);
        
        Screen screen116 = MockDaoForScreenResultImporter.makeDummyScreen(116);
        screenResultParser.parse(screen116, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                     ScreenResultParserTest.SCREEN_RESULT_116_TEST_WORKBOOK_FILE));
        screen116.getScreenResult().setShareable(true);
        assertEquals("screenresult import successful", 0, screenResultParser.getErrors().size());
        screen116.setLeadScreener(users[4]);
        
        dao.persistEntity(screen115);
        dao.persistEntity(screen116);
      }
    } );
    
    Screen screen = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);

    currentScreensaverUser.setScreensaverUser(users[0]);
    ScreenResult screenResult1 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertFalse("lead screener can view private screen result", screenResult1.isRestricted());

    currentScreensaverUser.setScreensaverUser(users[1]);
    ScreenResult screenResult2 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertFalse("screen collobarator can view private screen result", screenResult2.isRestricted());
    
    currentScreensaverUser.setScreensaverUser(users[2]);
    ScreenResult screenResult3 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("lab member cannot view private screen result, if not also lead screener or collaborator", screenResult3.isRestricted());

    currentScreensaverUser.setScreensaverUser(users[3]);
    ScreenResult screenResult4 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("lab head cannot view private screen result, if not also lead screener or collaborator", screenResult4.isRestricted());

    Screen screen116 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 116);

    currentScreensaverUser.setScreensaverUser(users[0]);
    ScreenResult screenResult5 = dao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertFalse("compound screener with deposited data can view shareable screen result", screenResult5.isRestricted());
    
    currentScreensaverUser.setScreensaverUser(users[2]);
    ScreenResult screenResult6 = dao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertTrue("compound screener without deposited data cannot view shareable screen result", screenResult6.isRestricted());
    
    currentScreensaverUser.setScreensaverUser(users[4]);
    ScreenResult screenResult7 = dao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("compound screener without deposited data cannot view private screen result", screenResult7.isRestricted());
  }
  

  // private methods
  
 
  private ScreeningRoomUser makeUserWithRoles(ScreensaverUserRole... roles)
  {
    Date created = new Date();
    Object object = new Object();
    ScreeningRoomUser user = new ScreeningRoomUser(created,
                                                   "first",
                                                   "last" + object.hashCode(),
                                                   "email@hms.harvard.edu",
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

