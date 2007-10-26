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
import java.util.Collection;
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
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.LabCherryPickColumnMajorOrderingComparator;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;


/**
 * A Hibernate entity bean representing a cherry pick request ("CPR"). A CPR
 * provides an abstraction for managing the workflow of performing a cherry pick
 * {@link Screening screening} for the set of cherry picks ("CP") requested by a
 * screener. Two types of CPs are managed: {@link ScreenerCherryPick}s and
 * {@link LabCherryPick}s. The ScreenerCPs represent the wells from the
 * original screen that are to be screened again (for validation purposes). The
 * LabCPs represent the wells from which "liquid" is physically drawn from and
 * that is transferred to one or more cherry pick assay plates ("assay plates").
 * Note that the source wells of LabCPs are usually different than the
 * ScreenerCP wells, as the lab maintains separate sets of plates ("library
 * copies") for use in the production of {@link CherryPickAssayPlate}s. It is
 * also possible that each ScreenerCP well will be "mapped" to <i>multiple</i>
 * LabCP wells, as is the case with RNAi SMARTPool libraries (from Dharmacon).
 * Finally, LabCPs for a given assay plate may be replicated in cases where the
 * creation of the assay plate failed (in the lab). The lab will need to
 * re-attempt creation of the plate and thus all LabCPs for that plate will be
 * duplicated (LabCPs are instrumental in determining how much liquid volume
 * remains in the wells of a library copy, and therefore the reliable tracking
 * of LabCPs is critical feature of Screensaver.)
 * <p>
 * LabCPs and the associated assay plates each progress through a range of
 * states, as a CPR is processed by the lab. LabCPs can have the following
 * states:
 * <p>
 * <table border="1">
 * <tr>
 * <td>State</td>
 * <td>Description</td>
 * <td>State Type</td>
 * <td>Valid Transition(s)</td>
 * <td>Affected Properties/Relationships</td>
 * </tr>
 * <tr>
 * <td>Unfulfilled</td>
 * <td>Liquid has not yet been allocated for the LabCP</td>
 * <td>Initial</td>
 * <td>Allocated</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>Allocated</td>
 * <td>Liquid has been allocated for the LabCP</td>
 * <td>Intermediate</td>
 * <td>Mapped+Allocated</td>
 * <td>sourceWell</td>
 * </tr>
 * <tr>
 * <td>Mapped+Unallocated</td>
 * <td>The LabCP has been assigned (mapped) to a particular well on a
 * particular assay plate, but has not been allocated. This occurs if a lab
 * cherry pick was created for a subsequent creation attempt of an assay plate,
 * but for which there was insufficient volume in any library copy.</td>
 * <td>Initial</td>
 * <td>Map+Allocated, Canceled</td>
 * <td>assayPlate, assayPlateRow, assayPlateColumn</td>
 * </tr>
 * <tr>
 * <td>Mapped+Allocated</td>
 * <td>The allocated LabCP has been assigned (mapped) to a particular well on a
 * particular assay plate.
 * <td>Intermediate</td>
 * <td>Failed, Canceled, Plated</td>
 * <td>assayPlate, assayPlateRow, assayPlateColumn</td>
 * </tr>
 * <tr>
 * <td>Failed</td>
 * <td>The LabCP was allocated and mapped, but the plate it was assigned to was
 * later marked as failed (workflow rules dictate that LabCPs can only be
 * canceled on a per-plate basis)</td>
 * <td>Terminal</td>
 * <td></td>
 * <td>assayPlate.cherryPickLiquidTransfer.isSuccessful</td>
 * </tr>
 * <tr>
 * <td>Canceled</td>
 * <td>The LabCP was previously allocated and mapped, but the plate it was
 * assigned to was later canceled (workflow rules dictate that LabCPs can only
 * be canceled on a per-plate basis)</td>
 * <td>Terminal</td>
 * <td></td>
 * <td>sourceWell, assayPlate.isCanceled</td>
 * </tr>
 * <tr>
 * <td>Plated</td>
 * <td>The LabCP has been allocated and mapped, and the assay plate it belongs
 * to was marked as "plated"</td>
 * <td>Terminal</td>
 * <td></td>
 * <td>assayPlate.cherryPickLiquidTransfer.isSuccessful</td>
 * </tr>
 * </table>
 * <p>
 * A CP Assay Plate also progresses through a range of states, as a CPR is
 * processed by the lab. CP Assay Plates can have the following states:
 * <p>
 * <table border="1">
 * <tr>
 * <td>State</td>
 * <td>Description</td>
 * <td>State Type</td>
 * <td>Valid Transition(s)</td>
 * <td>Affected Properties/Relationships</td>
 * </tr>
 * <tr>
 * <td>Not Plated</td>
 * <td>The assay plate has not yet been physically created in the lab.</td>
 * <td>Initial</td>
 * <td>Plated, Failed, Canceled</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>Plated</td>
 * <td>The assay plate has been physically created in the lab.</td>
 * <td>Final</td>
 * <td></td>
 * <td>cherryPickAssayPlate</td>
 * </tr>
 * <tr>
 * <td>Failed</td>
 * <td>The physical creation of the assay plate in the lab was attempted, but
 * failed. Liquid for each of its LabCPs was consumed in the failed attempt.</td>
 * <td>Final</td>
 * <td></td>
 * <td>cherryPickAssayPlate</td>
 * </tr>
 * <tr>
 * <td>Canceled</td>
 * <td>The assay plate has not yet been physically created in the lab.</td>
 * <td>Final</td>
 * <td></td>
 * <td>isCanceled</td>
 * </tr>
 * </table>
 * <p>
 * Note that business rules dictate that an assay plate is always created with
 * the set of lab cherry picks that are assigned to it, so there is no need for,
 * say, a "New" status.
 * <p>
 *
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


  // private instance data

  private Integer _cherryPickRequestId;
  private Integer _version;
  private Screen _screen;
  private Integer _legacyCherryPickRequestNumber; // ScreenDB visits.id
  private ScreeningRoomUser _requestedBy;
  private Date _dateRequested;
  private BigDecimal _microliterTransferVolumePerWellRequested;
  private BigDecimal _microliterTransferVolumePerWellApproved;
  private AdministratorUser _volumeApprovedBy;
  private Date _dateVolumeApproved;
  private boolean _randomizedAssayPlateLayout;
  private Set<Integer> _requestedEmptyColumnsOnAssayPlate = new HashSet<Integer>();
  private Set<Character> _requestedEmptyRowsOnAssayPlate = new HashSet<Character>();
  private String _comments;
  private Set<ScreenerCherryPick> _screenerCherryPicks = new HashSet<ScreenerCherryPick>();
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  private SortedSet<CherryPickAssayPlate> _cherryPickAssayPlates = new TreeSet<CherryPickAssayPlate>();
  private transient List<CherryPickAssayPlate> _activeAssayPlates;


  // public instance methods

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getCherryPickRequestId();
  }

  /**
   * Get the assay plate type.
   * @return the assay plate type
   */
  @Transient
  abstract public PlateType getAssayPlateType();

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
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
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
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
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
   * Get the set of active cherry pick assay plates.
   * @return the set of active cherry pick assay plates
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
   * Get the set of empty columns requested by the screener. The union of this
   * method's result with the result of {@link
   * #getRequiredEmptyColumnsOnAssayPlate()} determines the full set columns that must left empty.
   * @return 1-based column numbers that screener has requested be left empty on cherry pick assay plate
   * @see #getRequiredEmptyRowsOnAssayPlate()
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="requestedEmptyColumn", nullable=false)
  @JoinTable(
    name="cherryPickRequestRequestedEmptyColumn",
    joinColumns=@JoinColumn(name="cherryPickRequestId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_requested_empty_column_to_cherry_pick_request")
  @OrderBy("requestedEmptyColumn")
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="requestedEmptyColumnOnAssayPlate")
  public Set<Integer> getRequestedEmptyColumnsOnAssayPlate()
  {
    return _requestedEmptyColumnsOnAssayPlate;
  }

  /**
   * Add a set of requested empty columns to the set of empty columns requested by the screener.
   * @param requestedEmptyColumnsOnAssayPlate 1-based column numbers that screener has requested be
   * left empty on cherry pick assay plate
   */
  public void addRequestedEmptyColumnsOnAssayPlate(Collection<Integer> requestedEmptyColumnsOnAssayPlate)
  {
    _requestedEmptyColumnsOnAssayPlate.addAll(requestedEmptyColumnsOnAssayPlate);
  }

  /**
   * Add a requested empty column on the assay plate.
   * @param requestedEmptyColumn the requested empty column to add
   * @return true iff the requested empty column to add was not already in the set of requested
   * empty columns
   */
  public boolean addRequestedEmptyColumnOnAssayPlate(Integer requestedEmptyColumn)
  {
    return _requestedEmptyColumnsOnAssayPlate.add(requestedEmptyColumn);
  }

  /**
   * Clear the set of empty columns requested by the screener.
   */
  public void clearRequestedEmptyColumnsOnAssayPlate()
  {
    _requestedEmptyColumnsOnAssayPlate.clear();
  }

  /**
   * The union of this method's result with the result of
   * {@link #getEmptyColumnsOnAssayPlate()} determines the full set columns that
   * must left empty.
   * @return 1-based column numbers that are required to be left empty on cherry pick assay plate
   * @see #getEmptyRowsOnAssayPlate()
   */
  @Transient
  public Set<Integer> getRequiredEmptyColumnsOnAssayPlate()
  {
    return Collections.emptySet();
  }

  /**
   * Get the set of empty rows requested by the screener. The union of this
   * method's result with the result of {@link
   * #getRequiredEmptyRowsOnAssayPlate()} determines the full set rows that must left empty.
   * @return 1-based row numbers that screener has requested be left empty on cherry pick assay plate
   * @see #getRequiredEmptyRowsOnAssayPlate()
   */
  @org.hibernate.annotations.CollectionOfElements
  @Column(name="requestedEmptyRow", nullable=false)
  @JoinTable(
    name="cherryPickRequestRequestedEmptyRow",
    joinColumns=@JoinColumn(name="cherryPickRequestId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_request_requested_empty_row_to_cherry_pick_request")
  @OrderBy("requestedEmptyRow")
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="requestedEmptyRowOnAssayPlate")
  public Set<Character> getRequestedEmptyRowsOnAssayPlate()
  {
    return _requestedEmptyRowsOnAssayPlate;
  }

  /**
   * Add a set of requested empty rows to the set of empty rows requested by the screener.
   * @param requestedEmptyRowsOnAssayPlate 1-based row numbers that screener has requested be
   * left empty on cherry pick assay plate
   */
  public void addRequestedEmptyRowsOnAssayPlate(Collection<Character> requestedEmptyRowsOnAssayPlate)
  {
    _requestedEmptyRowsOnAssayPlate.addAll(requestedEmptyRowsOnAssayPlate);
  }

  /**
   * Add a requested empty row on the assay plate.
   * @param requestedEmptyRow the requested empty row to add
   * @return true iff the requested empty row to add was not already in the set of requested
   * empty rows
   */
  public boolean addRequestedEmptyRowOnAssayPlate(Character requestedEmptyRow)
  {
    return _requestedEmptyRowsOnAssayPlate.add(requestedEmptyRow);
  }

  /**
   * Clear the set of empty rows requested by the screener.
   */
  public void clearRequestedEmptyRowsOnAssayPlate()
  {
    _requestedEmptyRowsOnAssayPlate.clear();
  }

  /**
   * @return rows that are required to be left empty on cherry pick assay plate
   * @see #getEmptyRowsOnAssayPlate()
   */
  @Transient
  public Set<Character> getRequiredEmptyRowsOnAssayPlate()
  {
    return Collections.emptySet();
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
   * Get whether the lab cherry picks have <i>ever</i> been allocated,
   * including the case where they were allocated, mapped, and then deallocated
   * (i.e., canceled). If at least one lab cherry pick has been allocated the
   * entire cherry pick request is considered allocated.
   * <p>
   * TODO: it might be better to rename this method to 'wasAllocated', or something similar to
   * indicate that it also covers canceled lab cherry picks.
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
    return getCherryPickLiquidTransfers().size() > 0;
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
   * Get the legacy cherry pick request number.
   * @return the legacy cherry pick request number
   */
  private Integer getLegacyCherryPickRequestNumber()
  {
    return _legacyCherryPickRequestNumber;
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
   * Set the set of empty columns requested by the screener.
   * @param requestedEmptyColumnsOnAssayPlate 1-based column numbers that screener has requested be
   * left empty on cherry pick assay plate
   * @motivation for hibernate
   */
  private void setRequestedEmptyColumnsOnAssayPlate(Set<Integer> requestedEmptyColumnsOnAssayPlate)
  {
    _requestedEmptyColumnsOnAssayPlate = requestedEmptyColumnsOnAssayPlate;
  }

  /**
   * Set the set of empty rows requested by the screener.
   * @param requestedEmptyRowsOnAssayPlate 1-based row numbers that screener has requested be
   * left empty on cherry pick assay plate
   * @motivation for hibernate
   */
  private void setRequestedEmptyRowsOnAssayPlate(Set<Character> requestedEmptyRowsOnAssayPlate)
  {
    _requestedEmptyRowsOnAssayPlate = requestedEmptyRowsOnAssayPlate;
  }
}
