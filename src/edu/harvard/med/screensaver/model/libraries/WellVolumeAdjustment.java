// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.math.BigDecimal;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;

import org.apache.log4j.Logger;

/**
 * A Hibernate entity bean representing a well volume adjustment. A well volume
 * adjustment normally occurs when the lab removes liquid from a library copy
 * plate in order to produce assay plates; see {@link LabCherryPick}. Rarely, a
 * well volume adjustment occurs to reconcile any differences detected between
 * the physical volume of a library copy well and the volume reported by the
 * Screensaver database; see {@link WellVolumeCorrectionActivity}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="true"
 */
public class WellVolumeAdjustment extends AbstractEntity
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(WellVolumeAdjustment.class);


  // instance data members

  private Integer _wellVolumeAdjustmentId;
  private Integer _version;
  private Copy _copy;
  private Well _well;
  private BigDecimal _microliterVolume;

  
  // public constructors and methods

  public WellVolumeAdjustment(Copy copy, Well well, BigDecimal microliterVolume)
  {
    if (microliterVolume.scale() != Well.VOLUME_SCALE) {
      throw new IllegalArgumentException("scale must be " + Well.VOLUME_SCALE);
    }
    _copy = copy;
    _well = well;
    _microliterVolume = microliterVolume;
  }
  
  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    //return visitor.visit(this);
    return null;
  }

  @Override
  public Integer getEntityId()
  {
    return getWellVolumeAdjustmentId();
  }

  /**
   * Get the id for the activity.
   *
   * @return the id for the activity
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="well_volume_adjustment_id_seq"
   */
  public Integer getWellVolumeAdjustmentId()
  {
    return _wellVolumeAdjustmentId;
  }
  
  /**
   * Note: This is a unidirectional relationship for performance reasons.
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Copy"
   *   column="copy_id"
   *   not-null="true"
   *   foreign-key="fk_well_volume_adjustment_to_copy"
   *   cascade="save-update"
   * @return
   */
  @ToOneRelationship(unidirectional=true, nullable=false)
  public Copy getCopy()
  {
    return _copy;
  }

  /**
   * Note: This is a unidirectional relationship for performance reasons.
   * cascade="none" also for performance, and concurrent modification detection
   * will still work due to cascade="save-update" setting on Copy relationship.
   * 
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.libraries.Well"
   *                        column="well_id" not-null="true"
   *                        foreign-key="fk_well_volume_adjustment_to_well"
   *                        cascade="none"
   * @return
   */
  @ToOneRelationship(unidirectional=true, nullable=false)
  public Well getWell()
  {
    return _well;
  }

  /**
   * @hibernate.property type="big_decimal"
   * @return
   */
  public BigDecimal getMicroliterVolume()
  {
    return _microliterVolume;
  }

  
  // protected methods
  
  /**
   * @motivation for Hibernate & CGLIB2
   */
  protected WellVolumeAdjustment()
  {
  }
  
  @Override
  protected Object getBusinessKey()
  {
    // TODO: this is not a proper business key, as there can exist multiple
    // WellVolumeAdjustments for the same well and copy; however Screensaver
    // currently never attempts to add WellVolumeAdjustments of the same well
    // and copy to a given Set, so this is safe for now
    return _well.hashCode() * 11 + _copy.hashCode() * 17;
  }
  

  // private methods

  /**
   * Set the id.
   *
   * @param wellVolumeAdjustmentId the id
   * @motivation for hibernate
   */
  private void setWellVolumeAdjustmentId(Integer wellVolumeAdjustmentId) {
    _wellVolumeAdjustmentId = wellVolumeAdjustmentId;
  }
  
  /**
   * Get the version.
   *
   * @return the version
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version.
   *
   * @param version the new version 
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  private void setCopy(Copy copy)
  {
    _copy = copy;
  }

  private void setWell(Well well)
  {
    _well = well;
  }

  private void setMicroliterVolume(BigDecimal microliterVolume)
  {
    _microliterVolume = microliterVolume;
  }
}

