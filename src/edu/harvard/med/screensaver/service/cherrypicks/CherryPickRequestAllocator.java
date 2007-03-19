// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Set;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;

import org.apache.log4j.Logger;

/**
 * For a cherry pick request, selects source plate copies to draw from, and
 * records allocation of liquid needed to fulfill the request.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestAllocator
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestAllocator.class);


  // instance data members
  
  private DAO dao;


  // public constructors and methods

  public CherryPickRequestAllocator(DAO dao)
  {
    this.dao = dao;
  }

  /**
   * 
   * @param cherryPickRequest
   * @return the set of <i>unfulfillable</i> cherry picks
   */
  public Set<CherryPick> allocate(CherryPickRequest cherryPickRequest)
  {
    // TODO Auto-generated method stub
    return null;
  }


  public Set<CherryPick> getUnfillableCherryPicks()
  {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  // private methods

}

