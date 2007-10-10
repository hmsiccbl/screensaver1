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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a status item.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={
  "screenId",
  "statusDate",
  "statusValue"
}) })
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
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
    return getStatusItemId();
  }

  /**
   * Get the id for the status item.
   * @return the id for the status item
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="status_item_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="status_item_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="status_item_id_seq")
  public Integer getStatusItemId()
  {
    return _statusItemId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_status_item_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get the status date.
   * @return the status date
   */
  @Column(nullable=false)
  public Date getStatusDate()
  {
    return _statusDate;
  }

  /**
   * Set the status date.
   * @param statusDate the new status date
   */
  public void setStatusDate(Date statusDate)
  {
    _statusDate = truncateDate(statusDate);
  }

  /**
   * Get the status value.
   * @return the status value
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.StatusValue$UserType")
  public StatusValue getStatusValue()
  {
    return _statusValue;
  }

  /**
   * Set the status value.
   * @param statusValue the new status value
   */
  public void setStatusValue(StatusValue statusValue)
  {
    _statusValue = statusValue;
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


  // package constructor

  /**
   * Construct an initialized <code>StatusItem</code>. Intended only for use by {@link
   * Screen#createStatusItem}.
   *
   * @param screen the screen
   * @param statusDate the status date
   * @param statusValue the status value
   */
  StatusItem(Screen screen, Date statusDate, StatusValue statusValue)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _statusDate = truncateDate(statusDate);
    _statusValue = statusValue;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>StatusItem</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected StatusItem() {}


  // private instance methods

  /**
   * Set the screen.
   * @param screen the new screen
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the id for the status item.
   * @param statusItemId the new id for the status item
   * @motivation for hibernate
   */
  private void setStatusItemId(Integer statusItemId)
  {
    _statusItemId = statusItemId;
  }

  /**
   * Get the version for the status item.
   * @return the version for the status item
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the status item.
   * @param version the new version for the status item
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
