// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The Activity Indicator type vocabulary.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum AssayReadoutType implements VocabularyTerm
{
  
  // the vocabulary
  
  FLOURESCENCE("Flourescence"),
  PHOSPHORESCENCE("Phosphorescnce")
  // TODO: complete this list
  ;
  

  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link AssayReadoutType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<AssayReadoutType> {
    public UserType() {
      super(AssayReadoutType.values());
    }
  }


  // private instance field and constructor
  
  private String _value;

  /**
   * Constructs a <code>AssayReadoutType</code> vocabulary term.
   * @param value The value of the term.
   */
  private AssayReadoutType(String value) {
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
