// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;

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
public class OrchestraPropertyPlaceholderConfigurer
extends PropertyPlaceholderConfigurer
{
  
  // static members

  private static Logger log = Logger
    .getLogger(OrchestraPropertyPlaceholderConfigurer.class);
  
  
  // private instance fields
  
  private Properties _properties;
  
  
  // protected instance method

  synchronized protected void convertProperties(Properties properties)
  {
    _properties = properties;
    if (shouldConvertPropertiesFromOrchestraAuthFile()) {
      convertPropertiesFromOrchestraAuthFile();
    }
    else if (shouldReadPasswordFromDotPgpassFile()) {
      readPasswordFromDotPgpassFile();
    }
  }
  
  private boolean shouldConvertPropertiesFromOrchestraAuthFile()
  {
    boolean shouldConvert = _properties.getProperty("SCREENSAVER_PGSQL_SERVER") == null;
    if (shouldConvert) {
      log.info("Found SCREENSAVER_PGSQL_SERVER env var: assuming we are running on orchestra");
      // we have null values for the required datasource properties, so assume we are on
      // orchestra, and apply orchestra-specific method for obtaining them      
    }
    return shouldConvert;
  }

  private void convertPropertiesFromOrchestraAuthFile() {
    String catalinaBase = System.getenv("CATALINA_BASE");
    log.info("CATALINA_BASE env var=" + catalinaBase);
    if (catalinaBase == null || ! catalinaBase.matches("/www/[^/]+/tomcat")) {
      log.warn("We (admittedly not so cleverly) assumed that since the property " +
        "\"SCREENSAVER_PGSQL_SERVER\" was not set, either in screensaver.properties, or " +
        "in the environment variables passed to the JVM, that this was an instance " +
        "of Screensaver running on the orchestra webserver \"trumpet\". However, we " +
        "also expect such instances to have a \"CATALINA_BASE\" environment variable " +
        "passed to the JVM, whose value starts with \"/www/\", and ends with " +
        "\"/tomcat\", and whose remainder is the site name. Unfortunately, our " +
        "expectations in regards to the \"CATALINA_BASE\" environment variable " +
        "failed to hold. In this situation, we are afraid we have absolutely no way of " +
        "being able to appropriately set the datasource properties necessary for " +
        "Screensaver to run. (The value we found for the \"CATALINA_BASE\" environment " +
        "variable is \"" + catalinaBase + "\".)");

      // well, it turns out we are not actually on orchestra, so give up 
      return;
    }

    // obscurely trim the "/www/" and "/tomcat" prefix and postfix to get the site name
    String orchestraSite = catalinaBase.substring(5, catalinaBase.length() - 7);
    String authFilename = "/opt/apache/conf/auth/" + orchestraSite;

    // parse the datasource properties out of the file
    try {
      Pattern pattern = Pattern.compile("SetEnv\\s+(\\S+)\\s+(\\S+)");
      File authFile = new File(authFilename);
      FileInputStream authFileInputStream = new FileInputStream(authFile);
      InputStreamReader authInputStreamReader = new InputStreamReader(authFileInputStream);
      BufferedReader authBufferedReader = new BufferedReader(authInputStreamReader);
      String line = authBufferedReader.readLine();
      while (line != null) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          String property = matcher.group(1);
          String propertyValue = matcher.group(2);
          _properties.setProperty(property, propertyValue);
        }
        line = authBufferedReader.readLine();
      }
    }
    catch (IOException e) {
      log.warn("unable to read and parse the orchestra auth file", e);
    }
  }
  
  private boolean shouldReadPasswordFromDotPgpassFile()
  {
    return _properties.getProperty("SCREENSAVER_PGSQL_PASSWORD") == null;
  }
  
  private void readPasswordFromDotPgpassFile()
  {
    _properties.setProperty(
      "SCREENSAVER_PGSQL_PASSWORD",
      new DotPgpassFileParser().getPasswordFromDotPgpassFile(
        _properties.getProperty("SCREENSAVER_PGSQL_SERVER"),
        null,
        _properties.getProperty("SCREENSAVER_PGSQL_DB"),
        _properties.getProperty("SCREENSAVER_PGSQL_USER")));
  }
}

