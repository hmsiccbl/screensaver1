// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a copy info.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "copyId", "plateNumber" }) })
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=Copy.class)
public class CopyInfo extends AbstractEntity<Integer>
{

  // static fields

  private static final Logger log = Logger.getLogger(CopyInfo.class);
  private static final long serialVersionUID = 0L;


  // private instance fields

  private Integer _version;
  private Copy _copy;
  private Set<CopyAction> _copyActions = new HashSet<CopyAction>();
  private Integer _plateNumber;
  private String _location;
  private PlateType _plateType;
  /** The default initial volume for a well on this copy plate. */
  private Volume _wellVolume;
  private String _comments;
  private LocalDate _datePlated;
  private LocalDate _dateRetired;


  // public instance methods

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
    name="copy_info_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="copy_info_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="copy_info_id_seq")
  public Integer getCopyInfoId()
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
  @org.hibernate.annotations.ForeignKey(name="fk_copy_info_to_copy")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Copy getCopy()
  {
    return _copy;
  }

  /**
   * Get the set of copy actions.
   * @return the copy actions
   */
  @OneToMany(
    mappedBy="copyInfo",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("date")
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Set<CopyAction> getCopyActions()
  {
    return _copyActions;
  }

  /**
   * Create and return a new copy action for the copy info
   * @param description the description
   * @param date the date
   * @return the newly created copy action for the copy info
   */
  public CopyAction createCopyAction(String description, LocalDate date)
  {
    CopyAction copyAction = new CopyAction(this, description, date);
    // no need to check for duplicate entities here since copy actions are not SemanticID
    _copyActions.add(copyAction);
    return copyAction;
  }

  /**
   * Get the plate number.
   * @return the plate number
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false)
  public Integer getPlateNumber()
  {
    return _plateNumber;
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


  // package constructor

  /**
   * Construct an initialized <code>CopyInfo</code>. Intended only for use by {@link Copy}.
   * @param copy the copy
   * @param plateNumber the plate number
   * @param location the location
   * @param plateType the plate type
   * @param volume the volume
   * @motivation intended only for use by {@link Copy}
   */
  CopyInfo(
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


  // protected constructor

  /**
   * Construct an uninitialized <code>CopyInfo</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected CopyInfo() {}


  // private constructors and instance methods

  /**
   * Set the id for the copy info.
   * @param copyInfoId the new id for the copy info
   * @motivation for hibernate
   */
  private void setCopyInfoId(Integer copyInfoId)
  {
    setEntityId(copyInfoId);
  }

  /**
   * Get the version for the copy info.
   * @return the version for the copy info
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the copy info.
   * @param version the new version for the copy info
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
   * Set the copy actions.
   * @param copyActions the new copy actions
   * @motivation for hibernate
   */
  private void setCopyActions(Set<CopyAction> copyActions)
  {
    _copyActions = copyActions;
  }

  /**
   * Set the plate number.
   * @param plateNumber the new plate number
   * @motivation for hibernate
   */
  private void setPlateNumber(Integer plateNumber)
  {
    _plateNumber = plateNumber;
  }

  /**
   * Set the volume.
   * @param volume the new volume
   * @motivation for hibernate and package constructor
   */
  private void setWellVolume(Volume volume)
  {
    _wellVolume = volume;
  }
}
