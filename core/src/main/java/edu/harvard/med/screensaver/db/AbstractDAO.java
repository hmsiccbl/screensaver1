// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class AbstractDAO
{
  /**
   * This controls the number of to be read before flushing the hibernate
   * cache and persisting all of the entities. This value should be matched to
   * the hibernate.jdbc.batch_size property on the hibernateSessionFactory bean.
   */
  public static final int ROWS_TO_CACHE = 50;

  @PersistenceContext
  private EntityManager _entityManager;

  protected AbstractDAO()
  {}

  protected EntityManager getEntityManager()
  {
    return _entityManager;
  }

  protected void setEntityManager(EntityManager entityManager)
  {
    _entityManager = entityManager;
  }

  /**
   * @motivation provides a means of obtaining the underlying Hibernate session, until we can make all code (and HQL)
   *             JPA-compliant
   */
  @Deprecated
  public Session getHibernateSession()
  {
    return (Session) _entityManager.getDelegate();
  }

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
    getEntityManager().flush();
  }

  public void clear()
  {
    getEntityManager().clear();
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

  public <E> List<E> runQuery(edu.harvard.med.screensaver.db.Query<E> query)
  {
    return query.execute(getHibernateSession());
  }
  
  public ScrollableResults runScrollQuery(edu.harvard.med.screensaver.db.ScrollQuery query)
  {
    return query.execute(getHibernateSession());
  }

  public Query getQuery(String queryName) 
  {
    return _entityManager.createNamedQuery(queryName);
  }
}
