// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;

/**
 * A <code>ScreenResult</code> represents the data produced by machine-reading
 * each of the assay plates associated with a
 * {@link edu.harvard.med.screensaver.model.screens.Screen}. Each stock plate
 * of the library being screened will be replicated across one or more assay
 * plates ("replicates"). Each replicate assay plate can have one or more
 * readouts performed on it, possibly over time intervals and/or with different
 * assay readout technologies. Every distinct readout type is identified by a
 * {@link ResultValueType}. A <code>ScreenResult</code> becomes the parent of
 * {@link ResultValue}s. For visualization purposes, one can imagine a
 * <code>ScreenResult</code> as representing a spreadsheet, where the column
 * headings are represented by {@link ResultValueType}s and the rows are
 * identified by stock plate {@link Well}s, and each row contains a
 * {@link ResultValue} for each {@link ResultValueType} "column".
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class ScreenResult extends AbstractEntity
{

  private static final long serialVersionUID = 0;


  // private instance data

  private Integer _screenResultId;
  private Integer _version;
  private Screen _screen;
  private SortedSet<Well> _wells = new TreeSet<Well>();
  private Date _dateCreated;
  private Date _dateLastImported;
  private boolean _isShareable;
  private Integer _replicateCount;
  private SortedSet<ResultValueType> _resultValueTypes = new TreeSet<ResultValueType>();
  /**
   * @motivation optimization, to avoid loading inspecting all ResultValues when
   *             determining the set of plate numbers associated with this
   *             ScreenResult. Note that our data model does not represent
   *             Plates as first-order entities, as an optimization; a plate
   *             number is therefore stored along with each Well, but we have no
   *             normalized Plate table.
   */
  private SortedSet<Integer> _plateNumbers = new TreeSet<Integer>();
  private Integer _experimentalWellCount;

  private transient UniqueDataHeaderNames _uniqueDataHeaderNames;


  // public constructor

  /**
   * Construct an initialized <code>ScreenResult</code>. Intended only for use by {@link
   * Screen#createScreenResult(Date)} and {@link Screen#createScreenResult(Date, boolean, Integer)}.
   * @param screen the screen
   * @param dateCreated the date the screen result's data was initially created
   * @param isShareable whether this screen result can be viewed by all users of the system
   * @param replicateCount
   */
  public ScreenResult(Screen screen, Date dateCreated, boolean isShareable, Integer replicateCount)
  {
    setScreen(screen);
    setDateCreated(dateCreated);
    setDateLastImported(new Date());
    setShareable(isShareable);
    setReplicateCount(replicateCount);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getScreenResultId();
  }

  /**
   * Get the id for the screen result.
   * @return the id for the screen result
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="screen_result_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="screen_result_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="screen_result_id_seq")
  public Integer getScreenResultId()
  {
    return _screenResultId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @OneToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false, unique=true)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_screen_result_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the date that this <code>ScreenResult</code>'s data was initially created.
   * @return the date that this <code>ScreenResult</code>'s data was initially created
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="date")
  public Date getDateCreated()
  {
    return _dateCreated;
  }

  /**
   * Set the date that this <code>ScreenResult</code>'s data was initially created.
   * @param dateCreated the new date that this <code>ScreenResult</code>'s data was initially
   * created
   */
  public void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated);
  }

  /**
   * Get the date this <code>ScreenResult</code> was last imported into Screensaver.
   * @return the date this <code>ScreenResult</code> was last imported into Screensaver
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="date")
  public Date getDateLastImported()
  {
    return _dateLastImported;
  }

  /**
   * Set the date this <code>ScreenResult</code> was last imported into Screensaver.
   * @param dateImported the new date this <code>ScreenResult</code> was last imported into
   * Screensaver
   */
  public void setDateLastImported(Date dateImported)
  {
    _dateLastImported = truncateDate(dateImported);
  }

  /**
   * Get whether this screen result can be viewed by all users of
   * the system; that is, {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser
   * ScreeningRoomUsers} other than those associated with the {@link Screen}.
   * @return true iff this <code>ScreenResult</code> is shareable among all users
   */
  @Column(nullable=false, name="isShareable")
  public boolean isShareable()
  {
    return _isShareable;
  }

  /**
   * Set whether this <code>ScreenResult</code> can be viewed by all users of
   * the system; that is, {@link edu.harvard.med.screensaver.model.users.ScreeningRoomUser
   * ScreeningRoomUsers} other than those associated with the {@link Screen}.
   * @param isShareable true iff this <code>ScreenResult</code> is shareable among all users
   */
  public void setShareable(boolean isShareable)
  {
    _isShareable = isShareable;
  }

  /**
   * Get the ordered set of all {@link ResultValueType ResultValueTypes} for this screen result.
   * @return the ordered set of all {@link ResultValueType ResultValueTypes} for this screen
   * result.
   */
  @OneToMany(
    mappedBy="screenResult",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("ordinal")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public SortedSet<ResultValueType> getResultValueTypes()
  {
    return _resultValueTypes;
  }

  /**
   * Create and return a new result value type for the screen result.
   * @param name the name of this result value type
   * @return the new result value type
   */
  public ResultValueType createResultValueType(String name)
  {
    return createResultValueType(name, null, false, false, false, null);
  }

  /**
   * Create and return a new result value type for the screen result.
   * @param name the name of this result value type
   * @param replicateOrdinal the replicate ordinal
   * @param isDerived true iff the result value type is derived from other result value types
   * @param isPositiveIndicator true iff the result value type is an positive indicator
   * @param isFollowupData true iff the result value type contains follow up data
   * @param assayPhenotype the assay phenotype
   * @return the new result value type
   */
  public ResultValueType createResultValueType(
    String name,
    Integer replicateOrdinal,
    boolean isDerived,
    boolean isPositiveIndicator,
    boolean isFollowupData,
    String assayPhenotype)
  {
    ResultValueType resultValueType = new ResultValueType(
      this,
      name,
      replicateOrdinal,
      isDerived,
      isPositiveIndicator,
      isFollowupData,
      assayPhenotype);
    _resultValueTypes.add(resultValueType);
    return resultValueType;
  }

  /**
   * Get the unique data header names
   * @return the unique data header names
   * @see UniqueDataHeaderNames
   */
  @Transient
  public UniqueDataHeaderNames getUniqueDataHeaderNames()
  {
    if (_uniqueDataHeaderNames == null) {
      _uniqueDataHeaderNames = new UniqueDataHeaderNames(this);
    }
    return _uniqueDataHeaderNames;
  }

  /**
   * Get the number of replicates (assay plates) associated with this screen result. If the
   * replicate count was not explicitly specified at instantiation time, calculate the replicate
   * count by finding the maximum replicate ordinal value from the screen result's
   * ResultValueTypes; if none of the ResultValueTypes have their replicate
   * ordinal values defined, replicate count is 1.
   *
   * @return the number of replicates (assay plates) associated with this
   *         <code>ScreenResult</code>
   */
  @Column(nullable=false)
  public Integer getReplicateCount()
  {
    if (_replicateCount == null) {
      if (getResultValueTypes().size() == 0) {
        _replicateCount = 0;
      }
      else {
        ResultValueType maxOrdinalRvt =
          Collections.max(getResultValueTypes(),
            new Comparator<ResultValueType>()
            {
              public int compare(ResultValueType rvt1, ResultValueType rvt2)
              {
                if (rvt1.getReplicateOrdinal() == null && rvt2.getReplicateOrdinal() == null) {
                  return 0;
                }
                if (rvt1.getReplicateOrdinal() == null && rvt2.getReplicateOrdinal() != null) {
                  return -1;
                }
                if (rvt1.getReplicateOrdinal() != null && rvt2.getReplicateOrdinal() == null) {
                  return 1;
                }
                return rvt1.getReplicateOrdinal().compareTo(rvt2.getReplicateOrdinal());
              }
            } );
        _replicateCount = maxOrdinalRvt.getReplicateOrdinal();
        if (_replicateCount == null) {
          // every ResultValueType had null replicateOrdinal value
          _replicateCount = 1;
        }
      }
    }
    return _replicateCount;
  }

  /**
   * Set the number of replicates (assay plates) associated with this screen result.
   * @param replicateCount the new number of replicates (assay plates) associated with this
   * screen result
   */
  public void setReplicateCount(Integer replicateCount)
  {
    _replicateCount = replicateCount;
  }

  /**
   * Get the set of plate numbers associated with this screen result (via ResultValue Wells).
   * @return the set of plate numbers associated with this screen result
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="plateNumber", nullable=false)
  @JoinTable(
    name="screenResultPlateNumber",
    joinColumns=@JoinColumn(name="screenResultId")
  )
  @OrderBy("plateNumber")
  @org.hibernate.annotations.ForeignKey(name="fk_screen_result_plate_number_to_screen_result")
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public SortedSet<Integer> getPlateNumbers()
  {
    return _plateNumbers;
  }

  /**
   * Get the number of plate numbers associated with this screen result.
   * @return the number of plate numbers associated with this screen result
   * @motivation JSF EL 1.1 does not provide a size/length operator for collections.
   */
  @Transient
  public int getPlateNumberCount()
  {
    return _plateNumbers.size();
  }

  /**
   * Get the set of wells associated with this screen result. <i>Do not modify
   * the returned collection.</i> To add a well, call {@link #addWell}.
   * @return the set of wells associated with this screen result
   */
  @ManyToMany(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(
    name="screenResultWellLink",
    joinColumns=@JoinColumn(name="screenResultId"),
    inverseJoinColumns=@JoinColumn(name="wellId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_screen_result_well_link_to_screen_result")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  public SortedSet<Well> getWells()
  {
    return _wells;
  }

  /**
   * Get the number of wells associated with this screen result.
   * @return the number of wells associated with this screen result
   * @motivation JSF EL 1.1 does not provide a size/length operator for collections.
   */
  @Transient
  public int getWellCount()
  {
    return _wells.size();
  }

  /**
   * Add a well that is associated with this ScreenResult.
   * @param well the well to add
   * @return true iff the well was added successfully
   */
  public boolean addWell(Well well)
  {
    _plateNumbers.add(well.getPlateNumber());
    return _wells.add(well);
  }

  /**
   * Get the number of experimental wells that have data in this screen result.
   * @return the number of experimental wells that have data in this screen result
   * @motivation optimization
   */
  @Column(nullable=false)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Integer getExperimentalWellCount()
  {
    return _experimentalWellCount;
  }

  /**
   * Return a list of ResultValueTypes
   * @return an ordered list of ResultValueTypes
   * @motivation random access to ResultValueTypes by ordinal
   */
  @Transient
  public List<ResultValueType> getResultValueTypesList()
  {
    return new ArrayList<ResultValueType>(_resultValueTypes);
  }

  /**
   * Return the subset of ResultValueTypes that contain numeric ResultValue data.
   * @return the subset of ResultValueTypes that contain numeric ResultValue data
   */
  @Transient
  public List<ResultValueType> getNumericResultValueTypes()
  {
    List<ResultValueType> numericResultValueTypes = new ArrayList<ResultValueType>();
    for (ResultValueType rvt : getResultValueTypes()) {
      if (rvt.isNumericalnessDetermined() && rvt.isNumeric()) {
        numericResultValueTypes.add(rvt);
      }
    }
    return numericResultValueTypes;
  }


  // package instance method

  /**
   * Increment the number of experimental wells that have data in this screen result.
   * Intended only for use by {@link ResultValueType#addResultValue(ResultValue, Well)}.
   * @see #getExperimentalWellCount()
   */
  void incrementExperimentalWellCount()
  {
    _experimentalWellCount ++;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>ScreenResult</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ScreenResult() {}


  // private instance methods

  /**
   * Set the id for the screen result.
   * @param screenResultId the id for the screen result
   */
  private void setScreenResultId(Integer screenResultId)
  {
    _screenResultId = screenResultId;
  }

  /**
   * Get the version number of the screen result.
   * @return the version number of the screen result
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version number of the screen result.
   * @param version the new version number of the screen result
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the screen.
   * @param screen the new screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the ordered set of all {@link ResultValueType ResultValueTypes} for this screen result.
   * @param resultValueTypes the new ordered set of all {@link ResultValueType ResultValueTypes}
   * for this screen result.
   * @motivation for hibernate
   */
  private void setResultValueTypes(SortedSet<ResultValueType> resultValueTypes)
  {
    _resultValueTypes = resultValueTypes;
  }

  /**
   * Set the set of plate numbers associated with this screen result (via ResultValue Wells).
   * @param plateNumbers the new set of plate numbers
   * @motivation for Hibernate
   */
  private void setPlateNumbers(SortedSet<Integer> plateNumbers)
  {
    _plateNumbers = plateNumbers;
  }

  /**
   * Set the set of wells associated with this ScreenResult.
   * @param well the set of wells
   * @motivation for Hibernate
   */
  private void setWells(SortedSet<Well> wells)
  {
    _wells = wells;
  }

  /**
   * @motivation for Hibernate
   * @param experimentalWellCount
   */
  private void setExperimentalWellCount(Integer experimentalWellCount)
  {
    _experimentalWellCount = experimentalWellCount;
  }
}
