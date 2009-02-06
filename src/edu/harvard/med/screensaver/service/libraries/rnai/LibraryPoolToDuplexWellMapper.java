// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries.rnai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

//TODO: the name of this class evolved away from it original intended use of
//just performing the pool-to-duplex *well* mapping. It is simply more
//convenient to create the LabCherryPicks for a set of ScreenerCherryPicks of
//pool wells.  Should rename though...
public class LibraryPoolToDuplexWellMapper
{
  // static members

  private static Logger log = Logger.getLogger(LibraryPoolToDuplexWellMapper.class);


  // instance data members


  // public constructors and methods

  public LibraryPoolToDuplexWellMapper()
  {
  }

  /**
   * Creates LabCherryPicks in the specified CherryPickRequest by mapping
   * ScreenerCherryPick source wells of Dharmacon SMARTPool libraries to the
   * respective wells in the related Dharmacon duplex library, where the duplex
   * wells contain the same silencing reagents as the pool well.
   * <p>
   * Pool-to-duplex mapping anomalies (i.e., when less than 4 duplexes are
   * found, and, in particular, when 0 duplexes are found) can be determined by
   * finding resultant ScreenerCherryPicks that do not have 4 LabCherryPicks.
   */
  public Set<LabCherryPick> createDuplexLabCherryPicksforPoolScreenerCherryPicks(RNAiCherryPickRequest cherryPickRequest)
  {
    // TODO: currently assumes that all RNAi cherry picks are from Dharmacon
    // libraries, which are split into pool and duplex libraries

    Set<LabCherryPick> labCherryPicks = Sets.newHashSetWithExpectedSize(cherryPickRequest.getScreenerCherryPicks().size() * 4);
    Set<ScreenerCherryPick> screenerCherryPicks = cherryPickRequest.getScreenerCherryPicks();
    Set<Well> allDuplexWells = Sets.newHashSet();
    for (ScreenerCherryPick screenerCherryPick : screenerCherryPicks) {
      Well poolWell = screenerCherryPick.getScreenedWell();
      Set<Well> duplexWells = mapPoolWellToDuplexWells(poolWell);
      for (Well duplexWell : duplexWells) {
        if (allDuplexWells.contains(duplexWell)) {
          throw new BusinessRuleViolationException("screener cherry pick for pool well " + 
                                                   screenerCherryPick.getScreenedWell().getWellKey() + 
                                                   " maps to duplexes that have already been added for another screener cherry pick");
        }
        labCherryPicks.add(cherryPickRequest.createLabCherryPick(screenerCherryPick, duplexWell));
      }
      allDuplexWells.addAll(duplexWells);
    }
    return labCherryPicks;
  }

  public Set<Well> mapPoolWellToDuplexWells(Well poolWell)
  {
    Set<Well> duplexWells = new HashSet<Well>();
    String duplexLibraryName = getDuplexLibraryNameForPoolLibrary(poolWell.getLibrary());
    for (SilencingReagent silencingReagent : poolWell.getSilencingReagents()) {
      for (Well candidateDuplexWell : silencingReagent.getWells()) {
        if (candidateDuplexWell.getLibrary().getLibraryName().equals(duplexLibraryName)) {
          duplexWells.add(candidateDuplexWell);
        }
      }
    }
    return duplexWells;
  }

  public Map<Well,Well> mapDuplexWellsToPoolWells(Set<Well> duplexWells)
  {
    Map<Well,Well> result = new HashMap<Well,Well>();
    for (Well duplexWell : duplexWells) {
      result.put(duplexWell, mapDuplexWellToPoolWell(duplexWell));
    }
    return result;
  }

  public Well mapDuplexWellToPoolWell(Well duplexWell)
  {
    if (duplexWell.getSilencingReagents().size() != 1) {
      throw new BusinessRuleViolationException("to map duplex well to pool well, duplex well must have exactly 1 silencing reagent, " +
          "but has " + duplexWell.getSilencingReagents().size());
    }
    Well poolWell = null;
    SilencingReagent silencingReagent = duplexWell.getSilencingReagents().iterator().next();
    String poolLibraryName = getPoolLibraryNameForDuplexLibrary(duplexWell.getLibrary());
    for (Well candidatePoolWell : silencingReagent.getWells()) {
      if (candidatePoolWell.getLibrary().getLibraryName().equals(poolLibraryName)) {
        if (poolWell == null) {
          poolWell = candidatePoolWell;
        }
        else {
          throw new BusinessRuleViolationException("duplex well " + duplexWell.getWellKey() + " maps to multiple pool wells");
        }
      }
    }
    return poolWell;
  }


  // private methods

  private String getDuplexLibraryNameForPoolLibrary(Library library)
  {
    // Note: this mapping relies upon our library naming convention
    String duplexLibraryName = library.getLibraryName()
                                      .replace("Pools", "Duplexes");
    if (!duplexLibraryName.contains("Duplexes")) {
      throw new IllegalArgumentException("Dharmacon pool library '"
                                         + library.getLibraryName()
                                         + "' cannot be mapped to a duplex library name");
    }
    return duplexLibraryName;
  }

  private String getPoolLibraryNameForDuplexLibrary(Library library)
  {
    // Note: this mapping relies upon our library naming convention
    String poolLibraryName = library.getLibraryName()
                                      .replace("Duplexes", "Pools");
    if (!poolLibraryName.contains("Pools")) {
      throw new IllegalArgumentException("Dharmacon duplex library '"
                                         + library.getLibraryName()
                                         + "' cannot be mapped to a pool library name");
    }
    return poolLibraryName;
  }
}

