// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * Utility for manipulating schemas, via Spring+Hibernate.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SchemaUtil extends AbstractDAO
{
  private static Logger log = Logger.getLogger(SchemaUtil.class);


  private UsersDAO _usersDao;
  private List<String> _schemaTableNames = new ArrayList<String>();

  public void setUsersDao(UsersDAO usersDao)
  {
    _usersDao = usersDao;
  }

  /**
   * Truncate all the tables in the schema.
   * 
   * @motivation efficient means of wiping the schema clean between running a unit test
   */
  public void truncateTables()
  {
    log.debug("truncating tables for " + makeDataSourceString());
    verifyIsTestDatabase();

    try {
      List<String> tables = getSchemaTableNames();
      if (tables.isEmpty()) {
        throw new IllegalStateException("schema has not been instantiated in the database");
      }
      String sql = "TRUNCATE TABLE " + Joiner.on(",").join(tables);
      getEntityManager().createNativeQuery(sql).executeUpdate();
    }
    catch (Exception e) {
      log.error("could not truncate tables: " + e);
    }
  }

  /**
   * Grant all privileges on all tables to the developers.
   * @motivation allow developers to access and modify tables from psql
   */
  public void grantDeveloperPermissions()
  {
    log.info("granting developer permissions for " + makeDataSourceString());
    try {
      if (getSchemaTableNames().isEmpty()) {
        return;
      }
      String sql = "GRANT ALL ON " + Joiner.on(",").join(getSchemaTableNames()) + " TO ";
      List<String> developerECommonsIds = _usersDao.findDeveloperECommonsIds();
      if (developerECommonsIds.size() == 0) {
        return;
      }
      for (String eCommonsId : developerECommonsIds) {
        sql += eCommonsId + ", ";
      }
      sql = sql.substring(0, sql.length() - 2);
      getEntityManager().createNativeQuery(sql).executeUpdate();
    }
    catch (Exception e) {
      log.error("could not truncate tables or create schema: " + e);
    }
  }

  private String makeDataSourceString()
  {
    try {
      Session session = getHibernateSession();
      Connection connection = session.connection();
      String connectionUrl = connection.getMetaData().getURL();
      String connectionUserName = connection.getMetaData().getUserName();
      String dataSourceString = connectionUserName + "@" + connectionUrl;
      return dataSourceString;
    }
    catch (SQLException e) {
      log.error("could not determine connection properties");
      return "<unknown database connection>";
    }
  }

  /**
   * Return true iff the database is fully loaded.
   * 
   * @motivation Prevent dropping fully loaded databases, or truncating their
   *             tables, since this is a costly mistake.
   */
  public void verifyIsTestDatabase()
  {
    try {
      Connection connection = getHibernateSession().connection();
      String connectionUrl = connection.getMetaData().getURL();
      if (!connectionUrl.contains("test")) {
        throw new RuntimeException("attempted to drop non-test database");
      }
    }
    catch (SQLException e) {
      log.error("could not determine connection properties");
    }
  }

  private List<String> getSchemaTableNames() throws SQLException
  {
    if (_schemaTableNames.isEmpty()) {
      try {
        Connection connection = getHibernateSession().connection();
        String url = connection.getMetaData().getURL();
        String schemaName = url.substring(url.lastIndexOf('/') + 1);
        _schemaTableNames.addAll(getEntityManager().createNativeQuery("SELECT table_name FROM information_schema.tables WHERE table_catalog = ? AND table_schema = 'public'").setParameter(1, schemaName).getResultList());
      }
      catch (IllegalStateException e) {
        log.error("bad illegal state exception", e);
      }
    }
    return _schemaTableNames;
  }
}
