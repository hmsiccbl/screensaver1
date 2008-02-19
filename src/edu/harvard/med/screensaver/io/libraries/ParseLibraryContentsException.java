// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.List;

import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrors;

public class ParseLibraryContentsException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  private ParseErrors _errors;

  public ParseLibraryContentsException(ParseErrors errors)
  {
    _errors = errors;
  }

  public List<? extends ParseError> getErrors()
  {
    return _errors.getErrors();
  }
}
