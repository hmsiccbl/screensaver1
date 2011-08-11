
package edu.harvard.med.screensaver.model;

import java.util.SortedSet;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;

public class DownloadRestrictedAttachedFile extends AttachedFile
{

  private AttachedFile _delegate;

  public DownloadRestrictedAttachedFile(AttachedFile entity)
  {
    _delegate = entity;
  }

  @Override
  public byte[] getFileContents()
  {
    return null; // NOTE: returning null here, as expected; however, attachedFiles.xhtml will check the "isRestricted" flag, because the contents aren't available unless hibernate-managed (LOB type)
  }

  @Override
  public int compareTo(AttachedFile other)
  {
    return _delegate.compareTo(other);
  }

  @Override
  public boolean equals(Object obj)
  {
    return _delegate.equals(obj);
  }

  @Override
  public Integer getAttachedFileId()
  {
    return _delegate.getAttachedFileId();
  }

  @Override
  public ScreensaverUser getCreatedBy()
  {
    return _delegate.getCreatedBy();
  }

  @Override
  public DateTime getDateCreated()
  {
    return _delegate.getDateCreated();
  }

  @Override
  public Integer getEntityId()
  {
    return _delegate.getEntityId();
  }

  @Override
  public EntityViewPolicy getEntityViewPolicy()
  {
    return _delegate.getEntityViewPolicy();
  }

  @Override
  public LocalDate getFileDate()
  {
    return _delegate.getFileDate();
  }

  @Override
  public String getFilename()
  {
    return _delegate.getFilename();
  }

  @Override
  public AttachedFileType getFileType()
  {
    return _delegate.getFileType();
  }

  @Override
  public AdministrativeActivity getLastRecordedUpdateActivityOfType(AdministrativeActivityType activityType)
  {
    return _delegate.getLastRecordedUpdateActivityOfType(activityType);
  }

  @Override
  public AdministrativeActivity getLastUpdateActivityOfType(AdministrativeActivityType activityType)
  {
    return _delegate.getLastUpdateActivityOfType(activityType);
  }

  @Override
  public <P> P getPropertyValue(String propertyName, Class<P> propertyType)
  {
    return _delegate.getPropertyValue(propertyName, propertyType);
  }

  @Override
  public Reagent getReagent()
  {
    return _delegate.getReagent();
  }

  @Override
  public Screen getScreen()
  {
    return _delegate.getScreen();
  }

  @Override
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _delegate.getScreeningRoomUser();
  }

  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _delegate.getUpdateActivities();
  }

  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivitiesOfType(AdministrativeActivityType activityType)
  {
    return _delegate.getUpdateActivitiesOfType(activityType);
  }

  @Override
  public boolean isEquivalent(AbstractEntity that)
  {
    return _delegate.isEquivalent(that);
  }

  @Override
  public boolean isRestricted()
  {
    return true;
  }

  @Override
  public void setEntityViewPolicy(EntityViewPolicy<Entity> entityViewPolicy)
  {
    _delegate.setEntityViewPolicy(entityViewPolicy);
  }

  @Override
  public String toString()
  {
    return _delegate.toString();
  }

}
