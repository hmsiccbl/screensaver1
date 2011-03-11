// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Transient;

import com.google.common.collect.Sets;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


/**
 * Small Molecule reagent.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Immutable
@ContainedEntity(containingEntityClass=Well.class)
public class SmallMoleculeReagent extends Reagent
{
  private static final long serialVersionUID = 1L;

  public static final PropertyPath<SmallMoleculeReagent> compoundNames = RelationshipPath.from(SmallMoleculeReagent.class).toCollectionOfValues("compoundNames");
  public static final PropertyPath<SmallMoleculeReagent> pubchemCids = RelationshipPath.from(SmallMoleculeReagent.class).toCollectionOfValues("pubchemCids");
  public static final PropertyPath<SmallMoleculeReagent> chembankIds = RelationshipPath.from(SmallMoleculeReagent.class).toCollectionOfValues("chembankIds");
  public static final PropertyPath<SmallMoleculeReagent> molfileList = RelationshipPath.from(SmallMoleculeReagent.class).toCollectionOfValues("molfileList");
  
  public static final SmallMoleculeReagent NullSmallMoleculeReagent = 
    new SmallMoleculeReagent(ReagentVendorIdentifier.NULL_VENDOR_ID,
                             null,
                             null,
                             "",
                             "",
                             "",
                             null,
                             null,
                             new MolecularFormula(""));

  private List<String> _molfile = new ArrayList<String>();
  private String _smiles;
  private String _inchi;
  private BigDecimal _molecularMass;
  private BigDecimal _molecularWeight;
  private MolecularFormula _molecularFormula;
  private Set<String> _compoundNames = Sets.newHashSet();
  private Set<Integer> _pubchemCids = Sets.newHashSet();
  private Set<Integer> _chembankIds = Sets.newHashSet();

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected SmallMoleculeReagent() {}

  SmallMoleculeReagent(ReagentVendorIdentifier rvi,
                       Well well, 
                       LibraryContentsVersion libraryContentsVersion,                       
                       String molfile,
                       String smiles,
                       String inchi,
                       BigDecimal molecularMass,
                       BigDecimal molecularWeight,
                       MolecularFormula molecularFormula)
  {
    super(rvi, well, libraryContentsVersion);
    setMolfile(molfile);
    _smiles = smiles;
    _inchi = inchi;
    _molecularMass = molecularMass;
    _molecularWeight = molecularWeight;
    _molecularFormula = molecularFormula;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /* @Column(nullable=false) ICCB-L has legacy data where compound info was not known */
  @org.hibernate.annotations.Type(type="text")
  public String getSmiles()
  {
    return _smiles;
  }

  /* @Column(nullable=false) ICCB-L has legacy data where compound info was not known */
  @org.hibernate.annotations.Type(type="text")
  public String getInchi()
  {
    return _inchi;
  }

  @ElementCollection
  @edu.harvard.med.screensaver.model.annotations.ElementCollection(hasNonconventionalMutation = true)
  /* immutable, unless transient */
  @Column(name="compoundName", nullable=false)
  @JoinTable(
    name="smallMoleculeCompoundName",
    joinColumns=@JoinColumn(name="reagentId")
  )
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_small_molecule_compound_name_id_to_small_molecule_reagent")
  public Set<String> getCompoundNames()
  {
    return _compoundNames;
  }

  @Transient
  public int getNumCompoundNames()
  {
    return _compoundNames.size();
  }

  @ElementCollection
  @edu.harvard.med.screensaver.model.annotations.ElementCollection(hasNonconventionalMutation = true)
  /* immutable, unless transient */
  @Column(name="pubchemCid", nullable=false)
  @JoinTable(
    name="smallMoleculePubchemCid",
    joinColumns=@JoinColumn(name="reagentId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_small_molecule_pubchem_id_to_small_molecule_reagent")
  public Set<Integer> getPubchemCids()
  {
    return _pubchemCids;
  }

  @Transient
  public int getNumPubchemCids()
  {
    return _pubchemCids.size();
  }

  @ElementCollection
  @edu.harvard.med.screensaver.model.annotations.ElementCollection(hasNonconventionalMutation = true)
  /* immutable, unless transient */
  @Column(name="chembankId", nullable=false)
  @JoinTable(
    name="smallMoleculeChembankId",
    joinColumns=@JoinColumn(name="reagentId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_small_molecule_chembank_id_to_small_molecule_reagent")
  public Set<Integer> getChembankIds()
  {
    return _chembankIds;
  }

  @Transient
  public int getNumChembankIds()
  {
    return _chembankIds.size();
  }

  /**
   * @motivation we want lazy loading of molfile property, due to its large data
   *             size, but can only make it lazy loadable by mapping it in a
   *             value collection
   * @see #getMolfile()
   */
  @ElementCollection
  @JoinTable(name="molfile", joinColumns=@JoinColumn(name="reagent_id", unique=true)) // note "unique=true" ensures 1-to-1 mapping
  @Column(name="molfile", nullable=false)
  @IndexColumn(name="ordinal")
  @org.hibernate.annotations.Type(type="text")
  private List<String> getMolfileList()
  {
    return _molfile;
  }

  /**
   * @motivation we want lazy loading of molfile property, due to its large data
   *             size, but can only make it lazy loadable by mapping it in a
   *             value collection
   * @see #setMolfile(String)
   */
  private void setMolfileList(List<String> molfile)
  {
    _molfile = molfile;
  }

  @Transient
  public String getMolfile()
  {
    if (_molfile.size() == 0) {
      return null;
    }
    return _molfile.get(0);
  }

  public void setMolfile(String molfile)
  {
    _molfile.clear();
    if (molfile != null) {
      _molfile.add(molfile);
    }
  }

  @Column(precision=ScreensaverConstants.MOLECULAR_MASS_PRECISION,
          scale=ScreensaverConstants.MOLECULAR_MASS_SCALE)
  public BigDecimal getMolecularMass()
  {
    return _molecularMass;
  }

  private void setMolecularMass(BigDecimal molecularMass)
  {
    _molecularMass = molecularMass;
  }

  private void setMolecularWeight(BigDecimal value)
  {
    _molecularWeight = value;
  }

  @Column(precision=ScreensaverConstants.MOLECULAR_WEIGHT_PRECISION,
          scale=ScreensaverConstants.MOLECULAR_WEIGHT_SCALE)
  public BigDecimal getMolecularWeight()
  {
    return _molecularWeight;
  }

  @Type(type="text")
  public MolecularFormula getMolecularFormula()
  {
    return _molecularFormula;
  }

  private void setMolecularFormula(MolecularFormula molecularFormula)
  {
    _molecularFormula = molecularFormula;
  }

  /**
   * @motivation for hibernate
   */
  private void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * @motivation for hibernate
   */
  private void setInchi(String inchi)
  {
    _inchi = inchi;
  }

  /**
   * @motivation for hibernate
   */
  private void setCompoundNames(Set<String> compoundNames)
  {
    _compoundNames = compoundNames;
  }

  /**
   * @motivation for hibernate
   */
  private void setPubchemCids(Set<Integer> pubchemCids)
  {
    _pubchemCids = pubchemCids;
  }

  /**
   * @motivation for hibernate
   */
  private void setChembankIds(Set<Integer> chembankIds)
  {
    _chembankIds = chembankIds;
  }
}
