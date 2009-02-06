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
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;


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

  // note: having the root 'screensaverUser' role allows:
  // 1) accounts to be activated and deactivated
  // 2) guest accounts to be created (i.e., with no other roles) 
  SCREENSAVER_USER("screensaverUser", "Screensaver User", "Basic role for users, admins, and guests that have login privileges to Screensaver.  The person may or may not be a user of the screening facility."),

  READ_EVERYTHING_ADMIN("readEverythingAdmin", "Read Everything Administrator", "Administrators that can view and search over data of all categories, except screen billing information."),
  LIBRARIES_ADMIN("librariesAdmin", "Libraries Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify libraries."),
  USERS_ADMIN("usersAdmin", "Users Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify user accounts that are not lab heads."),
  LAB_HEADS_ADMIN("labHeadsAdmin", "Lab Heads Administrator", USERS_ADMIN, "Administrators that can create and modify user accounts that are lab heads."),
  SCREENS_ADMIN("screensAdmin", "Screens Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify screens."),
  SCREEN_RESULTS_ADMIN("screenResultsAdmin", "Screen Results Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify screen results."),
  CHERRY_PICK_REQUESTS_ADMIN("cherryPickRequestsAdmin", "Cherry Pick Requests Administrator", READ_EVERYTHING_ADMIN, "Administrators that can create and modify cherry pick requests, including the generation of cherry pick plate mapping files, and the recording of cherry pick liquid transfers."),
  BILLING_ADMIN("billingAdmin", "Billing Information Administrator", SCREENS_ADMIN, "Administrators that can view, create, and modify billing information for a screen."),

  SCREENER("screener", "Screener", "Generic role for  users that are performing screens."),
  SMALL_MOLECULE_SCREENER("smallMoleculeScreener", "Small Molecule Screener", SCREENER, "Users that are conducting small molecule screens at the facility.'."),
  RNAI_SCREENER("rnaiScreener", "RNAi Screener", SCREENER, "Users that are conducting RNAi screens at the facility."),
  // note: nonScreeningUser is *not* mutually exclusive with screener roles; user may have been a nonScreeningUser initially, then became screener later on
  NON_SCREENER("nonScreeningUser", "Non-screening User", "Users that are using the facility for purposes other than conducting a screen."),
  MEDICINAL_CHEMIST_USER("medicinalChemistUser", "Medicinal Chemist User", "Users that are medicinal chemists."),

  // note: developers do not automatically get admin roles (other than readEverythingAdmin), allowing developers to restrict themselves from mutating data in production environments
  DEVELOPER("developer", "Developer", READ_EVERYTHING_ADMIN, "Special users that have permission to invoke development-related functionality and view low-level system information.")
  ;

  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link LibraryScreeningStatus} vocabulary.
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
