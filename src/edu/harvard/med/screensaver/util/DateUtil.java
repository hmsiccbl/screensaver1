// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtil
{
  // static members

  private static Logger log = Logger.getLogger(DateUtil.class);

  /**
   * Returns a date. Time portion of date always represents 12:00am of the
   * specified day, facilitating comparison between dates.
   * 
   * @param year the year
   * @param month the month (1=Jan, ..., 12=Dec)
   * @param dayOfMonth the day of the month (1..{28,30,31})
   * @return a Date whose millisecond value corresponds to the start of the
   *         specified day
   */
  public static Date makeDate(int year,
                              int month,
                              int dayOfMonth)
  {
    Calendar calDate = Calendar.getInstance();
    calDate.clear();
    calDate.set(year,
                month - 1,
                dayOfMonth);
    return calDate.getTime();
  }

}

