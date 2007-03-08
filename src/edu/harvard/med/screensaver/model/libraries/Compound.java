// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openscience.cdk.SetOfMolecules;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.tools.MFAnalyser;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.EntityIdProperty;


/**
 * A Hibernate entity bean representing a molecular compound.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * 
 * @hibernate.class
 *   lazy="false"
 */
public class Compound extends AbstractEntity
{
  
  // static fields

  private static Logger log = Logger.getLogger(Compound.class);
  private static final long serialVersionUID = 8777411947575574126L;

  
  // instance fields
  
	private String      _compoundId;
  private Integer     _version;
  private Set<Well>   _wells = new HashSet<Well>();
  private String      _smiles;
  private String      _inchi;
  private boolean     _isSalt;
  private Set<String> _compoundNames = new HashSet<String>();
  private Set<String> _casNumbers = new HashSet<String>();
  private Set<String> _nscNumbers = new HashSet<String>();
  private Set<String> _pubchemCids = new HashSet<String>();
  private String      _chembankId;
  
  /** used to compute molecular mass and molecular formula. */
  private MFAnalyser _mfAnalyser;
  
  
  // public constructors
  
  /**
   * Constructs an initialized <code>Compound</code> object.
   * @param smiles
   */
  public Compound(String smiles)
  {
    this(smiles, false);
  }
  
  /**
   * Constructs an initialized <code>Compound</code> object.
   * @param smiles
   * @param isSalt
   */
  public Compound(String smiles, boolean isSalt)
  {
    _smiles = smiles;
    _isSalt = isSalt;
  }
  
  
  // public methods

  @Override
  public String getEntityId()
  {
    return getBusinessKey().toString();
  }

	/**
   * Get the compound id for the compound.
	 * @return the compound id for the compound
   * 
	 * @hibernate.id
   *   generator-class="assigned"
   *   length="2047"
   */
	public String getCompoundId()
  {
		return getBusinessKey().toString();
	}
  
  @DerivedEntityProperty
  public int getNumWells()
  {
    return _wells.size();
  }
  
  /**
  * Get an unmodifiable copy of the set of wells that contain this compound.
  * 
  * @return an unmodifiable copy of the set of wells that contain this compound
  */
  public Set<Well> getWells()
  {
    return Collections.unmodifiableSet(_wells);
  }

  /**
   * Add this compound to a well.
   * @param well the well to add this compound to
   * @return     true iff the compound was not already contained in the well
   */
  public boolean addWell(Well well)
  {
    assert !(well.getHbnCompounds().contains(this) ^
      getHbnWells().contains(this)) :
      "asymmetric compound/well association encountered";
    if (getHbnWells().add(well)) {
      return well.getHbnCompounds().add(this);
    }
    return false;
  }
  
  /**
   * Remove this compound from a well.
   * @param well the well to remove this compound from
   * @return     true iff the compound was previously contained in the well
   */
  public boolean removeWell(Well well)
  {
    assert !(well.getHbnCompounds().contains(this) ^
      getHbnWells().contains(this)) :
      "asymmetric compound/well association encountered";
    if (getHbnWells().remove(well)) {
      return well.getHbnCompounds().remove(this);
    }
    return false;
  }

  /**
   * Get the SMILES string for the compound.
   * @return the SMILES string for the compound
   * 
   * @hibernate.property
   *   type="text"
   *   column="smiles"
   *   not-null="true"
   *   unique="true"
   * @motivation for hibernate
   */
  @EntityIdProperty
  public String getSmiles()
  {
    return _smiles;
  }

  /**
   * Get the InChI string for the compound.
   * @return the InChI string for the compound
   * 
   * @hibernate.property
   *   type="text"
   *   column="inchi"
   * @motivation for hibernate
   */
  public String getInchi()
  {
    return _inchi;
  }

  /**
   * Set the InChI string for the compound.
   * @param inchi the new InChI string for the compound
   */
  public void setInchi(String inchi)
  {
    _inchi = inchi;
  }
  
  /**
   * Get the saltiness of the compound.
   * @return true iff the compound is a salt
   * 
   * @hibernate.property
   *   column="is_salt"
   *   not-null="true"
   */
  public boolean isSalt() {
    return _isSalt;
  }

  /**
   * Set the saltiness of the compound.
   * @param isSalt the new saltiness for the compound
   */
  public void setSalt(boolean isSalt)
  {
    _isSalt = isSalt;
  }

  @DerivedEntityProperty
  public int getNumCompoundNames()
  {
    return _compoundNames.size();
  }
  
