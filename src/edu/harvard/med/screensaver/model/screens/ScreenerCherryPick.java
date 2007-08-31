// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a cherry pick. See
 * {@link #CherryPickRequest} for explanation.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="true"
 */
public class ScreenerCherryPick extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(ScreenerCherryPick.class);
  private static final long serialVersionUID = 0L;

  // instance fields

  private Integer _screenerCherryPickId;
  private Integer _version;

  private CherryPickRequest _cherryPickRequest;
  private Well _screenedWell;
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();

  /* follow-up data from screener, after cherry pick screening is completed */
  private RNAiKnockdownConfirmation _rnaiKnockdownConfirmation;
  private IsHitConfirmedViaExperimentation _isHitConfirmedViaExperimentation;
  private String _notesOnHitConfirmation;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPick</code> object.
   *
   * @param cherryPickRequest the cherry pick request
   * @param well the well
   */
  public ScreenerCherryPick(CherryPickRequest cherryPickRequest,
                            Well screenedWell)
  {
    if (cherryPickRequest == null || screenedWell == null) {
        throw new NullPointerException();
    }

    // TODO: verify well was actually one that was screened

    _cherryPickRequest = cherryPickRequest;
    _screenedWell = screenedWell;
    boolean added = _cherryPickRequest.getScreenerCherryPicks().add(this);
    // TODO: remove relaxed constraint for CompoundCherryPickRequest after #79682 is fixed
    if (!(cherryPickRequest instanceof CompoundCherryPickRequest)) {
      if (!added) {
        throw new DuplicateEntityException(cherryPickRequest, this);
      }
    }
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public Integer getEntityId()
  {
    return getScreenerCherryPickId();
  }

  /**
   * Get the id for the screener cherry pick.
   *
   * @return the id for the screener cherry pick
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screener_cherry_pick_id_seq"
   */
  public Integer getScreenerCherryPickId()
  {
    return _screenerCherryPickId;
  }

  /**
   * Get the cherry pick request.
   *
   * @return the cherry pick request
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickRequest"
   *   column="cherry_pick_request_id"
   *   not-null="true"
   *   foreign-key="fk_screener_cherry_pick_to_cherry_pick_request"
   *   cascade="none"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false)
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * Get the screened library well for this cherry pick. The screened well
   * corresponds to a well that took part in the screen that generated this
   * cherry pick.  Screened wells are specified by the screener.
   *
   * @return the screened well
   * @see LabCherryPick#getSourceWell()
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.libraries.Well"
   *                        column="screened_well_id" not-null="true"
   *                        foreign-key="fk_screener_cherry_pick_to_screened_well"
   *                        cascade="none"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false)
  public Well getScreenedWell()
  {
    return _screenedWell;
  }

  /**
   * @return
   * @hibernate.set cascade="all-delete-orphan" lazy="true" inverse="true"
   * @hibernate.collection-key column="screener_cherry_pick_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screens.LabCherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(unidirectional=true)
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
  }

  private void setLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }

  /**
   * Get the RNAi knockdown confirmation.
   *
   * @return the RNAi knockdown confirmation
   * @hibernate.one-to-one
   *   class="edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation"
   *   property-ref="hbnScreenerCherryPick"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(inverseProperty="screenerCherryPick")
  public RNAiKnockdownConfirmation getRNAiKnockdownConfirmation()
  {
    return _rnaiKnockdownConfirmation;
  }

  /**
   * Get the is hit confirmed via experimentation.
   *
   * @return the is hit confirmed via experimentation
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.IsHitConfirmedViaExperimentation$UserType"
   */
  public IsHitConfirmedViaExperimentation getIsHitConfirmedViaExperimentation()
  {
    return _isHitConfirmedViaExperimentation;
  }

  /**
   * Set the is hit confirmed via experimentation.
   *
   * @param isHitConfirmedViaExperimentation the new is hit confirmed via experimentation
   */
  public void setIsHitConfirmedViaExperimentation(IsHitConfirmedViaExperimentation isHitConfirmedViaExperimentation)
  {
    _isHitConfirmedViaExperimentation = isHitConfirmedViaExperimentation;
  }

  /**
   * Get the notes on hit confirmation.
   *
   * @return the notes on hit confirmation
   * @hibernate.property
   *   type="text"
   */
  public String getNotesOnHitConfirmation()
  {
    return _notesOnHitConfirmation;
  }

  /**
   * Set the notes on hit confirmation.
   *
   * @param notesOnHitConfirmation the new notes on hit confirmation
   */
  public void setNotesOnHitConfirmation(String notesOnHitConfirmation)
  {
    _notesOnHitConfirmation = notesOnHitConfirmation;
  }


  /**
   * A business key class for the cherry pick
   */
  private class BusinessKey
  {

    /**
     * Get the cherry pick request.
     *
     * @return the cherry pick request
     */
    public CherryPickRequest getCherryPickRequest()
    {
      return _cherryPickRequest;
    }

    /**
     * Get the well.
     *
     * @return the well
     */
    public Well getWell()
    {
      return _screenedWell;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        this.getCherryPickRequest().equals(that.getCherryPickRequest()) &&
        this.getWell().equals(that.getWell());
    }

    @Override
    public int hashCode()
    {
      return
        this.getCherryPickRequest().hashCode() * 17 +
        this.getWell().hashCode() * 63;
    }

    @Override
    public String toString()
    {
      return this.getCherryPickRequest() + ":" + this.getWell();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the RNAi knockdown confirmation.
   *
   * @param rNAiKnockdownConfirmation the new RNAi knockdown confirmation
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setRNAiKnockdownConfirmation(RNAiKnockdownConfirmation rNAiKnockdownConfirmation)
  {
    _rnaiKnockdownConfirmation = rNAiKnockdownConfirmation;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>CherryPick</code> object.
   *
   * @motivation for hibernate and CGLIB2
   */
  protected ScreenerCherryPick() {}


  // private methods

  /**
   * Set the id for the screener cherry pick.
   *
   * @param screenerCherryPickId the new id for the screener cherry pick
   * @motivation for hibernate
   */
  private void setScreenerCherryPickId(Integer screenerCherryPickId) {
    _screenerCherryPickId = screenerCherryPickId;
  }

  /**
   * Get the version for the screener cherry pick.
   *
   * @return the version for the screener cherry pick
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the screener cherry pick.
   *
   * @param version the new version for the screener cherry pick
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the cherry pick request.
   *
   * @param cherryPickRequest the new cherry pick request
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }

  /**
   * Set the screened well.
   *
   * @param well the new well
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreenedWell(Well well)
  {
    _screenedWell = well;
  }
}
