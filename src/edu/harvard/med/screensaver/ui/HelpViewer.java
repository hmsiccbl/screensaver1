// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screenresults/HelpViewer.java $
// $Id: HelpViewer.java 706 2006-10-31 17:33:20Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * JSF backing bean for Help Viewer web page.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class HelpViewer extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(HelpViewer.class);

  private Map<String,Boolean> _isPanelCollapsedMap= new HashMap<String,Boolean>();
  
  public Map getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }
}
