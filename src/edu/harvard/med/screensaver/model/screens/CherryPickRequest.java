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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;


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


  // instance fields

  private Integer _cherryPickRequestId;
  private Integer _version;
  private Screen _screen;
  private Integer _ordinal; // only needed for business key 
  private ScreeningRoomUser _requestedBy;
  private Date _dateRequested;
  private BigDecimal _microliterTransferVolumePerWellRequested;
  private BigDecimal _microliterTransferVolumePerWellApproved;
  private boolean _randomizedAssayPlateLayout;
  private Set<Integer> _emptyColumnsOnAssayPlate;
  private String _comments;
  private Set<CherryPick> _cherryPicks = new HashSet<CherryPick>();
  private Set<CherryPickLiquidTransfer> _cherryPickLiquidTransfers = new HashSet<CherryPickLiquidTransfer>();


  // public constructor

  /**
   * Constructs an initialized <code>CherryPickRequest</code> object.
   * 
   * @param screen the screen
   * @param requestedBy the user that made the requet
   * @param dateRequested the date created
   * @throws DuplicateEntityException
   */
  public CherryPickRequest(Screen screen,
                           ScreeningRoomUser requestedBy,
                           Date dateRequested)
  {
    _screen = screen;
    _requestedBy = requestedBy;
    _dateRequested = dateRequested;
    _ordinal = screen.getCherryPickRequests().size();
    requestedBy.getHbnCherryPickRequests().add(this);
    screen.getCherryPickRequests().add(this);
    // TODO: guard against race condition; minimally, add database uniqueness constraint for business key fields (screen_id, ordinal)
  }


  // public methods

  public Integer getEntityId()
  {
    return getCherryPickRequestId();
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
   * Get the cherry picks.
   *
   * @return the cherry picks
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_request_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickRequest")
  public Set<CherryPick> getCherryPicks()
  {
    return _cherryPicks;
  }

  /**
   * Get the set of cherry pick liquid transfers for this cherry pick request.
   *
   * @return the cherry pick liquid transfers
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_request_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickRequest")
  public Set<CherryPickLiquidTransfer> getCherryPickLiquidTransfers()
  {
    return _cherryPickLiquidTransfers;
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
   * @return
   * @hibernate.set table="cherry_pick_request_empty_columns"
   * @hibernate.collection-key column="cherry_pick_request_id"
   * @hibernate.collection-element type="integer" not-null="true"
   */
  public Set<Integer> getEmptyColumnsOnAssayPlate()
  {
    return _emptyColumnsOnAssayPlate;
  }

  public void setEmptyColumnsOnAssayPlate(Set<Integer> emptyColumnsOnAssayPlate)
  {
    _emptyColumnsOnAssayPlate = emptyColumnsOnAssayPlate;
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

  @DerivedEntityProperty
  public Set<CherryPick> getCherryPicksForAssayPlate(String assayPlateName)
  {
    Set<CherryPick> cherryPicksForPlate = new HashSet<CherryPick>();

    for (CherryPick cherryPick : _cherryPicks) {
      if (cherryPick.isAllocated() && cherryPick.getAssayPlateName().equals(assayPlateName)) {
        cherryPicksForPlate.add(cherryPick);
      }
    }
    return cherryPicksForPlate;
  }
  
  @DerivedEntityProperty
  public Set<CherryPickLiquidTransfer> getCherryPickLiquidTransfersForAssayPlate(String assayPlateName)
  {
    Iterator<CherryPick> iter = getCherryPicksForAssayPlate(assayPlateName).iterator();
    if (iter.hasNext()) {
      return iter.next().getCherryPickLiquidTransfers();
    }
    return new TreeSet<CherryPickLiquidTransfer>();
  }

  @DerivedEntityProperty
  public boolean isAllocated()
  {
    Iterator<CherryPick> cherryPickIter = getCherryPicks().iterator();
    // we assume that if one cherry pick is allocated, they are all allocated
    return cherryPickIter.hasNext() && cherryPickIter.next().isAllocated();
  }

  @DerivedEntityProperty
  public Set<String> getAssayPlates()
  {
    if (!isAllocated()) {
      return new TreeSet<String>();
    }
    Set<String> assayPlates = new TreeSet<String>();
    for (CherryPick cherryPick : getCherryPicks()) {
      assayPlates.add(cherryPick.getAssayPlateName());
    }
    return assayPlates;
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
    
    public Integer getOrdinal()
    {
      return _ordinal;
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
        this.getOrdinal().equals(that.getOrdinal());
    }

    @Override
    public int hashCode()
    {
      return
        this.getScreen().hashCode() +
        this.getOrdinal().hashCode();
    }

    @Override
    public String toString()
    {
      return this.getScreen() + ":#" + this.getOrdinal();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }

  
  // protected constructor

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
  public void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the cherry picks.
   *
   * @param cherryPicks the new cherry picks
   * @motivation for hibernate
   */
  private void setCherryPicks(Set<CherryPick> cherryPicks)
  {
    _cherryPicks = cherryPicks;
  }
  
  /**
   * Set the set of cherry pick liquid transfers.
   * @param cherryPickLiquidTransfer the new cherry pick liquid transfer
   */
  private void setCherryPickLiquidTransfers(Set<CherryPickLiquidTransfer> cherryPickLiquidTransfers)
  {
    _cherryPickLiquidTransfers = cherryPickLiquidTransfers;
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

  /**
   * @hibernate.property type="integer" not-null="true"
   */
  private Integer getOrdinal()
  {
    return _ordinal;
  }

  private void setOrdinal(Integer ordinal)
  {
    _ordinal = ordinal;
  }
}
