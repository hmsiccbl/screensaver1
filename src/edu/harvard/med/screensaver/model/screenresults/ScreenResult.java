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
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * A <code>ScreenResult</code> represents the data produced by machine-reading
 * each of the assay plates associated with a
 * {@link edu.harvard.med.screensaver.model.screens.Screen}. Each stock plate
 * of the library being screened will be replicated across one or more assay
 * plates ("replicates"). Each replicate assay plate can have one or more
 * readouts performed on it, possibly over time intervals and/or with different
 * assay readout technologies. Every distinct readout type is identified by a
 * {@link ResultType}. A <code>ScreenResult</code> becomes the parent of
 * {@link ResultValue}s. For visualization purposes, one can imagine a
 * <code>ScreenResult</code> as representing a spreadsheet, where the column
 * headings are represented by {@link ResultValueType}s and the rows are
 * identified by stock plate {@link Well}, and each row contains a
 * {@link ResultValue} for each {@link ResultValueType} "column".
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class
 */
public class ScreenResult extends AbstractEntity
{

  private static final long serialVersionUID = 41904893172411174L;
  

  // persistent instance data
  
  private Integer                    _screenResultId;
  private Integer                    _version;
  private Date                       _dateCreated;
  private boolean                    _isShareable;
  private int                        _replicateCount;
  private SortedSet<ResultValueType> _resultValueTypes = new TreeSet<ResultValueType>();

  
  // constructors
  
  /**
   * Constructs an uninitialized <code>ScreenResult</code> object.
   * @motivation for Hibernate loading
   */
  public ScreenResult() {}
  
  
  // public getters and setters

  /**
   * Get a unique identifier for the <code>ScreenResult</code>.
   * 
   * @return an Integer representing a unique identifier for the
   *         <code>ScreenResult</code>
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screen_result_id_seq"
   */
  public Integer getScreenResultId() {
    return _screenResultId;
  }

  /**
   * Set the unique identifier for the <code>ScreenResult</code>.
   * 
   * @param screenResultId a unique identifier for the <code>ScreenResult</code>
   */
  public void setScreenResultId(Integer screenResultId) {
    _screenResultId = screenResultId;
  }

  /**
   * Get the date this <code>ScreenResult</code> was generated in the lab.
   * 
   * @return returns a {@link java.util.Date} representing the date this
   *         <code>ScreenResult</code> was created
   * @hibernate.property type="date" not-null="true"
   */
  public Date getDateCreated() {
    return _dateCreated;
  }
  
  /**
   * Set the date this <code>ScreenResult</code> was generated in the lab.
   * 
   * @param dateCreated the date this <code>ScreenResult</code> was generated
   *          in the lab
   */
  public void setDateCreated(Date dateCreated) {
    _dateCreated = dateCreated;
  }

  /**
   * Get whether this <code>ScreenResult</code> can be viewed by all users of
   * the system; that is,
   * {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser}s other
   * than those associated with the
   * {@link edu.harvard.med.screensaver.screens.Screen}.
   * 
   * @return <code>true</code> iff this <code>ScreenResult</code> is
   *         shareable among all users
   * @hibernate.property column="is_shareable" not-null="true"
   */
  public boolean isShareable() {
    return _isShareable;
  }

  /**
   * Set the shareability of this <code>ScreenResult</code>.
   * 
   * @param isShareable whether this <code>ScreenResult</code> can be viewed
   *          by all users of the system; that is,
   *          {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser}s
   *          other than those associated with the
   *          {@link edu.harvard.med.screensaver.screens.Screen}
   */
  public void setShareable(boolean isShareable) {
    _isShareable = isShareable;
  }

  /**
   * Get a {@link java.util.SortedSet} of all {@link ResultValueType}s for this
   * <code>ScreenResult</code>.
   * 
   * @motivation for Hibernate
   * @return an {@link java.util.SortedSet} of all {@link ResultValueType}s for
   *         this <code>ScreenResult</code>
   * @hibernate.set cascade="all-delete-orphan" inverse="true" sort="natural"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screenresults.ResultValueType"
   * @hibernate.collection-key column="screen_result_id"
   */
  public SortedSet<ResultValueType> getHbnResultValueTypes() {
    return _resultValueTypes;
  }

  /**
   * Set the ordered set of {@link ResultValueType}s that comprise this
   * <code>ScreenResult</code>.
   * 
   * @param resultValueTypes the {@link java.util.SortedSet} of
   *          {@link ResultValueType}s that comprise this
   *          <code>ScreenResult</code>.
   * @motivation for hibernate
   */
  public void setHbnResultValueTypes(SortedSet<ResultValueType> resultValueTypes) {
    _resultValueTypes = resultValueTypes;
  }

  /**
   * Get a ordered set of all {@link ResultValueType}s for this
   * <code>ScreenResult</code>.
   * 
   * @return an unmodifiable {@link java.util.SortedSet} of all
   *         {@link ResultValueType}s for this <code>ScreenResult</code>.
   */
  public SortedSet<ResultValueType> getResultValueTypes() {
    return Collections.unmodifiableSortedSet(_resultValueTypes);
  }

  /**
   * Get the number of replicates (assay plates) associated with this
   * <code>ScreenResult</code>.
   * 
   * @return the number of replicates (assay plates) associated with this
   *         <code>ScreenResult</code>
   * @hibernate.property type="integer" not-null="true"
   */
  public int getReplicateCount() {
    return _replicateCount;
  }
  
  /**
   * Set the number of replicates (assay plates) associated with this
   * <code>ScreenResult</code>.
   * 
   * @param replicateCount the number of replicates (assay plates) associated
   *          with this <code>ScreenResult</code>
   */
  public void setReplicateCount(int replicateCount) {
    _replicateCount = replicateCount;
  }
  
  
  // protected getters and setters
  
  /**
   * Get the version number of the compound.
   * 
   * @return the version number of the <code>ScreenResult</code>
   * @motivation for hibernate
   * @hibernate.version
   */
  protected Integer getVersion() {
    return _version;
  }

  /**
   * Set the version number of the <code>ScreenResult</code>
   * 
   * @param version the new version number for the <code>ScreenResult</code>
   * @motivation for hibernate
   */
  protected void setVersion(Integer version) {
    _version = version;
  }

}
