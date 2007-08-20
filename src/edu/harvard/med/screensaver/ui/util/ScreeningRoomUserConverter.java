// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * Converts a ScreeningRoomUser between its entity object and its entity ID (as a String object).
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreeningRoomUserConverter extends AbstractEntityConverter<ScreeningRoomUser>
{
  @Override
  protected Class<ScreeningRoomUser> getEntityClass()
  {
    return ScreeningRoomUser.class;
  }
}
