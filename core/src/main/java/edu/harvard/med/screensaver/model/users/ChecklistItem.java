// $HeadURL:http://forge.abcd.harvard.edu/svn/screensaver/trunk/src/edu/harvard/med/screensaver/model/users/ChecklistItem.java $
// $Id:ChecklistItem.java 2679 2008-08-05 13:34:13Z ant4 $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;


/**
 * ScreeningRoomUsers may have zero or more {@link ChecklistItemEvent ChecklistItemEvents}
 * for each ChecklistItem that is defined. A given ChecklistItem may be
 * "expirable", in which case multiple ChecklistItemEvents may be exist
 * for a user, where each pair activates and then expires the ChecklistItem.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={"checklistItemGroup", "orderStatistic"}) })
public class ChecklistItem extends AbstractEntity<Integer> implements Comparable<ChecklistItem>
{

  // private static fields

  private static final Logger log = Logger.getLogger(ChecklistItem.class);
  private static final long serialVersionUID = 0L;


  // private instance fields

  private Integer _version;
  private String _itemName;
  private boolean _isExpirable;
  private ChecklistItemGroup _group;
  private Integer _orderStatistic;


  // public constructor

  /**
   * Construct an initialized <code>ChecklistItem</code>.
   * @param itemName the item name
   * @param isExpirable whether this type of checklist item can be activated and
   *          then expired (repeatedly)
   * @param group the group it belongs to
   * @param orderStatistic the order of the item within its group, 1-based
   */
  public ChecklistItem(String itemName,
                       boolean isExpirable,
                       ChecklistItemGroup group,
                       Integer orderStatistic)
  {
    _itemName = itemName;
    _isExpirable = isExpirable;
    _group = group;
    _orderStatistic = orderStatistic;
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public int compareTo(ChecklistItem other)
  {
    int result = getChecklistItemGroup().compareTo(other.getChecklistItemGroup());
    if (result == 0) {
      result = getOrderStatistic().compareTo(other.getOrderStatistic());
    }
    return result;
  }

  /**
   * Get the id for the checklist item.
   *
   * @return the id for the checklist item
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(name = "checklist_item_id_seq", strategy = "sequence", parameters = { @Parameter(name = "sequence", value = "checklist_item_id_seq") })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checklist_item_id_seq")
  public Integer getChecklistItemId()
  {
    return getEntityId();
  }

  /**
   * Get the order statistic.
   *
   * @return the order statistic
   */
  @Column(nullable = false, updatable = false)
  public Integer getOrderStatistic()
  {
    return _orderStatistic;
  }

  /**
   * Get the item name.
   *
   * @return the item name
   */
  @Column(nullable = false, updatable = false, unique = true)
  @org.hibernate.annotations.Type(type = "text")
  public String getItemName()
  {
    return _itemName;
  }

  /**
   * Get whether this checklist item can be activated and then
   * expired (repeatedly)
   */
  @Column(nullable = false, updatable = false, name = "isExpirable")
  public boolean isExpirable()
  {
    return _isExpirable;
  }

  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.users.ChecklistItemGroup$UserType")
  public ChecklistItemGroup getChecklistItemGroup()
  {
    return _group;
  }

  // protected constructor

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected ChecklistItem()
  {}


  // private constructor and instance methods

  /**
   * @motivation for hibernate
   */
  private void setChecklistItemId(Integer checklistItemId)
  {
    setEntityId(checklistItemId);
  }

  /**
   * @motivation for hibernate
   */
  @Version
  @Column(nullable = false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * @motivation for hibernate
   */
  private void setOrderStatistic(Integer orderStatistic)
  {
    _orderStatistic = orderStatistic;
  }

  /**
   * @motivation for hibernate
   */
  private void setItemName(String itemName)
  {
    _itemName = itemName;
  }

  /**
   * @motivation for hibernate
   */
  private void setExpirable(boolean isExpirable)
  {
    _isExpirable = isExpirable;
  }

  private void setChecklistItemGroup(ChecklistItemGroup group)
  {
    _group = group;
  }
}
