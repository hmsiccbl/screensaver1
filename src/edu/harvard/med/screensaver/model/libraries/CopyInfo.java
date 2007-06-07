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
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a copy info.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class CopyInfo extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(CopyInfo.class);
  private static final long serialVersionUID = 0L;

  /**
   * The number of decimal places used when recording volume values.
   */
  public static int VOLUME_SCALE = 2;

  
  // instance fields

  private Integer _copyInfoId;
  private Integer _version;
  private Copy _copy;
  private Set<CopyAction> _copyActions = new HashSet<CopyAction>();
  private Integer _plateNumber;
  private String _location;
  private PlateType _plateType;
  /**
   * The default volume (microliters) for a well on this copy plate.
   */
  private BigDecimal _microliterWellVolume;
  /**
   * Sparse map of non-standard well volumes. If well is not represented in the
   * map, the well's volume is CopyInfo._microliterWellVolume.
   */
  // TODO: map should really be Map<WellName,BigDecimal>, but this is transient code so not going to fix...
  private Map<WellKey,BigDecimal> _microliterWellVolumes = new HashMap<WellKey,BigDecimal>();
  private String _comments;
  private Date _datePlated;
  private Date _dateRetired;


  // public constructor

  /**
   * Constructs an initialized <code>CopyInfo</code> object.
   *
   * @param copy the copy
   * @param plateNumber the plate number
   * @param location the location
   * @param plateType the plate type
   * @param volume the volume
   */
  public CopyInfo(
    Copy copy,
    Integer plateNumber,
    String location,
    PlateType plateType,
    BigDecimal volume)
  {
    _copy = copy;
    _plateNumber = plateNumber;
    _location = location;
    _plateType = plateType;
    setMicroliterWellVolume(volume);
    _copy.getHbnCopyInfos().add(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getCopyInfoId();
  }

  /**
   * Get the id for the copy info.
   *
   * @return the id for the copy info
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="copy_info_id_seq"
   */
  public Integer getCopyInfoId()
  {
    return _copyInfoId;
  }

  /**
   * Get the copy.
   *
   * @return the copy
   */
  public Copy getCopy()
  {
    return _copy;
  }

  /**
   * Set the copy.
   *
   * @param copy the new copy
   */
  public void setCopy(Copy copy)
  {
    _copy.getHbnCopyInfos().remove(this);
    _copy = copy;
    copy.getHbnCopyInfos().add(this);
  }

  /**
   * Get an unmodifiable copy of the set of copy actions.
   *
   * @return the copy actions
   */
  public Set<CopyAction> getCopyActions()
  {
    return Collections.unmodifiableSet(_copyActions);
  }

  /**
   * Add the copy action.
   *
   * @param copyAction the copy action to add
   * @return true iff the copy info did not already have the copy action
   */
  public boolean addCopyAction(CopyAction copyAction)
  {
    if (getHbnCopyActions().add(copyAction)) {
      copyAction.setHbnCopyInfo(this);
      return true;
    }
    return false;
  }

  /**
   * Get the plate number.
   *
   * @return the plate number
   */
  public Integer getPlateNumber()
  {
    return _plateNumber;
  }

  /**
   * Set the plate number.
   *
   * @param plateNumber the new plate number
   */
  public void setPlateNumber(Integer plateNumber)
  {
    _copy.getHbnCopyInfos().remove(this);
    _plateNumber = plateNumber;
    _copy.getHbnCopyInfos().add(this);
  }

  /**
   * Get the location.
   *
   * @return the location
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getLocation()
  {
    return _location;
  }

  /**
   * Set the location.
   *
   * @param location the new location
   */
  public void setLocation(String location)
  {
    _location = location;
  }

  /**
   * Get the plate type.
   *
   * @return the plate type
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.libraries.PlateType$UserType"
   *   not-null="true"
   */
  public PlateType getPlateType()
  {
    return _plateType;
  }

  /**
   * Set the plate type.
   *
   * @param plateType the new plate type
   */
  public void setPlateType(PlateType plateType)
  {
    _plateType = plateType;
  }

  /**
   * Use {@link #getMicroliterWellVolume(WellKey)}.
   */
  @DerivedEntityProperty
  public BigDecimal getDefaultMicroliterWellVolume()
  {
    return _microliterWellVolume;
  }

  /**
   * Get the initially available volume (microliters) for a well on this copy
   * plate.
   * 
   * @param wellKey
   * @return the initially available volume (microliters) the specified well
   * @motivation Some wells will differ from the plate-specified volume
   */
  @DerivedEntityProperty
  public BigDecimal getMicroliterWellVolume(WellKey wellKey)
  {
    if (_microliterWellVolumes.containsKey(wellKey)) {
      return _microliterWellVolumes.get(wellKey);
    }
    return _microliterWellVolume;
  }
  
  public void setMicroliterWellVolume(WellKey wellKey, BigDecimal microliterVolume)
  {
    _microliterWellVolumes.put(wellKey, microliterVolume.setScale(VOLUME_SCALE));
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
   * Get the date plated.
   *
   * @return the date plated
   * @hibernate.property
   */
  public Date getDatePlated()
  {
    return _datePlated;
  }

  /**
   * Set the date plated.
   *
   * @param datePlated the new date plated
   */
  public void setDatePlated(Date datePlated)
  {
    _datePlated = truncateDate(datePlated);
  }

  /**
   * Get the date retired.
   *
   * @return the date retired
   * @hibernate.property
   */
  public Date getDateRetired()
  {
    return _dateRetired;
  }

  /**
   * Set the date retired.
   *
   * @param dateRetired the new date retired
   */
  public void setDateRetired(Date dateRetired)
  {
    _dateRetired = truncateDate(dateRetired);
  }

  @DerivedEntityProperty
  public boolean isRetired()
  {
    return _dateRetired != null;
  }

  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the copy.
     *
     * @return the copy
     */
    public Copy getCopy()
    {
      return _copy;
    }
    
    /**
     * Get the plate number.
     *
     * @return the plate number
     */
    public Integer getPlateNumber()
    {
      return _plateNumber;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getCopy().equals(that.getCopy()) &&
        getPlateNumber().equals(that.getPlateNumber());
    }

    @Override
    public int hashCode()
    {
      return
        getCopy().hashCode() +
        getPlateNumber().hashCode();
    }

    @Override
    public String toString()
    {
      return getCopy() + ":" + getPlateNumber();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the copy.
   * Throw a NullPointerException when the copy is null.
   *
   * @param copy the new copy
   * @throws NullPointerException when the copy is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnCopy(Copy copy)
  {
    if (copy == null) {
      throw new NullPointerException();
    }
    _copy = copy;
  }

  /**
   * Get the copy actions.
   *
   * @return the copy actions
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="copy_info_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.libraries.CopyAction"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<CopyAction> getHbnCopyActions()
  {
    return _copyActions;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>CopyInfo</code> object.
   *
   * @motivation for hibernate
   */
  private CopyInfo() {}


  // private methods

  /**
   * Set the id for the copy info.
   *
   * @param copyInfoId the new id for the copy info
   * @motivation for hibernate
   */
  private void setCopyInfoId(Integer copyInfoId) {
    _copyInfoId = copyInfoId;
  }

  /**
   * Get the version for the copy info.
   *
   * @return the version for the copy info
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the copy info.
   *
   * @param version the new version for the copy info
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the copy.
   *
   * @return the copy
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Copy"
   *   column="copy_id"
   *   not-null="true"
   *   foreign-key="fk_copy_info_to_copy"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Copy getHbnCopy()
  {
    return _copy;
  }

  /**
   * Set the copy actions.
   *
   * @param copyActions the new copy actions
   * @motivation for hibernate
   */
  private void setHbnCopyActions(Set<CopyAction> copyActions)
  {
    _copyActions = copyActions;
  }
  
  /**
   * Get the plate number.
   *
   * @return the plate number
   * @hibernate.property
   *   column="plate_number"
   *   not-null="true"
   * @motivation for hibernate
   */
  private Integer getHbnPlateNumber()
  {
    return _plateNumber;
  }

  /**
   * Set the plate number.
   *
   * @param plateNumber the new plate number
   * @motivation for hibernate
   */
  private void setHbnPlateNumber(Integer plateNumber)
  {
    _plateNumber = plateNumber;
  }

  /**
   * Get the default volume (microliters) for wells on this copy plate.  
   * 
   * @return the volume
   * @hibernate.property type="big_decimal" not-null="true"
   */
  private BigDecimal getMicroliterWellVolume()
  {
    return _microliterWellVolume;
  }
  
  /**
   * Set the volume.
   *
   * @param volume the new volume
   */
  private void setMicroliterWellVolume(BigDecimal volume)
  {
    if (volume == null) {
      _microliterWellVolume = null;
    }
    else {
      _microliterWellVolume = volume.setScale(VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }

  /*
   * Note: Hibernate mapping is in hibernate-properties-CopyInfo.xml. Could be
   * done via XDoclet, but this is transient code, so not going to fix it.
   */
  private Map<WellKey,BigDecimal> getMicroliterWellVolumes()
  {
    return _microliterWellVolumes;
  }

  private void setMicroliterWellVolumes(Map<WellKey,BigDecimal> microliterWellVolumes)
  {
    _microliterWellVolumes = microliterWellVolumes;
  }
}
