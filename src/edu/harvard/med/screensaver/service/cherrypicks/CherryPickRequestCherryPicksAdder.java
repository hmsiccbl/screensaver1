// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.InvalidCherryPickWellException;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 * For a cherry pick request, adds the specified cherry pick wells as
 * {@link ScreenerCherryPick}s and {@link LabCherryPick}s, performing
 * pool-to-duplex deconvolution, if requested.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CherryPickRequestCherryPicksAdder
{
  private static Logger log = Logger.getLogger(CherryPickRequestCherryPicksAdder.class);

  private GenericEntityDAO _dao;

  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestCherryPicksAdder()
  {
  }

  public CherryPickRequestCherryPicksAdder(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  @Transactional
  public CherryPickRequest addCherryPicksForWells(CherryPickRequest cherryPickRequest,
                                                  Set<WellKey> cherryPickWellKeys,
                                                  boolean deconvoluteToDuplexWells)
    throws InvalidCherryPickWellException
  {
    cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequest);
    if (cherryPickRequest.isAllocated()) {
      throw new BusinessRuleViolationException("cherry picks cannot be added to a cherry pick request that has already been allocated");
    }

    for (WellKey wellKey : cherryPickWellKeys) {
      String[] eagerFetchRelationships = {};
      if (cherryPickRequest.getScreen().getScreenType() == ScreenType.RNAI) {
        eagerFetchRelationships = new String[] { Well.latestReleasedReagent.to(SilencingReagent.duplexWells).getPath() };
      }
      else if (cherryPickRequest.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
        eagerFetchRelationships = new String[] { Well.latestReleasedReagent.getPath() }; // TODO: necessary anymore?
      }
      Well well = _dao.findEntityById(Well.class,
                                      wellKey.toString(),
                                      true,
                                      eagerFetchRelationships);
      if (well == null) {
        throw new InvalidCherryPickWellException(wellKey, "no such well");
      }
      else {
        // note: well allow SCP to be created, even if LCP cannot be created for
        // it; this allows the screener's requested wells to be recorded, and
        // problems to be handled by admins
        ScreenerCherryPick screenerCherryPick = cherryPickRequest.createScreenerCherryPick(well);
        if (!deconvoluteToDuplexWells) {
          if (well.<Reagent>getLatestReleasedReagent() != null) { 
            screenerCherryPick.createLabCherryPick(well);
          }
        }
        else {
          SilencingReagent reagent = screenerCherryPick.getScreenedWell().<SilencingReagent>getLatestReleasedReagent();
          if (reagent != null) {
            Set<Well> duplexWells = reagent.getDuplexWells();
            for (Well duplexWell : duplexWells) {
              if (duplexWell.<Reagent>getLatestReleasedReagent() != null) {
                screenerCherryPick.createLabCherryPick(duplexWell);
              }
            }
          }
        }
      }
    }
    return cherryPickRequest;
  }
}