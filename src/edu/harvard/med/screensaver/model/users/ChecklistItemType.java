// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a checklist item type.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class ChecklistItemType extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(ChecklistItemType.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _checklistItemTypeId;
  private Integer _version;
  private Set<ChecklistItem> _checklistItems = new HashSet<ChecklistItem>();
  private Integer _orderStatistic;
  private String _itemName;
  private boolean _hasDeactivation;


  // public constructor

  /**
   * Constructs an initialized <code>ChecklistItemType</code> object.
   *
   * @param orderStatistic the order statistic
   * @param itemName the item name
   * @param hasDeactivation the has deactivation
   */
  public ChecklistItemType(
    Integer orderStatistic,
    String itemName,
    boolean hasDeactivation)
  {
    // TODO: verify the order of assignments here is okay
    _orderStatistic = orderStatistic;
    _itemName = itemName;
    _hasDeactivation = hasDeactivation;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getChecklistItemTypeId();
  }

  /**
   * Get the id for the checklist item type.
   *
   * @return the id for the checklist item type
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="checklist_item_type_id_seq"
   */
  public Integer getChecklistItemTypeId()
  {
    return _checklistItemTypeId;
  }

  /**
   * Get an unmodifiable copy of the set of checklist items.
   *
   * @return the checklist items
   */
  public Set<ChecklistItem> getChecklistItems()
  {
    return Collections.unmodifiableSet(_checklistItems);
  }

  /**
   * Add the checklist item.
   *
   * @param checklistItem the checklist item to add
   * @return true iff the checklist item type did not already have the checklist item
   */
  public boolean addChecklistItem(ChecklistItem checklistItem)
  {
    if (getHbnChecklistItems().add(checklistItem)) {
      checklistItem.setHbnChecklistItemType(this);
      return true;
    }
    return false;
  }

  /**
   * Get the order statistic.
   *
   * @return the order statistic
   * @hibernate.property
   *   not-null="true"
   *   unique="true"
   */
  public Integer getOrderStatistic()
  {
    return _orderStatistic;
  }

  /**
   * Set the order statistic.
   *
   * @param orderStatistic the new order statistic
   */
  public void setOrderStatistic(Integer orderStatistic)
  {
    _orderStatistic = orderStatistic;
  }

  /**
   * Get the item name.
   *
   * @return the item name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
  public String getItemName()
  {
    return _itemName;
  }

  /**
   * Set the item name.
   *
   * @param itemName the new item name
   */
  public void setItemName(String itemName)
  {
    _itemName = itemName;
  }

  /**
   * Get the has deactivation.
   *
   * @return the has deactivation
   * @hibernate.property
   *   not-null="true"
   */
  public boolean getHasDeactivation()
  {
    return _hasDeactivation;
  }

  /**
   * Set the has deactivation.
   *
   * @param hasDeactivation the new has deactivation
   */
  public void setHasDeactivation(boolean hasDeactivation)
  {
    _hasDeactivation = hasDeactivation;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return getOrderStatistic();
  }


  // package methods

  /**
   * Get the checklist items.
   *
   * @return the checklist items
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="checklist_item_type_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.users.ChecklistItem"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<ChecklistItem> getHbnChecklistItems()
  {
    return _checklistItems;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>ChecklistItemType</code> object.
   *
   * @motivation for hibernate
   */
  private ChecklistItemType() {}


  // private methods

  /**
   * Set the id for the checklist item type.
   *
   * @param checklistItemTypeId the new id for the checklist item type
   * @motivation for hibernate
   */
  private void setChecklistItemTypeId(Integer checklistItemTypeId) {
    _checklistItemTypeId = checklistItemTypeId;
  }

  /**
   * Get the version for the checklist item type.
   *
   * @return the version for the checklist item type
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the checklist item type.
   *
   * @param version the new version for the checklist item type
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the checklist items.
   *
   * @param checklistItems the new checklist items
   * @motivation for hibernate
   */
  private void setHbnChecklistItems(Set<ChecklistItem> checklistItems)
  {
    _checklistItems = checklistItems;
  }
}
