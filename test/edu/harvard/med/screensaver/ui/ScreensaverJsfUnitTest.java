// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import org.apache.log4j.Logger;
import org.jboss.jsfunit.facade.JSFClientSession;
import org.jboss.jsfunit.facade.JSFServerSession;
import org.jboss.jsfunit.framework.WebConversationFactory;

import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;

public class ScreensaverJsfUnitTest extends org.apache.cactus.ServletTestCase
{
  
  private static final Logger log = Logger.getLogger(ScreensaverJsfUnitTest.class);

  private static final String TEST_USER_NAME = "testuser";
  private static final String TEST_USER_PASSWORD = "testuser";
  
  public static Test suite()
  {
    return new TestSuite(ScreensaverJsfUnitTest.class);
  }

  private SchemaUtil _schemaUtil;
  private AdministratorUser _testUser;
  private GenericEntityDAO _dao;

  private WebConversation _webConv;
  
  @Override
  protected void setUp() throws Exception
  {
    // we create a CommandLineApplication so that we can obtain the DAO object
    // that is needed to initialize the test database; we need the
    // CommandLineApplication to get around a Catch-22: we cannot use the web
    // app's DAO object, since obtaining it first requires making a request,
    // which in turn requires authenticating as user, although no users are
    // defined in the database until we create one, which requires the DAO
    // object
    CommandLineApplication app = new CommandLineApplication(new String[] {});
    app.setSpringConfigurationResource("spring-context-cmdline.xml");
    boolean processOptions = app.processOptions(true, false);
    assertTrue("initialize command line application", processOptions);
    _dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    _schemaUtil = (SchemaUtil) app.getSpringBean("schemaUtil");

    _schemaUtil.truncateTablesOrCreateSchema();
    log.debug("truncated database tables");

    _testUser = new AdministratorUser("Test",
                                      "User",
                                      "test_user@hms.harvard.edu",
                                      "",
                                      "",
                                      "for jsfunit testing",
                                      "testuser",
                                      TEST_USER_PASSWORD);
    _testUser.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
    _testUser.addScreensaverUserRole(ScreensaverUserRole.DEVELOPER);
    _testUser.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    _testUser.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    _testUser.addScreensaverUserRole(ScreensaverUserRole.SCREEN_RESULTS_ADMIN);
    _testUser.addScreensaverUserRole(ScreensaverUserRole.CHERRY_PICK_ADMIN);
    _dao.persistEntity(_testUser);
    log.debug("created test user");

    // We assume BASIC servlet authentication mechanism is in use, and so we
    // specify authentication credentials in this way; all unit tests here can
    // assume the test user is logged in
    _webConv = WebConversationFactory.makeWebConversation();
    _webConv.setAuthorization(TEST_USER_NAME, TEST_USER_PASSWORD);

    JSFClientSession client = new JSFClientSession(_webConv, "/main/main.jsf");
    JSFServerSession server = new JSFServerSession(client);
    assertEquals("testscreensaver1",
                 server.getManagedBeanValue("#{envInfo.db}"));
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
    
    JSFClientSession client = new JSFClientSession(_webConv, "/libraries/wellFinder.jsf");
    JSFServerSession server = new JSFServerSession(client);
    
    WebForm form = client.getWebResponse().getFormWithID("wellFinderForm");
    form.setParameter("plateWellList", "01000 A1 A3");
    SubmitButton findWellsButton = form.getSubmitButton("wellFinderForm:findWellsSubmit");
    form.submit(findWellsButton);
    
    assertEquals("/libraries/wellSearchResults.xhtml", server.getCurrentViewID());
    
    DataTableModel model = (DataTableModel) server.getManagedBeanValue("#{wellsBrowser.dataTableModel}");
    assertEquals("row count", 2, model.getRowCount());
  }
  
}