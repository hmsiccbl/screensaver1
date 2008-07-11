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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a screening room user.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="screensaverUserId")
@org.hibernate.annotations.ForeignKey(name="fk_screening_room_user_to_screensaver_user")
@org.hibernate.annotations.Proxy
public class ScreeningRoomUser extends ScreensaverUser
{

  // private static data

  private static final Logger log = Logger.getLogger(ScreeningRoomUser.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private Set<ChecklistItem> _checklistItems = new HashSet<ChecklistItem>();
  private Set<Screen> _screensLed = new HashSet<Screen>();
  private Set<Screen> _screensCollaborated = new HashSet<Screen>();
  protected ScreeningRoomUserClassification _userClassification;
  private String _comsCrhbaPermitNumber;
  private String _comsCrhbaPermitPrincipalInvestigator;

  private LabHead _labHead;
  protected Lab _lab;


  // public constructor

  /**
   * Construct an uninitialized <code>ScreeningRoomUser</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors

   * @motivation for new ScreeningRoomUser creation via user interface, where even required
   *             fields are allowed to be uninitialized, initially
   */
   public ScreeningRoomUser() {}

  /**
   * Construct an initialized <code>ScreeningRoomUser</code>.
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
   */
  public ScreeningRoomUser(
    String firstName,
    String lastName,
    String email,
    String phone,
    String mailingAddress,
    String comments,
    String eCommonsId,
    String harvardId,
    ScreeningRoomUserClassification userClassification)
  {
    super(firstName,
          lastName,
          email,
          phone,
          mailingAddress,
          comments);
    setECommonsId(eCommonsId);
    setHarvardId(harvardId);
    setUserClassification(userClassification);
  }

  public ScreeningRoomUser(String firstName,
                           String lastName,
                           String email)
  {
    this(firstName,
         lastName,
         email,
         "",
         "",
         "",
         "",
         "",
         ScreeningRoomUserClassification.UNASSIGNED);
  }

  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the set of checklist items.
   * @return the checklist items
   */
  @OneToMany(
    mappedBy="screeningRoomUser",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("checklistItemType") // TODO: would like this to be checklistItemType.orderStatistic
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<ChecklistItem> getChecklistItems()
  {
    return _checklistItems;
  }

  /**
   * Create a new checklist item for the user.
   * @param checklistItemType the checklist item type
   * @return the new checklist item for the user
   */
  public ChecklistItem createChecklistItem(ChecklistItemType checklistItemType)
  {
    return createChecklistItem(checklistItemType, null, null, null, null);
  }

  /**
   * Create a new checklist item for the user.
   * @param checklistItemType the checklist item type
   * @param activationDate the activation date
   * @param activationInitials the activation initials
   * @return the new checklist item for the user
   */
  public ChecklistItem createChecklistItem(
    ChecklistItemType checklistItemType,
    LocalDate activationDate,
    String activationInitials)
  {
    return createChecklistItem(checklistItemType, activationDate, activationInitials, null, null);
  }

  /**
   * Create a new checklist item for the user.
   * @param checklistItemType the checklist item type
   * @param activationDate the activation date
   * @param activationInitials the activation initials
   * @param deactivationDate the deactivation date
   * @param deactivationInitials the deactivation initials
   * @return the new checklist item for the user
   */
  public ChecklistItem createChecklistItem(
    ChecklistItemType checklistItemType,
    LocalDate activationDate,
    String activationInitials,
    LocalDate deactivationDate,
    String deactivationInitials)
  {
    ChecklistItem checklistItem = new ChecklistItem(
      checklistItemType,
      this,
      activationDate,
      activationInitials,
      deactivationDate,
      deactivationInitials);
    _checklistItems.add(checklistItem);
    return checklistItem;
  }

  /**
   * Get the set of screens for which this user is the lead screener.
   * @return the set of screens for which this user is the lead screener
   */
  @OneToMany(
    mappedBy="leadScreener",
    fetch=FetchType.LAZY
  )
  @OrderBy("screenNumber")
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="screenLed")
  public Set<Screen> getScreensLed()
  {
    return _screensLed;
  }

  /**
   * Add the screen for which this user was the lead screener.
   * @param screenLed the screen for which this user was the lead screener
   * @return true iff the screening room user was not already lead screener for this screen
   */
  public boolean addScreenLed(Screen screenLed)
  {
    if (_screensLed.add(screenLed)) {
      screenLed.setLeadScreener(this);
      return true;
    }
    return false;
  }

  /**
   * Get the set of screens for which this user was a collaborator.
   * @return the set of screens for which this user was a collaborator
   */
  @ManyToMany(
    mappedBy="collaborators",
    targetEntity=Screen.class,
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.ForeignKey(name="fk_collaborator_link_to_screening_room_user")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(singularPropertyName="screenCollaborated")
  public Set<Screen> getScreensCollaborated()
  {
    return _screensCollaborated;
  }

  /**
   * Add the screens for which this user was a collaborator.
   * @param screenCollaborated the screens for which this user was a collaborato to add
   * @return true iff the screening room user did not already have the screens for which this user was a collaborato
   */
  public boolean addScreenCollaborated(Screen screenCollaborated)
  {
    if (_screensCollaborated.add(screenCollaborated)) {
      return screenCollaborated.getCollaborators().add(this);
    }
    return false;
  }

  /**
   * Remove the screens for which this user was a collaborator.
   * @param screenCollaborated the screens from which this user is no longer a collaborator
   * @return true iff the screening room user previously was a collaborator on the screen
   */
  public boolean removeScreenCollaborated(Screen screenCollaborated)
  {
    if (_screensCollaborated.remove(screenCollaborated)) {
      return screenCollaborated.getCollaborators().remove(this);
    }
    return false;
  }

  /**
   * Get the user classification.
   * @return the user classification
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification$UserType"
  )
  public ScreeningRoomUserClassification getUserClassification()
  {
    return _userClassification;
  }

  /**
   * Set the user classification.
   * @param userClassification the new user classification
   */
  public void setUserClassification(ScreeningRoomUserClassification userClassification)
  {
    assert userClassification != null : "user classification must be non-null";
    if (userClassification == ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR) {
      throw new BusinessRuleViolationException("cannot change the classification of a non-lab head to principal investigator");
    }
    _userClassification = userClassification;
  }

  /**
   * Get the COMS-CRHBA permit number.
   * @return the COMS-CRHBA permit number
   */
  @org.hibernate.annotations.Type(type="text")
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
   * @return the COMS-CRHBA permit principal investigator
   */
  @org.hibernate.annotations.Type(type="text")
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
   * @return <code>true</code> iff this user is an RNAi screener.
   */
  @Transient
  public boolean isRnaiUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENER);
  }

