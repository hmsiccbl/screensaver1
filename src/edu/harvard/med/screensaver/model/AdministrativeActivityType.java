// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/screens/StatusValue.java $
// $Id: StatusValue.java 2504 2008-06-18 14:56:20Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The vocabulary of values for AdministrativeActivity types.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum AdministrativeActivityType implements VocabularyTerm
{

  // the vocabulary

  WELL_VOLUME_CORRECTION("Well Volume Correction"),
  WELL_DEPRECATION("Well Deprecation"), 
  CHECKLIST_ITEM_EVENT("Checklist Item Event")
  ;


  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link AdministrativeActivityType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<AdministrativeActivityType>
  {
    public UserType()
    {
      super(AdministrativeActivityType.values());
    }
  }


  // private instance field and constructor

  private String _value;

  private AdministrativeActivityType(String value)
  {
    _value = value;
  }


  // public instance methods

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
