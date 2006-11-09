// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;


import javax.faces.event.ActionEvent;


import org.apache.log4j.Logger;
import org.apache.myfaces.custom.navmenu.htmlnavmenu.HtmlCommandNavigationItem;


public class Menu extends AbstractBackingBean
{
  
  // static data members
  
  private static Logger log = Logger.getLogger(Menu.class);


  // JSF event listener methods
  
  public void internalNodeEventListener(ActionEvent event)
  {
    HtmlCommandNavigationItem node = (HtmlCommandNavigationItem) event.getComponent();
//    boolean isOpen = node.isOpen();
//    node.setOpen(!isOpen);
//    node.toggleOpen();
    log.debug((node.isOpen() ? "expanded" : "collapsed") + " navigation node " + node.getId());
//    getFacesContext().renderResponse();
  }
  
}
