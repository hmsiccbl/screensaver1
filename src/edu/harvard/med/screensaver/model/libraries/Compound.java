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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.tools.MFAnalyser;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;


/**
 * A Hibernate entity bean representing a molecular compound.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class Compound extends SemanticIDAbstractEntity implements Comparable<Compound>
{

  // static fields

  private static Logger log = Logger.getLogger(Compound.class);
  private static final long serialVersionUID = 8777411947575574126L;


  // instance fields

 	private String _compoundId;
  private Integer _version;
  private Set<Well> _wells = new HashSet<Well>();
  private String _smiles;
  private String _inchi;
  private boolean _isSalt;
  private Set<String> _compoundNames = new HashSet<String>();
  private Set<String> _casNumbers = new HashSet<String>();
  private Set<String> _nscNumbers = new HashSet<String>();
  private Set<String> _pubchemCids = new HashSet<String>();
  private Set<String> _chembankIds = new HashSet<String>();

  /** used to compute molecular mass and molecular formula. */
  private MFAnalyser _mfAnalyser;


  // public constructors

  /**
   * Construct an initialized non-salt <code>Compound</code>.
   * @param smiles the SMILES string for the compound
   * @param inchi the InChI string for the compound
   */
  public Compound(String smiles, String inchi)
  {
    this(smiles, inchi, false);
  }

  /**
   * Construct an initialized <code>Compound</code>.
   * @param smiles the SMILES string for the compound
   * @param inchi the InChI string for the compound
   * @param isSalt the saltiness of the compound
   */
  public Compound(String smiles, String inchi, boolean isSalt)
  {
    _compoundId = smiles;
    _smiles = smiles;
    _inchi = inchi;
    _isSalt = isSalt;
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getEntityId()
  {
    return getCompoundId();
  }

  public int compareTo(Compound that)
  {
    int lengthCompare = that.getSmiles().length() - this.getSmiles().length();
    if (lengthCompare == 0) {
      return this.getSmiles().compareTo(that.getSmiles());
    }
    return lengthCompare;
  }

	/**
   * Get the compound id for the compound.
	 * @return the compound id for the compound
   */
  @Id
  @org.hibernate.annotations.Type(type="text")
	public String getCompoundId()
  {
		return _compoundId;
	}

  /**
  * Get the set of wells that contain this compound.
  * @return the set of wells that contain this compound
  */
  @ManyToMany(
    mappedBy="compounds",
    targetEntity=Well.class,
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.ForeignKey(name="fk_well_compound_link_to_compound")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  public Set<Well> getWells()
  {
    return _wells;
  }

  /**
   * Get the number of wells that contain this compound.
   * @return the number of wells that contain this compound
   */
  @Transient
  public int getNumWells()
  {
    return _wells.size();
  }

  /**
   * Add this compound to a well.
   * @param well the well to add this compound to
   * @return true iff the compound was not already contained in the well
   */
  public boolean addWell(Well well)
  {
    well.getCompounds().add(this);
    return _wells.add(well);
  }

  /**
   * Remove this compound from a well.
   * @param well the well to remove this compound from
   * @return true iff the compound was previously contained in the well
   */
  public boolean removeWell(Well well)
  {
    well.getCompounds().remove(this);
    return _wells.remove(well);
  }

  /**
   * Get the SMILES string for the compound.
   * @return the SMILES string for the compound
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false, unique=true)
  @org.hibernate.annotations.Type(type="text")
  public String getSmiles()
  {
    return _smiles;
  }

  /**
   * Get the InChI string for the compound.
   * @return the InChI string for the compound
   */
  // TODO: would like this to be unique, but there are currently 16 compounds with inchi = ''.
  // this happens when OpenBabel has problems parsing the SMILES.
  @org.hibernate.annotations.Immutable
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getInchi()
  {
    return _inchi;
  }

  /**
   * Get the saltiness of the compound.
   * @return true iff the compound is a salt
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false, name="isSalt")
  public boolean isSalt()
  {
    return _isSalt;
  }

  /**
   * Get the set of names for the compound.
   * @return the set of names for the compound
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="compoundName", nullable=false)
  @JoinTable(
    name="compoundCompoundName",
    joinColumns=@JoinColumn(name="compoundId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_compound_compound_name_to_compound")
  @OrderBy("compoundName")
  public Set<String> getCompoundNames()
  {
    return _compoundNames;
  }

  @Transient
  public int getNumCompoundNames()
  {
    return _compoundNames.size();
  }

  /**
   * Add a compound name for the compound.
   * @param compoundName the compound name to add to the compound
   * @return true iff the compound did not already have the compound name
   */
  public boolean addCompoundName(String compoundName)
  {
    return _compoundNames.add(compoundName);
  }

  /**
   * Remove a compound name from the compound.
   * @param compoundName the compound name to remove from the compound
   * @return true iff the compound previously had the compound name
   */
  public boolean removeCompoundName(String compoundName)
  {
    return _compoundNames.remove(compoundName);
  }

  /**
   * Get the set of CAS numbers for the compound.
   * @return the set of CAS numbers for the compound
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="casNumber", nullable=false)
  @JoinTable(
    name="compoundCasNumber",
    joinColumns=@JoinColumn(name="compoundId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_compound_cas_number_to_compound")
  @OrderBy("casNumber")
  public Set<String> getCasNumbers()
  {
    return _casNumbers;
  }

  /**
   * Get the number of CAS numbers for the compound.
   * @return the number of CAS numbers for the compound
   */
  @Transient
  public int getNumCasNumbers()
  {
    return _casNumbers.size();
  }

  /**
   * Add a CAS number to the compound.
   * @param casNumber the CAS number to add to the compound
   * @return true iff the compound did not already have the CAS number
   */
  public boolean addCasNumber(String casNumber)
  {
    return _casNumbers.add(casNumber);
  }

  /**
   * Remove a CAS number from the compound.
   * @param casNumber the CAS number to remove from the compound
   * @return true iff the compound previously had the CAS number
   */
  public boolean removeCasNumber(String casNumber)
  {
    return _casNumbers.remove(casNumber);
  }

  /**
   * Get the set of NSC numbers for the compound.
   * @return the set of NSC numbers for the compound
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="nscNumber", nullable=false)
  @JoinTable(
    name="compoundNscNumber",
    joinColumns=@JoinColumn(name="compoundId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_compound_nsc_number_to_compound")
  @OrderBy("nscNumber")
  public Set<String> getNscNumbers()
  {
    return _nscNumbers;
  }

  /**
   * Get the number of NSC numbers for the compound.
   * @return the number of NSC numbers for the compound
   */
  @Transient
  public int getNumNscNumbers()
  {
    return _nscNumbers.size();
  }

  /**
   * Add an NSC number to the compound.
   * @param nscNumber the NSC number to add to the compound
   * @return true iff the compound did not already have the NSC number
   */
  public boolean addNscNumber(String nscNumber)
  {
    return _nscNumbers.add(nscNumber);
  }

  /**
   * Remove an NSC number from the compound.
   * @param nscNumber the NSC number to remove from the compound
   * @return true iff the compound previously had the NSC number
   */
  public boolean removeNscNumber(String nscNumber)
  {
    return _nscNumbers.remove(nscNumber);
  }

  /**
   * Get the set of PubChem CIDs for the compound.
   * @return the set of PubChem CIDs for the compound
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="pubchemCid", nullable=false)
  @JoinTable(
    name="compoundPubchemCid",
    joinColumns=@JoinColumn(name="compoundId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_compound_pubchem_cid_to_compound")
  @OrderBy("pubchemCid")
  public Set<String> getPubchemCids()
  {
    return _pubchemCids;
  }

  /**
   * Get the number of PubChem CIDs for the compound.
   * @return the number of PubChem CIDs for the compound
   */
  @Transient
  public int getNumPubchemCids()
  {
    return _pubchemCids.size();
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
   * Get the set of ChemBank IDs for the compound.
   * @return the set of ChemBank IDs for the compound
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="chembankId", nullable=false)
  @JoinTable(
    name="compoundChembankId",
    joinColumns=@JoinColumn(name="compoundId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_compound_chembank_id_to_compound")
  @OrderBy("chembankId")
  public Set<String> getChembankIds()
  {
    return _chembankIds;
  }

  /**
   * Get the number of ChemBank IDs for the compound.
   * @return the number of ChemBank IDs for the compound
   */
  @Transient
  public int getNumChembankIds()
  {
    return _chembankIds.size();
  }

  /**
   * Add a ChemBank ID to the compound.
   * @param chembankId the ChemBank ID to add to the compound
   * @return true iff the compound did not already have the ChemBank ID
   */
  public boolean addChembankId(String chembankId)
  {
    return _chembankIds.add(chembankId);
  }

  /**
   * Remove a ChemBank ID from the compound.
   * @param chembankId the ChemBank ID to remove from the compound
   * @return true iff the compound previously had the ChemBank ID
   */
  public boolean removeChembankId(String chembankId)
  {
    return _chembankIds.remove(chembankId);
  }

  /**
   * Get the molecular mass of the compound, as computed by {@link MFAnalyser#getCanonicalMass()}.
   * @return the molecular mass of the compound
   */
  @Transient
  public float getMolecularMass()
  {
    MFAnalyser mfAnalyser = getMFAnalyser();
    try {
      if (mfAnalyser != null) {
        return mfAnalyser.getMass();
      }
      return -1;
    }
    catch (Exception e) {
      log.error("encountered Exception computing molecular mass!", e);
      return -1;
    }
  }

  /**
   * Get the (HTML-ized) molecular formula for the compound, as computed by {@link
   * MFAnalyser#getHTMLMolecularFormulaWithCharge()}.
   * @return the molecular formular for the compound
   */
  @Transient
  public String getMolecularFormula()
  {
    MFAnalyser mfAnalyser = getMFAnalyser();
    try {
      if (mfAnalyser != null) {
        return mfAnalyser.getHTMLMolecularFormulaWithCharge();
      }
      return null;
    }
    catch (Exception e) {
      log.error("encountered Exception computing molecular mass!", e);
      return null;
    }
  }


  // protected constructors

  /**
   * Construct an uninitialized <code>Compound</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Compound() {}


  // private constructors and instance methods

  /**
   * Set the compound id for the compound.
   * @param compoundId the new compound id for the compound
   * @motivation for hibernate
   */
  private void setCompoundId(String compoundId)
  {
    _compoundId = compoundId;
  }

  /**
   * Get the version number of the compound.
   * @return the version number of the compound
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version number of the compound.
   * @param version the new version number for the compound
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of wells that contain this compound.
   * @param wells the set of wells that contain this compound
   * @motivation for hibernate
   */
  private void setWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the SMILES string for the compound.
   * @param smiles the new SMILES string for the compound
   * @motivation for hibernate
   */
  private void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * Set the InChI string for the compound.
   * @param inchi the new InChI string for the compound
   * @motivation for hibernate
   */
  private void setInchi(String inchi)
  {
    _inchi = inchi;
  }

  /**
   * Set the saltiness of the compound.
   * @param isSalt the new saltiness for the compound
   * @motivation for hibernate
   */
  private void setSalt(boolean isSalt)
  {
    _isSalt = isSalt;
  }

  /**
   * Set the set of compound names for the compound.
   * @param compoundNames the new set of compound names for the compound
   * @motivation for hibernate
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

  /**
   * Set the set of ChemBank IDs for the compound.
   * @param chembankIds the new set of ChemBank IDs for the compound
   * @motivation for hibernate
   */
  private void setChembankIds(Set<String> chembankIds)
  {
    _chembankIds = chembankIds;
  }

  /**
   * Get the CDK MFAnalyser object for this compound. Create it if it does not already exist.
   * We use the CDK MFAnalyser to compute molecular weight and (HTML-ized) molecular formula.
   * @return the CDK MFAnalyser object for this compound
   * @see #getMolecularMass()
   * @see #getMolecularFormula()
   */
  @Transient
  private MFAnalyser getMFAnalyser()
  {
    if (_mfAnalyser != null) {
      return _mfAnalyser;
    }
    MoleculeSet moleculeSet = new MoleculeSet();
    SMILESReader smilesReader = new SMILESReader(new StringReader(_smiles));
    try {
      smilesReader.read(moleculeSet);
    }
    catch (CDKException e) {
      log.error("encountered Exception reading the SMILES!", e);
      return null;
    }
    IMolecule molecule = moleculeSet.getMolecule(0);
    if (molecule == null) {
      log.error("could not determine molecule for " + this);
      return null;
    }
    _mfAnalyser = new MFAnalyser(molecule);
    if (_mfAnalyser == null) {
      log.error("MFAnalyser creation failed");
    }
    return _mfAnalyser;
  }
}
