// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolver;
import edu.harvard.med.screensaver.db.NeedsScreensaverProperties;

/**
 * Resolves database connection settings from a special RITG-provided production environment file.
 * 
 * @author atolopko
 */
public class OrchestraAuthFileDatabaseConnectionSettingsResolver implements DatabaseConnectionSettingsResolver, NeedsScreensaverProperties
{
  private static final Logger log = Logger.getLogger(OrchestraAuthFileDatabaseConnectionSettingsResolver.class);

  private ScreensaverProperties _screensaverProperties;

  @Override
  public void setScreensaverProperties(ScreensaverProperties screensaverProperties)
  {
    _screensaverProperties = screensaverProperties;
  }

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    String orchestraAuthFilename = _screensaverProperties.getProperty("orchestra.db.connection.file");
    if (orchestraAuthFilename == null) {
      log.warn("no orchestra database connection settings file specified");
      return null;
    }
    log.info("using orchestra database connection settings from file " + orchestraAuthFilename);

    try {
      Pattern pattern = Pattern.compile("SetEnv\\s+(\\S+)\\s+(\\S+)");
      File authFile = new File(orchestraAuthFilename);
      FileInputStream authFileInputStream = new FileInputStream(authFile);
      InputStreamReader authInputStreamReader = new InputStreamReader(authFileInputStream);
      BufferedReader authBufferedReader = new BufferedReader(authInputStreamReader);
      Map<String,String> settings = Maps.newHashMap();
      String line = authBufferedReader.readLine();
      while (line != null) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
          String property = matcher.group(1);
          String propertyValue = matcher.group(2);
          settings.put(property, propertyValue);
          log.debug("parsed " + property + "=" + propertyValue);
        }
        line = authBufferedReader.readLine();
      }
      return new DatabaseConnectionSettings(settings.get("SCREENSAVER_PGSQL_SERVER"),
                                            null,
                                            settings.get("SCREENSAVER_PGSQL_DB"),
                                            settings.get("SCREENSAVER_PGSQL_USER"),
                                            settings.get("SCREENSAVER_PGSQL_PASSWORD"));
    }
    catch (IOException e) {
      throw new DatabaseConnectionSettingsResolutionException("unable to read and parse the orchestra auth file", e);
    }
  }
}
