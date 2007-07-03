// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 * A Hibernate entity bean representing a library screening performed on set of
 * RNAi cherry picks, as requested by a screener after an initial RNAi screening
 * has been performed.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="rnai_cherry_pick_screening" lazy="true"
 * @hibernate.joined-subclass-key column="activity_id"
 */
public class RNAiCherryPickScreening extends Screening
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(RNAiCherryPickScreening.class);


  // instance data members
  
  private RNAiCherryPickRequest _rnaiCherryPickRequest;


  // public constructors and methods

  public RNAiCherryPickScreening(Screen screen,
                                 ScreeningRoomUser performedBy,
                                 Date dateCreated,
                                 Date dateOfActivity,
                                 RNAiCherryPickRequest rnaiCherryPickRequest) 
    throws DuplicateEntityException
  {
    super(screen, performedBy, dateCreated, dateOfActivity);
    _rnaiCherryPickRequest = rnaiCherryPickRequest;
    _rnaiCherryPickRequest.setRnaiCherryPickScreening(this);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  @Override
  @ImmutableProperty
  public String getActivityTypeName()
  {
    return "RNAi Cherry Pick Screening";
  }

  /**
   * Get the RNAi cherry pick request.
   *
   * @return the RNAi cherry pick request
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest"
   *   column="cherry_pick_request_id"
   *   not-null="true"
   *   foreign-key="fk_rnai_cherry_pick_screening_to_rnai_cherry_pick_request"
   *   cascade="none"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false, inverseProperty="rnaiCherryPickScreening")
  public RNAiCherryPickRequest getRnaiCherryPickRequest()
  {
    return _rnaiCherryPickRequest;
  }
  
  /**
   * The original assay protocol, as provided by the screener in the cherry pick
   * request. The assay protocol actually used to perform the screening can
   * potentially be modified, and is available from {@link #getAssayProtocol()}.
   * 
   * @return a String of the requested assay protocol
   */
  @DerivedEntityProperty
  public String getRequestedAssayProtocol()
  {
    return _rnaiCherryPickRequest.getAssayProtocol();
  }


  // protected methods
  
  protected RNAiCherryPickScreening() {}

  
  // private methods

  private void setRnaiCherryPickRequest(RNAiCherryPickRequest rnaiCherryPickRequest)
  {
    _rnaiCherryPickRequest = rnaiCherryPickRequest;
  }
}