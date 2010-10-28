// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class DefaultPlateFacilityIdInitializerTest extends TestCase
{
  public void testDefaultPlateFacilityIdInitializer()
  {
    DefaultPlateFacilityIdInitializer defaultPlateFacilityIdInitializer = new DefaultPlateFacilityIdInitializer();
    Library library = new Library(null, "Library", "lib", ScreenType.RNAI, LibraryType.COMMERCIAL, 1, 1, PlateSize.WELLS_96);
    Copy copy = library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    Plate plate = copy.findPlate(1);
    assertTrue(defaultPlateFacilityIdInitializer.initializeFacilityId(plate));
    assertEquals("1-A", plate.getFacilityId());
    plate.setLocation("Freezer1");
    assertTrue(defaultPlateFacilityIdInitializer.initializeFacilityId(plate));
    assertEquals("1-A-Freezer1", plate.getFacilityId());
  }
}
