// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/service/cherrypicks/CherryPickRequestPlateMapper.java $
// $Id: CherryPickRequestPlateMapper.java 2292 2008-04-10 18:13:42Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

/**
 * For a cherry pick request, provides methods to upate the status of its assay plates.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CherryPickRequestPlateStatusUpdater
{
  private static Logger log = Logger.getLogger(CherryPickRequestPlateStatusUpdater.class);

  private GenericEntityDAO _dao;


  /** @motivation for CGLIB2 */
  protected CherryPickRequestPlateStatusUpdater() {} 

  public CherryPickRequestPlateStatusUpdater(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  @Transactional
  public void updateAssayPlatesStatus(Set<CherryPickAssayPlate> assayPlates,
                                      ScreensaverUser performedByIn,
                                      LocalDate datePerformed,
                                      String comments,
                                      CherryPickLiquidTransferStatus status)
  {
    if (assayPlates.isEmpty()) {
      return;
    }
    CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(assayPlates.iterator().next().getCherryPickRequest());
    ScreensaverUser performedBy = _dao.reloadEntity(performedByIn);
    CherryPickLiquidTransfer liquidTransfer =
      cherryPickRequest.getScreen().createCherryPickLiquidTransfer(performedBy,
                                                                   datePerformed,
                                                                   status);
    liquidTransfer.setComments(comments);
    _dao.saveOrUpdateEntity(liquidTransfer);
    for (CherryPickAssayPlate assayPlate : assayPlates) {
      if (!assayPlate.getCherryPickRequest().equals(cherryPickRequest)) {
        throw new DataModelViolationException("all assay plates must be from the specified cherry pick request");
      }
      if (assayPlate.isPlated()) {
        throw new BusinessRuleViolationException("cannot record successful liquid transfer more than once for a cherry pick assay plate");
      }
      assayPlate.setCherryPickLiquidTransfer(liquidTransfer);
      _dao.saveOrUpdateEntity(assayPlate);
    }
  }
  
}

