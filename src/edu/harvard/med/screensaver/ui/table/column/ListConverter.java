// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.ui.util.NoOpStringConverter;

public class ListConverter extends CollectionConverter<List<String>,String>
{
  public ListConverter()
  {
    super(new NoOpStringConverter());
  }

  @Override
  protected List<String> makeCollection()
  {
    return new ArrayList<String>();
  }
}
