// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;

import org.apache.log4j.Logger;

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
   * Maps cherry pick wells from a Dharmacon SMARTPool library to the respective
   * wells in the related Dharmacon duplex library. The mapping is keyed on the
   * SilencingReagents contained in the pool well, <i>not</i> the gene they are
   * silencing.
   */
  public Set<LabCherryPick> createDuplexLabCherryPicksforPoolScreenerCherryPicks(RNAiCherryPickRequest cherryPickRequest)
  {
    // TODO: currently assumes that all RNAi cherry picks are from Dharmacon
    // libraries, which are split into pool and duplex libraries
    
    // note: anomalies (i.e., when exactly 4 duplexes are not found, and,
    // most importantly, when 0 duplexes are found) are implicitly recorded in
    // our data model; a UI can handle notification of these cases as desired,
    // simply by finding ScreenerCherryPicks that do not have a sufficient
    // number of LabCherryPicks
    
    Set<LabCherryPick> labCherryPicks = new HashSet<LabCherryPick>(cherryPickRequest.getScreenerCherryPicks().size() * 4);
    for (ScreenerCherryPick screenerCherryPick : cherryPickRequest.getScreenerCherryPicks()) {
      Well poolWell = screenerCherryPick.getScreenedWell();
      String duplexLibraryName = getDuplexLibraryNameForPoolLibrary(poolWell.getLibrary());
      for (SilencingReagent silencingReagent : poolWell.getSilencingReagents()) {
        for (Well candidateCherryPickSourceWell : silencingReagent.getWells()) {
          if (candidateCherryPickSourceWell.getLibrary().getLibraryName().equals(duplexLibraryName)) {
            labCherryPicks.add(new LabCherryPick(screenerCherryPick, candidateCherryPickSourceWell));
          }
        }
      }
    }
    return labCherryPicks;
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

