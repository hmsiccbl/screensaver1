// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import java.io.IOException;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import edu.harvard.med.authentication.AuthenticationClient;
import edu.harvard.med.authentication.AuthenticationRequestException;
import edu.harvard.med.authentication.AuthenticationResponseException;
import edu.harvard.med.authentication.AuthenticationResult;
import edu.harvard.med.authentication.Credentials;
import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.log4j.Logger;

public class ScreensaverLoginModuleTest extends AbstractSpringTest
{
  
  // static data members
  
  private static final Logger log = Logger.getLogger(ScreensaverLoginModuleTest.class);
  
  private static final String TEST_VALID_ECOMMONS_USER_LOGIN = "ecom";
  private static final String TEST_VALID_SCREENSAVER_USER_LOGIN = "screensaverId";
  private static final String TEST_VALID_ECOMMONS_PASSWORD = "eCommonsPassword";
  private static final String TEST_VALID_SCREENSAVER_PASSWORD = "screensaverPassword";
  private static final String TEST_INVALID_USER_LOGIN = "!testUser";
  private static final String TEST_INVALID_PASSWORD = "!testPassword";

  
  // Spring-injected data members (must have protected access)
  
  protected ScreensaverLoginModule screensaverLoginModule;
  protected SchemaUtil schemaUtil;
  protected DAO dao;

  
  // instance data
  
  private CallbackHandler _mockCallbackHandlerForValidEcommonsUserAndPassword;
  private CallbackHandler _mockCallbackHandlerForValidEcommonsUserInvalidPassword;
  private CallbackHandler _mockCallbackHandlerForValidScreensaverUserAndPassword;
  private CallbackHandler _mockCallbackHandlerForValidScreensaverUserInvalidPassword;
  private CallbackHandler _mockCallbackHandlerForInvalidUser;
  private AuthenticationClient _mockECommonsAuthenticationClient;
  private Subject _subject;
  private ScreensaverUser _validUser;
  

  // test setup methods
  
  @Override
  protected String[] getConfigLocations() {
    return new String[] { "spring-context-test-security.xml" } ;
  }

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    _subject = new Subject();
    
