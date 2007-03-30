// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 * Tracks the event whereby a set of CherryPickAssayPlates have been plated for
 * a given CherryPickRequest. This signfies an actual transfer of liquid from
 * cherry pick copy plates to cherry pick assay plates. 
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="cherry_pick_liquid_transfer" lazy="false"
 * @hibernate.joined-subclass-key column="screening_room_activity_id"
 */
public class CherryPickLiquidTransfer extends ScreeningRoomActivity
{
  // static members

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(CherryPickLiquidTransfer.class);


  // instance data members

  private Set<CherryPickAssayPlate> _cherryPickAssayPlates = new HashSet<CherryPickAssayPlate>();


  // public constructors and methods

  public CherryPickLiquidTransfer(ScreeningRoomUser performedBy,
                                  Date dateCreated,
                                  Date dateOfActivity,
                                  CherryPickRequest cherryPickRequest) throws DuplicateEntityException
  {
    super(cherryPickRequest.getScreen(), performedBy, dateCreated, dateOfActivity);
  }

  @Override
  @ImmutableProperty
  public String getActivityTypeName()
  {
    // name provided by ces6
    return "Fulfill Cherry Pick";
  }

  @ToManyRelationship(inverseProperty="cherryPickLiquidTransfers")
  public Set<CherryPickAssayPlate> getCherryPickAssayPlates() 
  {
    return Collections.unmodifiableSet(_cherryPickAssayPlates);
  }
  
  public boolean addCherryPickAssayPlate(CherryPickAssayPlate assayPlate)
  {
    if (assayPlate.getCherryPickLiquidTransfer() != null && !assayPlate.getCherryPickLiquidTransfer().equals(this)) {
      throw new BusinessRuleViolationException("cherry pick assay plate can only be associated with one cherry pick liquid transfer");
    }
    boolean result = _cherryPickAssayPlates.add(assayPlate);
    assayPlate.setCherryPickLiquidTransfer(this);
    return result;
  }

  
  // private methods

  /**
   * @motivation for hibernate
   */
  private CherryPickLiquidTransfer() {}

  /**
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_liquid_transfer_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="cherryPickLiquidTransfers")
  Set<CherryPickAssayPlate> getHbnCherryPickAssayPlates()
  {
    return _cherryPickAssayPlates;
  }

  private void setHbnCherryPickAssayPlates(Set<CherryPickAssayPlate> cherryPickAssayPlates)
  {
    _cherryPickAssayPlates = cherryPickAssayPlates;
  }
}
