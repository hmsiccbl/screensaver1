// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="rnai_cherry_pick_request" lazy="false"
 * @hibernate.joined-subclass-key column="cherry_pick_request_id"
 */
public class RNAiCherryPickRequest extends CherryPickRequest
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(RNAiCherryPickRequest.class);

  private static final Set<Integer> REQUIRED_EMPTY_COLUMNS = 
    new HashSet<Integer>(Arrays.asList(Well.MIN_WELL_COLUMN,
                                       Well.MIN_WELL_COLUMN + 1,
                                       Well.MAX_WELL_COLUMN - 1,
                                       Well.MAX_WELL_COLUMN ));

  private static final PlateType RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE = PlateType.EPPENDORF;


  // instance data members

  private String _assayProtocol;
  private RNAiCherryPickScreening _rnaiCherryPickScreening;


  public RNAiCherryPickRequest(Screen screen,
                               ScreeningRoomUser requestedBy,
                               Date dateRequested)
  {
    super(screen, requestedBy, dateRequested);
    super.getEmptyColumnsOnAssayPlate().addAll(REQUIRED_EMPTY_COLUMNS);
  }

  // public constructors and methods
  
  /**
   * Set the assay protocol.
   * 
   * @param assayProtocol the new assay protocol
   */
  public void setAssayProtocol(String assayProtocol)
  {
    _assayProtocol = assayProtocol;
  }
  
  /**
   * Get the assay protocol.
   * 
   * @return the assay protocol
   * @hibernate.property
   */
  public String getAssayProtocol()
  {
    return _assayProtocol;
  }
  
  public PlateType getAssayPlateType()
  {
    return RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE;
  }

  /**
   * Get the set of RNAi cherry pick assay
   *
   * @return the RNAi cherry pick assay
   * @hibernate.one-to-one
   *   class="edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening"
   *   property-ref="rnaiCherryPickRequest"
   *   cascade="save-update"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToOneRelationship(inverseProperty="rnaiCherryPickRequest")
  public RNAiCherryPickScreening getRnaiCherryPickScreening()
  {
    return _rnaiCherryPickScreening;
  }

  public void setRnaiCherryPickScreening(RNAiCherryPickScreening rnaiCherryPickScreening)
  {
    _rnaiCherryPickScreening = rnaiCherryPickScreening;
  }
  
  @Override
  public void setEmptyColumnsOnAssayPlate(Set<Integer> emptyColumnsOnAssayPlate)
  {
    super.setEmptyColumnsOnAssayPlate(emptyColumnsOnAssayPlate);
    getEmptyColumnsOnAssayPlate().addAll(REQUIRED_EMPTY_COLUMNS);
  }

  @Override
  public Set<Well> findCherryPickSourceWells(Set<Well> poolCherryPickWells)
  {
    // TODO: currently assumes that all RNAi cherry picks are from Dharmacon
    // libraries, which are split into pool and duplex libraries
    
    // TODO: assert source/pool well is experimental
    
    // TODO: report anomalies, i.e., when 4 duplexes are not found
    
    Set<Well> duplexWells = new HashSet<Well>(poolCherryPickWells.size() * 4 /*duplexes per pool*/);
    for (Well poolCherryPickWell : poolCherryPickWells) {
      String duplexLibraryName = getDuplexLibraryNameForPoolLibrary(poolCherryPickWell.getLibrary());
      for (SilencingReagent silencingReagent : poolCherryPickWell.getSilencingReagents()) {
        for (Well candidateCherryPickSourceWell : silencingReagent.getWells()) {
          if (candidateCherryPickSourceWell.getLibrary().getLibraryName().equals(duplexLibraryName)) {
            duplexWells.add(candidateCherryPickSourceWell);
          }
        }
      }
    }
    return duplexWells;
  }


  // private methods
  
  /**
   * @motivation for hibernate
   */
  private RNAiCherryPickRequest()
  {
  }
  
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
}

