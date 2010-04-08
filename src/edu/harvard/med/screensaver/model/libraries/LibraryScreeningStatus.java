// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The is screenable vocabulary.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum LibraryScreeningStatus implements VocabularyTerm
{

  // the vocabulary

  // library is not available for screening because it has not yet been plated
  NOT_YET_PLATED("Not Yet Plated"),
  // library is not available for screening, by edict of the screening facility
  NOT_ALLOWED("Not Allowed"),
  // library is not available for screening, because it has been retired
  RETIRED("Retired"),
  // library is available for screening, but only if the screen has been explicitly authorized by the facility
  REQUIRES_PERMISSION("Requires Permission"),
  // library is available for screening, but the facility does not recommend its use
  NOT_RECOMMENDED("Not Recommended"),
  // library is available for screening, unconditionally
  ALLOWED("Allowed"),
  ;


  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link LibraryScreeningStatus} vocabulary.
   */
  public static class UserType extends VocabularyUserType<LibraryScreeningStatus>
  {
    public UserType()
    {
      super(LibraryScreeningStatus.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>IsScreenable</code> vocabulary term.
   * @param value The value of the term.
   */
  private LibraryScreeningStatus(String value)
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
