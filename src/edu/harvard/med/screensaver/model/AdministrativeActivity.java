// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.Date;

import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;

/**
 * Represents an activity involving administrative decisions or changes to data.
 * Provides auditing capabilities to data modifications and tracks the person
 * who approved these modifications.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.joined-subclass table="administrative_activity" lazy="true"
 * @hibernate.joined-subclass-key column="activity_id"
 */
abstract public class AdministrativeActivity extends Activity
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(AdministrativeActivity.class);


  // instance data members
  
  private AdministratorUser _approvedBy;
  private Date _dateApproved;

  
  // public constructors and methods

  public AdministrativeActivity(ScreensaverUser performedBy,
                                Date datePerformed,
                                Date dateRecorded)
    throws DuplicateEntityException
  {
    super(performedBy, datePerformed, dateRecorded);
  }

  public AdministrativeActivity(ScreensaverUser performedBy,
                                Date datePerformed)
    throws DuplicateEntityException
  {
    super(performedBy, datePerformed, new Date());
  }

  public AdministrativeActivity(AdministratorUser approvedBy,
                                Date dateApproved,
                                ScreensaverUser performedBy,
                                Date datePerformed,
                                Date dateRecorded)
    throws DuplicateEntityException
  {
    super(approvedBy, dateRecorded, datePerformed);
  }

  public AdministrativeActivity(AdministratorUser approvedBy,
                                Date dateApproved,
                                ScreensaverUser performedBy,
                                Date datePerformed)
    throws DuplicateEntityException
  {
    super(performedBy, datePerformed, new Date());
    _approvedBy = approvedBy;
    _dateApproved = dateApproved;
  }

  /**
   * Get the administrator user that approved the activity.
   *
   * @return the administrator user that approved the activity
   */
  @ToOneRelationship(nullable=false, inverseProperty="activitiesApproved")
  @DerivedEntityProperty
  public AdministratorUser getApprovedBy()
  {
//    return (AdministratorUser) getPerformedBy();
    return _approvedBy;
  }

  public void setApprovedBy(AdministratorUser approvedBy)
  {
//    setPerformedBy(approvedBy);
    _approvedBy.getHbnActivitiesApproved().remove(this);
    _approvedBy = approvedBy;
    _approvedBy.getHbnActivitiesApproved().add(this);
  }

  @Override
  @DerivedEntityProperty
  public String getActivityTypeName()
  {
    return "Administrative";
  }
  
  // protected methods
  
  protected AdministrativeActivity() {}


  // private methods

  /**
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.AdministratorUser"
   *   column="approved_by_id"
   *   not-null="false"
   *   foreign-key="fk_activity_to_administrator_user"
   *   cascade="save-update"
   */
  private AdministratorUser getHbnApprovedBy()
  {
    return _approvedBy;
  }

  private void setHbnApprovedBy(AdministratorUser approvedBy)
  {
    _approvedBy = approvedBy;
  }
}