  /**
   * Get the set of names for the compound.
   * @return the set of names for the compound
   *
   * @hibernate.set
   *   order-by="compound_name"
   *   table="compound_compound_name"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   *   foreign-key="fk_compound_compound_name_to_compound"
   * @hibernate.collection-element
   *   type="text"
   *   column="compound_name"
   *   not-null="true"
   */
  public Set<String> getCompoundNames()
  {
    return _compoundNames;
  }

  /**
   * Add a compound name for the compound.
   * @param compoundName the compound name to add to the compound
   * @return        true iff the compound did not already have the compound name
   */
  public boolean addCompoundName(String compoundName)
  {
    return _compoundNames.add(compoundName);
  }

  /**
   * Remove a compound name from the compound.
   * @param compoundName the compound name to remove from the compound
   * @return        true iff the compound previously had the compound name
   */
  public boolean removeCompoundName(String compoundName)
  {
    return _compoundNames.remove(compoundName);
  }

  @DerivedEntityProperty
  public int getNumCasNumbers()
  {
    return _casNumbers.size();
  }
  
  /**
   * Get the set of CAS numbers for the compound.
   * @return the set of CAS numbers for the compound
   * 
   * @hibernate.set
   *   order-by="cas_number"
   *   table="compound_cas_number"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   *   foreign-key="fk_compound_cas_number_to_compound"
   * @hibernate.collection-element
   *   type="text"
   *   column="cas_number"
   *   not-null="true"
   */
  public Set<String> getCasNumbers()
  {
    return _casNumbers;
  }

  /**
   * Add a CAS number to the compound.
   * @param casNumber the CAS number to add to the compound
   * @return          true iff the compound did not already have the CAS number
   */
  public boolean addCasNumber(String casNumber)
  {
    return _casNumbers.add(casNumber);
  }

  /**
   * Remove a CAS number from the compound.
   * @param casNumber the CAS number to remove from the compound
   * @return          true iff the compound previously had the CAS number
   */
  public boolean removeCasNumber(String casNumber)
  {
    return _casNumbers.remove(casNumber);
  }

  @DerivedEntityProperty
  public int getNumNscNumbers()
  {
    return _nscNumbers.size();
  }
  
  /**
   * Get the set of NSC numbers for the compound.
   * @return the set of NSC numbers for the compound
   * 
   * @hibernate.set
   *   order-by="nsc_number"
   *   table="compound_nsc_number"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   *   foreign-key="fk_compound_nsc_number_to_compound"
   * @hibernate.collection-element
   *   type="text"
   *   column="nsc_number"
   *   not-null="true"
   */
  public Set<String> getNscNumbers()
  {
    return _nscNumbers;
  }

  /**
   * Add an NSC number to the compound.
   * @param nscNumber the NSC number to add to the compound
   * @return          true iff the compound did not already have the NSC number
   */
  public boolean addNscNumber(String nscNumber)
  {
    return _nscNumbers.add(nscNumber);
  }
  
  /**
   * Remove an NSC number from the compound.
   * @param nscNumber the NSC number to remove from the compound
   * @return          true iff the compound previously had the NSC number
   */
  public boolean removeNscNumber(String nscNumber)
  {
    return _nscNumbers.remove(nscNumber);
  }

  @DerivedEntityProperty
  public int getNumPubchemCids()
  {
    return _pubchemCids.size();
  }
  
  /**
   * Get the set of PubChem CIDs for the compound.
   * @return the set of PubChem CIDs for the compound
   * 
   * @hibernate.set
   *   order-by="pubchem_cid"
   *   table="compound_pubchem_cid"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   *   foreign-key="fk_compound_pubchem_cid_to_compound"
   * @hibernate.collection-element
   *   type="text"
   *   column="pubchem_cid"
   *   not-null="true"
   */
  public Set<String> getPubchemCids()
  {
    return _pubchemCids;
  }

  /**
   * Add a PubChem CID to the compound.
   * @param pubchemCid the PubChem CID to add to the compound
   * @return true iff the compound did not already have the PubChem CID
   */
  public boolean addPubchemCid(String pubchemCid)
  {
    return _pubchemCids.add(pubchemCid);
  }
  
  /**
   * Remove a PubChem CID from the compound.
   * @param pubchemCid the PubChem CID to remove from the compound
   * @return true iff the compound previously had the PubChem CID
   */
  public boolean removePubchemCid(String pubchemCid)
  {
    return _pubchemCids.remove(pubchemCid);
  }

  /**
   * Get the ChemBank ID for the compound.
   * @return the ChemBank ID for the compound
   * 
   * @hibernate.property
   *   type="text"
   */
  public String getChembankId()
  {
    return _chembankId;
  }
  
  /**
   * Set the ChemBank ID for the compound.
   * @param chembankId the new ChemBank ID for the compound
   */
  public void setChembankId(String chembankId)
  {
    _chembankId = chembankId;
  }
  
