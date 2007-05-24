// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.screendb.ScreenDBSynchronizer;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;

import org.apache.log4j.Logger;

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

    getHibernateTemplate().delete(screenerCherryPick);
  }

  public void deleteLabCherryPick(LabCherryPick labCherryPick)
  {
    if (labCherryPick.getCherryPickRequest().isAllocated()) {
      throw new BusinessRuleViolationException("cannot delete a lab cherry pick for a cherry pick request that has been allocated");
    }

    // disassociate from related entities
    labCherryPick.getCherryPickRequest().getLabCherryPicks().remove(labCherryPick);
//    if (labCherryPick.getSourceCopy() != null) {
//      labCherryPick.getSourceCopy().getHbnLabCherryPicks().remove(labCherryPick);
//    }
    if (labCherryPick.getAssayPlate() != null) {
      labCherryPick.getAssayPlate().getLabCherryPicks().remove(labCherryPick);
    }

    getHibernateTemplate().delete(labCherryPick);
  }

  public void deleteCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    deleteCherryPickRequest(cherryPickRequest, false);
  }
  
  /**
   * @motivation Bypassing business rule violation checks is present for the purpose of the
   * {@link ScreenDBRnaiCherryPickSynchronizer} only. This is a special-case situation where
   * data needs to be loaded from multiple sources - the ScreenDB synchronizer, and
   * to-be-written code to import data from the AllCherryPicks.xls file and the cherry
   * pick request .csv files themselves. The data may well lead to busines rule violations, as
   * well as data model violations, in the intermediate state where the ScreenDB synchronizer
   * has run, but the AllCherryPicks.xls importer has not.
   */
  public void deleteCherryPickRequest(
    final CherryPickRequest cherryPickRequestIn,
    boolean bypassBusinessRuleViolationChecks)
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
    // note: the cherryPickRequest.screen child-to-parent relationship is not cascaded, so although
    // screen entity *will* be available (it never loaded as a proxy), the screen's
    // relationships will not be reattached and will thus be inaccessible; in
    // particular, screen.cherryPickRequests (needed below)
    _dao.reattachEntity(cherryPickRequestIn.getScreen());

    if (! bypassBusinessRuleViolationChecks) {
      if (cherryPickRequestIn.isAllocated()) {
        throw new BusinessRuleViolationException("cannot delete a cherry pick request that has been allocated");
      }
    }

    // disassociate from related entities
    
    cherryPickRequest.getRequestedBy().getHbnCherryPickRequests().remove(cherryPickRequest);
    cherryPickRequest.getScreen().getCherryPickRequests().remove(cherryPickRequest);    
    getHibernateTemplate().delete(cherryPickRequest);
  }

  /**
   * @motivation for {@link ScreenDBSynchronizer}, for efficient removal of cherry pick
   * requests.
   */
  public void deleteAllCherryPickRequests()
  {
    // TODO: want to do the following, as in Spring 2.0 API:
    // http://www.springframework.org/docs/api/org/springframework/orm/hibernate/HibernateTemplate.html#delete(java.lang.String)
    // but it doesn't work - it ends up treating the String as an entity, and fails.
    //getHibernateTemplate().delete("from CherryPickRequest");
    getHibernateTemplate().deleteAll(getHibernateTemplate().find("from CherryPickRequest"));
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

  @SuppressWarnings("unchecked")
  public Set<LabCherryPick> findLabCherryPicksForWell(Well well)
  {
    return new HashSet<LabCherryPick>(
      getHibernateTemplate().find("from LabCherryPick where sourceWell = ?", well));
  }

  @SuppressWarnings("unchecked")
  public Set<ScreenerCherryPick> findScreenerCherryPicksForWell(Well well)
  {
    return new HashSet<ScreenerCherryPick>(
      getHibernateTemplate().find("from ScreenerCherryPick where screenedWell = ?", well));
  }
  
  // private methods

}

