// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/db/SimpleDAOTest.java $
// $Id: SimpleDAOTest.java 466 2006-08-22 19:01:06Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.util.CryptoUtils;

import com.google.common.collect.ImmutableSortedSet;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ScreensaverUserTest extends AbstractSpringPersistenceTest
{
  public void testUserDigestedPassword()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = new ScreeningRoomUser("First", "Last", "first_last@hms.harvard.edu");
        user.setLoginId("myLoginId");
        user.updateScreensaverPassword("myPassword");
        genericEntityDao.saveOrUpdateEntity(user);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "loginId", "myLoginId");
        assertNotNull(user);
        assertEquals(CryptoUtils.digest("myPassword"),
                     user.getDigestedPassword());
      }
    });
  }
  
  public void testAddRoleToUser()
  {
    final String userEmail1 = "first_last1@hms.harvard.edu";
    final String userEmail2 = "first_last2@hms.harvard.edu";

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = new ScreeningRoomUser("First1", "Last1", userEmail1);
        user1.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        genericEntityDao.saveOrUpdateEntity(user1);
        ScreensaverUser user2 = new ScreeningRoomUser("First2", "Last2", userEmail2);
        user2.addScreensaverUserRole(ScreensaverUserRole.SCREENSAVER_USER);
        genericEntityDao.saveOrUpdateEntity(user2);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "email", userEmail1);
        assertEquals(ImmutableSortedSet.of(ScreensaverUserRole.SCREENSAVER_USER), user1.getScreensaverUserRoles());
        
        ScreensaverUser user2 = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "email", userEmail2);
        assertEquals(ImmutableSortedSet.of(ScreensaverUserRole.SCREENSAVER_USER), user2.getScreensaverUserRoles());
        assertEquals(user1.getScreensaverUserRoles(), user2.getScreensaverUserRoles());
      }
    });
  }
  
  
  public void testFullName() 
  {
    ScreensaverUser user = new ScreeningRoomUser("First", "Last", null);
    assertEquals("Last, First", user.getFullNameLastFirst());
    assertEquals("First Last", user.getFullNameFirstLast());

    user = new ScreeningRoomUser("First", "", null);
    assertEquals("First", user.getFullNameLastFirst());
    assertEquals("First", user.getFullNameFirstLast());
    user = new ScreeningRoomUser("First", null, null);
    assertEquals("First", user.getFullNameLastFirst());
    assertEquals("First", user.getFullNameFirstLast());

    user = new ScreeningRoomUser("", "Last", null);
    assertEquals("Last", user.getFullNameLastFirst());
    assertEquals("Last", user.getFullNameFirstLast());
    user = new ScreeningRoomUser(null, "Last", null);
    assertEquals("Last", user.getFullNameLastFirst());
    assertEquals("Last", user.getFullNameFirstLast());

    user = new ScreeningRoomUser("", "", null);
    assertEquals("", user.getFullNameLastFirst());
    assertEquals("", user.getFullNameFirstLast());
    user = new ScreeningRoomUser(null, null, null);
    assertEquals("", user.getFullNameLastFirst());
    assertEquals("", user.getFullNameFirstLast());
  }
 
}
