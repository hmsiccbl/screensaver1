// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.security.auth.spi.LoginModule;

import edu.harvard.med.screensaver.ui.authentication.tomcat.LoginModuleFactory;
import edu.harvard.med.screensaver.ui.authentication.tomcat.LoginModuleFactoryCapsule;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LoginModuleFactoryImpl implements LoginModuleFactory, ApplicationContextAware
{
  private static final Logger log = Logger.getLogger(LoginModuleFactoryImpl.class);

  private ApplicationContext _appCtx;
  
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
  {
    _appCtx = applicationContext;
  }
  
  public void initialize()
  {
    log.debug("'java.security.auth.login.config' system property = " + 
              System.getProperty("java.security.auth.login.config"));

    // place ourselves into the JNDI directory, using our
    // LoginModuleFactoryCapsule hack, which allows us to get around a
    // read-only JNDI directory
    try {
      Context initialCtx = new InitialContext();
      Context envCtx = (Context) initialCtx.lookup("java:comp/env");
      if (log.isDebugEnabled()) {
        log.debug("acquired JNDI env context");
        NamingEnumeration<NameClassPair> name = envCtx.list("bean");
        while (name.hasMore()) {
          log.debug(name.next().toString());
        }
      }
      LoginModuleFactoryCapsule factoryHolder = (LoginModuleFactoryCapsule) envCtx.lookup("bean/loginModuleFactoryCapsule");
      factoryHolder.setLoginModuleFactory(this);
      log.debug("LoginModuleFactoryImpl bound to JNDI directory");
    }
    catch (NoInitialContextException e) {
      log.warn("JNDI server not available, so LoginModuleFactory willl not be made available: " + e.getMessage());
    }
    catch (NamingException e) {
      log.error("could not set LoginModuleFactory in JNDI server: " + e.getMessage());
      e.printStackTrace();
    }
    
  }
  
  public LoginModule newLoginModule()
  {
    return (LoginModule) _appCtx.getBean("screensaverLoginModule");
  }
}
