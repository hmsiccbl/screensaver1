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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.EntityIdProperty;


/**
 * A Hibernate entity bean representing a well.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false"
 */
public class Well extends AbstractEntity implements Comparable
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Well.class);
  private static final long serialVersionUID = 2682270079212906959L;
  private static Pattern _wellParsePattern = Pattern.compile("([A-Za-z])(\\d{1,2})");

  // constants for well names
  // note that these can be hardcoded for now, since we only support 384 well plates
  public static final int MIN_WELL_COLUMN = 1;
  public static final int MAX_WELL_COLUMN = 24;
  public static final char MIN_WELL_ROW = 'A';
  public static final char MAX_WELL_ROW = 'P';
  public static final int PLATE_ROWS = (MAX_WELL_ROW - MIN_WELL_ROW) + 1;
  public static final int PLATE_COLUMNS = (MAX_WELL_COLUMN - MIN_WELL_COLUMN) + 1;
  public static final int PLATE_NUMBER_LEN = 5;
  /**
   * The number of decimal places used when recording volume values.
   */
  public static final int VOLUME_SCALE = 2;
  
  public static boolean isValidWellName(String wellName)
  {
    return _wellParsePattern.matcher(wellName).matches();
  }
  
  
  // instance fields

  private String _wellId;
  private Integer _version;
  private Library _library;
  private Set<Compound> _compounds = new HashSet<Compound>();
  private Set<SilencingReagent> _silencingReagents = new HashSet<SilencingReagent>();
  private String _iccbNumber;
  private String _vendorIdentifier;
  private WellType _wellType = WellType.EXPERIMENTAL;
  private String _smiles;
  // TODO: this is alwya either a 0 or 1-element set, so consider changing to a to-one relationship with a proxyable entity; the key is to maintain lazy loading of the molfile data 
  private Set<String> _molfile = new HashSet<String>();
  private String _genbankAccessionNumber;

  private transient WellKey _wellKey;


  // public constructors and instance methods

  /**
   * Constructs an initialized <code>Well</code> object.
   * 
   * @param parentLibrary
   * @param wellKey
   * @param wellType
   */
  public Well(Library parentLibrary, WellKey wellKey, WellType wellType) {
    _wellKey = wellKey;
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
    this(parentLibrary, new WellKey(plateNumber, wellName), WellType.EXPERIMENTAL);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
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
    assert _wellKey != null : "properties forming business key have not been defined";
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
   * Get the set of compounds, ordered by length of SMILES string, from longest to shortest.
   * @return the set of compounds, ordered by length of SMILES string, from longest to shortest
   */
  @DerivedEntityProperty
  public SortedSet<Compound> getOrderedCompounds()
  {
    SortedSet<Compound> orderedCompounds = new TreeSet<Compound>(new Comparator<Compound>() {
      public int compare(Compound compound1, Compound compound2)
      {
        int lengthCompare =
          compound2.getSmiles().length() - compound1.getSmiles().length();
        if (lengthCompare == 0) {
          return compound1.getSmiles().compareTo(compound2.getSmiles());
        }
        return lengthCompare;
      }
    });
    orderedCompounds.addAll(getHbnCompounds());
    return orderedCompounds;
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
   * Remove all the compounds. 
   */
  public void removeCompounds()
  {
    HashSet<Compound> compoundsCopy = new HashSet<Compound>(getHbnCompounds());
    for (Compound compound : compoundsCopy) {
      removeCompound(compound);
    }
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
   * Get the gene that has silencing reagents contained in this well.
   * @return the gene that have silencing reagents contained in this well
   */
  @DerivedEntityProperty
  public Gene getGene()
  {
    Set<Gene> genes = getGenes();
    if (genes.size() > 1) {
      throw new IndexOutOfBoundsException();
    }
    if (genes.size() == 0) {
      return null;
    }
    return genes.iterator().next();
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
      silencingReagent.getGene().getHbnSilencingReagents().remove(silencingReagent);
      return silencingReagent.getHbnWells().remove(this);
    }
    return false;
  }
  
  /**
   * Remove all the silencing reagents. 
   */
  public void removeSilencingReagents()
  {
    HashSet<SilencingReagent> silencingReagentsCopy =
      new HashSet<SilencingReagent>(getHbnSilencingReagents());
    for (SilencingReagent silencingReagent : silencingReagentsCopy) {
      removeSilencingReagent(silencingReagent);
    }
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
    return _wellKey.getPlateNumber();
  }

  /**
   * Set the plate number for the well.
   * 
   * @param plateNumber the new plate number for the well
   */
  private void setPlateNumber(Integer plateNumber)
  {
    if (_wellKey == null) {
      _wellKey = new WellKey(plateNumber, 0, 0);
    }
    else {
      _wellKey.setPlateNumber(plateNumber);
    }
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
    return _wellKey.getWellName();
  }

  /**
   * Set the well name for the well.
   * 
   * @param wellName the new well name for the well
   */
  private void setWellName(String wellName)
  {
    if (_wellKey == null) {
      _wellKey = new WellKey(0, wellName);
    }
    else {
      _wellKey.setWellName(wellName);
    }
  }
  
  @DerivedEntityProperty
  public WellKey getWellKey()
  {
    return _wellKey;
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
   * @hibernate.property type="text" not-null="false"
   */
  public String getVendorIdentifier()
  {
    return _vendorIdentifier;
  }
  
  @DerivedEntityProperty
  public String getFullVendorIdentifier()
  {
    String vendor = _library.getVendor();
    if (vendor == null) {
      return _vendorIdentifier;
    }
    if (_vendorIdentifier == null) {
      return vendor;
    }
    return vendor + " " + _vendorIdentifier;
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
   */
  public String getMolfile()
  {
    if (_molfile.isEmpty()) {
      return null;
    }
    return _molfile.iterator().next();
  }

  /**
   * Set the molfile for the well.
   * 
   * @param molfile The new molfile for the well
   */
  public void setMolfile(String molfile)
  {
    _molfile.clear();
    if (molfile != null) {
      _molfile.add(molfile);
    }
  }

  /**
   * @motivation Although molfile is just a simple property, we force it to be
   *             in a separate table in order to avoid loading its potentially
   *             large value and consuming memory unless it is explicitly
   *             requested by the application.
   * @hibernate.set class="java.lang.String" lazy="true" table="well_molfile"
   * @hibernate.collection-key column="well_id"
   * @hibernate.collection-element column="molfile" type="text"
   */
  private Set<String> getHbnMolfile()
  {
    return _molfile;
  }
  
  private void setHbnMolfile(Set<String> molfile)
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
   * Get the <i>zero-based</i> row index of this well.
   * @return the <i>zero-based</i> row index of this well
   */
  @DerivedEntityProperty
  public int getRow()
  {
    return _wellKey.getRow();
  }
  
  @DerivedEntityProperty
  /**
   * Get the row letter of this well.
   * @return the row letter of this well
   */
  public char getRowLetter()
  {
    return (char) (_wellKey.getRow() + MIN_WELL_ROW);
  }

  /**
   * Get the <i>zero-based</i> column index of this well.
   * @return the <i>zero-based</i> column index of this well
   */
  @DerivedEntityProperty
  public int getColumn()
  {
    return _wellKey.getColumn();
  }
  
  @DerivedEntityProperty
  public boolean isEdgeWell()
  {
    // TODO: use plate size/layout to determine this dynamically
    return _wellKey.getRow() == 0 || _wellKey.getRow() == PLATE_ROWS - 1 || 
    _wellKey.getColumn() == 0 || _wellKey.getColumn() == PLATE_COLUMNS - 1;
  }
  

  // protected getters and setters

  protected Object getBusinessKey()
  {
    return _wellKey;
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
   *   cascade="save-update"
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
   *   cascade="save-update"
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
   *   lazy="proxy"
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
  
  public int compareTo(Object o)
  {
    assert o instanceof Well : "input to compareTo() must be a Well";
    return getWellKey().compareTo(((Well) o).getWellKey());
  }
}
