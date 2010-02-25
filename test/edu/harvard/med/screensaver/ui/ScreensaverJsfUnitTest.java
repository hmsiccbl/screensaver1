// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import jxl.Sheet;
import jxl.Workbook;

import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.screens.ScreenViewerJsfUnitTest;
import edu.harvard.med.screensaver.ui.searchresults.GenericDataExporter;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.jsfunit.facade.JSFClientSession;
import org.jboss.jsfunit.facade.JSFServerSession;
import org.joda.time.LocalDate;
import org.xml.sax.SAXException;

/**
 * Top-level class for user interface unit tests. (As this grows, it will be
 * refactored into a reasonable hierarchy of classes.)
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreensaverJsfUnitTest extends AbstractJsfUnitTest
{
  
  private static final Logger log = Logger.getLogger(ScreensaverJsfUnitTest.class);

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ScreensaverJsfUnitTest.class);
    suite.addTest(ScreenViewerJsfUnitTest.suite());
    suite.addTest(InfrastructureJsfUnitTest.suite());
    return suite;
  }

//  public void testLogin() throws IOException, SAXException
//  {
//    JSFClientSession client = new JSFClientSession(_webConv, "/main/main.jsf");
//    JSFServerSession server = new JSFServerSession(client);
//
//    if (server.getCurrentViewID().equals("/main/login.xhtml")) {
//      log.info("logging in");
//      WebForm loginForm = client.getWebResponse().getFormWithID("loginForm");
//      assertNotNull("login form exists", loginForm);
//      loginForm.setParameter("j_username", _testUser.getLoginId());
//      loginForm.setParameter("j_password", TEST_USER_PASSWORD);
//      client.submit("loginForm:loginCommand");
////      Button loginButton = loginForm.getButtonWithID("loginForm:loginCommand");
////      assertNotNull("login command button exists", loginButton);
////      loginForm.submit(loginButton);
//      log.info("submitted login request");
//    };
//    server = new JSFServerSession(client);
//  }
  
//public void testLogout() throws Exception
//{
//  JSFClientSession client = new JSFClientSession(_webConv, "/main/main.jsf");
//  JSFServerSession server = new JSFServerSession(client);
//  assertEquals("/main/main.xhtml", server.getCurrentViewID());
//  client.clickCommandLink("userForm:logout");
//  assertEquals("/main/goodbye.xhtml", server.getCurrentViewID());
//  assertFalse("user is not logged in",
//              ((Boolean) server.getManagedBeanValue("#{appInfo.authenticatedUser}")).booleanValue());
//}


  public void testMainPage() throws Exception
  {
    JSFClientSession client = new JSFClientSession(_webConv, "/main/main.jsf");
    JSFServerSession server = new JSFServerSession(client);

    assertEquals("/main/main.xhtml", server.getCurrentViewID());
  
    // test current user is correct
    assertEquals("user is logged in", 
                 TEST_USER_NAME,
                 server.getManagedBeanValue("#{appInfo.screensaverUser.loginId}"));

    // test something on the page
    assertEquals(server.getComponentValue("welcomeMessage"),
                 "Welcome to Screensaver, Test User!");
  }
  
  public void testFindWells() throws Exception
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    _dao.persistEntity(library);

    visitPage("/libraries/wellFinder.jsf");
    submit("findWellsCommand", new Pair<String,String>("plateWellList", "01000 A1 A3")); 
    assertAtView("/screensaver/libraries/wellSearchResults.jsf");

    DataTableModel model = getBeanValue("wellsBrowser.dataTableModel");
    assertEquals("row count", 2, model.getRowCount());
  }
  
  public void testDownloadSearchResults() throws Exception
  {
    final Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    _dao.persistEntity(library);
    
    JSFClientSession client = new JSFClientSession(_webConv, "/libraries/wellFinder.jsf");
    JSFServerSession server = new JSFServerSession(client);
    client.setParameter("plateWellList", "01000 A1 A2 G12 G13 H20");
    client.submit("wellFinderForm:findWellsCommand");
    assertEquals("/libraries/wellSearchResults.xhtml", server.getCurrentViewID());
    assertEquals("search result size", new Integer(5), server.getManagedBeanValue("#{wellsBrowser.dataTableModel.rowCount}"));
  
    DataExporter<?> dataExporter = (DataExporter<?>) server.getManagedBeanValue("#{wellsBrowser.dataExporters[0]}");
    assertNotNull("exporter exists", dataExporter);
    assertEquals("exporter type", GenericDataExporter.FORMAT_NAME, dataExporter.getFormatName());
    // TODO: this is causing an error; we can rely upon the current behavior of the desired exporter being selected by default, by this makes the test more fragile, of course
//    client.setParameter("downloadFormat",  Integer.toString(dataExporter.hashCode()));
    client.submit("exportSearchResultsCommandButton");
    assertEquals("download file content type", GenericDataExporter.FORMAT_MIME_TYPE, client.getWebResponse().getContentType());
    assertEquals("download file name", "searchResult.xls", client.getWebResponse().getHeaderField("Content-Location"));
    Workbook workbook = Workbook.getWorkbook(client.getWebResponse().getInputStream());
    Sheet sheet = workbook.getSheet(0);
    assertEquals("workbook contents", "Library", sheet.getCell(0, 0).getContents());
    assertEquals("workbook contents", "Plate", sheet.getCell(1, 0).getContents());
    assertEquals("workbook contents", "Well", sheet.getCell(2, 0).getContents());
    
    // TODO: test filtering is respected in export
  }
  
//  public void testAddAndDeleteLibraryScreeningPlatesScreened() throws InterruptedException, MalformedURLException, IOException, SAXException
//  {
//    LibraryScreening libraryScreening = initializeLibraryScreening();
//    Library library = MakeDummyEntities.makeDummyLibrary(10, libraryScreening.getScreen().getScreenType(), 2);
//    _dao.persistEntity(library);
//
//    JSFClientSession client = new JSFClientSession(_webConv, "/main.jsf");
//    JSFServerSession server = new JSFServerSession(client);
//
//    ActivityViewer activityViewer = (ActivityViewer) server.getManagedBeanValue("#{activityViewer}");
//    activityViewer.viewActivity(libraryScreening);
//    
//    client = new JSFClientSession(_webConv, "/activities/activityViewer.jsf");
//    server = new JSFServerSession(client);
//
//    assertEquals(libraryScreening.getEntityId(),
//                 server.getManagedBeanValue("#{activityViewer.activity.entityId}"));
//    client.submit("editCommand");
//    assertEquals(Boolean.TRUE, server.getManagedBeanValue("#{activityViewer.editMode}"));
//    client.setParameter("newPlatesScreenedStartPlateField", "10000");
//    client.setParameter("newPlatesScreenedEndPlateField", "10001");
//    client.setParameter("newPlatesScreenedCopyField", "C");
//    client.submit("platesScreenedCollectionTableAddCommand");
//    DataModel model = (DataModel) server.getManagedBeanValue("#{activityViewer.libraryAndPlatesScreenedDataModel");
//    assertEquals(1, model.getRowCount());
//    model.setRowIndex(0);
//    LibraryAndPlatesUsed libraryAndPlatesUsed = (LibraryAndPlatesUsed) model.getRowData();
//    assertEquals(new Integer(10000), libraryAndPlatesUsed.getPlatesUsed().getStartPlate());
//    assertEquals(new Integer(10001), libraryAndPlatesUsed.getPlatesUsed().getStartPlate());
//    assertEquals("C", libraryAndPlatesUsed.getPlatesUsed().getCopy());
//    assertEquals(library, libraryAndPlatesUsed.getLibrary());
//    client.submit("saveCommand");
//    assertEquals(Boolean.FALSE, server.getManagedBeanValue("#{activityViewer.editMode}"));
//    model = (DataModel) server.getManagedBeanValue("#{activityViewer.libraryAndPlatesScreenedDataModel");
//    assertEquals(1, model.getRowCount());
//    libraryAndPlatesUsed = (LibraryAndPlatesUsed) model.getRowData();
//    assertEquals(new Integer(10000), libraryAndPlatesUsed.getPlatesUsed().getStartPlate());
//    assertEquals(new Integer(10001), libraryAndPlatesUsed.getPlatesUsed().getStartPlate());
//    assertEquals("C", libraryAndPlatesUsed.getPlatesUsed().getCopy());
//    assertEquals(library, libraryAndPlatesUsed.getLibrary());
//    client.submit("editCommand");
//    model = (DataModel) server.getManagedBeanValue("#{activityViewer.libraryAndPlatesScreenedDataModel");
//    assertEquals(1, model.getRowCount());
//    model.setRowIndex(0);
//    activityViewer.deletePlatesScreened();
//    client.submit("saveCommand");
//    model = (DataModel) server.getManagedBeanValue("#{activityViewer.libraryAndPlatesScreenedDataModel");
//    assertEquals(0, model.getRowCount());
//    assertEquals(Collections.emptySet(), server.getManagedBeanValue("#{activityViewer.activity.platesUsed}"));
//  }

  public void testAddLibraryScreeningDuplicatesMostRecentScreening() throws InterruptedException, MalformedURLException, IOException, SAXException
  {
    LibraryScreening previousScreening = initializeLibraryScreening();

    JSFClientSession client = new JSFClientSession(_webConv, "/main.jsf");
    JSFServerSession server = new JSFServerSession(client);

    client.setParameter("screenNumber", "1");
    client.submit("findScreenCommand");
    assertEquals("/screens/screenViewer.xhtml", server.getCurrentViewID());
    assertEquals(new Integer(1), 
                 server.getManagedBeanValue("#{screenDetailViewer.labActivitiesDataModel.rowCount}"));

    client.submit("addLibraryScreeningCommand");
    assertEquals("/activities/activityViewer.xhtml", server.getCurrentViewID());
    assertEquals(previousScreening.getAssayProtocol(),
                 server.getManagedBeanValue("#{activityViewer.activity.assayProtocol}"));
    assertEquals(previousScreening.getAssayProtocolType(),
                 server.getManagedBeanValue("#{activityViewer.activity.assayProtocolType}"));
    assertEquals(previousScreening.getNumberOfReplicates(),
                 server.getManagedBeanValue("#{activityViewer.activity.numberOfReplicates}"));
    assertEquals(previousScreening.getVolumeTransferredPerWell(),
                 server.getManagedBeanValue("#{activityViewer.activity.volumeTransferredPerWell}"));
    assertFalse(previousScreening.getEntityId().equals(server.getManagedBeanValue("#{activityViewer.activity.entityId}")));
  }
  
  private LibraryScreening initializeLibraryScreening()
  {
    LibraryScreening previousScreening = (LibraryScreening) _dao.runQuery(new Query() {
      public List execute(Session session) {
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        LibraryScreening previousScreening = screen.createLibraryScreening((AdministratorUser) screen.getCreatedBy(), screen.getLeadScreener(), new LocalDate());
        previousScreening.setAssayProtocol("assay protocol test value");
        _dao.persistEntity(screen.getLabHead());
        _dao.persistEntity(screen.getLeadScreener());
        _dao.persistEntity(screen);
        return Arrays.asList(previousScreening);
      }
    }).get(0);
    return previousScreening;
  }
  
}