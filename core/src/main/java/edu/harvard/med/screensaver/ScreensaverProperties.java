// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import edu.harvard.med.lincs.screensaver.LincsScreensaverConstants;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolver;
import edu.harvard.med.screensaver.db.NeedsScreensaverProperties;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

public class ScreensaverProperties implements ScreensaverConstants
{
  private static Logger log = Logger.getLogger(ScreensaverProperties.class);
  private Properties _properties = new Properties();
  private Properties _versionProperties = new Properties();
  private Map<String,Boolean> _featuresEnabled;
  private DatabaseConnectionSettings _databaseConnectionSettings;
  private List<DatabaseConnectionSettingsResolver> _databaseConnectionSettingsResolvers;

  public ScreensaverProperties(String defaultScreensaverPropertiesFile,
                               DatabaseConnectionSettingsResolver dbCxnSettingsResolver)
  {
    this(defaultScreensaverPropertiesFile,
         ImmutableList.of(dbCxnSettingsResolver));
  }

  public ScreensaverProperties(String screensaverPropertiesFile,
                               List<DatabaseConnectionSettingsResolver> dbCxnSettingsResolvers)
  {
    _databaseConnectionSettingsResolvers = dbCxnSettingsResolvers;
    try {
      initializeProperties(screensaverPropertiesFile);
      initializeVersionProperties();
      initializeFeaturesEnabled(_properties); // initialize
      validateProperties();
    }
    catch (IOException e) {
      throw new ScreensaverConfigurationException("error loading screensaver properties", e);
    }
  }

  public void initializeProperties(String propertiesFile) throws IOException
  {
    String propFileName =
      System.getProperty(ScreensaverConstants.SCREENSAVER_PROPERTIES_FILE_PROPERTY_NAME);
    InputStream screensaverPropertiesInputStream = null;
    if (!StringUtils.isEmpty(propFileName)) {
      log.info("loading screensaver properties from file location: " + propFileName);
      screensaverPropertiesInputStream = new FileInputStream(new File(propFileName));
    }
    else {
      log.info("loading screensaver properties from resource " + propertiesFile);
      screensaverPropertiesInputStream = ScreensaverProperties.class.getResourceAsStream(propertiesFile);
    }
    _properties.load(screensaverPropertiesInputStream);
    logProperties("Screensaver properties", _properties);
  }

  public void initializeVersionProperties() throws IOException
  {
    log.info("loading version properties from resource " + ScreensaverConstants.VERSION_PROPERTIES_RESOURCE);
    InputStream versionPropertiesInputStream = ScreensaverProperties.class.getResourceAsStream(ScreensaverConstants.VERSION_PROPERTIES_RESOURCE);
    _versionProperties.load(versionPropertiesInputStream);
    logProperties("Version properties", _versionProperties);
  }

  private void validateProperties()
  {
    if (!!!_versionProperties.containsKey(VERSION_PROPERTY)) {
      throw new ScreensaverConfigurationException("undefined version property '" + VERSION_PROPERTY + "'");
    }
    if (!!!_versionProperties.containsKey(BUILD_NUMBER_PROPERTY)) {
      throw new ScreensaverConfigurationException("undefined version property '" + BUILD_NUMBER_PROPERTY + "'");
    }
    if (isFeatureEnabled("cellHTS2")) {
      if (!isPropertySet("cellHTS2.report.directory")) {
        throw new ScreensaverConfigurationException("undefined system property 'cellHTS2.report.directory'");
      }
      File reportDir = new File(getProperty("cellHTS2.report.directory"));
      if (!reportDir.exists()) {
        throw new ScreensaverConfigurationException("'cellHTS2.report.directory' " + reportDir + " does not exist");
      }
      if (getBooleanProperty("cellHTS2.saveRObjects")) {
        File rObjectsDir = new File(getProperty("cellHTS2.saveRObjects.directory"));
        if (!rObjectsDir.exists()) {
          throw new ScreensaverConfigurationException("'cellHTS2.saveRObjects' " + rObjectsDir + " does not exist");
        }
      }
    }
  }

