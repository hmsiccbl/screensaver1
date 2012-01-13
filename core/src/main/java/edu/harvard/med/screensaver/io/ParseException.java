// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;


public class ParseException extends Exception
{
  private static final long serialVersionUID = 1L;
  
  private ParseError _error;

  public ParseException(ParseError error)
  {
    super(error.getErrorLocation() + ": " + error.getErrorMessage());
    _error = error;
  }

  public ParseError getError()
  {
    return _error;
  }
}
