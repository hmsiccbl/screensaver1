// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class ScreensaverUserRoleTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(ScreensaverUserRoleTest.class);

  public void testImpliedRoles()
  {
    assertEquals(Arrays.asList(ScreensaverUserRole.SCREENS_ADMIN,
                               ScreensaverUserRole.READ_EVERYTHING_ADMIN),
                 ScreensaverUserRole.BILLING_ADMIN.getImpliedRoles());
    assertEquals(Arrays.asList(ScreensaverUserRole.READ_EVERYTHING_ADMIN),
                 ScreensaverUserRole.DEVELOPER.getImpliedRoles());
    assertEquals(Arrays.asList(ScreensaverUserRole.SCREENER),
                 ScreensaverUserRole.RNAI_SCREENER.getImpliedRoles());
    assertEquals(Arrays.asList(ScreensaverUserRole.SCREENER),
                 ScreensaverUserRole.SMALL_MOLECULE_SCREENER.getImpliedRoles());
    assertEquals(Collections.emptyList(),
                 ScreensaverUserRole.SCREENSAVER_USER.getImpliedRoles());
  }
  
  public void testIsAdministrative()
  {
    assertFalse(ScreensaverUserRole.SCREENSAVER_USER.isAdministrative());
    assertTrue(ScreensaverUserRole.DEVELOPER.isAdministrative());
    assertTrue(ScreensaverUserRole.READ_EVERYTHING_ADMIN.isAdministrative());
    assertTrue(ScreensaverUserRole.SCREENS_ADMIN.isAdministrative());
    assertTrue(ScreensaverUserRole.BILLING_ADMIN.isAdministrative());
    assertFalse(ScreensaverUserRole.SCREENER.isAdministrative());
    assertFalse(ScreensaverUserRole.SMALL_MOLECULE_SCREENER.isAdministrative());
  }
  
  public void testDisplayableRoleName()
  {
    assertEquals("Small Molecule Screener", ScreensaverUserRole.SMALL_MOLECULE_SCREENER.getDisplayableRoleName());
    assertEquals("RNAi Screener", ScreensaverUserRole.RNAI_SCREENER.getDisplayableRoleName());
  }

}
