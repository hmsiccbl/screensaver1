// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;


public class PlateRange implements Iterable<AssayPlate>
{
  private SortedSet<AssayPlate> _assayPlates;

  public static List<PlateRange> splitIntoPlateCopyRanges(SortedSet<AssayPlate> assayPlates)
  {
    PeekingIterator<AssayPlate> iter = Iterators.peekingIterator(assayPlates.iterator());
    List<PlateRange> plateRanges = Lists.newArrayList();;
    while (iter.hasNext()) {
      PlateRange plateRange = PlateRange.findNextPlateRange(iter, true);
      plateRanges.add(plateRange);
    }
    return plateRanges;
  }
  
  public static List<PlateRange> splitIntoPlateRanges(SortedSet<AssayPlate> assayPlates)
  {
    PeekingIterator<AssayPlate> iter = Iterators.peekingIterator(assayPlates.iterator());
    List<PlateRange> plateRanges = Lists.newArrayList();;
    while (iter.hasNext()) {
      PlateRange plateRange = PlateRange.findNextPlateRange(iter, false);
      plateRanges.add(plateRange);
    }
    return plateRanges;
  }
  
  private static PlateRange findNextPlateRange(PeekingIterator<AssayPlate> iter, boolean splitOnCopy) 
  {
    SortedSet<AssayPlate> assayPlatesScreened = Sets.newTreeSet();
    assayPlatesScreened.add(iter.next());
    while (iter.hasNext()) {
      AssayPlate next = iter.peek();
      AssayPlate last = assayPlatesScreened.last();
      if (next.getPlateNumber() > last.getPlateNumber() + 1) {
        break;
      }
      else if (splitOnCopy && !next.getPlateScreened().getCopy().equals(last.getPlateScreened().getCopy())) {
        break;
      }
      assayPlatesScreened.add(iter.next());
    }
    return new PlateRange(assayPlatesScreened);
  }

  private PlateRange(SortedSet<AssayPlate> assayPlatesScreened)
  {
    _assayPlates = assayPlatesScreened;
  }
  
  public Plate getStartPlate()
  {
    return _assayPlates.first().getPlateScreened();
  }
  
  public Plate getEndPlate()
  {
    return _assayPlates.last().getPlateScreened();
  }
  
  public int getSize()
  {
    return _assayPlates.size();
  }
  
  public SortedSet<AssayPlate> getAssayPlates()
  {
    return _assayPlates;
  }

  public Iterator<AssayPlate> iterator()
  {
    return _assayPlates.iterator();
  }

  public Library getLibrary()
  {
    return _assayPlates.first().getPlateScreened().getCopy().getLibrary();
  }

  public String getAdminLibraryWarning()
  {
    Library library = getLibrary();
    if (library.getScreeningStatus() != LibraryScreeningStatus.ALLOWED) {
      // TODO: do not show warning if Screen.getLibrariesPermitted() contains this library
      return library.getScreeningStatus().getValue();
    }
    return null;
  }
  
  @Override
  public String toString()
  {
    return "[" + _assayPlates.first().getPlateNumber() + ".." + _assayPlates.last().getPlateNumber() + "]";
  }
}
