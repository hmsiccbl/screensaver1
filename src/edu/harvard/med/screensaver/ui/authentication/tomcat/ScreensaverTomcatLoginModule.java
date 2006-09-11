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
 * A JAAS LoginModule that can be instantiated by Tomcat's JAASRealm, via its
 * default constructor. This class delegates its behavior to a Spring-managed
 * LoginModule, which has been injected with its required dependencies. This
 * class constitutes a Spring-endorsed hack, allowing us to delegate to a
 * Spring-managed bean, since this class cannot be Spring-managed, as it is
 * instantiated directly by Tomcat.  Sigh.
 * 
 * @motivation Classes that are instantiated by third-party code cannot be
 *             Spring-managed beans. See <a
 *             href="http://www.springframework.org/docs/api/org/springframework/beans/factory/access/SingletonBeanFactoryLocator.html">SingletonBeanFactoryLoader</a>.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreensaverTomcatLoginModule implements LoginModule
{
  private static Logger log = Logger.getLogger(ScreensaverTomcatLoginModule.class);
  
  // It sure would be nice to place these parameters in a configuration file,
  // like, say, a Spring config file, but then we'd have a bootstrapping
  // problem, now wouldn't we? :)
  private static final String LOGIN_MODULE_BEAN_NAME = "screensaverLoginModule";
  private static final String APPLICATION_CONTEXT_BEAN_NAME = "edu.harvard.med.screensaver.Screensaver";
  
  private LoginModule _delegate;

  @SuppressWarnings("unchecked")
  public void initialize(
    Subject subject,
    CallbackHandler callbackHandler,
    Map sharedState,
    Map options)
  {
    log.debug("initialize()");
    // Use a Spring-endorsed hack to obtain a Spring-managed LoginModule, since
    // this class cannot be Spring managed.
    // TODO: this does not work as expected: we are not getting the same
    // instance of the login module bean as is in the ApplicationContext created
    // by the ServletListener.
//    System.out.println("ClassPathXmlApplicationContext is-a BeanFactory: " + BeanFactory.class.isAssignableFrom(ClassPathXmlApplicationContext.class));
//    BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance(META_SPRING_CONTEXT_FILE);
//    BeanFactoryReference bf = bfl.useBeanFactory(APPLICATION_CONTEXT_BEAN_NAME);
//    _delegate = (LoginModule) bf.getFactory().getBean(LOGIN_MODULE_BEAN_NAME);

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
