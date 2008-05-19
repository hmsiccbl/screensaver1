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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a screening library.
 * <ul>
 * <li>Screensaver supports libraries for either RNAi and Small Molecule
 * (compound) screens. RNAi library wells contain silencing reagents and small
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
public class Library extends AbstractEntity
{

  // private static data

  private static final Logger log = Logger.getLogger(Library.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private Integer _libraryId;
  private Integer _version;
  private Set<Well> _wells = new HashSet<Well>();
  private Set<Copy> _copies = new HashSet<Copy>();
  private String _libraryName;
  private String _shortName;
  private String _description;
  private String _vendor;
  private ScreenType _screenType;
  private LibraryType _libraryType;
  private Integer _startPlate;
  private Integer _endPlate;
  private String _alias;
  private IsScreenable _isScreenable;
  private Integer _compoundCount;
  private String _screeningCopy;
  private String _compoundConcentrationInScreeningCopy;
  private String _cherryPickCopy;
  private LocalDate _dateReceived;
  private LocalDate _dateScreenable;
  private String _nonCompoundWells;
  private String _screeningRoomComments;
  private String _diversitySetPlates;
  private String _mappedFromCopy;
  private String _mappedFromPlate;
  private String _purchasedUsingFundsFrom;
  private String _platingFundsSuppliedBy;
  private String _informaticsComments;
  private String _dataFileLocation;
  private String _chemistDOS;
  private String _chemistryComments;
  private String _screeningSet;


  // public constructor

  /**
   * Construct an initialized <code>Library</code> object.
   * @param libraryName the library name
   * @param shortName the short name
   * @param screenType the screen type (RNAi or Small Molecule)
   * @param libraryType the library type
   * @param startPlate the start plate
   * @param endPlate the end plate
   */
  public Library(
    String libraryName,
    String shortName,
    ScreenType screenType,
    LibraryType libraryType,
    Integer startPlate,
    Integer endPlate)
  {
    _libraryName = libraryName;
    _shortName = shortName;
    _screenType = screenType;
    _libraryType = libraryType;
    _startPlate = startPlate;
    _endPlate = endPlate;
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getLibraryId();
  }

  /**
   * Get the id for the screening library.
   * @return the id for the screening library
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="library_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="library_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="library_id_seq")
  public Integer getLibraryId()
  {
    return _libraryId;
  }

  /**
   * Get the set of wells.
   * @return the wells
   */
  @OneToMany(
    targetEntity=Well.class,
    mappedBy="library",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("wellId")
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<Well> getWells()
  {
    return _wells;
  }

  /**
   * Get the number of wells.
   * @return the number of wells
   * @motivation {@link #getWells} forces loading of all wells, just to get the
   *             size; Hibernate can optimize a collection size request, if we
   *             get directly from an underyling extra-lazy persistent collection.
   */
  @Transient
  public int getNumWells()
  {
    return _wells.size();
  }

  /**
   * Create and return a new well for the library.
   * @param wellKey the well key for the new well
   * @param wellType the well type for the new well
   * @return the new well
   */
  public Well createWell(WellKey wellKey, WellType wellType)
  {
    Well well = new Well(this, wellKey, wellType, null);
    if (! _wells.add(well)) {
      throw new DuplicateEntityException(this, well);
    }
    return well;
  }

  /**
   * Get the copies.
   * @return the copies
   */
  @OneToMany(
    mappedBy="library",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("copyId")
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Set<Copy> getCopies()
  {
    return _copies;
  }

  /**
   * Get the number of copies.
   * @return the number of copies
   * @motivation {@link #getCopis} forces loading of all copies, just to get the
   *             size; Hibernate can optimize a collection size request, if we
   *             get directly from an underyling extra-lazy persistent collection.
   */
  @Transient
  public int getNumCopies()
  {
    return _copies.size();
  }

  /**
   * Get the copy with the given copy name
   * @param copyName the copy name of the copy to get
   * @return the copy with the given copy name
   */
  @Transient
  public Copy getCopy(final String copyName)
  {
    return (Copy) CollectionUtils.find(_copies, new Predicate()
    {
      public boolean evaluate(Object e) { return ((Copy) e).getName().equals(copyName); };
    });
  }

  /**
   * Create a new copy for the library.
   * @param usageType the copy usage type
   * @param name the copy name
   */
  public Copy createCopy(CopyUsageType usageType, String name)
  {
    Copy copy = new Copy(this, usageType, name);
    if (! _copies.add(copy)) {
      throw new DuplicateEntityException(this, copy);
    }
    return copy;
  }

  /**
   * Get the library name.
   * @return the library name
   */
  @Column(unique=true, nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getLibraryName()
  {
    return _libraryName;
  }

  /**
   * Set the library name.
   * @param libraryName the new library name
   */
  public void setLibraryName(String libraryName)
  {
    _libraryName = libraryName;
  }

  /**
   * Get the short name.
   * @return the short name
   */
  @Column(unique=true, nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getShortName()
  {
    return _shortName;
  }

  /**
   * Set the short name.
   * @param shortName the new short name
   */
  public void setShortName(String shortName)
  {
    _shortName = shortName;
  }

  /**
   * Get the description.
   * @return the description
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set the description.
   * @param description the new description
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * Get the vendor.
   * @return the vendor
   */
  @org.hibernate.annotations.Type(type="text")
  public String getVendor()
  {
    return _vendor;
  }

  /**
   * Set the vendor.
   * @param vendor the new vendor
   */
  public void setVendor(String vendor)
  {
    _vendor = vendor;
  }

  /**
   * Get the screen type.
   * @return the screen type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.screens.ScreenType$UserType"
  )
  public ScreenType getScreenType()
  {
    return _screenType;
  }

  /**
   * Set the screen type.
   * @param screenType the new screen type
   */
  public void setScreenType(ScreenType screenType)
  {
    _screenType = screenType;
  }

  /**
   * Get the library type.
   * @return the library type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.LibraryType$UserType"
  )
  public LibraryType getLibraryType()
  {
    return _libraryType;
  }

  /**
   * Set the library type.
   * @param libraryType the new library type
   */
  public void setLibraryType(LibraryType libraryType)
  {
    _libraryType = libraryType;
  }

  /**
   * Get the start plate.
   * @return the start plate
   */
  @Column(unique=true, nullable=false)
  public Integer getStartPlate()
  {
    return _startPlate;
  }

  /**
   * Set the start plate.
   * @param startPlate the new start plate
   */
  public void setStartPlate(Integer startPlate)
  {
    _startPlate = startPlate;
  }

  /**
   * Get the end plate.
   * @return the end plate
   */
  @Column(unique=true, nullable=false)
  public Integer getEndPlate()
  {
    return _endPlate;
  }

  /**
   * Set the end plate.
   * @param endPlate the new end plate
   */
  public void setEndPlate(Integer endPlate)
  {
    _endPlate = endPlate;
  }

  /**
   * Return true iff this library contains the specified plate.
   * @param plateNumber
   * @return true iff this library contains the specified plate
   */
  public boolean containsPlate(Integer plateNumber)
  {
    return plateNumber != null && plateNumber >= getStartPlate() && plateNumber <= getEndPlate();
  }

  /**
   * Get the alias.
   * @return the alias
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAlias()
  {
    return _alias;
  }

  /**
   * Set the alias.
   * @param alias the new alias
   */
  public void setAlias(String alias)
  {
    _alias = alias;
  }

  /**
   * Get the screenability.
   * @return the screenability
   */
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.IsScreenable$UserType"
  )
  public IsScreenable getIsScreenable()
  {
    return _isScreenable;
  }

  /**
   * Set the screenability.
   * @param isScreenable the new screenability
   */
  public void setIsScreenable(IsScreenable isScreenable)
  {
    _isScreenable = isScreenable;
  }

  /**
   * Get the compound count.
   * @return the compound count
   * @motivation from the libraries Filemaker database. we dont need to store this forever,
   * since we can always compute it, but we need to reconcile the values from the Filemaker
   * database with reality before we can get rid of it.
   */
  public Integer getCompoundCount()
  {
    return _compoundCount;
  }

  /**
   * Set the compound count.
   * @param compoundCount the new compound count
   */
  public void setCompoundCount(Integer compoundCount)
  {
    _compoundCount = compoundCount;
  }

  /**
   * Get the screening copy.
   * @return the screening copy
   */
  @org.hibernate.annotations.Type(type="text")
  public String getScreeningCopy()
  {
    return _screeningCopy;
  }

  /**
   * Set the screening copy.
   * @param screeningCopy the new screening copy
   */
  public void setScreeningCopy(String screeningCopy)
  {
    _screeningCopy = screeningCopy;
  }

  /**
   * Get the compound concentration in the screening copy.
   * @return the compound concentration in the screening copy
   */
  @org.hibernate.annotations.Type(type="text")
  public String getCompoundConcentrationInScreeningCopy()
  {
    return _compoundConcentrationInScreeningCopy;
  }

  /**
   * Set the compound concentration in the screening copy.
   * @param compoundConcentrationInScreeningCopy the new compound concentration in the screening copy
   */
  public void setCompoundConcentrationInScreeningCopy(String compoundConcentrationInScreeningCopy)
  {
    _compoundConcentrationInScreeningCopy = compoundConcentrationInScreeningCopy;
  }

  /**
   * Get the cherry pick copy.
   * @return the cherry pick copy
   */
  @org.hibernate.annotations.Type(type="text")
  public String getCherryPickCopy()
  {
    return _cherryPickCopy;
  }

  /**
   * Set the cherry pick copy.
   * @param cherryPickCopy the new cherry pick copy
   */
  public void setCherryPickCopy(String cherryPickCopy)
  {
    _cherryPickCopy = cherryPickCopy;
  }

  /**
   * Get the date received.
   * @return the date received
   */
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDateReceived()
  {
    return _dateReceived;
  }

  /**
   * Set the date received.
   * @param dateReceived the new date received
   */
  public void setDateReceived(LocalDate dateReceived)
  {
    _dateReceived = dateReceived;
  }

  /**
   * Get the date screenable.
   * @return the date screenable
   */
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDateScreenable()
  {
    return _dateScreenable;
  }

  /**
   * Set the date screenable.
   * @param dateScreenable the new date screenable
   */
  public void setDateScreenable(LocalDate dateScreenable)
  {
    _dateScreenable = dateScreenable;
  }

  /**
   * Get the non-compound wells.
   * @return the non-compound wells
   */
  @org.hibernate.annotations.Type(type="text")
  public String getNonCompoundWells()
  {
    return _nonCompoundWells;
  }

  /**
   * Set the non-compound wells.
   * @param nonCompoundWells the new non-compound wells
   */
  public void setNonCompoundWells(String nonCompoundWells)
  {
    _nonCompoundWells = nonCompoundWells;
  }

  /**
   * Get the screening room comments.
   * @return the screening room comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getScreeningRoomComments()
  {
    return _screeningRoomComments;
  }

  /**
   * Set the screening room comments.
   * @param screeningRoomComments the new screening room comments
   */
  public void setScreeningRoomComments(String screeningRoomComments)
  {
    _screeningRoomComments = screeningRoomComments;
  }

  /**
   * Get the diversity set plates.
   * @return the diversity set plates
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDiversitySetPlates()
  {
    return _diversitySetPlates;
  }

  /**
   * Set the diversity set plates.
   * @param diversitySetPlates the new diversity set plates
   */
  public void setDiversitySetPlates(String diversitySetPlates)
  {
    _diversitySetPlates = diversitySetPlates;
  }

  /**
   * Get the mapped-from copy.
   * @return the mapped-from copy
   */
  @org.hibernate.annotations.Type(type="text")
  public String getMappedFromCopy()
  {
    return _mappedFromCopy;
  }

  /**
   * Set the mapped-from copy.
   * @param mappedFromCopy the new mapped-from copy
   */
  public void setMappedFromCopy(String mappedFromCopy)
  {
    _mappedFromCopy = mappedFromCopy;
  }

  /**
   * Get the mapped-from plate.
   * @return the mapped-from plate
   */
  @org.hibernate.annotations.Type(type="text")
  public String getMappedFromPlate()
  {
    return _mappedFromPlate;
  }

  /**
   * Set the mapped-from plate.
   * @param mappedFromPlate the new mapped-from plate
   */
  public void setMappedFromPlate(String mappedFromPlate)
  {
    _mappedFromPlate = mappedFromPlate;
  }

  /**
   * Get the purchased using funds from.
   * @return the purchased using funds from
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPurchasedUsingFundsFrom()
  {
    return _purchasedUsingFundsFrom;
  }

  /**
   * Set the purchased using funds from.
   * @param purchasedUsingFundsFrom the new purchased using funds from
   */
  public void setPurchasedUsingFundsFrom(String purchasedUsingFundsFrom)
  {
    _purchasedUsingFundsFrom = purchasedUsingFundsFrom;
  }

  /**
   * Get the plating funds supplied by.
   * @return the plating funds supplied by
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPlatingFundsSuppliedBy()
  {
    return _platingFundsSuppliedBy;
  }

  /**
   * Set the plating funds supplied by.
   * @param platingFundsSuppliedBy the new plating funds supplied by
   */
  public void setPlatingFundsSuppliedBy(String platingFundsSuppliedBy)
  {
    _platingFundsSuppliedBy = platingFundsSuppliedBy;
  }

  /**
   * Get the informatics comments.
   * @return the informatics comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getInformaticsComments()
  {
    return _informaticsComments;
  }

  /**
   * Set the informatics comments.
   * @param informaticsComments the new informatics comments
   */
  public void setInformaticsComments(String informaticsComments)
  {
    _informaticsComments = informaticsComments;
  }

  /**
   * Get the data file location.
   * @return the data file location
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDataFileLocation()
  {
    return _dataFileLocation;
  }

  /**
   * Set the data file location.
   * @param dataFileLocation the new data file location
   */
  public void setDataFileLocation(String dataFileLocation)
  {
    _dataFileLocation = dataFileLocation;
  }

  /**
   * Get the chemist DOS.
   * @return the chemist DOS
   */
  @org.hibernate.annotations.Type(type="text")
  public String getChemistDOS()
  {
    return _chemistDOS;
  }

  /**
   * Set the chemist DOS.
   * @param chemistDOS the new chemist DOS
   */
  public void setChemistDOS(String chemistDOS)
  {
    _chemistDOS = chemistDOS;
  }

  /**
   * Get the chemistry comments.
   * @return the chemistry comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getChemistryComments()
  {
    return _chemistryComments;
  }

  /**
   * Set the chemistry comments.
   * @param chemistryComments the new chemistry comments
   */
  public void setChemistryComments(String chemistryComments)
  {
    _chemistryComments = chemistryComments;
  }

  /**
   * Get the screening set.
   * @return the screening set
   */
  @org.hibernate.annotations.Type(type="text")
  public String getScreeningSet()
  {
    return _screeningSet;
  }

  /**
   * Set the screening set.
   * @param screeningSet the new screening set
   */
  public void setScreeningSet(String screeningSet)
  {
    _screeningSet = screeningSet;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>Library</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Library() {}


  // private instance methods

  /**
   * Set the id for the screening library.
   * @param libraryId the new id for the screening library
   * @motivation for hibernate
   */
  private void setLibraryId(Integer libraryId)
  {
    _libraryId = libraryId;
  }

  /**
   * Get the version for the screening library.
   * @return the version for the screening library
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the screening library.
   * @param version the new version for the screening library
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of wells.
   * @param wells the new set of wells
   * @motivation for hibernate
   */
  private void setWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the set of copies.
   * @param copies the new set of copies
   * @motivation for hibernate
   */
  private void setCopies(Set<Copy> copies)
  {
    _copies = copies;
  }
}
