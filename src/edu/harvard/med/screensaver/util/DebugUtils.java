// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DebugUtils
{
  // static members

  // instance data members

  // public constructors and methods

  public static void logCallStack(Logger log)
  {
    List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();
    StackTraceElement[] stackTraceArray = new Exception().getStackTrace();
    for (int i = 0; i < stackTraceArray.length; i++) {
      StackTraceElement stackTraceElement = stackTraceArray[i];
      if (stackTraceElement.getClassName().startsWith("edu.harvard.med.screensaver") &&
        !stackTraceElement.getClassName().contains("$$EnhancerByCGLIB$$")) {
        stackTrace.add(stackTraceElement);
      }
    }
    log.debug("debug stack trace:\n" + StringUtils.makeListString(stackTrace, "\n"));
  }

  public static long elapsedTime(String description, int repetitions, Logger log, Runnable r)
  {

    long start = System.currentTimeMillis();
    for (int i = 0; i < repetitions; ++i) {
      r.run();
    }
    long end = System.currentTimeMillis();
    long elapsed = end - start;
    log.debug(String.format("elapsed time for %s: %.3f",
                            description,
                            (elapsed / 1000.0) / (double) repetitions));
    return elapsed;
  }

  // private methods

}
