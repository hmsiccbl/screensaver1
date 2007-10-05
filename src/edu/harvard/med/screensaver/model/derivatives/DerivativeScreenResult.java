// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.derivatives;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a derivative screen result.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Derivative.class)
public class DerivativeScreenResult extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(DerivativeScreenResult.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _derivativeScreenResultId;
  private Integer _version;
  private Derivative _derivative;
  private String _activityLevel;
  private String _activityType;


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
    return getDerivativeScreenResultId();
  }

  /**
   * Get the id for the derivative screen result.
   * @return the id for the derivative screen result
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="derivative_screen_result_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="derivative_screen_result_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="derivative_screen_result_id_seq")
  public Integer getDerivativeScreenResultId()
  {
    return _derivativeScreenResultId;
  }

  /**
   * Get the derivative.
   * @return the derivative
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="derivativeId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_derivative_screen_result_to_derivative")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Derivative getDerivative()
  {
    return _derivative;
  }

  /**
   * Get the activity level.
   * @return the activity level
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getActivityLevel()
  {
    return _activityLevel;
  }

  /**
   * Set the activity level.
   * @param activityLevel the new activity level
   */
  public void setActivityLevel(String activityLevel)
  {
    _activityLevel = activityLevel;
  }

  /**
   * Get the activity type.
   * @return the activity type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getActivityType()
  {
    return _activityType;
  }

  /**
   * Set the activity type.
   * @param activityType the new activity type
   */
  public void setActivityType(String activityType)
  {
    _activityType = activityType;
  }


  // package constructor

  /**
   * Construct an initialized <code>DerivativeScreenResult</code>. Intended only for use by
   * {@link Derivative#createDerivativeScreenResult}.
   * @param derivative the derivative
   * @param activityLevel the activity level
   * @param activityType the activity type
   */
  DerivativeScreenResult(Derivative derivative, String activityLevel, String activityType)
  {
    if (derivative == null) {
      throw new NullPointerException();
    }
    _derivative = derivative;
    _activityLevel = activityLevel;
    _activityType = activityType;
  }


  // private constructor and instance methods

  /**
   * Construct an uninitialized <code>DerivativeScreenResult</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected DerivativeScreenResult() {}

  /**
   * Set the id for the derivative screen result.
   * @param derivativeScreenResultId the new id for the derivative screen result
   * @motivation for hibernate
   */
  private void setDerivativeScreenResultId(Integer derivativeScreenResultId)
  {
    _derivativeScreenResultId = derivativeScreenResultId;
  }

  /**
   * Get the version for the derivative screen result.
   * @return the version for the derivative screen result
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the derivative screen result.
   * @param version the new version for the derivative screen result
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the derivative.
   * @param derivative the new derivative
   * @motivation for hibernate
   */
  private void setDerivative(Derivative derivative)
  {
    _derivative = derivative;
  }
}
