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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Visit;


/**
 * A Hibernate entity bean representing a screening room user.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class ScreeningRoomUser extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ScreeningRoomUser.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _screeningRoomUserId;
  private Integer _version;
  private Set<ChecklistItem> _checklistItems = new HashSet<ChecklistItem>();
  private Set<Screen> _screensLed = new HashSet<Screen>();
  private Set<Screen> _screensHeaded = new HashSet<Screen>();
  private Set<Screen> _screensCollaborated = new HashSet<Screen>();
  private Set<Visit> _visitsPerformed = new HashSet<Visit>();
  private ScreeningRoomUser _labHead;
  private Set<ScreeningRoomUser> _labMembers = new HashSet<ScreeningRoomUser>();
  private LabAffiliation _labAffiliation;
  private Date _dateCreated;
  private String _firstName;
  private String _lastName;
  private String _email;
  private String _eCommonsId;
  private String _harvardId;
  private String _phone;
  private UserClassification _userClassification;
  private boolean _nonScreeningUser;
  private boolean _rnaiUser;
  private String _mailingAddress;
  private String _comments;


  // public constructors

  /**
   * Constructs an initialized <code>ScreeningRoomUser</code> object.
   *
   * @param dateCreated the date created
   * @param firstName the first name
   * @param lastName the last name
   * @param email the email
   * @param eCommonsId the eCommonds ID
   * @param harvardId the harvard ID
   * @param phone the phone number
   * @param userClassification the user classification
   * @param nonScreeningUser the non-screening user
   * @param rnaiUser the RNAi user
   * @param mailingAddress the mailing address
   * @param comments the comments
   */
  public ScreeningRoomUser(
    Date dateCreated,
    String firstName,
    String lastName,
    String email,
    String eCommonsId,
    String harvardId,
    String phone,
    UserClassification userClassification,
    boolean nonScreeningUser,
    boolean rnaiUser,
    String mailingAddress,
    String comments)
  {
    this(
      dateCreated,
      firstName,
      lastName,
      email,
      userClassification, 
      nonScreeningUser,
      rnaiUser);
    setECommonsId(eCommonsId);
    setHarvardId(harvardId);
    setPhone(phone);
    setMailingAddress(mailingAddress);
    setComments(comments);
  }

  /**
   * Constructs an initialized <code>ScreeningRoomUser</code> object.
   *
   * @param dateCreated the date created
   * @param firstName the first name
   * @param lastName the last name
   * @param email the email
   * @param userClassification the user classification
   * @param nonScreeningUser the non-screening user
   * @param rnaiUser the RNAi user
   */
  public ScreeningRoomUser(
    Date dateCreated,
    String firstName,
    String lastName,
    String email,
    UserClassification userClassification,
    boolean nonScreeningUser,
    boolean rnaiUser)
  {
    _dateCreated = truncateDate(dateCreated);
    _firstName = firstName;
    _lastName = lastName;
    _email = email;
    _userClassification = userClassification;
    _nonScreeningUser = nonScreeningUser;
    _rnaiUser = rnaiUser;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getScreeningRoomUserId();
  }

  /**
   * Get the id for the screening room user.
   *
   * @return the id for the screening room user
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screening_room_user_id_seq"
   */
  public Integer getScreeningRoomUserId()
  {
    return _screeningRoomUserId;
  }

  /**
   * Get an unmodifiable copy of the set of checklist items.
   *
   * @return the checklist items
   */
  public Set<ChecklistItem> getChecklistItems()
  {
    return Collections.unmodifiableSet(_checklistItems);
  }

  /**
   * Add the checklist item.
   *
   * @param checklistItem the checklist item to add
   * @return true iff the screening room user did not already have the checklist item
   */
  public boolean addChecklistItem(ChecklistItem checklistItem)
  {
    if (getHbnChecklistItems().add(checklistItem)) {
      checklistItem.setHbnScreeningRoomUser(this);
      return true;
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of screens for which this user was the lead screener.
   *
   * @return the screens for which this user was the lead screener
   */
  public Set<Screen> getScreensLed()
  {
    return Collections.unmodifiableSet(_screensLed);
  }

  /**
   * Add the screens for which this user was the lead screene.
   *
   * @param screenLed the screens for which this user was the lead screene to add
   * @return true iff the screening room user did not already have the screens for which this user was the lead screene
   */
  public boolean addScreenLed(Screen screenLed)
  {
    if (getHbnScreensLed().add(screenLed)) {
      screenLed.setHbnLeadScreener(this);
      return true;
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of screens for which this user was the lab head.
   *
   * @return the screens for which this user was the lab head
   */
  public Set<Screen> getScreensHeaded()
  {
    return Collections.unmodifiableSet(_screensHeaded);
  }

  /**
   * Add the screens for which this user was the lab hea.
   *
   * @param screenHeaded the screens for which this user was the lab hea to add
   * @return true iff the screening room user did not already have the screens for which this user was the lab hea
   */
  public boolean addScreenHeaded(Screen screenHeaded)
  {
    if (getHbnScreensHeaded().add(screenHeaded)) {
      screenHeaded.setHbnLabHead(this);
      return true;
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of screens for which this user was a collaborator.
   *
   * @return the screens for which this user was a collaborator
   */
  public Set<Screen> getScreensCollaborated()
  {
    return Collections.unmodifiableSet(_screensCollaborated);
  }

  /**
   * Add the screens for which this user was a collaborato.
   *
   * @param screenCollaborated the screens for which this user was a collaborato to add
   * @return true iff the screening room user did not already have the screens for which this user was a collaborato
   */
  public boolean addScreenCollaborated(Screen screenCollaborated)
  {
    if (getHbnScreensCollaborated().add(screenCollaborated)) {
      return screenCollaborated.getHbnCollaborators().add(this);
    }
    return false;
  }

  /**
   * Remove the screens for which this user was a collaborato.
   *
   * @param screenCollaborated the screens for which this user was a collaborato to remove
   * @return true iff the screening room user previously had the screens for which this user was a collaborato
   */
  public boolean removeScreenCollaborated(Screen screenCollaborated)
  {
    if (getHbnScreensCollaborated().remove(screenCollaborated)) {
      return screenCollaborated.getHbnCollaborators().remove(this);
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of visits performed.
   *
   * @return the visits performed
   */
  public Set<Visit> getVisitsPerformed()
  {
    return Collections.unmodifiableSet(_visitsPerformed);
  }

  /**
   * Add the visits performe.
   *
   * @param visitPerformed the visits performe to add
   * @return true iff the screening room user did not already have the visits performe
   */
  public boolean addVisitPerformed(Visit visitPerformed)
  {
    if (getHbnVisitsPerformed().add(visitPerformed)) {
      visitPerformed.setHbnPerformedBy(this);
      return true;
    }
    return false;
  }

  /**
   * Get the lab head.
   *
   * @return the lab head
   */
  public ScreeningRoomUser getLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   *
   * @param labHead the new lab head
   */
  public void setLabHead(ScreeningRoomUser labHead)
  {
    if (_labHead != null) {
      _labHead.getHbnLabMembers().remove(this);
    }
    _labHead = labHead;
    _labHead.getHbnLabMembers().add(this);
  }

  /**
   * Get an unmodifiable copy of the set of lab members.
   *
   * @return the lab members
   */
  public Set<ScreeningRoomUser> getLabMembers()
  {
    return Collections.unmodifiableSet(_labMembers);
  }

  /**
   * Add the lab member.
   *
   * @param labMember the lab member to add
   * @return true iff the screening room user did not already have the lab member
   */
  public boolean addLabMember(ScreeningRoomUser labMember)
  {
    if (getHbnLabMembers().add(labMember)) {
      labMember.setHbnLabHead(this);
      return true;
    }
    return false;
  }

  /**
   * Get the lab affiliation.
   *
   * @return the lab affiliation
   */
  public LabAffiliation getLabAffiliation()
  {
    return _labAffiliation;
  }

  /**
   * Set the lab affiliation.
   *
   * @param labAffiliation the new lab affiliation
   */
  public void setLabAffiliation(LabAffiliation labAffiliation)
  {
    if (_labAffiliation != null) {
      _labAffiliation.setHbnScreeningRoomUser(null);
    }
    _labAffiliation = labAffiliation;
    if (_labAffiliation != null) {
      _labAffiliation.setHbnScreeningRoomUser(this);
    }
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
    for (Screen screenCollaborated : _screensCollaborated) {
      screenCollaborated.getHbnCollaborators().remove(this);
    }
    if (_labHead != null) {
      _labHead.getHbnLabMembers().remove(this);
    }
    _eCommonsId = eCommonsId;
    for (Screen screenCollaborated : _screensCollaborated) {
      screenCollaborated.getHbnCollaborators().add(this);
    }
    if (_labHead != null) {
      _labHead.getHbnLabMembers().add(this);
    } 
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
   * Get the user classification.
   *
   * @return the user classification
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.users.UserClassification$UserType"
   *   not-null="true"
   */
  public UserClassification getUserClassification()
  {
    return _userClassification;
  }

  /**
   * Set the user classification.
   *
   * @param userClassification the new user classification
   */
  public void setUserClassification(UserClassification userClassification)
  {
    _userClassification = userClassification;
  }

  /**
   * Get the non-screening user.
   *
   * @return the non-screening user
   * @hibernate.property
   *   not-null="true"
   */
  public boolean getNonScreeningUser()
  {
    return _nonScreeningUser;
  }

  /**
   * Set the non-screening user.
   *
   * @param nonScreeningUser the new non-screening user
   */
  public void setNonScreeningUser(boolean nonScreeningUser)
  {
    _nonScreeningUser = nonScreeningUser;
  }

  /**
   * Get the RNAi user.
   *
   * @return the RNAi user
   * @hibernate.property
   *   not-null="true"
   */
  public boolean getRnaiUser()
  {
    return _rnaiUser;
  }

  /**
   * Set the RNAi user.
   *
   * @param rnaiUser the new RNAi user
   */
  public void setRnaiUser(boolean rnaiUser)
  {
    _rnaiUser = rnaiUser;
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


  // protected methods

  /**
   * Get the screens for which this user was the lead screener.
   *
   * @return the screens for which this user was the lead screener
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="lead_screener_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public Set<Screen> getHbnScreensLed()
  {
    return _screensLed;
  }

  /**
   * Get the screens for which this user was the lab head.
   *
   * @return the screens for which this user was the lab head
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="lab_head_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public Set<Screen> getHbnScreensHeaded()
  {
    return _screensHeaded;
  }

  /**
   * Get the screens for which this user was a collaborator.
   *
   * @return the screens for which this user was a collaborator
   * @hibernate.set
   *   inverse="true"
   *   table="collaborator_link"
   *   cascade="all"
   * @hibernate.collection-key
   *   column="collaborator_id"
   * @hibernate.collection-many-to-many
   *   column="screen_id"
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   foreign-key="fk_collaborator_link_to_screening_room_user"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public Set<Screen> getHbnScreensCollaborated()
  {
    return _screensCollaborated;
  }

  /**
   * Get the visits performed.
   *
   * @return the visits performed
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="performed_by_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.Visit"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public Set<Visit> getHbnVisitsPerformed()
  {
    return _visitsPerformed;
  }

  
  // package methods

  @Override
  protected Object getBusinessKey()
  {
    return getEmail();
  }

  
  // protected methods
  
  /**
   * Get the checklist items.
   *
   * @return the checklist items
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screening_room_user_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.users.ChecklistItem"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<ChecklistItem> getHbnChecklistItems()
  {
    return _checklistItems;
  }

  /**
   * Set the lab affiliation.
   *
   * @param labAffiliation the new lab affiliation
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnLabAffiliation(LabAffiliation labAffiliation)
  {
    _labAffiliation = labAffiliation;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>ScreeningRoomUser</code> object.
   *
   * @motivation for hibernate
   */
  private ScreeningRoomUser() {}


  // private methods

  /**
   * Set the id for the screening room user.
   *
   * @param screeningRoomUserId the new id for the screening room user
   * @motivation for hibernate
   */
  private void setScreeningRoomUserId(Integer screeningRoomUserId) {
    _screeningRoomUserId = screeningRoomUserId;
  }

  /**
   * Get the version for the screening room user.
   *
   * @return the version for the screening room user
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the screening room user.
   *
   * @param version the new version for the screening room user
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the checklist items.
   *
   * @param checklistItems the new checklist items
   * @motivation for hibernate
   */
  private void setHbnChecklistItems(Set<ChecklistItem> checklistItems)
  {
    _checklistItems = checklistItems;
  }

  /**
   * Set the screens for which this user was the lead screener.
   *
   * @param screensLed the new screens for which this user was the lead screener
   * @motivation for hibernate
   */
  private void setHbnScreensLed(Set<Screen> screensLed)
  {
    _screensLed = screensLed;
  }

  /**
   * Set the screens for which this user was the lab head.
   *
   * @param screensHeaded the new screens for which this user was the lab head
   * @motivation for hibernate
   */
  private void setHbnScreensHeaded(Set<Screen> screensHeaded)
  {
    _screensHeaded = screensHeaded;
  }

  /**
   * Set the screens for which this user was a collaborator.
   *
   * @param screensCollaborated the new screens for which this user was a collaborator
   * @motivation for hibernate
   */
  private void setHbnScreensCollaborated(Set<Screen> screensCollaborated)
  {
    _screensCollaborated = screensCollaborated;
  }

  /**
   * Set the visits performed.
   *
   * @param visitsPerformed the new visits performed
   * @motivation for hibernate
   */
  private void setHbnVisitsPerformed(Set<Visit> visitsPerformed)
  {
    _visitsPerformed = visitsPerformed;
  }

  /**
   * Get the lab head.
   *
   * @return the lab head
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="lab_head_id"
   *   foreign-key="fk_screening_room_user_to_lab_head"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ScreeningRoomUser getHbnLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   *
   * @param labHead the new lab head
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setHbnLabHead(ScreeningRoomUser labHead)
  {
    _labHead = labHead;
  }


  /**
   * Get the lab members.
   *
   * @return the lab members
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="lab_head_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private Set<ScreeningRoomUser> getHbnLabMembers()
  {
    return _labMembers;
  }


  /**
   * Set the lab members.
   *
   * @param labMembers the new lab members
   * @motivation for hibernate
   */
  private void setHbnLabMembers(Set<ScreeningRoomUser> labMembers)
  {
    _labMembers = labMembers;
  }

  /**
   * Get the lab affiliation.
   *
   * @return the lab affiliation
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.LabAffiliation"
   *   column="lab_affiliation_id"
   *   foreign-key="fk_screening_room_user_to_lab_affiliation"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private LabAffiliation getHbnLabAffiliation()
  {
    return _labAffiliation;
  }
}
