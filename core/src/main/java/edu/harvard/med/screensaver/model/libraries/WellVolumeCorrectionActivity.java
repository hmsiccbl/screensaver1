// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

/**
 * Tracks the activity of correcting the "known" volume of a set of wells on a
 * library copy plate, thereby reconciling differences between actual (physical)
 * volumes and the calculated volume, as determined by the cumulative
 * {@link WellVolumeAdjustment WellVolumeAdjustments}. Any difference, of
 * course, will always be due to inaccuracies or errors in lab operations,
 * rather than Screensaver's fault! :)
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_well_volume_correction_activity_to_activity")
@org.hibernate.annotations.Proxy
public class WellVolumeCorrectionActivity extends AdministrativeActivity
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(WellVolumeCorrectionActivity.class);

  private static final String ACTIVITY_TYPE_NAME =  "Well Volume Correction";


  // private instance datum

  private Set<WellVolumeAdjustment> _wellVolumeAdjustments = new HashSet<WellVolumeAdjustment>();


  // public constructor

  /**
   * Construct an initialized <code>WellVolumeCorrectionActivity</code>.
   * @param performedBy the user that performed the activity
   * @param datePerformed the date the activity took place
   */
  public WellVolumeCorrectionActivity(AdministratorUser performedBy, LocalDate datePerformed)
  {
    super(performedBy, datePerformed, AdministrativeActivityType.WELL_VOLUME_CORRECTION);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getActivityTypeName()
  {
    return ACTIVITY_TYPE_NAME;
  }

  /**
   * Get the set of well volume adjustments.
   * @return the set of well volume adjustments
   */
  @OneToMany(cascade = { CascadeType.ALL },
             fetch = FetchType.LAZY)
  @JoinColumn(name="wellVolumeCorrectionActivityId")
  @ToMany(hasNonconventionalMutation=true)
  @org.hibernate.annotations.ForeignKey(name="fk_well_volume_adjustment_to_well_volume_correction_activity")
  public Set<WellVolumeAdjustment> getWellVolumeAdjustments()
  {
    return _wellVolumeAdjustments;
  }

  /**
   * Create and return a new well volume adjustment for the well volume correction activity.
   * @param copy the copy
   * @param well the well
   * @param volume the volume
   * @return true the new well volume adjustment
   */
  public WellVolumeAdjustment createWellVolumeAdjustment(
      Copy copy,
      Well well,
      Volume volume)
  {
    WellVolumeAdjustment wellVolumeAdjustment = new WellVolumeAdjustment(
      copy,
      well,
      volume,
      this);
    _wellVolumeAdjustments.add(wellVolumeAdjustment);
    return wellVolumeAdjustment;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>WellVolumeCorrectionActivity</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected WellVolumeCorrectionActivity() {}


  // private constructor and instance method

  /**
   * Set the set of well volume adjustments.
   * @param wellVolumeAdjustments the new set of well volume adjustments
   * @motivation for hibernate
   */
  private void setWellVolumeAdjustments(Set<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    _wellVolumeAdjustments = wellVolumeAdjustments;
  }
}
