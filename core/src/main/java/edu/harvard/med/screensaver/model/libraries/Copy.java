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
import java.util.Map;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.Derived;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;


/**
 * A set of plates representing a copy of a {@link Library}'s contents. A facility creates and uses
 * multiple copies of a library, rather than working with a single master copy, in
 * order to reduce reagent freeze/thaw cycles, minimize the impact of loss due to a
 * physical loss, etc.  Note that in the Screensaver domain model, a library Copy represents
 * the physical instances of library plates that exist in reality.  A copy is a physical manifestation 
 * of a {@link Library}, which only specifies the layout of reagents across a 
 * set plates.  Therefore, even if a facility decided to work with a single, master set of library 
 * plates, in Screensaver one would still have to define a single Copy for this master set of plates. 
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "libraryId", "name" }) })
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=Library.class)
public class Copy extends AuditedAbstractEntity<Integer> implements Comparable<Copy>
{
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<Copy> plates = RelationshipPath.from(Copy.class).to("plates");
  public static final RelationshipPath<Copy> library = RelationshipPath.from(Copy.class).to("library", Cardinality.TO_ONE);

  public static final Function<Copy,String> ToName = new Function<Copy,String>() { public String apply(Copy c) { return c.getName(); } };
  public static final Function<Copy,Library> ToLibrary = new Function<Copy,Library>() { public Library apply(Copy c) { return c.getLibrary(); } };


  private Integer _version;
  private Library _library;
  private String _name;
  private CopyUsageType _usageType;
  private String _comments;
  private LocalDate _datePlated;
  private Map<Integer,Plate> _plates = Maps.newHashMap();
  private PlateLocation _primaryPlateLocation;
  private PlateStatus _primaryPlateStatus;
  private ScreeningStatistics _screeningStatistics;
  private VolumeStatistics _volumeStatistics;
  private Integer _platesAvailable;
  private Integer _plateLocationsCount;
  private ConcentrationStatistics _concentrationStatistics;

  private BigDecimal _wellConcentrationDilutionFactor;
  
  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public int compareTo(Copy other) 
  { 
    int result = getLibrary().getLibraryName().compareTo(other.getLibrary().getLibraryName());
    if (result == 0) {
      result = getName().compareTo(other.getName());
    }
    return result;
  }
  
  /**
   * Get the id for the copy.
   * @return the id for the copy
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(name = "copy_id_seq",
                                              strategy = "sequence",
                                              parameters = { @Parameter(name = "sequence", value = "copy_id_seq") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "copy_id_seq")
  public Integer getCopyId()
  {
    return getEntityId();
  }

  private void setCopyId(Integer copyId)
  {
    setEntityId(copyId);
  }

  /**
   * Get the library.
   * @return the library
   */
  @ManyToOne
  @JoinColumn(name="libraryId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_copy_to_library")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="copies")
  public Library getLibrary()
  {
    return _library;
  }


  /**
   * @motivation for hibernate
   */
  private void setLibrary(Library library)
  {
    _library = library;
  }

  @OneToMany(
    mappedBy="copy",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @MapKey(name="plateNumber")
  @ToMany(hasNonconventionalMutation=true /*Maps not yet supported by automated model testing framework*/)
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE, 
                                            org.hibernate.annotations.CascadeType.DELETE})
  public Map<Integer,Plate> getPlates()
  {
    return _plates;
  }

  private void setPlates(Map<Integer,Plate> plates)
  {
    _plates = plates;
  }

  private Plate createPlate(Integer plateNumber)
  {
    if (getLibrary().getStartPlate() > plateNumber || getLibrary().getEndPlate() < plateNumber) {
      throw new DataModelViolationException("plate number " + plateNumber + 
                                            " is outside of library plate range (" + 
                                            getLibrary().getStartPlate() + ".." + getLibrary().getEndPlate() + ")");
    }
    Plate plate = new Plate(this, plateNumber);
    if (_plates.containsKey(plateNumber)) {
      throw new DuplicateEntityException(this, plate);
    }
    _plates.put(plateNumber, plate);
    return plate;
  }

  public Plate findPlate(int plateNumber)
  {
    return _plates.get(plateNumber);
  }

  @Column(nullable = false)
  @org.hibernate.annotations.Type(type="text")
  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }

  @Column(nullable = false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.CopyUsageType$UserType"
  )
  public CopyUsageType getUsageType()
  {
    return _usageType;
  }

  public void setUsageType(CopyUsageType copyUsageType)
  {
    _usageType = copyUsageType;
  }

  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * @motivation intended for use by {@link Library#createCopy} only.
   */
  Copy(AdministratorUser createdBy, Library library, CopyUsageType usageType, String name)
  {
    this(createdBy, library);
    _library = library;
    _name = name;
    _usageType = usageType;
  }

  /**
   * @motivation UI DTO, for creating a new Copy
   */
  public Copy(AdministratorUser createdBy, Library library)
  {
    super(createdBy);
    _library = library;
    for (int p = library.getStartPlate(); p <= library.getEndPlate(); ++p) {
      createPlate(p);
    }
    _primaryPlateStatus = PlateStatus.NOT_SPECIFIED;
//    _concentrationStatistics = new ConcentrationStatistics();
  }

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Copy() {}

  /**
   * Get the version for the copy.
   * @return the version for the copy
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
  
  @Transient
  public ScreeningStatistics getScreeningStatistics()
  {
    if (_screeningStatistics == null) {
      _screeningStatistics = new ScreeningStatistics();
    }
    return _screeningStatistics;
  }

  public void setScreeningStatistics(ScreeningStatistics screeningStatistics)
  {
    _screeningStatistics = screeningStatistics;
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "copyUpdateActivity",
             joinColumns = @JoinColumn(name = "copyId", nullable = false, updatable = false),
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

  @Column
  @Type(type = "edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  @Derived
  public LocalDate getDatePlated()
  {
    return _datePlated;
  }

  public void setDatePlated(LocalDate datePlated)
  {
    _datePlated = datePlated;
  }

  @ManyToOne(cascade = { CascadeType.MERGE })
  @JoinColumn(name = "primaryPlateLocationId")
  @org.hibernate.annotations.ForeignKey(name = "fk_copy_to_primary_plate_location")
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional = true)
  @Derived
  public PlateLocation getPrimaryPlateLocation()
  {
    return _primaryPlateLocation;
  }

  public void setPrimaryPlateLocation(PlateLocation primaryPlateLocation)
  {
    _primaryPlateLocation = primaryPlateLocation;
  }

  @Column(nullable = false)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.libraries.PlateStatus$UserType")
  @Derived
  public PlateStatus getPrimaryPlateStatus()
  {
    return _primaryPlateStatus;
  }

  public void setPrimaryPlateStatus(PlateStatus primaryPlateStatus)
  {
    _primaryPlateStatus = primaryPlateStatus;
  }

  @Transient
  public VolumeStatistics getVolumeStatistics()
  {
    return _volumeStatistics;
  }

  public void setVolumeStatistics(VolumeStatistics volumeStatistics)
  {
    _volumeStatistics = volumeStatistics;
  }

  @Column(nullable = true)
  public void setPlatesAvailable(Integer integer)
  {
    _platesAvailable = integer;
  }

  public Integer getPlatesAvailable()
  {
    return _platesAvailable;
  }

  @Column(nullable = true)
  public void setPlateLocationsCount(Integer size)
  {
    _plateLocationsCount = size;
  }

  public Integer getPlateLocationsCount()
  {
    return _plateLocationsCount;
  }

  public void setConcentrationStatistics(ConcentrationStatistics _concentrationStatistics)
  {
    this._concentrationStatistics = _concentrationStatistics;
  }

  @Column
  public ConcentrationStatistics getConcentrationStatistics()
  {
      return _concentrationStatistics;
  }
      
  @Transient
  public ConcentrationStatistics getNullSafeConcentrationStatistics()
  {
    ConcentrationStatistics concentrationStatistics = getConcentrationStatistics();
    if (concentrationStatistics == null) {
      return ConcentrationStatistics.NULL;
    }
    return concentrationStatistics;
  }

  @Transient
  public MolarConcentration getMinMolarConcentration()
  {
    return getNullSafeConcentrationStatistics().getMinMolarConcentration();
  }

  @Transient
  public MolarConcentration getMaxMolarConcentration()
  {
    return getNullSafeConcentrationStatistics().getMaxMolarConcentration();
  }

  @Transient
  public BigDecimal getMinMgMlConcentration()
  {
    return getNullSafeConcentrationStatistics().getMinMgMlConcentration();
  }

  @Transient
  public BigDecimal getMaxMgMlConcentration()
  {
    return getNullSafeConcentrationStatistics().getMaxMgMlConcentration();
  }
  
  @Transient
  public BigDecimal getPrimaryWellMgMlConcentration()
  {
    return getNullSafeConcentrationStatistics().getPrimaryWellMgMlConcentration();
  }

  @Transient
  public MolarConcentration getPrimaryWellMolarConcentration()
  {
    return getNullSafeConcentrationStatistics().getPrimaryWellMolarConcentration();
  }

  public void setWellConcentrationDilutionFactor(BigDecimal _plateDilutionFactor)
  {
    this._wellConcentrationDilutionFactor = _plateDilutionFactor;
  }

  @Column(precision = ScreensaverConstants.PLATE_DILUTION_FACTOR_PRECISION, scale = ScreensaverConstants.PLATE_DILUTION_FACTOR_SCALE )
  public BigDecimal getWellConcentrationDilutionFactor()
  {
    if(_wellConcentrationDilutionFactor == null ) return BigDecimal.ONE; // Todo: this should not happen
    return _wellConcentrationDilutionFactor;
  }  
    
}
