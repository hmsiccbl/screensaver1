// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.util.CryptoUtils;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * A person that is a user of the screening facility and/or Screensaver itself. A Screensaver user
 * may be an {@link AdministratorUser} and/or a {@link ScreeningRoomUser}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@org.hibernate.annotations.Proxy
abstract public class ScreensaverUser extends AuditedAbstractEntity<Integer> implements Principal, Comparable<ScreensaverUser>
{

  // static fields

  private static final Logger log = Logger.getLogger(ScreensaverUser.class);
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<ScreensaverUser> labAffiliation = RelationshipPath.from(ScreensaverUser.class).to("labAffiliation"); 
  public static final RelationshipPath<ScreensaverUser> activitiesPerformed = RelationshipPath.from(ScreensaverUser.class).to("activitiesPerformed"); 
  public static final RelationshipPath<ScreensaverUser> updateActivities = RelationshipPath.from(ScreensaverUser.class).to("updateActivities"); 
  public static final PropertyPath<ScreensaverUser> roles = RelationshipPath.from(ScreensaverUser.class).toCollectionOfValues("screensaverUserRoles");

  public static final Function<ScreensaverUser,String> ToDisplayStringFunction = new Function<ScreensaverUser,String>() { public String apply(ScreensaverUser u) { return u.getFullNameFirstLast() + " (" + u.getEntityId() + ")"; } };
  public static final Function<ScreensaverUser,String> ToFullNameLastFirstAndId = new Function<ScreensaverUser,String>() {
    public String apply(ScreensaverUser u)
    {
      return u.getFullNameLastFirst() + " (" + u.getEntityId() + ")";
    }
  };


  // instance fields

  private Integer _version;
  private transient HashMap<String,Boolean> _rolesMap;
  private SortedSet<Activity> _activitiesPerformed = Sets.newTreeSet();
  private String _firstName;
  private String _lastName;
  private String _email;
  private String _phone;
  private String _mailingAddress;
  private String _comments;
  private Set<ScreensaverUserRole> _roles = Sets.newHashSet();
  private Set<ScreensaverUserRole> _primaryRoles;
  private String _loginId;
  private String _digestedPassword;
  private String _eCommonsId;
  private String _harvardId;
  private LocalDate _harvardIdExpirationDate;
  private LocalDate _harvardIdRequestedExpirationDate;
  
  private Gender _gender;
  // public constructors
  
  /**
   * @motivation for new ScreensaverUser creation via user interface, where even required
   *             fields are allowed to be uninitialized, initially
   */
  protected ScreensaverUser(AdministratorUser createdBy)
  {
    super(createdBy);
  }

  /** @motivation for test code only */
  protected ScreensaverUser(String firstName,
                            String lastName)
  {
    super(null); /* TODO */
    setFirstName(firstName);
    setLastName(lastName);
  }

  
  // public instance methods

  /**
   * Get the id for the Screensaver user.
   * @return the id for the Screensaver user
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="screensaver_user_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="screensaver_user_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="screensaver_user_id_seq")
  public Integer getScreensaverUserId()
  {
    return getEntityId();
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="screensaverUserUpdateActivity", 
             joinColumns=@JoinColumn(name="screensaverUserId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  @Sort(type=SortType.NATURAL)            
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  /**
   * Get the set of user roles that this user belongs to. Do not modify the
   * contents of the returned collection: use
   * {@link #addScreensaverUserRole(ScreensaverUserRole)} or
   * {@link #removeScreensaverUserRole(ScreensaverUserRole)} instead.
   * 
   * @return the set of user roles that this user belongs to
   */
  @ElementCollection
  @edu.harvard.med.screensaver.model.annotations.ElementCollection(hasNonconventionalMutation = true /*
                                                                                                      * valid roles
                                                                                                      * depend upon
                                                                                                      * concrete entity
                                                                                                      * type
                                                                                                      */)
  @Column(name = "screensaverUserRole", nullable = false)
  @JoinTable(name = "screensaverUserRole", joinColumns = @JoinColumn(name = "screensaverUserId"))
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.users.ScreensaverUserRole$UserType")
  @org.hibernate.annotations.ForeignKey(name = "fk_screensaver_user_role_type_to_screensaver_user")
  public Set<ScreensaverUserRole> getScreensaverUserRoles()
  {
    return _roles;
  }
  
  /**
   * @return the subset of this user's roles that are not implied by one of the extant roles
   */
  @Transient
  public Set<ScreensaverUserRole> getPrimaryScreensaverUserRoles()
  {
    _primaryRoles = Sets.difference(getScreensaverUserRoles(), getImpliedRoles());
    return ImmutableSet.copyOf(_primaryRoles);
  }

  @Transient
  public Set<ScreensaverUserRole> getImpliedRoles()
  {
    Set<ScreensaverUserRole> impliedRoles = Sets.newHashSet();
    for (ScreensaverUserRole role : getScreensaverUserRoles()) {
      impliedRoles.addAll(role.getImpliedRoles());
    }
    return impliedRoles;
  }

