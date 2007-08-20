//$HeadURL$
//$Id$
//
//Copyright 2006 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import edu.harvard.med.authentication.AuthenticationClient;
import edu.harvard.med.authentication.AuthenticationRequestException;
import edu.harvard.med.authentication.AuthenticationResponseException;
import edu.harvard.med.authentication.AuthenticationResult;
import edu.harvard.med.authentication.Credentials;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.CryptoUtils;

import org.apache.log4j.Logger;
    

/**
 * This LoginModule authenticates users via one of two mechanisms, in the
 * following order:
 * <ol>
 * <li>Via the login ID and password stored in the Screensaver database</li>
 * <li>Via the injected AuthenticationClient.</li>
 * </ol>
 * <p>
 * If user is successfully authenticated, a set of <code>Principal</code>s
 * will be added to the login Subject, and which will be subsequently removed
 * when the user is logged out. The Principals are obtained from a database, via
 * a GenericEntityDAO object.
 * <p>
 * The LoginModule also allows administrators to login as normal users by
 * specifying a composite login ID that is formed by concatenating admin's login
 * ID with the user's login ID, separated by a colon. ("admin:user").
 */
// TODO: refactor into 3 separate LoginModules, one for normal login ID
// strategy, one for the AuthenticationClient strategy, and one for the
// composite admin:user login ID strategy. combine strategies via a
// ChainedLoginModule class.
public class ScreensaverLoginModule implements LoginModule
{
  
  private static final Logger log = Logger.getLogger(ScreensaverLoginModule.class);

  private static final String NO_SUCH_USER = "No such user";
  private static final String FOUND_SCREENSAVER_USER = "Found Screensaver user";
  private static final String FOUND_ECOMMONS_USER = "Found eCommons user";
  
  private GenericEntityDAO _dao;
  private AuthenticationClient _authenticationClient;

  // initial state
  private Subject _subject;
  private CallbackHandler _callbackHandler;
  @SuppressWarnings("unchecked")
  private Map _sharedState;
  @SuppressWarnings("unchecked")
  private Map _options;
  
  // the authentication status
  private AuthenticationResult _authenticationResult;
  private boolean _isAuthenticated = false;
  private boolean _commitSucceeded = false;

  /**
   * The Principals, which identify the user and the roles the Subject belongs
   * to, and that were granted by this LoginModule (other LoginModules may grant
   * the Subject other Principals, so the Subject's list of Principals may be a
   * superset). By Tomcat's JAASRealm conventions, the first Principal must be
   * the "user" Principal, while the others are "role" Principals.
   */
  private ArrayList<Principal> _grantedPrincipals;

  private ScreensaverUser _user;

  
  // property getter and setter methods
  
  public AuthenticationClient getAuthenticationClient()
  {
    return _authenticationClient;
  }

  public void setAuthenticationClient(AuthenticationClient authenticationClient)
  {
    _authenticationClient = authenticationClient;
  }

  public GenericEntityDAO getDao()
  {
    return _dao;
  }

  public void setDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  // LoginModule interface methods

  /**
   * Initialize this <code>LoginModule</code>.
   * 
   * @param subject the <code>Subject</code> to be authenticated.
   * @param callbackHandler a <code>CallbackHandler</code> for communicating
   *          with the end user (prompting for user names and passwords, for
   *          example).
   * @param sharedState shared <code>LoginModule</code> state.
   * @param options _options specified in the login
   *          <code>Configuration</pcode> for this particular
   *      <code>LoginModule</code>.
   */
  @SuppressWarnings("unchecked")
  public void initialize(
    Subject subject,
    CallbackHandler callbackHandler,
    Map sharedState,
    Map options)
  {
    log.debug("initialize()");
    _subject = subject;
    _callbackHandler = callbackHandler;
    _sharedState = sharedState;
    _options = options;
    _authenticationResult = null;
    _isAuthenticated = false;
    _commitSucceeded = false;
  }
  
