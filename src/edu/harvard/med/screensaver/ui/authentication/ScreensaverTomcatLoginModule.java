// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

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
  // It sure would be nice to place these paramaters in a configuration file,
  // like, say, a Spring config file, but then we'd have a bootstrapping
  // problem, now wouldn't we? :)
  private static final String LOGIN_MODULE_BEAN_NAME = "screensaverLoginModule";
  private static final String APPLICATION_CONTEXT_BEAN_NAME = "edu.harvard.med.screensaver.Screensaver";
  private static final String META_SPRING_CONTEXT_FILE = "classpath:meta-spring-context.xml";
  
  private LoginModule _delegate;

  public void initialize(
    Subject subject,
    CallbackHandler callbackHandler,
    Map sharedState,
    Map options)
  {
    // Use a Spring-endorsed hack to obtain a Spring-managed LoginModule, since
    // this class cannot be Spring managed.
    BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance(META_SPRING_CONTEXT_FILE);
    BeanFactoryReference bf = bfl.useBeanFactory(APPLICATION_CONTEXT_BEAN_NAME);
    _delegate = (LoginModule) bf.getFactory().getBean(LOGIN_MODULE_BEAN_NAME);
  }

  public boolean login() throws LoginException
  {
    return _delegate.login();
  }

  public boolean commit() throws LoginException
  {
    return _delegate.commit();
  }

  public boolean abort() throws LoginException
  {
    return _delegate.abort();
  }

  public boolean logout() throws LoginException
  {
    return _delegate.logout();
  }

}
