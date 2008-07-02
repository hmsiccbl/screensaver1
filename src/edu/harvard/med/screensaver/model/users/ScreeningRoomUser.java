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
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a screening room user.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="screensaverUserId")
@org.hibernate.annotations.ForeignKey(name="fk_screening_room_user_to_screensaver_user")
@org.hibernate.annotations.Proxy()//lazy=false)
public class ScreeningRoomUser extends ScreensaverUser
{

  // private static data

  private static final Logger log = Logger.getLogger(ScreeningRoomUser.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private ScreeningRoomUser _labHead;
  private Set<ScreeningRoomUser> _labMembers = new HashSet<ScreeningRoomUser>();
  private LabAffiliation _labAffiliation;
  private Set<ChecklistItem> _checklistItems = new HashSet<ChecklistItem>();
  private Set<Screen> _screensHeaded = new HashSet<Screen>();
  private Set<Screen> _screensLed = new HashSet<Screen>();
  private Set<Screen> _screensCollaborated = new HashSet<Screen>();
  private ScreeningRoomUserClassification _userClassification;
  private String _comsCrhbaPermitNumber;
  private String _comsCrhbaPermitPrincipalInvestigator;


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
   * Get the ScreeningRoomUser that is the head of this user's lab. If this user
   * is the lab head, return null.
   * 
   * @see #getLab()
   * @see #getLabName()
   * 
   * @return the lab head; null if this user is the head of her own lab.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "labHeadId", nullable = true)
  @org.hibernate.annotations.ForeignKey(name = "fk_screening_room_user_to_lab_head")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty = "labMembers")
  public ScreeningRoomUser getLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   * @param labHead the new lab head
   */
  public void setLabHead(ScreeningRoomUser labHead)
  {
    // hibernate callers do not need maintenance of bi-directional integrity. they also crash
    // when you call getLabMembers from within setLabHead
    if (isHibernateCaller()) {
      _labHead = labHead;
      return;
    }
    if (labHead != null && labHead.equals(_labHead)) {
      return;
    }
    if (labHead != null && !getLabMembers().isEmpty()) {
      throw new DataModelViolationException("a lab head (with lab members) cannot itself have a lab head");
    }
    if (_labHead != null) {
      _labHead.getLabMembers().remove(this);
    }
    _labHead = labHead;
    if (_labHead != null) {
      _labHead.getLabMembers().add(this);
    }
  }

  /**
   * Return true iff this user is the head of a lab. Users are considered lab heads whenever
   * they do not themselves have any lab head.
   * @return true iff this user is the head of a lab
   * @motivation cannot name this method isLabHead or java.beans.BeanInfo classes get confused
   * about the appropriate type and getter method for property labHead, screwing up our model
   * unit tests
   */
  @Transient
  public boolean isHeadOfLab()
  {
    return _labHead == null;
  }

  /**
   * Get the set of lab members. If this ScreeningRoomUser is also the lab head, the result
   * will <i>not</i> contain this ScreeningRoomUser (i.e., the lab head is not considered a
   * lab member).
   * <p>
   * Note there is no corresponding <code>removeLabMember(ScreeningRoomUser)</code> here. This
   * is intentional, as opposed to other places in the code where the absence of such a remove
   * method is just sloppy. <code>b.setLabHead(a)</code> should really be called instead of
   * <code>a.removeLabMember(b)</code>, so we can appropriately set the new lab head for
   * <code>b</code>. It probably makes sense to not remove a lab member unless and until that
   * screener is a member of another lab. But adding a <code>previousLabMembers</code> property
   * is probably overkill :)
   *
   * @return the set of lab members
   */
  @OneToMany(
    mappedBy="labHead",
    fetch=FetchType.LAZY
  )
  public Set<ScreeningRoomUser> getLabMembers()
  {
    return _labMembers;
  }

  /**
   * Add the lab member.
   * @param labMember the lab member to add
   * @return true iff the screening room user did not already have the lab member
   */
  public boolean addLabMember(ScreeningRoomUser labMember)
  {
    if (getLabMembers().add(labMember)) {
      labMember.setLabHead(this);
      return true;
    }
    return false;
  }

  /**
   * Get the lab affiliation. The lab affiliation should always be present,
   * whether this user is a lab head or a lab member:
   * <ul>
   * <li>This allows a new user to be created even if her lab head is not yet
   * known at the time of creation.</li>
   * <li>For billing purposes sometimes a non-lab head user will have a
   * different lab affiliation, although this is very rare.</li>
   * </ul>
   *
   * @return the lab affiliation
   */
  @ManyToOne(fetch=FetchType.EAGER,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="labAffiliationId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_screening_room_user_to_lab_affiliation")
  //@org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE
  })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
  public LabAffiliation getLabAffiliation()
  {
    return _labAffiliation;
  }

  /**
   * Set the lab affiliation. Update the lab name.
   * @param labAffiliation the new lab affiliation
   */
  public void setLabAffiliation(LabAffiliation labAffiliation)
  {
    _labAffiliation = labAffiliation;
  }

  /**
   * Get the name of the lab affiliation.
   * @return the lab affiliation name
   */
  @Transient
  public String getLabAffiliationName()
  {
    LabAffiliation labAffiliation;
    if (isHeadOfLab()) {
      labAffiliation = getLabAffiliation();
    }
    else {
      labAffiliation = getLabHead().getLabAffiliation();
    }
    if (labAffiliation == null) {
      return "";
    }
    return labAffiliation.getAffiliationName();
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
   * Get the set of screens for which this user was the lab head.
   * @return the set of screens for which this user was the lab head
   */
  @OneToMany(
    mappedBy="labHead",
    fetch=FetchType.LAZY
  )
  @OrderBy("screenNumber")
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="screenHeaded")
  public Set<Screen> getScreensHeaded()
  {
    return _screensHeaded;
  }

  /**
   * Add the screens for which this user was the lab head.
   * @param screenHeaded the screens for which this user was the lab hea to add
   * @return true iff the screening room user did not already have the screens for which this user was the lab hea
   */
  public boolean addScreenHeaded(Screen screenHeaded)
  {
    if (_screensHeaded.add(screenHeaded)) {
      screenHeaded.setLabHead(this);
      return true;
    }
    return false;
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
   * Get the ScreeningRoomUser that represents the lab of this user, even if
   * this user is the head of the lab.
   * 
   * @see #getLabHead()
   * @return the ScreeningRoomUser that represents the lab of this user
   * @motivation provide a convenient way to determine the ScreeningRoomUser
   *             that represents the lab, keeping this logic out of client code
   */
  @Transient
  public ScreeningRoomUser getLab()
  {
    if (isHeadOfLab()) {
      return this;
    }
    else {
      return getLabHead().getLab();
    }
  }

  /**
   * Get the name of the lab this user is associated with. The user may be
   * either a lab head or a lab member. This is a combination of the name of
   * the lab head, last and first, and the lab affiliation name.
   * 
   * @return the lab name
   */
  @Transient
  public String getLabName()
  {
    if (isHeadOfLab()) {
      StringBuilder labName = new StringBuilder(getFullNameLastFirst());
      String labAffiliation = getLabAffiliationName();
      if (labAffiliation.length() > 0) {
        labName.append(" - ")
               .append(labAffiliation);
      }
      return labName.toString();
    }
    else {
      return getLabHead().getLabName();
    }
  }

  /**
   * Get whether this user is an RNAi screener.
   * @return <code>true</code> iff this user is an RNAi screener.
   */
  @Transient
  public boolean isRnaiUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENING_ROOM_USER);
  }

  /**
   * Get whether this user is a small compound screener.
   * @return <code>true</code> iff this user is a small compound screener.
   */
  @Transient
  public boolean isCompoundUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.SMALL_MOLECULE_SCREENING_ROOM_USER);
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
   * Set the lab members.
   * @param labMembers the new lab members
   * @motivation for hibernate
   */
  private void setLabMembers(Set<ScreeningRoomUser> labMembers)
  {
    _labMembers = labMembers;
  }

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
   * Set the screens for which this user was the lab head.
   * @param screensHeaded the new screens for which this user was the lab head
   * @motivation for hibernate
   */
  private void setScreensHeaded(Set<Screen> screensHeaded)
  {
    _screensHeaded = screensHeaded;
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
}
