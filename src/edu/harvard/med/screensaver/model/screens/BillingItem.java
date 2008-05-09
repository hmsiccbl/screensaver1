// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a billing item.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=BillingInformation.class)
public class BillingItem extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(BillingItem.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _billingItemId;
  private Integer _version;
  private BillingInformation _billingInformation;
  private String _itemToBeCharged;
  private String _amount;
  private LocalDate _dateFaxed;


  // public constructor

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getBillingItemId();
  }

  /**
   * Get the id for the billing item.
   * @return the id for the billing item
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="billing_item_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="billing_item_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="billing_item_id_seq")
  public Integer getBillingItemId()
  {
    return _billingItemId;
  }

  /**
   * Get the billing information.
   * @return the billing information
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="billingInformationId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_billing_item_to_billing_information")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public BillingInformation getBillingInformation()
  {
    return _billingInformation;
  }

  /**
   * Get the item to be charged.
   * @return the item to be charged
   */
  @org.hibernate.annotations.Type(type="text")
  public String getItemToBeCharged()
  {
    return _itemToBeCharged;
  }

  /**
   * Set the item to be charged.
   * @param itemToBeCharged the new item to be charged
   */
  public void setItemToBeCharged(String itemToBeCharged)
  {
    _itemToBeCharged = itemToBeCharged;
  }

  /**
   * Get the amount.
   * @return the amount
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getAmount()
  {
    return _amount;
  }

  /**
   * Set the amount.
   * @param amount the new amount
   */
  public void setAmount(String amount)
  {
    _amount = amount;
  }

  /**
   * Get the date faxed.
   * @return the date faxed
   */
  @Column(nullable=false)
  @Type(type="org.joda.time.contrib.hibernate.PersistentLocalDate")
  public LocalDate getDateFaxed()
  {
    return _dateFaxed;
  }

  /**
   * Set the date faxed.
   * @param dateFaxed the new date faxed
   */
  public void setDateFaxed(LocalDate dateFaxed)
  {
    _dateFaxed = dateFaxed;
  }


  // package constructor

  /**
   * Construct an initialized <code>BillingItem</code>. Intended for use by {@link
   * BillingInformation#createBillingItem} only.
   * @param billingInformation the billing information
   * @param itemToBeCharged the item to be charged
   * @param amount the amount
   * @param dateFaxed the date faxed
   */
  BillingItem(
    BillingInformation billingInformation,
    String itemToBeCharged,
    String amount,
    LocalDate dateFaxed)
  {
    if (billingInformation == null) {
      throw new NullPointerException();
    }
    _billingInformation = billingInformation;
    _itemToBeCharged = itemToBeCharged;
    _amount = amount;
    _dateFaxed = dateFaxed;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>BillingItem</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected BillingItem() {}


  // private constructor and instance methods

  /**
   * Set the id for the billing item.
   * @param billingItemId the new id for the billing item
   * @motivation for hibernate
   */
  private void setBillingItemId(Integer billingItemId)
  {
    _billingItemId = billingItemId;
  }

  /**
   * Get the version for the billing item.
   * @return the version for the billing item
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the billing item.
   * @param version the new version for the billing item
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the billing information.
   * @param billingInformation the new billing information
   * @motivation for hibernate
   */
  private void setBillingInformation(BillingInformation billingInformation)
  {
    _billingInformation = billingInformation;
  }
}
