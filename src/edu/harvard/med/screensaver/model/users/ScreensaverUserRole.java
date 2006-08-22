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

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a screening room user role.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false"
 */
public class ScreensaverUserRole extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ScreeningRoomUser.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _screensaverUserRoleId;
  private Integer _version;
  private String _roleName;
  private String _comments;
  private Set<ScreensaverUser> _usersInRole = new HashSet<ScreensaverUser>();


  // public constructors

  /**
   * Constructs an initialized <code>ScreensaveUserRole</code> object.
   *
   * @param roleName the role name
   * @param comments the comments
   */
  public ScreensaverUserRole(
    String roleName,
    String comments)
  {
    setRoleName(roleName);
    setComments(comments);
  }

  
  // public methods

  @Override
  public Integer getEntityId()
  {
    return getScreensaverUserRoleId();
  }

  /**
   * Get the id for the Screensaver user role.
   *
   * @return the id for the Screensaver user role
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screensaver_user_role_id_seq"
   */
  public Integer getScreensaverUserRoleId()
  {
    return _screensaverUserRoleId;
  }
  
  /**
   * Get the role name.
   *
   * @return the role name
   * @hibernate.property
   *   type="text"
   */
  public String getRoleName()
  {
    return _roleName;
  }

  /**
   * Set the role name.
   *
   * @param roleName the new role name
   */
  public void setRoleName(String roleName)
  {
    _roleName = roleName;
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get an unmodifiable copy of the set of users that belong to this role.
   *
   * @return the users that belong to this role
   */
  public Set<ScreensaverUser> getScreensaverUsers()
  {
    return Collections.unmodifiableSet(getHbnScreensaverUsers());
  }

  /**
   * Add the user to this role.
   * 
   * @param user the user to add
   * @return true iff the user was not already in the role
   */
  public boolean addScreensaverUser(ScreensaverUser user) {
    assert !(getHbnScreensaverUsers().contains(user) ^ user.getHbnScreensaverUserRoles()
      .contains(this)) : "asymmetric user/role association encountered";
    if (getHbnScreensaverUsers().add(user)) {
      return user.getHbnScreensaverUserRoles().add(this);
    }
    return false;
  }

  /**
   * Remove the user from this role.
   * 
   * @param user the user to remove
   * @return true iff the user was previously in the role
   */
  public boolean removeScreensaverUser(ScreensaverUser user) {
    assert !(getHbnScreensaverUsers().contains(user) ^ user.getHbnScreensaverUserRoles()
      .contains(this)) : "asymmetric user/role association encountered";
    if (getHbnScreensaverUsers().remove(user)) {
      return user.getHbnScreensaverUserRoles().remove(this);
    }
    return false;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return getRoleName();
  }
  
  
  // package methods

  /**
   * Get the modifiable set of users contained in the role. If the caller
   * modifies the returned collection, it must ensure that the bi-directional
   * relationship is maintained by updating the related {@link ScreensaverUser}
   * bean(s).
   * 
   * @return the set of users contained in the role
   * @motivation for Hibernate and for associated {@link ScreensaverUser} bean
   *             (so that it can maintain the bi-directional association between
   *             {@link ScreensaverUser} and {@link ScreensaverUserRole}).
   * @hibernate.set table="role_user_link" cascade="all"
   * @hibernate.collection-key column="screensaver_user_role_id"
   * @hibernate.collection-many-to-many column="screensaver_user_id"
   *                                    class="edu.harvard.med.screensaver.model.users.ScreensaverUser"
   *                                    foreign-key="fk_role_user_link_to_role"
   */
  Set<ScreensaverUser> getHbnScreensaverUsers()
  {
    return _usersInRole;
  }
  
  
  // private constructor

  /**
   * Construct an uninitialized <code>ScreensaverUserRole</code> object.
   *
   * @motivation for hibernate
   */
  private ScreensaverUserRole() {}


  // private methods

  /**
   * Set the id for the user role.
   *
   * @param screensaverUserRoleId the new id for the user role
   * @motivation for hibernate
   */
  private void setScreensaverUserRoleId(Integer screensaverUesrRoleId) {
    _screensaverUserRoleId = screensaverUesrRoleId;
  }

  /**
   * Get the version for the user role.
   *
   * @return the version for the user role
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the user role.
   *
   * @param version the new version for the user role
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
  
  /**
   * Set the set of users contained in the role.
   * 
   * @param users the new set of users contained in the role
   * @motivation for hibernate
   */
  private void setHbnScreensaverUsers(Set<ScreensaverUser> usersInRole)
  {
    _usersInRole = usersInRole;
  }

}