  /**
   * Add a role to this user (i.e., place the user into a new role).
   * @param role the role to add
   * @return true iff the user was not already added to this role
   */
  public boolean addScreensaverUserRole(ScreensaverUserRole role)
  {
    boolean result = _roles.add(role);
    for (ScreensaverUserRole impliedRole : role.getImpliedRoles()) {
      _roles.add(impliedRole);
    }

    validateRoles();
    return result;
  }

  /**
   * Remove the specified role from the user, including all roles implied by this role. 
   * @param role the role to remove this user from
   * @return true iff the user previously belonged to the role
   * @throws DataModelViolationException if another of the user's roles implies this role 
   */
  public boolean removeScreensaverUserRole(ScreensaverUserRole role)
  {
    if (!!!_roles.contains(role)) {
      return false;
    }
    Set<ScreensaverUserRole> primaryRoles = Sets.newHashSet(getPrimaryScreensaverUserRoles());
    if (!!!primaryRoles.contains(role)) {
      throw new DataModelViolationException("cannot remove an implied role: " + role + " (remove the primary role instead)");
    }
    primaryRoles.remove(role);
    _roles.clear();
    for (ScreensaverUserRole primaryRole : primaryRoles) {
      addScreensaverUserRole(primaryRole);
    }
    return true;
  }

  /**
   * Remove this user from all roles.
   */
  public void removeScreensaverUserRoles()
  {
    _roles.clear();
  }

  /**
   * Returns true whenever the user is in the specified role
   * @param role the role to check whether the user is in
   * @return true whenever the user is in the specified role
   */
  @Transient
  public boolean isUserInRole(ScreensaverUserRole role)
  {
    if (role == null || ! validateRole(role)) {
      return false;
    }
    return _roles.contains(role);
  }

  /**
   * Get a mapping from the names of the roles to boolean values indicating whether or not the
   * user is in the role. For use in with JSF EL expressions.
   * @return a mapping from the names of the roles to boolean values indicating whether or not the
   * user is in the role.
   * @motivation for JSF EL expressions
   */
  @Transient
  public Map<String,Boolean> getIsUserInRoleOfNameMap()
  {
    if (_rolesMap == null) {
      _rolesMap = new HashMap<String,Boolean>();
      for (ScreensaverUserRole role : ScreensaverUserRole.values()) {
        _rolesMap.put(role.getRoleName(), isUserInRole(role));
      }
    }
    return _rolesMap;
  }

