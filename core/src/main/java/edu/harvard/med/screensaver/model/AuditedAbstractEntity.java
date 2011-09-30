// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import edu.harvard.med.screensaver.model.activities.Activity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

/**
 * Parent class of entity types that require auditing of their creation and
 * update events. Creation information (who, when) is stored directly as
 * properties in the class. Update information (who, when, what) is stored as a
 * set of associated AdministrativeActivity objects of type
 * {@link AdministrativeActivityType#ENTITY_UPDATE}. (Note that
 * AdministrativeActivity is itself an {@link AuditedAbstractEntity}, but entity
 * update activities should not themselves every be updated, so we don't have a
 * recursion problem!)
 * <p/>
 * Every subclass will store its entity update activities in it own database
 * table. To make this work within Hibernate, the subclass must override the
 * {@link #getUpdateActivities()} method for the sole purpose of defining the
 * Hibernate mapping annotations there. See existing subclasses for examples of
 * the requisite annotations.
 * <p/>
 * Subclass constructors <i>must</i> call
 * {@link #AuditedAbstractEntity(AdministratorUser)} to ensure that a creation
 * timestamp is recorded (the {@link #AuditedAbstractEntity()} constructor is
 * for Hibernate's use only).
 */
@MappedSuperclass
public abstract class AuditedAbstractEntity<K extends Serializable> extends AbstractEntity<K>
{
  private static final long serialVersionUID = 1L;

  public static final RelationshipPath<AuditedAbstractEntity> createdBy = RelationshipPath.from(AuditedAbstractEntity.class).to("createdBy", Cardinality.TO_ONE);
  public static final RelationshipPath<AuditedAbstractEntity> updateActivities = RelationshipPath.from(AuditedAbstractEntity.class).to("updateActivities");
  
  protected SortedSet<AdministrativeActivity> _updateActivities = Sets.newTreeSet();
  
  private ScreensaverUser _createdBy;
  private DateTime _createdTimestamp;
  private DateTime _loadedTimestamp;
  private DateTime _publiclyAvailableTimestamp;
  
  /**
   * @motivation for Hibernate ONLY
   */
  protected AuditedAbstractEntity()
  {
  }
  
  protected AuditedAbstractEntity(AdministratorUser createdBy)
  {
    _createdBy = createdBy;
    _createdTimestamp = new DateTime(); 
  }

  
  //@Immutable
  @Column(nullable = false, updatable = false)
  @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
  public DateTime getDateCreated()
  {
    return _createdTimestamp;
  }
  
  private void setDateCreated(DateTime createdTimeStamp)
  {
    _createdTimestamp = createdTimeStamp;
  }  
  
  /**
   * The date that data is loaded into the database (used by LINCS)
   */
  @Column(nullable = true, updatable = false)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true) /* this property is currently set by LINCS scripts only */
  @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
  public DateTime getDateLoaded()
  {
    return _loadedTimestamp;
  }
  
  private void setDateLoaded(DateTime value)
  {
    _loadedTimestamp = value;
  }

  /**
   * The date that data has been made publicly available (used by LINCS)
   */
  @Column(nullable = true, updatable = false)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true) /* this property is currently set by LINCS scripts only */
  @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
  public DateTime getDatePubliclyAvailable()
  {
    return _publiclyAvailableTimestamp;
  }
  
  private void setDatePubliclyAvailable(DateTime value)
  {
    _publiclyAvailableTimestamp = value;
  }

  @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="createdById", updatable=false)
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true, hasNonconventionalSetterMethod=true /* nullable, immutable, to-one relationships not supported by domain model testing framework */)
  public ScreensaverUser getCreatedBy()
  {
    return _createdBy;
  }
  
  private void setCreatedBy(ScreensaverUser createdBy)
  {
    _createdBy = createdBy;
  }

  /**
   * To make update persistent, override in subclasses and add necessary Hibernate annotations on the overriding method.
   */
  @Transient
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }
  
  @Transient
  public SortedSet<AdministrativeActivity> getUpdateActivitiesOfType(AdministrativeActivityType activityType)
  {
    return Sets.newTreeSet(Sets.filter(_updateActivities,
                                       AdministrativeActivity.IsOfType(activityType)));
  }

  /**
   * @param activityType
   * @return the most recently performed {@link AdministrativeActivity} of the specified type; if no activity of the
   *         specified type exists, returns a {@link AdministrativeActivity} with all properties set to
   *         </code>null</code>
   */
  @Transient
  public AdministrativeActivity getLastUpdateActivityOfType(AdministrativeActivityType activityType)
  {
    SortedSet<AdministrativeActivity> activities = getUpdateActivitiesOfType(activityType);
    return activities.isEmpty() ? AdministrativeActivity.Null : activities.last();
  }
  
  private static Ordering<Activity> ActivityRecordedOrdering = Ordering.from(new Comparator<Activity>() {
    @Override
    public int compare(Activity a1, Activity a2)
    {
      return a1.getDateCreated().compareTo(a2.getDateCreated());
    }
  });

  @Transient
  public AdministrativeActivity getLastRecordedUpdateActivityOfType(AdministrativeActivityType activityType)
  {
    SortedSet<AdministrativeActivity> activities = getUpdateActivitiesOfType(activityType);
    return activities.isEmpty() ? AdministrativeActivity.Null : ActivityRecordedOrdering.max(activities);
  }

  private void setUpdateActivities(SortedSet<AdministrativeActivity> updateActivities)
  {
    _updateActivities = updateActivities;
  }
  
  public AdministrativeActivity createUpdateActivity(AdministratorUser recordedBy, String comments) {
    return createUpdateActivity(AdministrativeActivityType.ENTITY_UPDATE, recordedBy, comments);
  }
  
  public AdministrativeActivity createUpdateActivity(AdministrativeActivityType activityType,
                                                     AdministratorUser recordedBy,
                                                     String comments)
  {
    AdministrativeActivity updateActivity = 
      new AdministrativeActivity(recordedBy, 
                                 new DateTime().toLocalDate(), 
                                 activityType);
    updateActivity.setComments(comments);
    _updateActivities.add(updateActivity);
    return updateActivity;
  }

  public AdministrativeActivity createComment(AdministratorUser recordedBy,
                                              String comment)
  {
    return createUpdateActivity(AdministrativeActivityType.COMMENT,
                                recordedBy,
                                comment);
  }

  //  public DateTime getDateLastUpdated()
//  {
//    return null;
//  }
//  
//  public ScreensaverUser getLastUpdatedBy()
//  {
//    return null;
//  }
}
