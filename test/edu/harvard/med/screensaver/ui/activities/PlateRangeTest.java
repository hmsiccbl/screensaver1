// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableSortedSet;
import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

public class PlateRangeTest extends TestCase
{
  public void testPlateRangeScreened()
  {
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(103);
    Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    Copy copyB = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    Plate plate100 = copyA.findPlate(100).withWellVolume(new Volume(0));
    Plate plate101 = copyA.findPlate(101).withWellVolume(new Volume(0));
    Plate plate102 = copyB.findPlate(102).withWellVolume(new Volume(0));
    Plate plate103 = copyA.findPlate(103).withWellVolume(new Volume(0));
    Screen screen = dataFactory.newInstance(Screen.class);
    LibraryScreening libraryScreening = dataFactory.newInstance(LibraryScreening.class, screen);
    libraryScreening.setNumberOfReplicates(2);
    libraryScreening.addAssayPlatesScreened(plate100);
    libraryScreening.addAssayPlatesScreened(plate101);
    libraryScreening.addAssayPlatesScreened(plate102);
    libraryScreening.addAssayPlatesScreened(plate103);
    Iterator<AssayPlate> iter = libraryScreening.getAssayPlatesScreened().iterator();
    AssayPlate assayPlate100r1 = iter.next();
    AssayPlate assayPlate100r2 = iter.next();
    AssayPlate assayPlate101r1 = iter.next();
    AssayPlate assayPlate101r2 = iter.next();
    AssayPlate assayPlate102r1 = iter.next();
    AssayPlate assayPlate102r2 = iter.next();
    AssayPlate assayPlate103r1 = iter.next();
    AssayPlate assayPlate103r2 = iter.next();

    List<PlateRange> plateRanges = PlateRange.splitIntoPlateRanges(ImmutableSortedSet.of(assayPlate100r1, assayPlate100r2, assayPlate101r1, assayPlate101r2, assayPlate103r1, assayPlate103r2));
    assertEquals(2, plateRanges.size());
    assertEquals(ImmutableSortedSet.of(assayPlate100r1, assayPlate101r1, assayPlate100r2, assayPlate101r2), plateRanges.get(0).getAssayPlates());
    assertEquals(ImmutableSortedSet.of(assayPlate103r1,assayPlate103r2), plateRanges.get(1).getAssayPlates());
    
    plateRanges = PlateRange.splitIntoPlateCopyRanges(ImmutableSortedSet.of(assayPlate100r1, assayPlate100r2, assayPlate101r1, assayPlate101r2, assayPlate103r1, assayPlate103r2));
    assertEquals(2, plateRanges.size());
    assertEquals(ImmutableSortedSet.of(assayPlate100r1, assayPlate101r1, assayPlate100r2, assayPlate101r2), plateRanges.get(0).getAssayPlates());
    assertEquals(ImmutableSortedSet.of(assayPlate103r1,assayPlate103r2), plateRanges.get(1).getAssayPlates());

    plateRanges = PlateRange.splitIntoPlateRanges(ImmutableSortedSet.of(assayPlate100r1, assayPlate101r1, assayPlate102r1, assayPlate103r1, assayPlate100r2, assayPlate101r2, assayPlate102r2, assayPlate103r2));
    assertEquals(1, plateRanges.size());
    assertEquals(ImmutableSortedSet.of(assayPlate100r1, assayPlate101r1, assayPlate102r1, assayPlate103r1, assayPlate100r2, assayPlate101r2, assayPlate102r2, assayPlate103r2), plateRanges.get(0).getAssayPlates());

    plateRanges = PlateRange.splitIntoPlateCopyRanges(ImmutableSortedSet.of(assayPlate100r1, assayPlate101r1, assayPlate102r1, assayPlate103r1, assayPlate100r2, assayPlate101r2, assayPlate102r2, assayPlate103r2));
    assertEquals(3, plateRanges.size());
    assertEquals(ImmutableSortedSet.of(assayPlate100r1, assayPlate101r1, assayPlate100r2, assayPlate101r2), plateRanges.get(0).getAssayPlates());
    assertEquals(ImmutableSortedSet.of(assayPlate102r1, assayPlate102r2), plateRanges.get(1).getAssayPlates());
    assertEquals(ImmutableSortedSet.of(assayPlate103r1, assayPlate103r2), plateRanges.get(2).getAssayPlates());

    plateRanges = PlateRange.splitIntoPlateRanges(ImmutableSortedSet.<AssayPlate>of());
    assertEquals(0, plateRanges.size());
  }

}
