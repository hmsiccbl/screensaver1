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
 * The Activity Indicator type vocabulary.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum ActivityIndicatorType implements VocabularyTerm
{
  
  // the vocabulary
  
  NUMERICAL("Numerical"),
  BOOLEAN("Boolean"),
  SCALED("Scaled")
  ;

  
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link ActivityIndicatorType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<ActivityIndicatorType> {
    public UserType() {
      super(ActivityIndicatorType.values());
    }
  }


  // private instance field and constructor
  
  private String _value;

  /**
   * Constructs a <code>ActivityIndicatorType</code> vocabulary term.
   * @param value The value of the term.
   */
  private ActivityIndicatorType(String value) {
    _value = value;
  }
  
  
  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue() {
    return _value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getValue();
  }
}
