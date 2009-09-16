// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.util.List;

public class ParseErrorsException extends Exception
{
  private static final long serialVersionUID = 1L;

  private List<? extends ParseError> _errors;

  public ParseErrorsException(List<? extends ParseError> errors)
  {
    _errors = errors;
  }

  public List<? extends ParseError> getErrors()
  {
    return _errors;
  }
}
