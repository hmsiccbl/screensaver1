// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.ToOneRelationship;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a equipment used.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class EquipmentUsed extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(EquipmentUsed.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _equipmentUsedId;
  private Integer _version;
  private ScreeningRoomActivity _screeningRoomActivity;
  private String _equipment;
  private String _protocol;
  private String _description;


  // public constructor

  /**
   * Constructs an initialized <code>EquipmentUsed</code> object.
   *
   * @param screeningRoomActivity the screening room activity
   * @param equipment the equipment
   * @param protocol the protocol
   * @param description the description
   */
  public EquipmentUsed(
    ScreeningRoomActivity screeningRoomActivity,
    String equipment,
    String protocol,
    String description)
  {
    if (screeningRoomActivity == null) {
      throw new NullPointerException();
    }
    _screeningRoomActivity = screeningRoomActivity;
    _equipment = equipment;
    _protocol = protocol;
    _description = description;
    _screeningRoomActivity.getEquipmentUsed().add(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getEquipmentUsedId();
  }

  /**
   * Get the id for the equipment used.
   *
   * @return the id for the equipment used
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="equipment_used_id_seq"
   */
  public Integer getEquipmentUsedId()
  {
    return _equipmentUsedId;
  }

  /**
   * Get the screening room activity.
   *
   * @return the screening room activity
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity"
   *   column="screening_room_activity_id"
   *   not-null="true"
   *   foreign-key="fk_equipment_used_to_screening_room_activity"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false, inverseProperty="equipmentUsed")
  public ScreeningRoomActivity getScreeningRoomActivity()
  {
    return _screeningRoomActivity;
  }

  /**
   * Get the equipment.
   *
   * @return the equipment
   */
  public String getEquipment()
  {
    return _equipment;
  }

  /**
   * Set the equipment.
   *
   * @param equipment the new equipment
   */
  public void setEquipment(String equipment)
  {
    _equipment = equipment;
  }

  /**
   * Get the protocol.
   *
   * @return the protocol
   * @hibernate.property
   *   type="text"
   */
  public String getProtocol()
  {
    return _protocol;
  }

  /**
   * Set the protocol.
   *
   * @param protocol the new protocol
   */
  public void setProtocol(String protocol)
  {
    _protocol = protocol;
  }

  /**
   * Get the description.
   *
   * @return the description
   * @hibernate.property
   *   type="text"
   */
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set the description.
   *
   * @param description the new description
   */
  public void setDescription(String description)
  {
    _description = description;
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the screening room activity.
     *
     * @return the screening room activity
     */
    public ScreeningRoomActivity getScreeningRoomActivity()
    {
      return _screeningRoomActivity;
    }
    
    /**
     * Get the equipment.
     *
     * @return the equipment
     */
    public String getEquipment()
    {
      return _equipment;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        this.getScreeningRoomActivity().equals(that.getScreeningRoomActivity()) &&
        this.getEquipment().equals(that.getEquipment());
    }

    @Override
    public int hashCode()
    {
      return
        getScreeningRoomActivity().hashCode() +
        getEquipment().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreeningRoomActivity() + ":" + getEquipment();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // private constructor

  /**
   * Construct an uninitialized <code>EquipmentUsed</code> object.
   *
   * @motivation for hibernate
   */
  private EquipmentUsed() {}


  // private methods

  /**
   * Set the screening room activity.
   *
   * @param screening room activity the new screening room activity
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreeningRoomActivity(ScreeningRoomActivity screeningRoomActivity)
  {
    _screeningRoomActivity = screeningRoomActivity;
  }

  /**
   * Set the id for the equipment used.
   *
   * @param equipmentUsedId the new id for the equipment used
   * @motivation for hibernate
   */
  private void setEquipmentUsedId(Integer equipmentUsedId) {
    _equipmentUsedId = equipmentUsedId;
  }

  /**
   * Get the version for the equipment used.
   *
   * @return the version for the equipment used
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the equipment used.
   *
   * @param version the new version for the equipment used
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the equipment.
   *
   * @return the equipment
   * @hibernate.property
   *   column="equipment"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnEquipment()
  {
    return _equipment;
  }

  /**
   * Set the equipment.
   *
   * @param equipment the new equipment
   * @motivation for hibernate
   */
  private void setHbnEquipment(String equipment)
  {
    _equipment = equipment;
  }
}
