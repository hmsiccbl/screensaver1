// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication.tomcat;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;

/**
 * A JAAS LoginModule that delegates its behavior to another LoginModule, that
 * is found via JNDI under
 * <code>java:comp/env/bean/loginMOduleFactoryCapsule</code>.
 * 
 * @motivation The LoginModule class that Tomcat is configured to use (via
 *             login.conf) will be instantiated by Tomcat, and therefore cannot
 *             be initialized in any way by the web application (e.g. it cannot
 *             be a Spring-managed bean). To allow use of a LoginModule that
 *             <i>is</i> initialized by a web application, we use delegation,
 *             and receive the delegate via JNDI. This is a hack, of course.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreensaverTomcatLoginModule implements LoginModule
{
  private static Logger log = Logger.getLogger(ScreensaverTomcatLoginModule.class);
  
  private LoginModule _delegate;

  @SuppressWarnings("unchecked")
  public void initialize(
    Subject subject,
    CallbackHandler callbackHandler,
    Map sharedState,
    Map options)
  {
    log.debug("initialize()");

    Context initialCtx;
    try {
      initialCtx = new InitialContext();
      Context envCtx = (Context) initialCtx.lookup("java:comp/env");
      LoginModuleFactoryCapsule loginModuleFactoryCapsule = (LoginModuleFactoryCapsule) envCtx.lookup("bean/loginModuleFactoryCapsule");
      LoginModuleFactory loginModuleFactory = loginModuleFactoryCapsule.getLoginModuleFactory();
      _delegate = loginModuleFactory.newLoginModule();
      log.debug("obtained reference to LoginModule delegate via JNDI: " + _delegate);
    }
    catch (NamingException e) {
      e.printStackTrace();
      return;
    }
    
    _delegate.initialize(subject,
                         callbackHandler,
                         sharedState,
                         options);
  }

  public boolean login() throws LoginException
  {
    log.debug("login()");
    return _delegate.login();
  }

  public boolean commit() throws LoginException
  {
    log.debug("commit()");
    return _delegate.commit();
  }

  public boolean abort() throws LoginException
  {
    log.debug("abort()");
    return _delegate.abort();
  }

  public boolean logout() throws LoginException
  {
    log.debug("logout()");
    return _delegate.logout();
  }

}
