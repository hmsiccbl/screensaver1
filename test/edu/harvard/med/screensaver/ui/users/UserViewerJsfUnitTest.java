// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.users;




import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.ui.AbstractJsfUnitTest;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@SuppressWarnings("unchecked")
public class UserViewerJsfUnitTest extends AbstractJsfUnitTest
{
  private static final Logger log = Logger.getLogger(UserViewerJsfUnitTest.class);
  private Screen _screen;

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(UserViewerJsfUnitTest.class);
    return suite;
  }

  public void setUp() throws Exception
  {
    super.setUp();
    _screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
    _dao.persistEntity(_screen);
    // ensure _screen entity has an ID-based hashCode
    _screen = _dao.reloadEntity(_screen, true, "leadScreener");
  }

//  public void testOpenUserViewer() throws Exception
//  {
//    visitUserViewer(_screen.getLeadScreener());
//    assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
//    assertPageContainsText(_screen.getLeadScreener().getLastName());
//  }

//  public void testLowerCaseEcommonsId() throws Exception
//  {
//    visitUserViewer(_screen.getLeadScreener());
//    submit("userViewerBodyForm:commandsTop:editCommand");
//    //assertShowingScreen(_screen.getScreenNumber(), true);
//    submit("userViewerBodyForm:commandsTop:saveCommand",
//           new Pair<String,String>("ecommonsIdTextField", "XYZ1"));
//    //assertShowingScreen(_screen.getScreenNumber(), false);
//    assertEquals("ecommons id lowercased", "xyz1", getBeanValue("userViewer.user.ECommonsId"));
//    assertPageContainsText("xyz1");
//  }

//  public void testFindScreener() throws Exception
//  {
//
//
//    // test from virgin session, screens browser not yet opened/initialized
//    visitMainPage();
//    submit("findScreenCommand", new Pair<String,String>("screenNumber", "2"));
//    assertShowingScreen(2, false);
//    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
//
//    // test after screens browser has been opened/initialized; screen should be found in existing screen search results context
//    _client.clickCommandLink("browseScreensCommand");
//    assertAtView("/screensaver/screens/screensBrowser.jsf");
//    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
//    submit("findScreenCommand", new Pair<String,String>("screenNumber", "1"));
//    assertShowingScreen(1, false);
//    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
//  }

//  private void visitUserViewer(ScreensaverUser user)
//  {
//    UserViewer viewer = getBeanValue("userViewer");
//    viewer.viewUser(_screen.getLeadScreener());
//    visitPage("/users/screenersBrowser.jsf");
//    assertAtView("/screensaver/users/screenersBrowser.jsf");
//  }

//  private void assertShowingUser(ScreensaverUser user, boolean isEditModeExpected)
//  {
//    if (isEditModeExpected) {
//      assertEquals("screenDetailViewer screen number",
//                   new Integer(screenNumber),
//                   (Integer) getBeanValue("screenDetailViewer.screen.screenNumber"));
//      // when in edit mode, screen is shown within a screenDetailViewer, outside of a search results context
//      assertAtView("/screensaver/screens/screenDetailViewer.jsf");
//      assertElementTextEqualsRegex("screenDetailViewerForm:screenNumberLinkValue", Integer.toString(screenNumber));
//      assertTrue("edit mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
//    }
//    else {
//      assertEquals("screenViewer screen number",
//                   new Integer(screenNumber),
//                   (Integer) getBeanValue("screenViewer.screen.screenNumber"));
//      // when not in edit mode, screen is always to be shown within a search results context
//      assertAtView("/screensaver/screens/screensBrowser.jsf");
//      assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
//      assertElementTextEqualsRegex("screenDetailPanelForm:screenNumberLinkValue", Integer.toString(screenNumber));
//      assertFalse("read-only mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
//    }
//  }
}