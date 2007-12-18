// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;


public class FatalParseException extends RuntimeException
{
  // static members

  private static final long serialVersionUID = 1L;

  public FatalParseException(Throwable t)
  {
    super(t);
  }

  public FatalParseException(String s)
  {
    super(s);
  }
}

