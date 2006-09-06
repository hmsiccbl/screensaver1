// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Utility for manipulating schemas, via Spring+Hibernate.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SchemaUtil extends HibernateDaoSupport implements ApplicationContextAware
{
  
  // static fields

  private static Logger log = Logger.getLogger(SchemaUtil.class);
  
  
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
    app.addCommandLineOption(OptionBuilder.
                             withArgName("truncate").
                             withLongOpt("truncate").
                             withDescription("truncate all tables in the database schema (create new tables, if necessary)").
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
      else if (app.isCommandLineFlagSet("reset")) {
        app.getSpringBean("schemaUtil", SchemaUtil.class).truncateTablesOrCreateSchema();
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


  // instance fields
  
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

  
  // instance methods
  
  public void setSessionFactoryBeanId(String sessionFactoryBeanId)
  {
    // we go through some hoops to get the sessionFactory bean, because Spring
    // normally wants to provide a *product* of a factory, when a factory bean
    // is injected; but we want the factory itself, so we have to use getBean()
    // with the special "&" prefix syntax. See "3.6.1. Obtaining a FactoryBean,
    // not its product" in Spring 1.2.x. reference manual.
    _sessionFactoryBeanId = sessionFactoryBeanId;
  }
  
  /**
   * Setter for applicationContext Property.
   */
  public void setApplicationContext(ApplicationContext applicationContext)
  throws BeansException
  {
    _appCtx = applicationContext;
  }
  
  /**
   * Drop the schema that is configured for this Spring+Hibernate enabled project.
   */
  public void dropSchema() throws DataAccessException
  {
    log.info("dropping schema for " + makeDataSourceString());
    getLocalSessionFactoryBean().dropDatabaseSchema();
  }
  
  /**
   * Create the schema that is configured for this Spring+Hibernate enabled project.
   */
  public void createSchema() throws DataAccessException
  {
    log.info("creating schema for " + makeDataSourceString());
    getLocalSessionFactoryBean().createDatabaseSchema();
  }
  
  /**
   * Drop and create (in that order) the schema that is configured for this
   * Spring+Hibernate enabled project.
   */
  public void recreateSchema() throws DataAccessException
  {
    dropSchema();
    createSchema();
  }
  
  /**
   * Truncate all the tables in the schema. If there are no tables in the
   * schema, then {@link #createSchema() create the schema}. Note: will not
   * create new tables that may have been added to the schema, if any tables
   * already exist. For this use must use createSchema().
   */
  @SuppressWarnings("unchecked")
  public void truncateTablesOrCreateSchema()
  {
    recreateSchema();
//    log.info("truncating tables for " + makeDataSourceString());
//    Connection connection = getSession().connection();
//    
//    try {
//      String url = connection.getMetaData().getURL();
//      String schemaName = url.substring(url.lastIndexOf('/') + 1);
//
//      Statement statement = connection.createStatement();
//      statement.execute(
//        "SELECT table_name FROM information_schema.tables\n" +
//        "WHERE\n" +
//        " table_catalog = '" + schemaName + "' AND\n" +
//        " table_schema = 'public'\n");
//      
//      String sql = "TRUNCATE TABLE "; 
//      ResultSet resultSet = statement.getResultSet();
//      while (resultSet.next()) {
//        sql += resultSet.getString(1) + ", ";
//      }
//      
//      if (sql.equals("TRUNCATE TABLE ")) { // no tables in the schema
//        createSchema();
//        return;
//      }
//      
//      sql = sql.substring(0, sql.length() - 2);
//      statement.close();
//
//      statement = connection.createStatement();
//      statement.execute(sql);
//      statement.close();
//    }
//    catch (HibernateException e) {
//      throw convertHibernateAccessException(e);
//    }
//    catch (IllegalStateException e) {
//      log.error("bad illegal state exception", e);
//    }
//    catch (SQLException e) {
//      log.error("bad sql exception", e);
//    }
  }

  
  // private methods
  
  /**
   * Lazy acquisition of LocalSessionFactoryBean.
   * 
   * @motivation bean properties are not set in a deterministic order, but we
   *             depend upon multiple properties.
   * @return
   */
  private LocalSessionFactoryBean getLocalSessionFactoryBean()
  {
    if ( _sessionFactory == null ) {
      _sessionFactory = (LocalSessionFactoryBean) _appCtx.getBean("&" + _sessionFactoryBeanId);
    }
    return _sessionFactory;
  }

  private String makeDataSourceString()
  {
    BasicDataSource dataSource = (BasicDataSource) _appCtx.getBean("screensaverDataSource");
    assert dataSource != null : "spring bean 'screensaverDataSource' not found";
    return dataSource.getUsername() + "@" + dataSource.getUrl();
  }

}
