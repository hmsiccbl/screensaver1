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
import edu.harvard.med.screensaver.util.CryptoUtils;


/**
 * Tests the {@link DAOImpl} in some simple, straightfoward ways.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UserDigestedPasswordTest extends AbstractSpringTest
{
  
  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(UserDigestedPasswordTest.class);
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
}
