// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The method of quantification vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum MethodOfQuantification implements VocabularyTerm
{

  // the vocabulary
  
  RTPCR("RTPCR"),
  BRANCHED_DNA("Branched DNA"),
  WESTERN("Western"),
  NORTHERN("Northern"),
  IP_WESTERN("IP Western"),
  IMMUNOFLOURESCENCE("Immunoflourescence"),
  OTHER("Other")
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link MethodOfQuantification} vocabulary.
   */
  public static class UserType extends VocabularyUserType<MethodOfQuantification>
  {
    public UserType()
    {
      super(MethodOfQuantification.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>MethodOfQuantification</code> vocabulary term.
   * @param value The value of the term.
   */
  private MethodOfQuantification(String value)
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

  @Override
  public String toString()
  {
    return getValue();
  }
}
