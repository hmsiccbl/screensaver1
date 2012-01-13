// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.util.Pair;

/**
 * For a cherry pick request, generates the layout of the cherry picks onto a
 * set of assay plates.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestPlateMapper
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestPlateMapper.class);


  // instance data members

  private GenericEntityDAO genericEntityDao;


  // public constructors and methods

  /** @motivation for CGLIB2 */
  protected CherryPickRequestPlateMapper() {} 

  public CherryPickRequestPlateMapper(GenericEntityDAO dao)
  {
    this.genericEntityDao = dao;
  }


  // public constructors and methods

  @Transactional
  public void generatePlateMapping(final CherryPickRequest cherryPickRequestIn)
  {
    CherryPickRequest cherryPickRequest = (CherryPickRequest) genericEntityDao.reloadEntity(cherryPickRequestIn);
    doGeneratePlateMapping(cherryPickRequest);
  }


  // private methods

  private void doGeneratePlateMapping(CherryPickRequest cherryPickRequest)
  {
    List<LabCherryPick> toBeMapped = findLabCherryPicksToBeMapped(cherryPickRequest);
    List<WellName> availableWellNamesMaster = findAvailableWellNames(cherryPickRequest);
    if (availableWellNamesMaster.size() == 0) {
      throw new BusinessRuleViolationException("cannot map cherry picks because all cherry pick assay plate wells have been requested to be kept empty"); 
    }
    List<WellName> availableWellNamesOnCurrentPlate = null;
    int plateIndex = 0;
    CherryPickAssayPlate plate = null;

    List<LabCherryPick> labCherryPicksAssignedToCurrentPlate = new ArrayList<LabCherryPick>();
    while (toBeMapped.size() > 0) {
      plate = cherryPickRequest.createCherryPickAssayPlate(
        plateIndex++,
        0,
        cherryPickRequest.getAssayPlateType());

      labCherryPicksAssignedToCurrentPlate.clear();
      do {
        List<LabCherryPick> nextBlock;
        if (cherryPickRequest.isKeepSourcePlateCherryPicksTogether()) {
          nextBlock = findNextIndivisibleBlock(toBeMapped);
        }
        else {
          nextBlock = Lists.newArrayList(toBeMapped);
        }
        if (nextBlock.size() == 0) {
          break;
        }
        int remainingWellCountOnPlate = availableWellNamesMaster.size() - labCherryPicksAssignedToCurrentPlate.size();

        if (remainingWellCountOnPlate - nextBlock.size() < 0) {
          // if there were more wells from a given source plate than can fit on a single assay plate;
          // we have to split the source plate wells across multiple assay plates;
          // so we take lab cherry picks that wouldn't fit on the plate and add them
          // back to toBeMapped, for subsequent mapping on the next plate
          if (labCherryPicksAssignedToCurrentPlate.size() == 0) {
            // don't map the lab cherry picks that won't fit on this plate; this
            // is the one case where we are allowed to divide our indivisibleBlock!
            nextBlock.subList(remainingWellCountOnPlate, nextBlock.size()).clear();
          }
          else {
            break;
          }
        }
        labCherryPicksAssignedToCurrentPlate.addAll(nextBlock);
        toBeMapped.removeAll(nextBlock);
      } while (true);

      availableWellNamesOnCurrentPlate = new ArrayList<WellName>(availableWellNamesMaster);
      if (cherryPickRequest.isRandomizedAssayPlateLayout()) {
        // only randomize over the left-most set of wells whose size is sufficient
        // to accommodate the remaining cherry picks to be mapped
        availableWellNamesOnCurrentPlate.subList(Math.min(labCherryPicksAssignedToCurrentPlate.size(),
                                                          availableWellNamesOnCurrentPlate.size()),
                                                 availableWellNamesOnCurrentPlate.size()).clear();
        Collections.shuffle(availableWellNamesOnCurrentPlate);
      }

      plateMapCherryPicks(plate,
                          labCherryPicksAssignedToCurrentPlate,
                          availableWellNamesOnCurrentPlate);
    }
  }

  private Map<LabCherryPick,Pair<Integer,WellName>> plateMapCherryPicks(CherryPickAssayPlate assayPlate,
                                                                        List<LabCherryPick> labCherryPicks,
                                                                        List<WellName> availableWellNames)

  {
    assert availableWellNames.size() >= labCherryPicks.size() :
      "cannot plate map cherry picks, since available well count was too small to accommodate all labCherryPicks (calling method has violated contract)";

    Map<LabCherryPick,Pair<Integer,WellName>> plateWellMapping = new HashMap<LabCherryPick,Pair<Integer,WellName>>();
    Iterator<WellName> availableWellNamesIter = availableWellNames.iterator();
    for (Iterator nextIndivisibleBlockIter = labCherryPicks.iterator(); nextIndivisibleBlockIter.hasNext();) {
      LabCherryPick cherryPick = (LabCherryPick) nextIndivisibleBlockIter.next();

      WellName assayWellName = availableWellNamesIter.next();
      cherryPick.setMapped(assayPlate,
                           assayWellName.getRowIndex(),
                           assayWellName.getColumnIndex());
      if (log.isDebugEnabled()) {
        log.debug(cherryPick + " mapped to " + cherryPick.getAssayPlate().getPlateOrdinal() + ":" + cherryPick.getAssayPlateWellName());
      }
      genericEntityDao.saveOrUpdateEntity(cherryPick);
    }
    return plateWellMapping;
  }

  private List<LabCherryPick> findNextIndivisibleBlock(List<LabCherryPick> toBeMapped)
  {
    List<LabCherryPick> block = new ArrayList<LabCherryPick>();
    Integer plateNumber = null;
    String copyName = null;
    for (LabCherryPick cherryPick : toBeMapped) {
      if (plateNumber == null) {
        plateNumber = cherryPick.getSourceWell().getPlateNumber();
        copyName = cherryPick.getSourceCopy().getName();
      }

      if (!plateNumber.equals(cherryPick.getSourceWell().getPlateNumber()) ||
        !copyName.equals(cherryPick.getSourceCopy().getName())) {
        break;
      }
      block.add(cherryPick);
    }
    return block;
  }

  private List<WellName> findAvailableWellNames(CherryPickRequest cherryPickRequest)
  {
    List<WellName> availableWellNames = new ArrayList<WellName>();
    for (int column = 0; column < cherryPickRequest.getAssayPlateType().getPlateSize().getColumns(); column++) {
      for (char row = 0; row < cherryPickRequest.getAssayPlateType().getPlateSize().getRows(); row++) {
        if (!cherryPickRequest.getEmptyWellsOnAssayPlate().contains(new WellName(row, column))) {
          availableWellNames.add(new WellName(row, column));
        }
      }
    }
    return availableWellNames;
  }

  private List<LabCherryPick> findLabCherryPicksToBeMapped(CherryPickRequest cherryPickRequest)
  {
    List<LabCherryPick> toBeMapped = Lists.newArrayList();
    for (Iterator iter = cherryPickRequest.getLabCherryPicks().iterator(); iter.hasNext();) {
      LabCherryPick cherryPick = (LabCherryPick) iter.next();
      if (cherryPick.isMapped() || cherryPick.isPlated()) {
        // no cherry picks may be plated at this time
        throw new BusinessRuleViolationException("cannot generate assay plate mapping if any cherry pick has already been mapped or plated");
      }
      // note: some cherry picks may have been unfulfillable, and therefore not
      // allocated; this is acceptable and we simply plate map only those that
      // are allocated
      if (cherryPick.isAllocated()) {
        toBeMapped.add(cherryPick);
      }
    }
    Collections.sort(toBeMapped, PlateMappingCherryPickComparator.getInstance());
    return toBeMapped;
  }
  
  /**
   * Find any assay plates on the CherryPickRequest a set of lab cherry picks from the
   * same source plate than would fit on a single assay plate. This matters to
   * the lab, which must be notified to manually reload the source plate when
   * creating the cherry pick plates. We can detect this case when the last well
   * (containing a cherry pick) on an assay plate is from the same source plate
   * as the first well on the next assay plate.
   */
  @Transactional(readOnly=true)
  public Map<CherryPickAssayPlate, Integer> getAssayPlatesRequiringSourcePlateReload(CherryPickRequest cpr)
  {
    cpr = genericEntityDao.reloadEntity(cpr);
    Map<CherryPickAssayPlate,Integer> platesRequiringReload = new HashMap<CherryPickAssayPlate,Integer>();
    LabCherryPick last = null;
    for (CherryPickAssayPlate assayPlate : cpr.getActiveCherryPickAssayPlates()) {
      if (assayPlate.getLabCherryPicks().size() > 0) {
        if (last != null) {
          LabCherryPick first = Collections.max(assayPlate.getLabCherryPicks(),
                                                LabCherryPickColumnMajorOrderingComparator.getInstance());
          if (last.getSourceWell().getPlateNumber().equals(first.getSourceWell().getPlateNumber())) {
            platesRequiringReload.put(assayPlate, first.getSourceWell().getPlateNumber());
          }
        }
        last = Collections.max(assayPlate.getLabCherryPicks(),
                               LabCherryPickColumnMajorOrderingComparator.getInstance());
      }
    }
    return platesRequiringReload;
  }

  
}

