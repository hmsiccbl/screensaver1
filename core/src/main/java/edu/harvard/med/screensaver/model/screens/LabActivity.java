// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;


/**
 * An activity that occurs in the screening lab.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_lab_activity_to_activity")
@org.hibernate.annotations.Proxy
public abstract class LabActivity extends Activity
{

  // private static fields

  private static final Logger log = Logger.getLogger(LabActivity.class);
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<LabActivity> Screen = RelationshipPath.from(LabActivity.class).to("screen", Cardinality.TO_ONE);

  // private instance fields

  private Screen _screen;
  private Set<EquipmentUsed> _equipmentUsed = new HashSet<EquipmentUsed>();
  private Volume _volumeTransferredPerWellFromLibraryPlates;

  private MolarConcentration _molarConcentration;


  // public instance methods

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_lab_activity_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="labActivities")
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the volume transferred per well, from library plates
   * 
   * @return the volume transferred per well, from library plates
   */
  @Column(precision=ScreensaverConstants.VOLUME_PRECISION, scale=ScreensaverConstants.VOLUME_SCALE)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.db.usertypes.VolumeType") 
  public Volume getVolumeTransferredPerWellFromLibraryPlates()
  {
    return _volumeTransferredPerWellFromLibraryPlates;
  }
  
  /**
   * Set the volume transferred per well
   * @param volumeTransferredPerWell the new volume transferred per well
   */
  public void setVolumeTransferredPerWellFromLibraryPlates(Volume volumeTransferredPerWell)
  {
    _volumeTransferredPerWellFromLibraryPlates = volumeTransferredPerWell;
  }

  @Transient
  public String getMolarConcentrationValue()
  {
    return _molarConcentration == null ? null : _molarConcentration.getDisplayValue().toString();
  }  

  /**
   * Get the concentration
   * @return the concentration
   */
  @Column(precision=ScreensaverConstants.MOLAR_CONCENTRATION_PRECISION, scale=ScreensaverConstants.MOLAR_CONCENTRATION_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  public MolarConcentration getMolarConcentration()
  {
    return _molarConcentration;
  }

  /**
   * Set Concentration
   * @param value
   */
  public void setMolarConcentration(MolarConcentration value)
  {
    _molarConcentration = value;
  }
  
  @Transient
  public MolarUnit getMolarConcentrationUnits()
  {
    return _molarConcentration == null ? MolarUnit.DEFAULT : _molarConcentration.getUnits();
  }

  /**
   * Get the equipment used.
   * @return the equipment used
   */
  @OneToMany(
    mappedBy="labActivity",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  @edu.harvard.med.screensaver.model.annotations.ToMany(
    singularPropertyName="equipmentUsed"
      //,
    //inverseProperty="labActivity"
  )
  public Set<EquipmentUsed> getEquipmentUsed()
  {
    return _equipmentUsed;
  }

  /**
   * Create and return a new equipment used for the lab activity.
   * @param equipment the equipment
   * @param protocol the protocol
   * @param description the description
   * @return a new equipment used for the lab activity
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
   * Construct an initialized <code>LabActivity</code>.
   * @param screen the screen
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the lab activity took place
   */
  protected LabActivity(
    Screen screen,
    AdministratorUser recordedBy,
    ScreensaverUser performedBy,
    LocalDate dateOfActivity)
  {
    super(recordedBy, performedBy, dateOfActivity);
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }

  /**
   * Construct an uninitialized <code>LabActivity</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected LabActivity() {}


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
