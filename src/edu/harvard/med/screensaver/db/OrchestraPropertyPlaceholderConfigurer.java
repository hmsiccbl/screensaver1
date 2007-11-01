// $HeadURL$
// $Id$
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
    if (convertPropertiesFromOrchestraAuthFile()) {
      return;
    }
    if (shouldUseUnixUser()) {
      useUnixUser();
    }
    if (shouldReadPasswordFromDotPgpassFile()) {
      readPasswordFromDotPgpassFile();
    }
  }

  private boolean shouldConvertPropertiesFromOrchestraAuthFile()
  {
    boolean shouldConvert = _properties.getProperty("orchestra.db.connection.file") == null;
    if (shouldConvert) {
    }
    return shouldConvert;
  }

  private boolean convertPropertiesFromOrchestraAuthFile() {
    String orchestraAuthFilename = _properties.getProperty("orchestra.db.connection.file");
    if (orchestraAuthFilename == null) {
      return false;
    }
    log.info("using orchestra database connection settings from file " + orchestraAuthFilename);

    // parse the datasource properties out of the file
    try {
      Pattern pattern = Pattern.compile("SetEnv\\s+(\\S+)\\s+(\\S+)");
      File authFile = new File(orchestraAuthFilename);
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
      return true;
    }
    catch (IOException e) {
      log.warn("unable to read and parse the orchestra auth file", e);
      return false;
    }
  }

  private boolean shouldUseUnixUser()
  {
    return _properties.getProperty("SCREENSAVER_PGSQL_USER") == null;
  }

  private void useUnixUser()
  {
    String unixUser = System.getenv("USER");
    if (unixUser == null) {
      log.warn("could not determine UNIX user name");
    }
    else {
      _properties.setProperty("SCREENSAVER_PGSQL_USER", unixUser);
      log.info("using UNIX user name '" + unixUser + "'");
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