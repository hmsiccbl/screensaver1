// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
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
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

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

  public CherryPickRequestPlateMapper(GenericEntityDAO dao)
  {
    this.genericEntityDao = dao;
  }


  // public constructors and methods

  public void generatePlateMapping(final CherryPickRequest cherryPickRequestIn)
  {
    genericEntityDao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) genericEntityDao.reattachEntity(cherryPickRequestIn);
        doGeneratePlateMapping(cherryPickRequest);
      }
    });
  }

  
  // private methods

  private void doGeneratePlateMapping(CherryPickRequest cherryPickRequest)
  {
    SortedSet<LabCherryPick> toBeMapped = findLabCherryPicksToBeMapped(cherryPickRequest);
    List<WellName> availableWellNamesMaster = findAvailableWellNames(cherryPickRequest);
    List<WellName> availableWellNamesOnCurrentPlate = null;
    int plateIndex = 0;
    CherryPickAssayPlate plate = null;
    
    List<LabCherryPick> labCherryPicksAssignedToCurrentPlate = new ArrayList<LabCherryPick>();
    while (toBeMapped.size() > 0) {
      plate = new CherryPickAssayPlate(cherryPickRequest, 
                                       plateIndex++, 
                                       0, 
                                       cherryPickRequest.getAssayPlateType());

      labCherryPicksAssignedToCurrentPlate.clear();
      do {
        List<LabCherryPick> nextIndivisibleBlock = findNextIndivisibleBlock(toBeMapped);
        if (nextIndivisibleBlock.size() == 0) {
          break;
        }
        int remainingWellCountOnPlate = availableWellNamesMaster.size() - labCherryPicksAssignedToCurrentPlate.size();

        if (remainingWellCountOnPlate - nextIndivisibleBlock.size() < 0) {
          // if there were more wells from a given source plate than can fit on a single assay plate;
          // we have to split the source plate wells across multiple assay plates;
          // so we take lab cherry picks that wouldn't fit on the plate and add them
          // back to toBeMapped, for subsequent mapping on the next plate
          if (labCherryPicksAssignedToCurrentPlate.size() == 0) {
            // don't map the lab cherry picks that won't fit on this plate; this
            // is the one case where we are allowed to divide our indivisibleBlock!
            nextIndivisibleBlock.subList(remainingWellCountOnPlate, nextIndivisibleBlock.size()).clear();
          }
          else {
            break;
          }
        }
        labCherryPicksAssignedToCurrentPlate.addAll(nextIndivisibleBlock);
        toBeMapped.removeAll(nextIndivisibleBlock);
      } while (true);

      availableWellNamesOnCurrentPlate = new ArrayList<WellName>(availableWellNamesMaster);
      if (cherryPickRequest.isRandomizedAssayPlateLayout()) {
        // only randomize over the left-most set of wells whose size is sufficent
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
    }
    return plateWellMapping;
  }

  private List<LabCherryPick> findNextIndivisibleBlock(SortedSet<LabCherryPick> toBeMapped)
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
    for (int column = Well.MIN_WELL_COLUMN; column <= Well.MAX_WELL_COLUMN; column++) {
      for (char row = Well.MIN_WELL_ROW; row <= Well.MAX_WELL_ROW; row++) {
        if (!cherryPickRequest.getRequestedEmptyColumnsOnAssayPlate().contains(column) &&
          !cherryPickRequest.getRequiredEmptyColumnsOnAssayPlate().contains(column) &&
          !cherryPickRequest.getRequiredEmptyRowsOnAssayPlate().contains(row)) {
          availableWellNames.add(new WellName(row, column));
        }
      }
    }
    return availableWellNames;
  }

  private SortedSet<LabCherryPick> findLabCherryPicksToBeMapped(CherryPickRequest cherryPickRequest)
  {
    SortedSet<LabCherryPick> toBeMapped = new TreeSet<LabCherryPick>(PlateMappingCherryPickComparator.getInstance());
    for (Iterator iter = cherryPickRequest.getLabCherryPicks()
                                          .iterator(); iter.hasNext();) {
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
    return toBeMapped;
  }
}

