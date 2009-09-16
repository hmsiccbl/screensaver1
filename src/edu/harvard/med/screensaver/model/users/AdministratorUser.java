// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;


/**
 * A Hibernate entity bean representing an Administrator user.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="screensaverUserId")
@org.hibernate.annotations.ForeignKey(name="fk_administrator_user_to_screensaver_user")
@org.hibernate.annotations.Proxy
public class AdministratorUser extends ScreensaverUser
{

  // static fields

  static final Logger log = Logger.getLogger(AdministratorUser.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Set<AdministrativeActivity> _activitiesApproved = new HashSet<AdministrativeActivity>();


  // public constructor and instance methods

  /**
   * Constructs an initialized <code>ScreensaverUser</code> object.
   *
   * @param firstName the first name
   * @param lastName the last name
   * @param email the email
   * @param phone the phone number
   * @param mailingAddress the mailing address
   * @param comments the comments
   * @param loginId the Screensaver-managed login ID of the user
   * @param password the password associated with the screensaverId login ID of the user (plaintext)
   */
  public AdministratorUser(
    String firstName,
    String lastName,
    String email,
    String phone,
    String mailingAddress,
    String comments,
    String loginId,
    String password)
  {
    super(firstName,
          lastName,
          email,
          phone,
          mailingAddress,
          comments);
    setLoginId(loginId);
    updateScreensaverPassword(password);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the set of activities approved by this user.
   * @return the activities approved
   */
  @OneToMany(
    mappedBy="approvedBy",
    fetch=FetchType.LAZY
  )
  @OrderBy("dateApproved")
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="activityApproved")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Set<AdministrativeActivity> getActivitiesApproved()
  {
    return _activitiesApproved;
  }

  /**
   * Add an activity that was approved by this user.
   * @param activityApproved the new activity that was approved by this user
   * @return true iff the user did not already approve the activity
   */
  public boolean addActivityApproved(AdministrativeActivity activityApproved)
  {
    if (_activitiesApproved.add(activityApproved)) {
      activityApproved.setApprovedBy(this);
      return true;
    }
    return false;
  }


  // protected constructor and instance methods

  /**
   * Construct an uninitialized <code>AdministratorUser</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AdministratorUser() {}

  @Override
  protected boolean validateRole(ScreensaverUserRole role)
  {
    return role.isAdministrative() || role == ScreensaverUserRole.SCREENSAVER_USER;
  }


  // private constructor and instance methods

/**
   * Get the set of activities approved by this user.
   * @return the activities approved
   * @motivation for hibernate
   */
  private void setActivitiesApproved(Set<AdministrativeActivity> activitiesApproved)
  {
    _activitiesApproved = activitiesApproved;
  }
}

