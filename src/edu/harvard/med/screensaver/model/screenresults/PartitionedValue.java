// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * Vocabulary for {@link ResultValue#getValue()} when parent's
 * {@link ResultValueType#getActivityIndicatorType()} is
 * {@link edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType#PARTITION}.
 * <i>NOT CURRENTLY USED IN THE PERSISTED MODEL.  See {@link ResultValue#generateTypedValue()}.</i>
 * @author ant
 */
public enum PartitionedValue implements VocabularyTerm
{
  
  // the vocabulary
  
  STRONG("3", "S"),
  MEDIUM("2", "M"),
  WEAK("1", "W"),
  NONE("0", ""),
  ;
  
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link PartitionedValue} vocabulary.
   */
  public static class UserType extends VocabularyUserType<PartitionedValue>
  {
    public UserType()
    {
      super(PartitionedValue.values());
    }
  }

  private static Map<String,PartitionedValue> _value2Enum;
  static {
    _value2Enum = new HashMap<String,PartitionedValue>();
    for (PartitionedValue pv : values()) {
      _value2Enum.put(pv.getValue(), pv);
    }
  }
  
  public static PartitionedValue lookupByValue(String value)
  {
    PartitionedValue result = _value2Enum.get(value);
    if (result == null) {
      throw new IllegalArgumentException("no PartitionedValue enum for '" + value + "'");
    }
    return result;
  }


  // private instance field and constructor

  private String _value;
  private String _displayValue;

  /**
   * Constructs a <code>PartitionedValue</code> vocabulary term.
   * @param value The value of the term.
   */
  private PartitionedValue(String value, String displayValue)
  {
    _value = value;
    _displayValue = displayValue;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }
  
  public String getDisplayValue()
  { 
    return _displayValue;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }
}
