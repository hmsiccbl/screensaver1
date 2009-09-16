// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

/**
 * A Hibernate entity bean representing a library screening performed on set of
 * RNAi cherry picks, as requested by a screener after an initial RNAi screening
 * has been performed.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_rnai_cherry_pick_screening_to_activity")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class RNAiCherryPickScreening extends Screening
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(RNAiCherryPickScreening.class);

  public static final String ACTIVITY_TYPE_NAME = "RNAi Cherry Pick Screening";
  
  //public static final RelationshipPath<LabActivity> rnaiCherryPickRequest = new RelationshipPath<LabActivity>(LabActivity.class, "rnaiCherryPickRequest");
  public static final RelationshipPath<RNAiCherryPickScreening> rnaiCherryPickRequest = new RelationshipPath<RNAiCherryPickScreening>(RNAiCherryPickScreening.class, "rnaiCherryPickRequest");

  
  // private instance datum

  private RNAiCherryPickRequest _rnaiCherryPickRequest;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getActivityTypeName()
  {
    return ACTIVITY_TYPE_NAME;
  }

  /**
   * Get the RNAi cherry pick request.
   * @return the RNAi cherry pick request
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_rnai_cherry_pick_screening_to_rnai_cherry_pick_request")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="RNAiCherryPickScreenings")
  public RNAiCherryPickRequest getRnaiCherryPickRequest()
  {
    return _rnaiCherryPickRequest;
  }

  /**
   * The original assay protocol, as provided by the screener in the cherry pick
   * request. The assay protocol actually used to perform the screening can
   * potentially be modified, and is available from {@link #getAssayProtocol()}.
   * @return a String of the requested assay protocol
   */
  @Transient
  public String getRequestedAssayProtocol()
  {
    return _rnaiCherryPickRequest.getAssayProtocol();
  }


  // package constructor

  /**
   * Construct an initialized <code>RNAiCherryPickScreening</code>.
   * @param screen the screen
   * @param performedBy the user that performed the screening
   * @param dateOfActivity the date the screening took place
   * @param rnaiCherryPickRequest the RNAi cherry pick request
   */
  RNAiCherryPickScreening(
    Screen screen,
    ScreeningRoomUser performedBy,
    LocalDate dateOfActivity,
    RNAiCherryPickRequest rnaiCherryPickRequest)
  {
    super(screen, performedBy, dateOfActivity);
    _rnaiCherryPickRequest = rnaiCherryPickRequest;
    _rnaiCherryPickRequest.getRNAiCherryPickScreenings().add(this);
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>RNAiCherryPickScreening</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected RNAiCherryPickScreening() {}


  // private constructor and instance methods

  /**
   * Set the RNAi cherry pick request.
   * @param rnaiCherryPickRequest the new RNAi cherry pick request
   * @motivation for hibernate
   */
  private void setRnaiCherryPickRequest(RNAiCherryPickRequest rnaiCherryPickRequest)
  {
    _rnaiCherryPickRequest = rnaiCherryPickRequest;
  }
}