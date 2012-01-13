// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.auth;

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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import edu.harvard.med.authentication.AuthenticationClient;
import edu.harvard.med.authentication.AuthenticationRequestException;
import edu.harvard.med.authentication.AuthenticationResponseException;
import edu.harvard.med.authentication.AuthenticationResult;
import edu.harvard.med.authentication.Credentials;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.test.AbstractSpringTest;

@ContextConfiguration(locations = { "/spring-context-test-security.xml", "/spring-context-authentication.xml" }, inheritLocations = false)
public class ScreensaverLoginModuleTest extends AbstractSpringTest
{
  
  // static data members
  
  private static final Logger log = Logger.getLogger(ScreensaverLoginModuleTest.class);
  
  private static final String TEST_VALID_ECOMMONS_USER_LOGIN = "ecom";
  private static final String TEST_VALID_SCREENSAVER_USER_LOGIN = "screensaverId";
  private static final String TEST_VALID_SCREENSAVER_USER_EMAIL = "test.user@screensaver.com";
  private static final String TEST_VALID_ECOMMONS_PASSWORD = "eCommonsPassword";
  private static final String TEST_VALID_SCREENSAVER_PASSWORD = "screensaverPassword";
  private static final String TEST_INVALID_USER_LOGIN = "!testUser";
  private static final String TEST_INVALID_PASSWORD = "!testPassword";
  
  // Spring-injected data members (must have @Autowired protected access)
  

  @Autowired
  protected ScreensaverLoginModule screensaverLoginModule;
  @Autowired
  protected SchemaUtil schemaUtil;
  @Autowired
  protected GenericEntityDAO genericEntityDao;

  
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
  protected void setUp() throws Exception
  {
    super.setUp();
    
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
    
    schemaUtil.truncateTables();

    // create a user
    _validUser = new ScreeningRoomUser("Iam", "Authorized");
    _validUser.setEmail(TEST_VALID_SCREENSAVER_USER_EMAIL);
    _validUser.setECommonsId(TEST_VALID_ECOMMONS_USER_LOGIN);
    _validUser.setLoginId(TEST_VALID_SCREENSAVER_USER_LOGIN);
    _validUser.updateScreensaverPassword(TEST_VALID_SCREENSAVER_PASSWORD);
    _validUser.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    _validUser.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
    _validUser.addScreensaverUserRole(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);
    genericEntityDao.saveOrUpdateEntity(_validUser);
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
  
  public void testLoginValidUserWithoutLoginPrivileges()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = genericEntityDao.findEntityByProperty(ScreensaverUser.class,
                                                                     "email",
                                                                     _validUser.getEmail());
        user.getScreensaverUserRoles().remove(ScreensaverUserRole.SCREENSAVER_USER);
      }
    });

    try {
      screensaverLoginModule.initialize(_subject,
                                        _mockCallbackHandlerForValidScreensaverUserAndPassword,
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
    assertEquals("principals count", 4, _subject.getPrincipals().size());
    assertTrue("subject contains \"user\" Principal",
               _subject.getPrincipals().contains(new ScreensaverUserPrincipal(_validUser)));
    assertTrue("subject contains user role screensaverUser Principal",
               _subject.getPrincipals().contains(ScreensaverUserRole.SCREENSAVER_USER));
    assertTrue("subject contains user role smallMoleculeScreeningRoomUser Principal",
                 _subject.getPrincipals().contains(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS));
    assertTrue("subject contains user role rnaiScreeningRoomUser Principal",
               _subject.getPrincipals().contains(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS));
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
