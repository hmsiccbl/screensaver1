// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a billing information.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class BillingInformation extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(BillingInformation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _billingInformationId;
  private Integer _version;
  private Screen _screen;
  private Set<BillingItem> _billingItems = new HashSet<BillingItem>();
  private BillingInfoToBeRequested _billingInfoToBeRequested;
  private boolean _isBillingForSuppliesOnly;
  private LocalDate _billingInfoReturnDate;
  private String _amountToBeChargedForScreen;
  private String _facilitiesAndAdministrationCharge;
  private LocalDate _feeFormRequestedDate;
  private String _feeFormRequestedInitials;
  private boolean _isFeeFormOnFile;
  private LocalDate _dateCompleted5KCompounds;
  private LocalDate _dateFaxedToBillingDepartment;
  private LocalDate _dateCharged;
  private String _comments;


  // public constructor

  /**
   * Construct an initialized <code>BillingInformation</code>.
   * @param screen the screen
   * @param billingInfoToBeRequested is billing info to be requested
   */
  // TODO make package visible
  public BillingInformation(Screen screen, BillingInfoToBeRequested billingInfoToBeRequested)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _billingInfoToBeRequested = billingInfoToBeRequested;
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
    return getBillingInformationId();
  }

  /**
   * Get the id for the billing information.
   * @return the id for the billing information
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="billing_information_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="billing_information_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="billing_information_id_seq")
  public Integer getBillingInformationId()
  {
    return _billingInformationId;
  }

  /**
   * Get the screen.
   * @return the screen
   */
  @OneToOne(optional=false)
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_billing_information_to_screen")
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get an unmodifiable copy of the set of billing items.
   * @return the billing items
   */
  @OneToMany(
    mappedBy="billingInformation",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Set<BillingItem> getBillingItems()
  {
    return _billingItems;
  }

  /**
   * Create and return a new billing item for this billing information.
   * @param itemToBeCharged the item to be charged
   * @param amount the amount
   * @param dateFaxed the date faxed
   * @return the new billing item for this billing information
   */
  public BillingItem createBillingItem(String itemToBeCharged, String amount, LocalDate dateFaxed)
  {
    BillingItem billingItem = new BillingItem(this, itemToBeCharged, amount, dateFaxed);
    _billingItems.add(billingItem);
    return billingItem;
  }

  /**
   * Get the billing info to be requested.
   * @return the billing info to be requested
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.BillingInfoToBeRequested$UserType")
  public BillingInfoToBeRequested getBillingInfoToBeRequested()
  {
    return _billingInfoToBeRequested;
  }

  /**
   * Set the billing info to be requested.
   * @param billingInfoToBeRequested the new billing info to be requested
   */
  public void setBillingInfoToBeRequested(BillingInfoToBeRequested billingInfoToBeRequested)
  {
    _billingInfoToBeRequested = billingInfoToBeRequested;
  }

  /**
   * Get the billing for supplies only.
   * @return the billing for supplies only
   */
  @Column(nullable=false, name="isBillingForSuppliesOnly")
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
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
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
  @org.hibernate.annotations.Type(type="text")
  public String getAmountToBeChargedForScreen()
  {
    return _amountToBeChargedForScreen;
  }

  /**
   * Set the amount to be charged for screen.
   * @param amountToBeChargedForScreen the new amount to be charged for screen
   */
  public void setAmountToBeChargedForScreen(String amountToBeChargedForScreen)
  {
    _amountToBeChargedForScreen = amountToBeChargedForScreen;
  }

  /**
   * Get the facilities and administration charge.
   * @return the facilities and administration charge
   */
  @org.hibernate.annotations.Type(type="text")
  public String getFacilitiesAndAdministrationCharge()
  {
    return _facilitiesAndAdministrationCharge;
  }

  /**
   * Set the facilities and administration charge.
   * @param facilitiesAndAdministrationCharge the new facilities and administration charge
   */
  public void setFacilitiesAndAdministrationCharge(String facilitiesAndAdministrationCharge)
  {
    _facilitiesAndAdministrationCharge = facilitiesAndAdministrationCharge;
  }

  /**
   * Get the fee form requested date.
   * @return the fee form requested date
   */
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
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
  @Column(nullable=false)
  public boolean getIsFeeFormOnFile()
  {
    return _isFeeFormOnFile;
  }

  /**
   * Set the is fee form on file.
   * @param isFeeFormOnFile the new is fee form on file
   */
  public void setIsFeeFormOnFile(boolean isFeeFormOnFile)
  {
    _isFeeFormOnFile = isFeeFormOnFile;
  }

  /**
   * Get the date completed 5-10K compounds.
   * @return the date completed 5-10K compounds
   */
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
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
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
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
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
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
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>BillingInformation</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected BillingInformation() {}


  // private constructor and instance methods

  /**
   * Set the id for the billing information.
   * @param billingInformationId the new id for the billing information
   * @motivation for hibernate
   */
  private void setBillingInformationId(Integer billingInformationId)
  {
    _billingInformationId = billingInformationId;
  }

  /**
   * Get the version for the billing information.
   * @return the version for the billing information
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the billing information.
   * @param version the new version for the billing information
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the screen.
   * @param screen the new screen
   * @motivation for hibernate
   */
  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  /**
   * Set the set of billing items.
   * @param billingItems the new set of billing items
   * @motivation for hibernate
   */
  private void setBillingItems(Set<BillingItem> billingItems)
  {
    _billingItems = billingItems;
  }
}
