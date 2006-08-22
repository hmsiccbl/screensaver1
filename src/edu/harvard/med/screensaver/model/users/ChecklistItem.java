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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a checklist item.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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
  private Date _activationDate;
  private String _activationInitials;
  private Date _deactivationDate;
  private String _deactivationInitials;


  // public constructor

  /**
   * Constructs an initialized <code>ChecklistItem</code> object.
   *
   * @param checklistItemType the checklist item type
   * @param screeningRoomUser the screening room user
   * @param activationDate the activation date
   * @param activationInitials the activation initials
   */
  public ChecklistItem(
    ChecklistItemType checklistItemType,
    ScreeningRoomUser screeningRoomUser,
    Date activationDate,
    String activationInitials)
  {
    if (checklistItemType == null || screeningRoomUser == null) {
      throw new NullPointerException();
    }
    _checklistItemType = checklistItemType;
    _screeningRoomUser = screeningRoomUser;
    _activationDate = truncateDate(activationDate);
    _activationInitials = activationInitials;
    _checklistItemType.getHbnChecklistItems().add(this);
    _screeningRoomUser.getHbnChecklistItems().add(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getChecklistItemId();
  }

  /**
   * Get the id for the checklist item.
   *
   * @return the id for the checklist item
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="checklist_item_id_seq"
   */
  public Integer getChecklistItemId()
  {
    return _checklistItemId;
  }

  /**
   * Get the checklist item type.
   *
   * @return the checklist item type
   */
  public ChecklistItemType getChecklistItemType()
  {
    return _checklistItemType;
  }

  /**
   * Set the checklist item type.
   *
   * @param checklistItemType the new checklist item type
   */
  public void setChecklistItemType(ChecklistItemType checklistItemType)
  {
    if (checklistItemType == null) {
      throw new NullPointerException();
    }
    _checklistItemType.getHbnChecklistItems().remove(this);
    _screeningRoomUser.getHbnChecklistItems().remove(this);
    _checklistItemType = checklistItemType;
    _checklistItemType.getHbnChecklistItems().add(this);
    _screeningRoomUser.getHbnChecklistItems().add(this);
  }

  /**
   * Get the screening room user.
   *
   * @return the screening room user
   */
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  /**
   * Set the screening room user.
   *
   * @param screeningRoomUser the new screening room user
   */
  public void setScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    if (screeningRoomUser == null) {
      throw new NullPointerException();
    }
    _checklistItemType.getHbnChecklistItems().remove(this);
    _screeningRoomUser.getHbnChecklistItems().remove(this);
    _screeningRoomUser = screeningRoomUser;
    _checklistItemType.getHbnChecklistItems().add(this);
    _screeningRoomUser.getHbnChecklistItems().add(this);
  }

  /**
   * Get the activation date.
   *
   * @return the activation date
   * @hibernate.property
   *   not-null="true"
   */
  public Date getActivationDate()
  {
    return _activationDate;
  }

  /**
   * Set the activation date.
   *
   * @param activationDate the new activation date
   */
  public void setActivationDate(Date activationDate)
  {
    _activationDate = truncateDate(activationDate);
  }

  /**
   * Get the activation initials.
   *
   * @return the activation initials
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getActivationInitials()
  {
    return _activationInitials;
  }

  /**
   * Set the activation initials.
   *
   * @param activationInitials the new activation initials
   */
  public void setActivationInitials(String activationInitials)
  {
    _activationInitials = activationInitials;
  }

  /**
   * Get the deactivation date.
   *
   * @return the deactivation date
   * @hibernate.property
   */
  public Date getDeactivationDate()
  {
    return _deactivationDate;
  }

  /**
   * Set the deactivation date.
   *
   * @param deactivationDate the new deactivation date
   */
  public void setDeactivationDate(Date deactivationDate)
  {
    _deactivationDate = truncateDate(deactivationDate);
  }

  /**
   * Get the deactivation initials.
   *
   * @return the deactivation initials
   * @hibernate.property
   *   type="text"
   */
  public String getDeactivationInitials()
  {
    return _deactivationInitials;
  }

  /**
   * Set the deactivation initials.
   *
   * @param deactivationInitials the new deactivation initials
   */
  public void setDeactivationInitials(String deactivationInitials)
  {
    _deactivationInitials = deactivationInitials;
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the checklist item type.
     *
     * @return the checklist item type
     */
    public ChecklistItemType getChecklistItemType()
    {
      return _checklistItemType;
    }
    
    /**
     * Get the screening room user.
     *
     * @return the screening room user
     */
    public ScreeningRoomUser getScreeningRoomUser()
    {
      return _screeningRoomUser;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getChecklistItemType().equals(that.getChecklistItemType()) &&
        getScreeningRoomUser().equals(that.getScreeningRoomUser());
    }

    @Override
    public int hashCode()
    {
      return
        getChecklistItemType().hashCode() +
        getScreeningRoomUser().hashCode();
    }

    @Override
    public String toString()
    {
      return getChecklistItemType() + ":" + getScreeningRoomUser();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the checklist item type.
   * Throw a NullPointerException when the checklist item type is null.
   *
   * @param checklistItemType the new checklist item type
   * @throws NullPointerException when the checklist item type is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnChecklistItemType(ChecklistItemType checklistItemType)
  {
    if (checklistItemType == null) {
      throw new NullPointerException();
    }
    _checklistItemType = checklistItemType;
  }

  /**
   * Set the screening room user.
   * Throw a NullPointerException when the screening room user is null.
   *
   * @param screeningRoomUser the new screening room user
   * @throws NullPointerException when the screening room user is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    if (screeningRoomUser == null) {
      throw new NullPointerException();
    }
    _screeningRoomUser = screeningRoomUser;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>ChecklistItem</code> object.
   *
   * @motivation for hibernate
   */
  private ChecklistItem() {}


  // private methods

  /**
   * Set the id for the checklist item.
   *
   * @param checklistItemId the new id for the checklist item
   * @motivation for hibernate
   */
  private void setChecklistItemId(Integer checklistItemId) {
    _checklistItemId = checklistItemId;
  }

  /**
   * Get the version for the checklist item.
   *
   * @return the version for the checklist item
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the checklist item.
   *
   * @param version the new version for the checklist item
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the checklist item type.
   *
   * @return the checklist item type
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ChecklistItemType"
   *   column="checklist_item_type_id"
   *   not-null="true"
   *   foreign-key="fk_checklist_item_to_checklist_item_type"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ChecklistItemType getHbnChecklistItemType()
  {
    return _checklistItemType;
  }

  /**
   * Get the screening room user.
   *
   * @return the screening room user
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="screensaver_user_id"
   *   not-null="true"
   *   foreign-key="fk_checklist_item_to_screening_room_user"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ScreeningRoomUser getHbnScreeningRoomUser()
  {
    return _screeningRoomUser;
  }
}
