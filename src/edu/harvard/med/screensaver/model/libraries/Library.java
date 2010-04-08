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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.util.DevelopmentException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

/**
 * A library represents a set of reagents and their layout into wells across
 * multiple stock plates, and also includes the layout of control and other
 * special purpose wells. The reagents comprising a given library are intended
 * to be a cohesive set that arbitrarily groups the reagents by vendor, species
 * targeted, cellular function, chemical similarity, or any combination thereof.
 * Reagents may belong to multiple libraries.
 * <ul>
 * <li>Screensaver supports libraries for either RNAi and Small Molecule
 * screens. RNAi library wells contain silencing reagents and small
 * molecule library wells contain compounds.</li>
 * <li>Only 384 well plate configurations are currently supported.</li>
 * <li>A library must be defined for a set of plates that have a sequential
 * plate numbers.</li>
 * </ul>
 * <p>
 * Screensaver allows a Library to be defined independently of its wells and its
 * well reagents. In other words, the data model permits any of the following
 * states for a library:
 * <ul>
 * <li>Library is defined with related Wells and Well reagents. This is the
 * usual state of a library in Screensaver.
 * <li>Library is defined with related Wells, but without Well reagents. This
 * allows for a library's contents to be unloaded and reloaded without deleting
 * the Library definition itself, which is useful if updated/corrected well
 * contents data becomes available.
 * <li>Library is defined, but without related Wells, and thus without Well
 * reagents. This state is supported for legacy reasons only, but one must be
 * aware that a given member Well may not be defined. See
 * {@link LibrariesDAO#loadOrCreateWellsForLibrary(Library)}.
 * </ul>
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class Library extends AuditedAbstractEntity<Integer>
{

  // static data

  private static final Logger log = Logger.getLogger(Library.class);
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<Library> contentsVersions = new RelationshipPath<Library>(Library.class, "contentsVersions");
  public static final RelationshipPath<Library> latestReleasedContentsVersion = new RelationshipPath<Library>(Library.class, "latestReleasedContentsVersion");
  public static final RelationshipPath<Library> wells = new RelationshipPath<Library>(Library.class, "wells");
  public static final RelationshipPath<Library> copies = new RelationshipPath<Library>(Library.class, "copies");
  public static final PropertyPath<Library> startPlate = new PropertyPath<Library>(Library.class, "startPlate");
  public static final PropertyPath<Library> endPlate = new PropertyPath<Library>(Library.class, "endPlate");


  // private instance data

  private Integer _version;
  private Set<Well> _wells = new HashSet<Well>();
  private Set<Copy> _copies = new HashSet<Copy>();
  private String _libraryName;
  private String _shortName;
  private String _description;
  private String _vendor;
  private ScreenType _screenType;
  private LibraryType _libraryType;
  private boolean _isPool;
  private Integer _startPlate;
  private Integer _endPlate;
  private LibraryScreeningStatus _screeningStatus;
  private LocalDate _dateReceived;
  private LocalDate _dateScreenable;
  private String _comments;
  private PlateSize _plateSize;
  private ScreeningRoomUser _owner;
  private Integer _experimentalWellCount = new Integer(0);	
  private SortedSet<LibraryContentsVersion> _contentsVersions = Sets.newTreeSet();
  private LibraryContentsVersion _latestReleasedContentsVersion;	
  


  // public constructor

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Library() {}

  /**
   * 
   * Construct a new, unitialized Library
   * @motivation for new Library creation via user interface, where even required
   *             fields are allowed to be uninitialized, initially
   * @param createdBy
   */
  public Library(AdministratorUser createdBy)
  {
    super(createdBy);
  }

  /**
   * Construct an initialized <code>Library</code> object.
   *
   * @param libraryName the library name
   * @param shortName the short name
   * @param screenType the screen type (RNAi or Small Molecule)
   * @param libraryType the library type
   * @param startPlate the start plate
   * @param endPlate the end plate
   */
  public Library(String libraryName,
                 String shortName,
                 ScreenType screenType,
                 LibraryType libraryType,
                 Integer startPlate,
                 Integer endPlate,
                 PlateSize plateSize)
  {
    super(null); /* TODO */
    _libraryName = libraryName;
    _shortName = shortName;
    _screenType = screenType;
    _libraryType = libraryType;
    _startPlate = startPlate;
    _endPlate = endPlate;
    _plateSize = plateSize;
    setScreeningStatus(LibraryScreeningStatus.ALLOWED);
  }

  public Library(String libraryName,
                 String shortName,
                 ScreenType screenType,
                 LibraryType libraryType,
                 Integer startPlate,
                 Integer endPlate)
  {
    this(libraryName, shortName, screenType, libraryType, startPlate, endPlate, ScreensaverConstants.DEFAULT_PLATE_SIZE);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the id for the screening library.
   *
   * @return the id for the screening library
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(name = "library_id_seq", strategy = "sequence", parameters = { @Parameter(name = "sequence", value = "library_id_seq") })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_id_seq")
  public Integer getLibraryId()
  {
    return getEntityId();
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="libraryUpdateActivity", 
             joinColumns=@JoinColumn(name="libraryId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  @Sort(type=SortType.NATURAL)
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  /**
   * Get the set of wells.
   *
   * @return the wells
   */
  @OneToMany(targetEntity = Well.class, mappedBy = "library", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @OrderBy("wellId")
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
  public Set<Well> getWells()
  {
    return _wells;
  }

  /**
   * Get the number of wells.
   *
   * @return the number of wells
   * @motivation {@link #getWells} forces loading of all wells, just to get the
   *             size; Hibernate can optimize a collection size request, if we
   *             get directly from an underyling extra-lazy persistent
   *             collection.
   */
  @Transient
  public int getNumWells()
  {
    return _wells.size();
  }

  /**
   * Create and return a new well for the library.
   *
   * @param wellKey the well key for the new well
   * @param wellType the well type for the new well
   * @return the new well
   */
  public Well createWell(WellKey wellKey, LibraryWellType wellType)
  {
    Well well = new Well(this, wellKey, wellType);
    if (!_wells.add(well)) {
      throw new DuplicateEntityException(this, well);
    }
    return well;
  }

  /**
   * Get the copies.
   *
   * @return the copies
   */
  @OneToMany(mappedBy = "library", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @OrderBy("copyId")
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE })
  public Set<Copy> getCopies()
  {
    return _copies;
  }

  /**
   * Get the number of copies.
   *
   * @return the number of copies
   * @motivation {@link #getCopies()} forces loading of all copies, just to get
   *             the size; Hibernate can optimize a collection size request, if
   *             we get directly from an underyling extra-lazy persistent
   *             collection.
   */
  @Transient
  public int getNumCopies()
  {
    return _copies.size();
  }

  /**
   * Get the copy with the given copy name
   *
   * @param copyName the copy name of the copy to get
   * @return the copy with the given copy name
   */
  @Transient
  public Copy getCopy(final String copyName)
  {
    return (Copy) CollectionUtils.find(_copies, new Predicate() {
      public boolean evaluate(Object e)
      {
        return ((Copy) e).getName().equals(copyName);
      };
    });
  }

  /**
   * Create a new copy for the library.
   *
   * @param usageType the copy usage type
   * @param name the copy name
   */
  public Copy createCopy(CopyUsageType usageType, String name)
  {
    Copy copy = new Copy(this, usageType, name);
    if (!_copies.add(copy)) {
      throw new DuplicateEntityException(this, copy);
    }
    return copy;
  }

  /**
   * Get the library name.
   *
   * @return the library name
   */
  @Column(unique = true, nullable = false)
  @org.hibernate.annotations.Type(type = "text")
  public String getLibraryName()
  {
    return _libraryName;
  }

  /**
   * Set the library name.
   *
   * @param libraryName the new library name
   */
  public void setLibraryName(String libraryName)
  {
    _libraryName = libraryName;
  }

  /**
   * Get the short name.
   *
   * @return the short name
   */
  @Column(unique = true, nullable = false)
  @org.hibernate.annotations.Type(type = "text")
  public String getShortName()
  {
    return _shortName;
  }

  /**
   * Set the short name.
   *
   * @param shortName the new short name
   */
  public void setShortName(String shortName)
  {
    _shortName = shortName;
  }

  /**
   * Get the description.
   *
   * @return the description
   */
  @org.hibernate.annotations.Type(type = "text")
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set the description.
   *
   * @param description the new description
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * Get the vendor.
   *
   * @return the vendor
   */
  @org.hibernate.annotations.Type(type = "text")
  public String getVendor()
  {
    return _vendor;
  }

  /**
   * Set the vendor.
   *
   * @param vendor the new vendor
   */
  public void setVendor(String vendor)
  {
    _vendor = vendor;
  }

  /**
   * Get the screen type.
   *
   * @return the screen type
   */
  @Column(nullable = false)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.screens.ScreenType$UserType")
  public ScreenType getScreenType()
  {
    return _screenType;
  }

  /**
   * Set the screen type.
   *
   * @param screenType the new screen type
   */
  public void setScreenType(ScreenType screenType)
  {
    _screenType = screenType;
  }

  /**
   * Get the library type.
   *
   * @return the library type
   */
  @Column(nullable = false)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.libraries.LibraryType$UserType")
  public LibraryType getLibraryType()
  {
    return _libraryType;
  }

  /**
   * Set the library type.
   *
   * @param libraryType the new library type
   */
  public void setLibraryType(LibraryType libraryType)
  {
    _libraryType = libraryType;
  }

  /**
   * Determines whether this library's well contains pools of reagents. Intended
   * for use with RNAi libraries (in particular, Dharmacon siGENOME libraries),
   * but can be commandeered for any applicable library.
   * 
   * @return true if this library's wells contains pools of reagents, otherwise
   *         may return false or null.
   */
  @Column(name="isPool", nullable=false)
  public boolean isPool()
  {
    return _isPool;
  }

  public void setPool(boolean isPool)
  {
    _isPool = isPool;
  }

  @Transient
  public Class<? extends Reagent> getReagentType()
  {
    if (_screenType == ScreenType.SMALL_MOLECULE) {
      if (_libraryType == LibraryType.NATURAL_PRODUCTS) {
        return NaturalProductReagent.class;
      }
      return SmallMoleculeReagent.class;
    }
    else if (_screenType == ScreenType.RNAI) { 
      return SilencingReagent.class;
    }
    throw new DevelopmentException("unhandled screen/library type");
  }

  /**
   * Get the start plate.
   *
   * @return the start plate
   */
  @Column(unique = true, nullable = false)
  public Integer getStartPlate()
  {
    return _startPlate;
  }

  /**
   * Set the start plate.
   *
   * @param startPlate the new start plate
   */
  public void setStartPlate(Integer startPlate)
  {
    _startPlate = startPlate;
  }

  /**
   * Get the end plate.
   *
   * @return the end plate
   */
  @Column(unique = true, nullable = false)
  public Integer getEndPlate()
  {
    return _endPlate;
  }

  /**
   * Set the end plate.
   *
   * @param endPlate the new end plate
   */
  public void setEndPlate(Integer endPlate)
  {
    _endPlate = endPlate;
  }

  /**
   * Return true iff this library contains the specified plate.
   *
   * @param plateNumber
   * @return true iff this library contains the specified plate
   */
  public boolean containsPlate(Integer plateNumber)
  {
    return plateNumber != null && plateNumber >= getStartPlate() &&
           plateNumber <= getEndPlate();
  }

  /**
   * Get the screening status of this library, indicating whether this library
   * is available for screening.
   *
   * @return the screening status
   */
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus$UserType")
  @Column(nullable=false)
  public LibraryScreeningStatus getScreeningStatus()
  {
    return _screeningStatus;
  }

  /**
   * Set the screening status
   *
   * @param screeningStatus the new screening status
   */
  public void setScreeningStatus(LibraryScreeningStatus screeningStatus)
  {
    _screeningStatus = screeningStatus;
  }

  /**
   * Get the date received.
   *
   * @return the date received
   */
  @Type(type = "edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateReceived()
  {
    return _dateReceived;
  }

  /**
   * Set the date received.
   *
   * @param dateReceived the new date received
   */
  public void setDateReceived(LocalDate dateReceived)
  {
    _dateReceived = dateReceived;
  }

  /**
   * Get the date screenable.
   *
   * @return the date screenable
   */
  @Type(type = "edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateScreenable()
  {
    return _dateScreenable;
  }

  /**
   * Set the date screenable.
   *
   * @param dateScreenable the new date screenable
   */
  public void setDateScreenable(LocalDate dateScreenable)
  {
    _dateScreenable = dateScreenable;
  }

  @org.hibernate.annotations.Type(type = "text")
  public String getComments()
  {
    return _comments;
  }

  public void setComments(String comments)
  {
    _comments = comments;
  }


  // private instance methods

  /**
   * Set the id for the screening library.
   *
   * @param libraryId the new id for the screening library
   * @motivation for hibernate
   */
  private void setLibraryId(Integer libraryId)
  {
    setEntityId(libraryId);
  }

  /**
   * Get the version for the screening library.
   *
   * @return the version for the screening library
   * @motivation for hibernate
   */
  @Version
  @Column(nullable = false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the screening library.
   *
   * @param version the new version for the screening library
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of wells.
   *
   * @param wells the new set of wells
   * @motivation for hibernate
   */
  private void setWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the set of copies.
   *
   * @param copies the new set of copies
   * @motivation for hibernate
   */
  private void setCopies(Set<Copy> copies)
  {
    _copies = copies;
  }

  @Column(nullable=false/*, updatable=false*/)
  //@org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.libraries.PlateSize$UserType")
  public PlateSize getPlateSize()
  {
    return _plateSize;
  }

  /**
   * Note: if plateSize is changed, it is the responsibility of the caller to
   * also add/remove wells, as necessary, to match the new plate size.
   *
   * @param plateSize the new PlateSize
   */
  public void setPlateSize(PlateSize plateSize)
  {
//    if (!isHibernateCaller() && getEntityId() != null && plateSize != _plateSize) {
//      throw new DataModelViolationException("cannot change plate size after library is created");
//    }
    _plateSize = plateSize;
  }

  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Integer getExperimentalWellCount()
  {
    return _experimentalWellCount;
  }

  private void setExperimentalWellCount(Integer experimentalWellCount)
  {
    this._experimentalWellCount = experimentalWellCount;
  }

  void incExperimentalWellCount()
  {
    _experimentalWellCount = _experimentalWellCount + 1;
  }

  void decExperimentalWellCount()
  {
    assert _experimentalWellCount > 0;
    _experimentalWellCount = Math.max(0, _experimentalWellCount - 1);
  }

  @OneToMany(targetEntity = LibraryContentsVersion.class, 
             mappedBy = "library", 
             cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, 
             fetch = FetchType.LAZY)
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  public SortedSet<LibraryContentsVersion> getContentsVersions()
  {
    return _contentsVersions;
  }

  private void setContentsVersions(SortedSet<LibraryContentsVersion> contentsVersions)
  {
    _contentsVersions = contentsVersions;
  }

  /**
   * Get the most recently released {@link LibraryContentsVersion}, which
   * represents the latest contents version that is available for viewing by
   * {@link ScreeningRoomUser}s. Note that there may exist a newer
   * contents versions that has not yet been released for viewing, and so is
   * only available to {@link AdministratorUser}s.
   */
  @OneToOne(cascade={}, fetch=FetchType.LAZY)
  @JoinColumn(name="latest_released_contents_version_id")
  @ToOne(hasNonconventionalSetterMethod=true) /* the released contents versions must be one of the library's contents versions */
  public LibraryContentsVersion getLatestReleasedContentsVersion()
  {
    return _latestReleasedContentsVersion;
  }

  /*package*/ void setLatestReleasedContentsVersion(LibraryContentsVersion latestReleasedContentsVersion)
  {
    _latestReleasedContentsVersion = latestReleasedContentsVersion;
  }

  /**
   * Get the most recently created {@link LibraryContentsVersion}, which may be
   * newer than the latest <i>released</i> contents version, and so may only be
   * available to {@link AdministratorUser}s.
   */
  @Transient
  public LibraryContentsVersion getLatestContentsVersion()
  {
    if (_contentsVersions.isEmpty()) { 
      return null; 
  }
    return _contentsVersions.last();
  }

  /**
   * Create a new {@link LibraryContentsVersion}. This contents version will not
   * be available for viewing by {@link ScreeningRoomUser}s until its has been
   * released by calling
   * {@link #setLatestReleasedContentsVersion(LibraryContentsVersion)}.
   *
   * @param loadingAdminActivity
   * @return the new {@link LibraryContentsVersion}
   */
  public LibraryContentsVersion createContentsVersion(AdministrativeActivity loadingAdminActivity)
  {
    LibraryContentsVersion libraryContentsVersion = 
      new LibraryContentsVersion(this, 
                                 _contentsVersions.isEmpty() ? LibraryContentsVersion.FIRST_VERSION_NUMBER : _contentsVersions.last().getVersionNumber() + 1,
                                 loadingAdminActivity);
    _contentsVersions.add(libraryContentsVersion);
    return libraryContentsVersion;
  }


  //Set the FetchType to EAGER otherwise when browsing libraries: org.hibernate.LazyInitializationException: could not initialize proxy - no Session
  @ManyToOne(fetch=FetchType.EAGER, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="ownerScreenerId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_library_to_owner")
//  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE
  })
 
  public ScreeningRoomUser getOwner()
  {
    return _owner;
  }

  public void setOwner(ScreeningRoomUser owner)
  {
    _owner = owner;
  }
}
