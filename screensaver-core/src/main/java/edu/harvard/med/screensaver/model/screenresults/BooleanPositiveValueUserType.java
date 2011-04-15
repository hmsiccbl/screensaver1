// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class BooleanPositiveValueUserType implements UserType
{
  private static final int[] SQL_TYPES = { Types.CLOB };

  public int[] sqlTypes()
  {
    return SQL_TYPES;
  }

  public Class returnedClass()
  {
    return Boolean.class;
  }

  public boolean equals(Object x, Object y)
  {
    if (x == y) {
      return true;
    }
    if (null == x || null == y) {
      return false;
    }
    return x.equals(y);
  }

  public int hashCode(Object object) throws HibernateException
  {
    return object.hashCode();
  }

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
  throws HibernateException, SQLException {
    String termName = resultSet.getString(names[0]);
    if (resultSet.wasNull()) return null;
    if (Boolean.valueOf(termName)) {
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index)
  throws HibernateException, SQLException {
    if (value == null) {
      statement.setNull(index, Types.CLOB);
    }
    else {
      String storageValue = value.toString();
      statement.setString(index, storageValue);
    }
  }

  public Object deepCopy(Object value)
  {
    return value;
  }

  public boolean isMutable()
  {
    return false;
  }

  public Serializable disassemble(Object value) throws HibernateException
  {
    return (Serializable) value;
  }

  public Object assemble(Serializable cached, Object owner)
    throws HibernateException
  {
    return cached;
  }

  public Object replace(Object original, Object target, Object owner) throws HibernateException
  {
    return original;
  }

}