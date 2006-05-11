// $HeadURL$
// $Id$


package edu.harvard.med.screensaver.beans.libraries;

import java.util.HashSet;
import java.util.Set;

/**
 * A Hibernate entity bean representing a molecular compound.
 * 
 * @author john sullivan
 * @hibernate.class
 */
public class Compound {

  
  // instance fields
  
	private Integer     _compoundId;
  private Integer     _version;
  private Set<Well>   _wells = new HashSet<Well>();
	private String      _compoundName;
	private String      _smiles;
  private boolean     _isSalt;
  private Set<String> _synonyms = new HashSet<String>();
  private Set<String> _casNumbers = new HashSet<String>();
  private Set<String> _nscNumbers = new HashSet<String>();
  private String      _pubchemCid;
  private String      _chembankId;
	
  
  // getters and setters
  
	/**
	 * @return Returns the id.
   * 
	 * @hibernate.id
   *   column="compound_id"
   *   generator-class="sequence"
   * @hibernate.generator-param
   *   name="sequence"
   *   value="compound_id_seq"
   */
	public Integer getCompoundId() {
		return _compoundId;
	}
  
  /**
   * @param compoundId The compoundId to set.
   */
  public void setCompoundId(Integer compoundId) {
    _compoundId = compoundId;
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
   *   inverse="true"
   *   table="well_compound_link"
   *   lazy="true"
   *   cascade="save-update"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-many-to-many
   *   column="well_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Well"
   */
  public Set<Well> getWells() {
    return new HashSet<Well>(_wells);
  }

  /**
   * @return The actual set of wells.
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
   * @param well The well to add.
   * @return true if wells did not already contain the specified well.
   */
  public boolean addWell(Well well) {
    well.getMutableCompounds().add(this);
    return _wells.add(well);
  }
  
  /**
   * @param well The well to remove.
   * @return true if wells contained the specified well.
   */
  public boolean removeWell(Well well) {
    well.getMutableCompounds().remove(this);
    return _wells.remove(well);
  }
  
  /**
   * Get the compound name.
   * @return the compound name
   * 
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
  public String getCompoundName() {
    return _compoundName;
  }

  /**
   * Set the compound name.
   * @param compoundName the new name of the compound
   */
  public void setCompoundName(String compoundName) {
    _compoundName = compoundName;
  }

  /**
   * @return Returns the smiles.
   * 
   * @hibernate.property
   *   type="text"
   *   unique="true"
   */
  public String getSmiles() {
    return _smiles;
  }

  /**
   * @param smiles The smiles to set.
   */
  public void setSmiles(String smiles) {
    _smiles = smiles;
  }

  /**
   * @return Returns the isSalt.
   * 
   * @hibernate.property
   *   column="is_salt"
   *   not-null="true"
   */
  public boolean isSalt() {
    return _isSalt;
  }

  /**
   * @param isSalt The isSalt to set.
   */
  public void setSalt(boolean isSalt) {
    _isSalt = isSalt;
  }

  /**
   * @return Returns the synonyms.
   *
   * @hibernate.set
   *   order-by="synonym"
   *   table="compound_synonym"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="synonym"
   *   not-null="true"
   */
  public Set<String> getSynonyms() {
    return new HashSet<String>(_synonyms);
  }

  /**
   * @param synonyms The synonyms to set.
   */
  protected void setSynonyms(Set<String> synonyms) {
    _synonyms = synonyms;
  }

  /**
   * @param synonym The synonym to add.
   * @return true if the synonym did not already exist.
   */
  public boolean addSynonym(String synonym) {
    return _synonyms.add(synonym);
  }

  /**
   * @param synonym The synonym to remove.
   * @return true if the synonym existed.
   */
  public boolean removeSynonym(String synonym) {
    return _synonyms.remove(synonym);
  }

  /**
   * @return Returns the casNumber.
   * 
   * @hibernate.set
   *   order-by="cas_number"
   *   table="compound_cas_number"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="cas_number"
   *   not-null="true"
   */
  public Set<String> getCasNumbers() {
    return  new HashSet<String>(_casNumbers);
  }

  /**
   * @param casNumber The casNumber to set.
   */
  protected void setCasNumbers(Set<String> casNumber) {
    _casNumbers = casNumber;
  }

  /**
   * @param casNumber The casNumber to add.
   * @return true iff the casNumber was added.
   */
  public boolean addCasNumber(String casNumber) {
    return _casNumbers.add(casNumber);
  }

  /**
   * @param casNumber The casNumber to remove.
   * @return true iff the casNumber was removed.
   */
  public boolean removeCasNumber(Object casNumber) {
    return _casNumbers.remove(casNumber);
  }

  /**
   * @return Returns the nscNumber.
   * 
   * @hibernate.set
   *   order-by="nsc_number"
   *   table="compound_nsc_number"
   *   cascade="delete"
   *   laxy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="nsc_number"
   *   not-null="true"
   */
  public Set<String> getNscNumbers() {
    return new HashSet<String>(_nscNumbers);
  }

  /**
   * @param nscNumber The nscNumber to set.
   */
  protected void setNscNumbers(Set<String> nscNumber) {
    _nscNumbers = nscNumber;
  }

  /**
   * @param nscNumber The nscNumber to add.
   * @return true iff the nscNumebr was added.
   */
  public boolean addNscNumber(String nscNumber) {
    return _nscNumbers.add(nscNumber);
  }
  
  /**
   * @param nscNumber The nscNumber to remove.
   * @return true iff the nscNumber was removed.
   */
  public boolean removeNscNumber(String nscNumber) {
    return _nscNumbers.remove(nscNumber);
  }

  /**
   * @return Returns the pubchemCid.
   * @hibernate.property
   *   type="text"
   */
  public String getPubchemCid() {
    return _pubchemCid;
  }

  /**
   * @param pubchemCid The pubchemCid to set.
   */
  public void setPubchemCid(String pubchemCid) {
    _pubchemCid = pubchemCid;
  }

  /**
   * @return Returns the chembankId.
   * @hibernate.property
   *   type="text"
   */
  public String getChembankId() {
    return _chembankId;
  }
  
  /**
   * @param chembankId The chembankId to set.
   */
  public void setChembankId(String chembankId) {
    _chembankId = chembankId;
  }

  
  // identity methods
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object object) {
    if (! (object instanceof Compound)) {
      return false;
    }
    Compound that = (Compound) object;
    return
      this.getCompoundName().equals(that.getCompoundName());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getCompoundName().hashCode();
  }
}
