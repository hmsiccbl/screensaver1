// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.google.common.base.Function;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


/**
 * A plate for a particular {@link Library} {@link Copy}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "copyId", "plateNumber" }) })
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=Copy.class)
public class Plate extends AbstractEntity<Integer> implements Comparable<Plate>
{
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<Plate> copy = RelationshipPath.from(Plate.class).to("copy", Cardinality.TO_ONE);
  
  public static final Function<Plate,Integer> ToPlateNumber = new Function<Plate,Integer>() { public Integer apply(Plate p) { return p.getPlateNumber(); } };
  public static final Function<Plate,Copy> ToCopy = new Function<Plate,Copy>() { public Copy apply(Plate p) { return p.getCopy(); } };
    

  private Integer _version;
  private Copy _copy;
  private Integer _plateNumber;
  private String _location;
  private PlateType _plateType;
  /** The default initial volume for a well on this copy plate. */
  private Volume _wellVolume;
  private String _comments;
  private LocalDate _datePlated;
  private LocalDate _dateRetired;


  /**
   * Construct an initialized <code>Plate</code>. Intended only for use by {@link Copy}.
   * @param copy the copy
   * @param plateNumber the plate number
   * @param location the location
   * @param plateType the plate type
   * @param volume the volume
   * @motivation intended only for use by {@link Copy}
   */
  Plate(
    Copy copy,
    Integer plateNumber,
    String location,
    PlateType plateType,
    Volume volume)
  {
    _copy = copy;
    _plateNumber = plateNumber;
    _location = location;
    _plateType = plateType;
    setWellVolume(volume);
  }

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Plate() {}

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the id for the copy info.
   * @return the id for the copy info
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="plate_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="plate_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="plate_id_seq")
  public Integer getPlateId()
  {
    return getEntityId();
  }

  /**
   * Get the copy.
   * @return the copy
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="copyId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_plate_to_copy")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Copy getCopy()
  {
    return _copy;
  }

  private void setCopy(Copy copy)
  {
    _copy = copy;
  }

  /**
   * Get the plate number.
   * @return the plate number
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false)
  public int getPlateNumber()
  {
    return _plateNumber;
  }

  private void setPlateNumber(int plateNumber)
  {
    _plateNumber = plateNumber;
  }

  /**
   * Get the location.
   * @return the location
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getLocation()
  {
    return _location;
  }

  /**
   * Set the location.
   * @param location the new location
   */
  public void setLocation(String location)
  {
    _location = location;
  }

  /**
   * Get the plate type.
   * @return the plate type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.PlateType$UserType"
  )
  public PlateType getPlateType()
  {
    return _plateType;
  }

  /**
   * Set the plate type.
   * @param plateType the new plate type
   */
  public void setPlateType(PlateType plateType)
  {
    _plateType = plateType;
  }

  /**
   * Get the default volume for wells on this copy plate.
   * @return the volume
   */
  @Column(nullable=false, precision=ScreensaverConstants.VOLUME_PRECISION, scale=ScreensaverConstants.VOLUME_SCALE)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.db.usertypes.VolumeType") 
  public Volume getWellVolume()
  {
    return _wellVolume;
  }

  private void setWellVolume(Volume volume)
  {
    _wellVolume = volume;
  }
  /**
   * Get the comments.
   * @return the comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the date plated.
   * @return the date plated
   */
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDatePlated()
  {
    return _datePlated;
  }

  /**
   * Set the date plated.
   * @param datePlated the new date plated
   */
  public void setDatePlated(LocalDate datePlated)
  {
    _datePlated = datePlated;
  }

  /**
   * Get the date retired.
   * @return the date retired
   */
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateRetired()
  {
    return _dateRetired;
  }

  /**
   * Set the date retired.
   * @param dateRetired the new date retired
   */
  public void setDateRetired(LocalDate dateRetired)
  {
    _dateRetired = dateRetired;
  }

  /**
   * Return true iff this library has been retired.
   * @return true iff this library has been retired
   */
  @Transient
  public boolean isRetired()
  {
    return _dateRetired != null;
  }


  /**
   * @motivation for hibernate
   */
  private void setPlateId(Integer plateId)
  {
    setEntityId(plateId);
  }

  /**
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion() {
    return _version;
  }

  private void setVersion(Integer version)
  {
    _version = version;
  }
  
  @Transient
  public PlateSize getPlateSize()
  {
    return _copy.getLibrary().getPlateSize();
  }

  @Override
  public int compareTo(Plate other)
  {
    int result = this.getPlateNumber() > other.getPlateNumber() ? 1 : this.getPlateNumber() < other.getPlateNumber() ? -1 : 0; 
    if (result == 0) {
      result = this.getCopy().compareTo(other.getCopy());
    }
    return result;
  }
}
