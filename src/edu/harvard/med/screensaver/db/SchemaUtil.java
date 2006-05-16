// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Utility for manipulating schemas, via Spring+Hibernate.
 * @author ant
 */
public class SchemaUtil implements ApplicationContextAware
{

  /**
   * The Spring application context.
   * 
   * @motivation Needed to get a Hibernate SessionFactory from Spring. Normal
   *             injection of a SessionFactory is problematic, as Spring appears
   *             to treat factory beans as special cases, and injects the
   *             product of a factory, rather than the factory itself.
   */
  private ApplicationContext _appCtx;
  
  private LocalSessionFactoryBean _sessionFactory;

  private String _sessionFactoryBeanId;

  public void setSessionFactoryBeanId(String sessionFactoryBeanId) {
    // we go through some hoops to get the sessionFactory bean, because Spring
    // normally wants to provide a *product* of a factory, when a factory bean
    // is injected; but we want the factory itself, so we have to use getBean()
    // with the special "&" prefix syntax. See "3.6.1. Obtaining a FactoryBean,
    // not its product" in Spring 1.2.x. reference manual.
    _sessionFactoryBeanId = sessionFactoryBeanId;
  }
  
  /**
   * Lazy acquisition of LocalSessionFactoryBean.
   * 
   * @motivation bean properties are not set in a deterministic order, but we
   *             depend upon multiple properties.
   * @return
   */
  private LocalSessionFactoryBean getSessionFactory() {
    if ( _sessionFactory == null ) {
      _sessionFactory = (LocalSessionFactoryBean) _appCtx.getBean("&" + _sessionFactoryBeanId);
    }
    return _sessionFactory;
  }

  /**
   * Setter for applicationContext Property.
   */
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    _appCtx = applicationContext;
  }
  
  /**
   * Drop the schema that is configured for this Spring+Hibernate enabled project.
   */
  public void dropSchema() throws DataAccessException {
    getSessionFactory().dropDatabaseSchema();
  }
  
  /**
   * Create the schema that is configured for this Spring+Hibernate enabled project.
   */
  public void createSchema() throws DataAccessException {
    getSessionFactory().createDatabaseSchema();
  }
  
  /**
   * Drop and create (in that order) the schema that is configured for this
   * Spring+Hibernate enabled project.
   */
  public void recreateSchema() {
    dropSchema();
    createSchema();
  }

}
