// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.beans.libraries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A Hibernate entity bean representing a well.
 * 
 * @author john sullivan
 * @hibernate.class
 */
public class Well {

  
  // instance fields
  
  private Integer       _wellId;
  private Integer       _version;
  private Library       _library;
  private Set<Compound> _compounds = new HashSet<Compound>();
  private Integer       _plateNumber;
  private String        _wellName;
  private String        _iccbNumber;
  private String        _vendorIdentifier;
  
  
  // public getters and setters
  
  /**
   * Get the well id for the well.
   * @return the well id for the well
   *
   * @hibernate.id
   *   generator-class="sequence"
   * @hibernate.generator-param
   *   name="sequence"
   *   value="well_id_seq"
   */
  public Integer getWellId() {
    return _wellId;
  }

  /**
   * Get the library the well is in.
   * @return the library the well is in
   *
   * @hibernate.many-to-one
   *   column="library_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Library"
   *   cascade="save-update"
   *   not-null="true"
   *   foreign-key="fk_well_to_library"
   */
  public Library getLibrary() {
    return _library;
  }

  /**
   * Set the library the well is in.
   * @param library the new library for the well
   */
  public void setLibrary(Library library) {
    library.getModifiableWellSet().add(this);
    if (_library != null) {
      _library.getModifiableWellSet().remove(this);
    }
    _library = library;
  }

  /**
   * Get the set of compounds contained in the well.
   * @return the set of compounds contained in the well
   *
   * @hibernate.set
   *   table="well_compound_link"
   *   lazy="true"
   *   cascade="save-update"
   * @hibernate.collection-key
   *   column="well_id"
   * @hibernate.collection-many-to-many
   *   column="compound_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Compound"
   *   foreign-key="fk_well_compound_link_to_well"
   */
  public Set<Compound> getCompounds() {
    return Collections.unmodifiableSet(_compounds);
  }

  /**
   * Add the compound to the well.
   * @param compound the compound to add to the well
   * @return         true iff the compound was not already in the well
   */
  public boolean addCompound(Compound compound) {
    compound.getModifiableWellSet().add(this);
    return _compounds.add(compound);
  }

  /**
   * Remove the compound from the well.
   * @param compound the compound to remove from the well
   * @return         true iff the compound was previously in the well
   */
  public boolean removeCompound(Compound compound) {
    compound.getModifiableWellSet().remove(this);
    return _compounds.remove(compound);
  }
  
  /**
   * Get the plate number for the well.
   * @return the plate number for the well
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getPlateNumber() {
    return _plateNumber;
  }

  /**
   * Set the plate number for the well.
   * @param plateNumber the new plate number for the well
   */
  public void setPlateNumber(Integer plateNumber) {
    _plateNumber = plateNumber;
  }

  /**
   * Get the well name for the well.
   * @return the well name for the well
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getWellName() {
    return _wellName;
  }

  /**
   * Set the well name for the well.
   * @param wellName the new well name for the well
   */
  public void setWellName(String wellName) {
    _wellName = wellName;
  }

  /**
   * Get the ICCB number for the well.
   * @return the ICCB number for the well
   *
   * @hibernate.property
   *   type="text"
   */
  public String getIccbNumber() {
    return _iccbNumber;
  }
  
  /**
   * Set the ICCB number for the well.
   * @param iccbNumber The new ICCB number for the well
   */
  public void setIccbNumber(String iccbNumber) {
    _iccbNumber = iccbNumber;
  }
  
  /**
   * Get the vendor identifier for the well.
   * @return the vendor identifier for the well
   *
   * @hibernate.property
   *   type="text"
   */
  public String getVendorIdentifier() {
    return _vendorIdentifier;
  }
  
  /**
   * Set the vendor identifier for the well.
   * @param vendorIdentifier the new vendor identifier for the well
   */
  public void setVendorIdentifier(String vendorIdentifier) {
    _vendorIdentifier = vendorIdentifier;
  }

  
  // identity methods
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object object) {
    if (! (object instanceof Well)) {
      return false;
    }
    Well that = (Well) object;
    return
      this.getPlateNumber().equals(that.getPlateNumber()) &&
      this.getWellName().equals(that.getWellName());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getPlateNumber().hashCode() + getWellName().hashCode();
  }

  
  // protected getters and setters
  
  /**
   * Get the modifiable set of compounds.
   * @return the modifiable set of compounds
   * @motivation allow efficient maintenance of the bi-directional relationship
   *             between {@link Compound} and {@link Well}.
   */
  protected Set<Compound> getModifiableCompoundSet() {
    return _compounds;
  }

  
  // private getters and setters
  
  /**
   * Set the well id for the well.
   * @param wellId the new well id for the well
   * @motivation   for hibernate
   */
  private void setWellId(Integer wellId) {
    _wellId = wellId;
  }

  /**
   * Get the version of the well.
   * @return     the version of the well
   * @motivation for hibernate
   *
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version of the well.
   * @param version the new version of the well
   * @motivation    for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the set of compounds contained in the well.
   * @param compounds the new set of compounds contained in the well
   * @motivation      for hibernate
   * @motivation      hibernate actually calls this method with the result of
   *                  {@link #getCompounds}, which, for purposes of a coherent
   *                  public API for the bean, returns an unmodifiable set. we
   *                  must in turn recast the set into a modifiable set, so that
   *                  further calls to {@link #addCompound} and
   *                  {@link #removeCompound} function properly.
   */
  private void setCompounds(Set<Compound> compounds) {
    _compounds = compounds;
  }  
}
