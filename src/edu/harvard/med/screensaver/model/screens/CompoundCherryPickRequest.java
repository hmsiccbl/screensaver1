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

import edu.harvard.med.screensaver.model.libraries.PlateType;
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

  private static Logger log = Logger.getLogger(CompoundCherryPickRequest.class);


  // instance data members
  

  // public constructors and methods

  public CompoundCherryPickRequest(Screen screen,
                                   ScreeningRoomUser requestedBy,
                                   Date dateRequested)
  {
    super(screen, requestedBy, dateRequested);
  }

  public PlateType getAssayPlateType()
  {
    return COMPOUND_CHERRY_PICK_ASSAY_PLATE_TYPE;
  }


  // private methods
  
  /**
   * @motivation for hibernate
   */
  private CompoundCherryPickRequest()
  {
  }

}

