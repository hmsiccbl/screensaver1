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

import edu.harvard.med.screensaver.model.CollectionElementName;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a screening room user.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="screening_room_user" lazy="false"
 * @hibernate.joined-subclass-key column="screensaver_user_id"
 */
public class ScreeningRoomUser extends ScreensaverUser
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ScreeningRoomUser.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private ScreeningRoomUserClassification _userClassification;
  private Set<ChecklistItem> _checklistItems = new HashSet<ChecklistItem>();
  private Set<Screen> _screensLed = new HashSet<Screen>();
  private Set<Screen> _screensHeaded = new HashSet<Screen>();
  private Set<Screen> _screensCollaborated = new HashSet<Screen>();
  private Set<CherryPickRequest> _cherryPickRequests = new HashSet<CherryPickRequest>();
  private ScreeningRoomUser _labHead;
  private Set<ScreeningRoomUser> _labMembers = new HashSet<ScreeningRoomUser>();
  private LabAffiliation _labAffiliation;
  private boolean _isNonScreeningUser;
  private String _comsCrhbaPermitNumber;
  private String _comsCrhbaPermitPrincipalInvestigator;


  // public constructors

  /**
   * Constructs an initialized <code>ScreeningRoomUser</code> object.
   *
   * @param dateCreated the date created
   * @param firstName the first name
   * @param lastName the last name
   * @param email the email
   * @param phone the phone number
   * @param mailingAddress the mailing address
   * @param comments the comments
   * @param eCommonsId the eCommonds ID
   * @param harvardId the harvard ID
   * @param userClassification the user classification
   * @param isNonScreeningUser does not perform any screening, but is otherwise associated with Screens in this system
   */
  public ScreeningRoomUser(
    Date dateCreated,
    String firstName,
    String lastName,
    String email,
    String phone,
    String mailingAddress,
    String comments,
    String eCommonsId,
    String harvardId,
    ScreeningRoomUserClassification userClassification,
    boolean isNonScreeningUser)
  {
    super(dateCreated,
          firstName,
          lastName,
          email,
          phone,
          mailingAddress,
          comments);
    setECommonsId(eCommonsId);
    setHarvardId(harvardId);
    setUserClassification(userClassification);
    setNonScreeningUser(isNonScreeningUser);
  }


  // public methods

  /**
   * Get an unmodifiable copy of the set of checklist items.
   *
   * @return the checklist items
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="screensaver_user_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.users.ChecklistItem"
   */
  public Set<ChecklistItem> getChecklistItems()
  {
    return _checklistItems;
  }

  /**
   * Get an unmodifiable copy of the set of screens for which this user was the lead screener.
   *
   * @return the screens for which this user was the lead screener
   */
  @ToManyRelationship(inverseProperty="leadScreener")
  @CollectionElementName("screenLed")
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
  @ToManyRelationship(inverseProperty="labHead")
  @CollectionElementName("screenHeaded")
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
  @ToManyRelationship(inverseProperty="collaborators")
  @CollectionElementName("screenCollaborated")
  public Set<Screen> getScreensCollaborated()
  {
    return Collections.unmodifiableSet(_screensCollaborated);
  }

  /**
   * Add the screens for which this user was a collaborator.
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
   * Remove the screens for which this user was a collaborator.
   *
   * @param screenCollaborated the screens from which this user is no longer a collaborator
   * @return true iff the screening room user previously was a collaborator on the screen
   */
  public boolean removeScreenCollaborated(Screen screenCollaborated)
  {
    if (getHbnScreensCollaborated().remove(screenCollaborated)) {
      return screenCollaborated.getHbnCollaborators().remove(this);
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of cherry pick requests.
   *
   * @return the cherry pick requests
   */
  @ToManyRelationship(inverseProperty="requestedBy")
  public Set<CherryPickRequest> getCherryPickRequests()
  {
    return Collections.unmodifiableSet(_cherryPickRequests);
  }

  /**
   * Get the ScreeningRoomUser that is the head of this user's lab. If this user
   * is the lab head, return this user.
   *
   * @return the lab head; null if this user is the head of her own lab.
   */
  @ToOneRelationship(inverseProperty="labMembers")
  public ScreeningRoomUser getLabHead()
  {
    if (_labHead == null) {
      return this;
    }
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

  // note: cannot name this method "isLabHead", as this is ambiguous with "getLabHead", 
  // w.r.t. to JavaBean property names (causing a unit test to fail)
  @DerivedEntityProperty
  public boolean isHeadOfLab()
  {
    return getLabHead() == null;
  }

  /**
   * Get an unmodifiable copy of the set of lab members. If this
   * ScreeningRoomUser is also the lab head, the result will <i>not</i> contain
   * this ScreeningRoomUser (i.e., the lab head is not considered a lab member).
   * 
   * @return the lab members
   */
  @ToManyRelationship(inverseProperty="labHead")
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
   * Get the lab affiliation. The lab affiliation should always be present, whether this user
   * is a lab head or a lab member.
   *
   * @return the lab affiliation
   */
  @ToOneRelationship(nullable=true)
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
    if (labAffiliation == _labAffiliation) {
      return;
    }
    if (_labAffiliation != null) {
      _labAffiliation.getHbnScreeningRoomUsers().remove(this);
    }
    _labAffiliation = labAffiliation;
    if (_labAffiliation != null) {
      _labAffiliation.getHbnScreeningRoomUsers().add(this);
    }
  }

  /**
   * Set the email, updating this entity's membership in any related entity sets
   * that it already is a member of.
   * 
   * @param email the new email
   */
  @Override
  public void setEmail(String email)
  {
    if (_screensCollaborated != null) {
      for (Screen screenCollaborated : _screensCollaborated) {
        screenCollaborated.getHbnCollaborators().remove(this);
      }
    }
    if (_labHead != null) {
      _labHead.getHbnLabMembers().remove(this);
    }
    super.setEmail(email);
    if (_screensCollaborated != null) {
      for (Screen screenCollaborated : _screensCollaborated) {
        screenCollaborated.getHbnCollaborators().add(this);
      }
    }
    if (_labHead != null) {
      _labHead.getHbnLabMembers().add(this);
    } 
  }

  /**
   * Get the user classification.
   *
   * @return the user classification
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification$UserType"
   *   not-null="true"
   */
  public ScreeningRoomUserClassification getUserClassification()
  {
    return _userClassification;
  }

  /**
   * Set the user classification.
   *
   * @param userClassification the new user classification
   */
  public void setUserClassification(ScreeningRoomUserClassification userClassification)
  {
    _userClassification = userClassification;
  }

  /**
   * Get non-screening flag, indicating whether this user performs screening.
   * 
   * @return a flag indicating whether this user performs screening
   * @hibernate.property not-null="true"
   */
  public boolean getNonScreeningUser()
  {
    return _isNonScreeningUser;
  }

  /**
   * Set the non-screening flag.
   *
   * @param isNonScreeningUser a flag indicating whether this user performs screening.
   */
  public void setNonScreeningUser(boolean isNonScreeningUser)
  {
    _isNonScreeningUser = isNonScreeningUser;
  }

  /**
   * Get the COMS-CRHBA permit number.
   * 
   * @return the COMS-CRHBA permit number
   * @hibernate.property type="text"
   */
  public String getComsCrhbaPermitNumber()
  {
    return _comsCrhbaPermitNumber;
  }

  /**
   * Set the COMS-CRHBA permit number.
   * @param comsCrhbaPermitNumber the new COMS-CRHBA permit number for the user
   */
  public void setComsCrhbaPermitNumber(String comsCrhbaPermitNumber)
  {
    _comsCrhbaPermitNumber = comsCrhbaPermitNumber;
  }

  /**
   * Get the COMS-CRHBA permit principal investigator.
   * 
   * @return the COMS-CRHBA permit principal investigator
   * @hibernate.property type="text"
   */
  public String getComsCrhbaPermitPrincipalInvestigator()
  {
    return _comsCrhbaPermitPrincipalInvestigator;
  }

  /**
   * Set the COMS-CRHBA permit principal investigator.
   * @param comsCrhbaPermitNumber the new COMS-CRHBA permit principal investigator
   */
  public void setComsCrhbaPermitPrincipalInvestigator(String comsCrhbaPermitPrincipalInvestigator)
  {
    _comsCrhbaPermitPrincipalInvestigator = comsCrhbaPermitPrincipalInvestigator;
  }


  /**
   * Get whether this user is an RNAi screener.
   *
   * @return <code>true</code> iff this user is an RNAi screener.
   */
  @DerivedEntityProperty
  public boolean isRnaiUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
  }
  
  /**
   * Get whether this user is a small compound screener.
   *
   * @return <code>true</code> iff this user is a small compound screener.
   */
  @DerivedEntityProperty
  public boolean isCompoundUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.COMPOUND_SCREENING_ROOM_USER);
  }
  
  @DerivedEntityProperty
  public String getLabName() 
  {
    StringBuilder labName = new StringBuilder(getLabHead().getFullNameLastFirst());
    String labAffiliation = getLabAffiliationName();
    if (labAffiliation.length() > 0) {
      labName.append(" - ").append(labAffiliation);
    }
    return labName.toString();
  }
  
  @DerivedEntityProperty
  public String getLabAffiliationName()
  {
    LabAffiliation labAffiliation = getLabHead().getLabAffiliation();
    if (labAffiliation != null) {
      String labAffiliationName = labAffiliation.getAffiliationName();
      if (labAffiliationName != null && labAffiliationName.length() > 0) {
        return labAffiliationName;
      }
    }    
    return "";
  }
 
  
  // protected methods

  /**
   * Get the screens for which this user was the lead screener.
   *
   * @return the screens for which this user was the lead screener
   * @hibernate.set
   *   cascade="none"
   *   inverse="true"
   *   lazy="true"
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
   *   cascade="none"
   *   inverse="true"
   *   lazy="true"
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
   *   cascade="none"
   *   lazy="true"
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
   * Get the cherry pick requests made by this user.
   *
   * @return the cherry pick requests made by this user
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="requested_by_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickRequest"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * public access since relationship is cross-package
   */
  public Set<CherryPickRequest> getHbnCherryPickRequests()
  {
    return _cherryPickRequests;
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
   * Set the checklist items.
   *
   * @param checklistItems the new checklist items
   * @motivation for hibernate
   */
  private void setChecklistItems(Set<ChecklistItem> checklistItems)
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
   * Set the cherry pick requests made by this user.
   *
   * @param cherryPickRequests the cherry pick requests made by this user
   * @motivation for hibernate
   */
  private void setHbnCherryPickRequests(Set<CherryPickRequest> cherryPickRequests)
  {
    _cherryPickRequests = cherryPickRequests;
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
   *   lazy="no-proxy"
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
   *   inverse="true" 
   *   lazy="true"
   * @hibernate.collection-key
   *   column="lab_head_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * @motivation in order to avoid what I believe is a Hibernate bug, I
   *   have removed the cascade="save-update" from the hibernate.set
   *   annotation. this means that lab members will not automatically
   *   get saved or updated when you save or update the lab head! (to
   *   reproduce the bug, but the cascade back in, and run the
   *   {@link edu.harvard.med.screensaver.db.screendb.ScreenDBDataImporter}.
   *   (NOTE: if/when you fix this, make sure to remove the 2 hacks in
   *   EntityBeansPersistenceTest!)
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
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.LabAffiliation"
   *   column="lab_affiliation_id"
   *   foreign-key="fk_screening_room_user_to_lab_affiliation"
   *   cascade="save-update"
   */
  private LabAffiliation getHbnLabAffiliation()
  {
    return _labAffiliation;
  }

  private void setHbnLabAffiliation(LabAffiliation labAffiliation)
  {
    _labAffiliation = labAffiliation;
  }
}
