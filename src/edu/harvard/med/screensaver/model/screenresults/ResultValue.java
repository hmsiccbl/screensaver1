// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/src/edu/harvard/med/screensaver/model/screenresults/ResultValue.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;


/**
 * A <code>ResultValue</code> holds the value of a screen result data point for
 * a given {@link DataColumn}, and {@link AssayWell}. For text-based
 * DataColumns, the value is stored canonically as a String. For numeric
 * DataColumns, the value is stored canonically as a double, allowing for
 * efficient sorting and filtering of numeric values in the database. Note that
 * the parent {@link DataColumn#isNumeric()()} contains an "isNumeric" property
 * that indicates whether its member ResultValues are numeric (the isNumeric
 * flag is not stored with each ResultValue for space efficiency).
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=DataColumn.class)
@org.hibernate.annotations.Table(appliesTo = "result_value",
                                 indexes={ @Index(name = "result_value_data_column_and_value_index", columnNames={ "dataColumnId", "value" }),
                                           @Index(name = "result_value_data_column_and_numeric_value_index", columnNames={ "dataColumnId", "numericValue" }),
                                           @Index(name = "result_value_data_column_and_positive_index", columnNames={ "dataColumnId", "isPositive" }) })
public class ResultValue extends AbstractEntity<Integer>
{

  // private static data

  private static final long serialVersionUID = -4066041317098744417L;
  private static final Logger log = Logger.getLogger(ResultValue.class);

  public static final RelationshipPath<ResultValue> DataColumn = new RelationshipPath<ResultValue>(ResultValue.class, "dataColumn");


  /**
   * Returns the value of this <code>ResultValue</code> as an appropriately
   * typed object, depending upon {@link DataColumn#isPositiveIndicator()},
   * {@link DataColumn#isDerived()()}, and
   * {@link DataColumn#getPositiveIndicatorType()}, as follows:
   * <ul>
   * <li> Well type is non-data-producer: returns <code>null</code>
   * <li> Not Derived (Raw): returns Double
   * <li> Not an Activity Indicator: returns String
   * <li> DataType.BOOLEAN: returns Boolean
   * <li> DataType.PARTITION: returns String
   * (PartitionedValue.getDisplayValue())
   * </ul>
   *
   * @return a Boolean, Double, or String
   * @motivation to preserve typed data in exported Workbooks (rather than treat
   *             all result values as text strings)
   */
  @Transient
  public Object getTypedValue()
  {
    if (isNull()) {
      return null;
    }

    switch (getDataColumn().getDataType()) {
    case NUMERIC: return getNumericValue();
    case POSITIVE_INDICATOR_BOOLEAN: return Boolean.valueOf(getValue());
    case POSITIVE_INDICATOR_PARTITION: return PartitionedValue.lookupByValue(getValue()).getDisplayValue();
    default: return getValue();
    }
  }


  // private instance data

  private Well _well;
  private DataColumn _dataColumn;
  private String _value;
  private Double _numericValue;
  private AssayWellType _assayWellType;
  /**
   * Note that we maintain an "exclude" flag on a per-ResultValue basis. It is
   * up to the application code and/or user interface to manage excluding the
   * full set of ResultValues associated with a stock plate well (row) or with a
   * data column. But we do need to allow any arbitrary set of
   * ResultValues to be excluded.
   */
  private boolean _isExclude;
  private boolean _isPositive;


  // public constructors

  /**
   * Constructs a <code>ResultValue</code>.
   * @param dataColumn the parent DataColumn
   * @param well the well of this ResultValue
   * @param value the non-numerical value of the ResultValue
  */
  ResultValue(DataColumn dataColumn,
              AssayWell assayWell,
              String value)
  {
    this(dataColumn, assayWell, value, null, false, false);
  }

  /**
   * Constructs a numeric ResultValue object
   *
   * @param dataColumn the parent DataColumn
   * @param well the well of this ResultValue
   * @param numericValue the numerical value of the ResultValue
   */
  ResultValue(DataColumn dataColumn,
              AssayWell assayWell,
              Double numericValue)
  {
    this(dataColumn, assayWell, null, numericValue, false, false);
  }

  /**
   * Construct a numerical <code>ResultValue</code>. Intended for use only
   * for creating result values that will not need to be persisted.
   *
   * @param dataColumn the parent DataColumn
   * @param well the well of this ResultValue
   * @param assayWellType the AssayWellType of the ResultValue
   * @param numericalValue the numerical value of the ResultValue
   * @param exclude whether this ResultValue is to be (or was) ignored when performing analysis for the determination of positives 
   * @param isPositive whether this ResultValue is considered a 'positive' result 
   */
  public ResultValue(DataColumn dataColumn,
                     AssayWell assayWell,
                     Double numericalValue,
                     boolean exclude,
                     boolean isPositive)
  {
    this(dataColumn, assayWell, null, numericalValue, exclude, isPositive);
  }

  /**
   * Construct a non-numerical <code>ResultValue</code>. Intended for use
   * only for creating result values that will not need to be persisted.
   *
   * @param dataColumn the parent DataColumn
   * @param well the well of this ResultValue
   * @param assayWellType the AssayWellType of the ResultValue
   * @param value the non-numerical value of the ResultValue
   * @param exclude whether this ResultValue is to be (or was) ignored when performing analysis for the determination of positives 
   * @param isPositive whether this ResultValue is considered a 'positive' result 
   */
  public ResultValue(DataColumn dataColumn,
                     AssayWell assayWell,
                     String value,
                     boolean exclude,
                     boolean isPositive)
  {
    this(dataColumn, assayWell, value, null, exclude, isPositive);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="result_value_id_seq",
    strategy="seqhilo",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="result_value_id_seq"),
      @org.hibernate.annotations.Parameter(name="max_lo", value="384")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="result_value_id_seq")
  public Integer getResultValueId()
  {
    return getEntityId();
  }

  /**
   * Get the data column.
   * @return the data column
   */
  @ManyToOne(cascade={}, fetch=FetchType.LAZY)
  @JoinColumn(name="dataColumnId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_result_value_to_data_column")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @Index(name="result_value_data_column_index")
  public DataColumn getDataColumn()
  {
    return _dataColumn;
  }

  /**
   * Get the well.
   * @return the well
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="well_id", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_result_value_to_well")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @Index(name="result_value_well_index")
  public Well getWell()
  {
    return _well;
  }

  /**
   * Get the string value of this <code>ResultValue</code>.
   *
   * @return a {@link java.lang.String} representing the string value of this
   *         <code>ResultValue</code>; may return null.
   */
  @org.hibernate.annotations.Type(type="text")
  //@Index(name="result_value_value_index")
  public String getValue()
  {
    return _value;
  }

  /**
   * Return true whenever this result value has a null value.
   *
   * @return true whenever this result value has a null value
   * @motivation reduces confusion as to whether callers needs to check both
   *             {@link #getValue()} and {@link #getNumericValue()} to determine
   *             if ResultValue is null
   */
  @Transient
  public boolean isNull()
  {
    return _value == null;
  }

  /**
   * Get the numeric value of this <code>ResultValue</code>.
   *
   * @return a {@link java.lang.Double} representing the numeric value of this
   *         <code>ResultValue</code>; may return null.
   */
  //@Index(name="result_value_numeric_value_index")
  public Double getNumericValue()
  {
    return _numericValue;
  }

  /**
   * Get the assay well's type.
   *
   * @return the assay well's type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screenresults.AssayWellType$UserType")
  public AssayWellType getAssayWellType()
  {
    return _assayWellType;
  }

  /**
   * Get whether this <code>ResultValue</code> is to be excluded in any
   * subsequent analyses.
   *
   * @return <code>true</code> iff this <code>ResultValue</code> is to be
   *         excluded in any subsequent analysis
   */
  @Column(nullable=false, name="isExclude")
  public boolean isExclude()
  {
    return _isExclude;
  }

  /**
   * Get whether this result value indicates a positive. Returns false if the
   * {@link #getDataColumn() DataColumn} is not a positive indicator.
   *
   * @return true whenever this result value is a positive indicator
   */
  @Column(nullable=false, name="isPositive")
  @org.hibernate.annotations.Index(name="result_value_is_positive_index")
  public boolean isPositive()
  {
    return _isPositive;
  }

  /**
   * Return true iff the assay well type is
   * {@link AssayWellType#EXPERIMENTAL experimental}.
   *
   * @return true iff the assay well type is experimental
   * @see AssayWellType#EXPERIMENTAL
   */
  @Transient
  public boolean isExperimentalWell()
  {
    return getAssayWellType().equals(AssayWellType.EXPERIMENTAL);
  }

  /**
   * Return true iff the assay well type is a control.
   *
   * @return true iff the assay well type is a control
   * @see AssayWellType#isControl()
   */
  @Transient
  public boolean isControlWell()
  {
    return getAssayWellType().isControl();
  }

  @Transient
  public boolean isEdgeWell()
  {
    return getWell().isEdgeWell();
  }

  /**
   * Return true iff the assay well type is data producing.
   *
   * @return true iff the assay well type is data producing
   * @see AssayWellType#isDataProducing()
   */
  @Transient
  public boolean isDataProducerWell()
  {
    return getAssayWellType().isDataProducing();
  }

  /**
   * Return true iff the assay well type is {@link AssayWellType#OTHER other}.
   *
   * @return true iff the assay well type is other
   * @see AssayWellType#OTHER
   */
  @Transient
  public boolean isOtherWell()
  {
    return getAssayWellType().equals(AssayWellType.OTHER);
  }

  /**
   * Return true iff the assay well type is {@link AssayWellType#EMPTY empty}.
   *
   * @return true iff the assay well type is empty
   * @see AssayWellType#EMPTY
   */
  @Transient
  public boolean isEmptyWell()
  {
    return getAssayWellType().equals(AssayWellType.EMPTY);
  }


  // package constructor and instance method

  /**
   * Construct an initialized <code>ResultValue</code>. Intended only for use by
   * this class's constructors and {@link DataColumn}.
   * 
   * @param dataColumn the parent DataColumn
   * @param well the well of this ResultValue
   * @param assayWellType the AssayWellType of the ResultValue
   * @param value the non-numerical value of the ResultValue
   * @param numericalValue the numerical value of the ResultValue
   * @param exclude whether this ResultValue is to be (or was) ignored when performing analysis for the determination of positives 
   * @param isPositive whether this ResultValue is considered a 'positive' result 
   */
  ResultValue(DataColumn dataColumn,
              AssayWell assayWell,
              String value,
              Double numericalValue,
              boolean exclude,
              boolean isPositive)
  {
    if (dataColumn == null) {
      throw new DataModelViolationException("dataColumn is required for ResultValue");
    }
    if (assayWell == null) {
      throw new DataModelViolationException("assay well is required for ResultValue");
    }
    _dataColumn = dataColumn;
    _well = assayWell.getLibraryWell(); // TODO: remove

    // TODO: HACK: removing this update as it causes memory/performance
    // problems when loading ScreenResults; fortunately, when ScreenResult is
    // read in from database from a new Hibernate session, the in-memory
    // associations will be correct; these in-memory associations will only be
    // missing within the Hibernate session that was used to import the
    // ScreenResult
    // _well.getResultValues().put(dataColumn, this);

    setAssayWellType(assayWell.getAssayWellType()); // TODO: remove
    if (!!!dataColumn.isNumeric()) {
      setValue(value);
    }
    else {
      setNumericValue(numericalValue);
    }
    setExclude(exclude);
    setPositive(isPositive);
  }


  // package-protected methods

  /**
   * Set whether this result value is a positive. Intended only for use by
   * hibernate and {@link DataColumn}.
   * 
   * @param isPositive true iff this result value is a positive
   * @motivation for hibernate and DataColumn
   */
  void setPositive(boolean isPositive)
  {
    _isPositive = isPositive;
  }


  // private constructor and instance methods

  /**
   * Constructs an uninitialized ResultValue object.
   *
   * @motivation for hibernate
   */
  private ResultValue() {}

  /**
   * Set the id for the result value.
   * @param resultValueId the new id for the result value
   * @motivation for hibernate
   */
  private void setResultValueId(Integer resultValueId)
  {
    setEntityId(resultValueId);
  }

  /**
   * Set the well.
   *
   * @param well the new well
   * @motivation for hibernate
   */
  private void setWell(Well well)
  {
    _well = well;
  }

  /**
   * Set the data column.
   *
   * @param dataColumn the new data column
   * @motivation for hibernate
   */
  private void setDataColumn(DataColumn dataColumn)
  {
    _dataColumn = dataColumn;
  }

  /**
   * Set the actual value of this result value.
   *
   * @param value the new value of this result value
   * @motivation for hibernate
   */
  private void setValue(String value)
  {
    _value = value;
  }

  /**
   * Set the numerical value of this result value
   *
   * @param value the new numerical value
   * @motivation for hibernate
   */
  private void setNumericValue(Double value)
  {
    _numericValue = value;
  }

  /**
   * Set whether the screener has deemed that this <code>ResultValue</code>
   * should be excluded in any subsequent analyses.
   *
   * @param exclude set to <code>true</code> iff this <code>ResultValue</code>
   *          is to be excluded in any subsequent analysis
   * @motivation for hibernate
   */
  private void setExclude(boolean exclude)
  {
    _isExclude = exclude;
  }

  /**
   * Set the assay well's type. <i>Note: This is implemented as a denormalized
   * attribute. If you call this method, you must also call it for every
   * ResultValue that has the same Well (within the same parent screen result).</i>
   * Technically, we should have an AssayWell entity, which groups all the
   * ResultValues for a given stock plate well (within the parent screen
   * result). But it's creates a lot of new bidirectional relationships!
   *
   * @param assayWellType the new type of the assay well
   * @motivation for hibernate
   */
  private void setAssayWellType(AssayWellType assayWellType)
  {
    if (! isHibernateCaller()) {
      validateAssayWellType(assayWellType);
    }

    // TODO: consider updating all related ResultValues (i.e., for the same well
    // within this ScreenResult); would require parallel
    // {get,set}HbnAssayWellType methods.
    _assayWellType = assayWellType;
  }


  private void validateAssayWellType(AssayWellType assayWellType)
  {
    if (assayWellType == AssayWellType.ASSAY_CONTROL ||
      assayWellType == AssayWellType.ASSAY_POSITIVE_CONTROL ||
      assayWellType == AssayWellType.OTHER) {
      if (_well.getLibraryWellType() != LibraryWellType.EMPTY) {
        log.info(
        /**throw new DataModelViolationException( **/
                 "result value assay well type can only be 'assay control', 'assay positive control', or 'other' if the library well type is 'empty'");
      }
    }
    else if (!_well.getLibraryWellType().getValue().equals(assayWellType.getValue())) {
      log.info(
               /**throw new DataModelViolationException( **/
                 "result value assay well type: " + assayWellType.getValue()
               + " does not match library well type of associated well: " + _well.getLibraryWellType().getValue());
    }
  }
}
