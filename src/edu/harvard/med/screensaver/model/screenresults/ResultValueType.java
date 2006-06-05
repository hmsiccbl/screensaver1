// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * Provides the metadata for a subset of a
 * {@link edu.harvard.med.screensaver.model.libraries.Screen}'s
 * {@link ResultValue}s, all of which will have been produced with the "same
 * way". A <code>ResultValueType</code> can describe either how a subset of
 * raw data values were generated via automated machine reading of assay plates,
 * or how a subset of derived data values were calculated. For raw data values,
 * the metadata describes the parameters of a single, physical machine read of a
 * single replicate assay plate, using the same assay read technology, at the
 * same read time interval, etc.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class
 */
public class ResultValueType extends AbstractEntity implements Comparable
{

  // TODO: perhaps we should split ResultValueType into subclasses, one for raw
  // data value descriptions and one for derived data descriptions?
  
  private static final long serialVersionUID = -2325466055774432202L;

  
  // instance data

  private Integer                _resultValueTypeId;
  private Integer                _version;
  private ScreenResult           _screenResult;
  private SortedSet<ResultValue> _resultValues = new TreeSet<ResultValue>();
  private String                 _name;
  private String                 _description;
  private Integer                _ordinal;
  private int                    _replicateOrdinal;
  private String                 _assayReadoutTechnology;
  private String                 _timePoint;
  private boolean                _isDerived;
  private String                 _howDerived;
  private Set<ResultValueType>   _derivedFrom;
  private boolean                _isActivityIndicator;
  private enum                   _activityIndicatorType {NUMERICAL, BOOLEAN, SCALED};
  private enum                   _indicatorDirection {HIGH_VALUES_INDICATE, LOW_VALUES_INDICATE};
  private double                 _indicatorCutoff;
  private boolean                _isFollowUpData;
  private String                 _assayPhenotype;
  private boolean                _isCherryPick;
  private String                 _comments;


  // constructors
  
  /**
   * Constructs an uninitialized <code>ResultValueType</code> object.
   * @motivation for Hibernate loading
   */
  public ResultValueType() {}
  
  
  // public getters and setters

  /**
   * Get a unique identifier for the <code>ResultValueType</code>.
   * 
   * @return an Integer representing a unique i
   * dentifier for the
   *         <code>ResultValueType</code>
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="result_value_type_id_seq"
   */
  public Integer getResultValueTypeId() {
    return _resultValueTypeId;
  }

  /**
   * Set the unique identifier for the <code>ResultValueType</code>.
   * 
   * @param resultValueTypeId a unique identifier for the
   *          <code>ResultValueType</code>
   */
  public void setResultValueTypeId(Integer resultValueTypeId) {
    _resultValueTypeId = resultValueTypeId;
  }
  
  /**
   * Get the parent {@link ScreenResult}.
   * 
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screenresults.ScreenResult"
   *                        column="screen_result_id" not-null="true"
   */
  public ScreenResult getScreenResult() {
    return _screenResult;
  }
  
  /**
   * Set the parent {@link ScreenResult}.
   * @motivation for Hibernate
   */
  protected void setScreenResult(ScreenResult screenResult) {
    _screenResult = screenResult;
  }

  /**
   * Add this <code>ResultValueType</code> to the specified
   * {@link ScreenResult}, removing from the existing {@link ScreenResult}
   * parent, if necessary.
   */
  public void addToScreenResult(ScreenResult screenResult) {
    if (_screenResult != null && screenResult != _screenResult) {
      _screenResult.getHbnResultValueTypes().remove(this);
    }
    _screenResult = screenResult;
    setOrdinal(_screenResult.getHbnResultValueTypes().size());
    _screenResult.getHbnResultValueTypes().add(this);
  }

  /**
   * Get the set of {@link ResultValue}s that were generated for this
   * <code>ResultValueType</code>.
   * 
   * @motivation for Hibernate
   * @return the {@link java.util.SortedSet} of {@link ResultValue}s generated
   *         for this <code>ResultValueType</code>
   * @hibernate.set cascade="all-delete-orphan" inverse="true" sort="natural"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screenresults.ResultValue"
   * @hibernate.collection-key column="result_value_type_id"
   */
  public SortedSet<ResultValue> getHbnResultValues() {
    return _resultValues;
  }

  /**
   * Set the set of {@link ResultValue}s that comprise this
   * <code>ResultValueType</code>.
   * 
   * @param resultValue the {@link java.util.SortedSet} of {@link ResultValue}s
   *          generated for this <code>ResultValueType</code>.
   * @motivation for Hibernate
   */
  public void setHbnResultValues(SortedSet<ResultValue> resultValues) {
    _resultValues = resultValues;
  }

  /**
   * Get a set of all {@link ResultValue}s for this
   * <code>ResultValueType</code>.
   * 
   * @return an unmodifiable {@link java.util.SortedSet} of the
   *         {@link ResultValue}s generated for this
   *         <code>ResultValueType</code>.
   */
  public SortedSet<ResultValue> getResultValues() {
    return Collections.unmodifiableSortedSet(_resultValues);
  }

  /**
   * Get the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}.
   * 
   * @return an <code>Integer</code>
   * @hibernate.property type="integer" column="ordinal"
   */  
  public Integer getOrdinal() {
    return _ordinal;
  }
  
  /**
   * Set the ordinal position of this <code>ResultValueType</code> within its
   * parent {@link ScreenResult}. To be called by Hibernate only, as this
   * property is set automatically when {@link #addToScreenResult(ScreenResult)}
   * is called.
   * 
   * @return an <code>Integer</code>
   * @motivation for Hibernate
   */
  protected void setOrdinal(Integer ordinal) {
    _ordinal = ordinal;
  }

  public int getReplicateOrdinal() {
    return _replicateOrdinal;
  }
  
  protected void setReplicateOrdinal(int replicateOrdinal) {
    _replicateOrdinal = replicateOrdinal;
  }
  
  
  // public Object methods
  
  @Override
  public int hashCode()
  {
    return getBusinessKey().hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    return getBusinessKey().equals(((ResultValueType) other).getBusinessKey());
  }

  
  // Comparable interface methods

  public int compareTo(Object that) {
    return getBusinessKey().compareTo(((ResultValueType) that).getBusinessKey());
  }


  // protected getters and setters
  
  /**
   * Get the version number of the compound.
   * 
   * @return the version number of the <code>ResultValueType</code>
   * @motivation for hibernate
   * @hibernate.version
   */
  protected Integer getVersion() {
    return _version;
  }

  /**
   * Set the version number of the <code>ResultValueType</code>
   * 
   * @param version the new version number for the <code>ResultValueType</code>
   * @motivation for hibernate
   */
  protected void setVersion(Integer version) {
    _version = version;
  }

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
    assert _screenResult != null && _ordinal != null :
      "business key fields have not been defined";
    // TODO: should call getScreenResult().getBusinessKey(), but method doesn't yet exist
    return getScreenResult().getDateCreated() + ":" + getOrdinal();
  }

  
}
