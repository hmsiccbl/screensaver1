// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ToOneRelationship;


/**
 * A Hibernate entity bean representing a status item.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class StatusItem extends AbstractEntity implements Comparable
{
  
  // static fields

  private static final Logger log = Logger.getLogger(StatusItem.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _statusItemId;
  private Integer _version;
  private Screen _screen;
  private Date _statusDate;
  private StatusValue _statusValue;


  // public constructor

  /**
   * Constructs an initialized <code>StatusItem</code> object.
   *
   * @param screen the screen
   * @param statusDate the status date
   * @param statusValue the status value
   * @throws DuplicateEntityException 
   */
  public StatusItem(
    Screen screen,
    Date statusDate,
    StatusValue statusValue) throws DuplicateEntityException
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _statusDate = truncateDate(statusDate);
    _statusValue = statusValue;
    if (!_screen.getStatusItems().add(this)) {
      throw new DuplicateEntityException(_screen, this);
    }
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
    return getStatusItemId();
  }

  /**
   * Get the id for the status item.
   *
   * @return the id for the status item
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="status_item_id_seq"
   */
  public Integer getStatusItemId()
  {
    return _statusItemId;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_status_item_to_screen"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the status date.
   *
   * @return the status date
   * @hibernate.property
   *   not-null="true"
   */
  public Date getStatusDate()
  {
    return _statusDate;
  }

  /**
   * Set the status date.
   *
   * @param statusDate the new status date
   */
  public void setStatusDate(Date statusDate)
  {
    _statusDate = truncateDate(statusDate);
  }

  /**
   * Get the status value.
   *
   * @return the status value
   */
  public StatusValue getStatusValue()
  {
    return _statusValue;
  }

  /**
   * Set the status value.
   *
   * @param statusValue the new status value
   */
  public void setStatusValue(StatusValue statusValue)
  {
    _screen.getStatusItems().remove(this);
    _statusValue = statusValue;
    _screen.getStatusItems().add(this);
  }
  

  // Comparable interface methods

  public int compareTo(Object o)
  {
    if (o == null) {
      return 1;
    }
    StatusItem other = (StatusItem) o;
    int result = _statusValue.compareTo(other._statusValue);
    if (result == 0) {
      return _statusDate.compareTo(other._statusDate);
    }
    return result;
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the screen.
     *
     * @return the screen
     */
    public Screen getScreen()
    {
      return _screen;
    }

    /**
     * Get the status date.
     *
     * @return the status date
     */
    public Date getStatusDate()
    {
      return _statusDate;
    }
    
    /**
     * Get the status value.
     *
     * @return the status value
     */
    public StatusValue getStatusValue()
    {
      return _statusValue;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getScreen().equals(that.getScreen()) &&
        getStatusDate().equals(that.getStatusDate()) &&
        getStatusValue().equals(that.getStatusValue());
        
    }

    @Override
    public int hashCode()
    {
      return
        getScreen().hashCode() +
        17 * getStatusDate().hashCode() +
        163 * getStatusValue().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreen() + ":" + getStatusDate() + ":" + getStatusValue();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // private constructor

  /**
   * Construct an uninitialized <code>StatusItem</code> object.
   *
   * @motivation for hibernate
   */
  private StatusItem() {}


  // private methods

  /**
   * Set the screen.
   *
   * @param screen the new screen
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the id for the status item.
   *
   * @param statusItemId the new id for the status item
   * @motivation for hibernate
   */
  private void setStatusItemId(Integer statusItemId) {
    _statusItemId = statusItemId;
  }

  /**
   * Get the version for the status item.
   *
   * @return the version for the status item
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the status item.
   *
   * @param version the new version for the status item
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the status value.
   *
   * @return the status value
   * @hibernate.property
   *   column="status_value"
   *   type="edu.harvard.med.screensaver.model.screens.StatusValue$UserType"
   *   not-null="true"
   * @motivation for hibernate
   */
  private StatusValue getHbnStatusValue()
  {
    return _statusValue;
  }

  /**
   * Set the status value.
   *
   * @param statusValue the new status value
   * @motivation for hibernate
   */
  private void setHbnStatusValue(StatusValue statusValue)
  {
    _statusValue = statusValue;
  }

}
