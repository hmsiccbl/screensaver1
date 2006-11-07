// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * A <code>ResultValue</code> holds the actual value of a screen result data
 * point (in a text field) for a given {@link ScreenResult},
 * {@link ResultValueType}, and
 * {@link edu.harvard.med.screensaver.model.libraries.Well}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class
 *   lazy="false"
 */
public class ResultValue extends AbstractEntity implements Comparable
{

  private static final long serialVersionUID = -4066041317098744417L;

  
  // properties instance data
  
  private Integer         _resultValueId;
  private Integer         _version;
  private ResultValueType _resultValueType;
  private Well            _well;
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
  

  // non-properties instance data
  
  private BusinessKey _businessKey = new BusinessKey();
 

  // public constructors and instance methods
  
  /**
   * Constructs an initialized <code>ResultValue</code> object.
   * @param resultValueType
   * @param well
   * @param value
   */
  public ResultValue(ResultValueType resultValueType, Well well, String value)
  {
    this(resultValueType, well, AssayWellType.EXPERIMENTAL, value, false);
  }

  /**
   * Constructs an initialized <code>ResultValue</code> object.
   * @param resultValueType
   * @param well
   * @param assayWellType
   * @param value
   * @param exclude
   */
  public ResultValue(ResultValueType resultValueType, Well well, AssayWellType assayWellType, String value, boolean exclude)
  {
    _businessKey = new BusinessKey(well, resultValueType);
    _well = well; // HACK: to allow setResultValueType() to suceeed; ideally, we want to set both resultValueType and Well before adding to relationships' collections
    setResultValueType(resultValueType);
    setWell(well);
    setAssayWellType(assayWellType);
    setValue(value);
    setExclude(exclude);
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.model.AbstractEntity#getEntityId()
   */
  public Integer getEntityId()
  {
    return getResultValueId();
  }

  /**
   * Get a unique identifier for the <code>ResultValue</code>.
   * 
   * @return an {@link java.lang.Integer} representing a unique identifier for
   *         this <code>ResultValue</code>
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="result_value_id_seq"
   */
  public Integer getResultValueId() {
    return _resultValueId;
  }

  /**
   * @param resultValueId The resultValueId to set.
   */
  public void setResultValueId(Integer resultValueId) {
    _resultValueId = resultValueId;
  }

  /**
   * Get the parent {@link ResultValueType}.
   * 
   * @return the parent {@link ResultValueType}
   */
  public ResultValueType getResultValueType() {
    return _resultValueType;
  }
  
  /**
   * Add this <code>ResultValue</code> to the specified
   * {@link ResultValueType}, removing from the existing
   * {@link ResultValueType} parent, if necessary.
   * 
   * @param resultValueType the new parent {@link ResultValueType}
   */
  public void setResultValueType(ResultValueType resultValueType) {
    if (_resultValueType == resultValueType) {
      return;
    }
    if (_resultValueType != null) {
      _resultValueType.getHbnResultValues().remove(this);
    }
    _businessKey.setResultValueType(resultValueType);
    _resultValueType = resultValueType;
    _resultValueType.getHbnResultValues().add(this);
  }

  /**
   * Get the {@link edu.harvard.med.screensaver.model.libraries.Well} (of the
   * library stock plate) that was replicated and used to generate this
   * <code>ResultValue</code>.
   * 
   * @return the {@link edu.harvard.med.screensaver.model.libraries.Well} (of
   *         the library stock plate) that was replicated and used to generate
   *         this <code>ResultValue</code>
   */
  public Well getWell() {
    return getHbnWell();
  }

  /**
   * Set the {@link edu.harvard.med.screensaver.model.libraries.Well} (of the
   * library stock plate) that was replicated and used to generate this
   * <code>ResultValue</code>.
   * 
   * @param well the {@link edu.harvard.med.screensaver.model.libraries.Well}
   *          (of the library stock plate) that was replicated and used to
   *          generate this <code>ResultValue</code>
   */
  public void setWell(Well well) {
    if (_resultValueType != null) {
      _resultValueType.getHbnResultValues().remove(this);
    }
    _businessKey.setWell(well);
    if (_resultValueType != null) {
      _resultValueType.getHbnResultValues().add(this);
    }
    _well = well;
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
  public Object generateTypedValue()
  {
    if (!isDataProducer()) {
      return null;
    }
    
    if (!getResultValueType().isDerived()) {
      return Double.valueOf(_value);
    }
      
    if (getResultValueType().isActivityIndicator()) {
      ActivityIndicatorType activityIndicatorType = getResultValueType().getActivityIndicatorType();
      if (activityIndicatorType.equals(ActivityIndicatorType.BOOLEAN)) {
        return Boolean.valueOf(_value);
      }
      else if (activityIndicatorType.equals(ActivityIndicatorType.NUMERICAL)) {
        return Double.valueOf(_value);
      }
      else if (activityIndicatorType.equals(ActivityIndicatorType.PARTITION)) {
        for (PartitionedValue pv: PartitionedValue.values()) {
          if (pv.getValue().equals(_value)) {
            return pv;
          }
        }
        assert false : "not a PartitionValue in ResultValue.generateTypedValue()";
        /* return PartitionedValue.valueOf(_value);*/
      }
      assert false : "unhandled ActivityIndicatorType in ResultValue.generateTypedValue()";
    }
    return _value.toString();
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
  public boolean isExperimental()
  {
    return getAssayWellType().equals(AssayWellType.EXPERIMENTAL);
  }
  
  @DerivedEntityProperty
  public boolean isControl()
  {
    return getAssayWellType().equals(AssayWellType.ASSAY_NEGATIVE_CONTROL) ||
    getAssayWellType().equals(AssayWellType.ASSAY_POSITIVE_CONTROL) ||
    getAssayWellType().equals(AssayWellType.LIBRARY_CONTROL);
  }
  
  @DerivedEntityProperty
  public boolean isDataProducer()
  {
    // TODO: I'm assuming wells of type "other" can contain data values --ant
    return isExperimental() || isControl() || isOther();
  }
  
  public boolean isOther()
  {
    return getAssayWellType().equals(AssayWellType.OTHER);
  }

  @DerivedEntityProperty
  public boolean isEmpty()
  {
    return getAssayWellType().equals(AssayWellType.EMPTY);
  }
  
  @DerivedEntityProperty
  public boolean isEdge()
  {
    return getWell().isEdgeWell();
  }
  

  // Comparable interface methods
  
  /**
   * Defines a natural ordering of <code>ResultValue</code> objects, assuming
   * they are from the same {@link ScreenResult} parent; otherwise natural
   * ordering is undefined. Ordering is by plate name, then well name, then
   * result value type (i.e., row major ordering, if you think of a
   * ScreenResult's data as a matrix of plate/well rows and result value type
   * columns).
   * 
   * @motivation allows objects of this class to be sorted in Hibernate result
   *             sets
   */
  public int compareTo(Object o) {
    return _businessKey.compareTo(((ResultValue) o)._businessKey);
  }

  
  // public hibernate getters for cross-package relationships
  
  /**
   * Set the {@link edu.harvard.med.screensaver.model.libraries.Well} (of the
   * library stock plate) that was replicated and used to generate this
   * <code>ResultValue</code>.
   * 
   * @param well the {@link edu.harvard.med.screensaver.model.libraries.Well}
   *          (of the library stock plate) that was replicated and used to
   *          generate this <code>ResultValue</code>
   * @motivation for hibernate
   */
  public void setHbnWell(Well well) {
    _well = well;
    _businessKey.setWell(_well);
  }

  
  // protected getters and setters
  
  /**
   * Set the parent {@link ResultValueType}.
   * 
   * @param resultValueType the parent {@link ResultValueType}
   * @motivation for Hibernate
   */
  void setHbnResultValueType(ResultValueType resultValueType) {
    _resultValueType = resultValueType;
    _businessKey.setResultValueType(_resultValueType);
  }

  /**
   * A business key class for the <code>ResultValue</code>.
   */
  private class BusinessKey implements Comparable
  {
    
    private String _wellPlateNumber;
    private String _wellName;
    private ResultValueType _resultValueType;

    public BusinessKey() {}

    public BusinessKey(Well well,
                       ResultValueType resultValueType)
    {
      setWell(well);
      _resultValueType = resultValueType;
    }

    public void setWell(Well well)
    {
      // HACK: keep well properties as part of business key, to handle null Well case during delete of ScreenResult
      if (well != null) {
        _wellPlateNumber = well.getPlateNumber().toString();
        _wellName = well.getWellName();
      }
    }

    public void setResultValueType(ResultValueType resultValueType)
    {
      _resultValueType = resultValueType;
    }

    @Override
    public boolean equals(Object object)
    {
      if (!(object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return 
        _wellName.equals(that._wellName) &&
        _wellPlateNumber.equals(that._wellPlateNumber) &&
        _resultValueType.equals(that._resultValueType);
    }

    @Override
    public int hashCode()
    {
      return 
        _wellName.hashCode() +
        _wellPlateNumber.hashCode() +
        _resultValueType.hashCode();
    }

    @Override
    public String toString()
    {
      return _wellPlateNumber + _wellName + ":" + _resultValueType;
    }
    
    public int compareTo(Object o)
    {
      BusinessKey that = (BusinessKey) o;
      int result = _wellPlateNumber.compareTo(that._wellPlateNumber);
      if (result == 0) {
        result = _wellName.compareTo(that._wellName);
        if (result == 0) {
          result = _resultValueType.getOrdinal().compareTo(that._resultValueType.getOrdinal());
        }
      }
      return result;
    }    
  }

  @Override
  protected Object getBusinessKey() {
    return _businessKey;
  }
  
  // private constructors and instance methods

  /**
   * Constructs an uninitialized ResultValue object.
   * @motivation for hibernate
   */
  private ResultValue() {}
  
  /**
   * Get the version number of the <code>ResultValue</code>.
   * 
   * @return the version number of the <code>ResultValue</code>
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version number of the <code>ResultValue</code>
   * 
   * @param version the new version number for the <code>ResultValue</code>
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the parent {@link ResultValueType}.
   * 
   * @return the parent {@link ResultValueType}
   * @motivation for Hibernate
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screenresults.ResultValueType"
   *                        column="result_value_type_id" not-null="true"
   *                        foreign-key="fk_result_value_to_result_value_type"
   *                        cascade="save-update"
   */
  private ResultValueType getHbnResultValueType() {
    return _resultValueType;
  }
  
  /**
   * Get the {@link edu.harvard.med.screensaver.model.libraries.Well} (of the
   * library stock plate) that was replicated and used to generate this
   * <code>ResultValue</code>.
   * 
   * @return the {@link edu.harvard.med.screensaver.model.libraries.Well} (of
   *         the library stock plate) that was replicated and used to generate
   *         this <code>ResultValue</code>
   * @hibernate.many-to-one column="well_id" not-null="true"
   *   foreign-key="fk_result_value_to_well"
   *   cascade="save-update"
   */
  @ToOneRelationship(unidirectional=true)
  Well getHbnWell() {
    return _well;
  }
}
