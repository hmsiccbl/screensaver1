// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.util.Collections;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

public class ScreensaverUserRoleTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(ScreensaverUserRoleTest.class);

  public void testImpliedRoles()
  {
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SCREENS_ADMIN,
                                 ScreensaverUserRole.READ_EVERYTHING_ADMIN),
                                 ScreensaverUserRole.BILLING_ADMIN.getImpliedRoles());
    assertEquals(Sets.newHashSet(ScreensaverUserRole.USERS_ADMIN,
                                 ScreensaverUserRole.USER_CHECKLIST_ITEMS_ADMIN,
                                 ScreensaverUserRole.READ_EVERYTHING_ADMIN),
                                 ScreensaverUserRole.LAB_HEADS_ADMIN.getImpliedRoles());
    assertEquals(Sets.newHashSet(ScreensaverUserRole.READ_EVERYTHING_ADMIN),
                 ScreensaverUserRole.DEVELOPER.getImpliedRoles());
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS, ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES),
                 ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS.getImpliedRoles());
    assertEquals(Collections.emptySet(),
                 ScreensaverUserRole.SCREENSAVER_USER.getImpliedRoles());
  }
  
  public void testIsAdministrative()
  {
    assertFalse(ScreensaverUserRole.SCREENSAVER_USER.isAdministrative());
    assertTrue(ScreensaverUserRole.DEVELOPER.isAdministrative());
    assertTrue(ScreensaverUserRole.READ_EVERYTHING_ADMIN.isAdministrative());
    assertTrue(ScreensaverUserRole.SCREENS_ADMIN.isAdministrative());
    assertTrue(ScreensaverUserRole.BILLING_ADMIN.isAdministrative());
    assertFalse(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS.isAdministrative());
  }
  
  public void testDisplayableRoleName()
  {
    assertEquals("Screensaver User Login", ScreensaverUserRole.SCREENSAVER_USER.getDisplayableRoleName());
  }

}
