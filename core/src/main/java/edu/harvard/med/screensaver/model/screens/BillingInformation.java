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
public class BillingInformation
{

  // static fields

  private static final Logger log = Logger.getLogger(BillingInformation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  //private Set<BillingItem> _billingItems = new HashSet<BillingItem>();
  private boolean _toBeRequested;
  private boolean _seeComments;
  private boolean _isBillingForSuppliesOnly;
  private boolean _isFeeFormOnFile;
  private String _feeFormRequestedInitials;
  private BigDecimal _amountToBeChargedForScreen;
  private BigDecimal _facilitiesAndAdministrationCharge;
  private LocalDate _feeFormRequestedDate;
  private LocalDate _billingInfoReturnDate;
  private LocalDate _dateCompleted5KCompounds;
  private LocalDate _dateFaxedToBillingDepartment;
  private LocalDate _dateCharged;
  private String _billingComments;


  // public constructor

  /**
   * Construct an initialized <code>BillingInformation</code>.
   * @param toBeRequested is billing info to be requested
   */
  // TODO make package visible
  public BillingInformation(Screen screen, boolean toBeRequested)
  {
    _toBeRequested = toBeRequested;
  }


  // public methods

//  /**
//   * Get the set of billing items.
//   * @return the billing items
//   */
//  @org.hibernate.annotations.CollectionOfElements
//  @JoinTable(name = "screen_billing_item",
//             joinColumns = @JoinColumn(name = "screen_id"))
//  public Set<BillingItem> getBillingItems()
//  {
//    return _billingItems;
//  }

//  /**
//   * Create and return a new billing item for this billing information.
//   * @param itemToBeCharged the item to be charged
//   * @param amount the amount
//   * @param dateFaxed the date faxed
//   * @return the new billing item for this billing information
//   */
//  public BillingItem createBillingItem(String itemToBeCharged, BigDecimal amount, LocalDate dateFaxed)
//  {
//    BillingItem billingItem = new BillingItem(itemToBeCharged, amount, dateFaxed);
//    _billingItems.add(billingItem);
//    return billingItem;
//  }
//
//  public BillingItem createBillingItem(BillingItem dtoBillingItem)
//  {
//    return createBillingItem(dtoBillingItem.getItemToBeCharged(),
//                             dtoBillingItem.getAmount(),
//                             dtoBillingItem.getDateFaxed());
//  }

  @Column(nullable=false)
  public boolean isToBeRequested()
  {
    return _toBeRequested;
  }

  public void setToBeRequested(boolean billingInfoToBeRequested)
  {
    _toBeRequested = billingInfoToBeRequested;
  }

  @Column(nullable=false)
  public boolean isSeeComments()
  {
    return _seeComments;
  }

  public void setSeeComments(boolean seeComments)
  {
    _seeComments = seeComments;
  }

  /**
   * Get the billing for supplies only.
   * @return the billing for supplies only
   */
  @Column(name="isBillingForSuppliesOnly", nullable=false)
  public boolean isBillingForSuppliesOnly()
  {
    return _isBillingForSuppliesOnly;
  }

  /**
   * Set the billing for supplies only.
   * @param isBillingForSuppliesOnly the new billing for supplies only
   */
  public void setBillingForSuppliesOnly(boolean isBillingForSuppliesOnly)
  {
    _isBillingForSuppliesOnly = isBillingForSuppliesOnly;
  }

  /**
   * Get the billing info return date.
   * @return the billing info return date
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getBillingInfoReturnDate()
  {
    return _billingInfoReturnDate;
  }

  /**
   * Set the billing info return date.
   * @param billingInfoReturnDate the new billing info return date
   */
  public void setBillingInfoReturnDate(LocalDate billingInfoReturnDate)
  {
    _billingInfoReturnDate = billingInfoReturnDate;
  }

  /**
   * Get the amount to be charged for screen.
   * @return the amount to be charged for screen
   */
  @Column(precision=9, scale=2)
  @org.hibernate.annotations.Type(type="big_decimal")
  public BigDecimal getAmountToBeChargedForScreen()
  {
    return _amountToBeChargedForScreen;
  }

  /**
   * Set the amount to be charged for screen.
   * @param amountToBeChargedForScreen the new amount to be charged for screen
   */
  public void setAmountToBeChargedForScreen(BigDecimal amountToBeChargedForScreen)
  {
    _amountToBeChargedForScreen = amountToBeChargedForScreen;
  }

  /**
   * Get the facilities and administration charge.
   * @return the facilities and administration charge
   */
  @Column(precision=9, scale=2)
  @org.hibernate.annotations.Type(type="big_decimal")
  public BigDecimal getFacilitiesAndAdministrationCharge()
  {
    return _facilitiesAndAdministrationCharge;
  }

  /**
   * Set the facilities and administration charge.
   * @param facilitiesAndAdministrationCharge the new facilities and administration charge
   */
  public void setFacilitiesAndAdministrationCharge(BigDecimal facilitiesAndAdministrationCharge)
  {
    _facilitiesAndAdministrationCharge = facilitiesAndAdministrationCharge;
  }

  /**
   * Get the fee form requested date.
   * @return the fee form requested date
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getFeeFormRequestedDate()
  {
    return _feeFormRequestedDate;
  }

  /**
   * Set the fee form requested date.
   * @param feeFormRequestedDate the new fee form requested date
   */
  public void setFeeFormRequestedDate(LocalDate feeFormRequestedDate)
  {
    _feeFormRequestedDate = feeFormRequestedDate;
  }

  /**
   * Get the fee form requested initials.
   * @return the fee form requested initials
   */
  @Column
  @org.hibernate.annotations.Type(type="text")
  public String getFeeFormRequestedInitials()
  {
    return _feeFormRequestedInitials;
  }

  /**
   * Set the fee form requested initials.
   * @param feeFormRequestedInitials the new fee form requested initials
   */
  public void setFeeFormRequestedInitials(String feeFormRequestedInitials)
  {
    _feeFormRequestedInitials = feeFormRequestedInitials;
  }

  /**
   * Get the is fee form on file.
   * @return the is fee form on file
   */
  @Column(name="isFeeFormOnFile", nullable=false)
  public boolean isFeeFormOnFile()
  {
    return _isFeeFormOnFile;
  }

  /**
   * Set the is fee form on file.
   * @param isFeeFormOnFile the new is fee form on file
   */
  public void setFeeFormOnFile(boolean isFeeFormOnFile)
  {
    _isFeeFormOnFile = isFeeFormOnFile;
  }

  /**
   * Get the date completed 5-10K compounds.
   * @return the date completed 5-10K compounds
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateCompleted5KCompounds()
  {
    return _dateCompleted5KCompounds;
  }

  /**
   * Set the date completed 5-10K compounds.
   * @param dateCompleted5KCompounds the new date completed 5-10K compounds
   */
  public void setDateCompleted5KCompounds(LocalDate dateCompleted5KCompounds)
  {
    _dateCompleted5KCompounds = dateCompleted5KCompounds;
  }

  /**
   * Get the date faxed to billing department.
   * @return the date faxed to billing department
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateFaxedToBillingDepartment()
  {
    return _dateFaxedToBillingDepartment;
  }

  /**
   * Set the date faxed to billing department.
   * @param dateFaxedToBillingDepartment the new date faxed to billing department
   */
  public void setDateFaxedToBillingDepartment(LocalDate dateFaxedToBillingDepartment)
  {
    _dateFaxedToBillingDepartment = dateFaxedToBillingDepartment;
  }

  /**
   * Get the date charged.
   * @return the date charged
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateCharged()
  {
    return _dateCharged;
  }

  /**
   * Set the date charged.
   * @param dateCharged the new date charged
   */
  public void setDateCharged(LocalDate dateCharged)
  {
    _dateCharged = dateCharged;
  }

  /**
   * Get the comments.
   * @return the comments
   */
  @Column
  @org.hibernate.annotations.Type(type="text")
  public String getBillingComments()
  {
    return _billingComments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setBillingComments(String comments)
  {
    _billingComments = comments;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>BillingInformation</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected BillingInformation() {}


  // private constructor and instance methods

//  /**
//   * Set the set of billing items.
//   * @param billingItems the new set of billing items
//   * @motivation for hibernate
//   */
//  private void setBillingItems(Set<BillingItem> billingItems)
//  {
//    _billingItems = billingItems;
//  }
}
