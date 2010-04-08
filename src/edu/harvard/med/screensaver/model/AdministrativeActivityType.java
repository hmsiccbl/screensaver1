// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;

/**
 * The vocabulary of values for AdministrativeActivity types.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum AdministrativeActivityType implements VocabularyTerm
{

  // the vocabulary

  WELL_VOLUME_CORRECTION("Well Volume Correction", ScreensaverUserRole.LIBRARIES_ADMIN),
  WELL_DEPRECATION("Well Deprecation", ScreensaverUserRole.LIBRARIES_ADMIN), 
  PIN_TRANSFER_APPROVAL("Pin Transfer Approval", ScreensaverUserRole.SCREENS_ADMIN),
  LIBRARY_CONTENTS_LOADING("Library Contents Loading", ScreensaverUserRole.LAB_HEADS_ADMIN),
  LIBRARY_CONTENTS_VERSION_RELEASE("Library Contents Version Release", ScreensaverUserRole.LAB_HEADS_ADMIN),
  /** For general recording of changes made to the data in an entity (audit log) */
  ENTITY_UPDATE("Entity Update", ScreensaverUserRole.READ_EVERYTHING_ADMIN),
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
  private ScreensaverUserRole _editableByAdminRole;

  private AdministrativeActivityType(String value, ScreensaverUserRole editableByAdminRole)
  {
    _value = value;
    _editableByAdminRole = editableByAdminRole;
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


  public ScreensaverUserRole getEditableByRole()
  {
    return _editableByAdminRole;
  }
}
