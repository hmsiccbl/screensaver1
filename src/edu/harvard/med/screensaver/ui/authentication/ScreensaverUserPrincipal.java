// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import java.security.Principal;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;

/**
 * @motivation too problematic to have ScreensaverUser implement Principal
 *             interface (automated entity bean tests fail, due to
 *             getter-without-setter error), so creating a standalone Principal
 *             class for ScreensaverUser objects.
 */
public class ScreensaverUserPrincipal implements Principal
{
  private String _name;
  
  public ScreensaverUserPrincipal(ScreensaverUser user)
  {
    _name = user.getEmail();
  }
  
  public String getName()
  {
    return _name;
  }
  
  @Override
  public boolean equals(Object other)
  {
    if (!(other instanceof ScreensaverUserPrincipal)) {
      return false;
    }
    return _name.equals(((ScreensaverUserPrincipal) other)._name);
  }
  
  @Override
  public int hashCode()
  {
    return _name.hashCode();
  }
  
  @Override
  public String toString()
  {
    return _name;
  }

}
