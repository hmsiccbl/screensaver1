// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.Activity;

/**
 * A Hibernate entity bean representing a well volume adjustment. A well volume
 * adjustment normally occurs when the lab removes liquid from a library copy
 * plate in order to produce assay plates; see {@link LabCherryPick}. Rarely, a
 * well volume adjustment occurs to reconcile any differences detected between
 * the physical volume of a library copy well and the volume reported by the
 * Screensaver database; see {@link WellVolumeCorrectionActivity}.
 * <p>
 * {@link #getLabCherryPick()} returns non-null iff {@link
 * #getWellVolumeCorrectionActivity()} returns null, and vice-versa.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(
  containingEntityClass=LabCherryPick.class,
  hasAlternateContainingEntityClass=true,
  alternateContainingEntityClass=WellVolumeCorrectionActivity.class
)
public class WellVolumeAdjustment extends AbstractEntity
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(WellVolumeAdjustment.class);


  // private instance data

  private Integer _wellVolumeAdjustmentId;
  private Integer _version;
  private Copy _copy;
  private Well _well;
  private BigDecimal _microliterVolume;
  private LabCherryPick _labCherryPick;
  private WellVolumeCorrectionActivity _wellVolumeCorrectionActivity;


  // public constructor

  /**
   * Construct an initialized <code>WellVolumeAdjustment</code>. Intended only for use by
   * {@link LabCherryPick#createWellVolumeAdjustment(WellVolumeAdjustment)}.
   * @param copy the copy
   * @param well the well
   * @param microliterVolume the volume in microliters
   * @param labCherryPick the lab cherry pick
   */
  public WellVolumeAdjustment(Copy copy, Well well, BigDecimal microliterVolume, LabCherryPick labCherryPick)
  {
    this(copy, well, microliterVolume);
    _labCherryPick = labCherryPick;
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    //return visitor.visit(this);
    return null;
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getWellVolumeAdjustmentId();
  }

  /**
   * Get the id for the activity.
   * @return the id for the activity
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="well_volume_adjustment_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="well_volume_adjustment_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="well_volume_adjustment_id_seq")
  public Integer getWellVolumeAdjustmentId()
  {
    return _wellVolumeAdjustmentId;
  }

  /**
   * Get the copy.
   * Note: This is a unidirectional relationship for performance reasons.
   * @return the copy
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="copyId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_well_volume_adjustment_to_copy")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
  public Copy getCopy()
  {
    return _copy;
  }

  /**
   * Get the well.
   * Note: This is a unidirectional relationship for performance reasons.
   * cascade="none" also for performance, and concurrent modification detection
   * will still work due to cascade="save-update" setting on Copy relationship.
   * @return the well
   */
  @ManyToOne
  @JoinColumn(name="wellId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_well_volume_adjustment_to_well")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
  public Well getWell()
  {
    return _well;
  }

  /**
   * Get the volume in microliters.
   * @return the volume in microliters
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="big_decimal")
  public BigDecimal getMicroliterVolume()
  {
    return _microliterVolume;
  }

  /**
   * Get the lab cherry pick.
   * @return the lab cherry pick
   */
  @ManyToOne
  @JoinColumn(name="labCherryPickId", nullable=true, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_well_volume_adjustment_to_lab_cherry_pick")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public LabCherryPick getLabCherryPick()
  {
    return _labCherryPick;
  }

  /**
   * Get the well volume correction activity.
   * @return the well volume correction activity
   */
  @ManyToOne
  @JoinColumn(name="wellVolumeCorrectionActivityId", nullable=true, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_well_volume_adjustment_to_well_volume_correction_activity")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public WellVolumeCorrectionActivity getWellVolumeCorrectionActivity()
  {
    return _wellVolumeCorrectionActivity;
  }

  /**
   * Get either the {@link CherryPickLiquidTransfer}, if {@link #getLabCherryPick()} is
   * non-null, or the {@link WellVolumeCorrectionActivity}, if {@link
   * #getWellVolumeCorrectionActivity()} is non-null. Note that
   * {@link #getLabCherryPick()} returns non-null iff {@link
   * #getWellVolumeCorrectionActivity()} returns null, and vice-versa.
   *
   * @return either the cherry pick liquid transfer or the well volume correction activity
   * @see WellVolumeAdjustment
   */
  @Transient
  public Activity getRelatedActivity()
  {
    if (_labCherryPick != null) {
      if (_labCherryPick.isPlated() || _labCherryPick.isFailed()) {
        return _labCherryPick.getAssayPlate().getCherryPickLiquidTransfer();
      }
    }
    else {
      if (_wellVolumeCorrectionActivity != null) {
        return _wellVolumeCorrectionActivity;
      }
    }
    assert(false) : "either _labCherryPick or _wellVolumeCorrectionActivity is non-null";
    return null;
  }


  // package constructor

  /**
   * Construct an initialized <code>WellVolumeAdjustment</code>. Intended only for use by
   * {@link WellVolumeCorrectionActivity#createWellVolumeAdjustment(Copy, Well, BigDecimal)}.
   * @param copy the copy
   * @param well the well
   * @param microliterVolume the volume in microliters
   * @param wellVolumeCorrectionActivity
   */
  WellVolumeAdjustment(Copy copy, Well well, BigDecimal microliterVolume, WellVolumeCorrectionActivity wellVolumeCorrectionActivity)
  {
    this(copy, well, microliterVolume);
    _wellVolumeCorrectionActivity = wellVolumeCorrectionActivity;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>WellVolumeAdjustment</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected WellVolumeAdjustment() {}


  // private constructor

  /**
   * Construct a partially initialized <code>WellVolumeAdjustment</code>. Intended only for
   * use by
   * {@link #WellVolumeAdjustment(Copy, Well, BigDecimal, LabCherryPick)} and
   * {@link #WellVolumeAdjustment(Copy, Well, BigDecimal, WellVolumeCorrectionActivity)}.
   * @param copy the copy
   * @param well the well
   * @param microliterVolume the volume in microliters
   */
  private WellVolumeAdjustment(Copy copy, Well well, BigDecimal microliterVolume)
  {
    if (microliterVolume.scale() != Well.VOLUME_SCALE) {
      throw new IllegalArgumentException("scale must be " + Well.VOLUME_SCALE);
    }
    _copy = copy;
    _well = well;
    _microliterVolume = microliterVolume;
  }


  // private instance methods

  /**
   * Set the well volume adjustment id.
   * @param wellVolumeAdjustmentId the new well volume adjustment id
   * @motivation for hibernate
   */
  private void setWellVolumeAdjustmentId(Integer wellVolumeAdjustmentId)
  {
    _wellVolumeAdjustmentId = wellVolumeAdjustmentId;
  }

  /**
   * Get the version.
   * @return the version
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version.
   * @param version the new version
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the copy.
   * @param copy the new copy
   * @motivation for hibernate
   */
  private void setCopy(Copy copy)
  {
    _copy = copy;
  }

  /**
   * Set the well.
   * @param well the new well
   * @motivation for hibernate
   */
  private void setWell(Well well)
  {
    _well = well;
  }

  /**
   * Set the volume in microliters.
   * @param microliterVolume the new volume in microliters
   * @motivation for hibernate
   */
  private void setMicroliterVolume(BigDecimal microliterVolume)
  {
    _microliterVolume = microliterVolume;
  }


  private void setLabCherryPick(LabCherryPick labCherryPick)
  {
    _labCherryPick = labCherryPick;
  }


  private void setWellVolumeCorrectionActivity(WellVolumeCorrectionActivity wellVolumeCorrectionActivity)
  {
    _wellVolumeCorrectionActivity = wellVolumeCorrectionActivity;
  }
}
