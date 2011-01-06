// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserComparator;

import org.apache.log4j.Logger;

public class UsersDAO extends AbstractDAO
{
  // static members

  private static Logger log = Logger.getLogger(UsersDAO.class);


  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public UsersDAO()
  {
  }

  @SuppressWarnings("unchecked")
  public List<String> findDeveloperECommonsIds()
  {
    return new ArrayList<String>(getHibernateTemplate().find(
      "select ECommonsId from ScreensaverUser where ECommonsId != null and 'developer' in elements(screensaverUserRoles)"));
  }


  /**
   * Find all the screening room users that are lab heads.
   * @return a List of {@link ScreeningRoomUser}s.
   */
  @SuppressWarnings("unchecked")
  public SortedSet<LabHead> findAllLabHeads()
  {
    // note: we perform sorting via a TreeSet, rather than asking persistence
    // layer to do sorting, as this keeps sorting order policy in
    // ScreensaverUserComparator, and also keeps our query simpler. Also, the
    // SortedSet return type makes return value more explicit
    String hql =
      "select distinct lh from LabHead " +
      "lh left outer join lh.labHead " +
      "left outer join fetch lh.labAffiliation";
    SortedSet labHeads = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    labHeads.addAll((List<ScreeningRoomUser>) getHibernateTemplate().find(hql));
    return labHeads;
  }

  // private methods

}

