// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.security.Principal;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.libraries.IsScreenable;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a screening room user role.
 * Also acts as JAAS {@link java.security.Principal}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum ScreensaverUserRole implements VocabularyTerm, Principal
{

  // the vocabulary

  DEVELOPER("developer", true, "Special users that have permission to invoke development-related functionality and view low-level system information."),
  READ_EVERYTHING_ADMIN("readEverythingAdmin", true, "Read-everything administrators will have the ability to view and search over data of all categories, except a screen\'s billing information. In addition, they will have the ability to generate various reports on screens."),
  LIBRARIES_ADMIN("librariesAdmin", true, "Administrators that can create and modify libraries."),
  USERS_ADMIN("usersAdmin", true, "Administrators that can create and modify user accounts."),
  SCREENS_ADMIN("screensAdmin", true, "Administrators that can create and modify screens."),
  SCREEN_RESULTS_ADMIN("screenResultsAdmin", true, "Administrators that can create and modify screen results."),
  CHERRY_PICK_ADMIN("cherryPickAdmin", true, "Administrators that can create and modify cherry pick requests, including the generation of cherry pick plate mapping files, and the recording of cherry pick liquid transfers."),
  BILLING_ADMIN("billingAdmin", true, "Administrators that can view, create, and modify billing information for a screen."),
  SCREENING_ROOM_USER("screeningRoomUser", false, "Users that have permission to view and search over non-administrative information for certain data records."),
  COMPOUND_SCREENING_ROOM_USER("compoundScreeningRoomUser", false, "Users that have permission to view and search over non-administrative information for all compound screens and any compound screen results which are demarked \'shareable\'."),
  RNAI_SCREENING_ROOM_USER("rnaiScreeningRoomUser", false, "Users that have permission to view and search over non-administrative information for all RNAi screens."),
  MEDICINAL_CHEMIST_USER("medicinalChemistUser", false, "Users that are medicinal chemists."),
  GUEST_USER("guestUser", false, "External users that have permission to view our libraries and studies.");
  ;

  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link IsScreenable} vocabulary.
   */
  public static class UserType extends VocabularyUserType<ScreensaverUserRole>
  {
    public UserType()
    {
      super(ScreensaverUserRole.values());
    }
  }


  // static fields

  private static final Logger log = Logger.getLogger(ScreensaverUserRole.class);


  // private instance field and constructor

  private String _roleName;
  private boolean _isAdministrative;
  private String _comment;

  /**
   * Constructs a <code>ScreensaverUserRole</code> vocabulary term.
   * @param value The value of the term.
   */
  private ScreensaverUserRole(String roleName, boolean isAdministrative, String comment)
  {
    _roleName = roleName;
    _comment = comment;
    _isAdministrative = isAdministrative;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _roleName;
  }

  public String getRoleName()
  {
    return _roleName;
  }

  public String getComment()
  {
    return _comment;
  }

  @Override
  public String toString()
  {
    return getValue();
  }

  // Principal interface methods

  /**
   * Get the user Principal name.
   */
  public String getName()
  {
    return getValue();
  }

  public boolean isAdministrative()
  {
    return _isAdministrative;
  }
}