  /**
   * @return a Map of the UI features in Screensaver that can be enabled or
   *         disable. Map key is the feature name, as determined by the property
   *         name without the {@link ScreensaverConstants#SCREENSAVER_UI_FEATURE_PREFIX} prefix
   *         (e.g., the property key "screensaver.ui.feature.someFeature" would
   *         be identified by the key "someFeature" in the returned map). Map
   *         value is a Boolean, where <code>true</code> indicates the feature
   *         is enabled, otherwise it is disabled.
   */
  private void initializeFeaturesEnabled(Properties screensaverProperties)
  {
    _featuresEnabled = Maps.newHashMap();
    for (Map.Entry<Object,Object> featureEntry : screensaverProperties.entrySet()) {
      if (featureEntry.getKey().toString().startsWith(ScreensaverConstants.SCREENSAVER_UI_FEATURE_PREFIX)) {
        String featureKey = featureEntry.getKey().toString().substring(ScreensaverConstants.SCREENSAVER_UI_FEATURE_PREFIX.length());
        Boolean featureEnabled = Boolean.valueOf(featureEntry.getValue().toString());
        _featuresEnabled.put(featureKey, featureEnabled);
        log.info("screensaver feature " + featureKey + " is " + (featureEnabled ? "enabled" : "disabled"));
      }
    }
  }

  /**
   * @return true if the specified property has been defined, even if it has a null value
   * @see #isPropertySet(String)
   */
  public boolean isPropertyDefined(String name)
  {
    return _properties.containsKey(name);
  }
  
  /**
   * @return true if the specified property has been defined, and has a non-null value
   * @see #isPropertyDefined(String)
   */
  public boolean isPropertySet(String name)
  {
    if (!isPropertyDefined(name)) {
      return false;
    }
    return !StringUtils.isEmpty(_properties.getProperty(name));
  }
  
  public String getVersion()
  {
    return _versionProperties.getProperty(VERSION_PROPERTY);
  }

  public String getBuildNumber()
  {
    return _versionProperties.getProperty(BUILD_NUMBER_PROPERTY);
  }

  public String getProperty(String name)
  {
    return _properties.getProperty(name);
  }
  
  private static void logProperties(String name,
                                    Properties screensaverProperties)
  {
    log.info(name);
    for (Entry<Object,Object> entry : screensaverProperties.entrySet()) {
      String value = entry.getValue().toString();
      if (entry.getKey().toString().matches(".*(?i)password.*")) {
        value = "<password>";
      }
      log.info(entry.getKey() + "=" + value);
    }
  }

  /** 
   * @param name
   * @return false if the property is nonexistent or not equal (case-insensitive) "t" or "true"
   */
  public boolean getBooleanProperty(String name)
  {
    String temp = _properties.getProperty(name, "false").toLowerCase();
    return temp.equals("true") || temp.equals("t");
  }

  public boolean isFeatureEnabled(String featureKey)
  {
    if (_featuresEnabled.containsKey(featureKey)) {
      return _featuresEnabled.get(featureKey);
    }
    log.warn("unknown feature " + featureKey);
    return false;
  }

  public DatabaseConnectionSettings getDatabaseConnectionSettings()
  {
    if (_databaseConnectionSettings == null) {
      Iterator<DatabaseConnectionSettingsResolver> iter = _databaseConnectionSettingsResolvers.iterator();
      while (_databaseConnectionSettings == null && iter.hasNext()) {
        DatabaseConnectionSettingsResolver resolver = iter.next();
        log.info("resolving database connection settings using " + resolver);
        if (resolver instanceof NeedsScreensaverProperties) {
          ((NeedsScreensaverProperties) resolver).setScreensaverProperties(this);
        }
        _databaseConnectionSettings = resolver.resolve();
      }
      if (_databaseConnectionSettings == null) {
        throw new DatabaseConnectionSettingsResolutionException("could not resolve database connection settings");
      }
      log.info("using database connection settings: " + _databaseConnectionSettings);
    }
    return _databaseConnectionSettings;
  }

  /**
   * for JSF 1.1 EL expressions
   */
  public Properties getMap()
  {
    return _properties;
  }

  /**
   * Return a human-readable facility name; this is for display purposes only.
   * 
   * @see #getFacilityKey()
   */
  public String getFacilityName()
  {
    return getProperty(FACILITY_NAME);
  }

  /**
   * Return the facility key for the running application. This key can be used to conditionally enable or disable
   * facility-specific features or behaviors of the running application. Use the facility key, and not the
   * {@link #getFacilityName() facility name} for this purpose. Note that this property is set at build-time.
   * 
   * @see #getFacilityName()
   */
  public String getFacilityKey()
  {
    return _versionProperties.getProperty(FACILITY_KEY_PROPERTY);
  }

  public boolean isFacility(String facilityKey)
  {
    return NullSafeUtils.nullSafeEquals(getFacilityKey(), facilityKey);
  }

  /**
   * @deprecated use {@link #getFacilityKey()}
   */
  @Deprecated
  public boolean isLincsAppVersion()
  {
    return LincsScreensaverConstants.FACILITY_KEY.equals(getProperty(FACILITY_NAME));
  }

  public boolean isAllowGuestLogin()
  {
    return getBooleanProperty("screensaver.ui.feature.allow_guest_access");
  }
}
