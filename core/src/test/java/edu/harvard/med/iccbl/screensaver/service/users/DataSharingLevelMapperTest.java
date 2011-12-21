// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.users;

import junit.framework.TestCase;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

public class DataSharingLevelMapperTest extends TestCase
{
  public void testDataSharingLevelMapper()
  {
    doTest(null, ScreenDataSharingLevel.PRIVATE, null, ScreenDataSharingLevel.PRIVATE);

    doTest(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreenDataSharingLevel.PRIVATE, null, ScreenDataSharingLevel.PRIVATE);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_POSITIVES, null, ScreenDataSharingLevel.PRIVATE);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS, null, ScreenDataSharingLevel.PRIVATE);

    doTest(null, ScreenDataSharingLevel.PRIVATE, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS, ScreenDataSharingLevel.PRIVATE);
    doTest(null, ScreenDataSharingLevel.PRIVATE, ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_POSITIVES);
    doTest(null, ScreenDataSharingLevel.PRIVATE, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS);

    doTest(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreenDataSharingLevel.PRIVATE, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS, ScreenDataSharingLevel.PRIVATE);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_POSITIVES);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS);

    doTest(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreenDataSharingLevel.PRIVATE, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_POSITIVES, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS);
    
    doTest(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS, ScreenDataSharingLevel.PRIVATE);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES, ScreenDataSharingLevel.MUTUAL_POSITIVES);
    doTest(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS, ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, ScreenDataSharingLevel.MUTUAL_SCREENS);
  }

  private void doTest(ScreensaverUserRole smDslRole, ScreenDataSharingLevel expectedSmScreenDsl,
                      ScreensaverUserRole rnaiDslRole, ScreenDataSharingLevel expectedRnaiScreenDsl)
  {
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "Screener");
    assert user.getScreensaverUserRoles().isEmpty();
    if (smDslRole != null) {
      user.addScreensaverUserRole(smDslRole);
    }
    if (rnaiDslRole != null) {
      user.addScreensaverUserRole(rnaiDslRole);
    }
    assertEquals(expectedSmScreenDsl, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, user));
    assertEquals(expectedRnaiScreenDsl, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.RNAI, user));
  }

  public void testUserDataSharingLevelForScreenTypeAndLevel()
  {
    assertEquals(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(ScreenType.SMALL_MOLECULE, 1));
    assertEquals(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(ScreenType.SMALL_MOLECULE, 2));
    assertEquals(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(ScreenType.SMALL_MOLECULE, 3));
    assertEquals(ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(ScreenType.RNAI, 1));
    assertEquals(ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES, DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(ScreenType.RNAI, 2));
    assertEquals(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS, DataSharingLevelMapper.getUserDslRoleForScreenTypeAndLevel(ScreenType.RNAI, 3));
  }

  public void testPrimaryDataSharingLevelRoleForUser()
  {
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "Screener");
    assertNull(DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.SMALL_MOLECULE, user));
    assertNull(DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.RNAI, user));
    user.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    user.addScreensaverUserRole(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS);
    assertEquals(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.SMALL_MOLECULE, user));
    assertEquals(ScreensaverUserRole.RNAI_DSL_LEVEL3_SHARED_SCREENS, DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.RNAI, user));
    user.addScreensaverUserRole(ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS);
    assertEquals(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS, DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.SMALL_MOLECULE, user));
    assertEquals(ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.RNAI, user));
    user.removeScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    user.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    assertEquals(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.SMALL_MOLECULE, user));
    assertEquals(ScreensaverUserRole.RNAI_DSL_LEVEL1_MUTUAL_SCREENS, DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(ScreenType.RNAI, user));
  }
}
