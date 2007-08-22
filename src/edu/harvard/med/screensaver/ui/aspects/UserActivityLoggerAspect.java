// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.aspects;

import java.util.Arrays;
import java.util.List;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.util.StringUtils;

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
    StringBuilder s = new StringBuilder();
    s.append(joinPoint.getSignature().getName());

    Object target = joinPoint.getTarget();
    if (target instanceof EntityViewer) {
      EntityViewer entityViewer = (EntityViewer) target;
      AbstractEntity entity = entityViewer.getEntity();
      s.append(": ").append(entity);
    }
    List<Object> args = Arrays.asList(joinPoint.getArgs());
    s.append(" ").append(StringUtils.makeListString(args, ", "));
    _currentScreensaverUser.logActivity(s.toString());
  }
}
