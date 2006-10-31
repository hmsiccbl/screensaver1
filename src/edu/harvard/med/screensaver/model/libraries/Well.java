// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/libraries/Well.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.EntityIdProperty;
import edu.harvard.med.screensaver.model.screens.CherryPick;


/**
 * A Hibernate entity bean representing a well.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false"
 */
public class Well extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Well.class);
  private static final long serialVersionUID = 2682270079212906959L;
  private static Pattern _wellParsePattern = Pattern.compile("([A-Za-z])(\\d{1,2})");


  // instance fields

  private String _wellId;
  private Integer _version;
  private Library _library;
  private Set<Compound> _compounds = new HashSet<Compound>();
  private Set<SilencingReagent> _silencingReagents = new HashSet<SilencingReagent>();
  private Integer _plateNumber;
  private String _wellName;
  private String _iccbNumber;
  private String _vendorIdentifier;
  private WellType _wellType = WellType.EXPERIMENTAL;
  private Set<CherryPick> _cherryPicks = new HashSet<CherryPick>();
  private String _smiles;
  private String _molfile;
  private String _genbankAccessionNumber;
  private char _rowLetter;
  private int _column;


  // public constructors and instance methods

  /**
   * Constructs an initialized <code>Well</code> object.
   * 
   * @param parentLibrary
   * @param plateNumber
   * @param wellName
   */
  public Well(Library parentLibrary, Integer plateNumber, String wellName, WellType wellType) {
    _plateNumber = plateNumber;
    setWellName(wellName);
    _wellType = wellType;
    // this call must occur after assignments of wellName and plateNumber (to
    // ensure hashCode() works)
    setLibrary(parentLibrary);
  }

  /**
   * Constructs an initialized <code>Well</code> object.
   * 
   * @param parentLibrary
   * @param plateNumber
   * @param wellName
   */
  public Well(Library parentLibrary, Integer plateNumber, String wellName)
  {
    this(parentLibrary, plateNumber, wellName, WellType.EXPERIMENTAL);
  }

  @Override
  public String getEntityId()
  {
    return getBusinessKey().toString();
  }
  
  /**
   * Get the well id for the well.
   * 
   * @return the well id for the well
   * @hibernate.id
   *   generator-class="assigned"
   */
  public String getWellId()
  {
    return getBusinessKey().toString();
  }

  /**
   * Get the library the well is in.
   * 
   * @return the library the well is in.
   */
  public Library getLibrary()
  {
    return getHbnLibrary();
  }

  /**
   * Set the library the well is in. Throw a NullPointerException if the library
   * is null.
   * 
   * @param library the new library for the well
   * @throws NullPointerException when the library is null
   */
  public void setLibrary(Library library)
  {
    assert _wellName != null && _plateNumber != null : "properties forming business key have not been defined";
    if (_library != null) {
      _library.getHbnWells().remove(this);
    }
    setHbnLibrary(library);
    library.getHbnWells().add(this);
  }

  /**
   * Get an unmodifiable copy of the set of compounds.
   * 
   * @return an unmodifiable copy of the set of compounds
   */
  public Set<Compound> getCompounds()
  {
    return Collections.unmodifiableSet(getHbnCompounds());
  }

  /**
   * Get the primary compound: the compound that is most likely the one being tested for
   * bioactivity. Normally, we expect a single potentially bioactive
   * compound, plus salts and other solvents, in a compound well. (But be careful, because
   * sometimes the experimental compound is also a salt!) As an approximation, take the
   * compound with the longest smiles.
   * @return the primary compound
   */
  @DerivedEntityProperty
  public Compound getPrimaryCompound()
  {
    Compound compoundWithLongestSmiles = null;
    for (Compound compound : getHbnCompounds()) {
      if (
        compoundWithLongestSmiles == null ||
        compound.getSmiles().length() > compoundWithLongestSmiles.getSmiles().length()) {
        compoundWithLongestSmiles = compound;
      }
    }
    return compoundWithLongestSmiles; 
  }

  /**
   * Add the compound.
   * 
   * @param compound the compound to add
   * @return true iff the compound was not already in the well
   */
  public boolean addCompound(Compound compound)
  {
    assert !(getHbnCompounds().contains(compound) ^ compound.getHbnWells()
      .contains(this)) : "asymmetric compound/well association encountered";
    if (getHbnCompounds().add(compound)) {
      return compound.getHbnWells().add(this);
    }
    return false;
  }

  /**
   * Remove the compound.
   * 
   * @param compound the compound to remove
   * @return true iff the compound was previously in the well
   */
  public boolean removeCompound(Compound compound)
  {
    assert !(getHbnCompounds().contains(compound) ^ compound.getHbnWells()
      .contains(this)) : "asymmetric compound/well association encountered";
    if (getHbnCompounds().remove(compound)) {
      return compound.getHbnWells().remove(this);
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of silencing reagents.
   * 
   * @return an unmodifiable copy of the set of silencing reagents
   */
  public Set<SilencingReagent> getSilencingReagents()
  {
    return Collections.unmodifiableSet(getHbnSilencingReagents());
  }

  /**
   * Get the set of genes that have silencing reagents contained in this well.
   * @return the set of genes that have silencing reagents contained in this well
   */
  @DerivedEntityProperty
  public Set<Gene> getGenes()
  {
    Set<Gene> genes = new HashSet<Gene>();
    for (SilencingReagent silencingReagent : getSilencingReagents()) {
      genes.add(silencingReagent.getGene());
    }
    return genes;
  }
  
  /**
   * Add the silencing reagent.
   * 
   * @param silencingReagent the silencing reagent to add
   * @return true iff the silencing reagent was not already in the well
   */
  public boolean addSilencingReagent(SilencingReagent silencingReagent)
  {
    assert !(getHbnSilencingReagents().contains(silencingReagent) ^
      silencingReagent.getHbnWells().contains(this)) :
        "asymmetric compound/well association encountered";
    if (getHbnSilencingReagents().add(silencingReagent)) {
      return silencingReagent.getHbnWells().add(this);
    }
    return false;
  }

  /**
   * Remove the silencing reagent.
   * 
   * @param silencingReagent the silencing reagent to remove
   * @return true iff the compound was previously in the well
   */
  public boolean removeSilencingReagent(SilencingReagent silencingReagent)
  {
    assert ! (getHbnSilencingReagents().contains(silencingReagent) ^
      silencingReagent.getHbnWells().contains(this)) :
        "asymmetric compound/well association encountered";
    if (getHbnSilencingReagents().remove(silencingReagent)) {
      return silencingReagent.getHbnWells().remove(this);
    }
    return false;
  }

  /**
   * Get the plate number for the well.
   * 
   * @return the plate number for the well
   * @hibernate.property not-null="true"
   */
  @EntityIdProperty
  public Integer getPlateNumber()
  {
    return _plateNumber;
  }

  /**
   * Set the plate number for the well.
   * 
   * @param plateNumber the new plate number for the well
   */
  private void setPlateNumber(Integer plateNumber)
  {
    _plateNumber = plateNumber;
  }

  /**
   * Get the well name for the well.
   * 
   * @return the well name for the well
   * @hibernate.property type="text" not-null="true"
   */
  @EntityIdProperty
  public String getWellName()
  {
    return _wellName;
  }

  /**
   * Set the well name for the well.
   * 
   * @param wellName the new well name for the well
   */
  private void setWellName(String wellName)
  {
    _wellName = wellName;
    Matcher matcher = _wellParsePattern.matcher(wellName);
    if (matcher.matches()) {
      _rowLetter = matcher.group(1).toUpperCase().charAt(0);
      _column = Integer.parseInt(matcher.group(2)) - 1;
      if (_column < 0 || _column > 23) {
        throw new IllegalArgumentException("well column is illegal");
      }
    } 
    else {
      throw new IllegalArgumentException("well name is illegal");
    }
  }

  /**
   * Get the ICCB number for the well.
   * 
   * @return the ICCB number for the well
   * @hibernate.property type="text"
   */
  public String getIccbNumber()
  {
    return _iccbNumber;
  }

  /**
   * Set the ICCB number for the well.
   * 
   * @param iccbNumber The new ICCB number for the well
   */
  public void setIccbNumber(String iccbNumber)
  {
    _iccbNumber = iccbNumber;
  }

  /**
   * Get the vendor identifier for the well.
   * 
   * @return the vendor identifier for the well
   * @hibernate.property type="text"
   */
  public String getVendorIdentifier()
  {
    return _vendorIdentifier;
  }

  /**
   * Set the vendor identifier for the well.
   * 
   * @param vendorIdentifier the new vendor identifier for the well
   */
  public void setVendorIdentifier(String vendorIdentifier)
  {
    _vendorIdentifier = vendorIdentifier;
  }
  
  /**
   * Get the well's type.
   * 
   * @return the well's type
   * @hibernate.property type="edu.harvard.med.screensaver.model.libraries.WellType$UserType"
   *                     not-null="true"
   */
  public WellType getWellType()
  {
    return _wellType;
  }

  /**
   * Set the well's type.
   * 
   * @param wellType the new type of the well
   */
  public void setWellType(WellType wellType)
  {
    _wellType = wellType;
  }
  
  /**
   * Get the SMILES for the well.
   * 
   * @return the SMILES for the well
   * @hibernate.property type="text"
   */
  public String getSmiles()
  {
    return _smiles;
  }

  /**
   * Set the SMILES for the well.
   * 
   * @param smiles The new SMILES for the well
   */
  public void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * Get the molfile for the well.
   * 
   * @return the molfile for the well
   * @hibernate.property type="text"
   */
  public String getMolfile()
  {
    return _molfile;
  }

  /**
   * Set the molfile for the well.
   * 
   * @param molfile The new molfile for the well
   */
  public void setMolfile(String molfile)
  {
    _molfile = molfile;
  }
  
  /**
   * Get the GenBank Accession number.
   * 
   * @return the GenBank Accession number
   * @hibernate.property type="text"
   */
  public String getGenbankAccessionNumber()
  {
    return _genbankAccessionNumber;
  }

  /**
   * Set the GenBank Accession number.
   * 
   * @param genbankAccessionNumber the GenBank Accession number
   */
  public void setGenbankAccessionNumber(String genbankAccessionNumber)
  {
    _genbankAccessionNumber = genbankAccessionNumber;
  }
  
  
  // public hibernate methods for cross-package relationships
  

  /**
   * Get an unmodifiable copy of the set of cherry picks.
   *
   * @return the cherry picks
   */
  public Set<CherryPick> getCherryPicks()
  {
    return Collections.unmodifiableSet(_cherryPicks);
  }

  /**
   * Add the cherry pick.
   *
   * @param cherryPick the cherry pick to add
   * @return true iff the well did not already have the cherry pick
   */
  public boolean addCherryPick(CherryPick cherryPick)
  {
    if (getHbnCherryPicks().add(cherryPick)) {
      cherryPick.setHbnWell(this);
      return true;
    }
    return false;
  }
  
  /**
   * Get the cherry picks.
   *
   * @return the cherry picks
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="well_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  public Set<CherryPick> getHbnCherryPicks()
  {
    return _cherryPicks;
  }
  
  /**
   * Get the <i>zero-based</i> row index of this well.
   * @return the <i>zero-based</i> row index of this well
   */
  @DerivedEntityProperty
  public int getRow()
  {
    return _rowLetter - 'A';
  }
  
  @DerivedEntityProperty
  /**
   * Get the row letter of this well.
   * @return the row letter of this well
   */
  public char getRowLetter()
  {
    return _rowLetter;
  }

  /**
   * Get the <i>zero-based</i> column index of this well.
   * @return the <i>zero-based</i> column index of this well
   */
  @DerivedEntityProperty
  public int getColumn()
  {
    return _column;
  }
  
  @DerivedEntityProperty
  public boolean isEdgeWell()
  {
    // TODO: use plate size/layout to determine this dynamically
    return _rowLetter == 'A' || _rowLetter == 'P' || 
    _column == 0 || _column == 23;
  }
  
  /**
   * Write the well contents out as an SD file record to the print writer.
   * @param sdFilePrintWriter the SD file print writer
   */
  public void writeToSDFile(PrintWriter sdFilePrintWriter)
  {
    if (_molfile == null) {
      return;
    }
    sdFilePrintWriter.println(_molfile);
    sdFilePrintWriter.println(">  <Library>");
    sdFilePrintWriter.println(getLibrary().getLibraryName());
    sdFilePrintWriter.println();
    sdFilePrintWriter.println(">  <Plate>");
    sdFilePrintWriter.println(getPlateNumber());
    sdFilePrintWriter.println();
    sdFilePrintWriter.println(">  <Well>");
    sdFilePrintWriter.println(getWellName());
    sdFilePrintWriter.println();
    sdFilePrintWriter.println(">  <Plate_Well>");
    sdFilePrintWriter.println(getPlateNumber() + getWellName());
    sdFilePrintWriter.println();
    sdFilePrintWriter.println(">  <Well_Type>");
    sdFilePrintWriter.println(getWellType());
    sdFilePrintWriter.println();
    sdFilePrintWriter.println(">  <Smiles>");
    sdFilePrintWriter.println(getSmiles());
    sdFilePrintWriter.println();
    if (getIccbNumber() != null) {
      sdFilePrintWriter.println(">  <ICCB_Number>");
      sdFilePrintWriter.println(getIccbNumber());
      sdFilePrintWriter.println();
    }
    if (getVendorIdentifier() != null) {
      sdFilePrintWriter.println(">  <Vendor_Identifier>");
      if (getLibrary().getVendor() != null) {
        sdFilePrintWriter.println(getLibrary().getVendor() + " " + getVendorIdentifier());
      }
      else {
        sdFilePrintWriter.println(getVendorIdentifier());
      }
      sdFilePrintWriter.println();
    }
    Compound compound = getPrimaryCompound();
    if (compound != null) {
      for (String compoundName : compound.getCompoundNames()) {
        sdFilePrintWriter.println(">  <Compound_Name>");
        sdFilePrintWriter.println(compoundName);
        sdFilePrintWriter.println();
      }
      for (String casNumber : compound.getCasNumbers()) {
        sdFilePrintWriter.println(">  <CAS_Number>");
        sdFilePrintWriter.println(casNumber);
        sdFilePrintWriter.println();
      }
      for (String nscNumber : compound.getNscNumbers()) {
        sdFilePrintWriter.println(">  <NSC_Number>");
        sdFilePrintWriter.println(nscNumber);
        sdFilePrintWriter.println();
      }
      if (compound.getPubchemCid() != null) {
        sdFilePrintWriter.println(">  <PubChem_CID>");
        sdFilePrintWriter.println(compound.getPubchemCid());
        sdFilePrintWriter.println();        
      }
      if (compound.getChembankId() != null) {
        sdFilePrintWriter.println(">  <ChemBank_ID>");
        sdFilePrintWriter.println(compound.getChembankId());
        sdFilePrintWriter.println();        
      }
    }
    sdFilePrintWriter.println("$$$$");
  }

  

  // protected getters and setters

  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    /**
     * Get the plate number for the well.
     * @return the plate number for the well
     */
    private Integer getPlateNumber()
    {
      return _plateNumber;
    }
    
    /**
     * Get the well name.
     * @return the well name
     */
    private String getWellName()
    {
      return _wellName;
    }
    
    @Override
    public boolean equals(Object object)
    {
      if (!(object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return getPlateNumber().equals(that.getPlateNumber()) && getWellName().equals(
        that.getWellName());
    }

    @Override
    public int hashCode()
    {
      assert _plateNumber != null && _wellName != null : "business key fields have not been defined";
      return getPlateNumber().hashCode() + getWellName().hashCode();
    }

    @Override
    public String toString()
    {
      assert _plateNumber != null && _wellName != null : "business key fields have not been defined";
      return getPlateNumber() + ":" + getWellName();
    }
  }

  // package getters and setters

  /**
   * Set the library the well is in. Throw a NullPointerException when the library
   * is null.
   * 
   * @param library the new library for the well
   * @throws NullPointerException when the library is null
   * @motivation for hibernate
   */
  void setHbnLibrary(Library library)
  {
    if (library == null) {
      throw new NullPointerException();
    }
    _library = library;
  }

  /**
   * Get the modifiable set of compounds contained in the well. If the caller
   * modifies the returned collection, it must ensure that the bi-directional
   * relationship is maintained by updating the related {@link Compound}
   * bean(s).
   * 
   * @return the set of compounds contained in the well
   * @motivation for Hibernate and for associated {@link Compound} bean (so that
   *             it can maintain the bi-directional association between
   *             {@link Compound} and {@link Well}).
   * @hibernate.set
   *   table="well_compound_link"
   *   cascade="all"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="well_id"
   * @hibernate.collection-many-to-many
   *   column="compound_id"
   *   class="edu.harvard.med.screensaver.model.libraries.Compound"
   *   foreign-key="fk_well_compound_link_to_well"
   */
  Set<Compound> getHbnCompounds()
  {
    return _compounds;
  }

  /**
   * Get the silencing reagents.
   * 
   * @return the silencing reagents
   * @hibernate.set
   *   table="well_silencing_reagent_link"
   *   cascade="all"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="well_id"
   * @hibernate.collection-many-to-many
   *   column="silencing_reagent_id"
   *   class="edu.harvard.med.screensaver.model.libraries.SilencingReagent"
   *   foreign-key="fk_well_silencing_reagent_link_to_well"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<SilencingReagent> getHbnSilencingReagents()
  {
    return _silencingReagents;
  }

  
  // private methods
  
  /**
   * Constructs an uninitialized Well object.
   * 
   * @motivation for hibernate
   */
  private Well() {}

  /**
   * Set the well id for the well.
   * 
   * @param wellId the new well id for the well
   * @motivation for hibernate
   */
  private void setWellId(String wellId)
  {
    _wellId = wellId;
  }

  /**
   * Get the version of the well.
   * 
   * @return the version of the well
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() 
  {
    return _version;
  }

  /**
   * Set the version of the well.
   * 
   * @param version the new version of the well
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Get the library the well is in.
   * 
   * @return the library the well is in
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Library"
   *   column="library_id"
   *   not-null="true"
   *   foreign-key="fk_well_to_library"
   *   cascade="save-update"
   *   lazy="true"
   */
  private Library getHbnLibrary()
  {
    return _library;
  }

  /**
   * Set the set of compounds contained in the well.
   * 
   * @param compounds the new set of compounds contained in the well
   * @motivation for hibernate
   */
  private void setHbnCompounds(Set<Compound> compounds)
  {
    _compounds = compounds;
  }
  
  /**
   * Set the silencing reagents.
   * 
   * @param silencingReagents the new silencing reagents
   * @motivation for hibernate
   */
  private void setHbnSilencingReagents(Set<SilencingReagent> silencingReagents)
  {
    _silencingReagents = silencingReagents;
  }
  
  /**
   * Set the cherry picks.
   *
   * @param cherryPicks the new cherry picks
   * @motivation for hibernate
   */
  private void setHbnCherryPicks(Set<CherryPick> cherryPicks)
  {
    _cherryPicks = cherryPicks;
  }
}
