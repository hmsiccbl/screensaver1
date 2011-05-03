// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;



import edu.harvard.med.screensaver.model.VocabularyTerm;

/**
 * Vocabulary for {@link ResultValue#getBooleanPositiveValue()} when parent's {@link DataColumn#getDataType()} is
 * {@link edu.harvard.med.screensaver.model.screenresults.DataType#POSITIVE_INDICATOR_BOOLEAN}
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum BooleanPositiveValue implements VocabularyTerm
{
  TRUE(Boolean.TRUE.toString()),
  FALSE(Boolean.FALSE.toString());
  
  private String _value;

  private BooleanPositiveValue(String value)
  {
    _value = value;
  }

  public String getValue()
  {
    return _value;
  }
  
  @Override
  public String toString()
  {
    return getValue();
  }

  public String toStorageValue()
  {
    return _value;
  }
}
