// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/libraries/TemplateType.java $
// $Id: TemplateType.java 388 2006-07-31 21:14:40Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

// TODO: declare appropriate package
package edu.harvard.med.screensaver.model.TEMPLATE_SUBPACKAGE;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The silencing reagent type vocabulary.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum TemplateType implements VocabularyTerm
{

  // the vocabulary
  
  VALUE1("value1"),
  VALUE2("value2"),
  // ...
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link TemplateType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<TemplateType>
  {
    public UserType()
    {
      super(TemplateType.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>TemplateType</code> vocabulary term.
   * @param value The value of the term.
   */
  private TemplateType(String value)
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
