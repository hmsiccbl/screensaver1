// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/ui/util/SpringUtilTest.java
// $
//$Id$
//
//Copyright 2006 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.faces.application.FacesMessage;

import edu.harvard.med.screensaver.AbstractSpringTest;

public class MessagesTest extends AbstractSpringTest
{

  protected Messages messages;
  
  @Override
  protected String[] getConfigLocations()
  {
    List<String> configLocations = new ArrayList<String>(Arrays.asList(super.getConfigLocations()));
    configLocations.add("spring-context-ui.xml");
    return configLocations.toArray(new String[] {});
  }
  
  
  @SuppressWarnings("unchecked")
  public void testMessagesArgSubstitution() throws Exception {
    InputStream messagesStream = getClass().getClassLoader().getResourceAsStream("messages.properties");
    Properties messagesProperties = new Properties();
    messagesProperties.load(messagesStream);
    HashSet<Map.Entry<Object,Object>> msgSet = new HashSet<Map.Entry<Object,Object>>();
    msgSet.addAll((Set<Map.Entry<Object,Object>>) messagesProperties.entrySet());
    Object[] args = new Object[] {"arg1", "arg2", "arg3"};
    for (Map.Entry<Object,Object> msgEntry : msgSet) {
      FacesMessage facesMessage = messages.getFacesMessage((String) msgEntry.getKey(), args);
      String expectedMessageText = (String) msgEntry.getValue();
      
      // do our own param substitution for our "expected" value
      for (int i = 0; i < args.length; ++i) {
        expectedMessageText = expectedMessageText.replaceAll("\\Q{" + i + "}\\E",
                                                             args[i].toString());
        expectedMessageText = expectedMessageText.replaceAll("''", "'");
      }
      assertEquals(expectedMessageText,
                   facesMessage.getSummary());
    }
  }
}
