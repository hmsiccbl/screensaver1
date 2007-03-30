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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;

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
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestAllocator.class);


  // instance data members
  
  private DAO _dao;


  // public constructors and methods

  public CherryPickRequestAllocator(DAO dao)
  {
    _dao = dao;
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
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
        BigDecimal volume = cherryPickRequest.getMicroliterTransferVolumePerWellApproved();
        if (volume == null) {
          throw new BusinessRuleViolationException("cannot allocate cherry picks unless the approved transfer volume has been specified in the cherry pick request");
        }
        // TODO: this check should be done in CherryPickRequest instead
        if (volume.compareTo(BigDecimal.ZERO) <= 0) {
          throw new DataModelViolationException("cherry pick request approved transfer volume must be positive");
        }
        for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
          Copy copy = selectCopy(labCherryPick.getSourceWell(),
                                 cherryPickRequest.getMicroliterTransferVolumePerWellApproved());
          if (copy == null) {
            unfulfillableLabCherryPicks.add(labCherryPick);
          }
          else {
            labCherryPick.setAllocated(copy);
          }
        }
      }
    });
    return unfulfillableLabCherryPicks;
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
            throw new BusinessRuleViolationException("cannot deallocate a cherry pick after it is mapped");
          }
          labCherryPick.setAllocated(null);
        }
      }
    });
  }

  
  // private methods
  
  private Copy selectCopy(Well well, BigDecimal volumeNeeded)
  {
    List<Copy> copies = new ArrayList<Copy>(well.getLibrary().getCopies());
    Collections.sort(copies, SourceCopyComparator.getInstance());

    for (Copy copy : copies) {
      BigDecimal wellCopyVolumeRemaining = calculateRemainingVolumeInCopyWell(copy, well);
      if (wellCopyVolumeRemaining.compareTo(volumeNeeded) >= 0) {
        return copy;
      }
    }
    return null;
  }

  private BigDecimal calculateRemainingVolumeInCopyWell(Copy copy, Well well)
  {
    BigDecimal startingVolume = getStartingVolumeInCopyWell(copy, well);
    Set<LabCherryPick> existingLabCherryPicksForWell = well.getLabCherryPicks();

    BigDecimal remainingVolume = startingVolume;
    for (LabCherryPick existingLabCherryPick : existingLabCherryPicksForWell) {
      if (existingLabCherryPick.isAllocated()) { // implicitly ignores a cherry pick if it's in the process of being allocated (by caller)
        if (existingLabCherryPick.getSourceCopy().equals(copy)) {
          CherryPickRequest otherCherryPickRequest  = existingLabCherryPick.getCherryPickRequest();
          BigDecimal volumeUsed = otherCherryPickRequest.getMicroliterTransferVolumePerWellApproved();
          if (volumeUsed != null) {
            remainingVolume = remainingVolume.subtract(volumeUsed);
            if (remainingVolume.compareTo(BigDecimal.ZERO) <= 0) {
              return BigDecimal.ZERO;
            }
          }
        }
      }
    }
    return remainingVolume;
  }

  private BigDecimal getStartingVolumeInCopyWell(Copy copy, Well well)
  {
    CopyInfo plateCopyInfo = copy.getCopyInfo(well.getPlateNumber());
    if (plateCopyInfo == null) {
      // the library copy (apparently) does not have a copy for the plate of the requested well;
      // [ant4: assuming this is a valid state of the data model, otherwise we should throw a DataModelViolationException]
      return BigDecimal.ZERO;
    }
    if (plateCopyInfo.isRetired()) {
      // if plate has been retired, it has no usable volume
      return BigDecimal.ZERO;
    }
    BigDecimal startingVolume = plateCopyInfo.getVolume();
    return startingVolume;
  }
}
