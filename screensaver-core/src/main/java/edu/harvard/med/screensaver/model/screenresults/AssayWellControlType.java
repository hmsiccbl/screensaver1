// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
public enum AssayWellControlType implements VocabularyTerm
{
  ASSAY_POSITIVE_CONTROL("assay positive control", "P"),
  ASSAY_CONTROL("assay control", "N"), // aka "assay negative control", but stakeholders prefer simply "assay control"
  ASSAY_CONTROL_SHARED("assay control shared", "S"), // shared between screens
  OTHER_CONTROL("other", "O"),
  ;
 

  /**
   * A Hibernate <code>UserType</code> to map the {@link AssayWellControlType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<AssayWellControlType>
  {
    public UserType()
    {
      super(AssayWellControlType.values());
    }
  }

  
  private String _value;
  private String _abbreviation;

  /**
   * Constructs a <code>WellType</code> vocabulary term.
   * @param value The value of the term.
   */
  private AssayWellControlType(String value, String abbreviation)
  {
    _value = value;
    _abbreviation = abbreviation;
  }

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

  @Override
  public String toString()
  {
    return getValue();
  }
}
