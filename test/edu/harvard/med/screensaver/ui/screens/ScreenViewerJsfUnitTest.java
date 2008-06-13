// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;




import java.math.BigDecimal;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractJsfUnitTest;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@SuppressWarnings("unchecked")
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
    assertPageContainsText(_screen.getTitle());
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

    //_client.setParameter("screenDetailViewerForm:labNameMenu", Integer.toString(_screen.getLabHead().hashCode()));
    //_client.submitNoButton("screenDetailViewerForm");
    submit("submitLabHead",
           new Pair<String,String>("screenDetailViewerForm:labNameMenu", Integer.toString(_screen.getLabHead().hashCode())));
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

  public void testAddAndDeleteStatusItem() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("statusItemsTableAddCommand",
           new Pair<String,String>("newStatusItemDateDateField", "1/1/2008"),
           new Pair<String,String>("newStatusItemValue", "" + StatusValue.ACCEPTED.hashCode()));
    assertEquals(1, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
    assertEquals(StatusValue.ACCEPTED, ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).first().getStatusValue());
    assertEquals(new LocalDate(2008, 1, 1), ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).first().getStatusDate());
    submit("statusItemsTableAddCommand",
           new Pair<String,String>("newStatusItemDateDateField", "2/2/2008"),
           new Pair<String,String>("newStatusItemValue", "" + StatusValue.COMPLETED.hashCode()));
    assertEquals(2, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
    assertEquals(StatusValue.COMPLETED, ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).last().getStatusValue());
    assertEquals(new LocalDate(2008, 2, 2), ((SortedSet<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).last().getStatusDate());
    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");

    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("0:statusItemsTableDeleteCommand");
    assertEquals(1, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals(1, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
  }

  public void testFindScreenNumber() throws Exception
  {
    Screen screen2 = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    _dao.persistEntity(screen2);

    // test from virgin session, screens browser not yet opened/initialized
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

  public void testAddAndDeleteCollaborators() throws Exception
  {
    ScreeningRoomUser collaborator = new ScreeningRoomUser("Col", "Laborator", "col_laborator@hms.harvard.edu");
    _dao.persistEntity(collaborator);
    collaborator = _dao.reloadEntity(collaborator);

    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("collaboratorsTableAddCommand",
           new Pair<String,String>("collaboratorsEditable", Integer.toString(collaborator.hashCode())));
    assertTrue(((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.collaborators")).contains(collaborator));
    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertTrue(((Set<ScreeningRoomUser>) getBeanValue("screenViewer.screen.collaborators")).contains(collaborator));

    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("0:collaboratorsTableDeleteCommand");
    assertFalse(((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.collaborators")).contains(collaborator));
    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertFalse(((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.collaborators")).contains(collaborator));
  }

  public void testAddAndDeletePublication() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("publicationsTableAddCommand",
           new Pair<String,String>("publicationTitle", "Screensaver LIMS"),
           new Pair<String,String>("publicationAuthors", "Tolopko, Andrew"),
           new Pair<String,String>("publicationJournal", "Modern LIMS Development"),
           new Pair<String,String>("publicationVolume", "101"),
           new Pair<String,String>("publicationPages", "9-11"),
           new Pair<String,String>("publicationYear", "2008"),
           new Pair<String,String>("publicationPubMedId", "10001"));
    assertEquals(1, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertPageContainsText("Screensaver LIMS");
    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals(1, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertPageContainsText("Screensaver LIMS");

    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("0:publicationsTableDeleteCommand");
    assertEquals(0, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertPageContainsText("Screensaver LIMS", false);
    submit("saveCommand");
    assertEquals(0, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertPageContainsText("Screensaver LIMS", false);
  }

  public void testAddBillingInformationAndBillingItems() throws Exception
  {
    visitScreenViewer(_screen);
    assertNull(getBeanValue("screenDetailViewer.screen.billingInformation"));
    assertElementExists("billingInformationToggleText", false);
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    submit("addBillingInformationCommand");
    assertNotNull(getBeanValue("screenDetailViewer.screen.billingInformation"));
    assertElementTextEqualsRegex("billingInformationToggleText", "Hide.*");

    // test billing items
    submit("billingItemsTableAddCommand",
           new Pair<String,String>("newBillingItemItemToBeChargedTextField", "Plates"),
           new Pair<String,String>("newBillingItemAmountCurrencyField", "$19.99"),
           new Pair<String,String>("newBillingItemDateFaxedDateField", "02/18/2008"));
    assertEquals("billing item added (count)",
                 1, getCollectionSize("screenDetailViewer.screen.billingInformation.billingItems"));
    submit("billingItemsTableAddCommand",
           new Pair<String,String>("newBillingItemItemToBeChargedTextField", "DMSO"),
           new Pair<String,String>("newBillingItemAmountCurrencyField", "$14.39"),
           new Pair<String,String>("newBillingItemDateFaxedDateField", "02/19/2008"));
    assertEquals("billing item added (count)",
                 2, getCollectionSize("screenDetailViewer.screen.billingInformation.billingItems"));
    submit("1:billingItemsTableDeleteCommand");
    assertEquals("billing item added (count)",
                 1, getCollectionSize("screenDetailViewer.screen.billingInformation.billingItems"));

    BillingItem expectedBillingItem = new BillingItem();
    expectedBillingItem.setItemToBeCharged("Plates");
    expectedBillingItem.setAmount(new BigDecimal("19.99"));
    expectedBillingItem.setDateFaxed(new LocalDate(2008, 2, 18));
//    assertTrue("billing item added (value)",
//                 expectedBillingItem.isEquivalent(((Set<BillingItem>) getBeanValue("screenDetailViewer.screen.billingInformation.billingItems")).iterator().next()));

    submit("saveCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertElementTextEqualsRegex("billingInformationToggleText", "Hide.*");
    assertEquals("billing item added (count)",
                 1, getCollectionSize("screenDetailViewer.screen.billingInformation.billingItems"));
//    assertTrue("billing item added (value)",
//               expectedBillingItem.isEquivalent(((Set<BillingItem>) getBeanValue("screenDetailViewer.screen.billingInformation.billingItems")).iterator().next()));
  }

  private void visitScreenViewer(Screen screen)
  {
    ScreenViewer viewer = getBeanValue("screenViewer");
    viewer.viewScreen(screen);
    visitPage("/screens/screensBrowser.jsf");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
  }
}