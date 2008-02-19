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

public interface ParseErrors
{

  /**
   * Get the list of <code>ParseError</code> objects.
   *
   * @return a list of <code>ParseError</code> objects
   */
  public List<? extends ParseError> getErrors();

  /**
   * @motivation For JSF EL expressions
   */
  public boolean getHasErrors();

}