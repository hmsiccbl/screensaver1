// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

public class DatabaseConnectionSettingsResolutionException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  public DatabaseConnectionSettingsResolutionException()
  {
    super();
  }

  public DatabaseConnectionSettingsResolutionException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DatabaseConnectionSettingsResolutionException(String message)
  {
    super(message);
  }

  public DatabaseConnectionSettingsResolutionException(Throwable cause)
  {
    super(cause);
  }
}
