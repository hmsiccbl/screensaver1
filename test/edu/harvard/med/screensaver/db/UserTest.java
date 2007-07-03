// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/db/SimpleDAOTest.java $
// $Id: SimpleDAOTest.java 466 2006-08-22 19:01:06Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Date;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.util.CryptoUtils;


/**
 * Tests the {@link DAOImpl} in some simple, straightfoward ways.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserTest extends AbstractSpringTest
{
  
  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(UserTest.class);
  }

  
  // protected instance fields
  
  /**
   * Bean property, for database access via Spring and Hibernate.
   */
  protected GenericEntityDAO genericEntityDao;

  /**
   * For schema-related test setup tasks.
   */
  protected SchemaUtil schemaUtil;

  
  // protected instance methods

  @Override
  protected void onSetUp() throws Exception
  {
    schemaUtil.truncateTablesOrCreateSchema();
  }

  
  // public instance methods
  
  public void testUserDigestedPassword()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = new ScreeningRoomUser(new Date(), "First", "Last", "first_last@hms.harvard.edu", "", "", "", "", "", ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR, false);
        user.setLoginId("myLoginId");
        user.updateScreensaverPassword("myPassword");
        genericEntityDao.persistEntity(user);
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
        ScreensaverUser user1 = new ScreeningRoomUser(new Date(), "First1", "Last1", userEmail1, "", "", "", "", "", ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR, false);
        user1.addScreensaverUserRole(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        genericEntityDao.persistEntity(user1);
        ScreensaverUser user2 = new ScreeningRoomUser(new Date(), "First2", "Last2", userEmail2, "", "", "", "", "", ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR, false);
        user2.addScreensaverUserRole(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        genericEntityDao.persistEntity(user2);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "email", userEmail1);
        assertEquals(user1.getScreensaverUserRoles().size(), 1);
        assertTrue(user1.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER));
        
        ScreensaverUser user2 = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "email", userEmail2);
        assertEquals(user2.getScreensaverUserRoles().size(), 1);
        assertTrue(user2.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER));
        
        assertEquals(user1.getScreensaverUserRoles().iterator().next(),
                     user2.getScreensaverUserRoles().iterator().next());
        
      }
    });
    
  }
}
