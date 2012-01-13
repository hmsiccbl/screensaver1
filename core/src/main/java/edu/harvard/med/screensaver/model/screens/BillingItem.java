// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


@Embeddable
public class BillingItem
{

  // static fields

  private static final Logger log = Logger.getLogger(BillingItem.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private String _itemToBeCharged;
  private BigDecimal _amount;
  private LocalDate _dateSentForBilling;


  // public constructor

  /**
   * Get the item to be charged.
   * @return the item to be charged
   */
  @Column(nullable=false)
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
  @Column(nullable=false, precision=9, scale=2)
  public BigDecimal getAmount()
  {
    return _amount;
  }

  /**
   * Set the amount.
   * @param amount the new amount
   */
  public void setAmount(BigDecimal amount)
  {
    _amount = amount;
  }

  /**
   * Get the date sent for billing.
   * @return the date sent for billing
   */
  @Column(nullable=true)
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateSentForBilling()
  {
    return _dateSentForBilling;
  }

  /**
   * Set the date sent for billing.
   * @param value the new date sent for billing
   */
  public void setDateSentForBilling(LocalDate value)
  {
    _dateSentForBilling = value;
  }


  // package constructor

  /**
   * Construct an initialized <code>BillingItem</code>. Intended for use by {@link
   * Screen#createBillingItem} only.
   */
  BillingItem(
    String itemToBeCharged,
    BigDecimal amount,
    LocalDate dateSentForBilling)
  {
    _itemToBeCharged = itemToBeCharged;
    _amount = amount;
    _dateSentForBilling = dateSentForBilling;
  }

  /**
   * Construct an uninitialized <code>BillingItem</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   * @motivation DTO for user interface
   */
  public BillingItem() {}

  @Override
  public boolean equals(Object other)
  {
    if (this == other) {
      return true;
    }
    if (other instanceof BillingItem) {
      return _amount.equals(((BillingItem) other)._amount) &&
      _dateSentForBilling.equals(((BillingItem) other)._dateSentForBilling) &&
      _itemToBeCharged.equals(((BillingItem) other)._itemToBeCharged);
    }
    return false; 
  }
  
  @Override
  public int hashCode()
  {
    return (_itemToBeCharged + _amount + _dateSentForBilling).hashCode(); 
  }
}
