// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

/**
 * A person, usually a staff member of the screening facility, that can add and modify data in Screensaver, and has full
 * data viewing privileges.
 * 
 * @see ScreeningRoomUser
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

  /** @motivation for test code only */
  public AdministratorUser(String firstName,
                           String lastName)
  {
    super(firstName, lastName);
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

