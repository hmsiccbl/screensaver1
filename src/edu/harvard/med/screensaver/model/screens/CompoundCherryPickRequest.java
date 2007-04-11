// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import java.util.Set;

import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="compound_cherry_pick_request" lazy="false"
 * @hibernate.joined-subclass-key column="cherry_pick_request_id"
 */
public class CompoundCherryPickRequest extends CherryPickRequest
{
  // static members

  private static final long serialVersionUID = 1L;

  private static final PlateType COMPOUND_CHERRY_PICK_ASSAY_PLATE_TYPE = PlateType.ABGENE;

  private static final BigDecimal SOURCE_WELL_COUNT_PCT_LIMIT = new BigDecimal("0.003");

  private static Logger log = Logger.getLogger(CompoundCherryPickRequest.class);


  // instance data members
  

  // public constructors and methods

  public CompoundCherryPickRequest(Screen screen,
                                   ScreeningRoomUser requestedBy,
                                   Date dateRequested)
  {
    super(screen, requestedBy, dateRequested);
  }

  public CompoundCherryPickRequest(Screen screen,
                                   ScreeningRoomUser requestedBy,
                                   Date dateRequested,
                                   Integer cherryPickRequestNumber)
  {
    super(screen, requestedBy, dateRequested, cherryPickRequestNumber);
  }

  @DerivedEntityProperty
  public PlateType getAssayPlateType()
  {
    return COMPOUND_CHERRY_PICK_ASSAY_PLATE_TYPE;
  }

  /**
   * For compound screens, the cherry pick limit is 0.3% of the number of
   * distinct compounds selected (and not wells, which may have overlapping
   * compounds).
   */
  @DerivedEntityProperty
  public int getCherryPickAllowance()
  {
    Set<Compound> distinctCompounds = new HashSet<Compound>(getScreenerCherryPicks().size());
    for (Well well : getScreen().getScreenResult().getWells()) {
      distinctCompounds.add(well.getPrimaryCompound());
    }
    return SOURCE_WELL_COUNT_PCT_LIMIT.multiply(new BigDecimal(distinctCompounds.size())).intValue();
  }
  
  public int getCherryPickAllowanceUsed()
  {
    return getScreenerCherryPicks().size();
  }
  

  // private methods
  
  /**
   * @motivation for hibernate
   */
  private CompoundCherryPickRequest()
  {
  }
}

