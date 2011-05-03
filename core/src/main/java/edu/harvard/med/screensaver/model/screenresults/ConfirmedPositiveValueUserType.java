// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.0.0-dev/src/edu/harvard/med/screensaver/model/screenresults/ConfirmedPositiveValueUserType.java
// $
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;

import edu.harvard.med.screensaver.model.VocabularyUserType;

public class ConfirmedPositiveValueUserType extends VocabularyUserType<ConfirmedPositiveValue>
{
  public ConfirmedPositiveValueUserType()
  {
    super(ConfirmedPositiveValue.values());
  }
  
  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
  throws HibernateException, SQLException {
    String termName = resultSet.getString(names[0]);
    if (resultSet.wasNull()) return null;
    for (ConfirmedPositiveValue cpv : ConfirmedPositiveValue.values()) {
      if (termName.equals(cpv.toStorageValue())) return cpv;
    }
    // should only get here if the ResultValue.getDataColumn().isConfirmedPositiveIndicator() == false
    return null;
  }

  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index)
  throws HibernateException, SQLException {
    if (value != null) {
      for (ConfirmedPositiveValue cpv : ConfirmedPositiveValue.values()) {
        if (value.equals(cpv)) {
          statement.setString(index, cpv.toStorageValue());
          return;
        }
      }
    }
    statement.setNull(index, Types.CLOB);
  }
}