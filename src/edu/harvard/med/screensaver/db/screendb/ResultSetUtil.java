// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class ResultSetUtil
{
  /**
   * Retrieves a date from the ResultSet, ignoring its time zone.
   */
  public static LocalDate getDate(ResultSet rs, String dateField) throws SQLException
  {
    Date date = rs.getDate(dateField);
    if (date == null) {
      return null;
    }
    return new LocalDate(date.getTime(), DateTimeZone.UTC);
  }

  /**
   * Retrieves a date/time from the ResultSet
   */
  public static DateTime getDateTime(ResultSet rs, String dateField) throws SQLException
  {
    Date date = rs.getDate(dateField);
    if (date == null) {
      return null;
    }
    return new DateTime(date.getTime());
  }
}
