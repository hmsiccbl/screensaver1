// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication.tomcat;

public class LoginModuleFactoryCapsule
{
  private LoginModuleFactory _factory;

  public LoginModuleFactory getLoginModuleFactory() 
  {
    return _factory;
  }
  
  public void setLoginModuleFactory(LoginModuleFactory factory)
  {
    _factory = factory;
  }

}
