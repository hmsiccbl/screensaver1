// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.ToOneRelationship;


/**
 * A Hibernate entity bean representing a billing information.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
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
  private IsFeeToBeChargedForScreening _isFeeToBeChargedForScreening;
  private String _amountToBeChargedForScreen;
  private Date _feeFormRequestedDate;
  private String _feeFormRequestedInitials;
  private boolean _isFeeFormOnFile;
  private Date _dateCompleted5KCompounds;
  private Date _dateFaxedToBillingDepartment;
  private Date _dateCharged;
  private String _comments;


  // public constructor

  /**
   * Constructs an initialized <code>BillingInformation</code> object.
   *
   * @param screen the screen
   * @param isFeeToBeChargedForScreening is fee to be charged for screening
   * @param isFeeFormOnFile the is fee form on file
   */
  public BillingInformation(
    Screen screen,
    IsFeeToBeChargedForScreening isFeeToBeChargedForScreening,
    boolean isFeeFormOnFile)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _isFeeToBeChargedForScreening = isFeeToBeChargedForScreening;
    _isFeeFormOnFile = isFeeFormOnFile;
    _screen.setBillingInformation(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getBillingInformationId();
  }

  /**
   * Get the id for the billing information.
   *
   * @return the id for the billing information
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="billing_information_id_seq"
   */
  public Integer getBillingInformationId()
  {
    return _billingInformationId;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_billing_information_to_screen"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false)
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Get an unmodifiable copy of the set of billing items.
   *
   * @return the billing items
   */
  public Set<BillingItem> getBillingItems()
  {
    return Collections.unmodifiableSet(_billingItems);
  }

  /**
   * Add the billing item.
   *
   * @param billingItem the billing item to add
   * @return true iff the billing information did not already have the billing item
   */
  public boolean addBillingItem(BillingItem billingItem)
  {
    if (getHbnBillingItems().add(billingItem)) {
      billingItem.setHbnBillingInformation(this);
      return true;
    }
    return false;
  }

  /**
   * Get the is fee to be charged for screening.
   *
   * @return the is fee to be charged for screening
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.IsFeeToBeChargedForScreening$UserType"
   *   not-null="true"
   */
  public IsFeeToBeChargedForScreening getIsFeeToBeChargedForScreening()
  {
    return _isFeeToBeChargedForScreening;
  }

  /**
   * Set the is fee to be charged for screening.
   *
   * @param isFeeToBeChargedForScreening the new is fee to be charged for screening
   */
  public void setIsFeeToBeChargedForScreening(IsFeeToBeChargedForScreening isFeeToBeChargedForScreening)
  {
    _isFeeToBeChargedForScreening = isFeeToBeChargedForScreening;
  }

  /**
   * Get the amount to be charged for screen.
   *
   * @return the amount to be charged for screen
   * @hibernate.property
   *   type="text"
   */
  public String getAmountToBeChargedForScreen()
  {
    return _amountToBeChargedForScreen;
  }

  /**
   * Set the amount to be charged for screen.
   *
   * @param amountToBeChargedForScreen the new amount to be charged for screen
   */
  public void setAmountToBeChargedForScreen(String amountToBeChargedForScreen)
  {
    _amountToBeChargedForScreen = amountToBeChargedForScreen;
  }

  /**
   * Get the fee form requested date.
   *
   * @return the fee form requested date
   * @hibernate.property
   */
  public Date getFeeFormRequestedDate()
  {
    return _feeFormRequestedDate;
  }

  /**
   * Set the fee form requested date.
   *
   * @param feeFormRequestedDate the new fee form requested date
   */
  public void setFeeFormRequestedDate(Date feeFormRequestedDate)
  {
    _feeFormRequestedDate = truncateDate(feeFormRequestedDate);
  }

  /**
   * Get the fee form requested initials.
   *
   * @return the fee form requested initials
   * @hibernate.property
   *   type="text"
   */
  public String getFeeFormRequestedInitials()
  {
    return _feeFormRequestedInitials;
  }

  /**
   * Set the fee form requested initials.
   *
   * @param feeFormRequestedInitials the new fee form requested initials
   */
  public void setFeeFormRequestedInitials(String feeFormRequestedInitials)
  {
    _feeFormRequestedInitials = feeFormRequestedInitials;
  }

  /**
   * Get the is fee form on file.
   *
   * @return the is fee form on file
   * @hibernate.property
   */
  public boolean getIsFeeFormOnFile()
  {
    return _isFeeFormOnFile;
  }

  /**
   * Set the is fee form on file.
   *
   * @param isFeeFormOnFile the new is fee form on file
   */
  public void setIsFeeFormOnFile(boolean isFeeFormOnFile)
  {
    _isFeeFormOnFile = isFeeFormOnFile;
  }

  /**
   * Get the date completed 5-10K compounds.
   *
   * @return the date completed 5-10K compounds
   * @hibernate.property
   */
  public Date getDateCompleted5KCompounds()
  {
    return _dateCompleted5KCompounds;
  }

  /**
   * Set the date completed 5-10K compounds.
   *
   * @param dateCompleted5KCompounds the new date completed 5-10K compounds
   */
  public void setDateCompleted5KCompounds(Date dateCompleted5KCompounds)
  {
    _dateCompleted5KCompounds = truncateDate(dateCompleted5KCompounds);
  }

  /**
   * Get the date faxed to billing department.
   *
   * @return the date faxed to billing department
   * @hibernate.property
   */
  public Date getDateFaxedToBillingDepartment()
  {
    return _dateFaxedToBillingDepartment;
  }

  /**
   * Set the date faxed to billing department.
   *
   * @param dateFaxedToBillingDepartment the new date faxed to billing department
   */
  public void setDateFaxedToBillingDepartment(Date dateFaxedToBillingDepartment)
  {
    _dateFaxedToBillingDepartment = truncateDate(dateFaxedToBillingDepartment);
  }

  /**
   * Get the date charged.
   *
   * @return the date charged
   * @hibernate.property
   */
  public Date getDateCharged()
  {
    return _dateCharged;
  }

  /**
   * Set the date charged.
   *
   * @param dateCharged the new date charged
   */
  public void setDateCharged(Date dateCharged)
  {
    _dateCharged = truncateDate(dateCharged);
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return _screen;
  }


  // package methods

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
   * Get the billing items.
   *
   * @return the billing items
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="billing_information_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.BillingItem"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<BillingItem> getHbnBillingItems()
  {
    return _billingItems;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>BillingInformation</code> object.
   *
   * @motivation for hibernate
   */
  private BillingInformation() {}


  // private methods

  /**
   * Set the id for the billing information.
   *
   * @param billingInformationId the new id for the billing information
   * @motivation for hibernate
   */
  private void setBillingInformationId(Integer billingInformationId) {
    _billingInformationId = billingInformationId;
  }

  /**
   * Get the version for the billing information.
   *
   * @return the version for the billing information
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the billing information.
   *
   * @param version the new version for the billing information
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the billing items.
   *
   * @param billingItems the new billing items
   * @motivation for hibernate
   */
  private void setHbnBillingItems(Set<BillingItem> billingItems)
  {
    _billingItems = billingItems;
  }
}
