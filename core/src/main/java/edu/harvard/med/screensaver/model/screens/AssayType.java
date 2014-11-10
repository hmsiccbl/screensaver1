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
 * The Assay type vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum AssayType implements VocabularyTerm
{

  // the vocabulary
  // Note: all are used for Small Molecule screens
  BIOCHEMICAL("Biochemical"),
  CELL_BASED("Cell-based"),
  MULTI_CELLULAR("Multi-cellular organism");

  /**
   * A Hibernate <code>UserType</code> to map the {@link AssayType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<AssayType>
  {
    public UserType()
    {
      super(AssayType.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>AssayType</code> vocabulary term.
   * @param value The value of the term.
   */
  private AssayType(String value)
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
