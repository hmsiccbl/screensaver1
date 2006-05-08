// Compound.java
// by john sullivan, 2006.05
 
// TODO: get build.xml classpath working properly for ddl and hbm_xml rules


package edu.harvard.med.screensaver.beans.libraries;

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
  private Set<Well>   _wells;
	private String      _name;
	private String      _smiles;
  private boolean     _isSalt;
  private Set<String> _synonyms;
  private Set<String> _casNumbers;
  private Set<String> _nscNumbers;
  private String      _pubchemCid;
  private String      _chembankId;
	
  
  // getters and setters
  
	/**
	 * @return Returns the id.
   * 
	 * @hibernate.id
	 *   generator-class="native"
   *   column="compound_id"
   */
	public Integer getCompoundId() {
		return _compoundId;
	}
  
  /**
   * @return Returns the wells.
   *
   * @hibernate.set
   *   inverse="true"
   *   table="well_compound_link"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-many-to-many
   *   column="well_id"
   *   class="edu.harvard.med.screensaver.beans.libraries.Well"
   */
  public Set<Well> getWells() {
    return _wells;
  }

  /**
   * @param wells The wells to set.
   */
  public void setWells(Set<Well> wells) {
    _wells = wells;
  }

  /**
   * @return Returns the name.
   * 
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getName() {
    return _name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return Returns the smiles.
   * 
   * @hibernate.property
   *   type="text"
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
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="synonym"
   *   not-null="true"
   */
  public Set<String> getSynonyms() {
    return _synonyms;
  }

  /**
   * @param synonyms The synonyms to set.
   */
  public void setSynonyms(Set<String> synonyms) {
    _synonyms = synonyms;
  }

  /**
   * @return Returns the casNumber.
   * 
   * @hibernate.set
   *   order-by="csa_number"
   *   table="compound_cas_number"
   *   cascade="delete"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="cas_number"
   *   not-null="true"
   */
  public Set<String> getCasNumbers() {
    return _casNumbers;
  }

  /**
   * @param casNumber The casNumber to set.
   */
  public void setCasNumbers(Set<String> casNumber) {
    _casNumbers = casNumber;
  }

  /**
   * @return Returns the nscNumber.
   * 
   * @hibernate.set
   *   order-by="nsc_number"
   *   table="compound_nsc_number"
   *   cascade="delete"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-element
   *   type="text"
   *   column="nsc_number"
   *   not-null="true"
   */
  public Set<String> getNscNumbers() {
    return _nscNumbers;
  }

  /**
   * @param nscNumber The nscNumber to set.
   */
  public void setNscNumbers(Set<String> nscNumber) {
    _nscNumbers = nscNumber;
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
}
