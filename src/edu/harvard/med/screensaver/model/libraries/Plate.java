// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.math.BigDecimal;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

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
public class Plate extends AuditedAbstractEntity<Integer> implements Comparable<Plate>
{
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<Plate> copy = RelationshipPath.from(Plate.class).to("copy", Cardinality.TO_ONE);
  public static final RelationshipPath<Plate> location = RelationshipPath.from(Plate.class).to("location", Cardinality.TO_ONE);
  public static final RelationshipPath<Plate> platedActivity = RelationshipPath.from(Plate.class).to("platedActivity", Cardinality.TO_ONE);
  public static final RelationshipPath<Plate> retiredActivity = RelationshipPath.from(Plate.class).to("retiredActivity", Cardinality.TO_ONE);
  
  public static final Function<Plate,Integer> ToPlateNumber = new Function<Plate,Integer>() { public Integer apply(Plate p) { return p.getPlateNumber(); } };
  public static final Function<Plate,Copy> ToCopy = new Function<Plate,Copy>() { public Copy apply(Plate p) { return p.getCopy(); } };
  public static final Function<Plate,PlateLocation> ToLocation = new Function<Plate,PlateLocation>() {
    public PlateLocation apply(Plate p)
    {
      return p.getLocation();
    }
  };
  public static final Function<Plate,PlateStatus> ToStatus = new Function<Plate,PlateStatus>() {
    public PlateStatus apply(Plate p)
    {
      return p.getStatus();
    }
  };  
  
    

  private Integer _version;
  private Copy _copy;
  private Integer _plateNumber;
  private PlateLocation _location;
  private String _facilityId;
  private PlateType _plateType;
  /** The default initial volume for a well on this copy plate. */
  private Volume _wellVolume;
  private MolarConcentration _molarConcentration;
  private BigDecimal _mgMlConcentration;
  private PlateStatus _status;
  private AdministrativeActivity _platedActivity;
  private AdministrativeActivity _retiredActivity;
  private String _comments;
  private Set<AssayPlate> _assayPlates = ImmutableSet.of();
  private ScreeningStatistics _screeningStatistics;

  /**
   * Construct an initialized <code>Plate</code>. Intended only for use by {@link Copy}.
   * @motivation intended only for use by {@link Copy}
   */
  Plate(Copy copy,
        Integer plateNumber)
  {
    super((AdministratorUser) copy.getCreatedBy());
    _copy = copy;
    _plateNumber = plateNumber;
    _status = PlateStatus.NOT_SPECIFIED;
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

  @OneToMany(mappedBy = "plateScreened", cascade = {})
  @MapKey(name = "plateNumber")
  @ToMany(hasNonconventionalMutation = true /* we don't update this relationship in memory */)
  public Set<AssayPlate> getAssayPlates()
  {
    return _assayPlates;
  }

  private void setAssayPlates(Set<AssayPlate> assayPlates)
  {
    _assayPlates = assayPlates;
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
  @org.hibernate.annotations.Type(type="text")
  @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
  @JoinColumn(name = "plateLocationId")
  @org.hibernate.annotations.ForeignKey(name = "fk_plate_to_plate_location")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional = true)
  public PlateLocation getLocation()
  {
    return _location;
  }

  /**
   * Set the location.
   * @param location the new location
   */
  public void setLocation(PlateLocation location)
  {
    _location = location;
  }

  public String getFacilityId()
  {
    return _facilityId;
  }

  public void setFacilityId(String facilityId)
  {
    _facilityId = facilityId;
  }

  /**
   * Get the plate type.
   * @return the plate type
   */
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
  @Column(precision = ScreensaverConstants.VOLUME_PRECISION, scale = ScreensaverConstants.VOLUME_SCALE)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.db.usertypes.VolumeType") 
  public Volume getWellVolume()
  {
    return _wellVolume;
  }

  public void setWellVolume(Volume volume)
  {
    _wellVolume = volume;
  }

  public void setMolarConcentration(MolarConcentration molarConcentration)
  {
    _molarConcentration = molarConcentration;
  }

  @Column(precision = ScreensaverConstants.MOLAR_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MOLAR_CONCENTRATION_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  public MolarConcentration getMolarConcentration()
  {
    return _molarConcentration;
  }

  public void setMgMlConcentration(BigDecimal mgMlConcentration)
  {
    _mgMlConcentration = mgMlConcentration;
  }

  @Column(precision = ScreensaverConstants.MG_ML_CONCENTRATION_PRECISION, scale = ScreensaverConstants.MG_ML_CONCENTRATION_SCALE)
  public BigDecimal getMgMlConcentration()
  {
    return _mgMlConcentration;
  }

  public Plate withWellVolume(Volume volume)
  {
    setWellVolume(volume);
    return this;
  }

  @Column(nullable = false)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.libraries.PlateStatus$UserType")
  public PlateStatus getStatus()
  {
    return _status;
  }

  public void setStatus(PlateStatus status)
  {
    _status = status;
  }

  public Plate withStatus(PlateStatus status)
  {
    setStatus(status);
    return this;
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

  public Plate withLocation(PlateLocation location)
  {
    setLocation(location);
    return this;
  }

  public Plate withPlateType(PlateType plateType)
  {
    setPlateType(plateType);
    return this;
  }

  @Transient
  public ScreeningStatistics getScreeningStatistics()
  {
    return _screeningStatistics;
  }

  public void setScreeningStatistics(ScreeningStatistics screeningStatistics)
  {
    _screeningStatistics = screeningStatistics;
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "plateUpdateActivity",
             joinColumns = @JoinColumn(name = "plateId", nullable = false, updatable = false),
             inverseJoinColumns = @JoinColumn(name = "updateActivityId", nullable = false, updatable = false, unique = true))
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @Sort(type = SortType.NATURAL)
  @ToMany(singularPropertyName = "updateActivity", hasNonconventionalMutation = true /*
                                                                                      * model testing framework doesn't
                                                                                      * understand this is a containment
                                                                                      * relationship, and so requires
                                                                                      * addUpdateActivity() method
                                                                                      */)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  @OneToOne(cascade = {}, fetch = FetchType.LAZY)
  @JoinColumn(name = "plated_activity_id", nullable = true, updatable = true, unique = true)
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional = true,
                                                       hasNonconventionalSetterMethod = true)
  public AdministrativeActivity getPlatedActivity()
  {
    return _platedActivity;
  }

  /**
   * Get the activity associated with changing the plate status from {@link PlateStatus#AVAILABLE Available} to any
   * non-available status ({@link PlateStatus#RETIRED Retired} or greater.
   */
  @OneToOne(cascade = {}, fetch = FetchType.LAZY)
  @JoinColumn(name = "retired_activity_id", nullable = true, updatable = true, unique = true)
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional = true,
                                                       hasNonconventionalSetterMethod = true)
  public AdministrativeActivity getRetiredActivity()
  {
    return _retiredActivity;
  }

  public void setRetiredActivity(AdministrativeActivity retiredActivity)
  {
    _retiredActivity = retiredActivity;
  }

  public void setPlatedActivity(AdministrativeActivity platedActivity)
  {
    _platedActivity = platedActivity;
  }
}
