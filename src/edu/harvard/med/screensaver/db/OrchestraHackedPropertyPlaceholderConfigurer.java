// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * A <code>PropertyPlaceholderConfigurer</code> that applies an extra filter to correctly
 * obtain datasource properties. If the required datasource properties remain unset via
 * normal means, then assume we are on orchestra, and apply orchestra-specific method for
 * obtaining the datasource properties. Insert the obtained datasource properties into the
 * <code>Properties</code> object that contains the results of the {@link
 * PropertyResourceConfigurer}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class OrchestraHackedPropertyPlaceholderConfigurer
extends PropertyPlaceholderConfigurer
{
  
  // static members

  private static Logger log = Logger
    .getLogger(OrchestraHackedPropertyPlaceholderConfigurer.class);

  private static final String [] _propertiesToFixUp = {
    "SCREENSAVER_PGSQL_SERVER",
    "SCREENSAVER_PGSQL_DB",
    "SCREENSAVER_PGSQL_USER",
    "SCREENSAVER_PGSQL_PASSWORD",
  };
  
  
  // protected and private instance methods

  protected void convertProperties(Properties properties)
  {
    if (properties.getProperty(_propertiesToFixUp[0]) == null) {
      
      // we have null values for the required datasource properties, so assume we are on
      // orchestra, and apply orchestra-specific method for obtaining them
      
      String catalinaBase = System.getenv("CATALINA_BASE");
      log.debug("catalinaBase = " + catalinaBase);
      if (! catalinaBase.matches("/www/[!/]/tomcat")) {
        log.debug("catalinaBase doesnt match");

        // well, it turns out we are not actually on orchestra, so give up 
        return;
      }
      log.debug("catalinaBase matches");
      String orchestraSite = catalinaBase.substring(5, catalinaBase.length() - 7);
      log.debug("orch site = " + orchestraSite);

      
      for (String property : _propertiesToFixUp) {
        fixUpProperty(property, properties);
      }
    }
  }
  
  private void fixUpProperty(String property, Properties properties)
  {
    String propertyValue = properties.getProperty(property);
    log.debug("HACK: " + property + " = " + propertyValue);
  }
}

