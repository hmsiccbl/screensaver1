// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.screens.ScreenType;


/**
 * A Hibernate entity bean representing a screening library.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class Library extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Library.class);
  private static final long serialVersionUID = 0L;


  // instance fields

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
  private Date _dateReceived;
  private Date _dateScreenable;
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
   * Constructs an initialized <code>Library</code> object.
   *
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


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getLibraryId();
  }

  /**
   * Get the id for the screening library.
   *
   * @return the id for the screening library
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="library_id_seq"
   */
  public Integer getLibraryId()
  {
    return _libraryId;
  }

  /**
   * Get an unmodifiable copy of the set of wells.
   *
   * @return the wells
   */
  public Set<Well> getWells()
  {
    return Collections.unmodifiableSet(_wells);
  }
  
  /**
   * Get the number of wells.
   * 
   * @return the number of wells
   * @motivation {@link #getWells} forces loading of all wells, just to get the
   *             size; Hibernate can optimize a collection size request, if we
   *             get directly from the underyling persistent collection.
   */
  @DerivedEntityProperty
  public int getNumWells()
  {
    return _wells.size();
  }

  /**
   * Add the well.
   *
   * @param well the well to add
   * @return true iff the screening library did not already have the well
   */
  public boolean addWell(Well well)
  {
    if (getHbnWells().add(well)) {
      well.setHbnLibrary(this);
      return true;
    }
    return false;
  }
  
  /**
   * Get an unmodifiable copy of the set of copies.
   *
   * @return the copies
   */
  public Set<Copy> getCopies()
  {
    return Collections.unmodifiableSet(_copies);
  }

  /**
   * Get the number of copies.
   * 
   * @return the number of copies
   * @motivation {@link #getCopis} forces loading of all copies, just to get the
   *             size; Hibernate can optimize a collection size request, if we
   *             get directly from the underyling persistent collection.
   */
  @DerivedEntityProperty
  public int getNumCopies()
  {
    return _copies.size();
  }

  /**
   * Add the copy.
   *
   * @param copy the copie to add
   * @return true iff the screening library did not already have the copie
   */
  public boolean addCopy(Copy copy)
  {
    if (getHbnCopies().add(copy)) {
      copy.setHbnLibrary(this);
      return true;
    }
    return false;
  }

  /**
   * Get the library name.
   *
   * @return the library name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
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
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
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
   * @hibernate.property
   *   type="text"
   */
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
   * @hibernate.property
   *   type="text"
   */
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
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.ScreenType$UserType"
   *   not-null="true"
   */
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
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.libraries.LibraryType$UserType"
   *   not-null="true"
   */
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
   * Get the start plate.
   *
   * @return the start plate
   * @hibernate.property
   *   not-null="true"
   *   unique="true"
   */
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
   * @hibernate.property
   *   not-null="true"
   *   unique="true"
   */
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
   * Get the alias.
   *
   * @return the alias
   * @hibernate.property
   *   type="text"
   */
  public String getAlias()
  {
    return _alias;
  }

  /**
   * Set the alias.
   *
   * @param alias the new alias
   */
  public void setAlias(String alias)
  {
    _alias = alias;
  }

  /**
   * Get the screenability.
   *
   * @return the screenability
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.libraries.IsScreenable$UserType"
   */
  public IsScreenable getIsScreenable()
  {
    return _isScreenable;
  }

  /**
   * Set the screenability.
   *
   * @param isScreenable the new screenability
   */
  public void setIsScreenable(IsScreenable isScreenable)
  {
    _isScreenable = isScreenable;
  }

  /**
   * Get the compound count.
   *
   * @return the compound count
   * @hibernate.property
   */
  public Integer getCompoundCount()
  {
    return _compoundCount;
  }

  /**
   * Set the compound count.
   *
   * @param compoundCount the new compound count
   */
  public void setCompoundCount(Integer compoundCount)
  {
    _compoundCount = compoundCount;
  }

  /**
   * Get the screening copy.
   *
   * @return the screening copy
   * @hibernate.property
   *   type="text"
   */
  public String getScreeningCopy()
  {
    return _screeningCopy;
  }

  /**
   * Set the screening copy.
   *
   * @param screeningCopy the new screening copy
   */
  public void setScreeningCopy(String screeningCopy)
  {
    _screeningCopy = screeningCopy;
  }

  /**
   * Get the compound concentration in the screening copy.
   *
   * @return the compound concentration in the screening copy
   * @hibernate.property
   *   type="text"
   */
  public String getCompoundConcentrationInScreeningCopy()
  {
    return _compoundConcentrationInScreeningCopy;
  }

  /**
   * Set the compound concentration in the screening copy.
   *
   * @param compoundConcentrationInScreeningCopy the new compound concentration in the screening copy
   */
  public void setCompoundConcentrationInScreeningCopy(String compoundConcentrationInScreeningCopy)
  {
    _compoundConcentrationInScreeningCopy = compoundConcentrationInScreeningCopy;
  }

  /**
   * Get the cherry pick copy.
   *
   * @return the cherry pick copy
   * @hibernate.property
   *   type="text"
   */
  public String getCherryPickCopy()
  {
    return _cherryPickCopy;
  }

  /**
   * Set the cherry pick copy.
   *
   * @param cherryPickCopy the new cherry pick copy
   */
  public void setCherryPickCopy(String cherryPickCopy)
  {
    _cherryPickCopy = cherryPickCopy;
  }

  /**
   * Get the date received.
   *
   * @return the date received
   * @hibernate.property
   */
  public Date getDateReceived()
  {
    return _dateReceived;
  }

  /**
   * Set the date received.
   *
   * @param dateReceived the new date received
   */
  public void setDateReceived(Date dateReceived)
  {
    _dateReceived = dateReceived;
  }

  /**
   * Get the date screenable.
   *
   * @return the date screenable
   * @hibernate.property
   */
  public Date getDateScreenable()
  {
    return _dateScreenable;
  }

  /**
   * Set the date screenable.
   *
   * @param dateScreenable the new date screenable
   */
  public void setDateScreenable(Date dateScreenable)
  {
    _dateScreenable = dateScreenable;
  }

  /**
   * Get the non-compound wells.
   *
   * @return the non-compound wells
   * @hibernate.property
   *   type="text"
   */
  public String getNonCompoundWells()
  {
    return _nonCompoundWells;
  }

  /**
   * Set the non-compound wells.
   *
   * @param nonCompoundWells the new non-compound wells
   */
  public void setNonCompoundWells(String nonCompoundWells)
  {
    _nonCompoundWells = nonCompoundWells;
  }

  /**
   * Get the screening room comments.
   *
   * @return the screening room comments
   * @hibernate.property
   *   type="text"
   */
  public String getScreeningRoomComments()
  {
    return _screeningRoomComments;
  }

  /**
   * Set the screening room comments.
   *
   * @param screeningRoomComments the new screening room comments
   */
  public void setScreeningRoomComments(String screeningRoomComments)
  {
    _screeningRoomComments = screeningRoomComments;
  }

  /**
   * Get the diversity set plates.
   *
   * @return the diversity set plates
   * @hibernate.property
   *   type="text"
   */
  public String getDiversitySetPlates()
  {
    return _diversitySetPlates;
  }

  /**
   * Set the diversity set plates.
   *
   * @param diversitySetPlates the new diversity set plates
   */
  public void setDiversitySetPlates(String diversitySetPlates)
  {
    _diversitySetPlates = diversitySetPlates;
  }

  /**
   * Get the mapped-from copy.
   *
   * @return the mapped-from copy
   * @hibernate.property
   *   type="text"
   */
  public String getMappedFromCopy()
  {
    return _mappedFromCopy;
  }

  /**
   * Set the mapped-from copy.
   *
   * @param mappedFromCopy the new mapped-from copy
   */
  public void setMappedFromCopy(String mappedFromCopy)
  {
    _mappedFromCopy = mappedFromCopy;
  }

  /**
   * Get the mapped-from plate.
   *
   * @return the mapped-from plate
   * @hibernate.property
   *   type="text"
   */
  public String getMappedFromPlate()
  {
    return _mappedFromPlate;
  }

  /**
   * Set the mapped-from plate.
   *
   * @param mappedFromPlate the new mapped-from plate
   */
  public void setMappedFromPlate(String mappedFromPlate)
  {
    _mappedFromPlate = mappedFromPlate;
  }

  /**
   * Get the purchased using funds from.
   *
   * @return the purchased using funds from
   * @hibernate.property
   *   type="text"
   */
  public String getPurchasedUsingFundsFrom()
  {
    return _purchasedUsingFundsFrom;
  }

  /**
   * Set the purchased using funds from.
   *
   * @param purchasedUsingFundsFrom the new purchased using funds from
   */
  public void setPurchasedUsingFundsFrom(String purchasedUsingFundsFrom)
  {
    _purchasedUsingFundsFrom = purchasedUsingFundsFrom;
  }

  /**
   * Get the plating funds supplied by.
   *
   * @return the plating funds supplied by
   * @hibernate.property
   *   type="text"
   */
  public String getPlatingFundsSuppliedBy()
  {
    return _platingFundsSuppliedBy;
  }

  /**
   * Set the plating funds supplied by.
   *
   * @param platingFundsSuppliedBy the new plating funds supplied by
   */
  public void setPlatingFundsSuppliedBy(String platingFundsSuppliedBy)
  {
    _platingFundsSuppliedBy = platingFundsSuppliedBy;
  }

  /**
   * Get the informatics comments.
   *
   * @return the informatics comments
   * @hibernate.property
   *   type="text"
   */
  public String getInformaticsComments()
  {
    return _informaticsComments;
  }

  /**
   * Set the informatics comments.
   *
   * @param informaticsComments the new informatics comments
   */
  public void setInformaticsComments(String informaticsComments)
  {
    _informaticsComments = informaticsComments;
  }

  /**
   * Get the data file location.
   *
   * @return the data file location
   * @hibernate.property
   *   type="text"
   */
  public String getDataFileLocation()
  {
    return _dataFileLocation;
  }

  /**
   * Set the data file location.
   *
   * @param dataFileLocation the new data file location
   */
  public void setDataFileLocation(String dataFileLocation)
  {
    _dataFileLocation = dataFileLocation;
  }

  /**
   * Get the chemist DOS.
   *
   * @return the chemist DOS
   * @hibernate.property
   *   type="text"
   */
  public String getChemistDOS()
  {
    return _chemistDOS;
  }

  /**
   * Set the chemist DOS.
   *
   * @param chemistDOS the new chemist DOS
   */
  public void setChemistDOS(String chemistDOS)
  {
    _chemistDOS = chemistDOS;
  }

  /**
   * Get the chemistry comments.
   *
   * @return the chemistry comments
   * @hibernate.property
   *   type="text"
   */
  public String getChemistryComments()
  {
    return _chemistryComments;
  }

  /**
   * Set the chemistry comments.
   *
   * @param chemistryComments the new chemistry comments
   */
  public void setChemistryComments(String chemistryComments)
  {
    _chemistryComments = chemistryComments;
  }

  /**
   * Get the screening set.
   *
   * @return the screening set
   * @hibernate.property
   *   type="text"
   */
  public String getScreeningSet()
  {
    return _screeningSet;
  }

  /**
   * Set the screening set.
   *
   * @param screeningSet the new screening set
   */
  public void setScreeningSet(String screeningSet)
  {
    _screeningSet = screeningSet;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return getLibraryName();
  }


  // package methods

  /**
   * Get the wells.
   *
   * @return the wells
   * @hibernate.set
   *   order-by="plate_number,well_name"
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="library_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.libraries.Well"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<Well> getHbnWells()
  {
    return _wells;
  }

  /**
   * Get the copies.
   *
   * @return the copies
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="library_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.libraries.Copy"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<Copy> getHbnCopies()
  {
    return _copies;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>Library</code> object.
   *
   * @motivation for hibernate
   */
  private Library() {}


  // private methods

  /**
   * Set the id for the screening library.
   *
   * @param libraryId the new id for the screening library
   * @motivation for hibernate
   */
  private void setLibraryId(Integer libraryId) {
    _libraryId = libraryId;
  }

  /**
   * Get the version for the screening library.
   *
   * @return the version for the screening library
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the screening library.
   *
   * @param version the new version for the screening library
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the wells.
   *
   * @param wells the new wells
   * @motivation for hibernate
   */
  private void setHbnWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the copies.
   *
   * @param copies the new copies
   * @motivation for hibernate
   */
  private void setHbnCopies(Set<Copy> copies)
  {
    _copies = copies;
  }
}
