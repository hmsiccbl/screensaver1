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

import edu.harvard.med.screensaver.CommandLineApplication;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
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

  private static Logger log = Logger.getLogger(SchemaUtil.class);
  
  
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
    log.info("dropping schema for " + makeDataSourceString());
    getSessionFactory().dropDatabaseSchema();
  }
  
  /**
   * Create the schema that is configured for this Spring+Hibernate enabled project.
   */
  public void createSchema() throws DataAccessException {
    log.info("creating schema for " + makeDataSourceString());
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

  
  // private methods
  
  private String makeDataSourceString()
  {
    BasicDataSource dataSource = (BasicDataSource) _appCtx.getBean("screensaverDataSource");
    assert dataSource != null : "spring bean 'screensaverDataSource' not found";
    String userName = dataSource.getUsername();
    if (userName == null || userName.length() == 0) {
      try {
        userName = dataSource.getConnection().getMetaData().getUserName();
      }
      catch (SQLException e) {
        userName = "<unknown username>";
      }
    }
    return userName + "@" + dataSource.getUrl();
  }
  
  
  // static methods
  
  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.
                             withArgName("create").
                             withLongOpt("create").
                             withDescription("create database schema in an empty database").
                             create());
    app.addCommandLineOption(OptionBuilder.
                             withArgName("drop").
                             withLongOpt("drop").
                             withDescription("drop database schema").
                             create());
    app.addCommandLineOption(OptionBuilder.
                             withArgName("recreate").
                             withLongOpt("recreate").
                             withDescription("drop and then create database schema").
                             create());
    try {
      if (!app.processOptions(/*acceptDatabaseOptions=*/true, /*showHelpOnError=*/true)) {
        return;
      }
      
      if (app.isCommandLineFlagSet("drop")) {
        app.getSpringBean("schemaUtil", SchemaUtil.class).dropSchema();
      }
      else if (app.isCommandLineFlagSet("create")) {
        app.getSpringBean("schemaUtil", SchemaUtil.class).createSchema();
      }
      else if (app.isCommandLineFlagSet("recreate")) {
        app.getSpringBean("schemaUtil", SchemaUtil.class).recreateSchema();
      }
    }
    catch (DataAccessException e) {
      log.error("DataAccessException: " + e.getMessage());
      e.printStackTrace();
    }
    catch (ParseException e) {
      // handled sufficiently by CommandLineApplication
    }
  }

}
