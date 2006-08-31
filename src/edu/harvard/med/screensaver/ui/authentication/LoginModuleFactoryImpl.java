//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
//Copyright 2006 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
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
      // set the system property that will tell Tomcat where to find our login.config file
      // TODO: this is a convenient place to perform this, but it's
      // not really appropriate here (in fact it should be specified
      // as a command-line Java system property when Tomcat is
      // started, but us poor developers can't specify additional
      // system properties when starting Tomcat on orchestra.  Sigh.
    System.setProperty("java.security.auth.login.config", 
                        System.getProperty("catalina.base") + "/conf/login.config");
    log.debug("set 'java.security.auth.login.config' system property to " + 
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
