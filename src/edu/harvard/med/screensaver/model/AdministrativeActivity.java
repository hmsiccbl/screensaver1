// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.Set;
import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

/**
 * Represents an activity involving administrative decisions or changes to data.
 * Provides auditing capabilities to data modifications and tracks the person
 * who approved these modifications.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_administrative_activity_to_activity")
@org.hibernate.annotations.Proxy
public class AdministrativeActivity extends Activity
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(AdministrativeActivity.class);


  // private instance data

  private AdministrativeActivityType _type;
  private AdministratorUser _approvedBy;
  private LocalDate _dateApproved;


  // public instance methods

  @Override
  @Transient
  public String getActivityTypeName()
  {
    return getType().getValue();
  }
  
  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Column(name="administrativeActivityType", nullable=false, updatable=false)
  @Immutable
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.AdministrativeActivityType$UserType")
  public AdministrativeActivityType getType()
  {
    return _type;
  }

  /**
   * Get the administrator user that approved the activity.
   * @return the administrator user that approved the activity
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="approvedById")
  @org.hibernate.annotations.ForeignKey(name="fk_activity_to_administrator_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="activitiesApproved")
  public AdministratorUser getApprovedBy()
  {
    return _approvedBy;
  }

  /**
   * Set the administrator user that approved the activity.
   * @param approvedBy the new administrator user that approved the activity
   */
  public void setApprovedBy(AdministratorUser approvedBy)
  {
    if (! isHibernateCaller() && _approvedBy != null) {
      _approvedBy.getActivitiesApproved().remove(this);
    }
    _approvedBy = approvedBy;
    if (! isHibernateCaller() && _approvedBy != null) {
      _approvedBy.getActivitiesApproved().add(this);
    }
  }

  /**
   * Get the date the activity was approved.
   * @return the date the activity was approved
   */
  @Type(type="edu.harvard.med.screensaver.db.hibernate.LocalDateType")
  public LocalDate getDateApproved()
  {
    return _dateApproved;
  }

  /**
   * Set the date the activity was approved.
   * @param dateApproved the new date the activity was approved
   */
  public void setDateApproved(LocalDate dateApproved)
  {
    _dateApproved = dateApproved;
  }


  // protected constructors

  /**
   * Construct an initialized <code>AdministrativeActivity</code>.
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the activity took place
   */
  public AdministrativeActivity(ScreensaverUser performedBy,
                                LocalDate dateOfActivity,
                                AdministrativeActivityType type)
  {
    this(performedBy, dateOfActivity, null, null, type);
  }

  /**
   * Construct an initialized <code>AdministrativeActivity</code>.
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the activity took place
   * @param approvedBy the administrator use who approved the activity
   * @param dateApproved the date the activity was approved
   */
  public AdministrativeActivity(
    ScreensaverUser performedBy,
    LocalDate dateOfActivity,
    AdministratorUser approvedBy,
    LocalDate dateApproved,
    AdministrativeActivityType type)
  {
    super(performedBy, dateOfActivity);

    // we normally do not get involved with maintaining bi-directional relationships in the
    // constructors, because normally we have a @ContainedEntity(containingEntityClass) that
    // has factory methods to create the child entities and manage the relationships. but
    // AdministrativeActivities don't have any containing entities. you might argue that they
    // could be children of the performedBy, but this is problematic because we probably would
    // want the administrative activities to live beyond the scope of the administrator (i guess
    // administrators should be un-deletable), and also because they are bundled with other,
    // non-administrative activities in ScreensaverUser.activitiesPerformed. and those other
    // activities have a different parent entity (Screen). this is the only time i had to do
    // something like this so far, and maybe i should come back to it, but i think its perfectly
    // alright. -s
    performedBy.getActivitiesPerformed().add(this);
    if (approvedBy != null) {
      approvedBy.getActivitiesApproved().add(this);
    }

    _type = type;
    _approvedBy = approvedBy;
    _dateApproved = dateApproved;
  }

  /**
   * Construct an uninitialized <code>AdministrativeActivity</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AdministrativeActivity() {}

  private void setType(AdministrativeActivityType type)
  {
    _type = type;
  }
}
