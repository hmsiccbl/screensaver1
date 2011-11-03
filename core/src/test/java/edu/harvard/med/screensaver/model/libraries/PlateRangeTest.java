// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.List;

import com.google.common.collect.ImmutableSortedSet;
import junit.framework.TestCase;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateRange;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class PlateRangeTest extends TestCase
{
  public void testPlateRangeScreened()
  {
    Library library = new Library(null);
    library.setLibraryName("x");
    library.setStartPlate(100);
    library.setEndPlate(103);
    Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    Copy copyB = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    Plate plate100 = copyA.findPlate(100).withWellVolume(new Volume(0));
    Plate plate101 = copyA.findPlate(101).withWellVolume(new Volume(0));
    Plate plate102 = copyB.findPlate(102).withWellVolume(new Volume(0));
    Plate plate103 = copyA.findPlate(103).withWellVolume(new Volume(0));
    Screen screen = new Screen(null);
    LibraryScreening libraryScreening = screen.createLibraryScreening(null, new ScreeningRoomUser(null), new LocalDate());
    libraryScreening.setNumberOfReplicates(2);
    libraryScreening.addAssayPlatesScreened(plate100);
    libraryScreening.addAssayPlatesScreened(plate101);
    libraryScreening.addAssayPlatesScreened(plate102);
    libraryScreening.addAssayPlatesScreened(plate103);

    List<PlateRange> plateRanges = PlateRange.splitIntoPlateCopyRanges(ImmutableSortedSet.of(plate100, plate101, plate103));
    assertEquals(2, plateRanges.size());
    assertEquals(ImmutableSortedSet.of(plate100, plate101), plateRanges.get(0).getPlates());
    assertEquals(ImmutableSortedSet.of(plate103), plateRanges.get(1).getPlates());
    
    plateRanges = PlateRange.splitIntoPlateCopyRanges(ImmutableSortedSet.of(plate100, plate101, plate102, plate103));
    assertEquals(3, plateRanges.size());
    assertEquals(ImmutableSortedSet.of(plate100, plate101), plateRanges.get(0).getPlates());
    assertEquals(ImmutableSortedSet.of(plate102), plateRanges.get(1).getPlates());
    assertEquals(ImmutableSortedSet.of(plate103), plateRanges.get(2).getPlates());

    plateRanges = PlateRange.splitIntoPlateCopyRanges(ImmutableSortedSet.<Plate>of());
    assertEquals(0, plateRanges.size());
  }
}
