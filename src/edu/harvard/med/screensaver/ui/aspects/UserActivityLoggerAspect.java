// $HeadURL:$
// $Id:$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.aspects;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;

import org.aspectj.lang.JoinPoint;

/**
 * Aspect that will generate a "user activity" log entry, echoing the method
 * name and, if available, the name of the concrete AbstractEntity class that is
 * passed to the method.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class UserActivityLoggerAspect extends OrderedAspect
{
  private CurrentScreensaverUser _currentScreensaverUser;

  public void setCurrentScreensaverUser(CurrentScreensaverUser currentScreensaverUser)
  {
    _currentScreensaverUser = currentScreensaverUser;
  }

  public void logUserActivity(JoinPoint joinPoint)
  {
    AbstractEntity entity = null;
    if (joinPoint.getArgs().length > 0 &&
      joinPoint.getArgs()[0] instanceof AbstractEntity) {
      entity = (AbstractEntity) joinPoint.getArgs()[0];
    }
    _currentScreensaverUser.logActivity(joinPoint.getSignature().getName() + (entity == null ? "" : ": " + entity));
  }
}
