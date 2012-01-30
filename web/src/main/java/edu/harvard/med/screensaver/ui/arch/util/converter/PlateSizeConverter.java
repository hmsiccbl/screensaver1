// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/trunk/web/src/main/java/edu/harvard/med/screensaver/ui/arch/util/converter/ScreenTypeConverter.java $
// $Id: ScreenTypeConverter.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.converter;

import edu.harvard.med.screensaver.model.libraries.PlateSize;

public class PlateSizeConverter extends VocabularyConverter<PlateSize>
{
  public PlateSizeConverter()
  {
    super(PlateSize.values());
  }
}
