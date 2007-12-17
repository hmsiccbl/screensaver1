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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.AbstractEntityIdComparator;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.MapKeyManyToMany;


/**
 * A Hibernate entity bean representing a well.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={ "plateNumber", "wellName" })})
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Library.class)
public class Well extends SemanticIDAbstractEntity implements Comparable<Well>
{

  // static fields

  private static final Logger log = Logger.getLogger(Well.class);
  private static final long serialVersionUID = 2682270079212906959L;
  public static Pattern _wellParsePattern = Pattern.compile("([A-Za-z])(\\d{1,2})");

  public static final Well NULL_WELL = new Well();

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
  private Reagent _reagent;
  private Set<Compound> _compounds = new HashSet<Compound>();
  private Set<SilencingReagent> _silencingReagents = new HashSet<SilencingReagent>();
  private String _iccbNumber;
  private String _vendorIdentifier;
  private WellType _wellType = WellType.EXPERIMENTAL;
  private String _smiles;
  private List<String> _molfile = new ArrayList<String>();
  private String _genbankAccessionNumber;
  private Map<ResultValueType,ResultValue> _resultValues = new HashMap<ResultValueType,ResultValue>();

  private transient WellKey _wellKey;
  private transient Map<Serializable,ResultValue> _resultValueTypeIdToResultValue;


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
    return getWellKey().toString();
  }

  public int compareTo(Well other)
  {
    return getWellKey().compareTo(((Well) other).getWellKey());
  }

  /**
   * Get the well id for the well.
   * @return the well id for the well
   */
  @Id
  @org.hibernate.annotations.Type(type="text")
  public String getWellId()
  {
    return getWellKey().toString();
  }

  /**
   * Get the library the well is in.
   * @return the library the well is in.
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
             fetch=FetchType.LAZY)
  @JoinColumn(name="libraryId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_well_to_library")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  public Library getLibrary()
  {
    return _library;
  }

  /**
   * Get the set of compounds.
   * @return the set of compounds
   */
  @ManyToMany(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
              fetch=FetchType.LAZY)
  @JoinTable(
    name="wellCompoundLink",
    joinColumns=@JoinColumn(name="wellId"),
    inverseJoinColumns=@JoinColumn(name="compoundId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_well_compound_link_to_well")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  public Set<Compound> getCompounds()
  {
    return _compounds;
  }

  /**
   * Get the set of compounds, ordered by length of SMILES string, from longest to shortest.
   * @return the set of compounds, ordered by length of SMILES string, from longest to shortest
   */
  @Transient
  public SortedSet<Compound> getOrderedCompounds()
  {
    SortedSet<Compound> orderedCompounds = new TreeSet<Compound>();
    orderedCompounds.addAll(_compounds);
    return orderedCompounds;
  }

  /**
   * Get the primary compound: the compound that is most likely the one being tested for
   * bioactivity. {@link Compound Compounds} are comparable on just such a quality.
   * @return the primary compound
   */
  @Transient
  public Compound getPrimaryCompound()
  {
    if (_compounds.isEmpty()) {
      return null;
    }
    return getOrderedCompounds().first();
  }

  /**
   * Add the compound.
   * @param compound the compound to add
   * @return true iff the compound was not already in the well
   */
  public boolean addCompound(Compound compound)
  {
    compound.getWells().add(this);
    return _compounds.add(compound);
  }

  /**
   * Remove the compound.
   * @param compound the compound to remove
   * @return true iff the compound was previously in the well
   */
  public boolean removeCompound(Compound compound)
  {
    compound.getWells().remove(this);
    return _compounds.remove(compound);
  }

  /**
   * Remove all the compounds.
   */
  public void removeCompounds()
  {
    for (Compound compound : _compounds) {
      compound.getWells().remove(this);
    }
    _compounds.clear();
  }

  /**
   * Get the set of silencing reagents.
   * @return the set of silencing reagents
   */
  @ManyToMany(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
              fetch=FetchType.LAZY)
  @JoinTable(
    name="wellSilencingReagentLink",
    joinColumns=@JoinColumn(name="wellId"),
    inverseJoinColumns=@JoinColumn(name="silencingReagentId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_well_silencing_reagent_link_to_well")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  public Set<SilencingReagent> getSilencingReagents()
  {
    return _silencingReagents;
  }

  /**
   * Add the silencing reagent.
   * @param silencingReagent the silencing reagent to add
   * @return true iff the silencing reagent was not already in the well
   */
  public boolean addSilencingReagent(SilencingReagent silencingReagent)
  {
    silencingReagent.getWells().add(this);
    return _silencingReagents.add(silencingReagent);
  }

  /**
   * Remove the silencing reagent.
   * @param silencingReagent the silencing reagent to remove
   * @return true iff the compound was previously in the well
   */
  public boolean removeSilencingReagent(SilencingReagent silencingReagent)
  {
    silencingReagent.getWells().add(this);
    return _silencingReagents.remove(silencingReagent);
  }

  /**
   * Remove all the silencing reagents.
   */
  public void removeSilencingReagents()
  {
    for (SilencingReagent silencingReagent : _silencingReagents) {
      silencingReagent.getWells().remove(this);
    }
    _silencingReagents.clear();
  }

  /**
   * Get the set of genes that have silencing reagents contained in this well.
   * @return the set of genes that have silencing reagents contained in this well
   */
  @Transient
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
  @Transient
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
   * Get the plate number for the well.
   * @return the plate number for the well
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false)
  public Integer getPlateNumber()
  {
    return _wellKey.getPlateNumber();
  }

  /**
   * Get the well name for the well.
   * @return the well name for the well
   */
  @org.hibernate.annotations.Immutable
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getWellName()
  {
    return _wellKey.getWellName();
  }

  /**
   * Get the well key.
   * @return the well key
   */
  @Transient
  public WellKey getWellKey()
  {
    return _wellKey;
  }

  @Transient
  public String getSimpleVendorIdentifier()
  {
    if (_reagent == null) {
      return null;
    }
    return _reagent.getReagentId().getVendorIdentifier();
  }

  /**
   * Get the ICCB number for the well.
   * @return the ICCB number for the well
   */
  @org.hibernate.annotations.Type(type="text")
  public String getIccbNumber()
  {
    return _iccbNumber;
  }

  /**
   * Set the ICCB number for the well.
   * @param iccbNumber The new ICCB number for the well
   */
  public void setIccbNumber(String iccbNumber)
  {
    _iccbNumber = iccbNumber;
  }

  /**
   * Get the well's type.
   * @return the well's type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.WellType$UserType"
  )
  public WellType getWellType()
  {
    return _wellType;
  }

  /**
   * Set the well's type.
   * @param wellType the new type of the well
   */
  public void setWellType(WellType wellType)
  {
    _wellType = wellType;
  }

  /**
   * Get the SMILES for the well.
   * @return the SMILES for the well
   */
  @org.hibernate.annotations.Type(type="text")
  public String getSmiles()
  {
    return _smiles;
  }

  /**
   * Set the SMILES for the well.
   * @param smiles The new SMILES for the well
   */
  public void setSmiles(String smiles)
  {
    _smiles = smiles;
  }

  /**
   * Get the molfile for the well.
   * @return the molfile for the well
   */
  @Transient
  public String getMolfile()
  {
    if (_molfile.size() == 0) {
      return null;
    }
    return _molfile.get(0);
  }

  /**
   * Set the molfile for the well.
   * @param molfile The new molfile for the well
   */
  public void setMolfile(String molfile)
  {
    _molfile.clear();
    _molfile.add(molfile);
  }

  /**
   * Get the GenBank Accession number.
   * @return the GenBank Accession number
   */
  @org.hibernate.annotations.Type(type="text")
  public String getGenbankAccessionNumber()
  {
    return _genbankAccessionNumber;
  }

  /**
   * Set the GenBank Accession number.
   * @param genbankAccessionNumber the GenBank Accession number
   */
  public void setGenbankAccessionNumber(String genbankAccessionNumber)
  {
    _genbankAccessionNumber = genbankAccessionNumber;
  }

  /**
   * Get the reagent.
   * @return the reagent; can be null if library contents have not been loaded
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
             fetch=FetchType.LAZY)
  @JoinColumn(nullable=true, updatable=true, name="reagent_id")
  @org.hibernate.annotations.ForeignKey(name="fk_well_to_reagent")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @org.hibernate.annotations.Index(name="well_reagent_id_index", columnNames={"reagent_id"})
  public Reagent getReagent()
  {
    return _reagent;
  }

  /**
   * Set the new reagent.
   * @param reagent the new reagent
   */
  public void setReagent(Reagent reagent)
  {
    if (isHibernateCaller()) {
      _reagent = reagent;
      return;
    }

    if (_reagent != null) {
      _reagent.getWells().remove(this);
    }
    _reagent = reagent;
    if (_reagent != null) {
      _reagent.getWells().add(this);
    }

    if (_wellType.equals(WellType.EXPERIMENTAL) && _reagent == null) {
      throw new DataModelViolationException("experimental well must have a reagent");
    }
  }

  /**
   * Get the <i>zero-based</i> row index of this well.
   * @return the <i>zero-based</i> row index of this well
   */
  @Transient
  public int getRow()
  {
    return _wellKey.getRow();
  }

  /**
   * Get the row letter of this well.
   * @return the row letter of this well
   */
  @Transient
  public char getRowLetter()
  {
    return (char) (_wellKey.getRow() + MIN_WELL_ROW);
  }

  /**
   * Get the <i>zero-based</i> column index of this well.
   * @return the <i>zero-based</i> column index of this well
   */
  @Transient
  public int getColumn()
  {
    return _wellKey.getColumn();
  }

  /**
   * Return true iff this well is on the edge of the 384-well plate.
   * @return true iff this well is on the edge of the 384-well plate
   */
  @Transient
  public boolean isEdgeWell()
  {
    // TODO: use plate size/layout to determine this dynamically
    return _wellKey.getRow() == 0 || _wellKey.getRow() == PLATE_ROWS - 1 ||
    _wellKey.getColumn() == 0 || _wellKey.getColumn() == PLATE_COLUMNS - 1;
  }


  /**
   * Get the set of result values.
   * @return the set of result values
   */
  @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="well")
  @LazyCollection(LazyCollectionOption.EXTRA)
  @MapKeyManyToMany(joinColumns={ @JoinColumn(name="resultValueTypeId") }, targetEntity=ResultValueType.class)
  public Map<ResultValueType,ResultValue> getResultValues()
  {
    return _resultValues;
  }

  /**
   * Set a subset of eagerly fetched result values for this well. Well must be
   * detached or loaded read-only! Otherwise, Hibernate *might* persist the
   * limited subset! (untested theory). You probably want Map to be a TreeMap,
   * with a {@link AbstractEntityIdComparator} comparator, to allow entity
   * ID-based equality, rather than instance equality.
   *
   * The "_" prefix prevents our test code from treating this method as a property
   * setter method.
   *
   * @motivation Allows Well to be used as a DTO, of sorts, allowing a limited
   *             subset of result values to be loaded efficiently and then later
   *             accessed via client code, but naturally through the entity
   *             object model.
   * @param map
   */
  public void _setResultValuesSubset(Map<ResultValueType,ResultValue> resultValues)
  {
    _resultValues = resultValues;
  }


  // package constructors

  /**
   * Construct an initialized <code>Well</code> object.
   * @param library
   * @param wellKey
   * @param wellType
   * @motivation for use of {@link Library#createWell(WellKey, WellType)} only
   */
  Well(Library library, WellKey wellKey, WellType wellType, Reagent reagent)
  {
    // TODO: reinstate once entity model test code can be made to respect this constraint
//    if (wellKey.getPlateNumber() < library.getStartPlate() || wellKey.getPlateNumber() > library.getEndPlate()) {
//      throw new DataModelViolationException("well " + wellKey +
//                                            " is not within library plate range [" +
//                                            library.getStartPlate() + "," +
//                                            library.getEndPlate() + "]");
//    }
    _library = library;
    _wellKey = wellKey;
    _wellType = wellType;
    _reagent = reagent;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>Well</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Well() {}


  // private methods

  /**
   * Set the well id for the well.
   * @param wellId the new well id for the well
   * @motivation for hibernate
   */
  private void setWellId(String wellId)
  {
    _wellId = wellId;
  }

  /**
   * Get the version of the well.
   * @return the version of the well
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version of the well.
   * @param version the new version of the well
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the library the well is in.
   * @param library the new library for the well
   * @motivation for hibernate
   */
  private void setLibrary(Library library)
  {
    _library = library;
  }

  /**
   * Set the set of compounds
   * @param compounds the new set of compounds
   * @motivation for hibernate
   */
  private void setCompounds(Set<Compound> compounds)
  {
    _compounds = compounds;
  }

  /**
   * Set the set of silencing reagents
   * @param silencingReagents the new set of silencing reagents
   * @motivation for hibernate
   */
  private void setSilencingReagents(Set<SilencingReagent> silencingReagents)
  {
    _silencingReagents = silencingReagents;
  }

  /**
   * Set the plate number for the well.
   * @param plateNumber the new plate number for the well
   * @motivation for hibernate
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
   * Set the well name for the well.
   * @param wellName the new well name for the well
   * @motivation for hibernate
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

  /**
   * @motivation we want lazy loading of molfile property, due to its large data
   *             size, but can only make it lazy loadable by mapping it in a
   *             value collection
   * @see #getMolfile()
   */
  @org.hibernate.annotations.CollectionOfElements(fetch=FetchType.LAZY)
  @org.hibernate.annotations.Type(type="text")
  @JoinTable(name="wellMolfile", joinColumns=@JoinColumn(name="well_id", unique=true)) // note "unique=true" ensures 1-to-1 mapping
  @Column(name="molfile", nullable=false)
  @IndexColumn(name="ordinal")
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

  /**
   * Set the result values.
   * @param resultValues the new result values
   * @motivation for hibernate
   */
  private void setResultValues(Map<ResultValueType,ResultValue> resultValues)
  {
    _resultValues = resultValues;
  }
}