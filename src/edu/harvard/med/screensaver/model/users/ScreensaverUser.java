// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.util.CryptoUtils;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a Screensaver user. A Screensaver user
 * may be an {@link AdministratorUser} and/or a {@link ScreeningRoomUser}. 
 * Also acts as JAAS {@link java.security.Principal}.
 * <p>
 * This parent "user" class supports multiple forms of login IDs. The
 * <code>loginID</code> property is a Screensaver-managed login ID, whereas
 * the <code>eCommonsID</code> and <code>harvardID</code> properties are
 * managed at higher organizational levels, and the passwords for these latter
 * login IDs are <i<not</i> stored by Screensaver (authentication via these
 * IDs will require use of a network authentication service).
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false"
 */
public class ScreensaverUser extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ScreensaverUser.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _screensaverUserId;
  private Integer _version;
  private Date _dateCreated;
  private String _firstName;
  private String _lastName;
  private String _email;
  private String _phone;
  private String _mailingAddress;
  private String _comments;
  private Set<ScreensaverUserRole> _roles = new HashSet<ScreensaverUserRole>();
  private String _loginId;
  private String _digestedPassword;
  private String _eCommonsId;
  private String _harvardId;


  // public constructors

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
   */
  public ScreensaverUser(
    String firstName,
    String lastName,
    String email)
  {
    setDateCreated(new Date());
    setFirstName(firstName);
    setLastName(lastName);
    setEmail(email);
  }

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
   */
  public ScreensaverUser(
    Date dateCreated,
    String firstName,
    String lastName,
    String email,
    String phone,
    String mailingAddress,
    String comments)
  {
    setDateCreated(dateCreated);
    setFirstName(firstName);
    setLastName(lastName);
    setEmail(email);
    setPhone(phone);
    setMailingAddress(mailingAddress);
    setComments(comments);
  }

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
   */
  public ScreensaverUser(
    String firstName,
    String lastName,
    String email,
    String phone,
    String mailingAddress,
    String comments)
  {
    this(new Date(),
         firstName,
         lastName,
         email,
         phone,
         mailingAddress,
         comments);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getScreensaverUserId();
  }

  /**
   * Get the id for the Screensaver user.
   *
   * @return the id for the Screensaver user
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screensaver_user_id_seq"
   */
  public Integer getScreensaverUserId()
  {
    return _screensaverUserId;
  }

  /**
   * Get the date created.
   *
   * @return the date created
   * @hibernate.property
   *   not-null="true"
   */
  public Date getDateCreated()
  {
    return _dateCreated;
  }

  /**
   * Set the date created.
   *
   * @param dateCreated the new date created
   */
  public void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated);
  }

  /**
   * Get the first name.
   *
   * @return the first name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getFirstName()
  {
    return _firstName;
  }

  /**
   * Set the first name.
   *
   * @param firstName the new first name
   */
  public void setFirstName(String firstName)
  {
    _firstName = firstName;
  }

  /**
   * Get the last name.
   *
   * @return the last name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getLastName()
  {
    return _lastName;
  }

  /**
   * Set the last name.
   *
   * @param lastName the new last name
   */
  public void setLastName(String lastName)
  {
    _lastName = lastName;
  }
  
  /**
   * Get the full name (last, first).
   * @return the full name
   */
  public String generateFullName()
  {
    return _lastName + ", " + _firstName;
  }

  /**
   * Get the email.
   *
   * @return the email
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
  public String getEmail()
  {
    return _email;
  }

  /**
   * Set the email.
   *
   * @param email the new email
   */
  public void setEmail(String email)
  {
    _email = email;
  }
  
  /**
   * Get the phone.
   *
   * @return the phone
   * @hibernate.property
   *   type="text"
   */
  public String getPhone()
  {
    return _phone;
  }

  /**
   * Set the phone.
   *
   * @param phone the new phone
   */
  public void setPhone(String phone)
  {
    _phone = phone;
  }

  /**
   * Get the mailing address.
   *
   * @return the mailing address
   * @hibernate.property
   *   type="text"
   */
  public String getMailingAddress()
  {
    return _mailingAddress;
  }

  /**
   * Set the mailing address.
   *
   * @param mailingAddress the new mailing address
   */
  public void setMailingAddress(String mailingAddress)
  {
    _mailingAddress = mailingAddress;
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
   * Get an unmodifiable copy of the set of user roles that this user belongs to.
   * 
   * @return an unmodifiable copy of the set of user roles that this user belongs to
   *
   * @hibernate.set
   *   order-by="screensaver_user_role"
   *   table="screensaver_user_role_type"
   *   cascade="delete"
   *   lazy="false"
   * @hibernate.collection-key
   *   column="screensaver_user_id"
   *   foreign-key="fk_screensaver_user_role_type_to_screensaver_user"
   * @hibernate.collection-element
   *   type="edu.harvard.med.screensaver.model.users.ScreensaverUserRole$UserType"
   *   column="screensaver_user_role"
   *   not-null="true"
   */
  public Set<ScreensaverUserRole> getScreensaverUserRoles()
  {
    return _roles;
  }
  
  /**
   * Add a role to this user (i.e., place the user into a new role).
   *
   * @param role the role to add
   * @return true iff the user was not already added to this role
   */
  public boolean addScreensaverUserRole(ScreensaverUserRole role)
  {
    return _roles.add(role);
  }
  
  /**
   * Remove this user from a role.
   * @param well the role to remove this user from
   * @return     true iff the user previously belonged to the role
   */
  public boolean removeScreensaverUserRole(ScreensaverUserRole role)
  {
    return _roles.remove(role);
  }
  
  /**
   * Get the user's Screensaver-managed login ID.
   * 
   * @return the Screensaver login ID
   * @hibernate.property type="text"
   */
  public String getLoginId()
  {
    return _loginId;
  }

  /**
   * Set the user's Screensaver-managed login ID.
   *
   * @param loginID the new Screensaver login ID
   */
  public void setLoginId(String loginId)
  {
    _loginId = loginId;
  }

  /**
   * Get the digested (hashed) password.
   * @return the digested (hashed) password
   * 
   * @hibernate.property type="text"
   */
  public String getDigestedPassword()
  {
    return _digestedPassword;
  }

  /**
   * Set the digested (hashed) version of the password associated with the user's login ID.
   * @param screensaverDigestedPassword
   */
  public void setDigestedPassword(String screensaverDigestedPassword)
  {
    _digestedPassword = screensaverDigestedPassword;
  }

  /**
   * Set the password associated with the user's login ID, specified as a
   * plaintext password, but which will be digested (hashed) before being
   * stored, for security purposes.
   * 
   * @param screensaverPassword the plaintext password
   */
  public void updateScreensaverPassword(String screensaverPassword)
  {
    String digestedPassword = CryptoUtils.digest(screensaverPassword);
    if (digestedPassword != null) {
      setDigestedPassword(digestedPassword);
    }
    else {
      log.error("could not set new password for ScreensaverUser " + this);
    }
  }

  /**
   * Get the eCommons ID.
   *
   * @return the eCommons ID
   * @hibernate.property
   *   type="text"
   */
  public String getECommonsId()
  {
    return _eCommonsId;
  }

  /**
   * Set the eCommons ID.
   *
   * @param eCommonsId the new eCommons ID
   */
  public void setECommonsId(String eCommonsId)
  {
    _eCommonsId = eCommonsId;
  }

  /**
   * Get the harvard id.
   *
   * @return the harvard id
   * @hibernate.property
   *   type="text"
   */
  public String getHarvardId()
  {
    return _harvardId;
  }

  /**
   * Set the harvard id.
   *
   * @param harvardId the new harvard id
   */
  public void setHarvardId(String harvardId)
  {
    _harvardId = harvardId;
  }


  // package methods

  @Override
  protected Object getBusinessKey()
  {
    return getEmail();
  }

  
  // protected constructor

  /**
   * Construct an uninitialized <code>ScreeningRoomUser</code> object.
   *
   * @motivation for hibernate
   */
  protected ScreensaverUser() {}


  // private methods

  /**
   * Set the id for the Screensaver user.
   *
   * @param screeningRoomUserId the new id for the Screensaver user
   * @motivation for hibernate
   */
  private void setScreensaverUserId(Integer screensaverUserId) 
  {
    _screensaverUserId = screensaverUserId;
  }
  
  /**
   * Get the version for the Screensaver user.
   *
   * @return the version for the Screensaver user
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the Screensaver user.
   *
   * @param version the new version for the Screensaver user
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the screensaver user roles.
   *
   * @param roles the new screensaver user roles
   * @motivation for hibernate
   */
  private void setScreensaverUserRoles(Set<ScreensaverUserRole> roles)
  {
    _roles = roles;
  }

  
}