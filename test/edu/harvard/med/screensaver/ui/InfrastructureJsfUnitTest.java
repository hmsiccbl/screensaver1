// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

/**
 * Top-level class for user interface unit tests. (As this grows, it will be
 * refactored into a reasonable hierarchy of classes.)
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class InfrastructureJsfUnitTest extends AbstractJsfUnitTest
{
  
  private static final Logger log = Logger.getLogger(InfrastructureJsfUnitTest.class);

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(InfrastructureJsfUnitTest.class);
    return suite;
  }

  // failure of this documented in RT #143637
  public void testUIControllerMethodExceptionHandlerAspect() throws Exception
  {
    Integer screenNumber = 1;
    final Screen screen = MakeDummyEntities.makeDummyScreen(screenNumber);
    _dao.persistEntity(screen);
    
    visitMainPage();
    submit("findScreenCommand", new Pair<String,String>("screensForm:screenNumber", Integer.toString(screenNumber)));
    assertAtView("/screensaver/screens/screenViewer.jsf");
    assertEquals(screenNumber, (Integer) getBeanValue("screenViewer.screen.screenNumber"));
    
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() 
      {
        Screen screen2 = _dao.reloadEntity(screen);
        screen2.setComments("screen edited!");
      }
    });
    log.debug("edited screen in separate Hibernate session");
    
    submit("screenDetailPanelForm:editCommand");
    assertAtView("/screensaver/screens/screenDetailViewer.jsf");
    assertTrue("screen viewer in edit mode",
               ((Boolean) getBeanValue("screenDetailViewer.editMode")).booleanValue());
    log.debug("invoked Edit command on Screen Detail Viewer");
    submit("screenDetailPanelForm:saveCommand");
    log.debug("invoked Save command on Screen Detail Viewer");
    assertMessage(".*Other users have already modified the data that you are attempting to update.*");
    assertAtView("/screensaver/screens/screenViewer.jsf");
     
    assertEquals("screen updated to lastest version",
                 "screen edited!",
                 (String) getBeanValue("screenDetailViewer.screen.comments"));
  }
}