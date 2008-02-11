// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import org.apache.log4j.Logger;

public class ScreenDBSynchronizationException extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(ScreenDBSynchronizationException.class);

  public ScreenDBSynchronizationException(String message, Throwable throwable)
  {
    super(message, throwable);
  }
  public ScreenDBSynchronizationException(String message)
  {
    super(message);
  }
}

