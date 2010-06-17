// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;


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
        ScreeningRoomUser user1 = new LabHead(
          "first1",
          "last1",
          new LabAffiliation("organization1", AffiliationCategory.HMS));
        genericEntityDao.saveOrUpdateEntity(user1);
        ScreeningRoomUser user2 = new ScreeningRoomUser(
          "first2",
          "last2");
        genericEntityDao.saveOrUpdateEntity(user2);
        ScreeningRoomUser user3 = new ScreeningRoomUser(
          "first3",
          "last3");
        genericEntityDao.saveOrUpdateEntity(user3);
        ScreeningRoomUser user4 = new ScreeningRoomUser(
          "first4",
          "last4");
        genericEntityDao.saveOrUpdateEntity(user4);
        ScreeningRoomUser user5 = new LabHead(
          "first5",
          "last5",
          new LabAffiliation("organization3", AffiliationCategory.HMS));
        genericEntityDao.saveOrUpdateEntity(user5);
        user2.setLab(user1.getLab());
        user3.setLab(user1.getLab());
        expectedLabHeads.add(user1);
        expectedLabHeads.add(user5);
      }
    });

    Set<LabHead> actualLabHeads = usersDao.findAllLabHeads();
    assertEquals(expectedLabHeads, actualLabHeads);
  }
}
