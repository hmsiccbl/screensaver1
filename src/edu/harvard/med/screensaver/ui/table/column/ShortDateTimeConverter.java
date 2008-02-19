// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import javax.faces.convert.DateTimeConverter;

public class ShortDateTimeConverter extends DateTimeConverter
{
  public ShortDateTimeConverter()
  {
    setDateStyle("short");
  }
}
