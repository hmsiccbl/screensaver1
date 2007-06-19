// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

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
}
