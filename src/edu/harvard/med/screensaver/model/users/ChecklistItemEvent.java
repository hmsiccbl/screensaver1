// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.DataModelViolationException;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


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
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass = ScreeningRoomUser.class)
public class ChecklistItemEvent extends AbstractEntity implements Comparable<ChecklistItemEvent>
{

  // static fields

  private static final Logger log = Logger.getLogger(ChecklistItemEvent.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _checklistItemId;
  private Integer _version;
  private ChecklistItem _checklistItem;
  private ScreeningRoomUser _screeningRoomUser;
  private boolean _isExpiration;
  private LocalDate _datePerformed;
  private AdministrativeActivity _entryActivity;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getChecklistItemEventId();
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
    return _checklistItemId;
  }

  /**
   * Get the checklist item.
   * 
   * @return the checklist item
   */
  @ManyToOne(fetch = FetchType.LAZY, cascade = {})
  @JoinColumn(name = "checklistItemId", nullable = false, updatable = false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name = "fk_checklist_item_event_to_checklist_item")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value = {})
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional = true)
  public ChecklistItem getChecklistItem()
  {
    return _checklistItem;
  }

  /**
   * For checklist items that are {@link ChecklistItem#isExpirable() expirable},
   * get whether this checklist item event represents the activation or the
   * expiration event.
   */
  @Column(nullable = false, updatable = false, name = "isExpiration")
  @Immutable
  public boolean isExpiration()
  {
    return _isExpiration;
  }

  /**
   * Get the screening room user.
   * 
   * @return the screening room user
   */
  @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name = "screeningRoomUserId", nullable = false, updatable = false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name = "fk_checklist_item_event_to_screening_room_user")
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  /**
   * @return the date the checklist item was performed by the user or otherwise
   *         enacted
   */
  @Column(updatable=false)
  @Type(type = "edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  @Immutable
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

  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, updatable = false, name = "entry_admin_activity_id")
  @org.hibernate.annotations.ForeignKey(name = "fk_well_to_entry_admin_activity")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional = true)
  @Immutable
  public AdministrativeActivity getEntryActivity()
  {
    return _entryActivity;
  }


  // package constructor

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
   * @param entryActivity the administrative activity that tracks the
   *          who/when/why of this checklist item event information
   */
  ChecklistItemEvent(ChecklistItem checklistItem,
                     ScreeningRoomUser screeningRoomUser,
                     LocalDate datePerformed,
                     AdministrativeActivity entryActivity)
  {
    if (checklistItem == null || screeningRoomUser == null ||
        datePerformed == null || entryActivity == null) {
      throw new NullPointerException();
    }
    _checklistItem = checklistItem;
    _screeningRoomUser = screeningRoomUser;
    _isExpiration = false;
    _datePerformed = datePerformed;
    _entryActivity = entryActivity;
  }

  public ChecklistItemEvent createChecklistItemExpirationEvent(LocalDate datePerformed,
                                                               AdministrativeActivity entryActivity)
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
    if (datePerformed.compareTo(previousEvent.getDatePerformed()) < 0) {
      throw new DataModelViolationException("checklist item expiration date must be on or after the previous activation date");
    }
    ChecklistItemEvent checklistItemExpiration = new ChecklistItemEvent(this.getChecklistItem(),
                                                                        this.getScreeningRoomUser(),
                                                                        datePerformed,
                                                                        entryActivity);
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
    _checklistItemId = checklistItemEventId;
  }

  /**
   * @motivation for hibernate
   */
  @Version
  @Column(nullable = false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
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

  private void setEntryActivity(AdministrativeActivity entryActivity)
  {
    if (!isHibernateCaller()) {
      if (entryActivity != null &&
          entryActivity.getType() != AdministrativeActivityType.CHECKLIST_ITEM_EVENT) {
        throw new DataModelViolationException("can only add AdministrativeActivity of type " +
                                              AdministrativeActivityType.CHECKLIST_ITEM_EVENT);
      }
    }
    _entryActivity = entryActivity;
  }

  private void setExpiration(boolean isExpiration)
  {
    _isExpiration = isExpiration;
  }

  private static ChecklistItemEventComparator _checklistItemEventComparator = new ChecklistItemEventComparator();

  public int compareTo(ChecklistItemEvent other)
  {
    return _checklistItemEventComparator.compare(this, other);
  }
}
