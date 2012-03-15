// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


/**
 * A CherryPickRequest ("CPR") provides an abstraction for managing the workflow
 * of producing a set of {@link CherryPickAssayPlate CherryPickAssayPlates} that
 * are to be used to perform a validation {@link Screening screening}. The CPR
 * tracks the set of cherry picks ("CP") that have been requested by a screener.
 * Two types of CPs are managed: {@link ScreenerCherryPick ScreenerCherryPicks}
 * ("SCP") and {@link LabCherryPick LabCherryPicks} ("LCP").
 *
 * @see ScreenerCherryPick
 * @see LabCherryPick
 * @see CherryPickAssayPlate
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public abstract class CherryPickRequest extends AuditedAbstractEntity<Integer>
{
  private static final Logger log = Logger.getLogger(CherryPickRequest.class);
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<CherryPickRequest> thisEntity = RelationshipPath.from(CherryPickRequest.class);
  public static final RelationshipPath<CherryPickRequest> screen = thisEntity.to("screen", Cardinality.TO_ONE);
  public static final RelationshipPath<CherryPickRequest> requestedBy = thisEntity.to("requestedBy", Cardinality.TO_ONE);
  public static final RelationshipPath<CherryPickRequest> volumeApprovedBy = thisEntity.to("volumeApprovedBy", Cardinality.TO_ONE);
  public static final PropertyPath<CherryPickRequest> emptyWellsOnAssayPlate = thisEntity.toCollectionOfValues("emptyWellsOnAssayPlate");
  public static final RelationshipPath<CherryPickRequest> screenerCherryPicks = thisEntity.to("screenerCherryPicks");
  public static final RelationshipPath<CherryPickRequest> labCherryPicks = thisEntity.to("labCherryPicks");
  public static final RelationshipPath<CherryPickRequest> cherryPickAssayPlates = thisEntity.to("cherryPickAssayPlates");
  public static final RelationshipPath<CherryPickRequest> cherryPickScreenings = thisEntity.to("cherryPickScreenings");

  private Integer _version;
  private Screen _screen;
  private Integer _legacyCherryPickRequestNumber; // ScreenDB visits.id
  private ScreeningRoomUser _requestedBy;
  private LocalDate _dateRequested;
  private PlateType _assayPlateType;
  private Volume _transferVolumePerWellRequested;
  private Volume _transferVolumePerWellApproved;
  private AdministratorUser _volumeApprovedBy;
  private LocalDate _dateVolumeApproved;
  private boolean _randomizedAssayPlateLayout;
  private Integer _maxSkippedWellsPerPlate;
  private Set<WellName> _emptyWellsOnAssayPlate = new HashSet<WellName>();
  private String _comments;
  private Set<ScreenerCherryPick> _screenerCherryPicks = new HashSet<ScreenerCherryPick>();
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  private int _numberUnfulfilledLabCherryPicks;
  private SortedSet<CherryPickAssayPlate> _cherryPickAssayPlates = new TreeSet<CherryPickAssayPlate>();
  private transient List<CherryPickAssayPlate> _activeAssayPlates;
  private transient HashSet<CherryPickAssayPlate> _pendingAssayPlates;
  private transient HashSet<CherryPickAssayPlate> _completedAssayPlates;
  private transient SortedSet<WellKey> _labCherryPickWellKeys;
  private Set<CherryPickScreening> _cherryPickScreenings = new HashSet<CherryPickScreening>();

  // assay protocol-related fields
  private CherryPickAssayProtocolsFollowed _cherryPickAssayProtocolsFollowed;
  private String _cherryPickAssayProtocolComments;
  private CherryPickFollowupResultsStatus _cherryPickFollowupResultsStatus;
  private boolean _keepSourcePlateCherryPicksTogether = true;


  /**
   * Get the default assay plate type.  This value will be used when creating anew CherryPickRequest
   * @return the default assay plate type; may be null.
   */
  @Transient
  abstract public PlateType getDefaultAssayPlateType();

  /**
   * Get the default requested and approved transfer volume. This value will be
   * used when creating a new CherryPickRequest.
   *
   * @return the default approved transfer volume; may be null.
   */
  @Transient
  abstract public Volume getDefaultTransferVolume();

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
    return getEntityId();
  }

  /**
   * Get the legacy cherry pick request number.
   * @return the legacy cherry pick request number
   */
  @Column(updatable=false)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true) /* no new CPRs should use this property */
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
    return getEntityId();
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="cherryPickRequestUpdateActivity", 
             joinColumns=@JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false, unique=true))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
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
  @ManyToOne
  @JoinColumn(name="screenId", nullable=false, updatable=false)
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
  @OneToMany(mappedBy = "cherryPickRequest", cascade = { CascadeType.ALL })
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
  @OneToMany(mappedBy = "cherryPickRequest")
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
    cascade = { CascadeType.ALL },
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
   * @return the set of active cherry pick assay plates, which is the CPAP with the largest attemptOrdinal, for each
   *         distinct plate name.
   */
  @Transient
  public List<CherryPickAssayPlate> getActiveCherryPickAssayPlates()
  {
    if (_activeAssayPlates == null) {
      Map<Integer,CherryPickAssayPlate> plateOrdinalToActiveAssayPlate = new TreeMap<Integer,CherryPickAssayPlate>();
      for (CherryPickAssayPlate assayPlate : _cherryPickAssayPlates) {
        if (!plateOrdinalToActiveAssayPlate.containsKey(assayPlate.getPlateOrdinal()) ||
          assayPlate.getAttemptOrdinal() > plateOrdinalToActiveAssayPlate.get(assayPlate.getPlateOrdinal()).getAttemptOrdinal()) {
          plateOrdinalToActiveAssayPlate.put(assayPlate.getPlateOrdinal(), assayPlate);
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
   * @deprecated use {@link #createCherryPickAssayPlate(Integer, Integer, PlateType)} for all new {@link CherryPickAssayPlate}s
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
  @edu.harvard.med.screensaver.model.annotations.ToMany(unidirectional=true)
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
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true)
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
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateRequested()
  {
    return _dateRequested;
  }

  /**
   * Set the date created.
   * @param dateRequested the new date created
   */
  public void setDateRequested(LocalDate dateRequested)
  {
    _dateRequested = dateRequested;
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
   * Get the requested transferVolumePerWell.
   * @return the transferVolumePerWell
   */
  @Column(precision=ScreensaverConstants.VOLUME_PRECISION, scale=ScreensaverConstants.VOLUME_SCALE)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.db.usertypes.VolumeType")
  public Volume getTransferVolumePerWellRequested()
  {
    return _transferVolumePerWellRequested;
  }

  /**
   * Set the requested transferVolumePerWell.
   * @param transferVolumePerWell the new transferVolumePerWell
   */
  public void setTransferVolumePerWellRequested(Volume transferVolumePerWell)
  {
    if (transferVolumePerWell != null && transferVolumePerWell.compareTo(VolumeUnit.ZERO) <= 0) {
      throw new BusinessRuleViolationException("cherry pick request approved volume must be undefined or > 0");
    }
    _transferVolumePerWellRequested = transferVolumePerWell;
  }

  /**
   * Get the approved transferVolumePerWell.
   * @return the transferVolumePerWell
   */
  @Column(precision=ScreensaverConstants.VOLUME_PRECISION, scale=ScreensaverConstants.VOLUME_SCALE)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.db.usertypes.VolumeType")
  public Volume getTransferVolumePerWellApproved()
  {
    return _transferVolumePerWellApproved;
  }

  /**
   * Set the approved transferVolumePerWell.
   * @param transferVolumePerWell the new transferVolumePerWell
   */
  public void setTransferVolumePerWellApproved(Volume transferVolumePerWell)
  {
    if (transferVolumePerWell != null && transferVolumePerWell.compareTo(VolumeUnit.ZERO) <= 0) {
      throw new BusinessRuleViolationException("cherry pick request approved volume must be undefined or > 0");
    }
    _transferVolumePerWellApproved = transferVolumePerWell;
  }

  /**
   * Get the date the volume was approved.
   * @return the date the volume was approved
   */
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateVolumeApproved()
  {
    return _dateVolumeApproved;
  }

  /**
   * Set the date the volume was approved.
   * @param dateVolumeApproved the new date the volume was approved
   */
  public void setDateVolumeApproved(LocalDate dateVolumeApproved)
  {
    _dateVolumeApproved = dateVolumeApproved;
  }

  /**
   * @return the set of well names that should be left empty when generating the
   *         cherry pick plate mapping.
   */
  @ElementCollection
  @JoinTable(
    name="cherryPickRequestEmptyWell",
    joinColumns=@JoinColumn(name="cherryPickRequestId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_empty_wells_to_cherry_pick_request")
  @edu.harvard.med.screensaver.model.annotations.ElementCollection(singularPropertyName="emptyWellOnAssayPlate") 
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

  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayProtocolsFollowed$UserType")
  public CherryPickAssayProtocolsFollowed getCherryPickAssayProtocolsFollowed()
  {
    return _cherryPickAssayProtocolsFollowed;
  }

  public void setCherryPickAssayProtocolsFollowed(CherryPickAssayProtocolsFollowed cherryPickAssayProtocolsFollowed)
  {
    _cherryPickAssayProtocolsFollowed = cherryPickAssayProtocolsFollowed;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getAssayProtocolComments()
  {
    return _cherryPickAssayProtocolComments;
  }

  public void setAssayProtocolComments(String assayProtocolComments)
  {
    _cherryPickAssayProtocolComments = assayProtocolComments;
  }

  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.cherrypicks.CherryPickFollowupResultsStatus$UserType")
  public CherryPickFollowupResultsStatus getCherryPickFollowupResultsStatus()
  {
    return _cherryPickFollowupResultsStatus;
  }

  public void setCherryPickFollowupResultsStatus(CherryPickFollowupResultsStatus cherryPickFollowupResultsStatus)
  {
    _cherryPickFollowupResultsStatus = cherryPickFollowupResultsStatus;
  }
 
  /**
   * Get the maximum number of wells the plate mapper can skip from the available wells
   * to avoid having to reload a source plate.
   */
  public Integer getMaxSkippedWellsPerPlate() {
	return _maxSkippedWellsPerPlate;
  }

  public void setMaxSkippedWellsPerPlate(Integer maxSkippedWellsPerPlate) {
	_maxSkippedWellsPerPlate = maxSkippedWellsPerPlate;
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

  void incUnfulfilledLabCherryPicks()
  {
    ++_numberUnfulfilledLabCherryPicks;
  }

  public void decUnfulfilledLabCherryPicks()
  {
    --_numberUnfulfilledLabCherryPicks;
  }


  /**
   * Get the RNAiCherryPickScreenings for this RNAiCherryPickRequest.
   * <p>
   * Note: This is a non-cascading relationship; RNAiCherryPickScreenings are
   * cascaded from {@link Screen#getLabActivities()} instead.
   */
  @OneToMany(mappedBy="cherryPickRequest",
             cascade={},
             fetch=FetchType.LAZY)
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="cherryPickScreening")
  public Set<CherryPickScreening> getCherryPickScreenings()
  {
    return _cherryPickScreenings;
  }
  
  /**
   * Get if this CherryPickRequest has been screened (has at least one
   * associated CherryPickScreening activity).
   */
  @Transient
  public boolean isScreened()
  {
    return _cherryPickScreenings.size() > 0;
  }

  private void setCherryPickScreenings(Set<CherryPickScreening> cherryPickScreenings)
  {
    _cherryPickScreenings = cherryPickScreenings;
  }

  
  /**
   * Construct an initialized <code>CherryPickRequest</code>. Intended only for use by {@link
   * Screen}.
   * @param screen the screen
   * @param requestedBy the user that made the request
   * @param dateRequested the date created
   * @motivation for creating CherryPickRequests from legacy ScreenDB cherry pick visits
   */
  protected CherryPickRequest(AdministratorUser createdBy,
                              Screen screen,
                              ScreeningRoomUser requestedBy,
                              LocalDate dateRequested)
  {
    super(createdBy);
    _screen = screen;
    _requestedBy = requestedBy;
    _dateRequested = dateRequested;
    if (getDefaultTransferVolume() != null) {
      setTransferVolumePerWellRequested(getDefaultTransferVolume());
      setTransferVolumePerWellApproved(getDefaultTransferVolume());
    }
    if (getDefaultAssayPlateType() != null) {
      setAssayPlateType(getDefaultAssayPlateType());
    }
  }
  
  /**
   * Construct an uninitialized <code>CherryPickRequest</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected CherryPickRequest() {}


  /**
   * Set the unique identifier for the <code>ScreenResult</code>.
   * @param screenResultId a unique identifier for the <code>ScreenResult</code>
   */
  private void setCherryPickRequestId(Integer cherryPickRequestId)
  {
    setEntityId(cherryPickRequestId);
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

  void addLabCherryPick(LabCherryPick labCherryPick)
  {
    _labCherryPicks.add(labCherryPick);
    incUnfulfilledLabCherryPicks();
  }

  public boolean isKeepSourcePlateCherryPicksTogether()
  {
    return _keepSourcePlateCherryPicksTogether;
  }

  public void setKeepSourcePlateCherryPicksTogether(boolean keepSourcePlateCherryPicksTogether)
  {
    _keepSourcePlateCherryPicksTogether = keepSourcePlateCherryPicksTogether;
  }
}
