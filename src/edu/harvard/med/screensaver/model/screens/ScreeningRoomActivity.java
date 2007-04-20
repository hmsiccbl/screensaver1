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

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.EntityIdProperty;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a screening room activity.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public abstract class ScreeningRoomActivity extends AbstractEntity implements Comparable
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ScreeningRoomActivity.class);
  private static final long serialVersionUID = 0L;

  /**
   * The number of decimal places used when recording volume values.
   */
  public static int VOLUME_SCALE = 2;

  
  // instance fields

  private Integer _screeningRoomActivityId;
  private Integer _version;
  private Screen _screen;
  private Integer _ordinal;
  private ScreensaverUser _performedBy;
  private Set<EquipmentUsed> _equipmentUsed = new HashSet<EquipmentUsed>();
  private BigDecimal _microliterVolumeTransferedPerWell; // spelling error! "transferred"
  private Date _dateCreated;
  private Date _dateOfActivity;
  private String _comments;

  
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
    if (screen == null || performedBy == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _performedBy = performedBy;
    _dateCreated = truncateDate(dateCreated);
    _dateOfActivity = truncateDate(dateOfActivity);
    _ordinal = _screen.getAllTimeScreeningRoomActivityCount();
    _screen.setAllTimeScreeningRoomActivityCount(_ordinal + 1);
    if (!_screen.getScreeningRoomActivities().add(this)) {
      throw new DuplicateEntityException(_screen, this);
    }
    if (!_performedBy.getHbnScreeningRoomActivitiesPerformed().add(this)) {
      _screen.getScreeningRoomActivities().remove(this);
      throw new DuplicateEntityException(_performedBy, this);
    }
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getScreeningRoomActivityId();
  }

  /**
   * Get the id for the screening room activity.
   *
   * @return the id for the screening room activity
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screening_room_activity_id_seq"
   */
  public Integer getScreeningRoomActivityId()
  {
    return _screeningRoomActivityId;
  }
  
  @ImmutableProperty
  abstract public String getActivityTypeName();

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
   * Get the user that performed the activity.
   *
   * @return the user that performed the activity
   */
  @ToOneRelationship(nullable=false, inverseProperty="screeningRoomActivitiesPerformed")
  public ScreensaverUser getPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Set the user that performed the activity.
   *
   * @param performedBy the new user that performed the activity
   */
  public void setPerformedBy(ScreensaverUser performedBy)
  {
    _performedBy.getHbnScreeningRoomActivitiesPerformed().remove(this);
    _performedBy = performedBy;
    _performedBy.getHbnScreeningRoomActivitiesPerformed().add(this);
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
      _microliterVolumeTransferedPerWell = microliterVolumeTransferedPerWell.setScale(VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }

  /**
   * Get the date the activity entity was created.
   *
   * @return the date the activity entity was created
   * @hibernate.property
   *   not-null="true"
   */
  @ImmutableProperty
  public Date getDateCreated()
  {
    return _dateCreated;
  }

  /**
   * Get the date the activity was performed.
   *
   * @return the date the activity was performed
   * @hibernate.property
   *   not-null="true"
   */
  @ImmutableProperty
  public Date getDateOfActivity()
  {
    return _dateOfActivity;
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the equipment used.
   *
   * @return the equipment used
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="true"
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
   * Set the id for the screening room activity.
   *
   * @param screeninRoomActivityId the new id for the screening room activity
   * @motivation for hibernate
   */
  private void setScreeningRoomActivityId(Integer screeninRoomActivityId) {
    _screeningRoomActivityId = screeninRoomActivityId;
  }
  
  /**
   * Get the user that performed the activity.
   *
   * @return the user that performed the activity
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreensaverUser"
   *   column="performed_by_id"
   *   not-null="true"
   *   foreign-key="fk_screening_room_activity_to_performed_by"
   *   cascade="none"
   * @motivation for hibernate
   */
  private ScreensaverUser getHbnPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Set the user that performed the activity.
   *
   * @param performedBy the new user that performed the activity
   * @motivation for hibernate
   */
  private void setHbnPerformedBy(ScreensaverUser performedBy)
  {
    _performedBy = performedBy;
  }


  /**
   * Set the date the activity entity was created.
   *
   * @param dateCreated the new date the activity entity was created
   * @motivation for hibernate
   */
  private void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated);
  }


  /**
   * Set the date the activity was performed.
   *
   * @param dateCreated the new date the activity was performed.
   */
  private void setDateOfActivity(Date dateOfActivity)
  {
    _dateOfActivity = truncateDate(dateOfActivity);
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

  /**
   * Get the version for the screening room activity.
   *
   * @return the version for the screening room activity
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the screening room.
   *
   * @param version the new version for the screening room 
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
}
