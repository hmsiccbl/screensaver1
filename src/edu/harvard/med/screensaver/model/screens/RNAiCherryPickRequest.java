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

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.PlateType;
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
  private static final Set<Character> REQUIRED_EMPTY_ROWS = 
    new HashSet<Character>(Arrays.asList(Well.MIN_WELL_ROW,
                                         new Character((char) (Well.MIN_WELL_ROW + 1)),
                                         new Character((char) (Well.MAX_WELL_ROW - 1)),
                                         Well.MAX_WELL_ROW));

  /* Currently (2007-04-20), all RNAi cherry pick assay plates use EPPENDORF plate types. */
  public static final PlateType RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE = PlateType.EPPENDORF;
  private static final int CHERRY_PICK_SILENCING_AGENT_ALLOWANCE = 500 * 4;


  // instance data members

  private String _assayProtocol;
  private RNAiCherryPickScreening _rnaiCherryPickScreening;


  public RNAiCherryPickRequest(Screen screen,
                               ScreeningRoomUser requestedBy,
                               Date dateRequested)
  {
    super(screen, requestedBy, dateRequested);
  }
  
  public RNAiCherryPickRequest(Screen screen,
                               ScreeningRoomUser requestedBy,
                               Date dateRequested,
                               Integer legacyCherryPickRequestNumber)
  {
    super(screen, requestedBy, dateRequested, legacyCherryPickRequestNumber);
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
    // TODO: is assayProtocol needed here any more? i think the only assay protocol for cherry
    // pick requests would end up in the RNAiCherryPickScreening
    return _assayProtocol;
  }
  
  @DerivedEntityProperty
  public PlateType getAssayPlateType()
  {
    return RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE;
  }

  @DerivedEntityProperty
  public int getCherryPickAllowance()
  {
    return CHERRY_PICK_SILENCING_AGENT_ALLOWANCE;
  }
  
  @DerivedEntityProperty
  public int getCherryPickAllowanceUsed()
  {
    int silencingAgentsUsed = 0;
    for (ScreenerCherryPick screenerCherryPick : getScreenerCherryPicks()) {
      silencingAgentsUsed += screenerCherryPick.getScreenedWell().getSilencingReagents().size();
    }
    return silencingAgentsUsed;
  }
  
  /**
   * Get the set of RNAi cherry pick assay
   *
   * @return the RNAi cherry pick assay
   * @hibernate.one-to-one
   *   class="edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening"
   *   property-ref="rnaiCherryPickRequest"
   *   cascade="all"
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
  @ImmutableProperty
  public Set<Integer> getRequiredEmptyColumnsOnAssayPlate()
  {
    return REQUIRED_EMPTY_COLUMNS;
  }
  
  @Override
  @ImmutableProperty
  public Set<Character> getRequiredEmptyRowsOnAssayPlate()
  {
    return REQUIRED_EMPTY_ROWS;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  // private methods
  
  /**
   * @motivation for hibernate
   */
  private RNAiCherryPickRequest()
  {
  }
}