  /**
   * Get whether this user is a small compound screener.
   * @return <code>true</code> iff this user is a small compound screener.
   */
  @Transient
  public boolean isSmallMoleculeUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.SMALL_MOLECULE_SCREENER);
  }


  // protected instance methods

  @Override
  protected boolean validateRole(ScreensaverUserRole role)
  {
    if (log.isDebugEnabled()) {
      log.debug("validateRole " + this + " " + role);
    }
    return ! role.isAdministrative();
  }


  // private constructor and instance methods

  /**
   * Set the checklist items.
   * @param checklistItems the new checklist items
   * @motivation for hibernate
   */
  private void setChecklistItems(Set<ChecklistItem> checklistItems)
  {
    _checklistItems = checklistItems;
  }

  /**
   * Set the screens for which this user was the lead screener.
   * @param screensLed the new screens for which this user was the lead screener
   * @motivation for hibernate
   */
  private void setScreensLed(Set<Screen> screensLed)
  {
    _screensLed = screensLed;
  }

  /**
   * Set the screens for which this user was a collaborator.
   * @param screensCollaborated the new screens for which this user was a collaborator
   * @motivation for hibernate
   */
  private void setScreensCollaborated(Set<Screen> screensCollaborated)
  {
    _screensCollaborated = screensCollaborated;
  }

  // public lab methods

  /**
   * Get the Lab to which this user belongs. This is an abstraction of the
   * labHead relationship, as the labHead "is" the lab, but that concept is
   * confusing from the perspective of client code, so we provide an explicit
   * Lab object instead.
   *
   * @return the ScreeningRoomUser that represents the lab of this user; since a
   *         non-lab head is allowed to not have a lab at all, in this case a
   *         Lab will be returned that has a null labHead and empty labName and
   *         labAffiliation; modifying its labMembers collection will have not
   *         effect on persisted data.
   * @motivation prevents client code from being able to set properties that
   *             should only be set on the labHead (e.g. labAffiliation).
   */
  @Transient
  public Lab getLab()
  {
    if (_lab == null) {
      if (_labHead != null) {
        _lab = _labHead.getLab();
      }
      else {
        _lab = new Lab(null);
      }
    }
    return _lab;
  }

  /**
   * Set or change the lab of a non-lab head (i.e., non-Prinicipal Investigator) screening
   * room user.
   *
   * @param lab the new lab; null if the user is no longer a member of any lab
   */
  public void setLab(Lab lab)
  {
    if (isHeadOfLab()) {
      throw new DataModelViolationException("cannot modify the lab of a lab head");
    }

    // remove from existing lab
    getLab().getLabMembers().remove(this);

    _lab = lab;
    _labHead = null;

    // add to new lab
    getLab().getLabMembers().add(this);
    _labHead = getLab().getLabHead();
  }

  /**
   * Return true iff this user is the head of a lab. Users are considered a lab
   * heads if they have
   * {@link ScreeningRoomUserClassification#PRINCIPAL_INVESTIGATOR}
   * classification.
   *
   * @return true iff this user is the head of a lab
   * @motivation cannot name this method isLabHead or java.beans.BeanInfo
   *             classes get confused about the appropriate type and getter
   *             method for property labHead, screwing up our model unit tests
   */
  @Transient
  public boolean isHeadOfLab()
  {
    return false;
  }


  // private lab methods

  /**
   * Get the ScreeningRoomUser that is the head of this user's lab, which may be
   * this user.
   *
   * @see #getLab()
   * @return the lab head; <code>this</code>, if the this user is the lab head.
   */
  @ManyToOne(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name = "labHeadId", nullable = true)
  @org.hibernate.annotations.ForeignKey(name = "fk_screening_room_user_to_lab_head")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty = "labMembers")
  private LabHead getLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   * @param labHead the new lab head
   */
  protected void setLabHead(LabHead labHead)
  {
    _labHead = labHead;
  }
}
