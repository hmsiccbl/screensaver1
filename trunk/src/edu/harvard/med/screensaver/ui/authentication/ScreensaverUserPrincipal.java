// $HeadURL$
// $Id$
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
  private ScreensaverUser _screensaverUser;
  
  public ScreensaverUserPrincipal(ScreensaverUser user)
  {
    _screensaverUser = user;
  }
  
  public ScreensaverUser getScreensaverUser()
  {
    return _screensaverUser;
  }
  
  public String getName()
  {
    // return a unique, non-null identifier, which happens to be "email" for our ScreensaverUser entity
    return _screensaverUser.getEmail();
  }
  
  @Override
  public boolean equals(Object other)
  {
    if (!(other instanceof ScreensaverUserPrincipal)) {
      return false;
    }
    return getName().equals(((ScreensaverUserPrincipal) other).getName());
  }
  
  @Override
  public int hashCode()
  {
    return getName().hashCode();
  }
  
  @Override
  public String toString()
  {
    return getName();
  }

}