  /**
   * Get the set of activities performed by this user.
   * @return the set of activities performed by this user
   */
  @OneToMany(mappedBy = "performedBy")
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="activityPerformed")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  @Sort(type = SortType.NATURAL)
  public SortedSet<Activity> getActivitiesPerformed()
  {
    return _activitiesPerformed;
  }

  /**
   * Get the first name.
   * @return the first name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getFirstName()
  {
    return _firstName;
  }

  /**
   * Set the first name.
   * @param firstName the new first name
   */
  public void setFirstName(String firstName)
  {
    _firstName = firstName;
  }

  /**
   * Get the last name.
   * @return the last name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getLastName()
  {
    return _lastName;
  }

  /**
   * Set the last name.
   * @param lastName the new last name
   */
  public void setLastName(String lastName)
  {
    _lastName = lastName;
  }

  /**
   * Get the full name "Last, First".
   * @return the full name
   */
  @Transient
  public String getFullNameLastFirst()
  {
    return getFullName(true);
  }

  /**
   * Get the full name as "First Last".
   * @return the full name
   */
  @Transient
  public String getFullNameFirstLast()
  {
    return getFullName(false);
  }

  /**
   * Get the full name.
   * @param lastFirst true if desired format is "Last, First", false if desired format is "First Last"
   * @return the full name
   */
  @Transient
  public String getFullName(boolean lastFirst)
  {
    List<String> nameParts = new ArrayList<String>();
    if (!StringUtils.isEmpty(_firstName)) {
      nameParts.add(_firstName);
    }
    if (!StringUtils.isEmpty(_lastName)) {
      nameParts.add(_lastName);
    }
    if (lastFirst) {
      Collections.reverse(nameParts);
    }
    return Joiner.on(lastFirst ? ", " : " ").join(nameParts);
  }

  /**
   * Get the email.
   * @return the email
   */
  @org.hibernate.annotations.Type(type="text")
  public String getEmail()
  {
    return _email;
  }

  /**
   * Set the email.
   * @param email the new email
   */
  public void setEmail(String email)
  {
    _email = email;
  }

  /**
   * Get the phone.
   * @return the phone
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPhone()
  {
    return _phone;
  }

  /**
   * Set the phone.
   * @param phone the new phone
   */
  public void setPhone(String phone)
  {
    _phone = phone;
  }

  /**
   * Get the mailing address.
   * @return the mailing address
   */
  @org.hibernate.annotations.Type(type="text")
  public String getMailingAddress()
  {
    return _mailingAddress;
  }

  /**
   * Set the mailing address.
   * @param mailingAddress the new mailing address
   */
  public void setMailingAddress(String mailingAddress)
  {
    _mailingAddress = mailingAddress;
  }

  /**
   * Get the comments.
   * @return the comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the user's Screensaver-managed login ID.
   * @return the Screensaver login ID
   */
  @Column(unique=true)
  @org.hibernate.annotations.Type(type="text")
  public String getLoginId()
  {
    return _loginId;
  }

  /**
   * Set the user's Screensaver-managed login ID.
   * @param loginId the new Screensaver login ID
   */
  public void setLoginId(String loginId)
  {
    _loginId = loginId;
  }

  /**
   * Get the digested (hashed) password.
   * @return the digested (hashed) password
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDigestedPassword()
  {
    return _digestedPassword;
  }

  /**
   * Set the digested (hashed) version of the password associated with the user's login ID.
   * @param digestedPassword
   */
  public void setDigestedPassword(String digestedPassword)
  {
    _digestedPassword = digestedPassword;
  }

  /**
   * Set the password associated with the user's login ID, specified as a
   * plaintext password, but which will be digested (hashed) before being
   * stored, for security purposes.
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
   * Harvard-specific user identifier for authentication purposes.
   */
  // TODO: make this unique when duplicates are taken care of in the database
  //@Column(unique=true)
  @org.hibernate.annotations.Type(type="text")
  public String getECommonsId()
  {
    return _eCommonsId;
  }

  public void setECommonsId(String eCommonsId)
  {
    if (eCommonsId != null && !eCommonsId.toLowerCase().equals(eCommonsId)) {
      throw new IllegalArgumentException("eCommons ID must contain only lowercase letters");
    }
    _eCommonsId = eCommonsId;
  }

  /**
   * Harvard-specific user identifier.
   * 
   * @return the harvard id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getHarvardId()
  {
    return _harvardId;
  }

  public void setHarvardId(String harvardId)
  {
    _harvardId = harvardId;
  }

  /**
   * Get the harvard id expiration date.
   * @return the harvard id expiration date
   */
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getHarvardIdExpirationDate()
  {
    return _harvardIdExpirationDate;
  }

  /**
   * Set the harvard id expiration date.
   * @param harvardIdExpirationDate the new harvard id expiration date
   */
  public void setHarvardIdExpirationDate(LocalDate harvardIdExpirationDate)
  {
    _harvardIdExpirationDate = harvardIdExpirationDate;
  }
  
  /**
   * Get the harvard id requested expiration date.
   */
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getHarvardIdRequestedExpirationDate()
  {
    return _harvardIdRequestedExpirationDate;
  }

  /**
   * Set the harvard id requested expiration date.
   */
  public void setHarvardIdRequestedExpirationDate(LocalDate value)
  {
    _harvardIdRequestedExpirationDate = value;
  }
  
  // Comparable interface
  
  public int compareTo(ScreensaverUser other)
  {
    return ScreensaverUserComparator.getInstance().compare(this, other);
  }
  
  
  // Principal interface 
  
  @Transient
  public String getName()
  {
    return getFullNameFirstLast();
  }


  // protected constructor and instance method

  /**
   * Construct an uninitialized <code>ScreeningRoomUser</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ScreensaverUser() {}

  /**
   * Validate the specified role for this user. Throw a {@link DataModelViolationException}
   * when it is illegal for this type of user to have this role.
   * @throws DataModelViolationException when it is illegal for this type of user to have this role
   */
  abstract protected boolean validateRole(ScreensaverUserRole role);


  // private instance methods

  /**
   * Set the id for the Screensaver user.
   * @param screeningRoomUserId the new id for the Screensaver user
   * @motivation for hibernate
   */
  private void setScreensaverUserId(Integer screensaverUserId)
  {
    setEntityId(screensaverUserId);
  }

  /**
   * Get the version for the Screensaver user.
   * @return the version for the Screensaver user
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the Screensaver user.
   * @param version the new version for the Screensaver user
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the screensaver user roles.
   * @param roles the new screensaver user roles
   * @motivation for hibernate
   */
  private void setScreensaverUserRoles(Set<ScreensaverUserRole> roles)
  {
    _roles = roles;
  }

  /**
   * Set the activities performed by this user.
   * @param activitiesPerformed the screening room activities performed by this user
   * @motivation for hibernate
   */
  private void setActivitiesPerformed(SortedSet<Activity> activitiesPerformed)
  {
    _activitiesPerformed = activitiesPerformed;
  }

  /**
   * Validate the set of roles that this user has. Throw a {@link DataModelViolationException}
   * when one or more of the roles is not valid.
   * @throws DataModelViolationException when one or more of the roles is not valid
   */
  private void validateRoles()
  {
    for (ScreensaverUserRole role : _roles) {
      if (! validateRole(role)) {
        throw new DataModelViolationException(
          "user " + this + " has been granted illegal role: " + role);
      }
    }
  }
  
  @Column
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.users.Gender$UserType")
  public Gender getGender()
  {
    return _gender;
  }

  public void setGender(Gender value)
  {
    _gender = value;
  }  

  
  
  
}