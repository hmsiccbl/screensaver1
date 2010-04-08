// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

public class DataSharingLevelMapper
{
  public static final SortedSet<ScreensaverUserRole> UserSmDslRoles =
    ImmutableSortedSet.of(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS,
                          ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES,
                          ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
  
  static public ScreenDataSharingLevel getScreenDataSharingLevelForUser(ScreenType screenType, ScreensaverUser user)
  {
    if (user == null || screenType == null || screenType != ScreenType.SMALL_MOLECULE) {
      return ScreenDataSharingLevel.PRIVATE;
    }
    TreeSet<ScreensaverUserRole> userDslRoles = Sets.newTreeSet(Sets.intersection(user.getScreensaverUserRoles(), UserSmDslRoles));
    if (!userDslRoles.isEmpty()) {
      switch (userDslRoles.last()) {
      case SM_DSL_LEVEL1_MUTUAL_SCREENS: return ScreenDataSharingLevel.MUTUAL_SCREENS;
      case SM_DSL_LEVEL2_MUTUAL_POSITIVES: return ScreenDataSharingLevel.MUTUAL_POSITIVES;
      }
    }
    return ScreenDataSharingLevel.PRIVATE;
  }
}
