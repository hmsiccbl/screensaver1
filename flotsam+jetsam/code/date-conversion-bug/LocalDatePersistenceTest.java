//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDate;

public class LocalDatePersistenceTest extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(LocalDatePersistenceTest.class);

  protected GenericEntityDAO genericEntityDao;
  private Connection _connection;

  @Override
  protected void onSetUp() throws Exception
  {
    executeSql("CREATE TABLE local_date_test (d date);");
  }

  @Override
  protected void onTearDown() throws Exception
  {
    executeSql("DROP TABLE local_date_test;");
  }

  private interface ResultSetOperation 
  {
    void run(ResultSet rs) throws SQLException;
  }

  private void executeSql(final String sql, final ResultSetOperation op, final Date d) throws SQLException
  {
    genericEntityDao.runQuery(new Query() {
      public List execute(Session session)
      {
        try {
          _connection = session.connection();
          PreparedStatement statement = _connection.prepareStatement(sql);
          if (d != null) statement.setDate(1, d);
          log.debug("executing sql: " + sql);
          statement.execute();
          if (op != null) {
            op.run(statement.getResultSet());
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        return null; 
      }
    });
  }

  private void executeSql(String sql, Date d) throws SQLException
  {
    executeSql(sql, null, d);
  }

  private void executeSql(String sql) throws SQLException
  {
    executeSql(sql, null, null);
  }

  static LocalDate[] dates = new LocalDate[] { 
    new LocalDate(2008, 3, 8),
    new LocalDate(2008, 3, 9),
    new LocalDate(2008, 3, 10),

    new LocalDate(2008, 4, 5),
    new LocalDate(2008, 4, 6),
    new LocalDate(2008, 4, 7),

    new LocalDate(2008, 10, 25),
    new LocalDate(2008, 10, 26),
    new LocalDate(2008, 10, 27),

    new LocalDate(2008, 11, 1),
    new LocalDate(2008, 11, 2),
    new LocalDate(2008, 11, 3) };


  public void testLocalDatePersistence() throws Exception
  {
    for (final LocalDate localDate : dates) {
      executeSql("DELETE FROM local_date_test;");
      log.debug("testing " + localDate);
      //WRONG! Date sqlDateToInsert = new Date(localDate.toDateMidnight().getMillis());
      Date sqlDateToInsert = Date.valueOf(localDate.toString());
      assertEquals(localDate.toString(), sqlDateToInsert.toString());
      log.debug("sqlDateToInsert=" + sqlDateToInsert);
      executeSql("INSERT INTO local_date_test (d) values (?)", null, sqlDateToInsert);
      executeSql("SELECT d FROM local_date_test;",
                 new ResultSetOperation() {
        public void run(ResultSet rs) throws SQLException {
          rs.next();
          Date sqlDate = rs.getDate(1);
          String sqlDateString = rs.getString(1);
          //long localDateMillis = localDate.toDateMidnight().getMillis();
          //long sqlDateMillis = sqlDate.getTime();
          LocalDate sqlDateToLocalDate = new LocalDate(sqlDate);
          LocalDate sqlDateStringToLocalDate = new LocalDate(sqlDateString);
          //log.debug("\tlocalDate.millis=" + localDateMillis);
          //log.debug("\tsqlDate.millis=" + sqlDateMillis);
          log.debug("\tsqlDataString=" + sqlDateString);
          log.debug("\tsqlDataToLocalDate=" + sqlDateToLocalDate);
          log.debug("\tsqlDataStringToLocalDate=" + sqlDateStringToLocalDate);
          assertEquals(localDate.toString(), localDate, sqlDateToLocalDate);
          //assertEquals(localDate.toString(), localDateMillis, sqlDateMillis);
//          if (localDateMillis != sqlDateMillis) {
//            log.error("\tFAILED for local date " + localDate + " != " + sqlDate + "; differs by " + ((sqlDateMillis - localDateMillis) / 1000) + " sec");
//          }

        }
      }, null);
    }
  }
}