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
 * The library type vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum LibraryType implements VocabularyTerm
{

  // the vocabulary
  
  COMMERCIAL("Commercial"),
  DOS("DOS"),
  ANNOTATION("Annotation"),
  DISCRETE("Discrete"),
  KNOWN_BIOACTIVES("Known Bioactives"),
  NCI("NCI"),
  NATURAL_PRODUCTS("Natural Products"),
  SIRNA("siRNA"),
  MIRNA_INHIBITOR("miRNA Inhibitor"),
  MIRNA_MIMIC("miRNA Mimic"),
  OTHER("Other")
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link LibraryType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<LibraryType>
  {
    public UserType()
    {
      super(LibraryType.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>LibraryType</code> vocabulary term.
   * @param value The value of the term.
   */
  private LibraryType(String value)
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
