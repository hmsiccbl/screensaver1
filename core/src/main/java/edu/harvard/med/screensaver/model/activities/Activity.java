// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.activities;

import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

/**
 * Records the occurrence of an activity, which may be a {@link LabActivity} or {@link AdministrativeActivity}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@org.hibernate.annotations.Proxy
public abstract class Activity extends AuditedAbstractEntity<Integer> implements Comparable<Activity>
{
  private static final Logger log = Logger.getLogger(Activity.class);
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<Activity> performedBy = RelationshipPath.from(Activity.class).to("performedBy", Cardinality.TO_ONE);
  public static final PropertyPath<Activity> dateOfActivity = RelationshipPath.from(Activity.class).toProperty("dateOfActivity");

  // instance fields

  private Integer _version;
  private ScreensaverUser _performedBy;
  private LocalDate _dateOfActivity;
  private String _comments;


  // public instance methods

  @Transient
  public int compareTo(Activity o)
  {
    if (this.equals(o)) {
      return 0;
    }
    Activity other = (Activity) o;
    int result = getDateOfActivity().compareTo(other.getDateOfActivity());
    if (result != 0) {
      return result;
    }
    result = getDateCreated().compareTo(other.getDateCreated());
    if (result != 0) {
      return result;
    }
    return hashCode() > other.hashCode() ? 1 : -1;
  }

  /**
   * Get the id for the activity.
   * @return the id for the activity
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="activity_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="activity_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="activity_id_seq")
  public Integer getActivityId()
  {
    return getEntityId(); 
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="activityUpdateActivity", 
             joinColumns=@JoinColumn(name="activityId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  @Sort(type=SortType.NATURAL)           
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  @Transient
  abstract public String getActivityTypeName();
  
  /**
   * Get the user that performed the activity.
   * @return the user that performed the activity
   */
  @ManyToOne(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="performedById", nullable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_activity_to_screensaver_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="activitiesPerformed")
  public ScreensaverUser getPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Set the user that performed the activity.
   * @param performedBy the new user that performed the activity
   */
  public void setPerformedBy(ScreensaverUser performedBy)
  {
    if (isHibernateCaller()) {
      _performedBy = performedBy;
      return;
    }
    if (performedBy == null) {
      throw new NullPointerException();
    }
    if (performedBy.equals(_performedBy)) {
      return;
    }
    //_performedBy.getActivitiesPerformed().remove(this);
    _performedBy = performedBy;
    //_performedBy.getActivitiesPerformed().add(this);
  }

  /**
   * Get the date the activity was performed.
   * @return the date the activity was performed
   */
  @Column(nullable=false)
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateOfActivity()
  {
    return _dateOfActivity;
  }

  /**
   * Set the date the activity was performed.
   * @param dateOfActivity the new date the activity was performed.
   */
  public void setDateOfActivity(LocalDate dateOfActivity)
  {
    _dateOfActivity = dateOfActivity;
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


  // protected constructors

  /**
   * Construct an initialized <code>Activity</code>.
   * @param recordedBy the administrator using Screensaver when this activity was created
   * @param performedBy the user that performed the activity (not necessarily the user using Screensaver)
   * @param dateOfActivity the date the activity took place
   */
  protected Activity(AdministratorUser recordedBy, ScreensaverUser performedBy, LocalDate dateOfActivity)
  {
    super(recordedBy);
    if (performedBy == null) {
      throw new NullPointerException();
    }
    _performedBy = performedBy;
    setDateOfActivity(dateOfActivity);
  }

  /**
   * Construct an uninitialized <code>Activity</code>.
   * @motivation for hibernate and proxy and concrete subclasses constructors
   */
  protected Activity() {}


  // private instance methods

  /**
   * Set the id for the activity.
   * @param activityId the new id for the activity
   * @motivation for hibernate
   */
  private void setActivityId(Integer activityId)
  {
    setEntityId(activityId);
  }

  /**
   * Get the version for the activity.
   * @return the version for the activity
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the activity.
   * @param version the new version for the
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
