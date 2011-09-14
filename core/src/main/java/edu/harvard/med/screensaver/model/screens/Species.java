// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/quickies/core/src/main/java/edu/harvard/med/screensaver/model/screens/ScreenType.java $
// $Id: ScreenType.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The screen type vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum Species implements VocabularyTerm
{

  // the vocabulary
  // Note: all are used for Small Molecule screens
  BACTERIA("bacteria"),
  C_ELEGANS("C. Elegans"),
  DROSOPHILIA("Drosophila"),
  HUMAN("human"),
  MOUSE("mouse"),
  YEAST("yeast"),
  ZEBRAFISH("zebrafish"),
  OTHER("Other");

  public static Species[] getRNAiSpecies()
  {
     return new Species[] { HUMAN, MOUSE, OTHER };
  }

  /**
   * A Hibernate <code>UserType</code> to map the {@link Species} vocabulary.
   */
  public static class UserType extends VocabularyUserType<Species>
  {
    public UserType()
    {
      super(Species.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>ScreenType</code> vocabulary term.
   * @param value The value of the term.
   */
  private Species(String value)
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
