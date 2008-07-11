// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;




import junit.framework.Test;
import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.AbstractJsfUnitTest;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@SuppressWarnings("unchecked")
public class CherryPickRequestViewerJsfUnitTest extends AbstractJsfUnitTest
{
  private static final Logger log = Logger.getLogger(CherryPickRequestViewerJsfUnitTest.class);
  private Screen _screen;
  private CherryPickRequest _cpr;

  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(CherryPickRequestViewerJsfUnitTest.class);
    return suite;
  }

  public void setUp() throws Exception
  {
    super.setUp();
    _screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.SMALL_MOLECULE);
    _cpr = _screen.createCherryPickRequest();
    _dao.persistEntity(_screen);
    // ensure _screen entity and its children have ID-based hashCodes
    _screen = _dao.reloadEntity(_screen, true, "labHead", "leadScreener", "collaborators", "cherryPickRequests");
    _cpr = _screen.getCherryPickRequests().iterator().next();
  }

  public void testOpenCherryPickRequestViewer() throws Exception
  {
    visitCherryPickRequestViewer(_cpr);
    assertShowingCherryPickRequest(_cpr, false);
  }

  public void testFindCherryPickRequestNumber() throws Exception
  {
  }

  public void testDeprecatedWellWarning() throws Exception
  {
    visitCherryPickRequestViewer(_cpr);
    assertShowingCherryPickRequest(_cpr, false);
    fail("not implemented");
  }


  private void visitCherryPickRequestViewer(CherryPickRequest cpr)
  {
    CherryPickRequestViewer viewer = getBeanValue("cherryPickRequestViewer");
    viewer.viewCherryPickRequest(cpr);
    visitPage("/cherryPickRequests/cherryPickRequestsBrowser.jsf");
    assertAtView("/screensaver/cherryPickRequests/screensBrowser.jsf");
  }

  private void assertShowingCherryPickRequest(CherryPickRequest cpr, boolean isEditModeExpected)
  {
    if (isEditModeExpected) {
      fail("not implemented");
//      assertEquals("cherryPickRequestDetailViewer cherryPickRequest number",
//                   new Integer(cpr.getCherryPickRequestNumber()),
//                   (Integer) getBeanValue("cherryPickRequestDetailViewer.cherryPickRequest.cherryPickRequestNumber"));
//      // when in edit mode, cherryPickRequest is shown within a cherryPickRequestDetailViewer, outside of a search results context
//      assertAtView("/cherryPickRequestsaver/cherryPickRequests/cherryPickRequestDetailViewer.jsf");
//      assertElementTextEqualsRegex("cherryPickRequestDetailViewerForm:cherryPickRequestNumberLinkValue", Integer.toString(cherryPickRequestNumber));
//      assertTrue("edit mode", (Boolean) getBeanValue("cherryPickRequestDetailViewer.editMode"));
    }
    else {
      assertEquals("cherryPickRequestViewer cherryPickRequest number",
                   new Integer(cpr.getCherryPickRequestNumber()),
                   (Integer) getBeanValue("cherryPickRequestViewer.cherryPickRequest.cherryPickRequestNumber"));
//      // when not in edit mode, cherryPickRequest is always to be shown within a search results context
//      assertAtView("/cherryPickRequestsaver/cherryPickRequests/cherryPickRequestsBrowser.jsf");
//      assertTrue(((Boolean) getBeanValue("cherryPickRequestsBrowser.entityView")).booleanValue());
//      assertElementTextEqualsRegex("cherryPickRequestDetailPanelForm:cherryPickRequestNumberLinkValue", Integer.toString(cherryPickRequestNumber));
      assertFalse("read-only mode", (Boolean) getBeanValue("cherryPickRequestViewer.editMode"));
    }
  }
}