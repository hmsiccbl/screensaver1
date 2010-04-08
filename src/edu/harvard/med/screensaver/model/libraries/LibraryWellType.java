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
 * The library well type vocabulary.
 * 
 * @see edu.harvard.med.screensaver.model.screenresults.AssayWellControlType
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum LibraryWellType implements VocabularyTerm
{

  // the vocabulary
  
  UNDEFINED("<undefined>"), // for cases where library has been created, but library contents have not yet been loaded (or have been unloaded) 
  EXPERIMENTAL("experimental"),
  EMPTY("empty"), // for RNAi libraries, on assay, can become Assay Control, Assay Positive Control, Other (see AssayWellControlType)
  DMSO("DMSO"), // small molecule libraries only
  LIBRARY_CONTROL("library control"), // RNAi libraries only
  BUFFER("buffer")  // RNAi libraries only
  ;
  
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link LibraryWellType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<LibraryWellType>
  {
    public UserType()
    {
      super(LibraryWellType.values());
    }
  }

  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>LibraryWellType</code> vocabulary term.
   * @param value The value of the term.
   */
  private LibraryWellType(String value)
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
