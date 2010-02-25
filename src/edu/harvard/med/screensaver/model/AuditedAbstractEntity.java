// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;
import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;

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

  public static final RelationshipPath<AuditedAbstractEntity> dateCreated = new RelationshipPath<AuditedAbstractEntity>(AuditedAbstractEntity.class, "dateCreated");
  public static final RelationshipPath<AuditedAbstractEntity> createdBy = new RelationshipPath<AuditedAbstractEntity>(AuditedAbstractEntity.class, "createdBy");
  public static final RelationshipPath<AuditedAbstractEntity> updateActivities = new RelationshipPath<AuditedAbstractEntity>(AuditedAbstractEntity.class, "updateActivities");
  
  protected SortedSet<AdministrativeActivity> _updateActivities = Sets.newTreeSet();
  
  private ScreensaverUser _createdBy;
  private DateTime _createdTimestamp;
  
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

  
  @Immutable
  @Column(nullable=false)
  @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
  public DateTime getDateCreated()
  {
    return _createdTimestamp;
  }
  
  private void setDateCreated(DateTime createdTimeStamp)
  {
    _createdTimestamp = createdTimeStamp;
  }

  @ManyToOne(fetch=FetchType.LAZY, cascade={})
  @JoinColumn(name="createdById", updatable=false)
  @Immutable
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={})
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
   * @return
   */
  @Transient
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }
  
  private void setUpdateActivities(SortedSet<AdministrativeActivity> updateActivities)
  {
    _updateActivities = updateActivities;
  }
  
  public AdministrativeActivity createUpdateActivity(AdministratorUser recordedBy, String comments) {
    AdministrativeActivity updateActivity = 
      new AdministrativeActivity(recordedBy, 
                                 new DateTime().toLocalDate(), 
                                 AdministrativeActivityType.ENTITY_UPDATE);
    updateActivity.setComments(comments);
    _updateActivities.add(updateActivity);
    return updateActivity;
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
