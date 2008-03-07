// $HeadURL$
// $Id$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.OptimisticLock;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.ui.screenresults.MetaDataType;

/**
 * Provides the metadata for a subset of a
 * {@link edu.harvard.med.screensaver.model.screens.Screen Screen}'s
 * {@link ResultValue ResultValues}, all of which will have been produced "in the same
 * way". A <code>ResultValueType</code> can describe either how a subset of
 * raw data values were generated via automated machine reading of assay plates,
 * or how a subset of derived data values were calculated. For raw data values,
 * the metadata describes the parameters of a single, physical machine read of a
 * single replicate assay plate, using the same assay read technology, at the
 * same read time interval, etc.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=ScreenResult.class)
public class ResultValueType extends AbstractEntity implements MetaDataType, Comparable
{

  // TODO: perhaps we should split ResultValueType into subclasses, one for raw
  // data value descriptions and one for derived data descriptions?

  private static final Logger log = Logger.getLogger(ResultValueType.class);
  private static final long serialVersionUID = -2325466055774432202L;


  // private instance data

  private Integer _resultValueTypeId;
  private Integer _version;
  private ScreenResult _screenResult;
  private Map<WellKey,ResultValue> _resultValues = new HashMap<WellKey,ResultValue>();
  private String _name;
  private String _description;
  private Integer _ordinal;
  private Integer _replicateOrdinal;
  private AssayReadoutType _assayReadoutType;
  private String _timePoint;
  private boolean _isDerived;
  private String _howDerived;
  private SortedSet<ResultValueType> _typesDerivedFrom = new TreeSet<ResultValueType>();
  private SortedSet<ResultValueType> _derivedTypes = new TreeSet<ResultValueType>();
  private boolean _isPositiveIndicator;
  private PositiveIndicatorType _positiveIndicatorType;
  private PositiveIndicatorDirection _positiveIndicatorDirection;
  private Double _positiveIndicatorCutoff;
  private boolean _isFollowUpData;
  private String _assayPhenotype;
  private String _comments;
  private boolean _isNumeric;
  private boolean _isNumericalnessDetermined = false;
  private Integer _positivesCount;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getResultValueTypeId();
  }

  /**
   * Defines natural ordering of <code>ResultValueType</code> objects, based
   * upon their ordinal field value. Note that natural ordering is only defined
   * between <code>ResultValueType</code> objects that share the same parent
   * {@link ScreenResult}.
   */
  public int compareTo(Object that)
  {
    return getOrdinal().compareTo(((ResultValueType) that).getOrdinal());
  }

  /**
   * Get the id for the result value type.
   * @return the id for the result value type
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="result_value_type_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="result_value_type_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="result_value_type_id_seq")
  public Integer getResultValueTypeId()
  {
    return _resultValueTypeId;
  }

  /**
   * Get the parent {@link ScreenResult}.
   * @return the parent {@link ScreenResult}
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={})
  @JoinColumn(name="screenResultId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_result_value_type_to_screen_result")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  /**
   * Add a non-numeric, experimental type, non-excluded result value to the result value type.
   * @param well the well of the new ResultValue
   * @param value the value of the new ResultValue
   * @return a new ResultValue iff a result value did not already exist for the given well and result value type, otherwise null
   */
  public ResultValue createResultValue(Well well, String value)
  {
    return createResultValue(well, AssayWellType.EXPERIMENTAL, value, false);
  }

  /**
   * Add a non-numeric result value to the result value type.
   * @param well the well of the new ResultValue
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the value of the new ResultValue
   * @param exclude the exclude flag of the new ResultValue
   * @return a new ResultValue iff a result value did not already exist for the given well and result value type, otherwise null
   */
  public ResultValue createResultValue(Well well,
                                       AssayWellType assayWellType,
                                       String value,
                                       Boolean exclude)
  {
    return createResultValue(well, assayWellType, value, null, 0, exclude);
  }

  /**
   * Add a numeric, experimental type, non-excluded result value to the result value type.
   * @param well the well of the new ResultValue
   * @param numericValue the value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal point, when displayed
   * @return a new ResultValue iff a result value did not already exist for the given well and result value type, otherwise null
   */
  public ResultValue createResultValue(Well well,
                                       Double value,
                                       Integer decimalPrecision)
  {
    return createResultValue(well, AssayWellType.EXPERIMENTAL, value, decimalPrecision, false);
  }

  /**
   * Add a numeric result value to the result value type.
   * @param well the well of the new ResultValue
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param numericValue the numeric value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal point, when value is displayed
   * @param exclude the exclude flag of the new ResultValue
   * @return a new ResultValue iff a result value did not already exist for the given well and result value type, otherwise null
   */
  public ResultValue createResultValue(Well well,
                                       AssayWellType assayWellType,
                                       Double numericValue,
                                       Integer decimalPrecision,
                                       boolean exclude)
  {
    return createResultValue(well, assayWellType, null, numericValue, decimalPrecision, exclude);
  }

  /**
   * Return true iff this result value type contains numeric result values.
   * @return true iff this result value type contains numeric result values
   */
  @Column(nullable=false, name="isNumeric")
  public boolean isNumeric()
  {
    return _isNumeric;
  }

  /**
   * Set the numericalness of this result value type.
   * @param isNumeric the new numericalness of this result value type
   */
  public void setNumeric(boolean isNumeric)
  {
    _isNumeric = isNumeric;
  }
  
  /**
   * Set the numericalness of this result value type. Set the transient property
   * {@link #isNumericalnessDetermined() numericalnessDetermined} to true.
   * @param isNumeric the new numericalness of this result value type
   */
  public void detemineNumericalness(boolean isNumeric)
  {
    _isNumeric = isNumeric;
    _isNumericalnessDetermined = true;
  }

  /**
   * Return true iff the numericalness of this result value type has been determined.
   * @return true iff the numericalness of this result value type has been determined
   * @see #detemineNumericalness(boolean)
   */
  @Transient
  public boolean isNumericalnessDetermined()
  {
    return _isNumericalnessDetermined;
  }

  /**
   * Get the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}.
   * @return the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  public Integer getOrdinal()
  {
    return _ordinal;
  }

  /**
   * Get the replicate ordinal, a one-based index indicating the order of this
   * <code>ResultValueType</code> within its parent {@link ScreenResult}.
   * This ordering is really only significant from the standpoint of presenting
   * a {@link ScreenResult} to the user (historically speaking, it reflects the
   * ordering found during spreadsheet file import).
   *
   * @return the replicate ordinal
   */
  public Integer getReplicateOrdinal()
  {
    return _replicateOrdinal;
  }

  /**
   * Set the replicate ordinal, a 1-based index indicating the order of this
   * <code>ResultValueType</code> within its parent {@link ScreenResult}.
   * @param replicateOrdinal the replicate ordinal
   */
  public void setReplicateOrdinal(Integer replicateOrdinal)
  {
    assert replicateOrdinal == null || replicateOrdinal > 0 : "replicate ordinal values must be positive (non-zero), unless null";
    _replicateOrdinal = replicateOrdinal;
  }

  /**
   * Get the Positive Indicator Type.
   * @return an {@link PositiveIndicatorType} enum
   */
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType$UserType")
  public PositiveIndicatorType getPositiveIndicatorType()
  {
    return _positiveIndicatorType;
  }

  /**
   * Set the Positive Indicator Type.
   * @param positiveIndicatorType the Activity Indicator Type to set
   */
  public void setPositiveIndicatorType(PositiveIndicatorType positiveIndicatorType)
  {
    _positiveIndicatorType = positiveIndicatorType;
  }

  /**
   * Get the Assay Phenotype.
   * @return a <code>String</code> representing the Assay Phenotype
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAssayPhenotype()
  {
    return _assayPhenotype;
  }

  /**
   * Set the Assay Phenotype.
   * @param assayPhenotype the Assay Phenotype to set
   */
  public void setAssayPhenotype(String assayPhenotype)
  {
    _assayPhenotype = assayPhenotype;
  }

  /**
   * Get the Assay Readout Type.
   * @return an {@link AssayReadoutType} enum
   */
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.AssayReadoutType$UserType")
  public AssayReadoutType getAssayReadoutType()
  {
    return _assayReadoutType;
  }

  /**
   * Set the Assay Readout Type.
   * @param assayReadoutType the Assay Readout Type to set
   */
  public void setAssayReadoutType(AssayReadoutType assayReadoutType)
  {
    _assayReadoutType = assayReadoutType;
  }

  /**
   * Get the comments. Comments should describe real-world issues relating to
   * how the data was generated, how it may be problematic, etc. Contrast with
   * {@link #getDescription()}.
   * @return a <code>String</code> containing the comments
   * @see #getDescription()
   */
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments The comments to set.
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the set of result value types that this result value type was derived from.
   * By "derived", we mean that the calculated values of our {@link ResultValues} depend upon
   * the the {@link ResultValue ResultValues} of other {@link ResultValueType ResultValueTypes}
   * (of the same stock plate well). The details of the derivation should be specified via
   * {@link #setHowDerived}.
   * @return the set of result value types that this result value type was derived from
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(
    name="resultValueTypeDerivedFromLink",
    joinColumns=@JoinColumn(name="derivedFromResultValueTypeId"),
    inverseJoinColumns=@JoinColumn(name="derivedResultValueTypeId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_derived_from_result_value_type")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(
    inverseProperty="derivedTypes",
    singularPropertyName="typeDerivedFrom"
  )
  public SortedSet<ResultValueType> getTypesDerivedFrom()
  {
    return _typesDerivedFrom;
  }

  /**
   * Add the result value type to the types derived from. Updates the 'derived'
   * property and 'derived types' collection accordingly.
   * 
   * @param typeDerivedFrom the result value type to add
   * @return true iff the result value type was not already contained in the set
   *         of types derived from this type
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(ResultValueType)
   * @see #removeDerivedType(ResultValueType)
   * @see #removeTypeDerivedFrom(ResultValueType)
   */
  public boolean addTypeDerivedFrom(ResultValueType typeDerivedFrom) {
    assert !(typeDerivedFrom.getDerivedTypes().contains(this) ^ getTypesDerivedFrom().contains(typeDerivedFrom)) :
      "asymmetric types derived from / derived types association encountered";
    if (getTypesDerivedFrom().add(typeDerivedFrom)) {
      setDerived(true);
      return typeDerivedFrom.getDerivedTypes().add(this);
    }
    return false;
  }

  /**
   * Remove the result value type from the types derived from. Updates the
   * 'derived' property and 'derived types' collection accordingly.
   * 
   * @param typeDerivedFrom the result value type to remove
   * @return true iff the result value type was previously contained in the set
   *         of types derived from this type
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(ResultValueType)
   * @see #removeDerivedType(ResultValueType)
   * @see #addTypeDerivedFrom(ResultValueType)
   */
  public boolean removeTypeDerivedFrom(ResultValueType typeDerivedFrom) {
    assert ! (typeDerivedFrom.getDerivedTypes().contains(this) ^ getTypesDerivedFrom().contains(typeDerivedFrom)) :
      "asymmetric types derived from / derived types association encountered";
    if (getTypesDerivedFrom().remove(typeDerivedFrom)) {
      setDerived(! getTypesDerivedFrom().isEmpty());
      return typeDerivedFrom.getDerivedTypes().remove(this);
    }
    return false;
  }

  /**
   * Get the set of result value types that derive from this result value type.
   * @return the set of result value types that derive from this result value type
   * @see #isDerived()
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedTypes()
   * @see #addDerivedTypeFrom(ResultValueType)
   * @see #removeDerivedTypeFrom(ResultValueType)
   */
  @ManyToMany(
    mappedBy="typesDerivedFrom",
    targetEntity=ResultValueType.class,
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.ForeignKey(name="fk_derived_result_value_type")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(inverseProperty="typesDerivedFrom")
  public SortedSet<ResultValueType> getDerivedTypes()
  {
    return _derivedTypes;
  }

  /**
   * Add the result value type to the derived types. Updates the 'derived'
   * property and 'types derived from' collection accordingly.
   * 
   * @param derivedType the result value type to add
   * @return true iff the result value type was not already contained in the set
   *         of derived types
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #removeDerivedType(ResultValueType)
   * @see #addTypeDerivedFrom(ResultValueType)
   * @see #removeTypeDerivedFrom(ResultValueType)
   */
  public boolean addDerivedType(ResultValueType derivedType) {
    assert ! (derivedType.getTypesDerivedFrom().contains(this) ^ getDerivedTypes().contains(derivedType)) :
      "asymmetric derived types / types derived from association encountered";
    if (getDerivedTypes().add(derivedType)) {
      derivedType.getTypesDerivedFrom().add(this);
      derivedType.setDerived(true);
      return true;
    }
    return false;
  }

  /**
   * Remove the result value type from the derived types. Updates the 'derived'
   * property and 'types derived from' collection accordingly.
   * 
   * @param derivedType the result value type to remove
   * @return true iff the result value type was previously contained in the set
   *         of derived types
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(ResultValueType)
   * @see #addTypeDerivedFrom(ResultValueType)
   * @see #removeTypeDerivedFrom(ResultValueType)
   */
  public boolean removeDerivedType(ResultValueType derivedType) {
    assert ! (derivedType.getTypesDerivedFrom().contains(this) ^ getDerivedTypes().contains(derivedType)) :
      "asymmetric derived types / types derived from association encountered";
    if (getDerivedTypes().remove(derivedType)) {
      derivedType.getTypesDerivedFrom().remove(this);
      derivedType.setDerived(! derivedType.getTypesDerivedFrom().isEmpty());
      return true;
    }
    return false;
  }

  /**
   * Get a description of this <code>ResultValueType</code>.
   * @return a <code>String</code> description of this <code>ResultValueType</code>
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set a description of this <code>ResultValueType</code>.
   * @param description the new description of this <code>ResultValueType</code>
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * Get then description of how this <code>ResultValueType</code> was derived
   * from other <code>ResultValueType</code>s.
   * @return a <code>String</code> description of how this
   *         <code>ResultValueType</code> was derived from other
   *         <code>ResultValueType</code>s
   */
  @org.hibernate.annotations.Type(type="text")
  public String getHowDerived()
  {
    return _howDerived;
  }

  /**
   * Set the description of how this <code>ResultValueType</code> was derived
   * from other <code>ResultValueType</code>s.
   * @param howDerived a description of how this <code>ResultValueType</code>
   *          was derived from other <code>ResultValueType</code>s
   */
  public void setHowDerived(String howDerived)
  {
    _howDerived = howDerived;
  }

  /**
   * Get the indicator cutoff
   * @return the indicator cutoff
   */
  @org.hibernate.annotations.Type(type="double")
  public Double getPositiveIndicatorCutoff()
  {
    return _positiveIndicatorCutoff;
  }

  /**
   * Set the indicator cutoff
   * @param indicatorCutoff the indicator cutoff
   */
  public void setPositiveIndicatorCutoff(Double indicatorCutoff)
  {
    _positiveIndicatorCutoff = indicatorCutoff;
  }

  /**
   * Get the indicator direction, which indicates whether a "positive" exists
   * based upon whether a numeric result value is above or below the indicator
   * cutoff.
   * @return an {@link PositiveIndicatorDirection} enum
   */
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorDirection$UserType")
  public PositiveIndicatorDirection getPositiveIndicatorDirection()
  {
    return _positiveIndicatorDirection;
  }

  /**
   * Set the indicator direction, which indicates whether a "positive" exists
   * based upon whether a numeric result value is above or below the indicator
   * cutoff.
   * @param indicatorDirection the indicator direction
   */
  public void setPositiveIndicatorDirection(PositiveIndicatorDirection indicatorDirection)
  {
    _positiveIndicatorDirection = indicatorDirection;
  }

  /**
   * Get whether this result value type is a positive indicator, meaning that
   * its result value data is used to determine whether a given well is deemed a
   * positive result in the screen.
   *
   * @return true iff this result value type is a positive indicator
   */
  @Column(nullable=false, updatable=false, name="isPositiveIndicator")
  @org.hibernate.annotations.Immutable
  public boolean isPositiveIndicator()
  {
    return _isPositiveIndicator;
  }

  /**
   * Get whether this result value type contains follow up data.
   * <p>
   * TODO: presumably generated during a subsequent library screening?
   * @return true iff this result value type contains follow up data
   */
  @Column(nullable=false, name="isFollowUpData")
  public boolean isFollowUpData()
  {
    return _isFollowUpData;
  }

  /**
   * Set whether this <code>ResultValueType</code> contains follow up data
   * <p>
   * TODO: presumably generated during a subsequent library screening?
   * @param isFollowUpData set to <code>true</code> iff this
   *          <code>ResultValueType</code> contains follow up data
   */
  public void setFollowUpData(boolean isFollowUpData)
  {
    _isFollowUpData = isFollowUpData;
  }

  /**
   * Get the name of this result value type.
   * @return the name of this result value type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name of this <code>ResultValueType</code>.
   * @param name the name of this <code>ResultValueType</code>
   */
  public void setName(String name)
  {
    _name = name;
  }

  /**
   * Get the time point, indicating the time interval, relative to the time the
   * assay plate was first prepared, at which the {@link ResultValue ResultValues} for this
   * <code>ResultValueType</code> were read. The format and units for the time point is arbitrary.
   * @return the time point
   */
  @org.hibernate.annotations.Type(type="text")
  public String getTimePoint()
  {
    return _timePoint;
  }

  /**
   * Set the time point, indicating the time interval, relative to the time the
   * assay plate was first prepared, at which the {@link ResultValue ResultValues} for this
   * <code>ResultValueType</code> were read. The format and units for the time point is arbitrary.
   * @param timePoint the time point
   */
  public void setTimePoint(String timePoint)
  {
    _timePoint = timePoint;
  }

  /**
   * Get whether this result value type is derived from other result value
   * types. Due to legacy screen result data, it is allowed that an RVT be
   * derived, but have an empty set of "derived from" RVTs.
   * 
   * @return true iff this result value type is derived from other result value
   *         types
   * @see #setDerived(boolean)
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #addDerivedTypeFrom(ResultValueType)
   * @see #removeDerivedTypeFrom(ResultValueType)
   * @see #addTypeDerivedFrom(ResultValueType)
   * @see #removeTypeDerivedFrom(ResultValueType)
   */
  @Column(nullable=false, name="isDerived")
  public boolean isDerived()
  {
    return _isDerived;
  }

  /**
   * Set whether this <code>ResultValueType</code> is derived from other
   * <code>ResultValueType</code>s. Due to legacy screen result data, it is
   * allowed that an RVT be derived, but have an empty set of "derived from"
   * RVTs.
   * 
   * @param isDerived <code>true</code> iff this <code>ResultValueType</code>
   *          is derived from other <code>ResultValueType</code>s.
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #addDerivedType(ResultValueType)
   * @see #removeDerivedType(ResultValueType)
   * @see #addTypeDerivedFrom(ResultValueType)
   * @see #removeTypeDerivedFrom(ResultValueType)
   */
  public void setDerived(boolean isDerived)
  {
    _isDerived = isDerived;
  }

  /**
   * Get the number of ResultValues that are positives, if this is an
   * ActivityIndicator ResultValueType.
   * @return the number of ResultValues that are positives, if this is an
   *         ActivityIndicator ResultValueType; otherwise null
   */
  @edu.harvard.med.screensaver.model.annotations.Column(
    hasNonconventionalSetterMethod=true,
    isNotEquivalenceProperty=true
  )
  public Integer getPositivesCount()
  {
    return _positivesCount;
  }

  /**
   * Get the ratio of the number of positives to the total number of experimental wells in the
   * screen result.
   * @return the ratio of the number of positives to the total number of experimental wells in the
   * screen result
   * @see #getPositivesCount()
   * @see ScreenResult#getExperimentalWellCount()
   */
  @Transient
  public Double getPositivesRatio()
  {
    if (_positivesCount != null) {
      return _positivesCount / (double) getScreenResult().getExperimentalWellCount();
    }
    return null;
  }

  /**
   * Get a mapping from the well ids to the result values for this result value type.
   * <p>
   * WARNING: obtaining an iterator on the returned Map will cause Hibernate
   * to load all ResultValues. If you want to take advantage of extra-lazy
   * loading, be sure to call only <code>size()</code> and <code>get(String wellId)</code>
   * on the returned Map.
   * <p>
   * WARNING: removing an element from this map is not supported; doing so
   * breaks ScreenResult.plateNumbers semantics.
   * @return a mapping from the wells to result values for this result value type
   * @motivation for hibernate
   */
  @OneToMany(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
             mappedBy="resultValueType")
  @org.hibernate.annotations.MapKey(columns={ @Column(name="well_id") })
  @OptimisticLock(excluded=true)
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE})
  public Map<WellKey,ResultValue> getResultValues()
  {
    return _resultValues;
  }


  // package constructor

  /**
   * Construct an initialized <code>ResultValueType</code>. Intended only for use by {@link
   * ScreenResult#createResultValueType(String)} and {@link
   * ScreenResult#createResultValueType(String, Integer, boolean, boolean, boolean, String)}.
   * @param screenResult the screen result
   * @param name the name of this result value type
   * @param replicateOrdinal the replicate ordinal
   * @param isDerived true iff this result value type is derived from other result value types
   * @param isPositiveIndicator true iff this result value type is a positive indicator
   * @param isFollowupData true iff this result value type contains follow up data
   * @param assayPhenotype the assay phenotype
   */
  ResultValueType(
    ScreenResult screenResult,
    String name,
    Integer replicateOrdinal,
    boolean isDerived,
    boolean isPositiveIndicator,
    boolean isFollowupData,
    String assayPhenotype)
  {
    if (screenResult == null) {
      throw new NullPointerException();
    }
    setScreenResult(screenResult);
    setOrdinal(getScreenResult().getResultValueTypes().size());
    setName(name);
    setReplicateOrdinal(replicateOrdinal);
    setDerived(isDerived);
    setPositiveIndicator(isPositiveIndicator);
    if (isPositiveIndicator) {
      _positivesCount = 0;
    }
    setFollowUpData(isFollowupData);
    setAssayPhenotype(assayPhenotype);
  }


  // protected constructor

  /**
   * Constructs an uninitialized <code>ResultValueType</code> object.
   * @motivation for Hibernate and proxy/concrete subclass constructors
   */
  protected ResultValueType() {}


  // private constructor and instance methods

  /**
   * Set the id for the result value type.
   * @param resultValueTypeId the new id for the result value type
   * @motivation for hibernate
   */
  private void setResultValueTypeId(Integer resultValueTypeId)
  {
    _resultValueTypeId = resultValueTypeId;
  }

  /**
   * Get the version number of the compound.
   * @return the version number of the <code>ResultValueType</code>
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version number of the <code>ResultValueType</code>
   * @param version the new version number for the <code>ResultValueType</code>
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the parent screen result.
   * @param screenResult the new parent screen result
   */
  private void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  /**
   * Set whether this <code>ResultValueType</code> is a positive indicator.
   * @param isActivityIndicator set to <code>true</code> iff this
   *          <code>ResultValueType</code> is a positive indicator
   */
  private void setPositiveIndicator(boolean isActivityIndicator)
  {
    _isPositiveIndicator = isActivityIndicator;
  }

  /**
   * Set the mapping from the well ids to the result values for this result value type.
   * @param resultValue the new mapping from the well ids to the result values for this result
   * value type
   * @motivation for hibernate
   */
  private void setResultValues(Map<WellKey,ResultValue> resultValues)
  {
    _resultValues = resultValues;
  }

  /**
   * Add a result value to the result value type. If the <code>value</code> parameter is not
   * null, then the <code>numericValue</code> parameter should be null, and the result value
   * to add is non-numeric. Otherwise, <code>numericValue</code> should be non-null,
   * <code>decimalPrecision</code> should contain a meaningful value, and the result value to
   * add is numeric.
   * @param well the well of the new ResultValue
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the value of the new ResultValue
   * @param numericValue the numeric value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal point, when value is displayed
   * @param exclude the exclude flag of the new ResultValue
   * @return a new ResultValue iff a result value did not already exist for the given well and result value type
   */
  private ResultValue createResultValue(
    Well well,
    AssayWellType assayWellType,
    String value,
    Double numericValue,
    Integer decimalPrecision,
    boolean exclude)
  {
    // TODO: this assert seems to contradict the statement below that null numericValues are allowed for numeric data
    assert (value == null) != (numericValue == null) :  "either numeric or non-numeric value";
    if (_resultValues.containsKey(well.getWellKey())) {
      return null;
    }
    // TODO: this fail to set isNumeric=true if first numeric value happens to be null (which is allowed);
    // we really shouldn't use data values to determine a RVT's numerical-ness
    if (!_isNumericalnessDetermined) {
      if (value == null && numericValue != null) {
        detemineNumericalness(true);
      }
      else if (value != null && numericValue == null) {
        detemineNumericalness(false);
      }
      else if (value == null && numericValue == null) {
        log.warn("setting " + this + ".isNumeric=false since both value and numericValue are null");
        detemineNumericalness(false);
      }
      else {
        throw new IllegalArgumentException("either value or numericValue must be null");
      }
    }
    else if (isNumeric() && value != null) {
      throw new DataModelViolationException("cannot add a non-numeric value to a numeric ResultValueType");
    }
    else if (! isNumeric() && numericValue != null) {
      throw new DataModelViolationException("cannot add a numeric value to a non-numeric ResultValueType");
    }

    ResultValue resultValue = new ResultValue(this,
                                              well,
                                              assayWellType,
                                              value,
                                              numericValue,
                                              decimalPrecision,
                                              exclude,
                                              false);

    if (getOrdinal() == 0) { // yuck! due to denormalization...
      if (resultValue.isExperimentalWell()) {
        getScreenResult().incrementExperimentalWellCount();
      }
    }

    if (isPositive(resultValue)) {
      incrementPositivesCount();
      resultValue.setPositive(true);
    }
    else {
      resultValue.setPositive(false);
    }

    getScreenResult().addWell(well);

    _resultValues.put(well.getWellKey(), resultValue);
    return resultValue;
  }

  /**
   * Set the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}.
   * @param ordinal the ordinal position of this <code>ResultValueType</code>
   * within its parent {@link ScreenResult}
   * @motivation for Hibernate
   */
  private void setOrdinal(Integer ordinal)
  {
    _ordinal = ordinal;
  }

  /**
   * Set the set of result value types that this result value type was derived from. The caller
   * of this method must ensure bi-directionality is preserved.
   * @param  the set of result value types that this result value type was derived from
   * @motivation for hibernate
   */
  private void setTypesDerivedFrom(SortedSet<ResultValueType> derivedFrom)
  {
    _typesDerivedFrom = derivedFrom;
  }

  /**
   * Set the set of result value types that derive from this result value type. The caller of
   * this method must ensure bi-directionality is preserved.
   * @param  the set of result value types that derive from this result value type
   * @motivation for hibernate
   */
  private void setDerivedTypes(SortedSet<ResultValueType> derivedTypes)
  {
    _derivedTypes = derivedTypes;
  }

  /**
   * Determine whether a result value is to be considered a positive, using its
   * value and this ResultValueType's definition of what constitutes a positive.
   * Only applicable for ResultValueTypes that are positive indicators.
   *
   * @param rv
   * @return true iff ResultValueType is a positive indicator, result
   *         value is for an experimental well, result value is not excluded,
   *         and the value of the result value meets the positive indicator type's
   *         criteria.
   */
  @Transient
  private boolean isPositive(ResultValue rv)
  {
    boolean isPositive = false;
    if (isPositiveIndicator() && rv.isExperimentalWell() && !rv.isExclude()) {
      if (getPositiveIndicatorType().equals(PositiveIndicatorType.BOOLEAN)) {
        if (Boolean.parseBoolean(rv.getValue())) {
          isPositive = true;
        }
      }
      else if (getPositiveIndicatorType().equals(PositiveIndicatorType.NUMERICAL)) {
        if (rv.getNumericValue() != null) {
          if (getPositiveIndicatorDirection().equals(PositiveIndicatorDirection.HIGH_VALUES_INDICATE)) {
            if (rv.getNumericValue() >= getPositiveIndicatorCutoff()) {
              isPositive = true;
            }
          }
          else {
            if (rv.getNumericValue() <= getPositiveIndicatorCutoff()) {
              isPositive = true;
            }
          }
        }
      }
      else if (getPositiveIndicatorType().equals(PositiveIndicatorType.PARTITION)) {
        String resultValue = rv.getValue();
        for (PartitionedValue pv : PartitionedValue.values()) {
          if (!pv.equals(PartitionedValue.NONE) && pv.getValue().equals(resultValue)) {
            isPositive = true;
            break;
          }
        }
      }
    }
    if (log.isDebugEnabled()) {
      if (isPositive) {
        log.debug("result value [well=" + rv.getWell() + ", value=" + rv.getValue() + ", exclude=" + rv.isExclude() + ", wellType=" + rv.getAssayWellType() + "] is a positive");
      }
    }
    return isPositive;
  }

  /**
   * Set the positives count.
   * @param positivesCount the new positives count
   * @motivation for Hibernate
   */
  private void setPositivesCount(Integer positivesCount)
  {
    _positivesCount = positivesCount;
  }

  /**
   * Increment the positives count.
   */
  private void incrementPositivesCount()
  {
    if (_positivesCount == null) {
      _positivesCount = new Integer(1);
    }
    else {
      ++_positivesCount;
    }
  }
}
