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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellType;


/**
 * A <code>ResultValue</code> holds the actual value of a screen result data
 * point for a given {@link ScreenResult}, {@link ResultValueType}, and
 * {@link edu.harvard.med.screensaver.model.libraries.Well}. For alphanumeric
 * ResultValueTypes, the value is stored canonically as a string. For numeric
 * ResultValueTypes, the value is stored canonically as a double, allowing for
 * efficient sorting of numeric values in the database. For numeric ResultValue,
 * the getValue() will return a string representing the numeric value formatted
 * to the ResultValue's decimal precision. Note that the parent ResultValueType
 * contains an "isNumeric" property that indicates whether its member
 * ResultValues are numeric (the isNumeric flag is not stored with each
 * ResultValue for space efficiency).
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Entity(mutable=false)
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=ResultValueType.class)
@org.hibernate.annotations.Table(appliesTo = "result_value",
                                 indexes={ @Index(name = "result_value_rvt_and_value_index", columnNames={ "resultValueTypeId", "value" }),
                                           @Index(name = "result_value_rvt_and_numeric_value_index", columnNames={ "resultValueTypeId", "numericValue" }),
                                           @Index(name = "result_value_rvt_and_positive_index", columnNames={ "resultValueTypeId", "isPositive" }) })
public class ResultValue extends AbstractEntity
{

  // private static data

  private static final long serialVersionUID = -4066041317098744417L;
  private static final Logger log = Logger.getLogger(ResultValue.class);
  public static final int DEFAULT_DECIMAL_PRECISION = 3;
  private static final String[] FORMAT_STRINGS = new String[10];
  static {
    FORMAT_STRINGS[0] = "%1.0f";
    for (int i = 1; i < FORMAT_STRINGS.length; i++) {
      FORMAT_STRINGS[i] = "%1." + i + "f";
    }
  }


  // public static methods

  /**
   * Returns the value of this <code>ResultValue</code> as an appropriately
   * typed object, depending upon {@link ResultValueType#isPositiveIndicator()},
   * {@link ResultValueType#isDerived()()}, and
   * {@link ResultValueType#getPositiveIndicatorType()}, as follows:
   * <ul>
   * <li> Well type is non-data-producer: returns <code>null</code>
   * <li> Not Derived (Raw): returns Double
   * <li> Not an Activity Indicator: returns String
   * <li> PositiveIndicatorType.BOOLEAN: returns Boolean
   * <li> PositiveIndicatorType.NUMERICAL: returns Double
   * <li> PositiveIndicatorType.PARTITION: returns String
   * (PartitionedValue.getDisplayValue())
   * </ul>
   *
   * @return a Boolean, Double, or String
   * @motivation to preserve typed data in exported Workbooks (rather than treat
   *             all result values as text strings)
   */
  // TODO: now that ResultValue.resultValueType relationship exists, we can make this an instance method, and eliminate the RVT param
  public static Object getTypedValue(ResultValue rv, ResultValueType rvt)
  {
    if (rv == null || rv.isNull()) {
      return null;
    }

    if (rvt.isNumeric()) {
      return rv.getNumericValue();
    }

    if (rvt.isPositiveIndicator()) {
      PositiveIndicatorType activityIndicatorType = rvt.getPositiveIndicatorType();
      if (activityIndicatorType.equals(PositiveIndicatorType.BOOLEAN)) {
        return Boolean.valueOf(rv.getValue());
      }
      else if (activityIndicatorType.equals(PositiveIndicatorType.NUMERICAL)) {
        if (rvt.isNumeric()) {
          // should already have been handled above, but we include this case
          // for completeness
          return rv.getNumericValue();
        }
        else {
          log.warn("expected ResultValue to have numeric value, since parent ResultValueType is numerical");
          return rv.getValue();
        }
      }
      else if (activityIndicatorType.equals(PositiveIndicatorType.PARTITION)) {
        return PartitionedValue.lookupByValue(rv.getValue()).getDisplayValue();
      }
    }
    return rv.getValue();
  }


  // private instance data

  private Integer _resultValueId;
  private Well _well;
  private ResultValueType _resultValueType;
  private String _value;
  private Double _numericValue;
  private Integer _numericDecimalPrecision;
  private AssayWellType _assayWellType;
  /**
   * Note that we maintain an "exclude" flag on a per-ResultValue basis. It is
   * up to the application code and/or user interface to manage excluding the
   * full set of ResultValues associated with a stock plate well (row) or with a
   * data header (column). But we do need to allow any arbitrary set of
   * ResultValues to be excluded.
   */
  private boolean _isExclude;
  private boolean _isPositive;


  // public constructors

  /**
   * Constructs an initialized <code>ResultValue</code> object.
   * @param resultValueType
   * @param well
   * @param value
   */
  ResultValue(ResultValueType rvt,
              Well well,
              String value)
  {
    this(rvt, well, AssayWellType.EXPERIMENTAL, value, null, -1, false, false);
  }

  /**
   * Constructs a numeric ResultValue object, using a Double to specify the
   * numeric value.
   *
   * @param value
   * @param decimalPrecision the number of digits to appear after the decimal point, when displayed
   */
  ResultValue(ResultValueType rvt,
              Well well,
              Double value,
              int decimalPrecision)
  {
    this(rvt, well, AssayWellType.EXPERIMENTAL, null, value, decimalPrecision, false, false);
  }

  /**
   * Construct a numerical <code>ResultValue</code>. Intended for use only
   * for creating result values that will not need to be persisted.
   *
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the non-numerical value of the new result value
   * @param numericalValue the value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal
   *          point, when displayed
   * @param exclude the exclude flag of the new ResultValue
   */
  public ResultValue(ResultValueType rvt,
                     Well well,
                     AssayWellType assayWellType,
                     Double numericalValue,
                     int decimalPrecision,
                     boolean exclude,
                     boolean isPositive)
  {
    this(rvt, well, assayWellType, null, numericalValue, decimalPrecision, exclude, isPositive);
  }

  /**
   * Construct a non-numerical <code>ResultValue</code>. Intended for use
   * only for creating result values that will not need to be persisted.
   *
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the non-numerical value of the new result value
   * @param numericalValue the value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal
   *          point, when displayed
   * @param exclude the exclude flag of the new ResultValue
   */
  public ResultValue(ResultValueType rvt,
                     Well well,
                     AssayWellType assayWellType,
                     String value,
                     boolean exclude,
                     boolean isPositive)
  {
    this(rvt, well, assayWellType, value, null, 0, exclude, isPositive);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Serializable getEntityId()
  {
    return getResultValueId();
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
    return _resultValueId;
  }

  /**
   * Get the result value type.
   * @return the result value type
   */
  @ManyToOne(cascade={}, fetch=FetchType.LAZY)
  @JoinColumn(name="resultValueTypeId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_result_value_to_result_value_type")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @Index(name="result_value_result_value_type_index")
  public ResultValueType getResultValueType()
  {
    return _resultValueType;
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
   * Get the default decimal precision for this ResultValue's numeric value.
   *
   * @return the number of digits to be displayed after the decimal point when
   *         value is displayed; null if ResultValue is not numeric or if
   *         {@link #getNumericValue()} is null
   */
  public Integer getNumericDecimalPrecision()
  {
    return _numericDecimalPrecision;
  }

  /**
   * Formats the numeric value of this ResultValue.
   *
   * @param decimalPrecision if &lt; 0, uses "general" formatting (%g), with
   *          precision 9, as described in {@link java.util.Formatter}
   * @return the formatted numeric value
   */
  public String formatNumericValue(int decimalPrecision)
  {
    String strValue = null;
    if (_numericValue != null) {
      if (decimalPrecision < 0) {
        strValue = String.format("%g", _numericValue);
      }
      else if (decimalPrecision <  FORMAT_STRINGS.length) {
        // optimization: use precomputed format strings, rather than creating
        // all those string objects
        strValue = String.format(FORMAT_STRINGS[decimalPrecision], _numericValue);
      }
      else {
        strValue = String.format("%1." + decimalPrecision + "f", _numericValue);
      }
    }
    return strValue;
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
   * {@link #getResultValueType() ResultValueType} is not a positive indicator.
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
   * Construct an initialized <code>ResultValue</code>. Intended only for use
   * with {@link #ResultValue(AssayWellType, String, boolean, boolean)},
   * {@link #ResultValue(AssayWellType, Double, int, boolean, boolean)}, and
   * {@link
   * ResultValueType#createResultValue(edu.harvard.med.screensaver.model.libraries.Well,
   * AssayWellType, Double, int, boolean)}.
   *
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the non-numerical value of the new result value
   * @param numericalValue the value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal
   *          point, when displayed
   * @param exclude the exclude flag of the new ResultValue
   */
  ResultValue(ResultValueType rvt,
              Well well,
              AssayWellType assayWellType,
              String value,
              Double numericalValue,
              int decimalPrecision,
              boolean exclude,
              boolean isPositive)
  {
    if (rvt == null) {
      throw new DataModelViolationException("resultValueType is required for ResultValue");
    }
    if (well == null) {
      throw new DataModelViolationException("well is required for ResultValue");
    }
    _resultValueType = rvt;
    _well = well;

    // TODO: HACK: removing this update as it causes memory/performance
    // problems when loading ScreenResults; fortunately, when ScreenResult is
    // read in from database from a new Hibernate session, the in-memory
    // associations will be correct; these in-memory associations will only be
    // missing within the Hibernate session that was used to import the
    // ScreenResult
    // _well.getResultValues().put(rvt, this);

    setAssayWellType(assayWellType);
    if (value != null) {
      setValue(value);
    }
    else {
      setNumericValue(numericalValue);
      if (decimalPrecision >= 0) {
        setNumericDecimalPrecision(decimalPrecision);
      }
      setValue(formatNumericValue(decimalPrecision));
    }
    setExclude(exclude);
    setPositive(isPositive);
  }


  // package-protected methods

  /**
   * Set whether this result value is a positive. Intended only for use by
   * hibernate and {@link
   * ResultValueType#createResultValue(edu.harvard.med.screensaver.model.libraries.Well,
   * AssayWellType, Double, int, boolean)}.
   *
   * @param isPositive true iff this result value is a positive
   * @motivation for hibernate and ResultValueType.addResultValue
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
    _resultValueId = resultValueId;
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
   * Set the result value type.
   *
   * @param resultValueType the new result value type
   * @motivation for hibernate
   */
  private void setResultValueType(ResultValueType resultValueType)
  {
    _resultValueType = resultValueType;
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
   * Set the numerical decimal precision.
   *
   * @param numericDecimalPrecision the new numerical decimal precision
   * @motivation for hibernate
   */
  private void setNumericDecimalPrecision(Integer numericDecimalPrecision)
  {
    _numericDecimalPrecision = numericDecimalPrecision;
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
      if (_well.getWellType() != WellType.EMPTY) {
        log.warn(/*(
        throw new DataModelViolationException(*/"result value assay well type can only be 'assay control', 'assay positive control', or 'other' if the library well type is 'empty'");
      }
    }
    else if (!_well.getWellType().getValue().equals(assayWellType.getValue())) {
      log.warn(/*
      throw new DataModelViolationException(*/"result value assay well type does not match library well type of associated well");
    }
  }
}
