// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.PowerSet;

/**
 * For a cherry pick request, selects source plate copies to draw from, and
 * records allocation of liquid needed to fulfill the request.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CherryPickRequestAllocator
{
  /**
   * The amount of microliter volume in a source copy well at which the well
   * should be considered depleted. By setting this to a positive value, we
   * account for real-world inaccuracies that might otherwise cause a source
   * well to be overdrawn. (A theoretically better approach might be to base the
   * inaccuracy on the number of times the well was drawn from, but the above
   * strategy is considered sufficient by the lab).
   */
  public static final Volume MINIMUM_SOURCE_WELL_VOLUME = new Volume(5);

  private static Logger log = Logger.getLogger(CherryPickRequestAllocator.class);

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private CherryPickRequestDAO _cherryPickRequestDao;


  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestAllocator()
  {
  }

  public CherryPickRequestAllocator(GenericEntityDAO dao,
                                    LibrariesDAO librariesDao,
                                    CherryPickRequestDAO cherryPickRequestDao)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _cherryPickRequestDao = cherryPickRequestDao;
  }

  /**
   * @return the set of <i>unfulfillable</i> cherry picks
   */
  @Transactional
  public Set<LabCherryPick> allocate(CherryPickRequest cherryPickRequestIn) throws DataAccessException
  {
    // TODO: handle concurrency; perform appropriate locking to prevent race conditions (overdrawing well) among multiple allocate() calls
    CherryPickRequest cherryPickRequest = _dao.reloadEntity(cherryPickRequestIn, false, "labCherryPicks.sourceWell");
    validateAllocationBusinessRules(cherryPickRequest);

    Set<LabCherryPick> unfulfillableLabCherryPicks = new HashSet<LabCherryPick>();
    Multimap<Integer,LabCherryPick> labCherryPicksBySourcePlate = getLabCherryPicksBySourcePlate(cherryPickRequest);
    for (Integer plateNumber : labCherryPicksBySourcePlate.keySet()) {
      log.debug("allocating " + cherryPickRequest + " lab cherry picks from plate " + plateNumber);
      unfulfillableLabCherryPicks.addAll(allocate(labCherryPicksBySourcePlate.get(plateNumber)));
    }

    return unfulfillableLabCherryPicks;
  }

  /**
   * @return the set of <i>unfulfillable</i> cherry picks
   * @throws DataModelViolationException if the source wells for the labCherryPicks contain duplicates
   */
  @Transactional
  public Set<LabCherryPick> allocate(Collection<LabCherryPick> labCherryPicks)
  {
    Set<LabCherryPick> unfulfillableLabCherryPicks = new HashSet<LabCherryPick>();
    if (labCherryPicks.size() == 0) {
      return unfulfillableLabCherryPicks;
    }

    try {
      final ImmutableMap<Well,LabCherryPick> well2lcp =
        Maps.uniqueIndex(labCherryPicks,
                         new Function<LabCherryPick, Well>() { public Well apply(LabCherryPick lcp) { return lcp.getSourceWell(); } });
      Map<Well,Set<Copy>> copyCandidatesForWells =
        findCopyCandidatesForWells(well2lcp.keySet(),
                                   labCherryPicks.iterator().next().getCherryPickRequest().getTransferVolumePerWellApproved());
      // remove unfulfillable wells now, as they would force the minimum copy set to always be empty
      Set<Well> unfulfillableWells = removeUnfulfillableWells(copyCandidatesForWells);
      assert Sets.intersection(unfulfillableWells, copyCandidatesForWells.keySet()).isEmpty();
      Set<Copy> minimumCopySetForWells = findMinimumCopySetForWells(copyCandidatesForWells);
      if (log.isDebugEnabled()) {
        log.debug("using minimum copy set: " + minimumCopySetForWells);
      }
      for (LabCherryPick labCherryPick : labCherryPicks) {
        if (!unfulfillableWells.contains(labCherryPick.getSourceWell())) {
          Set<Copy> copyCandidatesForWell = copyCandidatesForWells.get(labCherryPick.getSourceWell());
          Set<Copy> copyCandidatesForWellAndPlate  =
            Sets.intersection(minimumCopySetForWells, copyCandidatesForWell);
          if (log.isDebugEnabled()) {
            log.debug("copy candidates for well " + copyCandidatesForWell);
            log.debug("copy candidates for well and plate " + copyCandidatesForWellAndPlate);
          }
          assert !copyCandidatesForWellAndPlate.isEmpty() : "algorithm for determining minimum set of copies is incorrect";
          Copy selectedCopy = Collections.min(copyCandidatesForWellAndPlate );
          labCherryPick.setAllocated(selectedCopy);
          if (log.isDebugEnabled()) {
            log.debug("volume for " + labCherryPick + " allocated from " + selectedCopy);
          }
        }
      }

      Iterable<LabCherryPick> unfulfillableLCPsIter =
        Iterables.transform(unfulfillableWells, new Function<Well,LabCherryPick>() {
          public LabCherryPick apply(Well well) { return well2lcp.get(well); }
        });
      HashSet<LabCherryPick> unfulfillableLCPs = Sets.newHashSet(unfulfillableLCPsIter);
      if (log.isDebugEnabled()) {
        log.debug("unfulfillable lab cherry picks: " + unfulfillableLCPs);
      }
      return unfulfillableLCPs;
    }
    catch (IllegalArgumentException e) {
      //  We do not allow requests for allocation of
      //  multiple lab cherry picks that have the same source well. This is critical,
      //  since multiple allocations of the same source well could result in
      //  overdrawing reagent from the source well. This is due to the fact that
      //  remaining well volume checking is based upon the remaining well volumes as
      //  recorded in the database, and the implementation, above, does not currently handle
      //  the case where two or more reservations are being made from the same source
      //  well (it could).
      throw new DataModelViolationException("cannot allocate lab cherry picks if source wells are not unique");
    }
  }

  private void validateAllocationBusinessRules(CherryPickRequest cherryPickRequest)
  {
    Volume volume = cherryPickRequest.getTransferVolumePerWellApproved();
    if (volume == null) {
      throw new BusinessRuleViolationException("cannot allocate cherry picks unless the approved transfer volume has been specified in the cherry pick request");
    }
    // TODO: this check should be done in CherryPickRequest instead
    if (volume.compareTo(VolumeUnit.ZERO) <= 0) {
      throw new DataModelViolationException("cherry pick request approved transfer volume must be positive");
    }
  }

  private Set<Well> removeUnfulfillableWells(Map<Well,Set<Copy>> copyCandidatesForWells)
  {
    Set<Well> unfulfillableWells = new HashSet<Well>();
    Iterator<Entry<Well,Set<Copy>>> iterator = copyCandidatesForWells.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Well,Set<Copy>> entry = iterator.next();
      if (entry.getValue().isEmpty()) {
        iterator.remove();
        unfulfillableWells.add(entry.getKey());
      }
    }
    return unfulfillableWells;
  }

  private Multimap<Integer,LabCherryPick> getLabCherryPicksBySourcePlate(CherryPickRequest cherryPickRequest)
  {
    Multimap<Integer,LabCherryPick> plate2Wells = new HashMultimap<Integer,LabCherryPick>();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      plate2Wells.put(labCherryPick.getSourceWell().getPlateNumber(), labCherryPick);
    }
    return plate2Wells;
  }

  private Map<Well,Set<Copy>> findCopyCandidatesForWells(Set<Well> wells, Volume wellVolumeNeeded)
  {
    Map<Well,Set<Copy>> result = new HashMap<Well,Set<Copy>>();
    for (Well well : wells) {
      result.put(well, findCopiesWithSufficientVolume(well, wellVolumeNeeded));
    }
    return result;
  }

  private SortedSet<Copy> findCopiesWithSufficientVolume(Well well, Volume volumeNeeded)
  {
    if (log.isDebugEnabled()) {
      log.debug("need " + volumeNeeded + " for " + well);
    }
    SortedSet<Copy> result = new TreeSet<Copy>();
    Map<Copy,Volume> wellCopiesVolumeRemaining = _librariesDao.findRemainingVolumesInWellCopies(well);
    for (Copy copy : wellCopiesVolumeRemaining.keySet()) {
      Volume wellCopyVolumeRemaining = wellCopiesVolumeRemaining.get(copy);
      if (log.isDebugEnabled()) {
        log.debug("remaining volume in " + well + " " + copy + ": " + wellCopyVolumeRemaining);
      }
      if (wellCopyVolumeRemaining.subtract(volumeNeeded).compareTo(MINIMUM_SOURCE_WELL_VOLUME) >= 0) {
        result.add(copy);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("copies that satisfy need for volume " + volumeNeeded + ": " + result);
    }
    return result;
  }

  private Set<Copy> findMinimumCopySetForWells(Map<Well,Set<Copy>> wellCandidateCopies)
  {
    if (log.isDebugEnabled()) {
      log.debug("finding minimum copy set for wells " + wellCandidateCopies.keySet());
    }
    Set<Copy> distinctCopies = new HashSet<Copy>();
    Set<Set<Copy>> distinctCandidateCopySets = new HashSet<Set<Copy>>();
    for (Well well : wellCandidateCopies.keySet()) {
      Set<Copy> candidateCopies = wellCandidateCopies.get(well);
      distinctCandidateCopySets.add(candidateCopies);
      for (Copy copy : candidateCopies) {
        distinctCopies.add(copy);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("considering copies " + distinctCopies);
    }

    for (Set<Copy> minimumCopySetCandidate : PowerSet.orderedPowerset(distinctCopies)) {
      if (!minimumCopySetCandidate.isEmpty()) { // the empty set it *too* minimal, for our purposes :)
        if (log.isDebugEnabled()) {
          log.debug("considering minimum copy set: " + minimumCopySetCandidate);
        }
        for (Set<Copy> distinctCopySet : distinctCandidateCopySets) {
          SetView<Copy> intersection = Sets.intersection(minimumCopySetCandidate, distinctCopySet);
          if (intersection.isEmpty()) {
            // this minimum copy set candidate will not work
            if (log.isDebugEnabled()) {
              log.debug("minimum copy set " + minimumCopySetCandidate + " fails to satsify well copy set " + distinctCopySet);
            }
            minimumCopySetCandidate = null;
            break;
          }
        }
        if (minimumCopySetCandidate != null) {
          if (log.isDebugEnabled()) {
            log.debug("minimum copy set that satisfies all wells: " + minimumCopySetCandidate);
          }
          return minimumCopySetCandidate;
        }
      }
    }
    return Collections.emptySet();
  }

  @Transactional
  public void deallocate(final CherryPickRequest cherryPickRequestIn)
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(cherryPickRequestIn);
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (labCherryPick.isMapped()) {
        // note: for safety, we do not allow wholesale deallocation of cherry picks once they have been mapped to plates;
        // we do allow this to occur on a per-plate basis, however; see deallocateAssayPlates()
        throw new BusinessRuleViolationException("cannot deallocate all cherry picks (at once) after request has mapped plates");
      }
      if (labCherryPick.isAllocated()) {
        labCherryPick.setAllocated(null);
        _dao.saveOrUpdateEntity(labCherryPick);
      }
    }
  }

  @Transactional
  public void deallocateAssayPlates(Set<CherryPickAssayPlate> assayPlates)
  {
    if (assayPlates.isEmpty()) {
      return;
    }
    CherryPickRequest cherryPickRequest = (CherryPickRequest) _dao.reattachEntity(assayPlates.iterator().next().getCherryPickRequest());
    // note: we iterator through all of CPR's labCherryPicks, since
    // cpr.labCherryPicks has an 'update' cascade, while
    // assayPlate.labCherryPicks does not
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (labCherryPick.isMapped() && assayPlates.contains(labCherryPick.getAssayPlate())) {
        // note: it is okay to cancel a plate that has some (or all) lab cherry
        // picks that are unallocated
        if (labCherryPick.isAllocated()) {
          labCherryPick.setAllocated(null);
        }
      }
    }
  }

  /**
   * Create new CherryPickAssayPlates for a set of existing ones, preserving the
   * plate ordinal and plate type, but incrementing the attempt ordinal. The new
   * assay plates will have a new set of lab cherry picks that duplicate the
   * original plate's lab cherry picks, preserving their original well layout,
   * and repeating the allocation process (consuming additional well volumes on source plates).
   * @return the set of lab cherry picks that could not be reallocated, due to insufficient source copy volume
   */
  @Transactional
  public Set<LabCherryPick> reallocateAssayPlates(Set<CherryPickAssayPlate> assayPlates)
  {
    Set<LabCherryPick> unfullfilable = Sets.newHashSet();
    for (CherryPickAssayPlate assayPlate : assayPlates) {
      _dao.reattachEntity(assayPlate);

      // TODO: protect against race condition (should enforce at schema level)
      CherryPickAssayPlate newAssayPlate = (CherryPickAssayPlate) assayPlate.clone();
      Map<LabCherryPick,LabCherryPick> newLabCherryPicks = new HashMap<LabCherryPick,LabCherryPick>();
      for (LabCherryPick labCherryPick : assayPlate.getLabCherryPicks()) {
        LabCherryPick newLabCherryPick =
          labCherryPick.getScreenerCherryPick().getCherryPickRequest().createLabCherryPick(labCherryPick.getScreenerCherryPick(),
                                                                                           labCherryPick.getSourceWell());
        _dao.saveOrUpdateEntity(newLabCherryPick);
        newLabCherryPicks.put(newLabCherryPick, labCherryPick);
      }
      unfullfilable.addAll(allocate(newLabCherryPicks.keySet()));
      for (LabCherryPick newLabCherryPick : newLabCherryPicks.keySet()) {
        if (!unfullfilable.contains(newLabCherryPick)) {
          LabCherryPick originalLabCherryPick = newLabCherryPicks.get(newLabCherryPick);
          newLabCherryPick.setMapped(newAssayPlate,
                                     originalLabCherryPick.getAssayPlateRow(),
                                     originalLabCherryPick.getAssayPlateColumn());
        }
      }
    }
    return unfullfilable;
  }

}
