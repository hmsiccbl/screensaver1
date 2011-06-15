// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import com.google.common.base.Predicate;

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
  SCREEN_RESULT_DATA_LOADING("Screen Result Data Loading", ScreensaverUserRole.SCREEN_RESULTS_ADMIN),
  SCREEN_RESULT_DATA_DELETION("Screen Result Data Deletion", ScreensaverUserRole.SCREEN_RESULTS_ADMIN),
  PLATE_LOCATION_TRANSFER("Plate Location Transfer", ScreensaverUserRole.LIBRARY_COPIES_ADMIN),
  PLATE_STATUS_UPDATE("Plate Status Update", ScreensaverUserRole.LIBRARY_COPIES_ADMIN),
  PLATE_VOLUME_CORRECTION("Plate Volume Correction", ScreensaverUserRole.LIBRARY_COPIES_ADMIN),
  PLATE_VOLUME_TRANSFER("Plate Volume Transfer", ScreensaverUserRole.LIBRARY_COPIES_ADMIN),
  /**
   * For general recording of changes made to the data in an entity (audit log). Comments should be used to record
   * old/new values or to explain why a change was made if it does not correspond to a real-world event (e.g. data
   * correction, database migration, etc.).
   */
  ENTITY_UPDATE("Entity Update", ScreensaverUserRole.READ_EVERYTHING_ADMIN),
  /**
   * For recording time-stamped comments on an entity (this is replacing entity-specific "comments"
   * properties). These types of comments are intended to be made more highly visible to administrators than comments
   * associated
   * with the other administrative activity types, which are intended more for data auditing purposes. Comments on this
   * activity type will usually provide information about the corresponding real-world entity.
   */
  COMMENT("Comment", ScreensaverUserRole.READ_EVERYTHING_ADMIN),
  /**
   * For recording manual overrides of lab cherry pick source copies.
   */
  LAB_CHERRY_PICK_SOURCE_COPY_OVERRIDE("Lab Cherry Pick Source Copy Override", ScreensaverUserRole.CHERRY_PICK_REQUESTS_ADMIN),
  ;

  public Predicate<AdministrativeActivity> isValuePredicate()
  {
    return new Predicate<AdministrativeActivity>() { 
      @Override
      public boolean apply(AdministrativeActivity activity)
      {
        return activity.getType() == AdministrativeActivityType.this;
      }
    };
  }

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
