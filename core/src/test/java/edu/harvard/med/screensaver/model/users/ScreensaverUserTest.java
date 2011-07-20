// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreensaverUserTest extends AbstractSpringPersistenceTest
{
  public void testAddRoleToUser()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = new ScreeningRoomUser("First1", "Last1");
        user1.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        genericEntityDao.saveOrUpdateEntity(user1);
        ScreensaverUser user2 = new ScreeningRoomUser("First2", "Last2");
        user2.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        genericEntityDao.saveOrUpdateEntity(user2);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "lastName", "Last1");
        assertEquals(ImmutableSortedSet.of(ScreensaverUserRole.SCREENSAVER_USER), user1.getScreensaverUserRoles());
        
        ScreensaverUser user2 = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "lastName", "Last2");
        assertEquals(ImmutableSortedSet.of(ScreensaverUserRole.SCREENSAVER_USER), user2.getScreensaverUserRoles());
        assertEquals(user1.getScreensaverUserRoles(), user2.getScreensaverUserRoles());
      }
    });
  }
  
  public void testFullName() 
  {
    ScreensaverUser user = new ScreeningRoomUser("First", "Last");
    assertEquals("Last, First", user.getFullNameLastFirst());
    assertEquals("First Last", user.getFullNameFirstLast());

    user = new ScreeningRoomUser("First", "");
    assertEquals("First", user.getFullNameLastFirst());
    assertEquals("First", user.getFullNameFirstLast());
    user = new ScreeningRoomUser("First", null);
    assertEquals("First", user.getFullNameLastFirst());
    assertEquals("First", user.getFullNameFirstLast());

    user = new ScreeningRoomUser("", "Last");
    assertEquals("Last", user.getFullNameLastFirst());
    assertEquals("Last", user.getFullNameFirstLast());
    user = new ScreeningRoomUser(null, "Last");
    assertEquals("Last", user.getFullNameLastFirst());
    assertEquals("Last", user.getFullNameFirstLast());

    user = new ScreeningRoomUser("", "");
    assertEquals("", user.getFullNameLastFirst());
    assertEquals("", user.getFullNameFirstLast());
    user = new ScreeningRoomUser(null, null);
    assertEquals("", user.getFullNameLastFirst());
    assertEquals("", user.getFullNameFirstLast());
  }
  
  public void testPrimaryRoleUpdates()
  {
    ScreensaverUser user = new ScreeningRoomUser("First", "Last");
    user.addScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    user.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
    user.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENS); // note: implies Screener role
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES, 
                                 ScreensaverUserRole.SCREENSAVER_USER, 
                                 ScreensaverUserRole.RNAI_SCREENS),
                 user.getPrimaryScreensaverUserRoles());
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS,
                                 ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES,
                                 ScreensaverUserRole.RNAI_SCREENS,
                                 ScreensaverUserRole.SCREENSAVER_USER), 
                 user.getScreensaverUserRoles());
    try {
      user.removeScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
      fail("expected DataModelViolationException when removing non-primary role");
    }
    catch (DataModelViolationException e) {}
    assertFalse(user.removeScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL1_MUTUAL_SCREENS));
    user.removeScreensaverUserRole(ScreensaverUserRole.SM_DSL_LEVEL2_MUTUAL_POSITIVES);
    assertEquals(Sets.newHashSet(ScreensaverUserRole.SCREENSAVER_USER, 
                                 ScreensaverUserRole.RNAI_SCREENS), 
                                 user.getScreensaverUserRoles());
  }
  
}
