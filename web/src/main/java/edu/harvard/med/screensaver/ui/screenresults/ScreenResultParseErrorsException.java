// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import org.apache.log4j.Logger;

public class ScreenResultParseErrorsException extends RuntimeException
{

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(ScreenResultParseErrorsException.class);

  public ScreenResultParseErrorsException(String msg)
  {
    super(msg);
  }
}

