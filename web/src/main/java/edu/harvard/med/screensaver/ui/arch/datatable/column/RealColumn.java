// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

import edu.harvard.med.screensaver.ui.arch.util.converter.DoubleRoundingConverter;


public abstract class RealColumn<R> extends TableColumn<R,Double>
{
  public RealColumn(String name, String description, String group, Integer decimalPlaces)
  {
    super(name, description, ColumnType.REAL, group);
    setConverter(DoubleRoundingConverter.getInstance(decimalPlaces));
  }
}
