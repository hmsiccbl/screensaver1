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
 * {@link edu.harvard.med.screensaver.model.libraries.Well}. The value is
 * always stored as a string, and this is considered the canonical version of
 * the value. However, for ResultValues that are deemed "numeric" the value is
 * redundantly stored as a numeric type, to allow for efficient sorting of
 * numeric values in the database. Client code must always set the value as
 * string, but may get the numeric value. Note that the ResultValueType contains
 * an "isNumeric" property that indicates whether its member ResultValues are
 * numeric.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ResultValue
{

  private static final long serialVersionUID = -4066041317098744417L;
  private static final Logger log = Logger.getLogger(ResultValue.class);
  
  // properties instance data
  
  private String          _value;
  private Double          _numericValue;
  private AssayWellType   _assayWellType;
  /**
   * Note that we maintain an "exclude" flag on a per-ResultValue basis. It is
   * up to the application code and/or user interface to manage excluding the
   * full set of ResultValues associated with a stock plate well (row) or with a
   * data header (column). But we do need to allow any arbitrary set of
   * ResultValues to be excluded.
   */
  private boolean _exclude;

  

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
   * Constructs a numeric ResultValue object, using a String to specify the
   * numeric value. This constructor should be used if the value is originally
   * made available as a String, as this preserves the full precision of the
   * value (rather than forcing all numeric values to be converted to Doubles
   * before constructing a numeric ResultValue).
   * 
   * @param value
   * @param isNumeric
   */
  ResultValue(String value, boolean isNumeric)
  {
    this(AssayWellType.EXPERIMENTAL, value, false, isNumeric);
  }
  
  /**
   * Constructs a numeric ResultValue object, using a Double to specify the
   * numeric value. If the value is originally made available as a String, use
   * {@link #ResultValue(String, boolean)}, as this will presever the full
   * precision of the value (it will be parsed into a Double, as well, and
   * stored redundantly as a String and a Double).
   * 
   * @param value
   */
  ResultValue(Double value)
  {
    this(AssayWellType.EXPERIMENTAL, value.toString(), false, true);
    _numericValue = value;
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
  ResultValue(AssayWellType assayWellType,
              Double value,
              boolean exclude)
  {
    setAssayWellType(assayWellType);
    if (value != null) {
      setValue(value.toString());
    }
    setNumericValue(value);
    setExclude(exclude);
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
  ResultValue(AssayWellType assayWellType,
              String value,
              boolean exclude,
              boolean isNumeric)
  {
    setAssayWellType(assayWellType);
    setValue(value);
    if (isNumeric && value != null) {
      setNumericValue(Double.parseDouble(value));
    }
    setExclude(exclude);
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
   * Get the string value of this <code>ResultValue</code>.
   * 
   * @return a {@link java.lang.String} representing the string value of this
   *         <code>ResultValue</code>
   */
  public String getValue() {
    return _value;
  }
  
  /**
   * Get the numeric value of this <code>ResultValue</code>.
   * 
   * @return a {@link java.lang.Double} representing the numeric value of this
   *         <code>ResultValue</code>
   */
  public Double getNumericValue() {
    return _numericValue;
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
  
  @DerivedEntityProperty
  public boolean isExperimentalWell()
  {
    return getAssayWellType().equals(AssayWellType.EXPERIMENTAL);
  }
  
  @DerivedEntityProperty
  public boolean isControlWell()
  {
    return getAssayWellType().equals(AssayWellType.ASSAY_NEGATIVE_CONTROL) ||
    getAssayWellType().equals(AssayWellType.ASSAY_POSITIVE_CONTROL) ||
    getAssayWellType().equals(AssayWellType.LIBRARY_CONTROL);
  }
  
  @DerivedEntityProperty
  public boolean isDataProducerWell()
  {
    // TODO: I'm assuming wells of type "other" can contain data values --ant
    return isExperimentalWell() || isControlWell() || isOtherWell();
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
   * typed object, depending upon {@link ResultValueType#isActivityIndicator()},
   * {@link ResultValueType#isDerived()()}, and
   * {@link ResultValueType#getActivityIndicatorType()}, as follows:
   * <ul>
   * <li> Well type is non-data-producer: returns <code>null</code>
   * <li> Not Derived (Raw): returns Double
   * <li> Not an Activity Indicator: returns String
   * <li> ActivityIndicatorType.BOOLEAN: returns Boolean
   * <li> ActivityIndicatorType.NUMERICAL: returns Double
   * <li> ActivityIndicatorType.PARTITION: returns PartitionedValue
   * </ul>
   * 
   * @return a Boolean, Double, or String
   * @motivation to preserve typed data in exported Workbooks (rather than treat
   *             all result values as text strings)
   */
  public static Object getTypedValue(ResultValue rv, ResultValueType rvt)
  {
    if (!rv.isDataProducerWell()) {
      return null;
    }
    
    if (rvt.isNumeric()) {
      return rv.getNumericValue();
    }
      
    if (rvt.isActivityIndicator()) {
      ActivityIndicatorType activityIndicatorType = rvt.getActivityIndicatorType();
      if (activityIndicatorType.equals(ActivityIndicatorType.BOOLEAN)) {
        return Boolean.valueOf(rv.getValue());
      }
      else if (activityIndicatorType.equals(ActivityIndicatorType.NUMERICAL)) {
        if (rvt.isNumeric()) {
          // should already have been handled above, but we include this case for completeness
          return rv.getNumericValue();
        }
        else {
          log.warn("expected ResultValue to have numeric value, since parent ResultValueType is numerical");
          return rv.getValue();
        }
      }
      else if (activityIndicatorType.equals(ActivityIndicatorType.PARTITION)) {
        for (PartitionedValue pv: PartitionedValue.values()) {
          if (pv.getValue().equals(rv.getValue())) {
            return pv;
          }
        }
        assert false : "not a PartitionValue in ResultValue.generateTypedValue()";
        /* return PartitionedValue.valueOf(rv.getValue());*/
      }
      assert false : "unhandled ActivityIndicatorType in ResultValue.generateTypedValue()";
    }
    return rv.getValue();
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
   * Set whether this <code>ResultValue</code> is to be excluded in any
   * subsequent analyses.
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
