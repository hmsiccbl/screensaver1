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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.hibernate.annotations.MapKeyManyToMany;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screens.ScreenType;

/**
 * Describes the location and contents of a well in a {@link Library} {@link Plate}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={ "plateNumber", "wellName" })})
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Library.class)
public class Well extends SemanticIDAbstractEntity<String> implements Comparable<Well>
{

  // static fields

  private static final Logger log = Logger.getLogger(Well.class);
  private static final long serialVersionUID = 2682270079212906959L;
  public static final Pattern _wellParsePattern = Pattern.compile("([A-Za-z])(\\d{1,2})");

  public static final Well NULL_WELL = new Well();
  
  public static boolean isValidWellName(String wellName)
  {
    return _wellParsePattern.matcher(wellName).matches();
  }

  public static final RelationshipPath<Well> library = RelationshipPath.from(Well.class).to("library", Cardinality.TO_ONE);
  public static final RelationshipPath<Well> reagents = RelationshipPath.from(Well.class).to("reagents");
  public static final RelationshipPath<Well> latestReleasedReagent = RelationshipPath.from(Well.class).to("latestReleasedReagent", Cardinality.TO_ONE);
  public static final RelationshipPath<Well> resultValues = RelationshipPath.from(Well.class).to("resultValues", ResultValue.class, "well", Cardinality.TO_MANY);
  public static final RelationshipPath<Well> deprecationActivity = RelationshipPath.from(Well.class).to("deprecationActivity", Cardinality.TO_ONE);


  // instance fields

  private transient WellKey _wellKey;
  private Integer _version;
  private Library _library;
  private LibraryWellType _wellType = LibraryWellType.UNDEFINED;
  private Map<LibraryContentsVersion,Reagent> _reagents = Maps.newHashMap();
  private Reagent _latestReleasedReagent;
  private String _facilityId;
  private Map<DataColumn,ResultValue> _resultValues = new HashMap<DataColumn,ResultValue>();
  private AdministrativeActivity _deprecationActivity;
  private MolarConcentration _concentration;


  /**
   * Construct an initialized <code>Well</code> object.
   * @param library
   * @param wellKey
   * @param wellType
   * @motivation for use of {@link Library#createWell(WellKey, LibraryWellType)} only
   */
  Well(Library library, WellKey wellKey, LibraryWellType wellType)
  {
    if (wellKey.getPlateNumber() < library.getStartPlate() || wellKey.getPlateNumber() > library.getEndPlate()) {
      throw new DataModelViolationException("well " + wellKey +
                                            " is not within library plate range [" +
                                            library.getStartPlate() + "," +
                                            library.getEndPlate() + "]");
    }
    _library = library;
    setWellId(wellKey.getKey());
    setLibraryWellType(wellType);
  }

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Well() {}

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
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
    return getEntityId();
  }

  @ManyToOne
  @JoinColumn(name="libraryId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_well_to_library")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Library getLibrary()
  {
    return _library;
  }

  private void setLibrary(Library library)
  {
    _library = library;
  }

  /**
   * The versioned reagents for this Well, accessible as a map, keyed on library
   * contents version number. A Well has only one reagent at a given point in
   * time.
   */
  @OneToMany(mappedBy = "well", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
  @ToMany(hasNonconventionalMutation=true) // Map-based collections not yet supported, tested in LibraryTest.testSmallCompoundLibraryAndrReagents
  @MapKey(name = "libraryContentsVersion")
  public Map<LibraryContentsVersion,Reagent> getReagents()
  {
    return _reagents;
  }
  
  
  public SilencingReagent createSilencingReagent(ReagentVendorIdentifier rvi,
                                                 SilencingReagentType silencingReagentType,
                                                 String sequence)
  {
    return createSilencingReagent(rvi, silencingReagentType, sequence, true);
  }

  /**
   * @param updateReagentsRelationship if false, Well.reagents relationship will
   *          not be updated <i>in memory</i>, which avoids having to load
   *          existing reagents from earlier library contents versions, thereby
   *          saving memory (useful for library contents loading operations);
   *          relationship will be updated when Well is loaded from persistent
   *          storage.
   */
  public SilencingReagent createSilencingReagent(ReagentVendorIdentifier rvi,
                                                 SilencingReagentType silencingReagentType,
                                                 String sequence,
                                                 boolean updateReagentsRelationship)
  {
    if (getLibrary().getLatestContentsVersion() == null) {
      throw new DataModelViolationException("a library contents version must be created first");
    }
    if (_reagents.containsKey(getLibrary().getLatestContentsVersion())) {
      throw new DuplicateEntityException(this, getLibrary().getLatestContentsVersion());
    }
    if (_library.getScreenType() != ScreenType.RNAI) {
      throw new DataModelViolationException("silencing reagents can only be created for RNAi libraries");
    }
    SilencingReagent silencingReagent = new SilencingReagent(rvi,
                                                             this, 
                                                             getLibrary().getLatestContentsVersion(),                                                                         
                                                             silencingReagentType,
                                                             sequence);
    if (updateReagentsRelationship) {
      _reagents.put(getLibrary().getLatestContentsVersion(), silencingReagent);
    }
    return silencingReagent;
  }
  
  /**
  * @throws DuplicateEntityException if a Reagent already exists for this Well and LibraryContentsVersion
  */
  public SmallMoleculeReagent createSmallMoleculeReagent(ReagentVendorIdentifier rvi,
                                                         String molfile,
                                                         String smiles,
                                                         String inChi,
                                                         BigDecimal molecularMass,
                                                         BigDecimal molecularWeight,
                                                         MolecularFormula molecularFormula)
  {
    return createSmallMoleculeReagent(rvi, molfile, smiles, inChi, molecularMass, molecularWeight, molecularFormula, true);
  }

  /**
   * @param updateReagentsRelationship if false, Well.reagents relationship will
   *          not be updated <i>in memory</i>, which avoids having to load
   *          existing reagents from earlier library contents versions, thereby
   *          saving memory (useful for library contents loading operations);
   *          relationship will be updated when Well is loaded from persistent
   *          storage.
   * @throws DuplicateEntityException if a Reagent already exists for this Well and LibraryContentsVersion
   */
  public SmallMoleculeReagent createSmallMoleculeReagent(ReagentVendorIdentifier rvi,
                                                         String molfile,
                                                         String smiles,
                                                         String inChi,
                                                         BigDecimal molecularMass,
                                                         BigDecimal molecularWeight,
                                                         MolecularFormula molecularFormula,
                                                         boolean updateReagentsRelationship)
  {
    if (getLibrary().getLatestContentsVersion() == null) {
      throw new DataModelViolationException("a library contents version must be created first");
    }
    if (_reagents.containsKey(getLibrary().getLatestContentsVersion())) {
      throw new DuplicateEntityException(this, getLibrary().getLatestContentsVersion());
    }
    if (_library.getScreenType() != ScreenType.SMALL_MOLECULE) {
      throw new DataModelViolationException("small molecule reagents can only be created for small molecule libraries");
    }
    SmallMoleculeReagent smallMoleculeReagent = new SmallMoleculeReagent(rvi,
                                                                         this, 
                                                                         getLibrary().getLatestContentsVersion(),                                                                         
                                                                         molfile,
                                                                         smiles,
                                                                         inChi,
                                                                         molecularMass,
                                                                         molecularWeight,
                                                                         molecularFormula);
    if (updateReagentsRelationship) {
      _reagents.put(getLibrary().getLatestContentsVersion(), smallMoleculeReagent);
    }
    return smallMoleculeReagent;
  }

  public NaturalProductReagent createNaturalProductReagent(ReagentVendorIdentifier rvi)
  {
    return createNaturalProductReagent(rvi, true);
  }

  /**
   * @param updateReagentsRelationship if false, Well.reagents relationship will
   *          not be updated <i>in memory</i>, which avoids having to load
   *          existing reagents from earlier library contents versions, thereby
   *          saving memory (useful for library contents loading operations);
   *          relationship will be updated when Well is loaded from persistent
   *          storage.
   */
  public NaturalProductReagent createNaturalProductReagent(ReagentVendorIdentifier rvi,
                                                           boolean updateReagentsRelationship)
  {
    if (getLibrary().getLatestContentsVersion() == null) {
      throw new DataModelViolationException("a library contents version must be created first");
    }
    if (_reagents.containsKey(getLibrary().getLatestContentsVersion())) {
      throw new DuplicateEntityException(this, getLibrary().getLatestContentsVersion());
    }
    if (!_library.getReagentType().equals(NaturalProductReagent.class)) {
      throw new DataModelViolationException("natural product reagents can only be created for natural products libraries");
    }
    NaturalProductReagent naturalProductReagent = new NaturalProductReagent(rvi, this, getLibrary().getLatestContentsVersion());
    if (updateReagentsRelationship) {
      _reagents.put(getLibrary().getLatestContentsVersion(), naturalProductReagent);
    }
    return naturalProductReagent;
    
  }

  private void setReagents(Map<LibraryContentsVersion,Reagent> reagents)
  {
    _reagents = reagents;
  }

  /**
   * Get the most recently released version of this Well's Reagent.
   * 
   * @return the most recently released Reagent for this Well, or null if this Well's
   *         Library has no contents versions that have been released.
   */
  @SuppressWarnings("unchecked")
  @OneToOne(targetEntity=Reagent.class, cascade={}, fetch=FetchType.LAZY)
  @JoinColumn(name="latestReleasedReagentId")
  @ToOne(unidirectional=true, hasNonconventionalSetterMethod=true) /* managed by Well.reagents instead */
  public <R extends Reagent> R getLatestReleasedReagent()
  {
    return (R) _latestReleasedReagent;
  }

  public void setLatestReleasedReagent(Reagent latestReleasedReagent)
  {
    _latestReleasedReagent = latestReleasedReagent;
  }

  @Transient
  public <R extends Reagent> R getPendingReagent()
  {
    LibraryContentsVersion latestContentsVersion = getLibrary().getLatestContentsVersion();
    if (latestContentsVersion.isReleased()) {
      return null;
    }
    return (R) getReagents().get(latestContentsVersion);
  }

  @Column(nullable = false, updatable = false)
  public Integer getPlateNumber()
  {
    return _wellKey.getPlateNumber();
  }

  private void setPlateNumber(Integer plateNumber)
  {
  }

  @Column(nullable = false, updatable = false)
  @org.hibernate.annotations.Type(type="text")
  public String getWellName()
  {
    return _wellKey.getWellName();
  }

  private void setWellName(String wellName)
  {
  }

  @Transient
  public WellKey getWellKey()
  {
    return _wellKey;
  }

  /**
   * Get the facility ID for the well, which can be used an alternate identifier
   * to plate/well pairs.
   * 
   * @return the facility ID for the well
   */
  @org.hibernate.annotations.Type(type="text")
  public String getFacilityId()
  {
    return _facilityId;
  }

  public void setFacilityId(String facilityId)
  {
    _facilityId = facilityId;
  }

  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.LibraryWellType$UserType"
  )
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /* immutable after defined & persisted */)
  public LibraryWellType getLibraryWellType()
  {
    return _wellType;
  }

  public void setLibraryWellType(LibraryWellType libraryWellType)
  {
    if (!isHibernateCaller()) {
      if (libraryWellType != _wellType) {
        if (_wellType != LibraryWellType.UNDEFINED && libraryWellType != LibraryWellType.UNDEFINED && _library.getLibraryId() != null) {
          throw new DataModelViolationException("cannot change library well type after it has been defined and persisted, unless the new well type is 'undefined'");
        }
      }
      if (_wellType != LibraryWellType.EXPERIMENTAL && libraryWellType == LibraryWellType.EXPERIMENTAL) {
        _library.incExperimentalWellCount();
      }
      else if (_wellType == LibraryWellType.EXPERIMENTAL && libraryWellType != LibraryWellType.EXPERIMENTAL) {
        _library.decExperimentalWellCount();
      }
    }
    _wellType = libraryWellType;
  }

  /**
   * Determines whether the well has been marked as deprecated. A deprecated
   * well is one that is no longer valid for screening. It will continue to be
   * available when a library is screened, but it should not be cherry picked.
   *
   * @return whether the well is deprecated
   */
  //TODO @Formula("deprecation_admin_activity_id IS NOT NULL")
  @Column(name="isDeprecated", nullable=false)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public boolean isDeprecated()
  {
    return _deprecationActivity != null;
  }
  
  private void setDeprecated(boolean isDeprecated) {}
  

  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
             fetch=FetchType.LAZY)
  @JoinColumn(nullable=true, updatable=true, name="deprecation_admin_activity_id")
  @org.hibernate.annotations.ForeignKey(name="fk_well_to_deprecation_admin_activity")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true, hasNonconventionalSetterMethod=true /*AdministrativeActivity.type must be AAT.WELL_DEPRECATION; tested in WellTest#testDeprecation()*/)
  //@org.hibernate.annotations.Index(name="well_deprecation_activity_id_index", columnNames={"deprecation_admin_activity"})
  public AdministrativeActivity getDeprecationActivity()
  {
    return _deprecationActivity;
  }

  public void setDeprecationActivity(AdministrativeActivity deprecationActivity)
  {
    if (!isHibernateCaller()) {
      if (deprecationActivity != null &&
        deprecationActivity.getType() != AdministrativeActivityType.WELL_DEPRECATION) {
        throw new DataModelViolationException("can only add AdministrativeActivity of type " + AdministrativeActivityType.WELL_DEPRECATION);
      }
    }
    _deprecationActivity = deprecationActivity;
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
    return _wellKey.getRow() == 0 || _wellKey.getRow() == _library.getPlateSize().getRows() - 1 ||
    _wellKey.getColumn() == 0 || _wellKey.getColumn() == _library.getPlateSize().getColumns() - 1;
  }


  /**
   * @return a Map of the result values associated with this well, as a map with
   *         {@link DataColumn}s as keys
   */
  @OneToMany(fetch=FetchType.LAZY, mappedBy="well")
  @MapKeyManyToMany(joinColumns={ @JoinColumn(name="dataColumnId") }, targetEntity=DataColumn.class)
  @ToMany(hasNonconventionalMutation=true) // Map-based collections not yet supported, tested in WellTest.testResultValueMap()
  public Map<DataColumn,ResultValue> getResultValues()
  {
    return _resultValues;
  }


  private void setResultValues(Map<DataColumn,ResultValue> resultValues)
  {
    _resultValues = resultValues;
  }

  private void setWellId(String wellId)
  {
    setEntityId(wellId);
    _wellKey = new WellKey(wellId);
  }

  /**
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  public void setMolarConcentration(MolarConcentration value)
  {
    _concentration = value;
  }
  
  /**
   * Concentration, in nanoMolar units
   * @return
   */
  @Column(precision = ScreensaverConstants.CONCENTRATION_FULL_PRECISION, scale = ScreensaverConstants.CONCENTRATION_FULL_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  public MolarConcentration getMolarConcentration()
  {
    return _concentration;
  }

  @Transient
  public MolarUnit getConcentrationUnit()
  {
    return _concentration.getUnits();
  }
}