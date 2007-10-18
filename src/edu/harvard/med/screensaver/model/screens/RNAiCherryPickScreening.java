// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/screens/RNAiCherryPickScreening.java $
// $Id: RNAiCherryPickScreening.java 1723 2007-08-20 20:26:50Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

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
    return "RNAi Cherry Pick Screening";
  }

  /**
   * Get the RNAi cherry pick request.
   * @return the RNAi cherry pick request
   */
  @ManyToOne(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_rnai_cherry_pick_screening_to_rnai_cherry_pick_request")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
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
   * @param dateCreated the date created
   * @param dateOfActivity the date the screening took place
   * @param rnaiCherryPickRequest the RNAi cherry pick request
   */
  RNAiCherryPickScreening(
    Screen screen,
    ScreeningRoomUser performedBy,
    Date dateCreated,
    Date dateOfActivity,
    RNAiCherryPickRequest rnaiCherryPickRequest)
  {
    super(screen, performedBy, dateCreated, dateOfActivity);
    _rnaiCherryPickRequest = rnaiCherryPickRequest;
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