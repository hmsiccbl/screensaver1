// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.derivatives;


import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a derivative screen result.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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


  // public constructor

  /**
   * Constructs an initialized <code>DerivativeScreenResult</code> object.
   *
   * @param derivative the derivative
   * @param activityLevel the activity level
   * @param activityType the activity type
   */
  public DerivativeScreenResult(
    Derivative derivative,
    String activityLevel,
    String activityType)
  {
    _derivative = derivative;
    _activityLevel = activityLevel;
    _activityType = activityType;
    _derivative.getHbnDerivativeScreenResults().add(this);
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  @Override
  public Integer getEntityId()
  {
    return getDerivativeScreenResultId();
  }

  /**
   * Get the id for the derivative screen result.
   *
   * @return the id for the derivative screen result
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="derivative_screen_result_id_seq"
   */
  public Integer getDerivativeScreenResultId()
  {
    return _derivativeScreenResultId;
  }

  /**
   * Get the derivative.
   *
   * @return the derivative
   */
  public Derivative getDerivative()
  {
    return _derivative;
  }

  /**
   * Set the derivative.
   *
   * @param derivative the new derivative
   */
  public void setDerivative(Derivative derivative)
  {
    _derivative.getHbnDerivativeScreenResults().remove(this);
    _derivative = derivative;
    derivative.getHbnDerivativeScreenResults().add(this);
  }

  /**
   * Get the activity level.
   *
   * @return the activity level
   */
  public String getActivityLevel()
  {
    return _activityLevel;
  }

  /**
   * Set the activity level.
   *
   * @param activityLevel the new activity level
   */
  public void setActivityLevel(String activityLevel)
  {
    _activityLevel = activityLevel;
  }

  /**
   * Get the activity type.
   *
   * @return the activity type
   */
  public String getActivityType()
  {
    return _activityType;
  }

  /**
   * Set the activity type.
   *
   * @param activityType the new activity type
   */
  public void setActivityType(String activityType)
  {
    _activityType = activityType;
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {

  /**
   * Get the derivative.
   *
   * @return the derivative
   */
  public Derivative getDerivative()
  {
    return _derivative;
  }

  /**
   * Get the activity level.
   *
   * @return the activity level
   */
  public String getActivityLevel()
  {
    return _activityLevel;
  }

  /**
   * Get the activity type.
   *
   * @return the activity type
   */
  public String getActivityType()
  {
    return _activityType;
  }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getDerivative().equals(that.getDerivative()) &&
        getActivityLevel().equals(that.getActivityLevel()) &&
        getActivityType().equals(that.getActivityType());
    }

    @Override
    public int hashCode()
    {
      return
        getDerivative().hashCode() +
        getActivityLevel().hashCode() +
        getActivityType().hashCode();
    }

    @Override
    public String toString()
    {
      return getDerivative() + ":" + getActivityLevel() + ":" + getActivityType();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the derivative.
   * Throw a NullPointerException when the derivative is null.
   *
   * @param derivative the new derivative
   * @throws NullPointerException when the derivative is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnDerivative(Derivative derivative)
  {
    if (derivative == null) {
      throw new NullPointerException();
    }
    _derivative = derivative;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>DerivativeScreenResult</code> object.
   *
   * @motivation for hibernate
   */
  private DerivativeScreenResult() {}


  // private methods

  /**
   * Set the id for the derivative screen result.
   *
   * @param derivativeScreenResultId the new id for the derivative screen result
   * @motivation for hibernate
   */
  private void setDerivativeScreenResultId(Integer derivativeScreenResultId) {
    _derivativeScreenResultId = derivativeScreenResultId;
  }

  /**
   * Get the version for the derivative screen result.
   *
   * @return the version for the derivative screen result
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the derivative screen result.
   *
   * @param version the new version for the derivative screen result
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the derivative.
   *
   * @return the derivative
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.derivatives.Derivative"
   *   column="derivative_id"
   *   not-null="true"
   *   foreign-key="fk_derivative_screen_result_to_derivative"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Derivative getHbnDerivative()
  {
    return _derivative;
  }
  
  /**
   * Get the activity level.
   *
   * @return the activity level
   * @hibernate.property
   *   column="activity_level"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnActivityLevel()
  {
    return _activityLevel;
  }

  /**
   * Set the activity level.
   *
   * @param activityLevel the new activity level
   * @motivation for hibernate
   */
  private void setHbnActivityLevel(String activityLevel)
  {
    _activityLevel = activityLevel;
  }

  /**
   * Get the activity type.
   *
   * @return the activity type
   * @hibernate.property
   *   column="activity_type"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnActivityType()
  {
    return _activityType;
  }

  /**
   * Set the activity type.
   *
   * @param activityType the new activity type
   * @motivation for hibernate
   */
  private void setHbnActivityType(String activityType)
  {
    _activityType = activityType;
  }
}
