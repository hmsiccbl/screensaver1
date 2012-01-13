// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import java.io.Serializable;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public class ScreensaverUserConverter extends AbstractEntityConverter<ScreensaverUser>
{
  public ScreensaverUserConverter(GenericEntityDAO dao)
  {
    super(ScreensaverUser.class, dao);
  }

  @Override
  protected Serializable parseEntityId(String entityIdStr)
  {
    return Integer.valueOf(entityIdStr);
  }
}
