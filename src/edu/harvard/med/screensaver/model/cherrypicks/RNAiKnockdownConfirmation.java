// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a RNAi Knockdown Confirmation.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=ScreenerCherryPick.class)
public class RNAiKnockdownConfirmation extends AbstractEntity
{

  // private static data

  private static final Logger log = Logger.getLogger(RNAiKnockdownConfirmation.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private Integer _rnaiKnockdownConfirmationId;
  private Integer _version;
  private ScreenerCherryPick _screenerCherryPick;
  private Double _percentKnockdown;
  private MethodOfQuantification _methodOfQuantification;
  private String _timing;
  private String _cellLine;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getRNAiKnockdownConfirmationId();
  }

  /**
   * Get the id for the RNAi Knockdown Confirmation.
   * @return the id for the RNAi Knockdown Confirmation
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="rnai_knockdown_confirmation_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="rnai_knockdown_confirmation_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="rnai_knockdown_confirmation_id_seq")
  public Integer getRNAiKnockdownConfirmationId()
  {
    return _rnaiKnockdownConfirmationId;
  }

  /**
   * Get the screener cherry pick.
   * @return the screener cherry pick
   */
  @OneToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
            fetch=FetchType.LAZY)
  @JoinColumn(name="screenerCherryPickId", nullable=false, updatable=false, unique=true)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_rnai_knockdown_confirmation_to_screener_cherry_pick")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="rnaiKnockdownConfirmation")
  public ScreenerCherryPick getScreenerCherryPick()
  {
    return _screenerCherryPick;
  }

  /**
   * Get the percent knockdown.
   * @return the percent knockdown
   */
  @Column(nullable=false)
  public Double getPercentKnockdown()
  {
    return _percentKnockdown;
  }

  /**
   * Set the percent knockdown.
   * @param percentKnockdown the new percent knockdown
   */
  public void setPercentKnockdown(Double percentKnockdown)
  {
    _percentKnockdown = percentKnockdown;
  }

  /**
   * Get the method of quantification.
   * @return the method of quantification
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.cherrypicks.MethodOfQuantification$UserType")
  public MethodOfQuantification getMethodOfQuantification()
  {
    return _methodOfQuantification;
  }

  /**
   * Set the method of quantification.
   * @param methodOfQuantification the new method of quantification
   */
  public void setMethodOfQuantification(MethodOfQuantification methodOfQuantification)
  {
    _methodOfQuantification = methodOfQuantification;
  }

  /**
   * Get the timing.
   * @return the timing
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getTiming()
  {
    return _timing;
  }

  /**
   * Set the timing.
   * @param timing the new timing
   */
  public void setTiming(String timing)
  {
    _timing = timing;
  }

  /**
   * Get the cell line.
   * @return the cell line
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getCellLine()
  {
    return _cellLine;
  }

  /**
   * Set the cell line.
   * @param cellLine the new cell line
   */
  public void setCellLine(String cellLine)
  {
    _cellLine = cellLine;
  }


  // package constructor

  /**
   * Construct an initialized <code>RNAiKnockdownConfirmation</code>. Intended only for use with
   * {@link ScreenerCherryPick}.
   * @param screenerCherryPick the screener cherry pick
   * @param percentKnockdown the percent knockdown
   * @param methodOfQuantification the method of quantification
   * @param timing the timing
   * @param cellLine the cell line
   */
  RNAiKnockdownConfirmation(
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
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>RNAiKnockdownConfirmation</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected RNAiKnockdownConfirmation() {}


  // private instance methods

  /**
   * Set the id for the RNAi Knockdown Confirmation.
   * @param rNAiKnockdownConfirmationId the new id for the RNAi Knockdown Confirmation
   * @motivation for hibernate
   */
  private void setRNAiKnockdownConfirmationId(Integer rNAiKnockdownConfirmationId)
  {
    _rnaiKnockdownConfirmationId = rNAiKnockdownConfirmationId;
  }

  /**
   * Get the version for the RNAi Knockdown Confirmation.
   * @return the version for the RNAi Knockdown Confirmation
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the RNAi Knockdown Confirmation.
   * @param version the new version for the RNAi Knockdown Confirmation
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the screener cherry pick.
   * @param screenerCherryPick the new screener cherry pick
   * @motivation for hibernate
   */
  private void setScreenerCherryPick(ScreenerCherryPick screenerCherryPick)
  {
    _screenerCherryPick = screenerCherryPick;
  }
}
