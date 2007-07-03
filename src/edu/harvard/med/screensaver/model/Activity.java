// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.Calendar;
import java.util.Date;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing an activity.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="true"
 */
public abstract class Activity extends AbstractEntity implements Comparable
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Activity.class);
  private static final long serialVersionUID = 0L;

  // instance fields

  private Integer _activityId;
  private Integer _version;
  private ScreensaverUser _performedBy;
  private Date _dateCreated;
  private Date _dateOfActivity;
  private String _comments;

  
  // public constructor

  /**
   * Constructs an initialized <code>Activity</code> object.
   *
   * @param performedBy the user that performed the activity
   * @param dateCreated the date created
   * @param dateOfActivity the date the activity took place
   */
  public Activity(
    ScreensaverUser performedBy,
    Date dateCreated,
    Date dateOfActivity) throws DuplicateEntityException
  {
    if (performedBy == null) {
      throw new NullPointerException();
    }
    _performedBy = performedBy;
    setDateCreated(dateCreated);
    setDateOfActivity(dateOfActivity);
    if (!_performedBy.getHbnActivitiesPerformed().add(this)) {
      throw new DuplicateEntityException(_performedBy, this);
    }
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getActivityId();
  }

  /**
   * Get the id for the activity.
   *
   * @return the id for the activity
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="activity_id_seq"
   */
  public Integer getActivityId()
  {
    return _activityId;
  }
  
  @ImmutableProperty
  abstract public String getActivityTypeName();

  /**
   * Get the user that performed the activity.
   *
   * @return the user that performed the activity
   */
  @ToOneRelationship(nullable=false, inverseProperty="activitiesPerformed")
  public ScreensaverUser getPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Set the user that performed the activity.
   *
   * @param performedBy the new user that performed the activity
   */
  public void setPerformedBy(ScreensaverUser performedBy)
  {
    if (performedBy == null) {
      throw new NullPointerException();
    }
    _performedBy.getHbnActivitiesPerformed().remove(this);
    _performedBy = performedBy;
    _performedBy.getHbnActivitiesPerformed().add(this);
  }

  /**
   * Get the date the activity entity was created.
   *
   * @return the date the activity entity was created
   * @hibernate.property
   *   not-null="true" type="timestamp"
   */
  @ImmutableProperty
  public Date getDateCreated()
  {
    return _dateCreated;
  }

  /**
   * Get the date the activity was performed.
   *
   * @return the date the activity was performed
   * @hibernate.property
   *   not-null="true"
   */
  @ImmutableProperty
  public Date getDateOfActivity()
  {
    return _dateOfActivity;
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

  @Override
  protected Object getBusinessKey()
  {
    return _dateCreated;
  }

  public int compareTo(Object o)
  {
    Activity other = (Activity) o;
    return getDateOfActivity().compareTo(other.getDateOfActivity());
  }
  
  // protected constructor
  
  /**
   * Construct an uninitialized <code>Activity</code> object.
   *
   * @motivation for hibernate and CGLIB2 proxy
   */
  protected Activity() {}


  // private methods

  /**
   * Set the id for the activity.
   *
   * @param screeninRoomActivityId the new id for the activity
   * @motivation for hibernate
   */
  private void setActivityId(Integer screeninRoomActivityId) {
    _activityId = screeninRoomActivityId;
  }
  
  /**
   * Get the user that performed the activity.
   *
   * @return the user that performed the activity
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreensaverUser"
   *   column="performed_by_id"
   *   not-null="true"
   *   foreign-key="fk_activity_to_performed_by"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ScreensaverUser getHbnPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Set the user that performed the activity.
   *
   * @param performedBy the new user that performed the activity
   * @motivation for hibernate
   */
  private void setHbnPerformedBy(ScreensaverUser performedBy)
  {
    _performedBy = performedBy;
  }


  /**
   * Set the date the activity entity was created.
   *
   * @param dateCreated the new date the activity entity was created
   * @motivation for hibernate
   */
  private void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated, Calendar.SECOND);
  }


  /**
   * Set the date the activity was performed.
   *
   * @param dateCreated the new date the activity was performed.
   */
  private void setDateOfActivity(Date dateOfActivity)
  {
    _dateOfActivity = truncateDate(dateOfActivity);
  }

  /**
   * Get the version for the activity.
   *
   * @return the version for the activity
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the activity.
   *
   * @param version the new version for the 
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
}
