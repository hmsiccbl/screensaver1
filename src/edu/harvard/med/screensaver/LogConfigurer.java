// $HeadURL$
// $Id$
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
 * A Spring bean that can configure Spring's Log4J logger. According to Spring's
 * Javadocs, a log4j.properties in the root of the classpath will be
 * automatically loaded, although empirically this does not seem to be the case.
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

  private String _logPropertiesResource;
  private long _refreshInterval;
  private Level _rootLoggerLevel;
  
  public LogConfigurer() {}
  
  /**
   * @motivation avoid duplicate this class form outputting duplicate log
   *             messages explaining that log system has been configured
   */
  public LogConfigurer(
    String logPropertiesResource,
    long refreshInterval,
    Level rootLoggerLevel)
  {
    _logPropertiesResource = logPropertiesResource;
    _refreshInterval = refreshInterval;
    _rootLoggerLevel = rootLoggerLevel;
    update();
  }

  public String getLogPropertiesResource()
  {
    return _logPropertiesResource;
  }

  /**
   * @see Log4jConfigurer#initLogging(java.lang.String)
   * @param logPropertiesResource
   */
  public void setLogPropertiesResource(String logPropertiesResource)
  {
    _logPropertiesResource = logPropertiesResource;
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
    if (_logPropertiesResource != null) {
      try {
        Log4jConfigurer.initLogging(_logPropertiesResource,
                                    _refreshInterval);
        if (_rootLoggerLevel != null) {
          Logger.getRootLogger().setLevel(_rootLoggerLevel);
        }
        Logger.getLogger(LogConfigurer.class).info("Configured logger with properties file '" + _logPropertiesResource);
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
  
}
