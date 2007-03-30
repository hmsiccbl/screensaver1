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
    SortedSet<LabCherryPick> toBeMapped = findLabCherryPicksToBeMapped(cherryPickRequest);
    List<WellName> availableWellNamesMaster = findAvailableWellNames(cherryPickRequest);
    List<WellName> availableWellNamesWorking = null;
    int plateIndex = 0;
    CherryPickAssayPlate assayPlate = null;

    List<LabCherryPick> nextIndivisibleBlock;
    while (toBeMapped.size() > 0) {
      int toBeMappedCount = toBeMapped.size();
      nextIndivisibleBlock = findNextIndivisibleBlock(toBeMapped);
      if (nextIndivisibleBlock.size() > availableWellNamesMaster.size() ){
        throw new BusinessRuleViolationException("there are more cherry picks from " + 
                                                 "a single source plate than can fit on a single assay plate");
      }

      // create next plate, if necessary
      if (assayPlate == null || nextIndivisibleBlock.size() > availableWellNamesWorking.size()) {
        availableWellNamesWorking = new ArrayList<WellName>(availableWellNamesMaster);
        
        if (cherryPickRequest.isRandomizedAssayPlateLayout()) {
          // only randomize over the left-most set of wells whose size is sufficent
          // to accommodate the remaining cherry picks to be mapped
          availableWellNamesWorking.subList(Math.min(toBeMappedCount,
                                                     availableWellNamesWorking.size()),
                                            availableWellNamesWorking.size()).clear();
          Collections.shuffle(availableWellNamesWorking);
        }
        assayPlate = new CherryPickAssayPlate(cherryPickRequest, 
                                              plateIndex++, 
                                              0, 
                                              cherryPickRequest.getAssayPlateType());
      }

      plateMapCherryPicks(assayPlate,
                          nextIndivisibleBlock,
                          availableWellNamesWorking);
    }

  }

  private Map<LabCherryPick,Pair<Integer,WellName>> plateMapCherryPicks(CherryPickAssayPlate assayPlate,
                                                                        List<LabCherryPick> nextIndivisibleBlock,
                                                                        List<WellName> availableWellNames)

  {
    Map<LabCherryPick,Pair<Integer,WellName>> plateWellMapping = new HashMap<LabCherryPick,Pair<Integer,WellName>>();
    assert availableWellNames.size() >= nextIndivisibleBlock.size();
    Iterator<WellName> availableWellNamesIter = availableWellNames.iterator();
    for (LabCherryPick cherryPick : nextIndivisibleBlock) {
      WellName assayWellName = availableWellNamesIter.next();
      cherryPick.setMapped(assayPlate,
                           assayWellName.getRowIndex(),
                           assayWellName.getColumnIndex());
      availableWellNamesIter.remove();
    }
    return plateWellMapping;
  }

  private List<LabCherryPick> findNextIndivisibleBlock(SortedSet<LabCherryPick> toBeMapped)
  {
    List<LabCherryPick> block = new ArrayList<LabCherryPick>();
    Integer plateNumber = null;
    String copyName = null;
    for (Iterator<LabCherryPick> iter = toBeMapped.iterator(); iter.hasNext();) {
      LabCherryPick cherryPick = (LabCherryPick) iter.next();
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
    for (Iterator iter = cherryPickRequest.getLabCherryPicks().iterator(); iter.hasNext();) {
      LabCherryPick cherryPick = (LabCherryPick) iter.next();
      if (cherryPick.isMapped() || cherryPick.isPlated()) {
        // no cherry picks may be plated at this time
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

