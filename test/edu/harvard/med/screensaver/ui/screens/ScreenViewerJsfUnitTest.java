// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;




import java.util.Set;
import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.ui.AbstractJsfUnitTest;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreenViewerJsfUnitTest extends AbstractJsfUnitTest
{
  private static final Logger log = Logger.getLogger(ScreenViewerJsfUnitTest.class);
  private Screen _screen;

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ScreenViewerJsfUnitTest.class);
    return suite;
  }

  public void setUp() throws Exception
  {
    super.setUp();
    _screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
    _dao.persistEntity(_screen);
    // ensure _screen entity has an ID-based hashCode
    _screen = _dao.reloadEntity(_screen, true, "labHead", "leadScreener", "collaborators");
  }

  public void testOpenScreenViewer() throws Exception
  {
    visitScreenViewer(_screen);
    assertTrue(_client.getWebResponse().getText().contains(_screen.getTitle()));
  }

  public void testAddAndSaveScreen() throws Exception
  {
    submit("addScreenCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    assertTrue("edit mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    assertEquals("screen number", new Integer(2), (Integer) getBeanValue("screenDetailViewer.screen.screenNumber"));
//    submit("saveCommand");
//    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
//    assertMessage("required.*titleTextField");
    _client.setParameter("screenDetailViewerForm:labNameMenu", Integer.toString(_screen.getLabHead().hashCode()));
    _client.submitNoButton("screenDetailViewerForm");
    assertEquals(2, getBeanValue("screenDetailViewer.leadScreener.size"));
    submit("saveCommand", 
           new Pair<String,String>("screenDetailViewerForm:titleTextField", "Test screen title"),
           new Pair<String,String>("screenDetailViewerForm:leadScreenerMenu", Integer.toString(_screen.getLabHead().hashCode())),
           new Pair<String,String>("screenDetailViewerForm:screenTypeMenu", ScreenType.SMALL_MOLECULE.toString()));
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals("screen number", new Integer(2), (Integer) getBeanValue("screenViewer.screen.screenNumber"));
    assertNotNull("screen created", _dao.findEntityByProperty(Screen.class, "screenNumber", 2));
  }

  public void testAddAndCancelScreen() throws Exception
  {
    submit("addScreenCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    assertTrue("edit mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    submit("cancelEditCommand");
    assertAtView("/screensaver/main/main.jsf");//screens/screensBrowser.jsf"
    assertEquals("screen not created", 1, _dao.findAllEntitiesOfType(Screen.class).size());
  }

  public void testEditAndSaveScreen() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("saveCommand",
           new Pair<String,String>("screenDetailViewerForm:titleTextField", "new title"));
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertFalse("read-only mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    assertEquals("title changed", "new title", getBeanValue("screenViewer.screen.title"));
  }

  public void testEditAndCancelScreen() throws Exception
  {
    String oldTitle = _screen.getTitle();
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("cancelEditCommand",
           new Pair<String,String>("screenDetailViewerForm:titleTextField", "new title"));
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertFalse("read-only mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    assertEquals("title not changed", oldTitle, getBeanValue("screenViewer.screen.title"));
  }

  public void testAddStatusItem() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("addStatusItemCommand",
           new Pair<String,String>("newStatusItemDate", "1/1/2008"),
           new Pair<String,String>("newStatusItemValue", "" + StatusValue.ACCEPTED.hashCode()));
    assertEquals(1, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
    assertEquals(StatusValue.ACCEPTED, ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).first().getStatusValue());
    assertEquals(new LocalDate(2008, 1, 1), ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).first().getStatusDate());
    submit("addStatusItemCommand",
           new Pair<String,String>("newStatusItemDate", "2/2/2008"),
           new Pair<String,String>("newStatusItemValue", "" + StatusValue.COMPLETED.hashCode()));
    assertEquals(2, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
    assertEquals(StatusValue.COMPLETED, ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).last().getStatusValue());
    assertEquals(new LocalDate(2008, 2, 2), ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).last().getStatusDate());
    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
  }
  
  public void testFindScreenNumber() throws Exception
  {
    Screen screen2 = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    _dao.persistEntity(screen2);

    // test from virgin session, screens browser not yet opened/initalized
    visitMainPage();
    submit("findScreenCommand", new Pair<String,String>("screenNumber", "2"));
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals(new Integer(2), getBeanValue("screenViewer.screen.screenNumber"));
    assertEquals(1, getBeanValue("screensBrowser.rowCount"));
    assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
    
    // test after screens browser has been opened/initialized; screen should be found in existing screen search results context
    _client.clickCommandLink("browseScreensCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
    submit("findScreenCommand", new Pair<String,String>("screenNumber", "1"));
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals(new Integer(1), getBeanValue("screenViewer.screen.screenNumber"));
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
    assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
  }
  
  private void visitScreenViewer(Screen screen)
  {
    ScreenViewer viewer = getBeanValue("screenViewer");
    viewer.viewScreen(screen);
    visitPage("/screens/screensBrowser.jsf");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
  }

}