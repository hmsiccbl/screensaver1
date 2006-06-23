// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.util.Log4jConfigurer;

/**
 * A Spring bean that can configure Spring's Log4J logger. There must be a more
 * standard way of configuring the Log4J logger directly via Spring XML context
 * files, but this class allows us to do what we need, so here it is.
 * <p>
 * Note that this bean is currently configured in the spring-context-logging.xml
 * file, so by telling Spring to use this file as one of its context files,
 * logging will be configured per our project-specific settings, which happen to
 * be stored in resources/log4j.properties (as of 2006-06-23).
 * 
 * @author ant
 */
public class LogConfigurer
{

  private String _logPropertiesFilename;
  private long _refreshInterval;
  private Level _rootLoggerLevel;

  public String getLogPropertiesFilename()
  {
    return _logPropertiesFilename;
  }

  public void setLogPropertiesFilename(String logPropertiesFilename)
  {
    _logPropertiesFilename = logPropertiesFilename;
    update();
  }
  
  public long getRefreshInterval()
  {
    return _refreshInterval;
  }

  public void setRefreshInterval(long refreshInterval)
  {
    _refreshInterval = refreshInterval;
    update();
  }

  public Level getRootLoggerLevel()
  {
    return _rootLoggerLevel;
  }

  public void setRootLoggerLevel(Level rootLoggerLevel)
  {
    _rootLoggerLevel = rootLoggerLevel;
  }
  
  private void update()
  {
    if (_logPropertiesFilename != null) {
      try {
        Log4jConfigurer.initLogging(_logPropertiesFilename,
                                    _refreshInterval);
        if (_rootLoggerLevel != null) {
          Logger.getRootLogger().setLevel(_rootLoggerLevel);
        }
        Logger.getLogger(LogConfigurer.class).debug("Configured logger with properties file '" + _logPropertiesFilename);
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
  
}
