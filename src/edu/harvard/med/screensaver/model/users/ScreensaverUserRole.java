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
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.libraries.IsScreenable;


/**
 * Defines the roles that can be assigned to a ScreensaverUser, controlling the
 * privileges granted to that user. Also acts as JAAS
 * {@link java.security.Principal}.
 * <p>
 * Roles can form a hierarchy, in that granting a given role can imply the
 * granting of more fundamental roles. ScreensaverUserRole merely provides
 * information about this hierarchy via {@link #getImpliedRole()} and
 * {@link #getImpliedRole()}, but does not otherwise enforce that implied roles
 * have been explicitly granted to a user.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum ScreensaverUserRole implements VocabularyTerm, Principal
{

  // the vocabulary

  READ_EVERYTHING_ADMIN("readEverythingAdmin", "Read Everything Administrator", "Administrators that can view and search over data of all categories, except screen billing information."),
  LIBRARIES_ADMIN("librariesAdmin", "Libraries Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify libraries."),
  USERS_ADMIN("usersAdmin", "Users Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify user accounts that are not classified as a Principle Investigator."),
  PRINCIPAL_INVESTIGATORS_ADMIN("principalInvestigatorsAdmin", "Principal Investigators Administrator", USERS_ADMIN, "Administrators that can create and modify user accounts that are classified as a Principle Investigator."),
  SCREENS_ADMIN("screensAdmin", "Screens Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify screens."),
  SCREEN_RESULTS_ADMIN("screenResultsAdmin", "Screen Results Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify screen results."),
  CHERRY_PICK_REQUESTS_ADMIN("cherryPickRequestsAdmin", "Cherry Pick Requests Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify cherry pick requests, including the generation of cherry pick plate mapping files, and the recording of cherry pick liquid transfers."),
  BILLING_ADMIN("billingAdmin", "Billing Information Administrator", SCREENS_ADMIN, "Administrators that can view, create, and modify billing information for a screen."),
  SCREENING_ROOM_USER("screeningRoomUser", "Screening Room User", "Users that have permission to view libraries and studies, but not screen information."),
  SMALL_MOLECULE_SCREENING_ROOM_USER("smallMoleculeScreeningRoomUser", "Small Molecule Screening Room User", SCREENING_ROOM_USER, "Users that have permission to view and search over non-administrative information for all small molecule screens, and associated public screen results'."),
  RNAI_SCREENING_ROOM_USER("rnaiScreeningRoomUser", "RNAi Screening Room User", SCREENING_ROOM_USER, "Users that have permission to view and search over non-administrative information for all RNAi screens, and associated public screen results"),
  MEDICINAL_CHEMIST_USER("medicinalChemistUser", "Medicinal Chemist User", SCREENING_ROOM_USER, "Users that are medicinal chemists."),
  DEVELOPER("developer", "Developer", READ_EVERYTHING_ADMIN, "Special users that have permission to invoke development-related functionality and view low-level system information.")
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


  // private instance field and constructor

  private String _roleName;
  private String _displayableRoleName;
  private boolean _isAdministrative;
  private String _comment;
  private ScreensaverUserRole _impliedRole;
  private List<ScreensaverUserRole> _impliedRoles;


  private ScreensaverUserRole(String roleName,
                              String displayableRoleName,
                              ScreensaverUserRole impliedRole,
                              String comment)
  {
    _roleName = roleName;
    _displayableRoleName = displayableRoleName;
    _comment = comment;
    _impliedRole = impliedRole;
    _impliedRoles = calcImpliedRoles();
  }

  private ScreensaverUserRole(String roleName,
                              String displayableRoleName,
                              String comment)
  {
    this(roleName, displayableRoleName, null, comment);
  }

  private List<ScreensaverUserRole> calcImpliedRoles()
  {
    List<ScreensaverUserRole> impliedRoles = new ArrayList<ScreensaverUserRole>();
    ScreensaverUserRole impliedRole = getImpliedRole();
    while (impliedRole != null && !impliedRoles.contains(impliedRole)) {
      impliedRoles.add(impliedRole);
      impliedRole = impliedRole.getImpliedRole();
    }
    return impliedRoles;
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

  public String getDisplayableRoleName()
  {
    return _displayableRoleName;
  }

  public String getComment()
  {
    return _comment;
  }

  public ScreensaverUserRole getImpliedRole()
  {
    return _impliedRole;
  }

  public List<ScreensaverUserRole> getImpliedRoles()
  {
    return _impliedRoles;
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
    return this == READ_EVERYTHING_ADMIN || _impliedRoles.contains(READ_EVERYTHING_ADMIN);
  }
}
