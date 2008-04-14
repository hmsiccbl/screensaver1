// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.CommandLineApplication;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import com.google.common.base.Join;

/**
 * Utility for manipulating schemas, via Spring+Hibernate.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@SuppressWarnings("deprecation")
public class SchemaUtil extends AbstractDAO implements ApplicationContextAware
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
                             create('c'));
    app.addCommandLineOption(OptionBuilder.
                             withArgName("drop").
                             withLongOpt("drop").
                             withDescription("drop database schema").
                             create('d'));
    app.addCommandLineOption(OptionBuilder.
                             withArgName("recreate").
                             withLongOpt("recreate").
                             withDescription("drop and create the database schema").
                             create('r'));
    try {
      if (!app.processOptions(/*acceptDatabaseOptions=*/true, /*showHelpOnError=*/true)) {
        return;
      }

      if (app.isCommandLineFlagSet("recreate")) {
        app.getSpringBean("schemaUtil", SchemaUtil.class).recreateSchema();
      }
      else {
        if (app.isCommandLineFlagSet("drop")) {
          app.getSpringBean("schemaUtil", SchemaUtil.class).dropSchema();
        }
        if (app.isCommandLineFlagSet("create")) {
          app.getSpringBean("schemaUtil", SchemaUtil.class).createSchema();
        }
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
   *             injection of a SessionFactory is problematic, as Spring
   *             treats factory beans as special cases, and injects the
   *             product of the factory, rather than the factory itself.
   */
  private ApplicationContext _appCtx;

  private LocalSessionFactoryBean _sessionFactory;

  private String _sessionFactoryBeanId;

  private UsersDAO _usersDao;

  private List<String> _schemaTableNames = new ArrayList<String>();


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

  public void setUsersDao(UsersDAO usersDao)
  {
    _usersDao = usersDao;
  }

  /**
   * Drop the schema that is configured for this Spring+Hibernate enabled
   * project. Will only execute if this is a test database (see
   * {@link #isTestDatabase()}).
   */
  public void dropSchema() throws DataAccessException
  {
    log.info("dropping schema for " + makeDataSourceString());
    verifyIsTestDatabase();
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
   * Truncate all the tables in the schema. If there are no tables in the schema, then
   * {@link #createSchema() create the schema}.
   * @motivation efficient means of wiping the schema clean between running a unit test
   */
  @SuppressWarnings("unchecked")
  public void truncateTablesOrCreateSchema()
  {
    log.info("truncating tables for " + makeDataSourceString());
    verifyIsTestDatabase();
    Session session = getSession();
    Connection connection = session.connection();

    try {
      String sql = "TRUNCATE TABLE " + Join.join(",", getSchemaTableNames());
      if (sql.equals("TRUNCATE TABLE ")) { // no tables in the schema
        createSchema();
        return;
      }

      Statement statement = connection.createStatement();
      statement.execute(sql);
      statement.close();

      // QUESTION: any reason to close the connection here? presumably if i am going
      // to release the session, the connection will get closed anyways. but who tf
      // knows? -s
      //connection.close();
    }
    catch (HibernateException e) {
      throw convertHibernateAccessException(e);
    }
    catch (IllegalStateException e) {
      log.error("bad illegal state exception", e);
    }
    catch (SQLException e) {
      log.error("bad sql exception", e);
    }
    finally {
      releaseSession(session);
    }
  }

  /**
   * Grant all privileges on all tables to the developers.
   * @motivation allow developers to access and modify tables from psql
   */
  @SuppressWarnings("unchecked")
  public void grantDeveloperPermissions()
  {
    log.info("granting developer permissions for " + makeDataSourceString());
    Session session = getSession();
    Connection connection = session.connection();

    try {
      if (getSchemaTableNames().isEmpty()) {
        return;
      }
      String sql = "GRANT ALL ON " + Join.join(",", getSchemaTableNames()) + " TO ";
      List<String> developerECommonsIds = _usersDao.findDeveloperECommonsIds();
      if (developerECommonsIds.size() == 0) {
        return;
      }
      for (String eCommonsId : developerECommonsIds)
      {
        sql += eCommonsId + ", ";
      }

      sql = sql.substring(0, sql.length() - 2);

      Statement statement = connection.createStatement();
      statement.execute(sql);
      statement.close();

      // QUESTION: any reason to close the connection here? presumably if i am going
      // to release the session, the connection will get closed anyways. but who tf
      // knows? -s
      //connection.close();
    }
    catch (HibernateException e) {
      throw convertHibernateAccessException(e);
    }
    catch (IllegalStateException e) {
      log.error("bad illegal state exception", e);
    }
    catch (SQLException e) {
      log.error("bad sql exception", e);
    }
    finally {
      releaseSession(session);
    }
  }


  // private methods

  /**
   * Lazy acquisition of LocalSessionFactoryBean.
   *
   * @motivation bean properties are not set in a deterministic order, but we
   *             depend upon multiple properties.

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
    Session session = getSession();
    try {
      Connection connection = session.connection();

      String connectionUrl = connection.getMetaData().getURL();
      String connectionUserName = connection.getMetaData().getUserName();
      String dataSourceString = connectionUserName + "@" + connectionUrl;

      // QUESTION: any need to close the connection? test suite stalling problem goes
      // away whether or not i close the conn. -s

      return dataSourceString;
    }
    catch (SQLException e) {
      log.error("could not determine connection properties");
      return "<unknown database connection>";
    }
    finally {
      releaseSession(session);
    }
  }

  /**
   * Return true iff the database is fully loaded.
   * 
   * @motviation Prevent dropping fully loaded databases, or truncating their
   *             tables, since this is a costly mistake.
   */
  private void verifyIsTestDatabase()
  {
    Session session = getSession();
    try {
      Connection connection = session.connection();
      String connectionUrl = connection.getMetaData().getURL();
      if (!connectionUrl.contains("test")) {
        throw new RuntimeException("attempted to drop non-test database");
      }
    }
    catch (SQLException e) {
      log.error("could not determine connection properties");
    }
    finally {
      releaseSession(session);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> getSchemaTableNames()
  {
    if (_schemaTableNames.isEmpty()) {
      Session session = getSession();
      Connection connection = session.connection();

      try {
        String url = connection.getMetaData().getURL();
        String schemaName = url.substring(url.lastIndexOf('/') + 1);

        Statement statement = connection.createStatement();
        statement.execute("SELECT table_name FROM information_schema.tables\n" +
                          "WHERE\n" +
                          " table_catalog = '" + schemaName + "' AND\n" +
                          " table_schema = 'public'\n");

        ResultSet resultSet = statement.getResultSet();
        while (resultSet.next()) {
          _schemaTableNames.add(resultSet.getString(1));
        }
        statement.close();
      }
      catch (HibernateException e) {
        throw convertHibernateAccessException(e);
      }
      catch (IllegalStateException e) {
        log.error("bad illegal state exception", e);
      }
      catch (SQLException e) {
        log.error("bad sql exception", e);
      }
      finally {
        releaseSession(session);
      }
    }
    return _schemaTableNames;
  }
}
