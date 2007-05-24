// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class HibernateSessionTest extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(HibernateSessionTest.class);


  // instance data members

  private HibernateTemplate _hibernateTemplate;
  private GenericEntityDAO _dao;
  private Integer _userId;
  private ScreensaverUser _user;


  // public constructors and methods

  public GenericEntityDAO getDao()
  {
    return _dao;
  }

  public void setDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public Integer getUserId()
  {
    return _userId;
  }

  public void setUserId(Integer userId)
  {
    _userId = userId;
  }

  public HibernateTemplate getHibernateTemplate()
  {
    return _hibernateTemplate;
  }

  public void setHibernateTemplate(HibernateTemplate hibTemplate)
  {
    _hibernateTemplate = hibTemplate;
  }

  public ScreensaverUser getUser()
  {
    return _user;
  }

  public void setUser(ScreensaverUser user)
  {
    _user = user;
  }
  
  public int getSessionId()
  {
    return getCurrentSession("getSessionId").hashCode();
  }
  
  public boolean isUserInSession()
  {
    return getCurrentSession("isUserInSession").contains(_user);
    
  }

  public boolean isSessionOpen()
  {
    return getCurrentSession("isSessionOpen").isOpen();
  }
  
  // JSF application methods
  
  public String load()
  {
    log.info("loading user with id " + _userId);
    _user = _dao.findEntityByProperty(ScreensaverUser.class, "screensaverUserId", _userId);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  
  // private methods

  private Session getCurrentSession(String fromMethod)
  {
    Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
    log.info(fromMethod + "(): current hibernate session = " + session.hashCode());
    return session;
  }

}

