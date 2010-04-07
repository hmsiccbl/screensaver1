//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.db.usertypes;

import java.io.Serializable;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.joda.time.LocalDate;

public class LocalDateType implements UserType
{
  // static members

  private static Logger log = Logger.getLogger(LocalDateType.class);

  private static final int[] SQL_TYPES = new int[] { Types.DATE };

  public int[] sqlTypes()
  {
    return SQL_TYPES;
  }

  public Class returnedClass()
  {
    return LocalDate.class;
  }

  public boolean equals(Object x, Object y) throws HibernateException
  {
    if (x == y)
    {
      return true;
    }
    if (x == null || y == null)
    {
      return false;
    }
    LocalDate dtx = (LocalDate) x;
    LocalDate dty = (LocalDate) y;

    return dtx.equals(dty);
  }

  public int hashCode(Object object) throws HibernateException
  {
    return object.hashCode();
  }

  public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
    throws HibernateException, SQLException
  {
    Date sqlDate = rs.getDate(names[0]);
    // Deferred check after first read
    if (rs.wasNull()) return null;
    LocalDate localDate = new LocalDate(sqlDate);
    assert sqlDate.toString().equals(localDate.toString()) : "date conversion failed";
    return localDate;
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException
  {
    if (value == null) {
      st.setNull(index, Hibernate.DATE.sqlType());
    } 
    else {
      LocalDate localDate = (LocalDate) value;
      /* This is WRONG! Fails for ranges [2008-03-09,2008-04-06] and [2008-10-26,2008-11-02], which are the dates between the old and nw Daylight Savings times!!!
      Date sqlDate = new Date(((LocalDate) value).toDateMidnight().getMillis());
       */
      Date sqlDate = Date.valueOf(localDate.toString());
      assert sqlDate.toString().equals(localDate.toString()) : "date conversion failed";
      st.setDate(index, sqlDate);
    }    
  }

  public Object deepCopy(Object value) throws HibernateException
  {
    if (value == null)
    {
      return null;
    }

    return new LocalDate(value);
  }

  public boolean isMutable()
  {
    return false;
  }

  public Serializable disassemble(Object value) throws HibernateException
  {
    return (Serializable) value;
  }

  public Object assemble(Serializable cached, Object value) throws HibernateException
  {
    return cached;
  }

  public Object replace(Object original, Object target, Object owner) throws HibernateException
  {
    return original;
  }
}
