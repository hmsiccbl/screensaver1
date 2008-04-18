// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.LabCherryPickColumnMajorOrderingComparator;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;


/**
 * A CherryPickRequest ("CPR") provides an abstraction for managing the workflow
 * of producing a set of {@link CherryPickAssayPlate CherryPickAssayPlates} that
 * are to be used to perform a validation {@link Screening screening}. The CPR
 * tracks the set of cherry picks ("CP") that have been requested by a screener.
 * Two types of CPs are managed: {@link ScreenerCherryPick ScreenerCherryPicks}
 * ("SCP") and {@link LabCherryPick LabCherryPicks} ("LCP").
 *
 * @see ScreenerCherryPick, LabCherryPick, CherryPickAssayPlate
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public abstract class CherryPickRequest extends AbstractEntity
{

  // private static data

  private static final Logger log = Logger.getLogger(CherryPickRequest.class);
  private static final long serialVersionUID = 0L;
  /**
   * When the {@link #_legacyCherryPickRequestNumber} is not provided to the constructor, add this
   * offset number to {@link #_ordinal} to determine the cherry pick request number. This value
   * needs to be bigger than the largest ScreenDB visit_id for Cherry Pick Visits.
   */
  private static final int CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET = 10000;

  private static final Set<WellName> DEFAULT_EMPTY_WELL_NAMES = new HashSet<WellName>();
  static {
    for (char row = Well.MIN_WELL_ROW; row <= Well.MAX_WELL_ROW; ++row) {
      for (int col = Well.MIN_WELL_COLUMN; col <= Well.MAX_WELL_COLUMN; ++col) {
        if (row == Well.MIN_WELL_ROW || row == (char) (Well.MIN_WELL_ROW + 1) ||
          row == Well.MAX_WELL_ROW || row == (char) (Well.MAX_WELL_ROW - 1) ||
          col == Well.MIN_WELL_COLUMN || col == Well.MIN_WELL_COLUMN + 1 ||
          col == Well.MAX_WELL_COLUMN || col == Well.MAX_WELL_COLUMN - 1) {
          DEFAULT_EMPTY_WELL_NAMES.add(new WellName(row, col));
        }
      }
    }
  }
  
  
  // private instance data

  private Integer _cherryPickRequestId;
  private Integer _version;
  private Screen _screen;
  private Integer _legacyCherryPickRequestNumber; // ScreenDB visits.id
  private ScreeningRoomUser _requestedBy;
  private Date _dateRequested;
  private PlateType _assayPlateType;
  private BigDecimal _microliterTransferVolumePerWellRequested;
  private BigDecimal _microliterTransferVolumePerWellApproved;
  private AdministratorUser _volumeApprovedBy;
  private Date _dateVolumeApproved;
  private boolean _randomizedAssayPlateLayout;
  private Set<WellName> _emptyWellsOnAssayPlate = new HashSet<WellName>();
  private String _comments;
  private Set<ScreenerCherryPick> _screenerCherryPicks = new HashSet<ScreenerCherryPick>();
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  private int _numberUnfulfilledLabCherryPicks;
  private SortedSet<CherryPickAssayPlate> _cherryPickAssayPlates = new TreeSet<CherryPickAssayPlate>();
  private transient List<CherryPickAssayPlate> _activeAssayPlates;
  private transient HashSet<CherryPickAssayPlate> _pendingAssayPlates;
  private transient HashSet<CherryPickAssayPlate> _completedAssayPlates;


  // public instance methods

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getCherryPickRequestId();
  }

  /**
   * Get the default assay plate type.  This value will be used when creating anew CherryPickRequest
   * @return the default assay plate type; may be null.
   */
  @Transient
  abstract public PlateType getDefaultAssayPlateType();

  /**
   * Get the default requested and approved transfer volume, in microliters. This value will be used when creating a new CherryPickRequest.
   * @return the default approved transfer volume, in microliters; may be null.
   */
  @Transient
  abstract public BigDecimal getDefaultMicroliterTransferVolume();

  /**
   * Get the cherry pick allowance.
   * @return the chery pick allowance
   */
  @Transient
  abstract public int getCherryPickAllowance();

  /**
   * Get the cherry pick allowance used.
   * @return the chery pick allowance used
   */
  @Transient
  abstract public int getCherryPickAllowanceUsed();

  /**
   * Get CherryPickRequest's number. If this entity was imported from a legacy
   * system, this is the ID assigned by the legacy system. Otherwise, it is the
   * entity ID, as assigned by the Screensaver system.
   */
  @Transient
  public Integer getCherryPickRequestNumber()
  {
    if (_legacyCherryPickRequestNumber != null) {
      return _legacyCherryPickRequestNumber;
    }
    return _cherryPickRequestId;
  }

  /**
   * Get the legacy cherry pick request number.
   * @return the legacy cherry pick request number
   */
  @Column(updatable=false)
  @org.hibernate.annotations.Immutable
  public Integer getLegacyCherryPickRequestNumber()
  {
    return _legacyCherryPickRequestNumber;
  }

  /**
   * Get the id for the cherry pick request.
   * @return the id for the cherry pick request
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="cherry_pick_request_id_seq",
    strategy="sequence",
    parameters = {
      @Parameter(name="sequence", value="cherry_pick_request_id_seq"),
      @Parameter(name="parameters", value="start with 10000")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="cherry_pick_request_id_seq")
  public Integer getCherryPickRequestId()
  {
    return _cherryPickRequestId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the screener cherry picks.
   * @return the screener cherry picks
   */
  @OneToMany(
    mappedBy="cherryPickRequest",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<ScreenerCherryPick> getScreenerCherryPicks()
  {
    return _screenerCherryPicks;
  }

  /**
   * Create and return a new screener cherry pick for the cherry pick request
   * @param screenedWell the screened well for the new screener cherry pick
   * @return the new screener cherry pick
   */
  public ScreenerCherryPick createScreenerCherryPick(Well screenedWell)
  {
    ScreenerCherryPick screenerCherryPick = new ScreenerCherryPick(this, screenedWell);
    if (!_screenerCherryPicks.add(screenerCherryPick)) {
      throw new DuplicateEntityException(this, screenerCherryPick);
    }
    return screenerCherryPick;
  }

  /**
   * Get the set lab cherry picks.
   * @return the lab cherry picks
   */
  @OneToMany(
    mappedBy="cherryPickRequest",
    fetch=FetchType.LAZY
  )
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
  }

  /**
   * Get the number of unfulfilled lab cherry picks, indicating whether this cherry pick request is complete.
   * @motivation optimization, to allow Hibernate queries to efficiently determine this value
   * @return the number of unfulfilled lab cherry picks
   */
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public int getNumberUnfulfilledLabCherryPicks()
  {
    return _numberUnfulfilledLabCherryPicks;
  }

  /**
   * Create and return a new lab cherry pick for the cherry pick request.
   * @param sourceWell the source well
   * @param screenerCherryPick the screener cherry pick
   * @return the new lab cherry pick
   * @throws DataModelViolationException whenever the cherry pick request for the provided
   * screener cherry pick does not match the cherry pick request asked to create the lab cherry
   * pick
   */
  public LabCherryPick createLabCherryPick(ScreenerCherryPick screenerCherryPick, Well sourceWell)
  {
    if (! this.equals(screenerCherryPick.getCherryPickRequest())) {
      throw new DataModelViolationException(
        "screener cherry pick has different cherry pick request than the attempted lab cherry pick");
    }
    LabCherryPick labCherryPick = new LabCherryPick(screenerCherryPick, sourceWell);
    _labCherryPicks.add(labCherryPick);
    screenerCherryPick.getLabCherryPicks().add(labCherryPick);
    incUnfulfilledLabCherryPicks();
    return labCherryPick;
  }

  /**
   * Get the set of cherry pick liquid transfers for this cherry pick request.
   * @return the cherry pick liquid transfers
   */
  @Transient
  public Set<CherryPickLiquidTransfer> getCherryPickLiquidTransfers()
  {
    Set<CherryPickLiquidTransfer> cherryPickLiquidTransfers = new HashSet<CherryPickLiquidTransfer>();
    for (CherryPickAssayPlate assayPlate : _cherryPickAssayPlates) {
      if (assayPlate.getCherryPickLiquidTransfer() != null) {
        cherryPickLiquidTransfers.add(assayPlate.getCherryPickLiquidTransfer());
      }
    }
    return cherryPickLiquidTransfers;
  }

  /**
   * Get the set of cherry pick assay plates.
   * @return the set of cherry pick assay plates
   */
  @OneToMany(
    mappedBy="cherryPickRequest",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public SortedSet<CherryPickAssayPlate> getCherryPickAssayPlates()
  {
    return _cherryPickAssayPlates;
  }

  /**
   * <i>Warning: This method will only return a valid result on its first
   * invocation, due to caching of the result; if the status of CPAPs change,
   * you should reload the CPR in a new Hibernate session to obtain an
   * up-to-date result!</i> Get the set of active cherry pick assay plates.
   * 
   * @return the set of active cherry pick assay plates, which excludes any
   *         assay plates that are failed
   */
  @Transient
  public List<CherryPickAssayPlate> getActiveCherryPickAssayPlates()
  {
    if (_activeAssayPlates == null) {
      Map<Integer,CherryPickAssayPlate> plateOrdinalToActiveAssayPlate = new TreeMap<Integer,CherryPickAssayPlate>();
      for (CherryPickAssayPlate assayPlate : _cherryPickAssayPlates) {
        if (!plateOrdinalToActiveAssayPlate.containsKey(assayPlate.getPlateOrdinal()) ||
          assayPlate.getAttemptOrdinal() > plateOrdinalToActiveAssayPlate.get(assayPlate.getPlateOrdinal()).getAttemptOrdinal()) {
          plateOrdinalToActiveAssayPlate.put(assayPlate.getPlateOrdinal(),
                                             assayPlate);
        }
      }
      _activeAssayPlates = new ArrayList<CherryPickAssayPlate>();
      for (Integer plateOrdinal : plateOrdinalToActiveAssayPlate.keySet()) {
        _activeAssayPlates.add(plateOrdinalToActiveAssayPlate.get(plateOrdinal));
      }
    }
    return _activeAssayPlates;
  }

  /**
   * <i>Warning: This method will only return a valid result on its first
   * invocation, due to caching of the result; if the status of CPAPs change,
   * you should reload the CPR in a new Hibernate session to obtain an
   * up-to-date result!</i>
   * 
   * @return the set of "pending" CherryPickAssayPlates, which means they are
   *         neither plated, canceled, nor failed; in other words, they need to
   *         be plated
   */
  @Transient
  public Set<CherryPickAssayPlate> getPendingCherryPickAssayPlates()
  {
    if (_pendingAssayPlates == null) {
      _pendingAssayPlates = new HashSet<CherryPickAssayPlate>();
      for (CherryPickAssayPlate cpap : getCherryPickAssayPlates()) {
        if (! (cpap.isCancelled() || cpap.isFailed() || cpap.isPlated())) {
          _pendingAssayPlates.add(cpap);
        }
      }
    }
    return _pendingAssayPlates;
  }

  /**
   * <i>Warning: This method will only return a valid result on its first
   * invocation, due to caching of the result; if the status of CPAPs change,
   * you should reload the CPR in a new Hibernate session to obtain an
   * up-to-date result!</i>
   * 
   * @return the set of "completed" CherryPickAssayPlates, which means they are
   *         either plated or canceled
   */
  @Transient
  public Set<CherryPickAssayPlate> getCompletedCherryPickAssayPlates()
  {
    if (_completedAssayPlates == null) {
      _completedAssayPlates = new HashSet<CherryPickAssayPlate>();
      for (CherryPickAssayPlate cpap : getCherryPickAssayPlates()) {
        // note: we do not consider 'failed' plates, since they are not "active" (every failed plate will have another active plate created in place of it)
        if (cpap.isCancelled() || cpap.isPlated()) {
          _completedAssayPlates.add(cpap);
        }
      }
    }
    return _completedAssayPlates;
  }

  /**
   * Create and return a new non-legacy cherry pick assay plate for the request.
   * @param plateOrdinal the plate ordinal
   * @param attemptOrdinal the attempt ordinal
   * @param plateType the plate type
   * @return the new cherry pick assay plate
   */
  public CherryPickAssayPlate createCherryPickAssayPlate(
    Integer plateOrdinal,
    Integer attemptOrdinal,
    PlateType plateType)
  {
    _activeAssayPlates = null;
    CherryPickAssayPlate cherryPickAssayPlate = new CherryPickAssayPlate(
      this,
      plateOrdinal,
      attemptOrdinal,
      plateType);
    log.debug("created cpap " + cherryPickAssayPlate);
    for (CherryPickAssayPlate plate : _cherryPickAssayPlates) {
      log.debug("existing cpap: " + plate);
    }
    _cherryPickAssayPlates.add(cherryPickAssayPlate);
    return cherryPickAssayPlate;
  }

  /**
   * Create and return a new legacy cherry pick assay plate for the request.
   * @param plateOrdinal the plate ordinal
   * @param attemptOrdinal the attempt ordinal
   * @param plateType the plate type
   * @param legacyPlateName the legacy plate name
   * @return the new legacy cherry pick assay plate
   */
  public LegacyCherryPickAssayPlate createLegacyCherryPickAssayPlate(
    Integer plateOrdinal,
    Integer attemptOrdinal,
    PlateType plateType,
    String legacyPlateName)
  {
    _activeAssayPlates = null;
    LegacyCherryPickAssayPlate cherryPickAssayPlate = new LegacyCherryPickAssayPlate(
      this,
      plateOrdinal,
      attemptOrdinal,
      plateType,
      legacyPlateName);
    _cherryPickAssayPlates.add(cherryPickAssayPlate);
    return cherryPickAssayPlate;
  }

  /**
   * Get the user that requested the cherry pick.
   * @return the user that requested the cherry pick
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="requestedById", nullable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_to_screening_room_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.OneToMany(unidirectional=true)
  public ScreeningRoomUser getRequestedBy()
  {
    return _requestedBy;
  }

  /**
   * Set the user that requested the cherry pick.
   * @param requestedBy the new user that requested the cherry pick
   */
  public void setRequestedBy(ScreeningRoomUser requestedBy)
  {
    _requestedBy = requestedBy;
  }

  /**
   * Get the administrator user that approved the volume.
   * @return the administrator user that approved the volume
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="volumeApprovedById")
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_to_administrator_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
  public AdministratorUser getVolumeApprovedBy()
  {
    return _volumeApprovedBy;
  }

  /**
   * Set the administrator user that approved the volume.
   * @param volumeApprovedBy the new administrator that approved the volume
   */
  public void setVolumeApprovedBy(AdministratorUser volumeApprovedBy)
  {
    _volumeApprovedBy = volumeApprovedBy;
  }

  /**
   * Get the date of the request
   * @return the date of the request
   */
  @Column(nullable=false)
  public Date getDateRequested()
  {
    return _dateRequested;
  }

  /**
   * Set the date created.
   * @param dateRequested the new date created
   */
  public void setDateRequested(Date dateRequested)
  {
    _dateRequested = truncateDate(dateRequested);
  }

  /**
   * Get the cherry pick assay plate type that will be assigned to each cherry pick assay plate as they are created.
   * @return a PlateType
   */
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.libraries.PlateType$UserType")
  public PlateType getAssayPlateType()
  {
    return _assayPlateType;
  }

  public void setAssayPlateType(PlateType assayPlateType)
  {
    _assayPlateType = assayPlateType;
  }

  /**
   * Get the requested microliterTransferVolumePerWell.
   * @return the microliterTransferVolumePerWell
   */
  @org.hibernate.annotations.Type(type="big_decimal")
  public BigDecimal getMicroliterTransferVolumePerWellRequested()
  {
    return _microliterTransferVolumePerWellRequested;
  }

  /**
   * Set the requested microliterTransferVolumePerWell.
   * @param microliterTransferVolumePerWell the new microliterTransferVolumePerWell
   */
  public void setMicroliterTransferVolumePerWellRequested(BigDecimal microliterTransferVolumePerWell)
  {
    if (microliterTransferVolumePerWell == null) {
      _microliterTransferVolumePerWellRequested = null;
    }
    else {
      if (microliterTransferVolumePerWell.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessRuleViolationException("cherry pick request requested volume must be undefined or > 0");
      }
      _microliterTransferVolumePerWellRequested = microliterTransferVolumePerWell.setScale(Well.VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }

  /**
   * Get the approved microliterTransferVolumePerWell.
   * @return the microliterTransferVolumePerWell
   */
  @org.hibernate.annotations.Type(type="big_decimal")
  public BigDecimal getMicroliterTransferVolumePerWellApproved()
  {
    return _microliterTransferVolumePerWellApproved;
  }

  /**
   * Set the approved microliterTransferVolumePerWell.
   * @param microliterTransferVolumePerWell the new microliterTransferVolumePerWell
   */
  public void setMicroliterTransferVolumePerWellApproved(BigDecimal microliterTransferVolumePerWell)
  {
    if (microliterTransferVolumePerWell == null) {
      _microliterTransferVolumePerWellApproved = null;
    }
    else {
      if (microliterTransferVolumePerWell.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessRuleViolationException("cherry pick request approved volume must be undefined or > 0");
      }
      _microliterTransferVolumePerWellApproved = microliterTransferVolumePerWell.setScale(Well.VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }

  /**
   * Get the date the volume was approved.
   * @return the date the volume was approved
   */
  public Date getDateVolumeApproved()
  {
    return _dateVolumeApproved;
  }

  /**
   * Set the date the volume was approved.
   * @param dateVolumeApproved the new date the volume was approved
   */
  public void setDateVolumeApproved(Date dateVolumeApproved)
  {
    _dateVolumeApproved = truncateDate(dateVolumeApproved);
  }

  /**
   * @return the set of well names that should be left empty when generating the
   *         cherry pick plate mapping.
   */
  @org.hibernate.annotations.CollectionOfElements
  @JoinTable(
    name="cherryPickRequestEmptyWell",
    joinColumns=@JoinColumn(name="cherryPickRequestId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_empty_wells_to_cherry_pick_request")
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="emptyWellOnAssayPlate")
  public Set<WellName> getEmptyWellsOnAssayPlate()
  {
    return _emptyWellsOnAssayPlate;
  }

  /**
   * Add a well to the set of wells to be left empty when generating the cherry
   * pick plate mapping.
   * 
   * @param wellName the well to add
   */
  public boolean addEmptyWellOnAssayPlate(WellName wellName)
  {
    return _emptyWellsOnAssayPlate.add(wellName);
  }

  /**
   * Add the specified wells to the set of requested empty wells.
   * @param wellNames the wells to add
   */
  public void addEmptyWellsOnAssayPlate(Set<WellName> wellNames)
  {
    _emptyWellsOnAssayPlate.addAll(wellNames);
  }

  /**
   * Clear the set of empty wells.
   */
  public void clearEmptyWellsOnAssayPlate()
  {
    _emptyWellsOnAssayPlate.clear();
  }

  /**
   * Return true iff the assay plate layout is randomized.
   * @return true iff the assay plate layout is randomized
   */
  @Column(nullable=false, name="isRandomizedAssayPlateLayout")
  public boolean isRandomizedAssayPlateLayout()
  {
    return _randomizedAssayPlateLayout;
  }

  /**
   * Set whether the assay plate layout is randomized.
   * @param randomizedAssayPlateLayout the new value for whether or not the assay plate
   * layout is randomized
   */
  public void setRandomizedAssayPlateLayout(boolean randomizedAssayPlateLayout)
  {
    _randomizedAssayPlateLayout = randomizedAssayPlateLayout;
  }

  /**
   * Get the comments.
   * @return the comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get whether the lab cherry picks have been allocated, including the case
   * where they were canceled (allocated, mapped, and then deallocated). If at
   * least one lab cherry pick has been allocated the entire cherry pick request
   * is considered allocated.
   * <p>
   * TODO: it might be better to rename this method to 'wasAllocated', or
   * something similar to indicate that it also covers canceled lab cherry
   * picks.
   */
  @Transient
  public boolean isAllocated()
  {
    // this is not efficient, but it's 2007 and we've got cycles to burn, right?
    for (LabCherryPick labCherryPick : _labCherryPicks) {
      if (labCherryPick.isAllocated() || labCherryPick.isCancelled()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true iff this cherry pick request has been mapped to assay plates.
   * @return true iff this cherry pick request has been mapped to assay plates
   */
  @Transient
  public boolean isMapped()
  {
    return _cherryPickAssayPlates.size() > 0;
  }

  /**
   * Return true iff this cherry pick request has been plated to assay plates.
   * @return true iff this cherry pick request has been plated to assay plates
   */
  @Transient
  public boolean isPlated()
  {
    return isMapped() && getPendingCherryPickAssayPlates().size() == 0;
  }

  /**
   * Determine if this CherryPickRequest has a set of lab cherry picks from the
   * same source plate than would fit on a single assay plate. This matters to
   * the lab, which must be notified to manually reload the source plate when
   * creating the cherry pick plates. We can detect this case when the last well
   * (containing a cherry pick) on an assay plate is from the same source plate
   * as the first well on the next assay plate.
   */
  @Transient
  public boolean isSourcePlateReloadRequired()
  {
    return getAssayPlatesRequiringSourcePlateReload().size() > 0;
  }

  /**
   * @see #isSourcePlateReloadRequired()
   */
  @Transient
  public Map<CherryPickAssayPlate, Integer> getAssayPlatesRequiringSourcePlateReload()
  {
    Map<CherryPickAssayPlate,Integer> platesRequiringReload = new HashMap<CherryPickAssayPlate,Integer>();
    LabCherryPick last = null;
    for (CherryPickAssayPlate assayPlate : getActiveCherryPickAssayPlates()) {
      if (assayPlate.getLabCherryPicks().size() > 0) {
        if (last != null) {
          LabCherryPick first = Collections.max(assayPlate.getLabCherryPicks(),
                                                LabCherryPickColumnMajorOrderingComparator.getInstance());
          if (last.getSourceWell().getPlateNumber().equals(first.getSourceWell().getPlateNumber())) {
            platesRequiringReload.put(assayPlate, first.getSourceWell().getPlateNumber());
          }
        }
        last = Collections.max(assayPlate.getLabCherryPicks(),
                               LabCherryPickColumnMajorOrderingComparator.getInstance());
      }
    }
    return platesRequiringReload;
  }


  // package methods

  void incUnfulfilledLabCherryPicks()
  {
    ++_numberUnfulfilledLabCherryPicks;
  }

  void decUnfulfilledLabCherryPicks()
  {
    --_numberUnfulfilledLabCherryPicks;
  }


  // protected constructors

  /**
   * Construct an initialized <code>CherryPickRequest</code>. Intended only for use by {@link
   * Screen#createCherryPickRequest(ScreeningRoomUser, Date, Integer)}.
   * @param screen the screen
   * @param requestedBy the user that made the request
   * @param dateRequested the date created
   * @param legacyId the legacy id from ScreenDB
   * @motivation for creating CherryPickRequests from legacy ScreenDB cherry pick visits
   */
  protected CherryPickRequest(
    Screen screen,
    ScreeningRoomUser requestedBy,
    Date dateRequested,
    Integer legacyId)
  {
    setLegacyCherryPickRequestNumber(legacyId);
    _screen = screen;
    _requestedBy = requestedBy;
    _dateRequested = truncateDate(dateRequested);
    if (getDefaultMicroliterTransferVolume() != null) {
      setMicroliterTransferVolumePerWellRequested(getDefaultMicroliterTransferVolume());
      setMicroliterTransferVolumePerWellApproved(getDefaultMicroliterTransferVolume());
    }
    if (getDefaultAssayPlateType() != null) {
      setAssayPlateType(getDefaultAssayPlateType());
    }
    addEmptyWellsOnAssayPlate(DEFAULT_EMPTY_WELL_NAMES);
  }

  /**
   * Construct an uninitialized <code>CherryPickRequest</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected CherryPickRequest() {}


  // private instance methods

  /**
   * Set the unique identifier for the <code>ScreenResult</code>.
   * @param screenResultId a unique identifier for the <code>ScreenResult</code>
   */
  private void setCherryPickRequestId(Integer cherryPickRequestId)
  {
    if (cherryPickRequestId < CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET) {
      throw new DataModelViolationException("new cherry pick request entity ID " + cherryPickRequestId +
                                            " must be >= " +
                                            CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET );
    }
    _cherryPickRequestId = cherryPickRequestId;
  }

  /**
   * Get the version for the cherry pick request.
   * @return the version for the cherry pick request
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the cherry pick request.
   * @param version the new version for the cherry pick request
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the legacy cherry pick request number
   * @param cherryPickRequestNumber the new legacy cherry pick request number
   */
  private void setLegacyCherryPickRequestNumber(Integer cherryPickRequestNumber)
  {
    if (cherryPickRequestNumber != null &&
      cherryPickRequestNumber >= CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET) {
      throw new DataModelViolationException("legacy cherry pick request numbers must be < " +
                                            CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET);
    }
    _legacyCherryPickRequestNumber = cherryPickRequestNumber;
  }

  /**
   * Set the screen.
   * @param the screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the screener cherry picks.
   * @param cherryPicks the new screener cherry picks
   * @motivation for hibernate
   */
  private void setScreenerCherryPicks(Set<ScreenerCherryPick> screenerCherryPicks)
  {
    _screenerCherryPicks = screenerCherryPicks;
  }

  /**
   * Set the lab cherry picks.
   * @param cherryPicks the new lab cherry picks
   * @motivation for hibernate
   */
  private void setLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }

  private void setNumberUnfulfilledLabCherryPicks(int n)
  {
    _numberUnfulfilledLabCherryPicks = n;
  }

  /**
   * Set the cherry pick assay plates.
   * @param cherryPicks the new cherry pick assay plates
   * @motivation for hibernate
   */
  private void setCherryPickAssayPlates(SortedSet<CherryPickAssayPlate> cherryPickAssayPlates)
  {
    _cherryPickAssayPlates = cherryPickAssayPlates;
  }

  /**
   * @motivation for hibernate
   */
  private void setEmptyWellsOnAssayPlate(Set<WellName> emptyWellsOnAssayPlate)
  {
    _emptyWellsOnAssayPlate = emptyWellsOnAssayPlate;
  }
}
