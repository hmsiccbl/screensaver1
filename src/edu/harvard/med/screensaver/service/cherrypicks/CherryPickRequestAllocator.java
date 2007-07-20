// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

/**
 * For a cherry pick request, selects source plate copies to draw from, and
 * records allocation of liquid needed to fulfill the request.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestAllocator
{
  /**
   * The amount of microliter volume in a source copy well at which the well
   * should be considered depleted. By setting this to a positive value, we
   * account for real-world inaccuracies that might otherwise cause a source
   * well to be overdrawn. (A theoretically better approach might be to base the
   * inaccurracy on the number of times the well was drawn from, but the above
   * strategy is considered sufficient by the lab).
   */
   public static final BigDecimal MINIMUM_SOURCE_WELL_VOLUME = new BigDecimal(1).setScale(Well.VOLUME_SCALE);


  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestAllocator.class);


  // instance data members
  
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private CherryPickRequestDAO _cherryPickRequestDao;


  // public constructors and methods

  public CherryPickRequestAllocator(GenericEntityDAO dao,
                                    LibrariesDAO librariesDao,
                                    CherryPickRequestDAO cherryPickRequestDao)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _cherryPickRequestDao = cherryPickRequestDao;
  }
  
  /**
   * 
   * @param cherryPickRequest
   * @return the set of <i>unfulfillable</i> cherry picks
   */
  public Set<LabCherryPick> allocate(final CherryPickRequest cherryPickRequestIn) throws DataAccessException
  {
    // TODO: handle concurrency; perform appropriate locking to prevent race conditions (overdrawing well) among multiple allocate() calls
    final Set<LabCherryPick> unfulfillableLabCherryPicks = new HashSet<LabCherryPick>();
    // TODO: new, non-nested txn should be required here, or if not possible, should perform a flush; this ensures that any LabCherryPicks allocated in an enclosing Hibernate session will be flushed to the database, ensuring that our genericEntityDao.findLabCherryPicksForWell() will return non-stale results
    _dao.flush();
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
//        // TODO: we really want to call reattachEntity(), to warn user of concurrent modification,
//        // but we get LazyInitEx, due to labCherryPicks.sourceWell not being cascaded
//        CherryPickRequest cherryPickRequest = (CherryPickRequest) 
//        _dao.reloadEntity(cherryPickRequestIn,
//                          false,
//                          "labCherryPicks.sourceWell.hbnLibrary.hbnCopies.hbnCopyInfos");
      CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
        validateAllocationBusinessRules(cherryPickRequest);
        for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
          if (!doAllocate(labCherryPick)) {
            unfulfillableLabCherryPicks.add(labCherryPick);
          }
        }
      }
    });
    return unfulfillableLabCherryPicks;
  }

  public boolean allocate(final LabCherryPick labCherryPickIn)
  {
    final boolean[] result = new boolean[1];
    // TODO: handle concurrency; perform appropriate locking to prevent race conditions (overdrawing well) among multiple allocate() calls
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        LabCherryPick labCherryPick = (LabCherryPick) _dao.reattachEntity(labCherryPickIn);
        validateAllocationBusinessRules(labCherryPick.getCherryPickRequest());
        result[0] = doAllocate(labCherryPick);
      }
    });
    return result[0];
  }

  public void deallocate(final CherryPickRequest cherryPickRequestIn)
  {
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
        for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
          if (labCherryPick.isMapped()) {
            // note: for safety, we do not allow wholesale deallocation of cherry picks once they have been mapped to plates;
            // we do allow this to occur on a per-plate basis, however, which requires the user to be more explicit about his intent/action
            throw new BusinessRuleViolationException("cannot deallocate all cherry picks (at once) after request has mapped plates");
          }
          if (labCherryPick.isAllocated()) {
            labCherryPick.setAllocated(null);
          }
        }
      }
    });
  }
  
  public void cancelAndDeallocateAssayPlates(final CherryPickRequest cherryPickRequestIn,
                                             final Set<CherryPickAssayPlate> assayPlates,
                                             final ScreensaverUser performedByIn,
                                             final Date dateOfLiquidTransfer,
                                             final String comments)
  {
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
        ScreensaverUser performedBy = _dao.reloadEntity(performedByIn);
        CherryPickLiquidTransfer cplt = new CherryPickLiquidTransfer(performedBy,
                                                                     new Date(),
                                                                     dateOfLiquidTransfer,
                                                                     cherryPickRequest,
                                                                     CherryPickLiquidTransferStatus.CANCELED);
        cplt.setComments(comments);
        // note: by iterating through cherryPickRequest's active assay plates, rather than the 
        // method assayPlates method arg, we are manipulating Hibernate-managed persistent entities, 
        // rather than deatch entities 
        for (CherryPickAssayPlate assayPlate : cherryPickRequest.getActiveCherryPickAssayPlates()) {
          if (assayPlates.contains(assayPlate)) {
            for (LabCherryPick labCherryPick : assayPlate.getLabCherryPicks()) {
              // note: it is okay to cancel a plate that has some (or all) lab cherry
              // picks that are unallocated
              if (labCherryPick.isAllocated()) {
                labCherryPick.setAllocated(null);
              }
            }
            assayPlate.setCherryPickLiquidTransfer(cplt);
          }
        }
      }
    });
  }
  

  // private methods
  
  private Copy selectCopy(Well well, BigDecimal volumeNeeded)
  {
    List<Copy> copies = new ArrayList<Copy>(well.getLibrary().getCopies());
    if (copies.size() == 0) {
      throw new DataModelViolationException("library " + well.getLibrary() + " has no Copies, so cannot allocate liquid");
    }
    Collections.sort(copies, SourceCopyComparator.getInstance());

    for (Copy copy : copies) {
      BigDecimal wellCopyVolumeRemaining = _librariesDao.findRemainingVolumeInWellCopy(well, copy);
      if (log.isDebugEnabled()) {
        log.debug("remaining volume in " + well + " " + copy + ": " + wellCopyVolumeRemaining);
      }
      if (wellCopyVolumeRemaining.subtract(volumeNeeded).compareTo(MINIMUM_SOURCE_WELL_VOLUME) >= 0) {
        if (log.isDebugEnabled()) {
          log.debug("selected " + copy + " to satisfy need for volume " + volumeNeeded);
        }
        return copy;
      }
    }
    return null;
  }

  private void validateAllocationBusinessRules(CherryPickRequest cherryPickRequest)
  {
    BigDecimal volume = cherryPickRequest.getMicroliterTransferVolumePerWellApproved();
    if (volume == null) {
      throw new BusinessRuleViolationException("cannot allocate cherry picks unless the approved transfer volume has been specified in the cherry pick request");
    }
    // TODO: this check should be done in CherryPickRequest instead
    if (volume.compareTo(BigDecimal.ZERO) <= 0) {
      throw new DataModelViolationException("cherry pick request approved transfer volume must be positive");
    }
  }

  private boolean doAllocate(LabCherryPick labCherryPick)
  {
    // note: we reload sourceWell, since lack of 'update' cascade for
    // labCherryPick.sourceWell will prevent sourceWell (or further transitive
    // relationships, such as libraries.copies) from being reattached in calling code
    Well sourceWell = _dao.reloadEntity(labCherryPick.getSourceWell(), true, "hbnLibrary.hbnCopies.hbnCopyInfos");
    Copy copy = selectCopy(sourceWell,
                           labCherryPick.getCherryPickRequest().getMicroliterTransferVolumePerWellApproved());
    if (copy == null) {
      return false;
    }
    else {
      labCherryPick.setAllocated(copy);
    }
    return true;
  }
}