    _mockCallbackHandlerForValidEcommonsUserAndPassword = new CallbackHandler() {
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
      {
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];
        nameCallback.setName(TEST_VALID_ECOMMONS_USER_LOGIN);
        passwordCallback.setPassword(TEST_VALID_ECOMMONS_PASSWORD.toCharArray());
      }
    };
    _mockCallbackHandlerForValidEcommonsUserInvalidPassword = new CallbackHandler() {
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
      {
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];
        nameCallback.setName(TEST_VALID_ECOMMONS_USER_LOGIN);
        passwordCallback.setPassword(TEST_INVALID_PASSWORD.toCharArray());
      }
    };
    _mockCallbackHandlerForValidScreensaverUserAndPassword = new CallbackHandler() {
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
      {
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];
        nameCallback.setName(TEST_VALID_SCREENSAVER_USER_LOGIN);
        passwordCallback.setPassword(TEST_VALID_SCREENSAVER_PASSWORD.toCharArray());
      }
    };
    _mockCallbackHandlerForValidScreensaverUserInvalidPassword = new CallbackHandler() {
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
      {
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];
        nameCallback.setName(TEST_VALID_SCREENSAVER_USER_LOGIN);
        passwordCallback.setPassword(TEST_INVALID_PASSWORD.toCharArray());
      }
    };
    _mockCallbackHandlerForInvalidUser = new CallbackHandler() {
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
      {
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];
        nameCallback.setName(TEST_INVALID_USER_LOGIN);
        passwordCallback.setPassword(TEST_INVALID_PASSWORD.toCharArray());
      }
    };
    
    _mockECommonsAuthenticationClient = new AuthenticationClient() {
      public AuthenticationResult authenticate(Credentials credentials) throws AuthenticationRequestException, AuthenticationResponseException
      {
        return new TestAuthenticationResult(credentials,
                                            credentials.getUserId().equals(TEST_VALID_ECOMMONS_USER_LOGIN) && 
                                            credentials.getPassword().equals(TEST_VALID_ECOMMONS_PASSWORD));
      }
    };
    screensaverLoginModule.setAuthenticationClient(_mockECommonsAuthenticationClient);
    
    schemaUtil.truncateTablesOrCreateSchema();

    // create a user
    _validUser = dao.defineEntity(ScreensaverUser.class, "Iam", "Authorized", "iam_authorized@unittest.com");
    _validUser.setLoginId(TEST_VALID_SCREENSAVER_USER_LOGIN);
    _validUser.updateScreensaverPassword(new String(TEST_VALID_SCREENSAVER_PASSWORD));
    _validUser.setECommonsId(TEST_VALID_ECOMMONS_USER_LOGIN);
    _validUser.addScreensaverUserRole(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
    _validUser.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
    dao.persistEntity(_validUser);
  }
  
  
  // test methods

  public void testInitialize()
  {
    screensaverLoginModule.initialize(_subject,
                                      _mockCallbackHandlerForValidEcommonsUserAndPassword,
                                      new HashMap(),
                                      new HashMap());
    // hmmm...nothing we can assert...well, that's an easy test to pass! (simply don't throw an exception!)
  }

  public void testLoginLogoutValidScreensaverUserAndPassword() 
  {
    try {
      screensaverLoginModule.initialize(_subject,
                                        _mockCallbackHandlerForValidScreensaverUserAndPassword,
                                        new HashMap(),
                                        new HashMap());
      
      assertSuccessfulLoginAndLogout();
    }
    catch (LoginException e) {
      e.printStackTrace();
      fail("login failed due to exception: " + e.getMessage());
    }
  }

  public void testLoginLogoutValidEcommonsUserAndPassword() 
  {
    try {
      screensaverLoginModule.initialize(_subject,
                                        _mockCallbackHandlerForValidEcommonsUserAndPassword,
                                        new HashMap(),
                                        new HashMap());
      
      assertSuccessfulLoginAndLogout();
    }
    catch (LoginException e) {
      e.printStackTrace();
      fail("login failed due to exception: " + e.getMessage());
    }
  }

  public void testLoginValidEcommonsUserInvalidPassword() 
  {
    try {
      screensaverLoginModule.initialize(_subject,
                                        _mockCallbackHandlerForValidEcommonsUserInvalidPassword,
                                        new HashMap(),
                                        new HashMap());
      screensaverLoginModule.login();
      fail("expected login failure");
    }
    catch (FailedLoginException e) {
      assertEquals("principals count", 0, _subject.getPrincipals().size());
      // test passed!
    }
    catch (LoginException e) {
      e.printStackTrace();
      fail("login failed due to exception " + e.getMessage());
    }
  }
  
  public void testLoginValidScreensaverUserInvalidPassword() 
  {
    try {
      screensaverLoginModule.initialize(_subject,
                                        _mockCallbackHandlerForValidScreensaverUserInvalidPassword,
                                        new HashMap(),
                                        new HashMap());
      screensaverLoginModule.login();
      fail("expected login failure");
    }
    catch (FailedLoginException e) {
      assertEquals("principals count", 0, _subject.getPrincipals().size());
      // test passed!
    }
    catch (LoginException e) {
      e.printStackTrace();
      fail("login failed due to exception " + e.getMessage());
    }
  }
  
  public void testLoginInvalidUser() 
  {
    try {
      screensaverLoginModule.initialize(_subject,
                                        _mockCallbackHandlerForInvalidUser,
                                        new HashMap(),
                                        new HashMap());
      screensaverLoginModule.login();
      fail("expected login failure");
    }
    catch (FailedLoginException e) {
      assertEquals("principals count", 0, _subject.getPrincipals().size());
      // test passed!
    }
    catch (LoginException e) {
      e.printStackTrace();
      fail("login failed due to exception " + e.getMessage());
    }
  }
  
  
  public void testLoginWithAbort()
  {
    // TODO: implement (but code can practically be verified by inspection alone...)
  }
  
  private void assertSuccessfulLoginAndLogout() throws LoginException
  {
    screensaverLoginModule.login();

    assertEquals("principals count", 0, _subject.getPrincipals().size());
    boolean loginResult = screensaverLoginModule.login();
    assertTrue("LoginModule's login method is \"in use\"", loginResult);
    assertEquals("principals count", 0, _subject.getPrincipals().size());

    boolean commitResult = screensaverLoginModule.commit();
    assertTrue("LoginModule's commit succeeded", commitResult);
    assertEquals("principals count", 3, _subject.getPrincipals().size());
    assertTrue("subject contains \"user\" Principal",
               _subject.getPrincipals().contains(new ScreensaverUserPrincipal(_validUser)));
    assertTrue("subject contains user role compoundScreeningRoomUser Principal",
                 _subject.getPrincipals().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER));
    assertTrue("subject contains user role rnaiScreeningRoomUser Principal",
               _subject.getPrincipals().contains(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER));
    assertFalse("subject does not contain user role userAdmin Principal",
                _subject.getPrincipals().contains(ScreensaverUserRole.USERS_ADMIN));

    boolean logoutResult = screensaverLoginModule.logout();
    assertTrue("LoginModule's logout method is \"in use\"", logoutResult);
    assertEquals("principals count reset", 0, _subject.getPrincipals().size());
  }

  private static class TestAuthenticationResult implements AuthenticationResult
  {
    
    private boolean _isAuthenticated;
    private Credentials _credentials;

    public TestAuthenticationResult(
      Credentials credentials,
      boolean isAuthenticated)
    {
      _credentials = credentials;
      _isAuthenticated = isAuthenticated;
    }

    public Credentials getCredentials()
    {
      return _credentials;
    }

    public boolean isAuthenticated() throws AuthenticationResponseException
    {
      return _isAuthenticated;
    }

    public int getStatusCode() throws AuthenticationResponseException
    {
      return _isAuthenticated ? 1 : -1;
    }

    public String getStatusCodeCategory() throws AuthenticationResponseException
    {
      return "";
    }

    public String getStatusMessage() throws AuthenticationResponseException
    {
      return "";
    }
  }

}
