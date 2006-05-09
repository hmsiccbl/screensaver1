// Well.java
// by john sullivan 2006.05

package edu.harvard.med.screensaver.beans.libraries;

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
  
  
  // getters and setters
  
  /**
   * @return Returns the wellId.
   *
   * @hibernate.id
   *   generator-class="sequence"
   *   column="well_id"
   */
  public Integer getWellId() {
    return _wellId;
  }

  /**
   * @param wellId The wellId to set.
   */
  public void setWellId(Integer wellId) {
    _wellId = wellId;
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
   * @return Returns the library.
   *
   * @hibernate.many-to-one
   *   column="library_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Library"
   *   cascade="delete"
   *   not-null="true"
   */
  public Library getLibrary() {
    return _library;
  }

  /**
   * @param library The library to set.
   */
  public void setLibrary(Library library) {
    _library = library;
  }

  /**
   * @return Returns the compounds.
   *
   * @hibernate.set
   *   table="well_compound_link"
   * @hibernate.collection-key
   *   column="well_id"
   * @hibernate.collection-many-to-many
   *   column="compound_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Compound"
   */
  public Set<Compound> getCompounds() {
    return _compounds;
  }

  /**
   * @param compounds The compounds to set.
   */
  public void setCompounds(Set<Compound> compounds) {
    _compounds = compounds;
  }

  /**
   * @param compound The compound to add.
   * @return true iff the compound was added.
   */
  public boolean addCompound(Compound compound) {
    return _compounds.add(compound);
  }
  
  /**
   * @param compound The compound to remove.
   * @return true iff the compound was removed.
   */
  public boolean removeCompound(Compound compound) {
    return _compounds.remove(compound);
  }
  
  /**
   * @return Returns the plateNumber.
   *
   * @hibernate.property
   *   not-null="true"
   */
  public Integer getPlateNumber() {
    return _plateNumber;
  }

  /**
   * @param plateNumber The plateNumber to set.
   */
  public void setPlateNumber(Integer plateNumber) {
    _plateNumber = plateNumber;
  }

  /**
   * @return Returns the wellName.
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getWellName() {
    return _wellName;
  }

  /**
   * @param wellName The wellName to set.
   */
  public void setWellName(String wellName) {
    _wellName = wellName;
  }

  /**
   * @return Returns the iccbNumber.
   *
   * @hibernate.property
   *   type="text"
   */
  public String getIccbNumber() {
    return _iccbNumber;
  }
  
  /**
   * @param iccbNumber The iccbNumber to set.
   */
  public void setIccbNumber(String iccbNumber) {
    _iccbNumber = iccbNumber;
  }
  
  /**
   * @return Returns the vendorIdentifier.
   *
   * @hibernate.property
   *   type="text"
   */
  public String getVendorIdentifier() {
    return _vendorIdentifier;
  }
  
  /**
   * @param vendorIdentifier The vendorIdentifier to set.
   */
  public void setVendorIdentifier(String vendorIdentifier) {
    _vendorIdentifier = vendorIdentifier;
  }
}
