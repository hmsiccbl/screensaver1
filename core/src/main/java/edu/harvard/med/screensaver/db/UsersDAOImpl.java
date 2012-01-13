// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserComparator;

public class UsersDAOImpl extends AbstractDAO implements UsersDAO
{
  private static Logger log = Logger.getLogger(UsersDAOImpl.class);
  
  GenericEntityDAO _dao;
  
  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public UsersDAOImpl()
  {}
  
  public UsersDAOImpl(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public List<String> findDeveloperECommonsIds()
  {
    return getHibernateSession().createQuery("select ECommonsId from ScreensaverUser where ECommonsId != null and 'developer' in elements(screensaverUserRoles)").list();
  }

  /**
   * Find all the screening room users that are lab heads.
   * @return a List of {@link ScreeningRoomUser}s.
   */
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
    SortedSet<LabHead> labHeads = Sets.newTreeSet(ScreensaverUserComparator.getInstance());;
    labHeads.addAll((List<LabHead>) getHibernateSession().createQuery(hql).list());
    return labHeads;
  }  
  
  public <UT extends ScreeningRoomUser> UT findSRU(Class<UT> userClass,
                                                          String firstName,
                                                          String lastName,
                                                          String email)
  {
    Map<String,Object> props = new HashMap<String,Object>();
    props.put("firstName", firstName);
    props.put("lastName", lastName);
    props.put("email", email);
    List<UT> users = _dao.findEntitiesByProperties(userClass, props);
    if (users.size() > 1) {
      throw new DuplicateEntityException(users.get(0));
    }
    if (users.size() == 1) {
      log.info("found existing user " + users.get(0) + " for " + firstName + " " + lastName + ", " + email);
      return users.get(0);
    }
    return null;
  }  
}

