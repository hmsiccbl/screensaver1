// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * A Hibernate entity bean representing a checklist item type.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class ChecklistItemType extends AbstractEntity
{

  // private static fields

  private static final Logger log = Logger.getLogger(ChecklistItemType.class);
  private static final long serialVersionUID = 0L;


  // private instance fields

  private Integer _checklistItemTypeId;
  private Integer _version;
  private Integer _orderStatistic;
  private String _itemName;
  private boolean _hasDeactivation;


  // public constructor

  /**
   * Construct an initialized <code>ChecklistItemType</code>.
   * @param orderStatistic the order statistic
   * @param itemName the item name
   * @param hasDeactivation the has deactivation
   */
  public ChecklistItemType(
    Integer orderStatistic,
    String itemName,
    boolean hasDeactivation)
  {
    _orderStatistic = orderStatistic;
    _itemName = itemName;
    _hasDeactivation = hasDeactivation;
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getChecklistItemTypeId();
  }

  /**
   * Get the id for the checklist item type.
   * @return the id for the checklist item type
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="checklist_item_type_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="checklist_item_type_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="checklist_item_type_id_seq")
  public Integer getChecklistItemTypeId()
  {
    return _checklistItemTypeId;
  }

  /**
   * Get the order statistic.
   * @return the order statistic
   */
  @Column(nullable=false, unique=true)
  @org.hibernate.annotations.Immutable
  public Integer getOrderStatistic()
  {
    return _orderStatistic;
  }

  /**
   * Get the item name.
   * @return the item name
   */
  @Column(nullable=false, unique=true)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="text")
  public String getItemName()
  {
    return _itemName;
  }

  /**
   * Get the has deactivation.
   * @return the has deactivation
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Immutable
  public boolean getHasDeactivation()
  {
    return _hasDeactivation;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>ChecklistItemType</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ChecklistItemType() {}


  // private constructor and instance methods

  /**
   * Set the id for the checklist item type.
   * @param checklistItemTypeId the new id for the checklist item type
   * @motivation for hibernate
   */
  private void setChecklistItemTypeId(Integer checklistItemTypeId)
  {
    _checklistItemTypeId = checklistItemTypeId;
  }

  /**
   * Get the version for the checklist item type.
   * @return the version for the checklist item type
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the checklist item type.
   * @param version the new version for the checklist item type
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the order statistic.
   * @param orderStatistic the new order statistic
   * @motivation for hibernate
   */
  private void setOrderStatistic(Integer orderStatistic)
  {
    _orderStatistic = orderStatistic;
  }

  /**
   * Set the item name.
   * @param itemName the new item name
   * @motivation for hibernate
   */
  private void setItemName(String itemName)
  {
    _itemName = itemName;
  }

  /**
   * Set the has deactivation.
   * @param hasDeactivation the new has deactivation
   * @motivation for hibernate
   */
  private void setHasDeactivation(boolean hasDeactivation)
  {
    _hasDeactivation = hasDeactivation;
  }
}
