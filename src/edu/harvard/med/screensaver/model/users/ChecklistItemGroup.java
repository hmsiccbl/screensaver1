// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/screens/StatusValue.java $
// $Id: StatusValue.java 2598 2008-07-23 10:41:17Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The vocabulary of values for ChecklistItemGroup.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum ChecklistItemGroup implements VocabularyTerm
{

  // the vocabulary
  
  MAILING_LISTS_AND_WIKIS("Mailing Lists & Wikis"),
  FORMS("Forms"),
  NON_HARVARD_SCREENERS("Non-Harvard Screeners"),
  IMAGING("Imaging"),
  LEGACY("Legacy");


  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link ChecklistItemGroup} vocabulary.
   */
  public static class UserType extends VocabularyUserType<ChecklistItemGroup>
  {
    public UserType()
    {
      super(ChecklistItemGroup.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>StatusValue</code> vocabulary term.
   * @param value The value of the term.
   */
  private ChecklistItemGroup(String value)
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
