// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;

import org.apache.log4j.Logger;

/**
 * An abstract superclass for the UIController classes.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class AbstractUIController extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(AbstractUIController.class);


  // instance data members
  
  
  // public constructors and methods

  public void logUserActivity(String s)
  {
    getCurrentScreensaverUser().logActivity(s);
  }
  
  // private methods

}

