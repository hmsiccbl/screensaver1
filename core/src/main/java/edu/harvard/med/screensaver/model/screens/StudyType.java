// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The study type vocabulary.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum StudyType implements VocabularyTerm
{

  // the vocabulary

  IN_VITRO("In vitro"),
  IN_SILICO("In silico")
  ;


  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link StudyType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<StudyType>
  {
    public UserType()
    {
      super(StudyType.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>StudyType</code> vocabulary term.
   * @param value The value of the term.
   */
  private StudyType(String value)
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
