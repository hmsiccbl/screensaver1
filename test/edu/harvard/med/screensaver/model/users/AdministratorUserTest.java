// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.DataModelViolationException;

import com.google.common.collect.Sets;

public class AdministratorUserTest extends AbstractEntityInstanceTest<AdministratorUser>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AdministratorUserTest.class, AdministratorUser.class);
  }

  public AdministratorUserTest() throws IntrospectionException
  {
    super(AdministratorUser.class);
  }
  
  public void testRoles() {
    final AdministratorUser user = new AdministratorUser("first", "last", "", "", "", "", "", "");

    user.addScreensaverUserRole(ScreensaverUserRole.SCREENS_ADMIN);
    user.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
    genericEntityDao.saveOrUpdateEntity(user);

    AdministratorUser user2 = genericEntityDao.findEntityById(AdministratorUser.class, user.getEntityId(), false, "screensaverUserRoles");
    assertEquals(Sets.newHashSet(ScreensaverUserRole.READ_EVERYTHING_ADMIN,
                                 ScreensaverUserRole.SCREENS_ADMIN,
                                 ScreensaverUserRole.LIBRARIES_ADMIN),
                 user2.getScreensaverUserRoles());

    try {
      AdministratorUser user3 = genericEntityDao.findEntityById(AdministratorUser.class, user.getEntityId(), false, "screensaverUserRoles");
      user3.addScreensaverUserRole(ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
      fail("expected DataModelViolationException after adding screening room user role to administrator user ");
    }
    catch (Exception e) {
      assertTrue(e instanceof DataModelViolationException);
    }
  }

  
}

