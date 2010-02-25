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
  private static final long serialVersionUID = 0L;

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
   * Construct an uninitialized <code>AdministratorUser</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AdministratorUser() {}

  @Override
  protected boolean validateRole(ScreensaverUserRole role)
  {
    return role.isAdministrative() || role == ScreensaverUserRole.SCREENSAVER_USER;
  }
}

