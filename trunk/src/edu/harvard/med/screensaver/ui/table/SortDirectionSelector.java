// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.Arrays;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public class SortDirectionSelector extends UISelectOneBean<SortDirection>
{
  // static members

  private static Logger log = Logger.getLogger(SortDirectionSelector.class);


  // instance data members

  // public constructors and methods
  
  public SortDirectionSelector()
  {
    super(Arrays.asList(SortDirection.values()));
  }

  // private methods

}

