// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/rest/core/src/main/java/edu/harvard/med/screensaver/ui/arch/view/aspects/UserActivityLoggerAspect.java $
// $Id: UserActivityLoggerAspect.java 5158 2011-01-06 14:26:53Z atolopko $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view.aspects;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;

import edu.harvard.med.screensaver.util.StringUtils;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class DataRequestActivityLoggerAspect extends OrderedAspect
{
  private static final Logger log = Logger.getLogger(DataRequestActivityLoggerAspect.class);

  public void logDataRequestActivity(JoinPoint joinPoint)
  {
    Object target = joinPoint.getTarget();
    List<Object> args = Arrays.asList(joinPoint.getArgs());

    StringBuilder s = new StringBuilder();
    s.append(target.getClass().getSimpleName()).append('.');
    s.append(joinPoint.getSignature().getName());

    s.append(" ").append(StringUtils.makeListString(args, ", "));

    log.info(s.toString());
  }
}
