// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The (stock plate) well type vocabulary.
 * 
 * @see edu.harvard.med.screensaver.model.screenresults.AssayWellType
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum WellType implements VocabularyTerm
{

  // the vocabulary
  
  EXPERIMENTAL("experimental"),
  EMPTY("empty"), // for RNAi, on assay, can become Assay Control, Assay Positive Control, Other
  DMSO("DMSO"), // small compound only
  LIBRARY_CONTROL("library control"), // RNAi only
  BUFFER("buffer"), // RNAi only
//  OTHER("other"),
  ;

  
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link WellType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<WellType>
  {
    public UserType()
    {
      super(WellType.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>WellType</code> vocabulary term.
   * @param value The value of the term.
   */
  private WellType(String value)
  {
    _value = value;
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }
}
