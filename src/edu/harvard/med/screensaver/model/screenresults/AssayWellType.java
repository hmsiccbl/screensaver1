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
 * The assay well type vocabulary.  
 * 
 * @see edu.harvard.med.screensaver.model.libraries.LibraryWellType
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum AssayWellType implements VocabularyTerm
{

  // the vocabulary
  
  EXPERIMENTAL("experimental", "X"),
  EMPTY("empty", "E"),  
  LIBRARY_CONTROL("library control", "C"),
  ASSAY_POSITIVE_CONTROL("assay positive control", "P"),
  ASSAY_CONTROL("assay control", "N"), // aka "assay negative control", but stakeholders prefer simply "assay control"
  ASSAY_CONTROL_SHARED("assay control shared", "S"), //shared between screens
  BUFFER("buffer", "B"), // RNAi only
  DMSO("DMSO", "D"), // small molecule only
                     // note: as of 2007-01-31, screen result files don't use this; they use EMPTY for what should be DMSO wells
  OTHER("other", "O"),
  ;
 
  
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link AssayWellType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<AssayWellType>
  {
    public UserType()
    {
      super(AssayWellType.values());
    }
  }


  // private instance field and constructor

  private String _value;
  private String _abbreviation;

  /**
   * Constructs a <code>WellType</code> vocabulary term.
   * @param value The value of the term.
   */
  private AssayWellType(String value, String abbreviation)
  {
    _value = value;
    _abbreviation = abbreviation;
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
  
  public String getAbbreviation()
  {
    return _abbreviation;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }

  public boolean isDataProducing()
  {
    // TODO: I'm assuming wells of type "other" can contain data values --ant
    return this.equals(AssayWellType.EXPERIMENTAL) || isControl() || this.equals(AssayWellType.OTHER);
  }

  public boolean isControl()
  {
    return this.equals(AssayWellType.ASSAY_CONTROL) ||
    this.equals(AssayWellType.ASSAY_CONTROL_SHARED) ||
    this.equals(AssayWellType.ASSAY_POSITIVE_CONTROL) ||
    this.equals(AssayWellType.LIBRARY_CONTROL);
  }
}
