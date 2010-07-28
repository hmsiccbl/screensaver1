// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.OptimisticLock;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.ui.screenresults.MetaDataType;

/**
 * Provides the metadata for a subset of a
 * {@link edu.harvard.med.screensaver.model.screens.Screen Screen}'s
 * {@link ResultValue ResultValues}, all of which will have been produced "in the same
 * way". A <code>DataColumn</code> can describe either how a subset of
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
public class DataColumn extends AbstractEntity<Integer> implements MetaDataType, Comparable<DataColumn>
{
  // TODO: perhaps we should split DataColumn into subclasses, one for raw
  // data value descriptions and one for derived data descriptions?

  private static final Logger log = Logger.getLogger(DataColumn.class);
  private static final long serialVersionUID = -2325466055774432202L;

  public static final RelationshipPath<DataColumn> ScreenResult = RelationshipPath.from(DataColumn.class).to("screenResult", Cardinality.TO_ONE);
  public static final RelationshipPath<DataColumn> typesDerivedFrom = RelationshipPath.from(DataColumn.class).to("typesDerivedFrom");
  public static final RelationshipPath<DataColumn> derivedTypes = RelationshipPath.from(DataColumn.class).to("derivedTypes");
  
  private static final DataType DEFAULT_DATA_TYPE = DataType.NUMERIC; 
  private static final int DEFAULT_DECIMAL_PLACES = 3;
  public static Predicate<DataColumn> isPositiveIndicator = new Predicate<DataColumn>() {
    @Override
    public boolean apply(DataColumn dc)
    {
      return dc.isPositiveIndicator();
    }
  };
  
  private Integer _version;
  private ScreenResult _screenResult;
  private Collection<ResultValue> _resultValues = new ArrayList<ResultValue>();
  private String _name;
  private String _description;
  private Integer _ordinal;
  private Integer _replicateOrdinal;
  private AssayReadoutType _assayReadoutType;
  private String _timePoint;
  private boolean _isDerived;
  private String _howDerived;
  private SortedSet<DataColumn> _typesDerivedFrom = new TreeSet<DataColumn>();
  private SortedSet<DataColumn> _derivedTypes = new TreeSet<DataColumn>();
  private DataType _dataType;
  private Integer _decimalPlaces;
  private boolean _isFollowUpData;
  private String _assayPhenotype;
  private String _comments;
  private Integer _positivesCount;
  private Map<PartitionedValue,Integer> _partitionPositivesCounts = Maps.newHashMap();
  private Integer channel;
  private Integer timePointOrdinal;
  private Integer zdepthOrdinal;


  /**
   * Constructs an uninitialized <code>DataColumn</code> object.
   * @motivation for Hibernate and proxy/concrete subclass constructors
   */
  protected DataColumn() {}

  /**
   * Construct an initialized <code>DataColumn</code>. Intended only for use by {@link ScreenResult}.
   * @param screenResult the screen result
   * @param name the name of this data column
   */
  DataColumn(ScreenResult screenResult, String name)
  {
    if (screenResult == null) {
      throw new NullPointerException();
    }
    setScreenResult(screenResult);
    setName(name);
    setOrdinal(getScreenResult().getDataColumns().size());
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Defines natural ordering of <code>DataColumn</code> objects, based
   * upon their ordinal field value.
   */
  public int compareTo(DataColumn other)
  {
    int result = getScreenResult().getScreen().getScreenNumber().compareTo(other.getScreenResult().getScreen().getScreenNumber());
    if (result == 0) {
      result = getOrdinal().compareTo(other.getOrdinal());
    }
    return result;
  }

  /**
   * Get the id for the data column.
   * @return the id for the data column
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="data_column_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="data_column_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="data_column_id_seq")
  public Integer getDataColumnId()
  {
    return getEntityId();
  }

  /**
   * Get the parent {@link ScreenResult}.
   * @return the parent {@link ScreenResult}
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={})
  @JoinColumn(name="screenResultId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_data_column_to_screen_result")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  /**
   * Add a non-numeric, experimental type, non-excluded result value to the data column.
   * @return a new ResultValue iff a result value did not already exist for the given well and data column, otherwise null
   */
  public ResultValue createResultValue(AssayWell assayWell, String value)
  {
    return createResultValue(assayWell, value, false);
  }

  /**
   * Add a non-numeric result value to the data column.
   * @return a new ResultValue iff a result value did not already exist for the given well and data column, otherwise null
   */
  public ResultValue createResultValue(AssayWell assayWell,
                                       String value,
                                       Boolean exclude)
  {
    if (getDataType() != DataType.TEXT) {
      throw new DataModelViolationException("not a text data column");
    }
    return createResultValue(assayWell, value, null, null, null, exclude);
  }

  /**
   * Add a numeric, experimental type, non-excluded result value to the data column.
   * @return a new ResultValue iff a result value did not already exist for the given well and data column, otherwise null
   */
  public ResultValue createResultValue(AssayWell assayWell,
                                       Double numericValue)
  {
    return createResultValue(assayWell, numericValue, false);
  }

  /**
   * Add a numeric result value to the data column.
   * @return a new ResultValue iff a result value did not already exist for the given well and data column, otherwise null
   */
  public ResultValue createResultValue(AssayWell assayWell,
                                       Double numericValue,
                                       boolean exclude)
  {
    if (getDataType() != DataType.NUMERIC) {
      throw new DataModelViolationException("not a numeric data column");
    }
    return createResultValue(assayWell, null, numericValue, null, null, exclude);
  }
  
  public ResultValue createPartitionedPositiveResultValue(AssayWell assayWell,
                                                          PartitionedValue value,
                                                          boolean exclude)
  {
    if (getDataType() != DataType.POSITIVE_INDICATOR_PARTITION) {
      throw new DataModelViolationException("not a partition positive indicator data column");
    }
    return createResultValue(assayWell, null, null, value, null, exclude);
  }
  
  public ResultValue createBooleanPositiveResultValue(AssayWell assayWell,
                                                      Boolean value,
                                                      boolean exclude)
  {
    if (getDataType() != DataType.POSITIVE_INDICATOR_BOOLEAN) {
      throw new DataModelViolationException("not a boolean positive indicator data column");
    }
    return createResultValue(assayWell, null, null, null, value, exclude);
  }

  @Column(nullable=false, updatable=false)
  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses make*() builder methods*/)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screenresults.DataType$UserType")
  public DataType getDataType()
  {
    if (_dataType == null) {
      _dataType = DEFAULT_DATA_TYPE;
    }
    return _dataType;
  }

  private void setDataType(DataType dataType)
  {
    if (isHibernateCaller()) {
      _dataType = dataType;
      return;
    }
    
    if (_dataType != null && _dataType != dataType) {
      throw new DataModelViolationException("data type is already set and cannot be changed");
    }
    _dataType = dataType;
    if (dataType.isPositiveIndicator()) {
      _positivesCount = Integer.valueOf(0);
      if (dataType == DataType.POSITIVE_INDICATOR_PARTITION) {
        _partitionPositivesCounts.put(PartitionedValue.STRONG, 0);
        _partitionPositivesCounts.put(PartitionedValue.MEDIUM, 0);
        _partitionPositivesCounts.put(PartitionedValue.WEAK, 0);
      }
    }
  }
  
  public DataColumn forReplicate(Integer replicateOrdinal)
  {
    setReplicateOrdinal(replicateOrdinal);
    return this;
  }

  public DataColumn makeTextual()
  {
    setDataType(DataType.TEXT);
    return this;
  }

  /**
   * @param decimalPlaces the number of decimal places to be shown when
   *          displaying the values of this column (this affect display only;
   *          the full precision of the values are always stored); if null or
   *          negative, the values are displayed to their full precision
   */
  public DataColumn makeNumeric(Integer decimalPlaces)
  {
    setDataType(DataType.NUMERIC);
    _decimalPlaces = decimalPlaces;
    return this;
  }
  
  public DataColumn makeBooleanPositiveIndicator()
  {
    setDataType(DataType.POSITIVE_INDICATOR_BOOLEAN);
    return this;
  }
  
  public DataColumn makePartitionPositiveIndicator()
  {
    setDataType(DataType.POSITIVE_INDICATOR_PARTITION);
    return this;
  }
  
  public DataColumn makeDerived(String howDerived, Set<DataColumn> derivedFrom)
  {
    setDerived(true);
    setHowDerived(howDerived);
    for (DataColumn col : derivedFrom) {
      addTypeDerivedFrom(col);;
    }
    return this;
  }

  public DataColumn forChannel(Integer channel)
  {
    setChannel(channel);
    return this;
  }
  
  public DataColumn forTimePoint(String timepoint)
  {
    setTimePoint(timepoint);
    return this;
  }
  
  public DataColumn forTimePointOrdinal(Integer timepointOrdinal)
  {
    setTimePointOrdinal(timePointOrdinal);
    return this;
  }
  
  public DataColumn forZdepthOrdinal(Integer zdepthOrdinal)
  {
    setZdepthOrdinal(zdepthOrdinal);
    return this;
  }
  
  public DataColumn forPhenotype(String phenotype)
  {
    setAssayPhenotype(phenotype);
    return this;
  }

  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses makeNumeric() builder method*/)
  public Integer getDecimalPlaces()
  {
    return _decimalPlaces;
  }
  
  private void setDecimalPlaces(Integer decimalPlaces)
  {
    _decimalPlaces = decimalPlaces;
  }

  /**
   * Return true iff this data column contains numeric result values.
   * @return true iff this data column contains numeric result values
   */
  @Transient
  public boolean isNumeric()
  {
    return getDataType() == DataType.NUMERIC;
  }

  /**
   * Get the ordinal position of this <code>DataColumn</code> within its
   * parent {@link ScreenResult}. This ordering is really only significant from
   * the standpoint of presenting a {@link ScreenResult} to the user
   * (historically speaking, it reflects the ordering found during spreadsheet
   * file import).
   * 
   * @return the ordinal position of this <code>DataColumn</code> within
   *         its parent {@link ScreenResult}
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  public Integer getOrdinal()
  {
    return _ordinal;
  }

  /**
   * Get the replicate ordinal, a 1-based index indicating the assay replicate
   * that produced this DataColumn's data.
   * 
   * @return the replicate ordinal. May be null if assay replicates were not
   *         produced.
   */
  // TODO: remove 'Ordinal' suffix, or replace with 'Number' (mathematically, ordinal includes the value 0)
  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses forReplicate() builder method*/)
  public Integer getReplicateOrdinal()
  {
    return _replicateOrdinal;
  }

  /**
   * Set the replicate ordinal, a 1-based index indicating the assay replicate
   * that produced this DataColumn's data. 
   * 
   * @param replicateOrdinal the replicate ordinal. May be null if assay replicates
   * were not produced.
   */
  private void setReplicateOrdinal(Integer replicateOrdinal)
  {
    assert replicateOrdinal == null || replicateOrdinal > 0 : "replicate ordinal values must be positive (non-zero), unless null";
    _replicateOrdinal = replicateOrdinal;
  }

  /**
   * Get the Assay Phenotype.
   * @return a <code>String</code> representing the Assay Phenotype
   */
  @org.hibernate.annotations.Type(type="text")
  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses forPhenotype() builder method*/)
  public String getAssayPhenotype()
  {
    return _assayPhenotype;
  }

  /**
   * Set the Assay Phenotype.
   * @param assayPhenotype the Assay Phenotype to set
   */
  private void setAssayPhenotype(String assayPhenotype)
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
   * Get the set of data columns that this data column was derived from.
   * By "derived", we mean that the calculated values of our {@link ResultValue}s depend upon
   * the {@link ResultValue}s of other {@link DataColumn}s
   * (of the same stock plate well). The details of the derivation should be specified via
   * {@link #setHowDerived}.
   * @return the set of data columns that this data column was derived from
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(
    name="dataColumnDerivedFromLink",
    joinColumns=@JoinColumn(name="derivedFromDataColumnId"),
    inverseJoinColumns=@JoinColumn(name="derivedDataColumnId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_derived_from_data_column")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ToMany(inverseProperty="derivedTypes", singularPropertyName="typeDerivedFrom")
  public SortedSet<DataColumn> getTypesDerivedFrom()
  {
    return _typesDerivedFrom;
  }

  /**
   * Add the data column to the types derived from. Updates the 'derived'
   * property and 'derived types' collection accordingly.
   * 
   * @param typeDerivedFrom the data column to add
   * @return true iff the data column was not already contained in the set
   *         of types derived from this type
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(DataColumn)
   * @see #removeDerivedType(DataColumn)
   * @see #removeTypeDerivedFrom(DataColumn)
   */
  public boolean addTypeDerivedFrom(DataColumn typeDerivedFrom) {
    assert !(typeDerivedFrom.getDerivedTypes().contains(this) ^ getTypesDerivedFrom().contains(typeDerivedFrom)) :
      "asymmetric types derived from / derived types association encountered";
    if (getTypesDerivedFrom().add(typeDerivedFrom)) {
      setDerived(true);
      return typeDerivedFrom.getDerivedTypes().add(this);
    }
    return false;
  }

  /**
   * Remove the data column from the types derived from. Updates the
   * 'derived' property and 'derived types' collection accordingly.
   * 
   * @param typeDerivedFrom the data column to remove
   * @return true iff the data column was previously contained in the set
   *         of types derived from this type
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(DataColumn)
   * @see #removeDerivedType(DataColumn)
   * @see #addTypeDerivedFrom(DataColumn)
   */
  public boolean removeTypeDerivedFrom(DataColumn typeDerivedFrom) {
    assert ! (typeDerivedFrom.getDerivedTypes().contains(this) ^ getTypesDerivedFrom().contains(typeDerivedFrom)) :
      "asymmetric types derived from / derived types association encountered";
    if (getTypesDerivedFrom().remove(typeDerivedFrom)) {
      setDerived(! getTypesDerivedFrom().isEmpty());
      return typeDerivedFrom.getDerivedTypes().remove(this);
    }
    return false;
  }

  /**
   * Get the set of data columns that derive from this data column.
   * @return the set of data columns that derive from this data column
   * @see #isDerived()
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(DataColumn)
   * @see #addTypeDerivedFrom(DataColumn)
   * @see #removeTypeDerivedFrom(DataColumn)
   */
  @ManyToMany(
    mappedBy="typesDerivedFrom",
    targetEntity=DataColumn.class,
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.ForeignKey(name="fk_derived_data_column")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ToMany(inverseProperty="typesDerivedFrom")
  public SortedSet<DataColumn> getDerivedTypes()
  {
    return _derivedTypes;
  }

  /**
   * Add the data column to the derived types. Updates the 'derived'
   * property and 'types derived from' collection accordingly.
   * 
   * @param derivedType the data column to add
   * @return true iff the data column was not already contained in the set
   *         of derived types
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #makeDerived(String, Set)
   * @see #removeDerivedType(DataColumn)
   * @see #addTypeDerivedFrom(DataColumn)
   * @see #removeTypeDerivedFrom(DataColumn)
   */
  public boolean addDerivedType(DataColumn derivedType) {
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
   * Remove the data column from the derived types. Updates the 'derived'
   * property and 'types derived from' collection accordingly.
   * 
   * @param derivedType the data column to remove
   * @return true iff the data column was previously contained in the set
   *         of derived types
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #setDerived(boolean)
   * @see #addDerivedType(DataColumn)
   * @see #addTypeDerivedFrom(DataColumn)
   * @see #removeTypeDerivedFrom(DataColumn)
   */
  public boolean removeDerivedType(DataColumn derivedType) {
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
   * Get a description of this <code>DataColumn</code>.
   * @return a <code>String</code> description of this <code>DataColumn</code>
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set a description of this <code>DataColumn</code>.
   * @param description the new description of this <code>DataColumn</code>
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * Get then description of how this <code>DataColumn</code> was derived
   * from other <code>DataColumn</code>s.
   * @return a <code>String</code> description of how this
   *         <code>DataColumn</code> was derived from other
   *         <code>DataColumn</code>s
   */
  @org.hibernate.annotations.Type(type="text")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /* uses makeDerived() instead */) 
  public String getHowDerived()
  {
    return _howDerived;
  }

  /**
   * Set the description of how this <code>DataColumn</code> was derived
   * from other <code>DataColumn</code>s.
   * @param howDerived a description of how this <code>DataColumn</code>
   *          was derived from other <code>DataColumn</code>s
   */
  private void setHowDerived(String howDerived)
  {
    _howDerived = howDerived;
  }

  /**
   * Get whether this data column is a positive indicator, meaning that
   * its result value data is used to determine whether a given well is deemed a
   * positive result in the screen.
   *
   * @return true iff this data column is a positive indicator
   */
  @Transient
  public boolean isPositiveIndicator()
  {
    return getDataType().isPositiveIndicator();
  }

  @Transient
  public boolean isPartitionPositiveIndicator()
  {
    return getDataType() == DataType.POSITIVE_INDICATOR_PARTITION;
  }

  @Transient
  public boolean isBooleanPositiveIndicator()
  {
    return getDataType() == DataType.POSITIVE_INDICATOR_BOOLEAN;
  }

  /**
   * Get whether this data column contains follow up data.
   * <p>
   * TODO: presumably generated during a subsequent library screening?
   * @return true iff this data column contains follow up data
   */
  @Column(nullable=false, name="isFollowUpData")
  public boolean isFollowUpData()
  {
    return _isFollowUpData;
  }

  /**
   * Set whether this <code>DataColumn</code> contains follow up data
   * <p>
   * TODO: presumably generated during a subsequent library screening?
   * @param isFollowUpData set to <code>true</code> iff this
   *          <code>DataColumn</code> contains follow up data
   */
  public void setFollowUpData(boolean isFollowUpData)
  {
    _isFollowUpData = isFollowUpData;
  }

  /**
   * Get the name of this data column.
   * @return the name of this data column
   */
  @Column(nullable=false, updatable=false)
  @Immutable
  @org.hibernate.annotations.Type(type="text")
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name of this <code>DataColumn</code>.
   * @param name the name of this <code>DataColumn</code>
   */
  private void setName(String name)
  {
    _name = name;
  }

  /**
   * Get the time point, indicating the time interval, relative to the time the
   * assay plate was first prepared, at which the {@link ResultValue ResultValues} for this
   * <code>DataColumn</code> were read. The format and units for the time point is arbitrary.
   * @return the time point
   */
  @org.hibernate.annotations.Type(type="text")
  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses forTimePoint() instead*/)
  public String getTimePoint()
  {
    return _timePoint;
  }

  /**
   * Set the time point, indicating the time interval, relative to the time the
   * assay plate was first prepared, at which the {@link ResultValue ResultValues} for this
   * <code>DataColumn</code> were read. The format and units for the time point is arbitrary.
   * @param timePoint the time point
   */
  private void setTimePoint(String timePoint)
  {
    _timePoint = timePoint;
  }

  /**
   * Get whether this data column is derived from other result value
   * types. Due to legacy screen result data, it is allowed that an DataColumn be
   * derived, but have an empty set of "derived from" DataColumns.
   * 
   * @return true iff this data column is derived from other result value
   *         types
   * @see #setDerived(boolean)
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #addDerivedType(DataColumn)
   * @see #removeDerivedType(DataColumn)
   * @see #addTypeDerivedFrom(DataColumn)
   * @see #removeTypeDerivedFrom(DataColumn)
   */
  @Column(nullable=false, name="isDerived")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /* uses makeDerived() instead */) 
  public boolean isDerived()
  {
    return _isDerived;
  }

  /**
   * Set whether this <code>DataColumn</code> is derived from other
   * <code>DataColumn</code>s. Due to legacy screen result data, it is
   * allowed that an DataColumn be derived, but have an empty set of "derived from"
   * DataColumns.
   * 
   * @param isDerived <code>true</code> iff this <code>DataColumn</code>
   *          is derived from other <code>DataColumn</code>s.
   * @see #isDerived
   * @see #getDerivedTypes()
   * @see #getTypesDerivedFrom()
   * @see #addDerivedType(DataColumn)
   * @see #removeDerivedType(DataColumn)
   * @see #addTypeDerivedFrom(DataColumn)
   * @see #removeTypeDerivedFrom(DataColumn)
   */
  private void setDerived(boolean isDerived)
  {
    _isDerived = isDerived;
  }

  /**
   * @return the number of ResultValues that are positives, if this is an
   *         PositiveIndicator DataColumn; otherwise null
   */
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true, isNotEquivalenceProperty=true)
  public Integer getPositivesCount()
  {
    return _positivesCount;
  }

  /**
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

  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true, isNotEquivalenceProperty=true)
  public Integer getStrongPositivesCount()
  {
    return _partitionPositivesCounts.get(PartitionedValue.STRONG);
  }

  private void setStrongPositivesCount(Integer count)
  {
    _partitionPositivesCounts.put(PartitionedValue.STRONG, count);
  }

  @Transient
  public Double getStrongPositivesRatio()
  {
    if (getStrongPositivesCount() != null) {
      return getStrongPositivesCount() / (double) getScreenResult().getExperimentalWellCount();
    }
    return null;
  }

  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true, isNotEquivalenceProperty=true)
  public Integer getMediumPositivesCount()
  {
    return _partitionPositivesCounts.get(PartitionedValue.MEDIUM);
  }
  
  private void setMediumPositivesCount(Integer count)
  {
    _partitionPositivesCounts.put(PartitionedValue.MEDIUM, count);
  }

  @Transient
  public Double getMediumPositivesRatio()
  {
    if (getMediumPositivesCount() != null) {
      return getMediumPositivesCount() / (double) getScreenResult().getExperimentalWellCount();
    }
    return null;
  }

  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true, isNotEquivalenceProperty=true)
  public Integer getWeakPositivesCount()
  {
    return _partitionPositivesCounts.get(PartitionedValue.WEAK);
  }

  private void setWeakPositivesCount(Integer count)
  {
    _partitionPositivesCounts.put(PartitionedValue.WEAK, count);
  }

  @Transient
  public Double getWeakPositivesRatio()
  {
    if (getWeakPositivesCount() != null) {
      return getWeakPositivesCount() / (double) getScreenResult().getExperimentalWellCount();
    }
    return null;
  }

  /**
   * Get the result values for this data column.
   * <p>
   * WARNING: obtaining an iterator on the returned collection will cause Hibernate
   * to load all of the ResultValues for this DataColumn, which may be very large!
   * <p>
   * WARNING: removing an element from this map is not supported
   * @return a Collection of the result values for this data column
   * @motivation for hibernate
   */
  @OneToMany(fetch=FetchType.LAZY, mappedBy="dataColumn")
  @org.hibernate.annotations.Cascade({ org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.PERSIST })
  @OptimisticLock(excluded=true)
  public Collection<ResultValue> getResultValues()
  {
    return _resultValues;
  }
  
  public void clearResultValues()
  {
    _resultValues.clear();
  }
  
  /**
   * @deprecated Use HQL to retrieve <code>DataColumn.resultValues</code>,
   *             as needed. Not performant for large collection of ResultValues,
   *             and has excessive memory requirements. For legacy code only.
   */
  @Transient
  @Deprecated
  public Map<WellKey,ResultValue> getWellKeyToResultValueMap()
  {
    HashMap<WellKey,ResultValue> map = new HashMap<WellKey,ResultValue>(_resultValues.size());  
    for (ResultValue resultValue : _resultValues) {
      map.put(resultValue.getWell().getWellKey(), resultValue);
    }
    return map;
  }

  /**
   * Set the id for the data column.
   * @param dataColumnId the new id for the data column
   * @motivation for hibernate
   */
  private void setDataColumnId(Integer dataColumnId)
  {
    setEntityId(dataColumnId);
  }

  /**
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version number of the <code>DataColumn</code>
   * @param version the new version number for the <code>DataColumn</code>
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
   * Set the result values for this data column.
   * 
   * @param resultValue the new result values for this data column
   * @motivation for hibernate
   */
  private void setResultValues(Collection<ResultValue> resultValues)
  {
    _resultValues = resultValues;
  }

  /**
   * Add a result value to the data column. If the <code>value</code> parameter is not
   * null, then the <code>numericValue</code> parameter should be null, and the result value
   * to add is non-numeric. Otherwise, <code>numericValue</code> should be non-null.
   * @param assayWell the assayWell of the new ResultValue
   * @param value the value of the new ResultValue
   * @param numericValue the numeric value of the new ResultValue
   * @param exclude the exclude flag of the new ResultValue
   * @return a new ResultValue iff a result value did not already exist for the given well and data column
   */
  private ResultValue createResultValue(AssayWell assayWell,
                                        String value,
                                        Double numericValue,
                                        PartitionedValue partitionPositiveIndicatorValue,
                                        Boolean booleanPositiveIndicatorValue,
                                        boolean exclude)
  {
    ResultValue resultValue = new ResultValue(this,
                                              assayWell,
                                              value,
                                              numericValue,
                                              partitionPositiveIndicatorValue,
                                              booleanPositiveIndicatorValue,
                                              exclude);

    if (getOrdinal() == 0) { // yuck! due to denormalization... // TODO: should move to AssayWell constructor
      if (assayWell.getLibraryWell().getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
        getScreenResult().incrementExperimentalWellCount();
      }
    }

    if (resultValue.isPositive()) {
      incrementPositivesCount(resultValue);
      assayWell.setPositive(true);
    }

    _resultValues.add(resultValue);
    
    return resultValue;
  }

  /**
   * @motivation for Hibernate
   */
  private void setOrdinal(Integer ordinal)
  {
    _ordinal = ordinal;
  }

  /**
   * Set the set of data columns that this data column was derived from. The caller
   * of this method must ensure bi-directionality is preserved.
   * @param  the set of data columns that this data column was derived from
   * @motivation for hibernate
   */
  private void setTypesDerivedFrom(SortedSet<DataColumn> derivedFrom)
  {
    _typesDerivedFrom = derivedFrom;
  }

  /**
   * Set the set of data columns that derive from this data column. The caller of
   * this method must ensure bi-directionality is preserved.
   * @param  the set of data columns that derive from this data column
   * @motivation for hibernate
   */
  private void setDerivedTypes(SortedSet<DataColumn> derivedTypes)
  {
    _derivedTypes = derivedTypes;
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
  private void incrementPositivesCount(ResultValue resultValue)
  {
    if (resultValue.isPositive()) {
      if (_positivesCount == null) {
        _positivesCount = 1;
      }
      else {
        ++_positivesCount;
      }
      if (getDataType() == DataType.POSITIVE_INDICATOR_PARTITION) {
        PartitionedValue pv = (PartitionedValue) resultValue.getTypedValue();
        if (_partitionPositivesCounts.get(pv) == null) {
          _partitionPositivesCounts.put(pv, 1);
        }
        else {
          _partitionPositivesCounts.put(pv, _partitionPositivesCounts.get(pv) + 1);
        }
      }
    }
  }

  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses forChannel() builder method*/)
  public Integer getChannel()
  {
    return channel;
  }

  private void setChannel(Integer channel)
  {
    this.channel = channel;
  }

  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses forTimePointOrdinal() builder method*/)
  public Integer getTimePointOrdinal()
  {
    return timePointOrdinal;
  }

  private void setTimePointOrdinal(Integer timePointOrdinal)
  {
    this.timePointOrdinal = timePointOrdinal;
  }

  @Immutable
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true /*uses forZdepthOrdinal() builder method*/)
  public Integer getZdepthOrdinal()
  {
    return zdepthOrdinal;
  }

  private void setZdepthOrdinal(Integer zdepthOrdinal)
  {
    this.zdepthOrdinal = zdepthOrdinal;
  }
}
