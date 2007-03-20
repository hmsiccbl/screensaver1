// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.screens.CherryPick;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
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
  
  private DAO dao;


  // public constructors and methods

  public CherryPickRequestPlateMapper(DAO dao)
  {
    this.dao = dao;
  }


  // public constructors and methods

  public void generatePlateMapping(final CherryPickRequest cherryPickRequestIn)
  {
    dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        CherryPickRequest cherryPickRequest = (CherryPickRequest) dao.reattachEntity(cherryPickRequestIn);
        doGeneratePlateMapping(cherryPickRequest);
      }
    });
  }

  
  // private methods

  private void doGeneratePlateMapping(CherryPickRequest cherryPickRequest)
  {
    SortedSet<CherryPick> toBeMapped = findCherryPicksToBeMapped(cherryPickRequest);
    List<WellName> availableWellNamesMaster = findAvailableWellNames(cherryPickRequest);
    List<WellName> availableWellNamesWorking = null;
    int plateIndex = -1;
    Map<CherryPick,Pair<Integer,WellName>> plateWellMapping = new HashMap<CherryPick,Pair<Integer,WellName>>();

    // first pass: assign cherry picks to wells, in a temporary data structure
    // (since we need to know total plate count before calling
    // cherryPick.setMapped())
    List<CherryPick> nextIndivisibleBlock;
    while (toBeMapped.size() > 0) {
      nextIndivisibleBlock = findNextIndivisibleBlock(toBeMapped);
      if (nextIndivisibleBlock.size() > availableWellNamesMaster.size() ){
        throw new BusinessRuleViolationException("there are more cherry picks from " + 
                                                 "a single source plate than can fit on a single assay plate");
      }

      // create next plate, if necessary
      if (plateIndex == -1 || nextIndivisibleBlock.size() > availableWellNamesWorking.size()) {
        availableWellNamesWorking = new ArrayList<WellName>(availableWellNamesMaster);
        
        if (cherryPickRequest.isRandomizedAssayPlateLayout()) {
          Collections.shuffle(availableWellNamesWorking);
        }
        plateIndex++;
      }

      plateWellMapping.putAll(plateMapCherryPicks(nextIndivisibleBlock,
                                                  plateIndex,
                                                  availableWellNamesWorking));
    }

    // second pass: for each cherry pick, generate plate name and call cherryPick.setMapped()
    updateCherryPicks(cherryPickRequest, plateIndex + 1, plateWellMapping);
  }

  private void updateCherryPicks(CherryPickRequest cherryPickRequest,
                                 int plateCount,
                                 Map<CherryPick,Pair<Integer,WellName>> plateWellMapping)
  {
    for (Map.Entry<CherryPick,Pair<Integer,WellName>> entry : plateWellMapping.entrySet()) {
      CherryPick cherryPick = entry.getKey();
      Integer assayPlateNumber = entry.getValue().getFirst();
      WellName assayWellName = entry.getValue().getSecond();

      String assayPlateName = makePlateName(cherryPickRequest,
                                                assayPlateNumber,
                                                plateCount);
      cherryPick.setMapped(cherryPickRequest.getAssayPlateType(),
                           assayPlateName,
                           assayWellName.getRowIndex(),
                           assayWellName.getColumnIndex());
    }
  }

  private String makePlateName(CherryPickRequest cherryPickRequest,
                                   int i,
                                   int totalPlateCount)
  {
    StringBuilder name = new StringBuilder();
    name.append(cherryPickRequest.getRequestedBy().getFullNameFirstLast()).
    append(" (").append(cherryPickRequest.getEntityId()).append(") ").
    append("CP").append(cherryPickRequest.getOrdinal()).
    append("  Plate ").append(String.format("%02d", (i + 1))).append(" of ").
    append(totalPlateCount);
    return name.toString();
  }

  private Map<CherryPick,Pair<Integer,WellName>> plateMapCherryPicks(List<CherryPick> nextIndivisibleBlock,
                                                                     int plateIndex,
                                                                     List<WellName> availableWellNames)

  {
    Map<CherryPick,Pair<Integer,WellName>> plateWellMapping = new HashMap<CherryPick,Pair<Integer,WellName>>();
    assert availableWellNames.size() >= nextIndivisibleBlock.size();
    Iterator<WellName> availableWellNamesIter = availableWellNames.iterator();
    for (CherryPick cherryPick : nextIndivisibleBlock) {
      WellName assayWellName = availableWellNamesIter.next();
      plateWellMapping.put(cherryPick,
                           new Pair<Integer,WellName>(plateIndex, assayWellName));
      availableWellNamesIter.remove();
    }
    return plateWellMapping;
  }


  private List<CherryPick> findNextIndivisibleBlock(SortedSet<CherryPick> toBeMapped)
  {
    List<CherryPick> block = new ArrayList<CherryPick>();
    Integer plateNumber = null;
    String copyName = null;
    for (Iterator<CherryPick> iter = toBeMapped.iterator(); iter.hasNext();) {
      CherryPick cherryPick = (CherryPick) iter.next();
      if (plateNumber == null) {
        plateNumber = cherryPick.getSourceWell().getPlateNumber();
        copyName = cherryPick.getSourceCopy().getName();
      }
      
      if (!plateNumber.equals(cherryPick.getSourceWell().getPlateNumber()) ||
        !copyName.equals(cherryPick.getSourceCopy().getName())) {
        break;
      }

      iter.remove();
      block.add(cherryPick);
    }
    return block;
  }

  private List<WellName> findAvailableWellNames(CherryPickRequest cherryPickRequest)
  {
    List<WellName> availableWellNames = new ArrayList<WellName>();
    for (int column = Well.MIN_WELL_COLUMN; column <= Well.MAX_WELL_COLUMN; column++) {
      for (char row = Well.MIN_WELL_ROW; row <= Well.MAX_WELL_ROW; row++) {
        if (!cherryPickRequest.getEmptyColumnsOnAssayPlate().contains(column) &&
          !cherryPickRequest.getEmptyRowsOnAssayPlate().contains(row)) {
          availableWellNames.add(new WellName(row, column));
        }
      }
    }
    return availableWellNames;
  }

  private SortedSet<CherryPick> findCherryPicksToBeMapped(CherryPickRequest cherryPickRequest)
  {
    SortedSet<CherryPick> toBeMapped = new TreeSet<CherryPick>(PlateMappingCherryPickComparator.getInstance());
    for (Iterator iter = cherryPickRequest.getCherryPicks().iterator(); iter.hasNext();) {
      CherryPick cherryPick = (CherryPick) iter.next();
      // no cherry pick may be plated at this time
      if (cherryPick.isPlated()) {
        throw new BusinessRuleViolationException("cannot generate assay plate mapping if any cherry pick has already been plated");
      }
      // note: some cherry picks may have been unfulfillable, and therefore not allocated
      if (cherryPick.isAllocated()) {
        toBeMapped.add(cherryPick);
      }
    }
    return toBeMapped;
  }
}

