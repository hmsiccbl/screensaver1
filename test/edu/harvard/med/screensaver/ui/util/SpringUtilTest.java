// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.faces.application.FacesMessage;

import edu.harvard.med.screensaver.ui.util.Messages;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.Log4jConfigurer;

/**
 * Tests Spring/Hibernate integration. This is one of the rare cases where
 * testing must be Spring-aware, as we're testing our application's Spring
 * configuration. Thus we use
 * <code>AbstractDependencyInjectionSpringContextTests</code> to have Spring
 * inject our persistence-related objects into our test class.
 * 
 * @author andrew tolopko
 */
public class SpringUtilTest
  extends AbstractDependencyInjectionSpringContextTests
{

  /**
   * Spring configuration will be loaded from the configuration file(s)
   * specified in this constant.
   */
   private static final String[] SPRING_CONFIG_FILES = new String[] {"spring-context-ui.xml"};

   protected Messages messages;
  

  static {
    try {
      Log4jConfigurer.initLogging("classpath:log4j.debug.properties");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public SpringUtilTest() {
    // have AbstractDependencyInjectionSpringContextTests inject the properties
    // we need into protected data members that share the same name as beans in
    // our Spring configuration files.
    setPopulateProtectedVariables(true);
  }
  
  // bean property setter/getter methods

  public void setSpringUiUtil(Messages messages) {
    this.messages = messages;
  }

  
  @Override
  /**
   * Provides the Spring framework with the configuration files we need loaded
   * in order to execute our tests.
   */
  protected String[] getConfigLocations()
  {
    return SPRING_CONFIG_FILES;
  }
  

  /* AbstractDependencyInjectionSpringContextTests methods */

  @Override
  protected void onSetUp() throws Exception
  {
  }


  /* JUnit test methods */
  
  @SuppressWarnings("unchecked")
  public void testMessages() throws Exception {
    InputStream messagesStream = getClass().getClassLoader().getResourceAsStream("messages.properties");
    Properties messagesProperties = new Properties();
    messagesProperties.load(messagesStream);
    HashSet<Map.Entry<String,String>> msgSet = new HashSet<Map.Entry<String,String>>();
    msgSet.addAll( (Collection<? extends Entry<String,String>>) messagesProperties.entrySet());
    Object[] args = new Object[] {"arg1", "arg2", "arg3"};
    for (Map.Entry<String,String> msgEntry : msgSet) {
      FacesMessage facesMessage = messages.getFacesMessage(msgEntry.getKey(), args);
      String expectedMessageText = msgEntry.getValue();

      // do our own param substitution for our "expected" value
      for (int i = 0; i < args.length; ++i) {
        expectedMessageText = expectedMessageText.replaceAll("\\Q{" + i + "}\\E",
                                                             args[i].toString());
        assertEquals(expectedMessageText,
                     facesMessage.getDetail());
      }
    }
  }
}
