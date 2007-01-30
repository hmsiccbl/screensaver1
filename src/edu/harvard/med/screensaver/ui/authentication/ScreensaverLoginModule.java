//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
//Copyright 2006 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.


/*
 * Derived from Sun's SampleLoginModule.java 1.18 00/01/11
 *
 * Copyright 2000-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 * notice, this  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */

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
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.util.CryptoUtils;

import org.apache.log4j.Logger;
    

/**
 * This LoginModule authenticates users via an injected AuthenticationClient
 * service.
 * <p>
 * If user is successfully authenticated, a set <code>Principal</code>s will
 * added to the login Subject, which will be subsequently removed when the user
 * is logged out.  The Principals are obtained from a database, via a DAO object.
 */
public class ScreensaverLoginModule implements LoginModule
{
  
  private static final Logger log = Logger.getLogger(ScreensaverLoginModule.class);

  private static final String NO_SUCH_USER = "No such user";

  private static final String FOUND_SCREENSAVER_USER = "Found Screensaver user";

  private static final String FOUND_ECOMMONS_USER = "Found eCommons user";
  
  private DAO _dao;
  private AuthenticationClient _authenticationClient;

  // initial state
  private Subject _subject;
  private CallbackHandler _callbackHandler;
  private Map _sharedState;
  private Map _options;
  
  // the authentication status
  private AuthenticationResult _authenticationResult;
  private boolean _isAuthenticated = false;
  private boolean _commitSucceeded = false;
  
  // username and password
  private String _username;
  private char[] _password;
  
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

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }


  // LoginModule interface methods

  /**
   * Initialize this <code>LoginModule</code>.
   * 
   * @param _subject the <code>Subject</code> to be authenticated.
   * @param _callbackHandler a <code>CallbackHandler</code> for communicating
   *          with the end user (prompting for user names and passwords, for
   *          example).
   * @param _sharedState shared <code>LoginModule</code> state.
   * @param _options _options specified in the login
   *          <code>Configuration</pcode> for this particular
   *      <code>LoginModule</code>.
   */
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
    
    // prompt for a user name and _password
    if (_callbackHandler == null)
      throw new LoginException("Error: no CallbackHandler available " +
      "to garner authentication information from the user");
    
    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("user name: ");
    callbacks[1] = new PasswordCallback("password: ", false);
    
    try {
      _callbackHandler.handle(callbacks);
      _username = ((NameCallback) callbacks[0]).getName();
      char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
      // treat a NULL password as an empty password
      if (tmpPassword == null) {
        tmpPassword = new char[0];
      }
      _password = new char[tmpPassword.length];
      System.arraycopy(tmpPassword, 0,
                       _password, 0, tmpPassword.length);
      ((PasswordCallback)callbacks[1]).clearPassword();
      
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException("Error: " + uce.getCallback().toString() + 
                               " not available to garner authentication information from the user");
    }
    
    log.debug("attempting authentication for user '" + _username + "'");
    //log.debug("user entered _password: " + new String(_password));
    
    // verify the username/password
    try {
      _user = findUser(_username, "loginId");
      if (_user != null) {
        log.info(FOUND_SCREENSAVER_USER + " '" + _username + "'");
        if (_user.getDigestedPassword().equals(CryptoUtils.digest(_password))) {
          _isAuthenticated = true;
          _authenticationResult = new SimpleAuthenticationResult(_username,
                                                                 new String(_password), 
                                                                 true,
                                                                 1,
                                                                 "success",
                                                                 "user authenticated with native Screensaver account");
        } 
        else {
          _isAuthenticated = false;
          _authenticationResult = new SimpleAuthenticationResult(_username,
                                                                 new String(_password), 
                                                                 _isAuthenticated,
                                                                 0,
                                                                 "failure",
                                                                 "user authentication failed for native Screensaver account");
        }
      }
      else {
        String normalizedUsername = _username.toLowerCase();
        if (!normalizedUsername.equals(_username)) {
          log.warn("lowercasing eCommons ID '" + _username + " to " + normalizedUsername);
        }
        _user = findUser(normalizedUsername, "ECommonsId");
        if (_user != null) {
          log.info(FOUND_ECOMMONS_USER + " '" + normalizedUsername + "'");
          _authenticationResult = _authenticationClient.authenticate(new Credentials(normalizedUsername,
                                                                                     new String(_password)));
          _isAuthenticated = _authenticationResult.isAuthenticated();
        }
        else {
          String message = NO_SUCH_USER + " '" + normalizedUsername + "'";
          log.info(message);
          throw new FailedLoginException(message);
        }
      }

      if (_isAuthenticated) {
        log.info("authentication succeeded for user '" + _username + 
                 "' with status code " + _authenticationResult.getStatusCode() + 
                 " (" + _authenticationResult.getStatusCodeCategory() + ")");
        return true;
      } 
      else {
        // authentication failed, clean out state
        log.info("authentication failed for user '" + _username + 
                 "' with status code " + _authenticationResult.getStatusCode() + 
                 " (" + _authenticationResult.getStatusCodeCategory() + ")");
        String statusMessage = _authenticationResult.getStatusMessage();
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

  /**
   * @param userId the userId
   * @param userIdField the field in {@link ScreensaverUser} entity bean:
   *          "loginId" or "eCommonsId" to lookup the userId in
   * @return a {@link ScreensaverUser} or <code>null</code> if no user found
   *         with userId for given userIdField
   */
  private ScreensaverUser findUser(String userId, String userIdField)
  {
    ScreensaverUser user = _dao.findEntityByProperty(ScreensaverUser.class, 
                                                     userIdField,
                                                     userId);
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
    _username = null;
    if (_password != null) {
      for (int i = 0; i < _password.length; i++) {
        _password[i] = ' ';
      }
    }
    _password = null;
    if (alsoResetPrincipals) {
      _grantedPrincipals = null;
    }
  }

}
