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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;


/**
 * A Hibernate entity bean representing a screening room activity.
 * <p>
 * TODO: consider renaming to <code>ScreenActivity</code>.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_screening_room_activity_to_activity")
@org.hibernate.annotations.Proxy
public abstract class ScreeningRoomActivity extends Activity
{

  // private static fields

  private static final Logger log = Logger.getLogger(ScreeningRoomActivity.class);
  private static final long serialVersionUID = 0L;


  // private instance fields

  private Screen _screen;
  private Set<EquipmentUsed> _equipmentUsed = new HashSet<EquipmentUsed>();
  private BigDecimal _microliterVolumeTransferredPerWell;


  // public instance methods

  @Override
  public int compareTo(Object o)
  {
    if (o instanceof ScreeningRoomActivity) {
      ScreeningRoomActivity other = (ScreeningRoomActivity) o;
      return getDateOfActivity().compareTo(other.getDateOfActivity());
    }
    return 0;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_screening_room_activity_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="screeningRoomActivities")
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the volume transferred per well, in microliters
   * @return the volume transferred per well, in microliters
   */
  @org.hibernate.annotations.Type(type="big_decimal")
  public BigDecimal getMicroliterVolumeTransferedPerWell()
  {
    return _microliterVolumeTransferredPerWell;
  }

  /**
   * Set the volume transferred per well, in microliters
   * @param microliterVolumeTransferedPerWell the new volume transferrde per well, in microliters
   */
  public void setMicroliterVolumeTransferedPerWell(
    BigDecimal microliterVolumeTransferedPerWell)
  {
    if (microliterVolumeTransferedPerWell == null) {
      _microliterVolumeTransferredPerWell = null;
    }
    else {
      _microliterVolumeTransferredPerWell = microliterVolumeTransferedPerWell.setScale(Well.VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }

  /**
   * Get the equipment used.
   * @return the equipment used
   */
  @OneToMany(
    mappedBy="screeningRoomActivity",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  @edu.harvard.med.screensaver.model.annotations.OneToMany(
    singularPropertyName="equipmentUsed"
      //,
    //inverseProperty="screeningRoomActivity"
  )
  public Set<EquipmentUsed> getEquipmentUsed()
  {
    return _equipmentUsed;
  }

  /**
   * Create and return a new equipment used for the screening room activity.
   * @param equipment the equipment
   * @param protocol the protocol
   * @param description the description
   * @return a new equipment used for the screening room activity
   */
  public EquipmentUsed createEquipmentUsed(
    String equipment,
    String protocol,
    String description)
  {
    EquipmentUsed equipmentUsed = new EquipmentUsed(this, equipment, protocol, description);
    _equipmentUsed.add(equipmentUsed);
    return equipmentUsed;
  }


  // protected constructors

  /**
   * Construct an initialized <code>ScreeningRoomActivity</code>.
   * @param screen the screen
   * @param performedBy the user that performed the activity
   * @param dateCreated the date created
   * @param dateOfActivity the date the screening room activity took place
   */
  protected ScreeningRoomActivity(
    Screen screen,
    ScreensaverUser performedBy,
    Date dateCreated,
    Date dateOfActivity)
  {
    super(performedBy, dateCreated, dateOfActivity);
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }

  /**
   * Construct an uninitialized <code>ScreeningRoomActivity</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ScreeningRoomActivity() {}


  // private methods

  /**
   * Set the screen.
   * @param screen the new screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the equipment used.
   * @param equipmentUsed the new equipment used
   * @motivation for hibernate
   */
  private void setEquipmentUsed(Set<EquipmentUsed> equipmentUsed)
  {
    _equipmentUsed = equipmentUsed;
  }
}