  /**
   * Authenticate the user by prompting for a user name and password.
   * 
   * @return true in all cases since this <code>LoginModule</code> should not
   *         be ignored.
   * @throws FailedLoginException if the authentication fails.
   * @throws LoginException if this <code>LoginModule</code> is unable to
   *           perform the authentication.
   */
  public boolean login() throws LoginException {
    
    log.debug("login()");
    
    // prompt for a user name and password
    if (_callbackHandler == null)
      throw new LoginException("Error: no CallbackHandler available " +
      "to garner authentication information from the user");
    
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("user name: ");
    callbacks[1] = new PasswordCallback("password: ", false);
    
    String username;
    String switchToUsername = null;
    char[] password;
    try {
      _callbackHandler.handle(callbacks);
      // username and password
      username = ((NameCallback) callbacks[0]).getName();
      if (username.indexOf(':') > 0) {
        String[] names = username.split(":");
        username = names[0];
        switchToUsername = names[1];
      }
      char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
      // treat a NULL password as an empty password
      if (tmpPassword == null) {
        tmpPassword = new char[0];
      }
      password = new char[tmpPassword.length];
      System.arraycopy(tmpPassword, 0,
                       password, 0, tmpPassword.length);
      ((PasswordCallback)callbacks[1]).clearPassword();
      
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException("Error: " + uce.getCallback().toString() + 
                               " not available to garner authentication information from the user");
    }
    
    log.debug("attempting authentication for user '" + username + "'");
    //log.debug("user entered password: " + new String(password));

    if (authenticateUser(username, password)) {
      if (switchToUsername != null) {
        switchUser(_user, switchToUsername);
      }
      return true;
    }
    return false;
  }

  private boolean authenticateUser(String username, char[] password) throws LoginException
  {
    // verify the username/password
    try {
      _user = findUserByLoginId(username);
      if (_user != null) {
        log.info(FOUND_SCREENSAVER_USER + " '" + username + "'");
        if (_user.getDigestedPassword().equals(CryptoUtils.digest(password))) {
          _isAuthenticated = true;
          _authenticationResult = new SimpleAuthenticationResult(username,
                                                                 new String(password), 
                                                                 true,
                                                                 1,
                                                                 "success",
                                                                 "user authenticated with native Screensaver account");
        } 
        else {
          _isAuthenticated = false;
          _authenticationResult = new SimpleAuthenticationResult(username,
                                                                 new String(password), 
                                                                 _isAuthenticated,
                                                                 0,
                                                                 "failure",
                                                                 "user authentication failed for native Screensaver account");
        }
      }
      else {
        _user = findUserByECommonsId(username);
        if (_user != null) {
          log.info(FOUND_ECOMMONS_USER + " '" + _user.getECommonsId() + "'");
          _authenticationResult = _authenticationClient.authenticate(new Credentials(_user.getECommonsId(),
                                                                                     new String(password)));
          _isAuthenticated = _authenticationResult.isAuthenticated();
        }
        else {
          String message = NO_SUCH_USER + " '" + username + "'";
          log.info(message);
          throw new FailedLoginException(message);
        }
      }
      
      if (_isAuthenticated) {
        log.info("authentication succeeded for user '" + username + 
                 "' with status code " + _authenticationResult.getStatusCode() + 
                 " (" + _authenticationResult.getStatusCodeCategory() + ")");
        return true;
      }
      else {
        // authentication failed, clean out state
        String statusMessage = _authenticationResult.getStatusMessage();
        log.info("authentication failed for user '" + username + 
                 "' with status code " + _authenticationResult.getStatusCode() + 
                 " (" + _authenticationResult.getStatusCodeCategory() + ": '" + statusMessage + "')");
        reset(true);
        throw new FailedLoginException(statusMessage);
      }
    }
    catch (AuthenticationRequestException e) {
      log.error("error during login with authentication server request: " + e.getMessage());
      throw new LoginException(e.getMessage());
    }
    catch (AuthenticationResponseException e) {
      log.error("error during login with authentication server response: " + e.getMessage());
      throw new LoginException(e.getMessage());
    }
  }

