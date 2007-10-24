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
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parent;

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
@Embeddable
public class ResultValue
{

  // private static data

  private static final long serialVersionUID = -4066041317098744417L;
  private static final Logger log = Logger.getLogger(ResultValue.class);
  private static final int DEFAULT_DECIMAL_PRECISION = 3;
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

  private String _wellId;
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
  ResultValue(String value)
  {
    this(AssayWellType.EXPERIMENTAL, value, false, false);
  }

  /**
   * Constructs a numeric ResultValue object, using a Double to specify the
   * numeric value.
   *
   * @param value
   * @param decimalPrecision the number of digits to appear after the decimal point, when displayed
   */
  ResultValue(Double value, int decimalPrecision)
  {
    this(AssayWellType.EXPERIMENTAL, value, decimalPrecision, false, false);
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
  public ResultValue(
    AssayWellType assayWellType,
    Double numericalValue,
    int decimalPrecision,
    boolean exclude,
    boolean isPositive)
  {
    this(assayWellType, null, numericalValue, decimalPrecision, exclude, isPositive);
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
  public ResultValue(
    AssayWellType assayWellType,
    String value,
    boolean exclude,
    boolean isPositive)
  {
    this(assayWellType, value, null, 0, exclude, isPositive);
  }


  // public instance methods

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof ResultValue)) {
      return false;
    }
    ResultValue other = (ResultValue) o;
    return
    ((_value == null && other._value == null) ||
      (_value != null && other._value != null && _value.equals(other._value))) &&
      _assayWellType.equals(other._assayWellType) &&
      _isExclude == other._isExclude;
  }

  /**
   * Get the well id.
   * @return the well id
   */
  // TODO: should be text not null
  @Transient
  @org.hibernate.annotations.Type(type="text")
  // TODO: hibernate annotations is not processing the @Index on the @Embeddable columns
  @org.hibernate.annotations.Index(name="index_rvtrv_well_id")
  public String getWellId()
  {
    return _wellId;
  }

  /**
   * Get the result value type.
   * @return the result value type
   */
  @Parent
  // TODO: hibernate annotations is not processing @Column(name="...") on the @Parent
  @Column(name="resultValueTypeId")
  // TODO: hibernate annotations is not processing the @Index on the @Embeddable columns
  @org.hibernate.annotations.Index(name="index_rvtrv_rvt")
  public ResultValueType getResultValueType()
  {
    return _resultValueType;
  }

  /**
   * Get the string value of this <code>ResultValue</code>.
   *
   * @return a {@link java.lang.String} representing the string value of this
   *         <code>ResultValue</code>; may return null.
   */
  @org.hibernate.annotations.Type(type="text")
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
  @org.hibernate.annotations.Index(name="index_rvtrv_is_positive")
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
   * ResultValueType#addResultValue(edu.harvard.med.screensaver.model.libraries.Well,
   * AssayWellType, Double, int, boolean)}.
   *
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the non-numerical value of the new result value
   * @param numericalValue the value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal
   *          point, when displayed
   * @param exclude the exclude flag of the new ResultValue
   */
  ResultValue(
    AssayWellType assayWellType,
    String value,
    Double numericalValue,
    int decimalPrecision,
    boolean exclude,
    boolean isPositive)
  {
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
   * ResultValueType#addResultValue(edu.harvard.med.screensaver.model.libraries.Well,
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
   * @motivation for hibernatexs
   */
  private ResultValue() {}

  /**
   * Set the well id.
   *
   * @param wellId the new well id
   * @motivation for hibernate
   */
  private void setWellId(String wellId)
  {
    _wellId = wellId;
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
    // TODO: consider updating all related ResultValues (i.e., for the same well
    // within this ScreenResult); would require parallel
    // {get,set}HbnAssayWellType methods.
    _assayWellType = assayWellType;
  }
}
