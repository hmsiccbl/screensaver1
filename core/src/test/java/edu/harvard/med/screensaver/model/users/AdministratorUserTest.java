// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import com.google.common.collect.Sets;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.DataModelViolationException;

public class AdministratorUserTest extends AbstractEntityInstanceTest<AdministratorUser>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AdministratorUserTest.class, AdministratorUser.class);
  }

  public AdministratorUserTest()
  {
    super(AdministratorUser.class);
  }
  
  public void testRoles() {
    final AdministratorUser user = new AdministratorUser("first", "last");

    user.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    user.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(user);

    AdministratorUser user2 = genericEntityDao.findEntityById(AdministratorUser.class, user.getEntityId(), false, ScreensaverUser.roles.castToSubtype(AdministratorUser.class));
    assertEquals(Sets.newHashSet(ScreensaverUserRole.READ_EVERYTHING_ADMIN,
                                 ScreensaverUserRole.SCREENS_ADMIN,
                                 ScreensaverUserRole.LIBRARIES_ADMIN),
                 user2.getScreensaverUserRoles());

    try {
      AdministratorUser user3 = genericEntityDao.findEntityById(AdministratorUser.class, user.getEntityId(), false, ScreensaverUser.roles.castToSubtype(AdministratorUser.class));
      user3.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
      fail("expected DataModelViolationException after adding screening room user role to administrator user ");
    }
    catch (Exception e) {
      assertTrue(e instanceof DataModelViolationException);
    }
  }

  
}

