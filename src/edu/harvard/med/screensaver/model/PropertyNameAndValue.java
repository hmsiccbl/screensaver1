// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import edu.harvard.med.screensaver.util.Pair;

public class PropertyNameAndValue extends Pair<String,Object>
{

  public PropertyNameAndValue(String name, Object value)
  {
    super(name, value);
  }

  public String getName() { return getFirst(); }

  public Object getValue() { return getSecond(); }

}
