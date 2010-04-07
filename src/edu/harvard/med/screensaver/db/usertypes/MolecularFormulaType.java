// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.usertypes;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.harvard.med.screensaver.model.Volume;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class MolecularFormulaType implements UserType
{
  public int[] sqlTypes() 
  {
    return new int[]{ Hibernate.TEXT.sqlType() };
  }
  
  public Object assemble(Serializable cached, Object owner)
    throws HibernateException
  {
    return cached;
  }

  public Object deepCopy(Object value) throws HibernateException
  {
    return value;
  }

  public Serializable disassemble(Object value) throws HibernateException
  {
    return (Serializable) value;
  }

  public boolean equals(Object x, Object y) throws HibernateException
  {
    if (x == y) return true;
    if (x == null || y == null) return false;
    return x.equals(y);
  }

  public int hashCode(Object x) throws HibernateException
  {
    return x.hashCode();
  }

  public boolean isMutable()
  {
    return false;
  }

  public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
    throws HibernateException,
    SQLException
  {
    String rawValue = rs.getString(names[0]);
    // Deferred check after first read
    if (rs.wasNull()) return null;
    return rawValue;
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index)
    throws HibernateException,
    SQLException
  {
    if (value == null) {
      st.setNull(index, Hibernate.TEXT.sqlType());
    } 
    else {
      st.setString(index, value.toString());
    }    
  }

  public Object replace(Object original, Object target, Object owner)
    throws HibernateException
  {
    return original;
  }

  public Class returnedClass()
  {
    return Volume.class;
  }

  // private methods

}
