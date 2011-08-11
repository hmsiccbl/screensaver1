// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFilesEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

/**
 * A person that is using the screening facility to conduct one or more {@link Screen}s.
 * 
 * @see AdministratorUser
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="screensaverUserId")
@org.hibernate.annotations.ForeignKey(name="fk_screening_room_user_to_screensaver_user")
@org.hibernate.annotations.Proxy
public class ScreeningRoomUser extends ScreensaverUser implements AttachedFilesEntity<UserAttachedFileType,Integer>, ChecklistItemsEntity<Integer>
{

  // private static data

  private static final Logger log = Logger.getLogger(ScreeningRoomUser.class);
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<ScreeningRoomUser> LabHead = RelationshipPath.from(ScreeningRoomUser.class).to("labHead", Cardinality.TO_ONE);
  public static final RelationshipPath<ScreeningRoomUser> attachedFiles = RelationshipPath.from(ScreeningRoomUser.class).to("attachedFiles");
  public static final RelationshipPath<ScreeningRoomUser> screensLed = RelationshipPath.from(ScreeningRoomUser.class).to("screensLed");
  public static final RelationshipPath<ScreeningRoomUser> screensCollaborated = RelationshipPath.from(ScreeningRoomUser.class).to("screensCollaborated");
  public static final PropertyPath<ScreeningRoomUser> facilityUsageRoles = PropertyPath.from(ScreeningRoomUser.class).toCollectionOfValues("facilityUsageRoles");
  public static final RelationshipPath<ScreeningRoomUser> checklistItemEvents = RelationshipPath.from(ScreeningRoomUser.class).to("checklistItemEvents");
  public static final RelationshipPath<ScreeningRoomUser> serviceActivities = RelationshipPath.from(ScreeningRoomUser.class).to("serviceActivities");

  public static final Function<ScreeningRoomUser,String> ToDisplayStringFunction = new Function<ScreeningRoomUser,String>() {
    public String apply(ScreeningRoomUser u)
    {
      return ScreensaverUser.ToDisplayStringFunction.apply(u);
    }
  };
  public static final Function<ScreeningRoomUser,String> ToFullNameLastFirstAndIdAndLabName = new Function<ScreeningRoomUser,String>() {
    public String apply(ScreeningRoomUser lh)
    {
      return ScreeningRoomUser.ToFullNameLastFirstAndId.apply(lh)
        + (lh.getLab().getLabAffiliation() == null ? "" : (" - " + lh.getLab().getLabAffiliation().getAffiliationName()));
    }
  };


  // private instance data

  private SortedSet<ChecklistItemEvent> _checklistItemEvents = Sets.newTreeSet();
  private Set<Screen> _screensLed = Sets.newHashSet();
  private Set<Screen> _screensCollaborated = Sets.newHashSet();
  private Set<AttachedFile> _attachedFiles = Sets.newHashSet();
  protected ScreeningRoomUserClassification _userClassification;
  private Set<FacilityUsageRole> _facilityUsageRoles = Sets.newHashSet();
  private String _comsCrhbaPermitNumber;
  private String _comsCrhbaPermitPrincipalInvestigator;
  private ChecklistItemEvent _lastNotifiedSMUAChecklistItemEvent;
  private SortedSet<ServiceActivity> _serviceActivities;

  private LabHead _labHead;
  protected transient Lab _lab;


  // public constructor

  /**
   * Construct an uninitialized <code>ScreeningRoomUser</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
   protected ScreeningRoomUser() {}
   
   /**
    * Construct an uninitialized <code>ScreeningRoomUser</code>.
    * @motivation for new ScreeningRoomUser creation via user interface, where even required
    *             fields are allowed to be uninitialized, initially
    */
   public ScreeningRoomUser(AdministratorUser createdBy) 
   {
     super(createdBy);
   }

  /** @motivation for test code only */
  public ScreeningRoomUser(
    String firstName,
    String lastName,
    ScreeningRoomUserClassification userClassification)
  {
    super(firstName,
          lastName);
    setUserClassification(userClassification);
  }

  /** @motivation for test code only */
  public ScreeningRoomUser(String firstName,
                           String lastName)
  {
    this(firstName,
         lastName,
         ScreeningRoomUserClassification.UNASSIGNED);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the set of checklist item events.
   * @return the checklist item events
   */
  @OneToMany(mappedBy = "screeningRoomUser", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @Sort(type=SortType.NATURAL)
  public SortedSet<ChecklistItemEvent> getChecklistItemEvents()
  {
    return _checklistItemEvents;
  }

  @Transient
  public SortedSet<ChecklistItemEvent> getChecklistItemEvents(ChecklistItem checklistItem)
  {
    SortedSet<ChecklistItemEvent> result = new TreeSet<ChecklistItemEvent>();
    for (ChecklistItemEvent itemEvent : getChecklistItemEvents()) {
      if (itemEvent.getChecklistItem().equals(checklistItem)) {
        result.add(itemEvent);
      }
    }
    return result;
  }

  /**
   * Create a new checklist item activation/completed event for the user.
   * @param checklistItem the checklist item
   * @param datePerformed the date the checklist item was performed by the user or otherwise enacted
   * @return the new checklist item for the user
   * @see ChecklistItemEvent#createChecklistItemExpirationEvent(LocalDate, AdministratorUser)
   */
  public ChecklistItemEvent createChecklistItemActivationEvent(ChecklistItem checklistItem,
                                                               LocalDate datePerformed,
                                                               AdministratorUser recordedBy)
  {
    SortedSet<ChecklistItemEvent> checklistItemEvents = getChecklistItemEvents(checklistItem);
    if (checklistItemEvents.size() > 0) {
      if (!checklistItemEvents.last().isExpiration()) {
        throw new DataModelViolationException("cannot add checklist item activation when checklist item is already activated");
      }
      if (datePerformed.compareTo(checklistItemEvents.last().getDatePerformed()) < 0) {
        throw new DataModelViolationException("checklist item activation date must be on or after the previous expiration date");
      }
    }
    ChecklistItemEvent checklistItemEvent =
      new ChecklistItemEvent(checklistItem,
                             this,
                             datePerformed,
                             recordedBy);
    _checklistItemEvents.add(checklistItemEvent);
    return checklistItemEvent;
  }

  /**
   * Create a new checklist item "not applicable" event for the user. Onlvy
   * valid to call this method if no other events exist for this checklist item,
   * for this user.
   *
   * @param checklistItem the checklist item
   * @param datePerformed the date the checklist item was marked as
   *          "not applicable"
   * @return the new checklist item for the user
   */
  public ChecklistItemEvent createChecklistItemNotApplicableEvent(ChecklistItem checklistItem,
                                                                  LocalDate datePerformed,
                                                                  AdministratorUser recordedBy)
  {
    SortedSet<ChecklistItemEvent> checklistItemEvents = getChecklistItemEvents(checklistItem);
    if (checklistItemEvents.size() > 0) {
        throw new DataModelViolationException("cannot set a checklist item to 'not applicable' if the item is already activated/deactivated/completed");
    }
    ChecklistItemEvent checklistItemEvent =
      new ChecklistItemEvent(checklistItem,
                             this,
                             datePerformed,
                             recordedBy,
                             true);
    _checklistItemEvents.add(checklistItemEvent);
    return checklistItemEvent;
  }


  /**
   * Get the set of screens for which this user is the lead screener.
   * @return the set of screens for which this user is the lead screener
   */
  @OneToMany(
    mappedBy="leadScreener",
    fetch=FetchType.LAZY
  )
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="screenLed", inverseProperty="leadScreener")
  public Set<Screen> getScreensLed()
  {
    return _screensLed;
  }

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
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="screenCollaborated", inverseProperty="collaborators")
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
   * @return a Set of Screens comprised of the screens this user has led and collaborated on.
   */
  @Transient
  public Set<Screen> getAllAssociatedScreens()
  {
    Set<Screen> screens = new HashSet<Screen>();
    screens.addAll(getScreensLed());
    screens.addAll(getScreensCollaborated());
    return screens;
  }

  /**
   * @return the Set of ScreeningRoomUsers that are lab members or the lab head
   *         of this user's lab, and all collaborators on screens this user is
   *         associated with. This user himself is not included in the returned
   *         Set.
   */
  @Transient
  public Set<ScreeningRoomUser> getAssociatedUsers()
  {
    Set<ScreeningRoomUser> associates = Sets.newHashSet(getLab().getLabMembersAndLabHead());
    for (Screen screen : getAllAssociatedScreens()) {
      associates.addAll(screen.getAssociatedScreeningRoomUsers());
    }
    associates.remove(this);
    return associates;
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

  @ElementCollection
  @Column(name="facilityUsageRole", nullable=false)
  @JoinTable(name="screening_room_user_facility_usage_role", joinColumns=@JoinColumn(name="screening_room_user_id"))
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.users.FacilityUsageRole$UserType")
  public Set<FacilityUsageRole> getFacilityUsageRoles()
  {
    return _facilityUsageRoles;
  }

  public void setFacilityUsageRoles(Set<FacilityUsageRole> facilityUsages)
  {
    _facilityUsageRoles = facilityUsages;
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
   * @param comsCrhbaPermitPrincipalInvestigator the new COMS-CRHBA permit principal investigator
   */
  public void setComsCrhbaPermitPrincipalInvestigator(String comsCrhbaPermitPrincipalInvestigator)
  {
    _comsCrhbaPermitPrincipalInvestigator = comsCrhbaPermitPrincipalInvestigator;
  }

  /**
   * Get the attached files.
   * @return the attached files
   */
  @OneToMany(mappedBy = "screeningRoomUser", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @ToMany(hasNonconventionalMutation=true)
  public Set<AttachedFile> getAttachedFiles()
  {
    return _attachedFiles;
  }

  private void setAttachedFiles(Set<AttachedFile> attachedFiles)
  {
    _attachedFiles = attachedFiles;
  }

  public AttachedFile createAttachedFile(String filename, UserAttachedFileType fileType, LocalDate fileDate, String fileContents) throws IOException
  {
    return createAttachedFile(filename, fileType, fileDate, new ByteArrayInputStream(fileContents.getBytes()));
  }

  public AttachedFile createAttachedFile(String filename,
                                         UserAttachedFileType fileType,
                                         LocalDate fileDate,
                                         InputStream fileContents) throws IOException
  {
    AttachedFile attachedFile = new AttachedFile(this, filename, fileType, fileDate, fileContents);
    _attachedFiles.add(attachedFile);
    return attachedFile;
  }

  public void removeAttachedFile(AttachedFile attachedFile)
  {
    _attachedFiles.remove(attachedFile);
  }

  /**
   * Get whether this user is an RNAi screener.
   * @return <code>true</code> iff this user is an RNAi screener.
   */
  @Transient
  public boolean isRnaiUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.RNAI_SCREENS);
  }

  /**
   * Get whether this user is a small molecule screener.
   * @return <code>true</code> iff this user is a small molecule screener.
   */
  @Transient
  public boolean isSmallMoleculeUser()
  {
    return getScreensaverUserRoles().contains(ScreensaverUserRole.SM_DSL_LEVEL3_SHARED_SCREENS);
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
   * @motivation for hibernate
   */
  private void setChecklistItemEvents(SortedSet<ChecklistItemEvent> checklistItemEvents)
  {
    _checklistItemEvents = checklistItemEvents;
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
   * @return the Lab to which this user belongs; a ScreeningRoomUser that is not a LabHead may not have a lab at all, in
   *         which case a Lab will be returned that has a null labHead and empty labName and labAffiliation (Null Object
   *         pattern); modifying its labMembers collection will have not effect on persisted
   *         data.
   * @motivation prevents client code from being able to set properties that
   *             should only be set on the associated LabHead (e.g. labAffiliation).
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
   * Set or change the lab of a non-lab head (i.e., non-Principal Investigator) screening
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
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty = "labMembers")
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
  
  /**
   * Adds the appropriate {@link FacilityUsageRole} for the screen type of the
   * screen with which the user is being associated with.
   */
  public void updateFacilityUsageRoleForAssociatedScreens()
  {
    _facilityUsageRoles.clear();
    for (Screen screen : getAllAssociatedScreens()) {
      if (screen.getScreenType() == ScreenType.SMALL_MOLECULE) {
        getFacilityUsageRoles().add(FacilityUsageRole.SMALL_MOLECULE_SCREENER);
      }
      else if (screen.getScreenType() == ScreenType.RNAI) {
        getFacilityUsageRoles().add(FacilityUsageRole.RNAI_SCREENER);
      }
    }
  }

  /**
   * Get the ChecklistItemEvent of the successful notification, if any, that was sent to this User 
   * for the Small Molecule User Agreement expiration.<p>
   * This is an ICCB-specific property, which should not be part of the Screensaver domain model, but is currently, for practical reasons  .
   */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lastNotifiedSmuaChecklistItemEventId", nullable = true)
  @org.hibernate.annotations.ForeignKey(name = "fk_screening_room_user_to_notified_checklist_item_event")
  @ToOne(hasNonconventionalSetterMethod=true /* this is only set under special circumstances */)
  public ChecklistItemEvent getLastNotifiedSMUAChecklistItemEvent()
  {
    return _lastNotifiedSMUAChecklistItemEvent;
  }

  public void setLastNotifiedSMUAChecklistItemEvent(ChecklistItemEvent lastExpiredSMUAChecklistItemEvent)
  {
    _lastNotifiedSMUAChecklistItemEvent = lastExpiredSMUAChecklistItemEvent;
  }

  @Transient
  public SortedSet<Activity> getAssociatedActivities()
  {
    SortedSet<Activity> activities = Sets.newTreeSet();
    activities.addAll(getActivitiesPerformed());
    activities.addAll(getServiceActivities());
    return activities;
  }

  @OneToMany(mappedBy = "servicedUser")
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName = "serviceActivity")
  @Sort(type = SortType.NATURAL)
  public SortedSet<ServiceActivity> getServiceActivities()
  {
    return _serviceActivities;
  }

  private void setServiceActivities(SortedSet<ServiceActivity> serviceActivities)
  {
    _serviceActivities = serviceActivities;
  }
}
