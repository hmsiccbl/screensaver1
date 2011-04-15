// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Screen;

public class CherryPickRequestDAO extends AbstractDAO
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestDAO.class);


  // instance data members

  private GenericEntityDAO _dao;


  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public CherryPickRequestDAO()
  {
  }

  public CherryPickRequestDAO(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public void deleteScreenerCherryPick(ScreenerCherryPick screenerCherryPick)
  {
    if (screenerCherryPick.getCherryPickRequest().isAllocated()) {
      throw new BusinessRuleViolationException("cannot delete a screener cherry pick for a cherry pick request that has been allocated");
    }

    // disassociate from related entities
    screenerCherryPick.getCherryPickRequest().getScreenerCherryPicks().remove(screenerCherryPick);
    for (LabCherryPick cherryPick : new ArrayList<LabCherryPick>(screenerCherryPick.getLabCherryPicks())) {
      deleteLabCherryPick(cherryPick);
    }

    getEntityManager().remove(screenerCherryPick);
  }

  public void deleteLabCherryPick(LabCherryPick labCherryPick)
  {
    if (labCherryPick.getCherryPickRequest().isAllocated()) {
      throw new BusinessRuleViolationException("cannot delete a lab cherry pick for a cherry pick request that has been allocated");
    }

    // dissociate from related entities
    labCherryPick.getCherryPickRequest().getLabCherryPicks().remove(labCherryPick);
    if (labCherryPick.getAssayPlate() != null) {
      labCherryPick.getAssayPlate().getLabCherryPicks().remove(labCherryPick);
    }
    
    labCherryPick.getCherryPickRequest().decUnfulfilledLabCherryPicks();

    getEntityManager().remove(labCherryPick);
  }
  
  public void deleteAllCherryPicks(CherryPickRequest cherryPickRequest)
  {
   cherryPickRequest = _dao.reattachEntity(cherryPickRequest);
    _dao.need(cherryPickRequest, CherryPickRequest.labCherryPicks.to(LabCherryPick.sourceWell));
    if (cherryPickRequest.isAllocated()) {
      throw new BusinessRuleViolationException("cherry picks cannot be deleted once a cherry pick request has been allocated");
    }
    _dao.need(cherryPickRequest, CherryPickRequest.screenerCherryPicks.to(ScreenerCherryPick.screenedWell));
    Set<ScreenerCherryPick> cherryPicksToDelete = new HashSet<ScreenerCherryPick>(cherryPickRequest.getScreenerCherryPicks());
    for (ScreenerCherryPick cherryPick : cherryPicksToDelete) {
      deleteScreenerCherryPick(cherryPick);
    }
  }

  public void deleteCherryPickRequest(final CherryPickRequest cherryPickRequestIn)
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
    // note: the cherryPickRequest.screen child-to-parent relationship is not cascaded, so although
    // screen entity *will* be available (it never loaded as a proxy), the screen's
    // relationships will not be reattached and will thus be inaccessible; in
    // particular, screen.cherryPickRequests (needed below)
    _dao.reattachEntity(cherryPickRequestIn.getScreen());

    if (cherryPickRequestIn.isAllocated()) {
      throw new BusinessRuleViolationException("cannot delete a cherry pick request that has been allocated");
    }
    if (cherryPickRequestIn instanceof RNAiCherryPickRequest &&
      ((RNAiCherryPickRequest) cherryPickRequestIn).isScreened()) {
      throw new BusinessRuleViolationException("cannot delete a cherry pick request that has been screened");
    }

    // dissociate from related entities
    cherryPickRequest.getScreen().getCherryPickRequests().remove(cherryPickRequest);
    getEntityManager().remove(cherryPickRequest);
  }

  public CherryPickRequest findCherryPickRequestByNumber(int cherryPickRequestNumber)
  {
    CherryPickRequest cherryPickRequest = _dao.findEntityByProperty(CherryPickRequest.class,
                                                                    "legacyCherryPickRequestNumber",
                                                                    cherryPickRequestNumber);
    if (cherryPickRequest == null) {
      int cherryPickRequestId = cherryPickRequestNumber;
      cherryPickRequest = _dao.findEntityById(CherryPickRequest.class, cherryPickRequestId);
    }
    return cherryPickRequest;
  }

  public Set<LabCherryPick> findLabCherryPicksForWell(Well well)
  {
    return new HashSet<LabCherryPick>(_dao.findEntitiesByProperty(LabCherryPick.class,
                                                                  "sourceWell",
                                                                  well));
  }

  public Set<ScreenerCherryPick> findScreenerCherryPicksForWell(Well well)
  {
    return new HashSet<ScreenerCherryPick>(_dao.findEntitiesByProperty(ScreenerCherryPick.class,
                                                                       "screenedWell",
                                                                       well));
  }

  public Map<WellKey,Number> findDuplicateCherryPicksForScreen(final Screen screen)
  {
    Query query = getHibernateSession().createQuery("select sw.wellId, count(*) " +
                                                    "from Screen s left join s.cherryPickRequests cpr left join cpr.screenerCherryPicks scp join scp.screenedWell sw " +
                                                    "where s." + Screen.facilityId.getPropertyName() + " = :screenId " +
                                                    "group by sw.wellId having count(*) > 1");
    query.setReadOnly(true);
    query.setParameter("screenId", screen.getFacilityId());
    Map<WellKey,Number> result = new HashMap<WellKey,Number>();
    for (Iterator iter = query.list().iterator(); iter.hasNext();) {
      Object[] row = (Object[]) iter.next();
      result.put(new WellKey(row[0].toString()), (Number) row[1]);
    }
    return result;
  }
}

