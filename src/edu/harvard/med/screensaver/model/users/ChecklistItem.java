// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a checklist item.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "screeningRoomUserId", "checklistItemTypeId" }) })
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=ScreeningRoomUser.class)
public class ChecklistItem extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(ChecklistItem.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _checklistItemId;
  private Integer _version;
  private ChecklistItemType _checklistItemType;
  private ScreeningRoomUser _screeningRoomUser;
  private LocalDate _activationDate;
  private String _activationInitials;
  private LocalDate _deactivationDate;
  private String _deactivationInitials;


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
    return getChecklistItemId();
  }

  /**
   * Get the id for the checklist item.
   * @return the id for the checklist item
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="checklist_item_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="checklist_item_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="checklist_item_id_seq")
  public Integer getChecklistItemId()
  {
    return _checklistItemId;
  }

  /**
   * Get the checklist item type.
   * @return the checklist item type
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="checklistItemTypeId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_checklist_item_to_checklist_item_type")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
  public ChecklistItemType getChecklistItemType()
  {
    return _checklistItemType;
  }

  /**
   * Get the screening room user.
   * @return the screening room user
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screeningRoomUserId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_checklist_item_to_screening_room_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  /**
   * Get the activation date.
   * @return the activation date
   */
  @Type(type="org.joda.time.contrib.hibernate.PersistentLocalDate")
  public LocalDate getActivationDate()
  {
    return _activationDate;
  }

  /**
   * Set the activation date.
   * @param activationDate the new activation date
   */
  public void setActivationDate(LocalDate activationDate)
  {
    _activationDate = activationDate;
  }

  /**
   * Get the activation initials.
   * @return the activation initials
   */
  @org.hibernate.annotations.Type(type="text")
  public String getActivationInitials()
  {
    return _activationInitials;
  }

  /**
   * Set the activation initials.
   * @param activationInitials the new activation initials
   */
  public void setActivationInitials(String activationInitials)
  {
    _activationInitials = activationInitials;
  }

  /**
   * Get the deactivation date.
   * @return the deactivation date
   */
  @Type(type="org.joda.time.contrib.hibernate.PersistentLocalDate")
  public LocalDate getDeactivationDate()
  {
    return _deactivationDate;
  }

  /**
   * Set the deactivation date.
   * @param deactivationDate the new deactivation date
   */
  public void setDeactivationDate(LocalDate deactivationDate)
  {
    _deactivationDate = deactivationDate;
  }

  /**
   * Get the deactivation initials.
   * @return the deactivation initials
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDeactivationInitials()
  {
    return _deactivationInitials;
  }

  /**
   * Set the deactivation initials.
   * @param deactivationInitials the new deactivation initials
   */
  public void setDeactivationInitials(String deactivationInitials)
  {
    _deactivationInitials = deactivationInitials;
  }


  // package constructor

  /**
   * Construct an initialized <code>ChecklistItem</code>.
   * <p>
   * Intended only for use by {@link
   * ScreeningRoomUser#createChecklistItem(ChecklistItemType, LocalDate, String, LocalDate, String)}.
   * @param checklistItemType the checklist item type
   * @param screeningRoomUser the screening room user
   * @param activationDate the activation date
   * @param activationInitials the activation initials
   * @param deactivationDate the deactivation date
   * @param deactivationInitials the deactivation initials
   */
  ChecklistItem(
    ChecklistItemType checklistItemType,
    ScreeningRoomUser screeningRoomUser,
    LocalDate activationDate,
    String activationInitials,
    LocalDate deactivationDate,
    String deactivationInitials)
  {
    if (checklistItemType == null || screeningRoomUser == null) {
      throw new NullPointerException();
    }
    _checklistItemType = checklistItemType;
    _screeningRoomUser = screeningRoomUser;
    _activationDate = activationDate;
    _activationInitials = activationInitials;
    _deactivationDate = deactivationDate;
    _deactivationInitials = deactivationInitials;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>ChecklistItem</code> object.
   *
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ChecklistItem() {}


  // private constructor and instance methods

  /**
   * Set the id for the checklist item.
   *
   * @param checklistItemId the new id for the checklist item
   * @motivation for hibernate
   */
  private void setChecklistItemId(Integer checklistItemId)
  {
    _checklistItemId = checklistItemId;
  }

  /**
   * Get the version for the checklist item.
   * @return the version for the checklist item
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the checklist item.
   * @param version the new version for the checklist item
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the checklist item type.
   * @param checklistItemType the new checklist item type
   * @motivation for hibernate
   */
  private void setChecklistItemType(ChecklistItemType checklistItemType)
  {
    _checklistItemType = checklistItemType;
  }

  /**
   * Set the screening room user.
   * @param screeningRoomUser the new screening room user
   * @motivation for hibernate
   */
  private void setScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    _screeningRoomUser = screeningRoomUser;
  }
}
