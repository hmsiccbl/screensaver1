// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

/**
 * Tracks the event whereby a set of CherryPickAssayPlates have been plated for
 * a given CherryPickRequest. This signfies an actual transfer of liquid from
 * cherry pick copy plates to cherry pick assay plates.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_liquid_transfer_to_activity")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class CherryPickLiquidTransfer extends LabActivity
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(CherryPickLiquidTransfer.class);

  public static final String ACTIVITY_TYPE_NAME =  "Cherry Pick Plate Activity";

  
  // private instance data

  private CherryPickLiquidTransferStatus _status;
  private Set<CherryPickAssayPlate> _cherryPickAssayPlates = new HashSet<CherryPickAssayPlate>();


  // public constructor

  /**
   * Construct an initialized <code>CherryPickLiquidTransfer</code>. Intended only for use
   * with {@link Screen#createCherryPickLiquidTransfer(ScreensaverUser, Date, Date,
   * CherryPickLiquidTransferStatus)}.
   * @param screen the screen
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the screening room activity took place
   * @param status the status of the cherry pick liquid transfer
   */
  public CherryPickLiquidTransfer(
    Screen screen,
    ScreensaverUser performedBy,
    LocalDate dateOfActivity,
    CherryPickLiquidTransferStatus status)
  {
    // TODO: business logic to test that cherryPickRequest.getScreen().equals(screen)
    super(screen, performedBy, dateOfActivity);
    if (status == null) {
      throw new NullPointerException("status is required");
    }
    _status = status;
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getActivityTypeName()
  {
    return ACTIVITY_TYPE_NAME;
  }

  /**
   * Get the status of the cherry pick liquid transfer.
   * @return the status of the cherry pick liquid transfer
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus$UserType")
  public CherryPickLiquidTransferStatus getStatus()
  {
    return _status;
  }

  /**
   * Return true iff the cherry pick liquid transfer is {@link
   * CherryPickLiquidTransferStatus#SUCCESSFUL successful}.
   * @return true iff the cherry pick liquid transfer is successful
   */
  @Transient
  public boolean isSuccessful()
  {
    return _status.equals(CherryPickLiquidTransferStatus.SUCCESSFUL);
  }

  /**
   * Return true iff the cherry pick liquid transfer is {@link
   * CherryPickLiquidTransferStatus#FAILED failed}.
   * @return true iff the cherry pick liquid transfer is failed
   */
  @Transient
  public boolean isFailed()
  {
    return _status.equals(CherryPickLiquidTransferStatus.FAILED);
  }

  /**
   * Return true iff the cherry pick liquid transfer is {@link
   * CherryPickLiquidTransferStatus#CANCELED cancelled}.
   * @return true iff the cherry pick liquid transfer is cancelled
   */
  @Transient
  public boolean isCancelled()
  {
    return _status.equals(CherryPickLiquidTransferStatus.CANCELED);
  }

  /**
   * Get the set of cherry pick assay plates.
   * @return the set of cherry pick assay plates
   */
  @OneToMany(mappedBy="cherryPickLiquidTransfer", fetch=FetchType.LAZY)
  public Set<CherryPickAssayPlate> getCherryPickAssayPlates()
  {
    return _cherryPickAssayPlates;
  }

  /**
   * Add a cherry pick assay plate to the cherry pick liquid transfer.
   * @param assayPlate the cherry pick assay plate to add
   * @return true iff the cherry pick assay plate did not already belong to the cherry pick
   * liquid transfer
   */
  public boolean addCherryPickAssayPlate(CherryPickAssayPlate assayPlate)
  {
    if (assayPlate.getCherryPickLiquidTransfer() != null && !assayPlate.getCherryPickLiquidTransfer().equals(this)) {
      throw new BusinessRuleViolationException("cherry pick assay plate can only be associated with one cherry pick liquid transfer");
    }
    boolean result = _cherryPickAssayPlates.add(assayPlate);
    assayPlate.setCherryPickLiquidTransfer(this);
    return result;
  }


  // protected constructor

  /**
   * Constructor an uninitialized <code>CherryPickLiquidTransfer</code>
   * @motivation for hibernate and proxy/concrete subclasses
   */
  protected CherryPickLiquidTransfer() {}


  // private instance methods

  /**
   * Set the status of the cherry pick liquid transfer
   * @param status the new status of the cherry pick liquid transfer
   * @motivation for hibernate
   */
  private void setStatus(CherryPickLiquidTransferStatus status)
  {
    _status = status;
  }

  /**
   * Set the cherry pick assay plates.
   * @param cherryPickAssayPlates the new cherry pick assay plates
   * @motivation for hibernate
   */
  private void setCherryPickAssayPlates(Set<CherryPickAssayPlate> cherryPickAssayPlates)
  {
    _cherryPickAssayPlates = cherryPickAssayPlates;
  }
}
