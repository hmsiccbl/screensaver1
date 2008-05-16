// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 2359 2008-05-09 21:16:57Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;

import org.apache.log4j.Logger;


/**
 * Tests the {@link DAOImpl} in some more complicated ways than
 * {@link GenericEntityDAOTest}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class UsersDAOTest extends AbstractSpringPersistenceTest
{

  private static final Logger log = Logger.getLogger(UsersDAOTest.class);

  protected UsersDAO usersDao;
 
  public void testFindLabHeads()
  {
    final Collection<ScreeningRoomUser> expectedLabHeads =
      new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreeningRoomUser user1 = new ScreeningRoomUser (
          "first1",
          "last1",
          "email1@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(user1);
        ScreeningRoomUser user2 = new ScreeningRoomUser (
          "first2",
          "last2",
          "email2@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(user2);
        ScreeningRoomUser user3 = new ScreeningRoomUser (
          "first3",
          "last3",
          "email3@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(user3);
        ScreeningRoomUser user4 = new ScreeningRoomUser (
          "first4",
          "last4",
          "email4@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(user4);
        ScreeningRoomUser user5 = new ScreeningRoomUser (
          "first5",
          "last5",
          "email5@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(user5);
        ScreeningRoomUser user6 = new ScreeningRoomUser (
          "first6",
          "last6",
          "email6@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(user6);
        user2.setLabHead(user1);
        user3.setLabHead(user1);
        user5.setLabHead(user4);
        expectedLabHeads.add(user1);
        expectedLabHeads.add(user4);
        expectedLabHeads.add(user6);
      }
    });

    Set<ScreeningRoomUser> actualLabHeads = usersDao.findAllLabHeads();
    assertTrue(expectedLabHeads.containsAll(actualLabHeads) && actualLabHeads.containsAll(expectedLabHeads));
  }
}
