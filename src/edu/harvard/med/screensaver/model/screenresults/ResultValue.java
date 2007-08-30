// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import edu.harvard.med.screensaver.model.DerivedEntityProperty;

import org.apache.log4j.Logger;

/**
 * A <code>ResultValue</code> holds the actual value of a screen result data
 * point for a given {@link ScreenResult}, {@link ResultValueType}, and
 * {@link edu.harvard.med.screensaver.model.libraries.Well}. For alphanumeric
 * ResultValueTypes, the value is stored canonically as a string. For numeric
 * ResultValueTypes, the value is stored canonically as a double, allowing for
 * efficient sorting of numeric values in the database. For numeric ResultValue, the
 * getValue() will return a string representing the numeric value formatted to
 * the ResultValue's decimal precision. Note that the parent ResultValueType
 * contains an "isNumeric" property that indicates whether its member
 * ResultValues are numeric (the isNumeric flag is not stored with each
 * ResultValue for space efficiency).
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ResultValue
{

  private static final long serialVersionUID = -4066041317098744417L;
  private static final Logger log = Logger.getLogger(ResultValue.class);
  private static final int DEFAULT_DECIMAL_PRECISION = 3;
  private static final String[] FORMAT_STRINGS = new String[10];
  static {
    FORMAT_STRINGS[0] = "%1f";
    for (int i = 1; i < FORMAT_STRINGS.length; i++) {
      FORMAT_STRINGS[i] = "%1." + i + "f";
    }
  }

  // properties instance data

  private String          _value;
  private Double          _numericValue;
  private Integer         _numericDecimalPrecision;
  private AssayWellType   _assayWellType;
  /**
   * Note that we maintain an "exclude" flag on a per-ResultValue basis. It is
   * up to the application code and/or user interface to manage excluding the
   * full set of ResultValues associated with a stock plate well (row) or with a
   * data header (column). But we do need to allow any arbitrary set of
   * ResultValues to be excluded.
   */
  private boolean _exclude;
  private boolean _isPositive;


  // public constructors and instance methods

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
   * Constructs an initialized <code>ResultValue</code> object.
   * <p>
   * Warning: to add a ResultValue to the model, call
   * {@link ResultValueType#addResultValue}.
   *
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the value of the new ResultValue
   * @param decimalPrecision the number of digits to appear after the decimal
   *          point, when displayed
   * @param exclude the exclude flag of the new ResultValue
   */
  public ResultValue(AssayWellType assayWellType,
                     Double value,
                     int decimalPrecision,
                     boolean exclude,
                     boolean isPositive)
  {
    setAssayWellType(assayWellType);
    setNumericValue(value);
    if (decimalPrecision >= 0) {
      setNumericDecimalPrecision(decimalPrecision);
    } // else null
    setValue(formatNumericValue(decimalPrecision));
    setExclude(exclude);
    setPositive(isPositive);
  }

  /**
   * Constructs an initialized <code>ResultValue</code> object.
   * <p>
   * Warning: to add a ResultValue to the model, call
   * {@link ResultValueType#addResultValue}. This constructor is package
   * protected so that ResultValueType can use it, otherwise it would be
   * private.
   *
   * @param assayWellType the AssayWellType of the new ResultValue
   * @param value the value of the new ResultValue
   * @param exclude the exclude flag of the new ResultValue
   * @param isNumeric true, iff ths new ResultValue's value is a number
   */
  public ResultValue(AssayWellType assayWellType,
                     String value,
                     boolean exclude,
                     boolean isPositive)
  {
    setAssayWellType(assayWellType);
    setValue(value);
    setExclude(exclude);
    setPositive(isPositive);
  }

  /**
   * Get the assay well's type.
   *
   * @return the assay well's type
   */
  public AssayWellType getAssayWellType()
  {
    return _assayWellType;
  }

  /**
   * @motivation reduces confusion as to whether callers needs to check both
   *             getValue() and getNumericValue() to determine if ResultValue is
   *             null
   * @return
   */
  public boolean isNull()
  {
    return _value == null;
  }

  /**
   * Get the string value of this <code>ResultValue</code>.
   *
   * @return a {@link java.lang.String} representing the string value of this
   *         <code>ResultValue</code>; may return null.
   */
  public String getValue() {
    return _value;
  }

  /**
   * Get the numeric value of this <code>ResultValue</code>.
   *
   * @return a {@link java.lang.Double} representing the numeric value of this
   *         <code>ResultValue</code>; may return null.
   */
  public Double getNumericValue() {
    return _numericValue;
  }

  /**
   * Get the default decimal precision for this ResultValue's numeric value.
   *
   * @return the number of digits to be displayed after the decimal point, when
   *         value is displayed; null if ResultValue is not numeric or not
   *         defined
   */
  public Integer getNumericDecimalPrecision()
  {
    return _numericDecimalPrecision;
  }

  /**
   * Formats the numeric value of this ResultValue.
   *
   * @param decimalPrecision if &lt; 0, uses "general" formatting (%g), with precision 9, as
   *          described in {@link java.util.Formatter}
   * @return formatted numeric value
   */
  public String formatNumericValue(int decimalPrecision)
  {
    String strValue = null;
    if (_numericValue != null) {
      if (decimalPrecision < 0) {
        strValue = String.format("%g", _numericValue);
      }
      else if (decimalPrecision <  FORMAT_STRINGS.length) {
        // optimization: use precomputed format strings, rather than creating all those string objects
        strValue = String.format(FORMAT_STRINGS[decimalPrecision], _numericValue);
      }
      else {
        strValue = String.format("%1." + decimalPrecision + "f", _numericValue);
      }
    }
    return strValue;
  }

  /**
   * Get whether this <code>ResultValue</code> is to be excluded in any
   * subsequent analyses.
   *
   * @return <code>true</code> iff this <code>ResultValue</code> is to be
   *         excluded in any subsequent analysis
   */
  public boolean isExclude()
  {
    return _exclude;
  }

  public boolean isPositive()
  {
    return _isPositive;
  }

  @DerivedEntityProperty
  public boolean isExperimentalWell()
  {
    return getAssayWellType().equals(AssayWellType.EXPERIMENTAL);
  }

  @DerivedEntityProperty
  public boolean isControlWell()
  {
    return getAssayWellType().isControl();
  }

  @DerivedEntityProperty
  public boolean isDataProducerWell()
  {
    return getAssayWellType().isDataProducing();
  }

  @DerivedEntityProperty
  public boolean isOtherWell()
  {
    return getAssayWellType().equals(AssayWellType.OTHER);
  }

  @DerivedEntityProperty
  public boolean isEmptyWell()
  {
    return getAssayWellType().equals(AssayWellType.EMPTY);
  }

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
      _exclude == other._exclude;
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
   * <li> PositiveIndicatorType.PARTITION: returns String (PartitionedValue.getDisplayValue())
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
          // should already have been handled above, but we include this case for completeness
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


  // package-protected methods

  /**
   * Set whether this <code>ResultValue</code> is a positive.
   *
   * @param isPositive set to <code>true</code> iff this <code>ResultValue</code>
   *          is a positive
   * @motivation for hibernate and ResultValueType
   */
  void setPositive(boolean isPositive)
  {
    _isPositive = isPositive;
  }


  // private constructors and instance methods

  /**
   * Constructs an uninitialized ResultValue object.
   * @motivation for hibernate
   */
  private ResultValue() {}

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
    if (assayWellType == null) {
      throw new NullPointerException("assayWellType must be non-null");
    }
    // TODO: consider updating all related ResultValues (i.e., for the same well
    // within this ScreenResult); would require parallel
    // {get,set}HbnAssayWellType methods.
    _assayWellType = assayWellType;
  }

  /**
   * Set the actual value of this <code>ResultValue</code>.
   *
   * @param value the value to set
   * @motivation for hibernate
   */
  private void setValue(String value) {
    _value = value;
  }

  /**
   * Set the actual value of this <code>ResultValue</code>.
   *
   * @param value the value to set
   * @motivation for hibernate
   */
  private void setNumericValue(Double value) {
    _numericValue = value;
  }

  /**
   *
   * @param numericDecimalPrecision
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
    _exclude = exclude;
  }

}
