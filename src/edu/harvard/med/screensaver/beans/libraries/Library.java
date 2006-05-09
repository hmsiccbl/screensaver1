// Library.java
// by john sullivan 2006.05

package edu.harvard.med.screensaver.beans.libraries;

import java.util.HashSet;
import java.util.Set;


/**
 * A Hibernate entity bean representing a screening library.
 *
 * @author john sullivan
 * @hibernate.class
 */
public class Library {

  
  // instance fields
  
  private Integer   _libraryId;
  private Integer   _version;
  private Set<Well> _wells = new HashSet<Well>();
  private String    _libraryName;
  private String    _shortName;
  private String    _description;
  private String    _vendor;
  private String    _libraryType;
  private Integer   _startPlate;
  private Integer   _endPlate;
  
  
  // getters and setters
  
  /**
   * @return Returns the libraryId.
   *
   * @hibernate.id
   *   generator-class="sequence"
   *   column="library_id"
   */
  public Integer getLibraryId() {
    return _libraryId;
  }

  /**
   * @param libraryId The libraryId to set.
   */
  public void setLibraryId(Integer libraryId) {
    _libraryId = libraryId;
  }

  /**
   * @return Returns the version.
   *
   * @hibernate.version
   */
  public Integer getVersion() {
    return _version;
  }

  /**
   * @param version The version to set.
   */
  public void setVersion(Integer version) {
    _version = version;
  }

  /**
   * @return Returns the wells.
   *
   * @hibernate.set
   *   lazy="true"
   *   order-by="plate_number,well_name"
   * @hibernate.collection-key
   *   column="library_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.beans.libraries.Well"
   */
  public Set<Well> getWells() {
    return new HashSet<Well>(_wells);
  }

  /**
   * @return the actual wells.
   */
  protected Set<Well> getMutableWells() {
    return _wells;
  }
  
  /**
   * @param wells The wells to set.
   */
  protected void setWells(Set<Well> wells) {
    _wells = wells;
  }

  /**
   * @return Returns the libraryName.
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getLibraryName() {
    return _libraryName;
  }

  /**
   * @param name The libraryName to set.
   */
  public void setLibraryName(String name) {
    _libraryName = name;
  }

  /**
   * @return Returns the shortName.
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getShortName() {
    return _shortName;
  }

  /**
   * @param shortName The shortName to set.
   */
  public void setShortName(String shortName) {
    _shortName = shortName;
  }

  /**
   * @return Returns the description.
   *
   * @hibernate.property
   *   type="text"
   */
  public String getDescription() {
    return _description;
  }
  
  /**
   * @param description The description to set.
   */
  public void setDescription(String description) {
    _description = description;
  }
  
  /**
   * @return Returns the vendor.
   *
   * @hibernate.property
   *   type="text"
   */
  public String getVendor() {
    return _vendor;
  }

  /**
   * @param vendor The vendor to set.
   */
  public void setVendor(String vendor) {
    _vendor = vendor;
  }

  /**
   * @return Returns the libraryType.
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getLibraryType() {
    return _libraryType;
  }

  /**
   * @param library_type The libraryType to set.
   */
  public void setLibraryType(String library_type) {
    _libraryType = library_type;
  }

  /**
   * @return Returns the startPlate.
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getStartPlate() {
    return _startPlate;
  }

  /**
   * @param startPlate The startPlate to set.
   */
  public void setStartPlate(Integer startPlate) {
    _startPlate = startPlate;
  }

  /**
   * @return Returns the endPlate.
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getEndPlate() {
    return _endPlate;
  }
  
  /**
   * @param endPlate The endPlate to set.
   */
  public void setEndPlate(Integer endPlate) {
    _endPlate = endPlate;
  }
}
