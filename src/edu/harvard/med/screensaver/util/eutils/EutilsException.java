// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

/**
 * An exception thrown by one of the {@link EutilsUtils}.
 * @author s
 */
public class EutilsException extends Exception
{
  private static final long serialVersionUID = 1L;

  public EutilsException()
  {
  }

  public EutilsException(String message)
  {
    super(message);
  }

  public EutilsException(Throwable cause)
  {
    super(cause);
  }

  public EutilsException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
