// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.math.IntRange;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.activities.PlateRange;
import edu.harvard.med.screensaver.util.CollectionUtils;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Maintains the raw data (from screening instrument output) and the screener-provided, curated results that are
 * produced from performing a {@link Screen}. An important curated
 * result of the screen is the set of "screening positive" reagents that have been identified as having the desired
 * biological activity in the screening assay.
 * If a screen is performed in replicate (i.e., multiple, redundant assay plates are created for each library
 * {@link Plate} being screened), then each replicate produces at least one data column of data.
 * Each replicate assay plate may have one or more readouts
 * performed on it, possibly over time intervals and/or with different assay
 * readout technologies. Every distinct raw data readout is a {@link ResultValue} that is stored in a {@link DataColumn}
 * .
 * For example, if 2 replicates are used, with 3 time intervals, and 2 readout types, then the screen result will have
 * 2*3*2=12 data columns.
 * <p/>
 * In addition to the {@link DataColumn}s containing the raw data, a <code>ScreenResult</code> may also contain
 * additional, screener-provided "derived" {@link DataColumn}s for storing calculated values for normalized, scored, and
 * "positive" indicator data. Textual data columns may also be added for storing screener-provided comments.
 * <p/>
 * A <code>ScreenResult</code> becomes the parent of a table of {@link ResultValue}s, with {@link DataColumn}s defining
 * the horizontal axis of the table and {@link AssayWell}s defining the vertical axis of the table.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class ScreenResult extends AuditedAbstractEntity<Integer>
{

  private static final String SCREEN_RESULT_DATA_LOADING_ACTIVITY_COMMENT_PREFIX = "Loaded screen result data.  ";

  private static final long serialVersionUID = 0;

  public static final RelationshipPath<ScreenResult> screen = RelationshipPath.from(ScreenResult.class).to("screen");
  public static final RelationshipPath<ScreenResult> dataColumns = RelationshipPath.from(ScreenResult.class).to("dataColumns");
  public static final RelationshipPath<ScreenResult> assayWells = RelationshipPath.from(ScreenResult.class).to("assayWells");

  private static final Function<IntRange,String> formatPlateNumberRange = new Function<IntRange,String>() {
    public String apply(IntRange range) { return PlateRange.toString(range.getMinimumInteger(), range.getMaximumInteger()); }
  };


  // private instance data

  private Integer _version;
  private Screen _screen;
  private SortedSet<AssayWell> _assayWells = Sets.newTreeSet(); 
  private Integer _replicateCount;
  private Integer _channelCount;
  private SortedSet<DataColumn> _dataColumns = Sets.newTreeSet();

  private Integer _experimentalWellCount = 0; // can't be null


  // public constructor

  /**
   * Construct an initialized <code>ScreenResult</code>. Intended only for use by {@link Screen}.
   */
  public ScreenResult(Screen screen, Integer replicateCount)
  {
    super(null); /* TODO */
    setScreen(screen);
    setReplicateCount(replicateCount);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
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
    return getEntityId();
  }

  @ManyToMany(cascade = { CascadeType.ALL })
  @JoinTable(name="screenResultUpdateActivity", 
             joinColumns=@JoinColumn(name="screenResultId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false))
  @Sort(type=SortType.NATURAL)            
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @OneToOne
  @JoinColumn(name="screenId", nullable=false, updatable=false, unique=true)
  @org.hibernate.annotations.ForeignKey(name="fk_screen_result_to_screen")
  public Screen getScreen()
  {
    return _screen;
  }
  
  public AdministrativeActivity createScreenResultDataLoading(AdministratorUser performedBy,
                                                              Map<Integer,Integer> plateNumbersLoadedWithMaxReplicates,
                                                              String comments)
  {
    Set<AssayPlate> assayPlatesDataLoaded = findOrCreateAssayPlatesDataLoaded(plateNumbersLoadedWithMaxReplicates);
    SortedSet<Integer> plateNumbers = Sets.newTreeSet(Iterables.transform(assayPlatesDataLoaded, AssayPlate.ToPlateNumber));
    String mandatoryComments = "Loaded data for " + plateNumbers.size() + " plates " +
      Joiner.on(",").join(Iterables.transform(CollectionUtils.splitIntoSequentialRanges(plateNumbers),
                                              formatPlateNumberRange));
    
    if (StringUtils.isEmpty(comments)) {
      comments = "";
    }
    comments = Joiner.on(".  ").join(mandatoryComments, comments);

    AdministrativeActivity screenResultDataLoading = 
      getScreen().createUpdateActivity(AdministrativeActivityType.SCREEN_RESULT_DATA_LOADING,
                                       performedBy, 
                                       comments);
    for (AssayPlate assayPlate : assayPlatesDataLoaded) {
      assayPlate.setScreenResultDataLoading(screenResultDataLoading);
    }
    
    return screenResultDataLoading;
  }
  
  private Set<AssayPlate> findOrCreateAssayPlatesDataLoaded(Map<Integer,Integer> plateNumbersLoadedWithMaxReplicates)
  {
    SortedSet<AssayPlate> assayPlatesDataLoaded = Sets.newTreeSet();
    for (Map.Entry<Integer,Integer> entry : plateNumbersLoadedWithMaxReplicates.entrySet()) {
      assayPlatesDataLoaded.addAll(findOrCreateAssayPlatesDataLoaded(entry.getKey(), entry.getValue()));
    }
    return assayPlatesDataLoaded;
  }
  
  private SortedSet<AssayPlate> findOrCreateAssayPlatesDataLoaded(int plateNumber, int replicatesDataLoaded) 
  {
    SortedSet<AssayPlate> mostRecentAssayPlatesForPlateNumber = Sets.newTreeSet();
    SortedSet<AssayPlate> allAssayPlatesForPlateNumber = getScreen().findAssayPlates(plateNumber);
    if (!allAssayPlatesForPlateNumber.isEmpty()) {
      final LibraryScreening lastLibraryScreening =
        ImmutableSortedSet.copyOf(Iterables.transform(allAssayPlatesForPlateNumber, AssayPlate.ToLibraryScreening)).last();
      assert lastLibraryScreening != null;
      mostRecentAssayPlatesForPlateNumber.addAll(Sets.filter(allAssayPlatesForPlateNumber, new Predicate<AssayPlate>() {
        public boolean apply(AssayPlate ap)
        {
          return lastLibraryScreening.equals(ap.getLibraryScreening());
        }
      }));
    }
    SortedSet<AssayPlate> assayPlatesDataLoaded = Sets.newTreeSet();
    // if there are fewer assay plates screened replicates than we have data
    // for, then a library screening must not have been recorded for the assay
    // plates that were used to generate this data, so we'll create them now
    if (mostRecentAssayPlatesForPlateNumber.size() < replicatesDataLoaded) {
      //log.warn("creating missing assay plate(s) for plate number " + plateNumber);
      for (int r = 0; r < replicatesDataLoaded; r++) {
        assayPlatesDataLoaded.add(getScreen().createAssayPlate(plateNumber, r));
      }
    }
    else {
      for (AssayPlate assayPlate : mostRecentAssayPlatesForPlateNumber) {
        if (assayPlate.getReplicateOrdinal() < replicatesDataLoaded) {
          assayPlatesDataLoaded.add(assayPlate);
        }
      }
    }
    return assayPlatesDataLoaded;
  }

  /**
   * The last {@link AdministrativeActivityType#SCREEN_RESULT_DATA_LOADING}
   * screen result data loading activity that full or incremental data was
   * loaded for this ScreenResult.
   */
  @Transient
  public AdministrativeActivity getLastDataLoadingActivity()
  {
    SortedSet<AdministrativeActivity> screenResultDataLoadings =
      Sets.newTreeSet(Iterables.filter(getScreen().getUpdateActivities(), 
                                       AdministrativeActivityType.SCREEN_RESULT_DATA_LOADING.isValuePredicate()));
    if (screenResultDataLoadings.isEmpty()) {
      return null;
    }
    return screenResultDataLoadings.last();
  }

  /**
   * Get the assay readout types.
   * @return the assay readout types
   */
  @Transient
  public Set<AssayReadoutType> getAssayReadoutTypes()
  {
    Set<AssayReadoutType> assayReadoutTypes = new HashSet<AssayReadoutType>();
    for (DataColumn col : getDataColumns()) {
      if (col.getAssayReadoutType() != null) {
        assayReadoutTypes.add(col.getAssayReadoutType());
      }
    }
    return assayReadoutTypes;
  }


  /**
   * Get the ordered set of all {@link DataColumn}s for this screen result.
   * @return the ordered set of all {@link DataColumn}s for this screen
   * result.
   */
  @OneToMany(mappedBy = "screenResult", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  public SortedSet<DataColumn> getDataColumns()
  {
    return _dataColumns;
  }

  /**
   * Create and return a new data column for the screen result.
   * @param name the name of this data column
   * @return the new data column
   */
  public DataColumn createDataColumn(String name)
  {
    verifyNameIsUnique(name);
    DataColumn dataColumn = new DataColumn(this, name);
    _dataColumns.add(dataColumn);
    return dataColumn;
  }

  public boolean deleteDataColumn(DataColumn dataColumn)
  {
    if (!dataColumn.getDerivedTypes().isEmpty()) {
      throw new DataModelViolationException("cannot delete " + dataColumn + " since it is derived from " + dataColumn.getDerivedTypes());
    }
    Set<DataColumn> dissociateFrom = new HashSet<DataColumn>();
    for (DataColumn derivedFrom : dataColumn.getTypesDerivedFrom()) {
      dissociateFrom.add(derivedFrom);
    }
    for (DataColumn derivedFrom : dissociateFrom) {
      derivedFrom.removeDerivedType(dataColumn);
    }
    return getDataColumns().remove(dataColumn);
  }

  /**
   * Get the number of replicates (assay plates) associated with this screen result. If the
   * replicate count was not explicitly specified at instantiation time, calculate the replicate
   * count by finding the maximum replicate ordinal value from the screen result's
   * data columns; if none of the data columns have their replicate
   * ordinal values defined, replicate count is 1.
   *
   * @return the number of replicates (assay plates) associated with this
   *         <code>ScreenResult</code>
   */
  @Column(nullable=false)
  public Integer getReplicateCount()
  {
    if (_replicateCount == null) {
      if (getDataColumns().size() == 0) {
        _replicateCount = 0;
      }
      else {
        DataColumn maxOrdinalCol =
          Collections.max(getDataColumns(),
            new Comparator<DataColumn>()
            {
              public int compare(DataColumn col1, DataColumn col2)
              {
                if (col1.getReplicateOrdinal() == null && col2.getReplicateOrdinal() == null) {
                  return 0;
                }
                if (col1.getReplicateOrdinal() == null && col2.getReplicateOrdinal() != null) {
                  return -1;
                }
                if (col1.getReplicateOrdinal() != null && col2.getReplicateOrdinal() == null) {
                  return 1;
                }
                return col1.getReplicateOrdinal().compareTo(col2.getReplicateOrdinal());
              }
            } );
        _replicateCount = maxOrdinalCol.getReplicateOrdinal();
        if (_replicateCount == null) {
          // every DataColumn had null replicateOrdinal value
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
   * Get the number of channels (assay plates) associated with this screen result. If the
   * channel count was not explicitly specified at instantiation time, calculate the channel
   * count by finding the maximum channel ordinal value from the screen result's
   * DataColumns; if none of the DataColumns have their channel
   * ordinal values defined, channel count is 1.
   * 
   * @return the number of channels (assay plates) associated with this <code>ScreenResult</code>
   */
  @Column(nullable=false)
  public Integer getChannelCount()
  {
    if (_channelCount == null) {
      if (getDataColumns().size() == 0) {
        _channelCount = 0;
      }
      else {
        DataColumn maxOrdinalRvt =
          Collections.max(getDataColumns(),
                          new Comparator<DataColumn>()
            {
              public int compare(DataColumn rvt1, DataColumn rvt2)
              {
                if (rvt1.getChannel() == null && rvt2.getChannel() == null) {
                  return 0;
                }
                if (rvt1.getChannel() == null && rvt2.getChannel() != null) {
                  return -1;
                }
                if (rvt1.getChannel() != null && rvt2.getChannel() == null) {
                  return 1;
                }
                return rvt1.getChannel().compareTo(rvt2.getChannel());
              }
            } );
        _channelCount = maxOrdinalRvt.getChannel();
        if (_channelCount == null) {
          // every DataColumn had null Channel value
          _channelCount = 1;
        }
      }
    }
    return _channelCount;
  }

  /**
   * Set the number of channels (assay plates) associated with this screen result.
   * @param channelCount the new number of channels (assay plates) associated with this
   * screen result
   */
  public void setChannelCount(Integer channelCount)
  {
    _channelCount = channelCount;
  }

  @OneToMany(mappedBy = "screenResult", cascade = { CascadeType.ALL })
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  public SortedSet<AssayWell> getAssayWells()
  {
    return _assayWells;
  }
  
  private void setAssayWells(SortedSet<AssayWell> assayWells)
  {
    _assayWells = assayWells;
  }
  
  public AssayWell createAssayWell(Well libraryWell)
  {
    AssayWell assayWell = new AssayWell(this, libraryWell);
    if (!_assayWells.add(assayWell)) {
      throw new DuplicateEntityException(this, assayWell);
    }
    return assayWell;
  }

  /**
   * Get the number of experimental wells that have data in this screen result.
   * @return the number of experimental wells that have data in this screen result
   * @motivation optimization
   */
  // TODO: move this to Screen, to be with other screening/loading statistics 
  @Column(nullable=false)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Integer getExperimentalWellCount()
  {
    return _experimentalWellCount;
  }

  public void setExperimentalWellCount(Integer experimentalWellCount)
  {
    _experimentalWellCount = experimentalWellCount;
  }

  @Transient
  public Set<Integer> getPlateNumbers()
  {
    return Sets.newHashSet(Iterables.transform(getAssayWells(), AssayWell.ToPlateNumber));
  }

  /**
   * Return a list of DataColumns
   * @return an ordered list of DataColumns
   * @motivation random access to DataColumns by ordinal
   */
  @Transient
  public List<DataColumn> getDataColumnsList()
  {
    return Lists.newArrayList(_dataColumns);
  }

  @Transient
  public List<DataColumn> getPartitionedPositivesDataColumns()
  {
    return Lists.newArrayList(Iterables.filter(_dataColumns, DataColumn.isPositiveIndicator));
  }

  /**
   * Return the subset of DataColumns that contain numeric ResultValue data.
   * @return the subset of DataColumns that contain numeric ResultValue data
   */
  @Transient
  public List<DataColumn> getNumericDataColumns()
  {
    List<DataColumn> numericDataColumns = new ArrayList<DataColumn>();
    for (DataColumn col : getDataColumns()) {
      if (col.isNumeric()) {
        numericDataColumns.add(col);
      }
    }
    return numericDataColumns;
  }

  /**
   * Return the subset of DataColumns that contain raw, numeric ResultValue data.
   * @return the subset of DataColumns that contain raw, numeric ResultValue data
   */
  @Transient
  public List<DataColumn> getRawNumericDataColumns()
  {
    List<DataColumn> rawNumericDataColumns = new ArrayList<DataColumn>();
    for (DataColumn col : getDataColumns()) {
      if (!col.isDerived() && col.isNumeric()) {
        rawNumericDataColumns.add(col);
      }
    }
    return rawNumericDataColumns;
  }
  
  /**
   * Construct an uninitialized <code>ScreenResult</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ScreenResult() {}

  /**
   * Set the id for the screen result.
   * @param screenResultId the id for the screen result
   */
  private void setScreenResultId(Integer screenResultId)
  {
    setEntityId(screenResultId);
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
   * Set the ordered set of all {@link DataColumn}s for this screen result.
   * @param dataColumns the new ordered set of all {@link DataColumn}s
   * for this screen result.
   * @motivation for hibernate
   */
  private void setDataColumns(SortedSet<DataColumn> dataColumns)
  {
    _dataColumns = dataColumns;
  }

  private void verifyNameIsUnique(String name)
  {
    for (DataColumn col : getDataColumns()) {
      if (col.getName().equals(name)) {
        throw new DuplicateEntityException(this, col);
      }
    }
  }
}
