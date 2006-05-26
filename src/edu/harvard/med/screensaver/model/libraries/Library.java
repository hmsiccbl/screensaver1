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
  
  
  // public getters and setters
  
  /**
   * Get the library id for the library.
   * @return the library id for the library
   *
   * @hibernate.id
   *   generator-class="sequence"
   * @hibernate.generator-param
   *   name="sequence"
   *   value="library_id_seq"
   */
  public Integer getLibraryId() {
    return _libraryId;
  }

  /**
   * Get the modifiable set of wells for the library. If the caller modifies the
   * returned collection, it must ensure that the bi-directional relationship is
   * maintained by updating the related {@Well} bean(s).
   * 
   * @return the set of wells for the library
   * @motivation for Hibernate and for associated {@link Well} bean (so that it
   *             can maintain the bi-directional association between
   *             {@link Well} and {@link Library}).
   * @hibernate.set order-by="plate_number,well_name"
   *                cascade="all-delete-orphan" 
   *                inverse="true"
   * @hibernate.collection-key column="library_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.beans.libraries.Well"
   */
  public Set<Well> getModifiableWells() {
    return _wells;
  }

  /**
   * Get the immutable set of wells contained in the library.
   * 
   * @return the immutable set of wells contained in the library
   */
  public Set<Well> getWells() {
    return Collections.unmodifiableSet(_wells);
  }

  /**
   * Get the name of the library.
   * @return the name of the library
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getLibraryName() {
    return _libraryName;
  }

  /**
   * Set the name of the library.
   * @param name the new name for the library
   */
  public void setLibraryName(String name) {
    _libraryName = name;
  }

  /**
   * Get the short name of the library.
   * @return the short name of the library
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getShortName() {
    return _shortName;
  }

  /**
   * Set the short name of the library.
   * @param shortName the new short name of the library
   */
  public void setShortName(String shortName) {
    _shortName = shortName;
  }

  /**
   * Get the library description.
   * @return the library description
   *
   * @hibernate.property
   *   type="text"
   */
  public String getDescription() {
    return _description;
  }
  
  /**
   * Set the library description.
   * @param description the new library description
   */
  public void setDescription(String description) {
    _description = description;
  }
  
  /**
   * Get the library vendor.
   * @return the library vendor
   *
   * @hibernate.property
   *   type="text"
   */
  public String getVendor() {
    return _vendor;
  }

  /**
   * Set the library vendor.
   * @param vendor the new library vendor
   */
  public void setVendor(String vendor) {
    _vendor = vendor;
  }

  /**
   * Get the library type.
   * @return the library type
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getLibraryType() {
    return _libraryType;
  }

  /**
   * Set the library type.
   * @param library_type the new library type
   */
  public void setLibraryType(String library_type) {
    _libraryType = library_type;
  }

  /**
   * Get the library start plate.
   * @return the library start plate
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getStartPlate() {
    return _startPlate;
  }

  /**
   * Set the library start plate.
   * @param startPlate the new library start plate
   */
  public void setStartPlate(Integer startPlate) {
    _startPlate = startPlate;
  }

  /**
   * Get the library end plate.
   * @return Returns the library end plate
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getEndPlate() {
    return _endPlate;
  }
  
  /**
   * Set the library end plate.
   * @param endPlate the new library end plate
   */
  public void setEndPlate(Integer endPlate) {
    _endPlate = endPlate;
  }

  
  // package getters and setters
  
  
  // private getters and setters
  
  /**
   * Set the library id for the library.
   * @param libraryId the new library id for the library
   * @motivation      for hibernate
   */
  private void setLibraryId(Integer libraryId) {
    _libraryId = libraryId;
  }

  /**
   * Get the version of the library.
   * @return     the version of the library
   * @motivation for hibernate
   *
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version of the library
   * @param version the new version of the library
   * @motivation    for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the set of wells for the library.
   * @param wells the new set of wells for the library
   * @motivation  for hibernate
   */
  private void setModifiableWells(Set<Well> wells) {
    _wells = wells;
  }
}
