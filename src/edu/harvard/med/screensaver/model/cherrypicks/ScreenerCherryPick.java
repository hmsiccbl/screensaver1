// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.IsHitConfirmedViaExperimentation;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;


/**
 * Represents a screener-selected set of wells from a {@link Screen} that are to
 * be screened again for validation purposes. ScreenerCherryPicks are managed by
 * a {@link CherryPickRequest}.
 *
 * @see LabCherryPick
 * @see CherryPickRequest
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "cherryPickRequestId", "screenedWellId" }) })
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=CherryPickRequest.class)
public class ScreenerCherryPick extends AbstractEntity
{

  // private static data

  private static final Logger log = Logger.getLogger(ScreenerCherryPick.class);
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<ScreenerCherryPick> cherryPickRequest = new RelationshipPath<ScreenerCherryPick>(ScreenerCherryPick.class, "cherryPickRequest");
  public static final RelationshipPath<ScreenerCherryPick> screenedWell = new RelationshipPath<ScreenerCherryPick>(ScreenerCherryPick.class, "screenedWell");
  public static final RelationshipPath<ScreenerCherryPick> labCherryPicks = new RelationshipPath<ScreenerCherryPick>(ScreenerCherryPick.class, "labCherryPicks");


  // private instance data

  private Integer _screenerCherryPickId;
  private Integer _version;
  private CherryPickRequest _cherryPickRequest;
  private Well _screenedWell;
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  /* follow-up data from screener, after cherry pick screening is completed */
  private RNAiKnockdownConfirmation _rnaiKnockdownConfirmation;
  private IsHitConfirmedViaExperimentation _isHitConfirmedViaExperimentation;
  private String _notesOnHitConfirmation;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public boolean equals(Object obj)
  {
    return obj == this || (obj instanceof ScreenerCherryPick && obj.hashCode() == hashCode());
  }

  @Override
  public int hashCode()
  {
    return _cherryPickRequest.hashCode() * 7 + _screenedWell.hashCode() * 17;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + _cherryPickRequest.toString() + ":" + _screenedWell.toString() + ")";
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getScreenerCherryPickId();
  }

  /**
   * Get the id for the screener cherry pick.
   * @return the id for the screener cherry pick
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="screener_cherry_pick_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="screener_cherry_pick_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="screener_cherry_pick_id_seq")
  public Integer getScreenerCherryPickId()
  {
    return _screenerCherryPickId;
  }

  /**
   * Get the cherry pick request.
   * @return the cherry pick request
   */
  @ManyToOne
  @JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_screener_cherry_pick_to_cherry_pick_request")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * Get the screened library well for this cherry pick. The screened well
   * corresponds to a well that took part in the screen that generated this
   * cherry pick.  Screened wells are specified by the screener.
   * @return the screened well
   * @see LabCherryPick#getSourceWell()
   */
  @ManyToOne
  @JoinColumn(name="screenedWellId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_screener_cherry_pick_to_screened_well")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true)
  public Well getScreenedWell()
  {
    return _screenedWell;
  }

  /**
   * Get the set of lab cherry picks associated with this screener cherry pick.
   * @return the set of lab cherry picks associated with this screener cherry pick
   */
  @OneToMany(
    mappedBy="screenerCherryPick",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
  }

  /**
   * Get the RNAi knockdown confirmation.
   * @return the RNAi knockdown confirmation
   */
  @OneToOne(
    mappedBy="screenerCherryPick",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public RNAiKnockdownConfirmation getRnaiKnockdownConfirmation()
  {
    return _rnaiKnockdownConfirmation;
  }

  /**
   * Create and return a new RNAi knockdown confirmation for the screener cherry pick.
   * @param percentKnockdown the percent knockdown
   * @param methodOfQuantification the method of quantification
   * @param timing the timing
   * @param cellLine the cell line
   * @return the new RNAi knockdown confirmation
   */
  public RNAiKnockdownConfirmation createRNAiKnockdownConfirmation(
    Double percentKnockdown,
    MethodOfQuantification methodOfQuantification,
    String timing,
    String cellLine)
  {
    RNAiKnockdownConfirmation rnaiKnockdownConfirmation = new RNAiKnockdownConfirmation(
      this,
      percentKnockdown,
      methodOfQuantification,
      timing,
      cellLine);
    _rnaiKnockdownConfirmation = rnaiKnockdownConfirmation;
    return rnaiKnockdownConfirmation;
  }

  /**
   * Get the is hit confirmed via experimentation.
   * @return the is hit confirmed via experimentation
   */
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.IsHitConfirmedViaExperimentation$UserType")
  public IsHitConfirmedViaExperimentation getIsHitConfirmedViaExperimentation()
  {
    return _isHitConfirmedViaExperimentation;
  }

  /**
   * Set the is hit confirmed via experimentation.
   * @param isHitConfirmedViaExperimentation the new is hit confirmed via experimentation
   */
  public void setIsHitConfirmedViaExperimentation(IsHitConfirmedViaExperimentation isHitConfirmedViaExperimentation)
  {
    _isHitConfirmedViaExperimentation = isHitConfirmedViaExperimentation;
  }

  /**
   * Get the notes on hit confirmation.
   * @return the notes on hit confirmation
   */
  @org.hibernate.annotations.Type(type="text")
  public String getNotesOnHitConfirmation()
  {
    return _notesOnHitConfirmation;
  }

  /**
   * Set the notes on hit confirmation.
   * @param notesOnHitConfirmation the new notes on hit confirmation
   */
  public void setNotesOnHitConfirmation(String notesOnHitConfirmation)
  {
    _notesOnHitConfirmation = notesOnHitConfirmation;
  }


  // package instance methods

  /**
   * Construct an initialized <code>ScreenerCherryPick</code>. Intended only for use by {@link CherryPickRequest}.
   * @param cherryPickRequest the cherry pick request
   * @param screenedWell the screened well
   */
  ScreenerCherryPick(CherryPickRequest cherryPickRequest, Well screenedWell)
  {
    if (cherryPickRequest == null || screenedWell == null) {
      throw new NullPointerException();
    }
    // TODO: verify well was actually one that was screened
    _cherryPickRequest = cherryPickRequest;
    _screenedWell = screenedWell;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>ScreenerCherryPick</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ScreenerCherryPick() {}


  // private instance methods

  /**
   * Set the id for the screener cherry pick.
   * @param screenerCherryPickId the new id for the screener cherry pick
   * @motivation for hibernate
   */
  private void setScreenerCherryPickId(Integer screenerCherryPickId)
  {
    _screenerCherryPickId = screenerCherryPickId;
  }

  /**
   * Get the version for the screener cherry pick.
   * @return the version for the screener cherry pick
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the screener cherry pick.
   * @param version the new version for the screener cherry pick
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the cherry pick request.
   * @param cherryPickRequest the new cherry pick request
   * @motivation for hibernate
   */
  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }

  /**
   * Set the screened well.
   * @param screenedWell the new screened well
   * @motivation for hibernate
   */
  private void setScreenedWell(Well screenedWell)
  {
    _screenedWell = screenedWell;
  }

  /**
   * Set the set of lab cherry picks associated with this screener cherry pick.
   * @param labCherryPicks the new set of lab cherry picks
   * @motivation for hibernate
   */
  private void setLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }

  /**
   * Set the RNAi knockdown confirmation.
   * @param rnaiKnockdownConfirmation the new RNAi knockdown confirmation
   * @motivation for hibernate
   */
  private void setRnaiKnockdownConfirmation(RNAiKnockdownConfirmation rnaiKnockdownConfirmation)
  {
    _rnaiKnockdownConfirmation = rnaiKnockdownConfirmation;
  }

  /**
   * Create and return a new lab cherry pick for the cherry pick request.
   * @param cherryPickRequest TODO
   * @param sourceWell the source well
   * @return the new lab cherry pick
   * @throws DataModelViolationException whenever the cherry pick request for the provided
   * screener cherry pick does not match the cherry pick request asked to create the lab cherry
   * pick
   */
  public LabCherryPick createLabCherryPick(Well sourceWell)
  {
    LabCherryPick labCherryPick = new LabCherryPick(this, sourceWell);
    _cherryPickRequest.addLabCherryPick(labCherryPick);
    getLabCherryPicks().add(labCherryPick);
    return labCherryPick;
  }
}
