// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;




import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.BillingItem;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusItem;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractJsfUnitTest;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.xml.sax.SAXException;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@SuppressWarnings("unchecked")
public class ScreenViewerJsfUnitTest extends AbstractJsfUnitTest
{
  private static final Logger log = Logger.getLogger(ScreenViewerJsfUnitTest.class);
  private Screen _screen;
  private AttachedFileType _attachedFileType;

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

    _attachedFileType = new ScreenAttachedFileType("Application");
    _dao.saveOrUpdateEntity(_attachedFileType);
    
    // ensure _screen entity has an ID-based hashCode
    _screen = _dao.reloadEntity(_screen, true, "labHead", "leadScreener", "collaborators");
    _attachedFileType = _dao.reloadEntity(_attachedFileType, true);
  }

  public void testOpenScreenViewer() throws Exception
  {
    visitScreenViewer(_screen);
    assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
    assertPageContainsText(_screen.getTitle());
  }

  public void testAddAndSaveScreenWithUninitializedScreensBrowser() throws Exception
  {
    doAddScreen();
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
  }

  public void testAddAndSaveScreenWithInitializedScreensBrowser() throws Exception
  {
    _client.clickCommandLink("browseScreensCommand");
    doAddScreen();
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
  }

  private void doAddScreen() throws SAXException, IOException
  {
    submit("addScreenCommand");
    assertShowingScreen(2, true);
    submit("submitLabHead",
           new Pair<String,String>("screenDetailViewerForm:labNameMenu", Integer.toString(_screen.getLabHead().hashCode())));
    assertEquals(2, getBeanValue("screenDetailViewer.leadScreener.size"));
    submit("saveCommand",
           new Pair<String,String>("screenDetailViewerForm:titleTextField", "Test screen title"),
           new Pair<String,String>("screenDetailViewerForm:leadScreenerMenu", Integer.toString(_screen.getLabHead().hashCode())),
           new Pair<String,String>("screenDetailViewerForm:screenTypeMenu", ScreenType.SMALL_MOLECULE.toString()));
    assertNotNull("screen created", _dao.findEntityByProperty(Screen.class, "screenNumber", 2));
    assertShowingScreen(2, false);
  }

  public void testAddAndCancelScreen() throws Exception
  {
    submit("addScreenCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    assertTrue("edit mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    submit("cancelEditCommand");
    assertAtView("/screensaver/main/main.jsf");
    assertEquals("screen not created", 1, _dao.findAllEntitiesOfType(Screen.class).size());
  }

  public void testEditAndSaveScreen() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("saveCommand",
           new Pair<String,String>("screenDetailViewerForm:titleTextField", "new title"));
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertEquals(1, getBeanValue("screensBrowser.rowCount"));
    assertEquals("title changed", "new title", getBeanValue("screenViewer.screen.title"));
    submit("summaryViewerCommand");
    assertPageContainsText("new title"); // ensure search result was updated with edited screen data
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
    assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
    assertFalse("read-only mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    assertEquals("title not changed", oldTitle, getBeanValue("screenViewer.screen.title"));
  }

  public void testAddAndDeleteStatusItem() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
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
    assertShowingScreen(_screen.getScreenNumber(), false);

    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("0:statusItemsTableDeleteCommand");
    assertEquals(1, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
    submit("saveCommand");
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertEquals(1, ((Set<StatusItem>) getBeanValue("screenDetailViewer.screen.statusItems")).size());
  }

  public void testFindScreenNumber() throws Exception
  {
    Screen screen2 = MakeDummyEntities.makeDummyScreen(2, ScreenType.SMALL_MOLECULE);
    _dao.persistEntity(screen2);

    // test from virgin session, screens browser not yet opened/initialized
    visitMainPage();
    submit("findScreenCommand", new Pair<String,String>("screenNumber", "2"));
    assertShowingScreen(2, false);
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));

    // test after screens browser has been opened/initialized; screen should be found in existing screen search results context
    _client.clickCommandLink("browseScreensCommand");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
    submit("findScreenCommand", new Pair<String,String>("screenNumber", "1"));
    assertShowingScreen(1, false);
    assertEquals(2, getBeanValue("screensBrowser.rowCount"));
  }

  public void testAddAndDeleteCollaborators() throws Exception
  {
    ScreeningRoomUser collaborator = new ScreeningRoomUser("Col", "Laborator");
    _dao.persistEntity(collaborator);
    collaborator = _dao.reloadEntity(collaborator);

    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("collaboratorsTableAddCommand",
           new Pair<String,String>("collaboratorsEditable", Integer.toString(collaborator.hashCode())));
    assertTrue(((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.collaborators")).contains(collaborator));
    submit("saveCommand");
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertTrue(((Set<ScreeningRoomUser>) getBeanValue("screenViewer.screen.collaborators")).contains(collaborator));

    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("0:collaboratorsTableDeleteCommand");
    assertFalse(((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.collaborators")).contains(collaborator));
    submit("saveCommand");
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertFalse(((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.collaborators")).contains(collaborator));
  }

  public void testAddAndDownloadAndDeleteAttachedFiles() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("attachedFilesTableAddCommand",
           new Pair<String,String>("newAttachedFileType", Integer.toString(_attachedFileType.getEntityId())),
           new Pair<String,String>("newAttachedFilenameTextField", "test file name"),
           new Pair<String,String>("newAttachedFileContentsTextareaField1", "test file contents"));
    assertEquals(1, ((Set<AttachedFile>) getBeanValue("screenDetailViewer.screen.attachedFiles")).size());
    assertPageContainsText("test file name");
    submit("saveCommand");
    assertShowingScreen(_screen.getScreenNumber(), false);
    _client.clickCommandLink("viewAdminOnlyFieldsCommand");
    assertTrue("admin view mode", ((Boolean) getBeanValue("screenDetailViewer.adminViewMode")));
    assertEquals(1, ((Set<AttachedFile>) getBeanValue("screenDetailViewer.screen.attachedFiles")).size());
    assertPageContainsText("test file name");
    
    // TODO: test this
    //_client.clickCommandLink("downloadAttachedFileCommandLink");

    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("0:attachedFilesTableDeleteCommand");
    assertEquals(0, ((Set<AttachedFile>) getBeanValue("screenDetailViewer.screen.attachedFiles")).size());
    assertPageContainsText("test file name", false);
    submit("saveCommand");
    assertEquals(0, ((Set<AttachedFile>) getBeanValue("screenDetailViewer.screen.attachedFiles")).size());
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertTrue("admin view mode", ((Boolean) getBeanValue("screenDetailViewer.adminViewMode")));
    assertPageContainsText("test file name", false);
  }

  public void testAddAndDeletePublication() throws Exception
  {
    visitScreenViewer(_screen);
    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
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
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertEquals(1, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertPageContainsText("Screensaver LIMS");

    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
    submit("0:publicationsTableDeleteCommand");
    assertEquals(0, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertPageContainsText("Screensaver LIMS", false);
    submit("saveCommand");
    assertEquals(0, ((Set<ScreeningRoomUser>) getBeanValue("screenDetailViewer.screen.publications")).size());
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertPageContainsText("Screensaver LIMS", false);
  }

  public void testAddBillingInformationAndBillingItems() throws Exception
  {
    visitScreenViewer(_screen);
    assertNull(getBeanValue("screenDetailViewer.screen.billingInformation"));
    assertElementExists("billingInformationToggleText", false);
    submit("screenDetailPanelForm:editCommand");
    assertShowingScreen(_screen.getScreenNumber(), true);
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
    expectedBillingItem.setDateSentForBilling(new LocalDate(2008, 2, 18));
//    assertTrue("billing item added (value)",
//                 expectedBillingItem.isEquivalent(((Set<BillingItem>) getBeanValue("screenDetailViewer.screen.billingInformation.billingItems")).iterator().next()));

    submit("saveCommand");
    assertShowingScreen(_screen.getScreenNumber(), false);
    assertElementTextEqualsRegex("billingInformationToggleText", "Hide.*");
    assertEquals("billing item added (count)",
                 1, getCollectionSize("screenDetailViewer.screen.billingInformation.billingItems"));
//    assertTrue("billing item added (value)",
//               expectedBillingItem.isEquivalent(((Set<BillingItem>) getBeanValue("screenDetailViewer.screen.billingInformation.billingItems")).iterator().next()));
  }

  private void visitScreenViewer(Screen screen)
  {
    ScreenViewer viewer = getBeanValue("screenViewer");
    viewer.viewEntity(screen);
    visitPage("/screens/screensBrowser.jsf");
    assertAtView("/screensaver/screens/screensBrowser.jsf");
  }

  private void assertShowingScreen(int screenNumber, boolean isEditModeExpected)
  {
    if (isEditModeExpected) {
      assertEquals("screenDetailViewer screen number",
                   new Integer(screenNumber),
                   (Integer) getBeanValue("screenDetailViewer.screen.screenNumber"));
      // when in edit mode, screen is shown within a screenDetailViewer, outside of a search results context
      assertAtView("/screensaver/screens/screenDetailViewer.jsf");
      assertElementTextEqualsRegex("screenDetailViewerForm:screenNumberLinkValue", Integer.toString(screenNumber));
      assertTrue("edit mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    }
    else {
      assertEquals("screenViewer screen number",
                   new Integer(screenNumber),
                   (Integer) getBeanValue("screenViewer.screen.screenNumber"));
      // when not in edit mode, screen is always to be shown within a search results context
      assertAtView("/screensaver/screens/screensBrowser.jsf");
      assertTrue(((Boolean) getBeanValue("screensBrowser.entityView")).booleanValue());
      assertElementTextEqualsRegex("screenDetailPanelForm:screenNumberLinkValue", Integer.toString(screenNumber));
      assertFalse("read-only mode", (Boolean) getBeanValue("screenDetailViewer.editMode"));
    }
  }
}