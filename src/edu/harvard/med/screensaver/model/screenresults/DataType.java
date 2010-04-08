// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * Vocabulary used to specify how the determination of "positives" is being
 * recorded by a {@link DataColumn}, when
 * {@link DataColumn#isPositiveIndicator()} is <code>true</code>.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum DataType implements VocabularyTerm
{

  // the vocabulary
  
  NUMERIC("Numeric", false),
  TEXT("Text", false),
  POSITIVE_INDICATOR_BOOLEAN("Boolean Positive Indicator", true),
  POSITIVE_INDICATOR_PARTITION("Partition Positive Indicator", true)
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link DataType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<DataType>
  {
    public UserType()
    {
      super(DataType.values());
    }
  }

  private String _value;
  private boolean _isPositiveIndicator;

  
  /**
   * Constructs a <code>DataType</code> vocabulary term.
   * @param value The value of the term.
   */
  private DataType(String value, boolean isPositiveIndicator)
  {
    _value = value;
    _isPositiveIndicator = isPositiveIndicator;
  }

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  @Override
  public String toString()
  {
    return getValue();
  }


  public boolean isPositiveIndicator()
  {
    return _isPositiveIndicator;
  }
}
