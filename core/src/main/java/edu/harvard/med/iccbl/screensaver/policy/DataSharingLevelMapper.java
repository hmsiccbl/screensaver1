// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.NullSafeUtils;

public class DataSharingLevelMapper
{
  private static final SortedSet<ScreensaverUserRole> smUserDslRoles = ImmutableSortedSet.of(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
  private static final SortedSet<ScreensaverUserRole> rnaiUserDslRoles = ImmutableSortedSet.of(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS);
  public static final Map<ScreenType,SortedSet<ScreensaverUserRole>> UserDslRoles =
    ImmutableMap.of(ScreenType.SMALL_MOLECULE, smUserDslRoles,
                    ScreenType.RNAI, rnaiUserDslRoles);
  
  public static ScreensaverUserRole getPrimaryDataSharingLevelRoleForUser(ScreenType screenType, ScreensaverUser user)
  {
    TreeSet<ScreensaverUserRole> userDslRoles = Sets.newTreeSet(Sets.intersection(user.getScreensaverUserRoles(), UserDslRoles.get(screenType)));
    if (!userDslRoles.isEmpty()) {
      return userDslRoles.last();
    }
    return null;
  }

  public static ScreenDataSharingLevel getScreenDataSharingLevelForUser(ScreenType screenType, ScreensaverUser user)
  {
    return NullSafeUtils.value(getScreenDataSharingLevelForRole(getPrimaryDataSharingLevelRoleForUser(screenType, user)), ScreenDataSharingLevel.PRIVATE);
  }

  public static ScreenDataSharingLevel getScreenDataSharingLevelForRole(ScreensaverUserRole userDslRole)
  {
    if (userDslRole != null) {
      switch (userDslRole) {
        case SM_DSL_LEVEL1_MUTUAL_SCREENS:
        case RNAI_DSL_LEVEL1_MUTUAL_SCREENS:
          return ScreenDataSharingLevel.MUTUAL_SCREENS;
        case SM_DSL_LEVEL2_MUTUAL_POSITIVES:
        case RNAI_DSL_LEVEL2_MUTUAL_POSITIVES:
          return ScreenDataSharingLevel.MUTUAL_POSITIVES;
      }
    }
    return ScreenDataSharingLevel.PRIVATE;
  }

  public static ScreensaverUserRole getUserDslRoleForScreenTypeAndLevel(ScreenType screenType, int level)
  {
    SortedSet<ScreensaverUserRole> roles = UserDslRoles.get(screenType);
    ScreensaverUserRole role = Iterables.get(roles, roles.size() - level);
    assert (role.getRoleName().contains(Integer.toString(level)));
    return role;
  }
}
