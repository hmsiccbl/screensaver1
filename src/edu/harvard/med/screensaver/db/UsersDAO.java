// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

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
   * @return a List of {@link ScreeningRoomUsers}s.
   */
  @SuppressWarnings("unchecked")
  public SortedSet<ScreeningRoomUser> findAllLabHeads()
  {
    // note: we perform sorting via a TreeSet, rather than asking persistence
    // layer to do sorting, as this keeps sorting order policy in
    // ScreensaverUserComparator, and also keeps our query simpler. Also, the
    // SortedSet return type makes return value more explicit
    String hql = "select distinct lh from ScreeningRoomUser lh left outer join lh.hbnLabHead where lh.hbnLabHead is null";
    SortedSet labHeads = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    labHeads.addAll((List<ScreeningRoomUser>) getHibernateTemplate().find(hql));
    return labHeads;
  }
  
  @SuppressWarnings("unchecked")
  public SortedSet<ScreeningRoomUser> findCandidateCollaborators()
  {
    // note: we perform sorting via a TreeSet, rather than asking persistence
    // layer to do sorting, as this keeps sorting order policy in
    // ScreensaverUserComparator, and also keeps our query simpler. Also, the
    // SortedSet return type makes return value more explicit. Performance
    // is not really an issue.
    SortedSet<ScreeningRoomUser> collaborators = 
      new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    collaborators.addAll((List<ScreeningRoomUser>) 
                         getHibernateTemplate().execute(new HibernateCallback() 
                         {
                           public Object doInHibernate(Session session) throws HibernateException, SQLException
                           {
                             return new ArrayList<ScreeningRoomUser>(session.
                               createCriteria(ScreeningRoomUser.class).
                               list());
                           }
                         }));
    return collaborators;
  }

  // private methods

}