  private ScreensaverUser switchUser(ScreensaverUser user, String switchToECommonsId) throws LoginException {
    if (!(user instanceof AdministratorUser && user.isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN))) {
      log.info("user " + user + " is not authorized to switch to another user");
    }
    else {
      ScreensaverUser switchToUser = findUserByECommonsId(switchToECommonsId);
      if (switchToUser == null) {
        String msg = "cannot switch to user " + switchToECommonsId + ": no such user";
        log.info(msg);
        throw new LoginException(msg);
      } 
      else if (!(switchToUser instanceof ScreeningRoomUser)) {
        String msg = "switching to non-screening room user " + switchToUser + " is forbidden";
        log.info(msg);
        throw new LoginException(msg);
      }
      else {
        log.info("switching to screening room user " + switchToUser); 
        _user = switchToUser;
      }
    }
    return _user;
  }

  /**
   * This method is called if the LoginContext's overall authentication
   * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
   * LoginModules succeeded).
   * <p>
   * If this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the <code>login</code> method),
   * then this method associates the user and role <code>Principal</code>s
   * with the <code>Subject</code> located in the <code>LoginModule</code>.
   * If this LoginModule's own authentication attempted failed, then this method
   * removes any state that was originally saved.
   * 
   * @throws LoginException if the commit fails.
   * @return true if this LoginModule's own login and commit attempts succeeded,
   *         or false otherwise.
   */
  public boolean commit() throws LoginException {
    log.debug("commit()");

    if (!_isAuthenticated) {
      reset(true);
      return false;
    } 
    else {
      
      // add Principals (authenticated identities) to the Subject
      _grantedPrincipals = new ArrayList<Principal>();
      _grantedPrincipals.add(new ScreensaverUserPrincipal(_user));
      _grantedPrincipals.addAll(_user.getScreensaverUserRoles());
      _subject.getPrincipals().addAll(_grantedPrincipals);
      log.debug("authorized Subject with these Principals: " + _grantedPrincipals);
      
      // in any case, clean out state
      reset(false);
      
      _commitSucceeded = true;
      return true;
    }
  }

  private ScreensaverUser findUserByLoginId(String username) {
    return findUser(username, "loginId");
  }

  private ScreensaverUser findUserByECommonsId(String username) {
    String normalizedUsername = username.toLowerCase();
    if (!normalizedUsername.equals(username)) {
      log.warn("lowercasing eCommons ID '" + username + "' to '" + normalizedUsername + "'");
    }
    return findUser(normalizedUsername, "ECommonsId");
  }
      
    /**
   * @param userId the userId
   * @param userIdField the field in {@link ScreensaverUser} entity bean:
   *          "loginId" or "eCommonsId" to lookup the userId in
   * @return a {@link ScreensaverUser} or <code>null</code> if no user found
   *         with userId for given userIdField
   */
  private ScreensaverUser findUser(final String userId, final String userIdField)
  {
    ScreensaverUser user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                                     userIdField,
                                                     userId,
                                                     true,
                                                     "screensaverUserRoles");
    if (user != null) {
      log.debug("found user '" + userId + "' in database using field '" + userIdField +"'" );
    }
    else {
      log.debug("no such user '" + userId + "' in database using field '" + userIdField +"'" );
    }
    return user;
  }

  /**
   * This method is called if the LoginContext's overall authentication failed.
   * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did
   * not succeed).
   * <p>
   * If this LoginModule's own authentication attempt succeeded (checked by
   * retrieving the private state saved by the <code>login</code> and
   * <code>commit</code> methods), then this method cleans up any state that
   * was originally saved.
   * 
   * @throws LoginException if the abort fails.
   * @return false if this LoginModule's own login and/or commit attempts
   *         failed, and true otherwise.
   */
  public boolean abort() throws LoginException {
    log.debug("abort()");
    
    if (!_isAuthenticated) {
      return false;
    } 
    else if (_isAuthenticated && _commitSucceeded == false) {
      // login succeeded but overall authentication failed
      reset(true);
    } 
    else {
      // overall authentication succeeded and commit succeeded,
      // but someone else's commit failed
      logout();
    }
    return true;
  }
  
  /**
   * Logout the user.
   * <p>
   * This method removes the <code>Principal</code>s that were added to the
   * <code>Subject</code> by the <code>commit</code> method.
   * 
   * @throws LoginException if the logout fails.
   * @return true in all cases since this <code>LoginModule</code> should not
   *         be ignored.
   */
  public boolean logout() throws LoginException 
  {
    log.debug("logout()");

    // remove the Principals from the Subject (i.e., authorizations begone!)
    if (_grantedPrincipals != null) {
      _subject.getPrincipals().removeAll(_grantedPrincipals);
    }
    reset(true);
    return true;
  }
  
  
  // private methods
  
  /**
   * Reset internal state.
   * @param alsoResetPrincipals resets the list of Principals that were granted to the subject
   */
  private void reset(boolean alsoResetPrincipals)
  {
    // note: _subject must only be modified in initialize(); it can be inspected after logout, etc.
    _authenticationResult = null;
    _isAuthenticated = false;
    if (alsoResetPrincipals) {
      _grantedPrincipals = null;
    }
  }
}
