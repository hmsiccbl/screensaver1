// $HeadURL$
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

public class PartitionedValueUserType extends VocabularyUserType<PartitionedValue>
{
  public PartitionedValueUserType()
  {
    super(PartitionedValue.values());
  }
  
  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
  throws HibernateException, SQLException {
    String termName = resultSet.getString(names[0]);
    if (resultSet.wasNull()) return null;
    for (PartitionedValue pv : PartitionedValue.values()) {
      if (termName.equals(pv.toStorageValue())) return pv;
    }
    // should only get here if the ResultValue.getDataColumn().isPartitionedPositiveIndicator() == false
    return null;
  }

  @Override
  public void nullSafeSet(PreparedStatement statement, Object value, int index)
  throws HibernateException, SQLException {
    if (value != null) {
      for (PartitionedValue pv : PartitionedValue.values()) {
        if (value.equals(pv)) {
          statement.setString(index, pv.toStorageValue());
          return;
        }
      }
    }
    statement.setNull(index, Types.CLOB);
  }
}