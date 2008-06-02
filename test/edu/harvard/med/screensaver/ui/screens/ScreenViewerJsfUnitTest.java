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
import org.apache.myfaces.custom.schedule.renderer.ScheduleCompactMonthRenderer;
import org.hibernate.type.TrueFalseType;
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
//    assertMessage("required.*titleTextareaField1");
    submit("saveCommand", new Pair<String,String>("screenDetailViewerForm:titleTextareaField1", "Test screen title"));
    assertAtView("/screensaver/screens/screenViewer.jsf");
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
           new Pair<String,String>("screenDetailViewerForm:titleTextareaField1", "new title"));
    assertAtView("/screensaver/screens/screenViewer.jsf");
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
           new Pair<String,String>("screenDetailViewerForm:titleTextareaField1", "new title"));
    assertAtView("/screensaver/screens/screenViewer.jsf");
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
    assertAtView("/screensaver/screens/screenViewer.jsf");
  }

  private void visitScreenViewer(Screen screen)
  {
    ScreenViewer viewer = getBeanValue("screenViewer");
    viewer.setScreen(screen);
    visitPage("/screens/screenViewer.jsf");
  }

}