// Well.java
// by john sullivan 2006.05

package edu.harvard.med.screensaver.beans.libraries;

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
  private Set<Compound> _compounds;
  private String        _plateName;
  private String        _wellName;
  private String        _iccbNumber;
  private String        _vendorIdentifier;
  
  
  // getters and setters
  
  /**
   * @return Returns the wellId.
   *
   * @hibernate.id
   *   generator-class="native"
   *   column="well_id"
   */
  public Integer getWellId() {
    return _wellId;
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
   * @return Returns the plateName.
   *
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getPlateName() {
    return _plateName;
  }

  /**
   * @param plateName The plateName to set.
   */
  public void setPlateName(String plateName) {
    _plateName = plateName;
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
