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

  // instance data
  
  private Integer         _resultValueId;
  private Integer         _version;
  private ResultValueType _resultValueType;
  private Well            _well;
  private String          _value;
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
  public ResultValue(ResultValueType resultValueType, Well well, String value)
  {
    this(resultValueType, well, value, false);
  }

  /**
   * Constructs an initialized <code>ResultValue</code> object.
   * @param resultValueType
   * @param well
   * @param value
   * @param exclude
   */
  public ResultValue(ResultValueType resultValueType, Well well, String value, boolean exclude)
  {
    setWell(well);
    setValue(value);
    setExclude(exclude);
    setResultValueType(resultValueType);
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
   * @param newResultValueType the new parent {@link ResultValueType}
   */
  public void setResultValueType(ResultValueType newResultValueType) {
    if (_resultValueType != null && newResultValueType != _resultValueType) {
      _resultValueType.getHbnResultValues().remove(this);
    }
    setHbnResultValueType(newResultValueType);
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
   * @hibernate.many-to-one column="well_id" not-null="true"
   */
  public Well getWell() {
    return _well;
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
    // note: this is a unidirectional many-to-one relationship, so no need to
    // update any list ResultValue in affected Well (it doesn't track
    // ResultValue members at all)
    _well = well;
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
    assert _resultValueType != null && _well != null :
      "business key fields have not been defined";
    ResultValue that = (ResultValue) o;
    int result = this.getWell().getPlateNumber().compareTo(that.getWell().getPlateNumber());
    if (result == 0) {
      result = this.getWell().getWellName().compareTo(that.getWell().getWellName());
      if (result == 0) {
        result = this.getResultValueType().getOrdinal().compareTo(that.getResultValueType().getOrdinal());
      }
    }
    return result;
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

  
  // protected getters and setters
  
  /**
   * Get a business key that uniquely represents this object and that is based
   * upon some subset of its domain-model data fields.
   * 
   * @motivation for Hibernate (as hashCode()-based set membership cannot rely
   *             upon database sequence ID)
   * @motivation used by Comparable methods
   * @return a <code>String</code> representing the business key
   */
  protected String getBusinessKey() {
    assert _resultValueType != null && _well != null :
      "business key fields have not been defined";
    return _well.getPlateNumber() + _well.getWellName() + _resultValueType.getOrdinal();
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
   */
  private ResultValueType getHbnResultValueType() {
    return _resultValueType;
  }

  /**
   * Set the parent {@link ResultValueType}.
   * 
   * @param resultValueType the parent {@link ResultValueType}
   * @motivation for Hibernate
   */
  private void setHbnResultValueType(ResultValueType resultValueType) {
    _resultValueType = resultValueType;
  }

}
