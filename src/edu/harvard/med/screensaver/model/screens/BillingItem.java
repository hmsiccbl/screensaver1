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


/**
 * A Hibernate entity bean representing a billing item.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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
  private Date _dateFaxed;


  // public constructor

  /**
   * Constructs an initialized <code>BillingItem</code> object.
   *
   * @param billingInformation the billing information
   * @param itemToBeCharged the item to be charged
   * @param amount the amount
   * @param dateFaxed the date faxed
   */
  public BillingItem(
    BillingInformation billingInformation,
    String itemToBeCharged,
    String amount,
    Date dateFaxed)
  {
    // TODO: verify the order of assignments here is okay
    _billingInformation = billingInformation;
    _itemToBeCharged = itemToBeCharged;
    _amount = amount;
    _dateFaxed = truncateDate(dateFaxed);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getBillingItemId();
  }

  /**
   * Get the id for the billing item.
   *
   * @return the id for the billing item
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="billing_item_id_seq"
   */
  public Integer getBillingItemId()
  {
    return _billingItemId;
  }

  /**
   * Get the billing information.
   *
   * @return the billing information
   */
  public BillingInformation getBillingInformation()
  {
    return _billingInformation;
  }

  /**
   * Set the billing information.
   *
   * @param billingInformation the new billing information
   */
  public void setBillingInformation(BillingInformation billingInformation)
  {
    _billingInformation = billingInformation;
    billingInformation.getHbnBillingItems().add(this);
  }

  /**
   * Get the item to be charged.
   *
   * @return the item to be charged
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getItemToBeCharged()
  {
    return _itemToBeCharged;
  }

  /**
   * Set the item to be charged.
   *
   * @param itemToBeCharged the new item to be charged
   */
  public void setItemToBeCharged(String itemToBeCharged)
  {
    _itemToBeCharged = itemToBeCharged;
  }

  /**
   * Get the amount.
   *
   * @return the amount
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getAmount()
  {
    return _amount;
  }

  /**
   * Set the amount.
   *
   * @param amount the new amount
   */
  public void setAmount(String amount)
  {
    _amount = amount;
  }

  /**
   * Get the date faxed.
   *
   * @return the date faxed
   * @hibernate.property
   *   not-null="true"
   */
  public Date getDateFaxed()
  {
    return _dateFaxed;
  }

  /**
   * Set the date faxed.
   *
   * @param dateFaxed the new date faxed
   */
  public void setDateFaxed(Date dateFaxed)
  {
    _dateFaxed = truncateDate(dateFaxed);
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the billing information.
     *
     * @return the billing information
     */
    public BillingInformation getBillingInformation()
    {
      return _billingInformation;
    }
    
    /**
     * Get the item to be charged.
     *
     * @return the item to be charged
     */
    public String getItemToBeCharged()
    {
      return _itemToBeCharged;
    }
    
    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getBillingInformation().equals(that.getBillingInformation()) &&
        getItemToBeCharged().equals(that.getItemToBeCharged());
    }

    @Override
    public int hashCode()
    {
      return
        getBillingInformation().hashCode() +
        getItemToBeCharged().hashCode();
    }

    @Override
    public String toString()
    {
      return getBillingInformation() + ":" + getItemToBeCharged();
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
   * Set the billing information.
   * Throw a NullPointerException when the billing information is null.
   *
   * @param billingInformation the new billing information
   * @throws NullPointerException when the billing information is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnBillingInformation(BillingInformation billingInformation)
  {
    if (billingInformation == null) {
      throw new NullPointerException();
    }
    _billingInformation = billingInformation;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>BillingItem</code> object.
   *
   * @motivation for hibernate
   */
  private BillingItem() {}


  // private methods

  /**
   * Set the id for the billing item.
   *
   * @param billingItemId the new id for the billing item
   * @motivation for hibernate
   */
  private void setBillingItemId(Integer billingItemId) {
    _billingItemId = billingItemId;
  }

  /**
   * Get the version for the billing item.
   *
   * @return the version for the billing item
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the billing item.
   *
   * @param version the new version for the billing item
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the billing information.
   *
   * @return the billing information
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.BillingInformation"
   *   column="billing_information_id"
   *   not-null="true"
   *   foreign-key="fk_billing_item_to_billing_information"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private BillingInformation getHbnBillingInformation()
  {
    return _billingInformation;
  }
}
