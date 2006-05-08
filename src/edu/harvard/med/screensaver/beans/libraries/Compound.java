// Compound.java
// by john sullivan, 2006.05

// TODO: implement synonyms
// TODO: implement well

// TODO: autogen hibernate.cfg.xml
// TODO: get build.xml classpath working porperly for ddl and hbm_xml rules


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
  
	private Integer              _compoundId;
	private String               _name;
	private String               _smiles;
  private boolean              _isSalt;
  private Set<CompoundSynonym> _synonyms;
  private String               _pubchemCid;
  private String               _chembankId;
	
  
  // getters and setters
  
	/**
	 * @return Returns the id.
	 * @hibernate.id
	 *   generator-class="native"
   *   column="compound_id"
   */
	public Integer getCompoundId() {
		return _compoundId;
	}
  
  /**
   * @return Returns the name.
   * @hibernate.property
   *   type="text"
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
   * @hibernate.property
   *   column="is_salt"
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
   * commenting out these XDoclet attributes for now so the build works:
   * (at)hibernate.set
   *   inverse="false"
   *   order-by="synonym"
   * (at)hibernate.collection-key
   *   column="compound_id"
   */
  public Set<CompoundSynonym> getSynonyms() {
    return _synonyms;
  }

  /**
   * @param synonyms The synonyms to set.
   */
  public void setSynonyms(Set<CompoundSynonym> synonyms) {
    _synonyms = synonyms;
  }

  /**
   * @return Returns the pubchemCid.
   * @hibernate.property
   *   column="pubchem_id"
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
   *   column="chembank_id"
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
