// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import edu.harvard.med.screensaver.model.users.ScreensaverUser;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Embeddable
public class EntityUpdate
{
  private DateTime timeStamp;
  private ScreensaverUser recordedBy;
  private String description;
  
  protected EntityUpdate() {}
  
  public EntityUpdate(ScreensaverUser recordedBy,
                      String description)
  {
    this.timeStamp = new DateTime();
    this.recordedBy = recordedBy;
    this.description = description;
  }

  @Column(nullable=false)
  @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
  public DateTime getTimeStamp()
  {
    return timeStamp;
  }

  private void setTimeStamp(DateTime timeStamp)
  {
    this.timeStamp = timeStamp;
  }
  
  @ManyToOne(fetch=FetchType.LAZY, cascade={ /*CascadeType.PERSIST, */CascadeType.MERGE })
  @JoinColumn(nullable=true) /*eventually, we should make non-nullable*/
  @org.hibernate.annotations.ForeignKey(name="fk_entity_change_log_to_screensaver_user")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  //@org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true)
  public ScreensaverUser getRecordedBy()
  {
    return recordedBy;
  }
  
  private void setRecordedBy(ScreensaverUser performedBy)
  {
    this.recordedBy = performedBy;
  }

  @Type(type="text")
  public String getDescription()
  {
    return description;
  }
  
  private void setDescription(String description)
  {
    this.description = description;
  }
}
