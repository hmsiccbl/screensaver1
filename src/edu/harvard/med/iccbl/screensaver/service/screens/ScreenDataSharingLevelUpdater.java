// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ScreenDataSharingLevelUpdater
{
  private static Logger log = Logger.getLogger(ScreenDataSharingLevelUpdater.class);
  private GenericEntityDAO _dao;
  
  protected ScreenDataSharingLevelUpdater() {}
  
  public ScreenDataSharingLevelUpdater(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  @Transactional
  public AdministrativeActivity updateScreen(Screen screen, 
                                             ScreenDataSharingLevel screenDataSharingLevel, 
                                             AdministratorUser recordedBy)
  {
    verifyOperationPermitted(recordedBy);
    ScreenDataSharingLevel oldLevel = screen.getDataSharingLevel();
    screen.setDataSharingLevel(screenDataSharingLevel);
    return screen.createUpdateActivity(recordedBy,
                                       "Data Sharing Level updated from : " + oldLevel 
                                       + " to  " + screenDataSharingLevel);
  }
  
  /**
   * Return the Set of Screens that have dataPrivacyExpirationDates that are on or before the given date.
   * @param date
   * @return not null, empty Set if nothing is found
   */
  @Transactional
  public Set<Screen> findNewExpired(LocalDate date)
  {
    String hql = "from Screen where  " +
    		" dataPrivacyExpirationDate <= ? " +
        " and dataSharingLevel > ? ";
    return Sets.newHashSet(_dao.findEntitiesByHql(Screen.class, hql, date, ScreenDataSharingLevel.MUTUAL_SCREENS));
  }

  /**
   * Expire any Screen wherein the dataPrivacyExpirationDate is less than or equal to the passed in date.
   * @param date
   * @return Screens updated
   */
  @Transactional
  public List<Pair<Screen,AdministrativeActivity>> expireScreenDataSharingLevels(LocalDate date, AdministratorUser recordedBy)
  {
    verifyOperationPermitted(recordedBy);
    Set<Screen> screens = findNewExpired(date);
    List<Pair<Screen,AdministrativeActivity>> results = Lists.newLinkedList();
    for(Screen screen:screens)
    {
      assert(screen.getDataPrivacyExpirationDate().compareTo(date) <= 0);
      AdministrativeActivity activity = updateScreen(screen, ScreenDataSharingLevel.MUTUAL_SCREENS, recordedBy);
      results.add(new Pair<Screen,AdministrativeActivity>(screen,activity));
    }
    return results;
  }
  
  private void verifyOperationPermitted(AdministratorUser recordedBy)
  throws OperationRestrictedException
  {
    if (!!!recordedBy.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENS_ADMIN)) {
      throw new OperationRestrictedException("to update a user's user agreement, administrator must have " + ScreensaverUserRole.SCREENS_ADMIN.getDisplayableRoleName() + " role");
    }
  }
}
