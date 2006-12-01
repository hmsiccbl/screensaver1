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

/**
 * A <code>ResultValue</code> holds the actual value of a screen result data
 * point (in a text field) for a given {@link ScreenResult},
 * {@link ResultValueType}, and
 * {@link edu.harvard.med.screensaver.model.libraries.Well}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ResultValue
{

  private static final long serialVersionUID = -4066041317098744417L;

  
  // properties instance data
  
  private String          _value;
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
    this(AssayWellType.EXPERIMENTAL, value, false);
  }

  /**
   * Constructs an initialized <code>ResultValue</code> object.  <p>
   * Wanring: to add a ResultValue to the model, call {@link ResultValueType#addResultValue}.
   * @param resultValueType
   * @param assayWellType
   * @param value
   * @param exclude
   */
  public ResultValue(AssayWellType assayWellType, String value, boolean exclude)
  {
    setAssayWellType(assayWellType);
    setValue(value);
    setExclude(exclude);
  } 

  /**
   * Get the assay well's type.
   * 
   * @return the assay well's type
   * @hibernate.property type="edu.harvard.med.screensaver.model.screenresults.AssayWellType$UserType"
   *                     not-null="true"
   */
  public AssayWellType getAssayWellType()
  {
    return _assayWellType;
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
   */
  public void setAssayWellType(AssayWellType assayWellType)
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
   * Get the actual value of this <code>ResultValue</code>.
   * 
   * @return a {@link java.lang.String} representing the actual value of this
   *         <code>ResultValue</code>
   * @hibernate.property type="text" not-null="true"
   */
  public String getValue() {
    return _value;
  }
  
  /**
   * Set the actual value of this <code>ResultValue</code>.
   * 
   * @param value the value to set
   */
  public void setValue(String value) {
    _value = value;
  }

  /**
   * Get whether this <code>ResultValue</code> is to be excluded in any
   * subsequent analyses.
   * 
   * @return <code>true</code> iff this <code>ResultValue</code> is to be
   *         excluded in any subsequent analysis
   * @hibernate.property type="boolean" not-null="true"
   */
  public boolean isExclude()
  {
    return _exclude;
  }
  
  /**
   * Set whether this <code>ResultValue</code> is to be excluded in any
   * subsequent analyses.
   * 
   * @param exclude set to <code>true</code> iff this <code>ResultValue</code>
   *          is to be excluded in any subsequent analysis
   */
  public void setExclude(boolean exclude)
  {
    _exclude = exclude;
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
  

  // @DerivedEntityProperty
  // public boolean isEdgeWell()
  // {
  // return getWell().isEdgeWell();
  // }


  public boolean equals(Object o)
  {
    if (!(o instanceof ResultValue)) {
      return false;
    }
    ResultValue other = (ResultValue) o;
    return 
      _value.equals(other._value) && 
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
    
    if (!rvt.isDerived()) {
      return Double.valueOf(rv.getValue());
    }
      
    if (rvt.isActivityIndicator()) {
      ActivityIndicatorType activityIndicatorType = rvt.getActivityIndicatorType();
      if (activityIndicatorType.equals(ActivityIndicatorType.BOOLEAN)) {
        return Boolean.valueOf(rv.getValue());
      }
      else if (activityIndicatorType.equals(ActivityIndicatorType.NUMERICAL)) {
        return Double.valueOf(rv.getValue());
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
    return rv.getValue().toString();
  }


  // private constructors and instance methods

  /**
   * Constructs an uninitialized ResultValue object.
   * @motivation for hibernate
   */
  private ResultValue() {}
  
}
