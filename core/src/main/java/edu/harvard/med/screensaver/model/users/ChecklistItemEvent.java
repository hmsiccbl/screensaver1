// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


/**
 * A ChecklistItemEvent is used to either 1) mark a non-expirable ChecklistItem
 * as "completed" or 2) mark an expirable ChecklistItem as either "activated" or
 * "expired". All ChecklistItemEvents have an associated
 * AdministrativeActivity that records the administrator that entered the event
 * and the date it was entered.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass = ScreeningRoomUser.class)
public class ChecklistItemEvent extends AuditedAbstractEntity<Integer> implements Comparable<ChecklistItemEvent>
{
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<ChecklistItemEvent> screeningRoomUser = RelationshipPath.from(ChecklistItemEvent.class).to("screeningRoomUser", Cardinality.TO_ONE);
  public static final RelationshipPath<ChecklistItemEvent> checklistItem = RelationshipPath.from(ChecklistItemEvent.class).to("checklistItem", Cardinality.TO_ONE);

  private ChecklistItem _checklistItem;
  private ScreeningRoomUser _screeningRoomUser;
  private boolean _isExpiration;
  private boolean _isNotApplicable;
  private LocalDate _datePerformed;

  
  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the id for the checklist item.
   * 
   * @return the id for the checklist item
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(name = "checklist_item_event_id_seq", strategy = "sequence", parameters = { @Parameter(name = "sequence", value = "checklist_item_event_id_seq") })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checklist_item_event_id_seq")
  public Integer getChecklistItemEventId()
  {
    return getEntityId();
  }

  /**
   * Get the checklist item.
   * 
   * @return the checklist item
   */
  @ManyToOne(fetch = FetchType.LAZY, cascade = {})
  @JoinColumn(name = "checklistItemId", nullable = false, updatable = false)
  //@org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name = "fk_checklist_item_event_to_checklist_item")
  @org.hibernate.annotations.LazyToOne(org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade({})
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional = true)
  public ChecklistItem getChecklistItem()
  {
    return _checklistItem;
  }

  // TODO: we never intend to use this, but need it to allow unit tests to pass
  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="checklistItemEventUpdateActivity", 
             joinColumns=@JoinColumn(name="checklistItemEventId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false, unique=true))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  @Sort(type=SortType.NATURAL)            
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  /**
   * For checklist items that are {@link ChecklistItem#isExpirable() expirable},
   * get whether this checklist item event represents the activation or the
   * expiration event.
   */
  @Column(nullable = false, updatable = false, name = "isExpiration")
  public boolean isExpiration()
  {
    return _isExpiration;
  }

  /**
   * If set, indicates that this checklist item is "not applicable"
   */
  @Column(nullable = false, updatable = false, name = "isNotApplicable")
  public boolean isNotApplicable()
  {
    return _isNotApplicable;
  }

  /**
   * Get the screening room user.
   * 
   * @return the screening room user
   */
  @ManyToOne(fetch = FetchType.LAZY, cascade = { /*CascadeType.PERSIST, CascadeType.MERGE*/ })
  @JoinColumn(name = "screeningRoomUserId", nullable = false, updatable = false)
  @org.hibernate.annotations.ForeignKey(name = "fk_checklist_item_event_to_screening_room_user")
  @org.hibernate.annotations.Cascade({ /*org.hibernate.annotations.CascadeType.SAVE_UPDATE*/ })
  @org.hibernate.annotations.LazyToOne(org.hibernate.annotations.LazyToOneOption.PROXY)
  @ToOne(unidirectional=true)
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  /**
   * @return the date the checklist item was performed by the user or otherwise
   *         enacted
   */
  @Column(updatable=false)
  @Type(type = "edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  //@Immutable
  public LocalDate getDatePerformed()
  {
    return _datePerformed;
  }

  /**
   * Set the date of the event.
   * 
   * @param datePerformed the new date of the event
   */
  private void setDatePerformed(LocalDate datePerformed)
  {
    _datePerformed = datePerformed;
  }

  /**
   * Construct an initialized "activation" or "completed" <code>ChecklistItemEvent</code> 
   * <p>
   * Intended only for use by {@link ScreeningRoomUser}.
   * 
   * @param checklistItem the checklist item
   * @param screeningRoomUser the screening room user to which this checklist
   *          item event applies
   * @param datePerformed the date the checklist item event was performed by the user
   *          or otherwise enacted
   */
  ChecklistItemEvent(ChecklistItem checklistItem,
                     ScreeningRoomUser screeningRoomUser,
                     LocalDate datePerformed,
                     AdministratorUser recordedBy)
  {
    super(recordedBy);
    if (checklistItem == null || screeningRoomUser == null ||
        datePerformed == null || recordedBy == null) {
      throw new NullPointerException();
    }
    _checklistItem = checklistItem;
    _screeningRoomUser = screeningRoomUser;
    _isExpiration = false;
    _isNotApplicable = false;
    _datePerformed = datePerformed;
  }
  
  /**
   * Construct an initialized "not applicable" <code>ChecklistItemEvent</code> 
   * <p>
   * Intended only for use by {@link ScreeningRoomUser}.
   **/
  ChecklistItemEvent(ChecklistItem checklistItem,
                     ScreeningRoomUser screeningRoomUser,
                     LocalDate datePerformed,
                     AdministratorUser recordedBy,
                     boolean isNotApplicable)
  {
    this(checklistItem, screeningRoomUser, datePerformed, recordedBy);
    _isNotApplicable = isNotApplicable;
  }

  public ChecklistItemEvent createChecklistItemExpirationEvent(LocalDate datePerformed, AdministratorUser recordedBy)
  {
    if (!getChecklistItem().isExpirable()) {
      throw new DataModelViolationException("cannot expire checklist item " + getChecklistItem().getItemName());
    }
    SortedSet<ChecklistItemEvent> checklistItemEvents = getScreeningRoomUser().getChecklistItemEvents(getChecklistItem());
    if (checklistItemEvents.isEmpty()) {
      throw new DataModelViolationException("cannot add checklist item expiration when checklist item has not yet been activated");
    }
    ChecklistItemEvent previousEvent = checklistItemEvents.last();
    if (!previousEvent.equals(this)) {
      throw new DataModelViolationException("can only apply expiration to the last checklist event of this type");
    }
    if (previousEvent.isExpiration()) {
      throw new DataModelViolationException("can only expire a previously active checklist item");
    }
    if (previousEvent.isNotApplicable()) {
      throw new DataModelViolationException("can not expire a checklist item that has been marked \"not applicable\"");
    }
    if (datePerformed.compareTo(previousEvent.getDatePerformed()) < 0) {
      throw new DataModelViolationException("checklist item expiration date must be on or after the previous activation date");
    }
    ChecklistItemEvent checklistItemExpiration = new ChecklistItemEvent(this.getChecklistItem(),
                                                                        this.getScreeningRoomUser(),
                                                                        datePerformed,
                                                                        recordedBy);
    checklistItemExpiration._isExpiration = true;
    this.getScreeningRoomUser().getChecklistItemEvents().add(checklistItemExpiration);
    return checklistItemExpiration;
  }

  /**
   * Construct an uninitialized <code>ChecklistItem</code> object.
   * 
   * @motivation for hibernate and proxy/concrete subclass constructors
   * @motivation for user interface entity creation
   */
  public ChecklistItemEvent()
  {}


  // private methods

  /**
   * @motivation for hibernate
   */
  private void setChecklistItemEventId(Integer checklistItemEventId)
  {
    setEntityId(checklistItemEventId);
  }

  /**
   * @motivation for hibernate
   */
  private void setChecklistItem(ChecklistItem checklistItem)
  {
    _checklistItem = checklistItem;
  }

  /**
   * @motivation for hibernate
   */
  private void setScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    _screeningRoomUser = screeningRoomUser;
  }

  private void setExpiration(boolean isExpiration)
  {
    _isExpiration = isExpiration;
  }

  private void setNotApplicable(boolean value)
  {
    _isNotApplicable = value;
  }


  private static ChecklistItemEventComparator _checklistItemEventComparator = new ChecklistItemEventComparator();

  public int compareTo(ChecklistItemEvent other)
  {
    return _checklistItemEventComparator.compare(this, other);
  }
}
