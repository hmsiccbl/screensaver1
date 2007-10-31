// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AbstractDAO extends HibernateDaoSupport
{
  // static members

  private static Logger log = Logger.getLogger(AbstractDAO.class);


  // instance data members

  // public constructors and methods

  /**
   * This method can be called before invoking other GenericEntityDAO methods that issue HQL
   * (or Criteria-based) queries to ensure that newly instantiated and persisted
   * entities are considered by the query. This is never necessary if the
   * Hibernate session flush mode is AUTO or or ALWAYS, since in these cases
   * Hibernate will ensure the session is always flushed prior to executing an
   * HQL query.
   */
  public void flush()
  {
    getHibernateTemplate().flush();
  }

  public void clear()
  {
    getHibernateTemplate().clear();
  }

  /**
   * Executes a block of code, presumably with multiple GenericEntityDAO calls, into a single
   * transactions.
   * <p>
   * <i>It is now preferred that any code that needs to be executed within a
   * transaction is instead contained within a method of a Spring-managed bean
   * class that has a {@link Transactional} annotation.</i>
   *
   * @param daoTransaction the object encapsulating the transactional code to
   *          execute.
   */
  public void doInTransaction(DAOTransaction daoTransaction)
  {
    daoTransaction.runTransaction();
  }


  @SuppressWarnings("unchecked")
  public <E> List<E> runQuery(final edu.harvard.med.screensaver.db.Query query)
  {
    return (List<E>)
    getHibernateTemplate().execute(new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        Query hibQuery = query.buildQuery(session);
        return hibQuery.list();
      }
    });
  }

  // private methods

  protected AbstractDAO() {}

}

