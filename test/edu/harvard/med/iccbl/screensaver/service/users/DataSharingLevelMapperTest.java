// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.users;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import junit.framework.TestCase;

public class DataSharingLevelMapperTest extends TestCase
{
  public void testDataSharingLevelMapper()
  {
    ScreeningRoomUser user = new ScreeningRoomUser("Test", "Screener");
    assert user.getScreensaverUserRoles().isEmpty();
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.RNAI, user));
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, user));
    user.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.RNAI, user));
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, user));
    user.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.RNAI, user));
    assertEquals(ScreenDataSharingLevel.MUTUAL_POSITIVES, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, user));
    user.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS);
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.RNAI, user));
    assertEquals(ScreenDataSharingLevel.MUTUAL_SCREENS, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, user));
    user.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENS);
    assertEquals(ScreenDataSharingLevel.PRIVATE, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.RNAI, user));
    assertEquals(ScreenDataSharingLevel.MUTUAL_SCREENS, DataSharingLevelMapper.getScreenDataSharingLevelForUser(ScreenType.SMALL_MOLECULE, user));
  }
}
