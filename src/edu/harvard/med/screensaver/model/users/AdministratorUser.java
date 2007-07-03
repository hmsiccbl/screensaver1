// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.CollectionElementName;
import edu.harvard.med.screensaver.model.ToManyRelationship;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing an Administrator user.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.joined-subclass table="administrator_user" lazy="false"
 * @hibernate.joined-subclass-key column="screensaver_user_id"
 */
public class AdministratorUser extends ScreensaverUser
{
  
  // static fields

  static final Logger log = Logger.getLogger(AdministratorUser.class);
  private static final long serialVersionUID = 0L;


  // instance fields
  
  private Set<AdministrativeActivity> _activitiesApproved = new HashSet<AdministrativeActivity>();

  /**
   * Constructs an initialized <code>ScreensaverUser</code> object.
   *
   * @param dateCreated the date created
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

  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get an unmodifiable copy of the set of activities approved by this user.
   *
   * @return the activities approved
   */
  @ToManyRelationship(inverseProperty="approvedBy")
  @CollectionElementName("activityApproved")
  public Set<AdministrativeActivity> getActivitiesApproved()
  {
    return Collections.unmodifiableSet(_activitiesApproved);
  }

  /**
   * Add an activity that was approved by this user.
   *
   * @param activityApproved the new activity that was approved by this user
   * @return true iff the user did not already approve the activity
   */
  public boolean addAdministrativeActivityApproved(AdministrativeActivity activityApproved)
  {
    if (getHbnActivitiesApproved().add(activityApproved)) {
      activityApproved.setApprovedBy(this);
      return true;
    }
    return false;
  }

  /**
   * Get the activities approved by this administrator.
   *
   * @return the activities approved by this administrator
   * @hibernate.set
   *   cascade="none"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="approved_by_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.AdministrativeActivity"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public Set<AdministrativeActivity> getHbnActivitiesApproved()
  {
    return _activitiesApproved;
  }


  // protected methods

  protected boolean validateRole(ScreensaverUserRole role) 
  {
    // TODO: reinstate once production database has been corrected: some admins have *User roles  
    //return role.isAdministrative();
    return true;
  }
  
  
  // private methods

  /**
   * Construct an uninitialized <code>AdministratorUser</code> object.
   *
   * @motivation for hibernate
   */
  private AdministratorUser() {}
  
  /**
   * Set the activities approved by this user.
   *
   * @param activitiesApproved the screening room activities approved by this user
   * @motivation for hibernate
   */
  private void setHbnActivitiesApproved(Set<AdministrativeActivity> activitiesApproved)
  {
    _activitiesApproved = activitiesApproved;
  }
  
}
