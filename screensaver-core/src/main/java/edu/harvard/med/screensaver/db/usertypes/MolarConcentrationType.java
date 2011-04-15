// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.usertypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;

public class MolarConcentrationType implements UserType
{

  // static members

  public static final MolarUnit NORMALIZED_UNITS = MolarUnit.NORMALIZED_UNITS; 


  // instance data members

  // public constructors and methods


  public int[] sqlTypes() 
  {
    return new int[]{ Hibernate.BIG_DECIMAL.sqlType() };
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
    BigDecimal rawValue = rs.getBigDecimal(names[0]);
    // Deferred check after first read
    if (rs.wasNull()) return null;
    return new MolarConcentration(rawValue.toString(), NORMALIZED_UNITS).convertToReasonableUnits();
  }

  public void nullSafeSet(PreparedStatement st, Object value, int index)
    throws HibernateException,
    SQLException
  {
    if (value == null) {
      st.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
    } 
    else {
      st.setBigDecimal(index, ((MolarConcentration) value).getValue(NORMALIZED_UNITS));
    }    
  }

  public Object replace(Object original, Object target, Object owner)
    throws HibernateException
  {
    return original;
  }

  public Class returnedClass()
  {
    return MolarConcentration.class;
  }

  // private methods

}
