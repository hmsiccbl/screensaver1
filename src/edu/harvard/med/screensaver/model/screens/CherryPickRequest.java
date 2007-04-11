// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.math.BigDecimal;
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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.service.cherrypicks.LabCherryPickColumnMajorOrderingComparator;


/**
 * A Hibernate entity bean representing a cherry pick request.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public abstract class CherryPickRequest extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(CherryPickRequest.class);
  private static final long serialVersionUID = 0L;
  
  /**
   * When the {@link #_cherryPickRequestNumber} is not provided to the constructor, add this
   * offset number to {@link #_ordinal} to determine the cherry pick request number. This value
   * needs to be bigger than the largest ScreenDB visit_id for Cherry Pick Visits.
   */
  private static final int CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET = 10000;


  // instance fields

  private Integer _cherryPickRequestId;
  private Integer _version;
  private Screen _screen;
  private Integer _cherryPickRequestNumber; // ScreenDB visits.id
  private ScreeningRoomUser _requestedBy;
  private Date _dateRequested;
  private BigDecimal _microliterTransferVolumePerWellRequested;
  private BigDecimal _microliterTransferVolumePerWellApproved;
  private boolean _randomizedAssayPlateLayout;
  private Set<Integer> _requestedEmptyColumnsOnAssayPlate = new HashSet<Integer>();
  private String _comments;
  private Set<ScreenerCherryPick> _screenerCherryPicks = new HashSet<ScreenerCherryPick>();
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  private SortedSet<CherryPickAssayPlate> _cherryPickAssayPlates = new TreeSet<CherryPickAssayPlate>();

  private transient List<CherryPickAssayPlate> _activeAssayPlates;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPickRequest</code> object.
   * 
   * @param screen the screen
   * @param requestedBy the user that made the request
   * @param dateRequested the date created
   * @throws DuplicateEntityException
   */
  public CherryPickRequest(Screen screen,
                           ScreeningRoomUser requestedBy,
                           Date dateRequested)
  {
    int cherryPickRequestNumber =
      _screen.getAllTimeCherryPickRequestCount() + CHERRY_PICK_REQUEST_NUMBER_GENERATION_OFFSET;
    initialize(screen, requestedBy, dateRequested, cherryPickRequestNumber);
  }

  /**
   * Constructs an initialized <code>CherryPickRequest</code> object.
   * 
   * @param screen the screen
   * @param requestedBy the user that made the request
   * @param dateRequested the date created
   * @throws DuplicateEntityException
   */
  public CherryPickRequest(Screen screen,
                           ScreeningRoomUser requestedBy,
                           Date dateRequested,
                           Integer cherryPickRequestNumber)
  {
    initialize(screen, requestedBy, dateRequested, cherryPickRequestNumber);
  }

  private void initialize(
    Screen screen,
    ScreeningRoomUser requestedBy,
    Date dateRequested,
    Integer cherryPickRequestNumber)
  {
    _screen = screen;
    _requestedBy = requestedBy;
    _dateRequested = truncateDate(dateRequested);
    _screen.setAllTimeCherryPickRequestCount(_screen.getAllTimeCherryPickRequestCount() + 1);
    _cherryPickRequestNumber = cherryPickRequestNumber;
    requestedBy.getHbnCherryPickRequests().add(this);
    screen.getCherryPickRequests().add(this);
  }
  

  // public methods

  public Integer getEntityId()
  {
    return getCherryPickRequestId();
  }

  /**
   * @hibernate.property type="integer" not-null="true"
   */
  public Integer getCherryPickRequestNumber()
  {
    return _cherryPickRequestNumber;
  }
  
  /**
   * Get the id for the cherry pick request.
   *
   * @return the id for the cherry pick request
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="cherry_pick_request_id_seq"
   */
  public Integer getCherryPickRequestId()
  {
    return _cherryPickRequestId;
  }
  
  /**
   * Get the screener cherry picks.
   *
   * @return the screener cherry picks
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_request_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.ScreenerCherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickRequest")
  public Set<ScreenerCherryPick> getScreenerCherryPicks()
  {
    return _screenerCherryPicks;
  }

  /**
   * Get the set lab cherry picks.
   *
   * @return the lab cherry picks
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_request_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.LabCherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickRequest")
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
  }

  /**
   * Get the set of cherry pick liquid transfers for this cherry pick request.
   *
   * @return the cherry pick liquid transfers
   * */
  @DerivedEntityProperty
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
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_request_to_screen"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false, inverseProperty="cherryPickRequests")
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the user that requested the cherry pick.
   *
   * @return the user that requested the cherry pick
   */
  @ToOneRelationship(nullable=false, inverseProperty="cherryPickRequests")
  public ScreeningRoomUser getRequestedBy()
  {
    return _requestedBy;
  }

  /**
   * Set the user that requested the cherry pick.
   *
   * @param requestedBy the new user that requested the cherry pick
   */
  public void setRequestedBy(ScreeningRoomUser requestedBy)
  {
    if (requestedBy == null) {
      throw new IllegalArgumentException("requestedBy property cannot be null");
    }
    _requestedBy.getHbnCherryPickRequests().remove(this);
    _requestedBy = requestedBy;
    _requestedBy.getHbnCherryPickRequests().add(this);
  }

  /**
   * Get the date of the request
   *
   * @return the date of the request
   * @hibernate.property
   *   not-null="true"
   */
  public Date getDateRequested()
  {
    return _dateRequested;
  }

  /**
   * Set the date created.
   *
   * @param dateRequested the new date created
   */
  public void setDateRequested(Date dateRequested)
  {
    _dateRequested = truncateDate(dateRequested);
  }

  @DerivedEntityProperty
  abstract public PlateType getAssayPlateType();
  
  @DerivedEntityProperty
  abstract public int getCherryPickAllowance();
  
  @DerivedEntityProperty
  abstract public int getCherryPickAllowanceUsed();

  /**
   * Get the requested microliterTransferVolumePerWell.
   *
   * @return the microliterTransferVolumePerWell
   * @hibernate.property
   *   type="big_decimal"
   */
  public BigDecimal getMicroliterTransferVolumePerWellRequested()
  {
    return _microliterTransferVolumePerWellRequested;
  }

  /**
   * Set the requested microliterTransferVolumePerWell.
   *
   * @param microliterTransferVolumePerWell the new microliterTransferVolumePerWell
   */
  public void setMicroliterTransferVolumePerWellRequested(BigDecimal microliterTransferVolumePerWell)
  {
    _microliterTransferVolumePerWellRequested = microliterTransferVolumePerWell;
  }

  /**
   * Get the approved microliterTransferVolumePerWell.
   *
   * @return the microliterTransferVolumePerWell
   * @hibernate.property
   *   type="big_decimal"
   */
  public BigDecimal getMicroliterTransferVolumePerWellApproved()
  {
    return _microliterTransferVolumePerWellApproved;
  }

  /**
   * Set the approved microliterTransferVolumePerWell.
   *
   * @param microliterTransferVolumePerWell the new microliterTransferVolumePerWell
   */
  public void setMicroliterTransferVolumePerWellApproved(BigDecimal microliterTransferVolumePerWell)
  {
    _microliterTransferVolumePerWellApproved = microliterTransferVolumePerWell;
  }

  /**
   * Get the set of empty columns requested by the screener. The union of this
   * method's result with the result of
   * {@link #getRequiredEmptyColumnsOnAssayPlate()} determines the full set
   * columns that must left empty.
   * 
   * @see #getRequiredEmptyRowsOnAssayPlate()
   * @return
   * @hibernate.set table="cherry_pick_request_requested_empty_columns"
   * @hibernate.collection-key column="cherry_pick_request_id"
   * @hibernate.collection-element type="integer" not-null="true"
   */
  @DerivedEntityProperty
  public Set<Integer> getRequestedEmptyColumnsOnAssayPlate()
  {
    return _requestedEmptyColumnsOnAssayPlate;
  }

  public void setRequestedEmptyColumnsOnAssayPlate(Set<Integer> requestedEmptyColumnsOnAssayPlate)
  {
    _requestedEmptyColumnsOnAssayPlate = requestedEmptyColumnsOnAssayPlate;
  }

  @SuppressWarnings("unchecked")
  @ImmutableProperty
  /**
   * The union of this method's result with the result of
   * {@link #getEmptyColumnsOnAssayPlate()} determines the full set columns that
   * must left empty.
   * 
   * @see #getEmptyRowsOnAssayPlate()
   */
  public Set<Integer> getRequiredEmptyColumnsOnAssayPlate()
  {
    return Collections.EMPTY_SET;
  }

  @SuppressWarnings("unchecked")
  @ImmutableProperty
  public Set<Character> getRequiredEmptyRowsOnAssayPlate()
  {
    return Collections.EMPTY_SET;
  }

  /**
   * @hibernate.property type="boolean" not-null="true"
   */
  public boolean isRandomizedAssayPlateLayout()
  {
    return _randomizedAssayPlateLayout;
  }

  public void setRandomizedAssayPlateLayout(boolean randomizedAssayPlateLayout)
  {
    _randomizedAssayPlateLayout = randomizedAssayPlateLayout;
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get whether the lab cherry picks have been allocated. If at least one lab
   * cherry pick has been allocated the entire cherry pick request is considered
   * allocated.
   */
  @DerivedEntityProperty
  public boolean isAllocated()
  {
    // this is not efficient, but it's 2007 and we've got cycles to burn, right?
    for (LabCherryPick labCherryPick : _labCherryPicks) {
      if (labCherryPick.isAllocated()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get whether the lab cherry picks have been at least partially allocated. If
   * at least one lab cherry pick has been allocated and at least one has not
   * been allocated, the cherry pick request is considered partially allocated.
   * 
   * @motivation to warn the user that not all cherry picks could fulfilled (a
   *             cherry pick request that is only partially allocated does not
   *             impact subsequent workflow options)
   */
  @DerivedEntityProperty
  public boolean isOnlyPartiallyAllocated()
  {
    boolean atLeastOneIsAllocated = false;
    boolean atLeastOneIsNotAllocated = false;
    for (LabCherryPick labCherryPick : _labCherryPicks) {
      if (labCherryPick.isAllocated()) {
        atLeastOneIsAllocated = true;
      }
      else {
        atLeastOneIsNotAllocated = true;
      }
      if (atLeastOneIsAllocated && atLeastOneIsNotAllocated) {
        return true;
      }
    }
    return false;
  }

  @DerivedEntityProperty
  public boolean isMapped()
  {
    return _cherryPickAssayPlates.size() > 0;
  }

  @DerivedEntityProperty
  public boolean isPlated()
  {
    return getCherryPickLiquidTransfers().size() > 0;
  }

  /**
   * 
   * @return
   * @hibernate.set
   *   sort="natural"
   *   cascade="all-delete-orphan"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_request_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickRequest")
  public SortedSet<CherryPickAssayPlate> getCherryPickAssayPlates()
  {
    return /*Collections.unmodifiableSortedSet(*/_cherryPickAssayPlates/*)*/;
  }
  
  public boolean addCherryPickAssayPlate(CherryPickAssayPlate assayPlate)
  {
    _activeAssayPlates = null;
    return _cherryPickAssayPlates.add(assayPlate);
  }

  @DerivedEntityProperty
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
   * Determine if this CherryPickRequest has a set of lab cherry picks from the
   * same source plate than would fit on a single assay plate. This matters to
   * the lab, which must be notified to manually reload the source plate when
   * creating the cherry pick plates. We can detect this case when the last well
   * (containing a cherry pick) on an assay plate is from the same source plate
   * as the first well on the next assay plate.
   */
  @DerivedEntityProperty
  public boolean isSourcePlateReloadRequired()
  {
    return getAssayPlatesRequiringSourcePlateReload().size() > 0;
  }
  
  /**
   * @see #isSourcePlateReloadRequired()
   */
  @DerivedEntityProperty
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

  /**
   * A business key class for the CherryPickRequest.
   */
  private class BusinessKey
  {
    public Screen getScreen()
    {
      return _screen;
    }
    
    public Integer getCherryPickRequestNumber()
    {
      return _cherryPickRequestNumber;
    }
        
    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        this.getScreen().equals(that.getScreen()) &&
        this.getCherryPickRequestNumber().equals(that.getCherryPickRequestNumber());
    }

    @Override
    public int hashCode()
    {
      return
        this.getScreen().hashCode() +
        this.getCherryPickRequestNumber().hashCode();
    }

    @Override
    public String toString()
    {
      return this.getScreen() + ":#" + this.getCherryPickRequestNumber();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }

  
  // protected methods

  /**
   * Construct an uninitialized <code>CherryPickRequest</code> object.
   *
   * @motivation for hibernate
   */
  protected CherryPickRequest() {}


  // private methods

  /**
   * Set the unique identifier for the <code>ScreenResult</code>.
   * 
   * @param screenResultId a unique identifier for the <code>ScreenResult</code>
   */
  private void setCherryPickRequestId(Integer cherryPickRequestId)
  {
    _cherryPickRequestId = cherryPickRequestId;
  }

  /**
   * Get the version for the cherry pick request.
   *
   * @return the version for the cherry pick request
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the cherry pick request.
   *
   * @param version the new version for the cherry pick request
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the screen.
   *
   * @param the screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the screener cherry picks.
   *
   * @param cherryPicks the new screener cherry picks
   * @motivation for hibernate
   */
  private void setScreenerCherryPicks(Set<ScreenerCherryPick> screenerCherryPicks)
  {
    _screenerCherryPicks = screenerCherryPicks;
  }
  
  /**
   * Set the lab cherry picks.
   *
   * @param cherryPicks the new lab cherry picks
   * @motivation for hibernate
   */
  private void setLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }
  
  /**
   * Set the cherry pick assay plates.
   *
   * @param cherryPicks the new cherry pick assay plates
   * @motivation for hibernate
   */
  private void setCherryPickAssayPlates(SortedSet<CherryPickAssayPlate> cherryPickAssayPlates)
  {
    _cherryPickAssayPlates = cherryPickAssayPlates;
  }
  
  /**
   * Get the user that requested the cherry pick.
   *
   * @return the user that requested the cherry pick
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="requested_by_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_request_to_requested_by"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false)
  private ScreeningRoomUser getHbnRequestedBy()
  {
    return _requestedBy;
  }

  /**
   * Set the user that requested the cherry pick.
   *
   * @param requestedBy the user that requested the cherry pick
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setHbnRequestedBy(ScreeningRoomUser requestedBy)
  {
    _requestedBy = requestedBy;
  }
  
  private void setCherryPickRequestNumber(Integer cherryPickRequestNumber)
  {
    _cherryPickRequestNumber = cherryPickRequestNumber;
  }
}
