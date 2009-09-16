// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;


/**
 * A Hibernate <code>UserType</code> to map the {@link VocabularyTerm
 * VocabularyTerms}. This abstract superclass provides all the functionality
 * to make the <code>VocabularyTerms</code> work as Hibernate types. All it
 * needs to know are the <code>Enum.values()</code> array of vocabulary terms.
 *
 * <p>
 *
 * Let's say the name of the <code>VocabularyTerm</code> subclass is
 * <code>term.VTS</code>. In order to make <code>term.VTS</code> function
 * properly, two things need to be done. First, declare a static nested class
 * like so:
 *
 * <pre>
 *   public static class UserType extends VocabularyUserType<VTS> {
 *     public UserType() {
 *       super(VTS.values());
 *     }
 *   }
 * </pre>
 *
 * Second, every entity property that has type <code>term.VTS</code> must be
 * Hibernate-annotated with the <code>UserType</code>. For example:
 *
 * <pre>
 *   @org.hibernate.annotations.Type(
 *     type="term.VTS$UserType"
 *   )
 * </pre>
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class VocabularyUserType<VT extends VocabularyTerm>
implements UserType
{

  // private static fields

  private static final int[] SQL_TYPES = {Types.CLOB};


  // private instance fields

  private Map<String,VT> _valueToTerm = new HashMap<String,VT>();
  private Map<String,VT> _valueToTermCaseInsensitive = new HashMap<String,VT>();


  // protected constructor

  protected VocabularyUserType(VT [] vocabularyTerms)
  {
    for (VT vocabularyTerm : vocabularyTerms) {
      _valueToTerm.put(vocabularyTerm.getValue(), vocabularyTerm);
      _valueToTermCaseInsensitive.put(vocabularyTerm.getValue().toLowerCase(), vocabularyTerm);
    }
  }


  // public method for general use

  public VT getTermForValue(String value)
  {
    return _valueToTerm.get(value);
  }

  public VT getTermForValueCaseInsensitive(String value)
  {
    if (value == null) {
      return null;
    }
    VT vt = _valueToTermCaseInsensitive.get(value.toLowerCase());
    if (vt == null) {
      throw new IllegalArgumentException("'" + value + "' must be one of: " + _valueToTerm.keySet());
    }
    return vt;
  }


  // public instance methods that implement UserType

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  public int[] sqlTypes() { return SQL_TYPES; }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  public Class returnedClass() { return VocabularyTerm.class; }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  public boolean equals(Object x, Object y) {
    if (x == y) {
      return true;
    }
    if (null == x || null == y) {
      return false;
    }
    return x.equals(y);
  }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
   */
  public int hashCode(Object object) throws HibernateException {
    return object.hashCode();
  }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
   */
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
    throws HibernateException, SQLException {
    String termName = resultSet.getString(names[0]);
    return resultSet.wasNull() ? null : _valueToTerm.get(termName);
  }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
   */
  public void nullSafeSet(PreparedStatement statement, Object value, int index)
    throws HibernateException, SQLException {
    if (value == null) {
      statement.setNull(index, Types.CLOB);
    }
    else {
      // TODO: value.toString() should be value.getValue(), otherwise concrete class must implement toString() as delegate to getValue(), which is not a documented requirement of this class
      statement.setString(index, value.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  public Object deepCopy(Object value) { return value; }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#isMutable()
   */
  public boolean isMutable() { return false; }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
   */
  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) value;
  }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  public Object assemble(Serializable cached, Object owner)
    throws HibernateException {
    return cached;
  }

  /* (non-Javadoc)
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return original;
  }
}
