// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractController;

public class WellViewerController extends AbstractController
{
  private Well well;

  public Well getWell()
  {
    return well;
  }

  public void setWell(Well well)
  {
    this.well = well;
  }

  
  // NOTE: I turned off the Done button for the time being. Sorry! -s
  
  public boolean getDisplayDone()
  {
    return false;
  }
  
  public String done()
  {
    return DONE_ACTION_RESULT;
  }
}