  /**
   * Get the molecular mass of the compound, as computed by {@link MFAnalyser#getCanonicalMass()}.
   * @return the molecular mass of the compound
   */
  @DerivedEntityProperty
  public float getMolecularMass()
  {
    MFAnalyser mfAnalyser = getMFAnalyser();
    try {
      return mfAnalyser.getCanonicalMass();
    }
    catch (Exception e) {
      log.error("encountered Exception computing molecular mass!", e);
      return -1;
    }
  }
  
  /**
   * Get the molecular formula for the compound, as computed by {@link
   * MFAnalyser#getHTMLMolecularFormulaWithCharge()}.
   * @return the molecular formular for the compound
   */
  @DerivedEntityProperty
  public String getMolecularFormula()
  {
    MFAnalyser mfAnalyser = getMFAnalyser();
    try {
      return mfAnalyser.getHTMLMolecularFormulaWithCharge();
    }
    catch (Exception e) {
      log.error("encountered Exception computing molecular mass!", e);
      return null;
    }
  }

  
  // protected getters and setters
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.model.AbstractEntity#getBusinessKey()
   */
  protected Object getBusinessKey()
  {
    return getSmiles();
  }

  
  // package getters and setters

  /**
   * Get the set of wells that contain this compound. If the caller modifies the
   * returned collection, it must ensure that the bi-directional relationship is
   * maintained by updating the related {@link Well} bean(s).
   * 
   * @motivation for Hibernate and for associated {@link Well} bean (so that it
   *             can maintain the bi-directional association between
   *             {@link Well} and {@link Compound}).
   * @return the set of wells that contain this compound
   * @hibernate.set
   *   order-by="well_id"
   *   inverse="true"
   *   table="well_compound_link"
   *   cascade="all"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="compound_id"
   * @hibernate.collection-many-to-many
   *   column="well_id"
   *   class="edu.harvard.med.screensaver.model.libraries.Well"
   *   foreign-key="fk_well_compound_link_to_compound"
   */
  Set<Well> getHbnWells()
  {
    return _wells;
  }
  
  
  // private constructors and instance methods

  /**
   * Constructs an uninitialized Compound object.
   * @motivation for hibernate
   */
  private Compound() {}
  
  /**
   * Set the compound id for the compound.
   * @param compoundId the new compound id for the compound
   * @motivation       for hibernate
   */
  private void setCompoundId(String compoundId)
  {
    _compoundId = compoundId;
  }

  /**
   * Get the version number of the compound.
   * @return     the version number of the compound
   * @motivation for hibernate
   *
   * @hibernate.version
   */
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version number of the compound.
   * @param version the new version number for the compound
   * @motivation    for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the SMILES string for the compound.
   * @param smiles the new SMILES string for the compound
   */
  private void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * Set the set of wells that contain this compound.
   * @param wells the new set of wells that contain this compound
   * @motivation  for hibernate
   */
  private void setHbnWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the set of compound names for the compound.
   * @param compoundNames the new set of compound names for the compound
   * @motivation     for hibernate
   */
  private void setCompoundNames(Set<String> compoundNames)
  {
    _compoundNames = compoundNames;
  }

  /**
   * Set the set of CAS numbers for the compound.
   * @param casNumbers the new set of CAS numbers for the compound
   * @motivation for hibernate
   */
  private void setCasNumbers(Set<String> casNumbers)
  {
    _casNumbers = casNumbers;
  }

  /**
   * Set the set of NSC numbers for the compound.
   * @param nscNumbers the new set of NSC numbers for the compound
   * @motivation for hibernate
   */
  private void setNscNumbers(Set<String> nscNumbers)
  {
    _nscNumbers = nscNumbers;
  }

  /**
   * Set the set of PubChem CIDs for the compound.
   * @param pubchemCids the new set of PubChem CIDs for the compound
   * @motivation for hibernate
   */
  private void setPubchemCids(Set<String> pubchemCids)
  {
    _pubchemCids = pubchemCids;
  }
  
  private MFAnalyser getMFAnalyser()
  {
    if (_mfAnalyser != null) {
      return _mfAnalyser;
    }
    SetOfMolecules setOfMolecules = new SetOfMolecules();
    SMILESReader smilesReader = new SMILESReader(new StringReader(_smiles));
    try {
      smilesReader.read(setOfMolecules);
    }
    catch (CDKException e) {
      log.error("encountered Exception reading the SMILES!", e);
      return null;
    }
    IMolecule molecule = setOfMolecules.getMolecule(0);
    _mfAnalyser = new MFAnalyser(molecule);
    return _mfAnalyser;
  }
}
