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
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParserTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

import org.apache.log4j.Logger;

/**
 * Tests WedDataAccessPolicy implementation, as well as Hibernate interceptor-based
 * mechanism for setting "restricted" flag on entities.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class WebDataAccessPolicyTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(WebDataAccessPolicyTest.class);


  // instance data members
  
  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
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
  
  public void testScreensaverUserPermissions()
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[4];
    genericEntityDao.doInTransaction(new DAOTransaction() {
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
    List<ScreeningRoomUser> filteredUsers = genericEntityDao.findAllEntitiesOfType(ScreeningRoomUser.class);
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
    filteredUsers = genericEntityDao.findAllEntitiesOfType(ScreeningRoomUser.class);
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

  public void testScreenPermissions()
  {
    ScreeningRoomUser rnaiUser = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
    ScreeningRoomUser compoundUser = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
    ScreeningRoomUser compoundRnaiUser = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER, 
                                                           ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);

    Screen rnaiScreen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
    ScreenResult screenResult1 = new ScreenResult(rnaiScreen, new Date());
    screenResult1.setShareable(true);
    Screen compoundScreen = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    ScreenResult screenResult2 = new ScreenResult(compoundScreen, new Date());
    screenResult2.setShareable(true);

    rnaiScreen.setLeadScreener(rnaiUser);
    compoundScreen.setLeadScreener(compoundUser);
    rnaiScreen.addCollaborator(compoundRnaiUser);
    compoundScreen.addCollaborator(compoundRnaiUser);
    
    genericEntityDao.persistEntity(rnaiScreen);
    genericEntityDao.persistEntity(compoundScreen);

    List<Screen> screens = genericEntityDao.findAllEntitiesOfType(Screen.class);
    assertEquals("screens count", 2, screens.size());
    for (Screen screen : screens) {
      if (screen.getScreenType().equals(ScreenType.RNAI)) {
        currentScreensaverUser.setScreensaverUser(rnaiUser);
        assertTrue("rnai user is not restricted from rnai screens", !screen.isRestricted());
        assertTrue("rnai user is not restricted from shared rnai screen result", !screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(compoundUser);
        assertTrue("compound user is restricted from rnai screens", screen.isRestricted());
        assertTrue("compound user is restricted from shared rnai screen result", screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(compoundRnaiUser);
        assertTrue("compound+rnai user is not restricted from rnai screens", !screen.isRestricted());
        assertTrue("compound+rnai user is not restricted from shared rnai screen result", !screen.getScreenResult().isRestricted());
      } 
      else if (screen.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        currentScreensaverUser.setScreensaverUser(rnaiUser);
        assertTrue("rnai user is restricted from compound screens", screen.isRestricted());
        assertTrue("rnai user is restricted from shared compound screen result", screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(compoundUser);
        assertTrue("compound user is not restricted from compound screens", !screen.isRestricted());
        assertTrue("compound user is not restricted from shared compound screen result", !screen.getScreenResult().isRestricted());
        currentScreensaverUser.setScreensaverUser(compoundRnaiUser);
        assertTrue("compound+rnai user is not restricted from compound screens", !screen.isRestricted());
        assertTrue("compound+rnai user is not restricted from shared compound screen result", !screen.getScreenResult().isRestricted());
      }
      else {
        fail("unknown screen type" + screen.getScreenType());
      }
    }
  }
  
  public void testRNAiScreenResultPermissions()
  {
    doTestScreenResultPermissionsForScreenType(ScreenType.RNAI);
  }

  public void testCompoundScreenResultPermissions()
  {
    doTestScreenResultPermissionsForScreenType(ScreenType.SMALL_MOLECULE);
  }
  
  public void doTestScreenResultPermissionsForScreenType(final ScreenType screenType) 
  {
    final ScreeningRoomUser[] users = new ScreeningRoomUser[7];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        // define the library and wells needed to import ScreenResultTest115.xls
        Library library = new Library(
          "library 1",
          "lib1",
          screenType,
          LibraryType.COMMERCIAL,
          1,
          3);
        librariesDao.loadOrCreateWellsForLibrary(library);
        genericEntityDao.persistEntity(library);
        genericEntityDao.flush();

        ScreensaverUserRole role = screenType.equals(ScreenType.SMALL_MOLECULE) ? ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER : ScreensaverUserRole.RNAI_SCREENING_ROOM_USER;
        ScreensaverUserRole otherRole = screenType.equals(ScreenType.SMALL_MOLECULE) ? ScreensaverUserRole.RNAI_SCREENING_ROOM_USER : ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER;
        users[0] = makeUserWithRoles(role); // lead screener for screen 115
        users[1] = makeUserWithRoles(role); // collaborator for screen 115
        users[2] = makeUserWithRoles(role); // lab member with above users, but not associated with screen 115, and no deposited screen result data
        users[3] = makeUserWithRoles(role); // lab head of above users, not otherwise associated with screen 115
        users[4] = makeUserWithRoles(role); // unaffiliated with previous users, and has deposited screen result data
        users[5] = makeUserWithRoles(otherRole); // unaffiliated with previous users, missing appropriate screening type role
        users[6] = makeUserWithRoles(role, otherRole); // unaffiliated with previous users, dual screening type roles, has deposited screen result data
        
        users[3].addLabMember(users[0]);
        users[3].addLabMember(users[1]);
        users[3].addLabMember(users[2]);

        Screen screen115 = MakeDummyEntities.makeDummyScreen(115, screenType);
        screenResultParser.parse(screen115, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                     ScreenResultParserTest.SCREEN_RESULT_115_TEST_WORKBOOK_FILE));

        if (screenResultParser.getHasErrors()) {
          log.error(screenResultParser.getErrors());
        }
        assertFalse("screenresult import successful", screenResultParser.getHasErrors());
       
        screen115.setLabHead(users[3]);
        screen115.setLeadScreener(users[0]);
        screen115.addCollaborator(users[1]);
        
        Screen screen116 = MakeDummyEntities.makeDummyScreen(116, screenType);
        screenResultParser.parse(screen116, new File(ScreenResultParserTest.TEST_INPUT_FILE_DIR, 
                                                     ScreenResultParserTest.SCREEN_RESULT_116_TEST_WORKBOOK_FILE));
        screen116.getScreenResult().setShareable(true);
        assertEquals("screenresult import successful", 0, screenResultParser.getErrors().size());
        screen116.setLeadScreener(users[4]);
        screen116.addCollaborator(users[6]);
        
        Screen screen117 = MakeDummyEntities.makeDummyScreen(117, screenType);
        new ScreenResult(screen117, new Date());
        screen117.setLeadScreener(users[6]);
        
        genericEntityDao.persistEntity(screen115);
        genericEntityDao.persistEntity(screen116);
        genericEntityDao.persistEntity(screen117);
        
      }
    } );
    
    Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);

    currentScreensaverUser.setScreensaverUser(users[0]);
    ScreenResult screenResult1 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("lead screener can view own, private screen result", !screenResult1.isRestricted());

    currentScreensaverUser.setScreensaverUser(users[1]);
    ScreenResult screenResult2 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("screen collaborator can view own, private screen result", !screenResult2.isRestricted());
    
    currentScreensaverUser.setScreensaverUser(users[3]);
    ScreenResult screenResult3 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("lab head can view own private screen result", !screenResult3.isRestricted());

    currentScreensaverUser.setScreensaverUser(users[2]);
    ScreenResult screenResult4 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("lab member cannot view lab's private screen result, if not also lead screener, lab head, or collaborator", screenResult4.isRestricted());

    Screen screen116 = genericEntityDao.findEntityByProperty(Screen.class, "hbnScreenNumber", 116);

    currentScreensaverUser.setScreensaverUser(users[0]);
    ScreenResult screenResult5 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertTrue("screener with deposited data can view shareable screen result", !screenResult5.isRestricted());
    
    currentScreensaverUser.setScreensaverUser(users[2]);
    ScreenResult screenResult6 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertTrue("screener without deposited data cannot view shareable screen result", screenResult6.isRestricted());
    
    currentScreensaverUser.setScreensaverUser(users[4]);
    ScreenResult screenResult7 = genericEntityDao.findEntityById(ScreenResult.class, screen.getScreenResult().getEntityId());
    assertTrue("screener with deposited data cannot view private screen result", screenResult7.isRestricted());

    currentScreensaverUser.setScreensaverUser(users[5]);
    ScreenResult screenResult8 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertTrue("screener missing appropriate screen type role cannot view shareable screen result", screenResult8.isRestricted());

    currentScreensaverUser.setScreensaverUser(users[6]);
    ScreenResult screenResult9 = genericEntityDao.findEntityById(ScreenResult.class, screen116.getScreenResult().getEntityId());
    assertTrue("screener with dual screen type roles can view shareable screen result", !screenResult9.isRestricted());
  }
  
  public void testRNAiCherryPickRequestPermissions()
  {
    schemaUtil.initializeDatabase(); // required to set the cherryPickRequest sequence start value
    final ScreeningRoomUser[] users = new ScreeningRoomUser[6];
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        // define the library and wells needed to import ScreenResultTest115.xls
        Library library = new Library(
          "library 1",
          "lib1",
          ScreenType.RNAI,
          LibraryType.COMMERCIAL,
          1,
          3);
        librariesDao.loadOrCreateWellsForLibrary(library);
        genericEntityDao.persistEntity(library);
        genericEntityDao.flush();

        users[0] = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER); // lead screener for screen 115
        users[1] = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER); // collaborator for screen 115
        users[2] = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER); // lab member with above users, but not associated with screen 115
        users[3] = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER); // lab head of above users, not otherwise associated with screen 115
        users[4] = makeUserWithRoles(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER); // unaffiliated with previous users
        users[5] = makeUserWithRoles(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER); // unaffiliated with previous users, compound screener role only
        
        users[3].addLabMember(users[0]);
        users[3].addLabMember(users[1]);
        users[3].addLabMember(users[2]);

        Screen screen = MakeDummyEntities.makeDummyScreen(115, ScreenType.RNAI);
        screen.setLabHead(users[3]);
        screen.setLeadScreener(users[0]);
        screen.addCollaborator(users[1]);
        new RNAiCherryPickRequest(screen, users[0], new Date());
        genericEntityDao.persistEntity(screen);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Screen screen = genericEntityDao.findEntityByProperty(Screen.class, "hbnScreenNumber", 115);
        currentScreensaverUser.setScreensaverUser(users[0]);
        RNAiCherryPickRequest rnaiCherryPickRequest = genericEntityDao.findEntityById(RNAiCherryPickRequest.class, screen.getCherryPickRequests().iterator().next().getEntityId());
        assertTrue("lead screener can view RNAi Cherry Pick Request", !rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(users[1]);
        assertTrue("collaborator can view RNAi Cherry Pick Request", !rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(users[3]);
        assertTrue("lab head can view RNAi Cherry Pick Request", !rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(users[2]);
        assertTrue("lab member not associated with screen cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(users[2]);
        assertTrue("lab member not associated with screen cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(users[4]);
        assertTrue("non-lab member cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
        currentScreensaverUser.setScreensaverUser(users[5]);
        assertTrue("non-rnai user cannot view RNAi Cherry Pick Request", rnaiCherryPickRequest.isRestricted());
      }
    });
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
    genericEntityDao.persistEntity(user);
    return user;
  }
  


}

