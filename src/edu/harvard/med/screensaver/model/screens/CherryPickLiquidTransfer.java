// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * Tracks the event whereby a set (or subset) of cherry picks have been plated
 * for a given CherryPickRequest. This corresponds to an actual transfer of
 * liquid from cherry pick copy plates to cherry pick assay plates. Note that
 * plated cherry picks occur on a per-destination-plate basis and so must be
 * specified via {@link #addPlatedCherryPicksForPlate(String)} or
 * {@link #addPlatedCherryPicksForPlates(Set)}.  Note that this allows
 * for the case where only a subset of cherry pick requests have been plated.  
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="cherry_pick_liquid_transfer"
 *                            lazy="false"
 * @hibernate.joined-subclass-key column="screening_room_activity_id"
 */
public class CherryPickLiquidTransfer extends ScreeningRoomActivity
{
  // static members

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(CherryPickLiquidTransfer.class);


  // instance data members

  private CherryPickRequest _cherryPickRequest;
  private Set<CherryPick> _platedCherryPicks = new HashSet<CherryPick>();


  // public constructors and methods

  public CherryPickLiquidTransfer(ScreeningRoomUser performedBy,
                                  Date dateCreated,
                                  Date dateOfActivity,
                                  CherryPickRequest cherryPickRequest) throws DuplicateEntityException
  {
    
    super(cherryPickRequest.getScreen(), performedBy, dateCreated, dateOfActivity);
    _cherryPickRequest = cherryPickRequest;
    _cherryPickRequest.setCherryPickLiquidTransfer(this);
  }

  @Override
  @ImmutableProperty
  public String getActivityTypeName()
  {
    // name provided by ces6
    return "Fulfill Cherry Pick";
  }

  /**
   * Get the set of cherry pick request.
   *
   * @return the cherry pick request
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickRequest"
   *   column="cherry_pick_request_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_liquid_transfer_to_cherry_pick_request"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false, inverseProperty="cherryPickLiquidTransfer")
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * Enforces constraint that plated cherry picks can only be specified as a
   * group, on a per-destiation-plate basis (note the lack of a
   * addPlatedCherryPick() method).
   * 
   * @param plateName
   */
  public void addPlatedCherryPicksForPlate(String cherryPickDestinationPlateName)
  {
    for (CherryPick cherryPick : _cherryPickRequest.getCherryPicksForDestinationPlate(cherryPickDestinationPlateName)) {
      cherryPick.addCherryPickLiquidTransfer(this);
    }
  }

  /**
   * Enforces constraint that plated cherry picks can only be specified as a
   * group, on a per-destiation-plate basis (note the lack of a
   * addPlatedCherryPicks() method).
   * 
   * @param plateName
   */
  public void addPlatedCherryPicksForPlates(Set<String> cherryPickDestinationPlateNames) 
  {
    for (String cherryPickDestinationPlateName : cherryPickDestinationPlateNames) {
      addPlatedCherryPicksForPlate(cherryPickDestinationPlateName);
    }
  }

  
  // private methods

  /**
   * @motivation for hibernate
   */
  private CherryPickLiquidTransfer() {}

  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }
  
  /**
   * @hibernate.set
   *   table="cherry_pick_liquid_transfer_cherry_pick_link"
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_liquid_transfer_id"
   * @hibernate.collection-many-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPick"
   *   column="cherry_pick_id"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickLiquidTransfers")
  private Set<CherryPick> getHbnPlatedCherryPicks()
  {
    return _platedCherryPicks;
  }

  private void setHbnPlatedCherryPicks(Set<CherryPick> platedCherryPicks)
  {
    _platedCherryPicks = platedCherryPicks;
  }
}
