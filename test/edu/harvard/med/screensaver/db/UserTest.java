// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/db/SimpleDAOTest.java $
// $Id: SimpleDAOTest.java 466 2006-08-22 19:01:06Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.AbstractSpringTest;
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
  protected DAO dao;

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
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = dao.defineEntity(ScreensaverUser.class, "First", "Last", "first_last@hms.harvard.edu");
        user.setLoginId("myLoginId");
        user.updateScreensaverPassword("myPassword");
      }
    });
    
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = dao.findEntityByProperty(ScreensaverUser.class, "loginId", "myLoginId");
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

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = dao.defineEntity(ScreensaverUser.class, "First1", "Last1", userEmail1);
        user1.addScreensaverUserRole(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
        ScreensaverUser user2 = dao.defineEntity(ScreensaverUser.class, "First2", "Last2", userEmail2);
        user2.addScreensaverUserRole(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
      }
    });
    
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user1 = dao.findEntityByProperty(ScreensaverUser.class, "email", userEmail1);
        assertEquals(user1.getScreensaverUserRoles().size(), 1);
        assertTrue(user1.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER));
        
        ScreensaverUser user2 = dao.findEntityByProperty(ScreensaverUser.class, "email", userEmail2);
        assertEquals(user2.getScreensaverUserRoles().size(), 1);
        assertTrue(user2.getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER));
        
        assertEquals(user1.getScreensaverUserRoles().iterator().next(),
                     user2.getScreensaverUserRoles().iterator().next());
        
      }
    });
    
  }
}
