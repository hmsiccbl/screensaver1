// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.ToOneRelationship;


/**
 * A Hibernate entity bean representing a RNAi Knockdown Confirmation.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="true"
 */
public class RNAiKnockdownConfirmation extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(RNAiKnockdownConfirmation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _rnaiKnockdownConfirmationId;
  private Integer _version;
  private ScreenerCherryPick _screenerCherryPick;
  private Double _percentKnockdown;
  private MethodOfQuantification _methodOfQuantification;
  private String _timing;
  private String _cellLine;


  // public constructor

  /**
   * Constructs an initialized <code>RNAiKnockdownConfirmation</code> object.
   *
   * @param screenerCherryPick the screener cherry pick
   * @param percentKnockdown the percent knockdown
   * @param methodOfQuantification the method of quantification
   * @param timing the timing
   * @param cellLine the cell line
   */
  public RNAiKnockdownConfirmation(
    ScreenerCherryPick screenerCherryPick,
    Double percentKnockdown,
    MethodOfQuantification methodOfQuantification,
    String timing,
    String cellLine)
  {
    if (screenerCherryPick == null) {
      throw new NullPointerException();
    }
    _screenerCherryPick = screenerCherryPick;
    _percentKnockdown = percentKnockdown;
    _methodOfQuantification = methodOfQuantification;
    _timing = timing;
    _cellLine = cellLine;
    _screenerCherryPick.setRNAiKnockdownConfirmation(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getRNAiKnockdownConfirmationId();
  }

  /**
   * Get the id for the RNAi Knockdown Confirmation.
   *
   * @return the id for the RNAi Knockdown Confirmation
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="rnai_knockdown_confirmation_id_seq"
   */
  public Integer getRNAiKnockdownConfirmationId()
  {
    return _rnaiKnockdownConfirmationId;
  }

  /**
   * Get the screener cherry pick.
   *
   * @return the screener cherry pick
   */
  @ToOneRelationship(nullable=false, inverseProperty="RNAiKnockdownConfirmation")
  public ScreenerCherryPick getScreenerCherryPick()
  {
    return _screenerCherryPick;
  }

  /**
   * Get the percent knockdown.
   *
   * @return the percent knockdown
   * @hibernate.property
   *   not-null="true"
   */
  public Double getPercentKnockdown()
  {
    return _percentKnockdown;
  }

  /**
   * Set the percent knockdown.
   *
   * @param percentKnockdown the new percent knockdown
   */
  public void setPercentKnockdown(Double percentKnockdown)
  {
    _percentKnockdown = percentKnockdown;
  }

  /**
   * Get the method of quantification.
   *
   * @return the method of quantification
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.MethodOfQuantification$UserType"
   *   not-null="true"
   */
  public MethodOfQuantification getMethodOfQuantification()
  {
    return _methodOfQuantification;
  }

  /**
   * Set the method of quantification.
   *
   * @param methodOfQuantification the new method of quantification
   */
  public void setMethodOfQuantification(MethodOfQuantification methodOfQuantification)
  {
    _methodOfQuantification = methodOfQuantification;
  }

  /**
   * Get the timing.
   *
   * @return the timing
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getTiming()
  {
    return _timing;
  }

  /**
   * Set the timing.
   *
   * @param timing the new timing
   */
  public void setTiming(String timing)
  {
    _timing = timing;
  }

  /**
   * Get the cell line.
   *
   * @return the cell line
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getCellLine()
  {
    return _cellLine;
  }

  /**
   * Set the cell line.
   *
   * @param cellLine the new cell line
   */
  public void setCellLine(String cellLine)
  {
    _cellLine = cellLine;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return getScreenerCherryPick();
  }


  // package methods

  /**
   * Set the screener cherry pick.
   * Throw a NullPointerException when the cherry pick is null.
   *
   * @param screenerCherryPick the new cherry pick
   * @throws NullPointerException when the cherry pick is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnScreenerCherryPick(ScreenerCherryPick screenerCherryPick)
  {
    if (screenerCherryPick == null) {
      throw new NullPointerException();
    }
    _screenerCherryPick = screenerCherryPick;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>RNAiKnockdownConfirmation</code> object.
   *
   * @motivation for hibernate and CGLIB2
   */
  protected RNAiKnockdownConfirmation() {}


  // private methods

  /**
   * Set the id for the RNAi Knockdown Confirmation.
   *
   * @param rNAiKnockdownConfirmationId the new id for the RNAi Knockdown Confirmation
   * @motivation for hibernate
   */
  private void setRNAiKnockdownConfirmationId(Integer rNAiKnockdownConfirmationId) {
    _rnaiKnockdownConfirmationId = rNAiKnockdownConfirmationId;
  }

  /**
   * Get the version for the RNAi Knockdown Confirmation.
   *
   * @return the version for the RNAi Knockdown Confirmation
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the RNAi Knockdown Confirmation.
   *
   * @param version the new version for the RNAi Knockdown Confirmation
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the cherry pick.
   *
   * @return the cherry pick
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.ScreenerCherryPick"
   *   column="screener_cherry_pick_id"
   *   not-null="true"
   *   foreign-key="fk_rnai_knockdown_confirmation_to_screener_cherry_pick"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ScreenerCherryPick getHbnScreenerCherryPick()
  {
    return _screenerCherryPick;
  }
}
