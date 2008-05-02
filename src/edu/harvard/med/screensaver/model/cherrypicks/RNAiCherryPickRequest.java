// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 * A hibernate entity representing an RNAi cherry pick request.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="cherryPickRequestId")
@org.hibernate.annotations.ForeignKey(name="fk_rnai_cherry_pick_request_to_cherry_pick_request")
@org.hibernate.annotations.Proxy
public class RNAiCherryPickRequest extends CherryPickRequest
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(RNAiCherryPickRequest.class);
  /* Currently (2007-04-20), all RNAi cherry pick assay plates use EPPENDORF plate types. */
  public static final PlateType RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE = PlateType.EPPENDORF;
  private static final int CHERRY_PICK_SILENCING_AGENT_ALLOWANCE = 500 * 4;
  private static final BigDecimal DEFAULT_MICROLITER_TRANSFER_VOLUME = null;


  // private instance datum

  private String _assayProtocol;
  private Set<RNAiCherryPickScreening> _rnaiCherryPickScreenings = new HashSet<RNAiCherryPickScreening>();


  // public constructor

  /**
   * Construct an initialized <code>RNAiCherryPickRequest</code>. Intended only for use
   * by {@link Screen#createCherryPickRequest(ScreeningRoomUser, Date, Integer)}.
   * @param screen the screen
   * @param requestedBy the user that made the request
   * @param dateRequested the date created
   * @param legacyId the legacy id from ScreenDB
   * @motivation for creating CherryPickRequests from legacy ScreenDB cherry pick visits
   */
  public RNAiCherryPickRequest(
    Screen screen,
    ScreeningRoomUser requestedBy,
    Date dateRequested,
    Integer legacyCherryPickRequestNumber)
  {
    super(screen, requestedBy, dateRequested, legacyCherryPickRequestNumber);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public PlateType getDefaultAssayPlateType()
  {
    return RNAI_CHERRY_PICK_ASSAY_PLATE_TYPE;
  }
  
  @Override
  @Transient
  public BigDecimal getDefaultMicroliterTransferVolume()
  {
    return DEFAULT_MICROLITER_TRANSFER_VOLUME;
  }


  /**
   * Get the RNAiCherryPickScreenings for this RNAiCherryPickRequest.
   * <p>
   * Note: This is a non-cascading relationship; RNAiCherryPickScreenings are
   * cascaded from {@link Screen#getLabActivities()} instead.
   */
  @OneToMany(mappedBy="rnaiCherryPickRequest",
             cascade={},
             fetch=FetchType.LAZY)
  @edu.harvard.med.screensaver.model.annotations.OneToMany(singularPropertyName="rnaiCherryPickScreening")
  public Set<RNAiCherryPickScreening> getRNAiCherryPickScreenings()
  {
    return _rnaiCherryPickScreenings;
  }
  
  /**
   * Get if this RNAiCherryPickRequest has been screened (has at least one
   * associated RNAiCherryPickScreening activity).
   */
  @Transient
  public boolean isScreened()
  {
    return _rnaiCherryPickScreenings.size() > 0;
  }

  @Override
  @Transient
  public int getCherryPickAllowance()
  {
    return CHERRY_PICK_SILENCING_AGENT_ALLOWANCE;
  }

  @Override
  @Transient
  public int getCherryPickAllowanceUsed()
  {
    int silencingAgentsUsed = 0;
    for (ScreenerCherryPick screenerCherryPick : getScreenerCherryPicks()) {
      silencingAgentsUsed += screenerCherryPick.getScreenedWell().getSilencingReagents().size();
    }
    return silencingAgentsUsed;
  }

  /**
   * Get the assay protocol.
   * @return the assay protocol
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAssayProtocol()
  {
    // TODO: is assayProtocol needed here any more? i think the only assay protocol for cherry
    // pick requests would end up in the RNAiCherryPickScreening
    return _assayProtocol;
  }

  /**
   * Set the assay protocol.
   * @param assayProtocol the new assay protocol
   */
  public void setAssayProtocol(String assayProtocol)
  {
    _assayProtocol = assayProtocol;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>RNAiCherryPickRequest</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected RNAiCherryPickRequest() {}
  

  // private methods
  
  private void setRNAiCherryPickScreenings(Set<RNAiCherryPickScreening> rnaiCherryPickScreenings)
  {
    _rnaiCherryPickScreenings = rnaiCherryPickScreenings;
  }
}

