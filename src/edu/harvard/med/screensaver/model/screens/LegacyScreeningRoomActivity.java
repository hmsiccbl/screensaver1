// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;

import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 * For legacy "Special Visit" type from ScreenDB.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * 
 * @hibernate.joined-subclass table="legacy_screening_room_activity" lazy="false"
 * @hibernate.joined-subclass-key column="screening_room_activity_user_id"
 */
public class LegacyScreeningRoomActivity extends ScreeningRoomActivity
{
  // static members

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(LegacyScreeningRoomActivity.class);

  
  // instance data members
  
  private String _activityTypeName;


  // public constructors and methods

  public LegacyScreeningRoomActivity(Screen screen, ScreeningRoomUser performedBy, Date dateCreated, Date dateOfActivity, String activityTypeName) throws DuplicateEntityException
  {
    super(screen, performedBy, dateCreated, dateOfActivity);
    _activityTypeName = activityTypeName;
  }
  
  @Override
  @DerivedEntityProperty(isPersistent=true)
  public String getActivityTypeName()
  {
    return _activityTypeName;
  }
  
  /**
   * @hibernate.property type="text"
   */
  @ImmutableProperty
  public String getLegacyActivityTypeName()
  {
    return _activityTypeName;
  }
  
  // private methods

  /**
   * @motivation for hibernate
   */
  private LegacyScreeningRoomActivity()
  {
  }

  private void setLegacyActivityTypeName(String activityTypeName)
  {
    _activityTypeName = activityTypeName;
  }
}

