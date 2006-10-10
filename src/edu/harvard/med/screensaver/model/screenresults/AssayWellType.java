// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/libraries/TemplateType.java $
// $Id: TemplateType.java 388 2006-07-31 21:14:40Z js163 $
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
 * @see edu.harvard.med.screensaver.model.libraries.WellType
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum AssayWellType implements VocabularyTerm
{

  // the vocabulary
  
  EXPERIMENTAL("experimental"),
  EMPTY("empty"),
  LIBRARY_CONTROL("library control"),
  ASSAY_POSITIVE_CONTROL("assay positive control"),
  ASSAY_NEGATIVE_CONTROL("assay negative control"),
  BUFFER("buffer"),
  OTHER("other"),
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

  /**
   * Constructs a <code>WellType</code> vocabulary term.
   * @param value The value of the term.
   */
  private AssayWellType(String value)
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
