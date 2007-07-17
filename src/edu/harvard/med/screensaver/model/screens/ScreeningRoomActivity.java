// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.EntityIdProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a screening room activity.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="screening_room_activity" lazy="true"
 * @hibernate.joined-subclass-key column="activity_id"
 */
public abstract class ScreeningRoomActivity extends Activity 
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ScreeningRoomActivity.class);
  private static final long serialVersionUID = 0L;

  
  // instance fields

  private Screen _screen;
  private Integer _ordinal;
  private Set<EquipmentUsed> _equipmentUsed = new HashSet<EquipmentUsed>();
  private BigDecimal _microliterVolumeTransferedPerWell; // spelling error! "transferred"

  
  // public constructor

  /**
   * Constructs an initialized <code>ScreeningRoomActivity</code> object.
   *
   * @param screen the screen
   * @param performedBy the user that performed the activity
   * @param dateCreated the date created
   * @param dateOfActivity the date the screening room activity took place
   * @throws DuplicateEntityException 
   */
  public ScreeningRoomActivity(
    Screen screen,
    ScreensaverUser performedBy,
    Date dateCreated,
    Date dateOfActivity) throws DuplicateEntityException
  {
    super(performedBy, dateCreated, dateOfActivity, false);
    
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _ordinal = _screen.getAllTimeScreeningRoomActivityCount();
    _screen.setAllTimeScreeningRoomActivityCount(_ordinal + 1);
    if (!_screen.getScreeningRoomActivities().add(this)) {
      throw new DuplicateEntityException(_screen, this);
    }
    if (!_performedBy.getHbnActivitiesPerformed().add(this)) {
      throw new DuplicateEntityException(_performedBy, this);
    }
  }


  // public methods

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_screening_room_activity_to_screen"
   *   cascade="none"
   */
  @ToOneRelationship(nullable=false, inverseProperty="screeningRoomActivities")
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * @hibernate.property type="integer" not-null="true"
   * @return
   */
  @EntityIdProperty
  public Integer getOrdinal()
  {
    return _ordinal;
  }

  public void setOrdinal(Integer ordinal)
  {
    _ordinal = ordinal;
  }

  /**
   * Get the volume transferred per well, in microliters
   * @return the volume transferred per well, in microliters
   * @hibernate.property type="big_decimal"
   */
  public BigDecimal getMicroliterVolumeTransferedPerWell()
  {
    return _microliterVolumeTransferedPerWell;
  }

  /**
   * Set the volume transferred per well, in microliters
   * @param microliterVolumeTransferedPerWell the new volume transferrde per well, in microliters
   */
  public void setMicroliterVolumeTransferedPerWell(
    BigDecimal microliterVolumeTransferedPerWell)
  {
    if (microliterVolumeTransferedPerWell == null) {
      _microliterVolumeTransferedPerWell = null;
    } 
    else {
      _microliterVolumeTransferedPerWell = microliterVolumeTransferedPerWell.setScale(Well.VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }

  /**
   * Get the equipment used.
   *
   * @return the equipment used
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="screening_room_activity_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.EquipmentUsed"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="screeningRoomActivity")
  public Set<EquipmentUsed> getEquipmentUsed()
  {
    return _equipmentUsed;
  }

  public int compareTo(Object o)
  {
    if (o instanceof ScreeningRoomActivity) {
      ScreeningRoomActivity other = (ScreeningRoomActivity) o;
      return getDateOfActivity().compareTo(other.getDateOfActivity());
    }
    return 0;
  }

  
  // protected constructor
  
  /**
   * Construct an uninitialized <code>ScreeningRoomActivity</code> object.
   *
   * @motivation for hibernate
   */
  protected ScreeningRoomActivity() {}


  // protected methods
  
  /**
   * A business key class for the ScreeningRoomActivity.
   */
  private class BusinessKey
  {
    
    /**
     * Get the screen.
     *
     * @return the screen
     */
    public Screen getScreen()
    {
      return _screen;
    }

    public Integer getOrdinal()
    {
      return _ordinal;
    }
    
    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        this.getScreen().equals(that.getScreen()) &&
        this.getOrdinal().equals(that.getOrdinal());
    }

    @Override
    public int hashCode()
    {
      return
        this.getScreen().hashCode() +
        163 * this.getOrdinal().hashCode();
    }

    @Override
    public String toString()
    {
      return this.getScreen() + ":" + this.getOrdinal();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // private methods

  /**
   * Set the screen.
   * Throw a NullPointerException when the screen is null.
   *
   * @param screen the new screen
   * @throws NullPointerException when the screen is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreen(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }

  /**
   * Set the equipment used.
   *
   * @param equipmentUsed the new equipment used
   * @motivation for hibernate
   */
  private void setEquipmentUsed(Set<EquipmentUsed> equipmentUsed)
  {
    _equipmentUsed = equipmentUsed;
  }
}
