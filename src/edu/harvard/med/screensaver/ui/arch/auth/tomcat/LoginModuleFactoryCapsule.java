// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.auth.tomcat;

import org.apache.log4j.Logger;

/**
 *
 * @motivation Tomcat JNDI directory is read-only, but we need to add
 * our LoginModuleFactory to the directory, so we need an object--that
 * Tomcat will add for us--that can be used to hold our
 * LoginModuleFactory, after the LoginModuleFactory has been
 * instantiated and initialized by Spring.  Sigh.
 */
public class LoginModuleFactoryCapsule
{
  private static Logger log = Logger.getLogger(LoginModuleFactoryCapsule.class);
  
  private LoginModuleFactory _factory;
  
  public LoginModuleFactoryCapsule()
  {
    log.debug("instantiated a LoginModuleFactoryCapsule: " + this);
  }

  public LoginModuleFactory getLoginModuleFactory() 
  {
    return _factory;
  }
  
  public void setLoginModuleFactory(LoginModuleFactory factory)
  {
    _factory = factory;
  }

}
