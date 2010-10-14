// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.util.StringUtils;

public class ScreensaverProperties
{
  private static Logger log = Logger.getLogger(ScreensaverProperties.class);
  private static final String SCREENSAVER_PROPERTIES_RESOURCE = 
    "../../../../screensaver.properties"; // relative to current package
  private Properties _properties = new Properties();
  private Map<String,Boolean> _featuresEnabled;

  public ScreensaverProperties()
  {
    InputStream screensaverPropertiesInputStream =
      ScreensaverProperties.class.getResourceAsStream(SCREENSAVER_PROPERTIES_RESOURCE);
    try {
      initializeProperties(screensaverPropertiesInputStream);
      initializeFeaturesEnabled(_properties); // initialize
      validateProperties(_properties);
    }
    catch (IOException e) {
      throw new ScreensaverConfigurationException("error loading screensaver.properties resource", e);
    }
  }

  public void initializeProperties(InputStream screensaverPropertiesInputStream) throws IOException
  {
    _properties.load(screensaverPropertiesInputStream);
    logProperties(_properties);
  }

  private void validateProperties(Properties screensaverProperties)
  {
    if (isFeatureEnabled("cellHTS2")) {
      if (!isPropertySet("cellHTS2.report.directory")) {
        throw new ScreensaverConfigurationException("undefined system property 'cellHTS2.report.directory'");
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
  public boolean isPropertySet(String propKey)
  {
    return !StringUtils.isEmpty(_properties.getProperty(propKey));
  }
  
  public String getProperty(String name)
  {
    return _properties.getProperty(name);
  }
  
  private static void logProperties(Properties screensaverProperties)
  {
    log.info("Screensaver system properties:");
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

  /**
   * for JSF 1.1 EL expressions
   */
  public Properties getMap()
  {
    return _properties;
  }
}
