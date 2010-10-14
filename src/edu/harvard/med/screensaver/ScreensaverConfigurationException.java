// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;


public class ScreensaverConfigurationException extends RuntimeException
{
  private static final long serialVersionUID = 1;

  public ScreensaverConfigurationException(String message)
  {
    super(message);
  }

  public ScreensaverConfigurationException(String message, Throwable e)
  {
    super(message, e);
  }
}
