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
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryScreeningStatus;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;


public class PlateRange implements Iterable<Plate>
{
  private SortedSet<Plate> _plates;

  public static List<PlateRange> splitIntoPlateCopyRanges(SortedSet<Plate> plates)
  {
    PeekingIterator<Plate> iter = Iterators.peekingIterator(plates.iterator());
    List<PlateRange> plateRanges = Lists.newArrayList();;
    while (iter.hasNext()) {
      PlateRange plateRange = PlateRange.findNextPlateRange(iter, true);
      plateRanges.add(plateRange);
    }
    return plateRanges;
  }
  
  public static List<PlateRange> splitIntoPlateRanges(SortedSet<Plate> plates)
  {
    PeekingIterator<Plate> iter = Iterators.peekingIterator(plates.iterator());
    List<PlateRange> plateRanges = Lists.newArrayList();;
    while (iter.hasNext()) {
      PlateRange plateRange = PlateRange.findNextPlateRange(iter, false);
      plateRanges.add(plateRange);
    }
    return plateRanges;
  }
  
  private static PlateRange findNextPlateRange(PeekingIterator<Plate> iter, boolean splitOnCopy)
  {
    SortedSet<Plate> platesScreened = Sets.newTreeSet();
    platesScreened.add(iter.next());
    while (iter.hasNext()) {
      Plate next = iter.peek();
      Plate last = platesScreened.last();
      if (next.getPlateNumber() > last.getPlateNumber() + 1) {
        break;
      }
      else if (splitOnCopy && !next.getCopy().equals(last.getCopy())) {
        break;
      }
      platesScreened.add(iter.next());
    }
    return new PlateRange(platesScreened);
  }

  private PlateRange(SortedSet<Plate> platesScreened)
  {
    _plates = platesScreened;
  }
  
  public Plate getStartPlate()
  {
    return _plates.first();
  }
  
  public Plate getEndPlate()
  {
    return _plates.last();
  }
  
  public int getSize()
  {
    return _plates.size();
  }
  
  public SortedSet<Plate> getPlates()
  {
    return _plates;
  }

  public Iterator<Plate> iterator()
  {
    return _plates.iterator();
  }

  public Library getLibrary()
  {
    return _plates.first().getCopy().getLibrary();
  }

  public String getAdminLibraryWarning()
  {
    List<String> warnings = Lists.newArrayList();
    Library library = getLibrary();
    if (library.getScreeningStatus() != LibraryScreeningStatus.ALLOWED) {
      // TODO: do not show warning if Screen.getLibrariesPermitted() contains this library
      warnings.add("Library status is " + library.getScreeningStatus().getValue());
    }

    Predicate<Plate> plateStatusNotAvailable = new Predicate<Plate>() {
      @Override
      public boolean apply(Plate p)
      {
        return p.getStatus() != PlateStatus.AVAILABLE;
      }
    };
    Set<Plate> invalidPlates = Sets.newTreeSet(Iterables.filter(_plates, plateStatusNotAvailable));
    Set<PlateStatus> invalidStatuses = Sets.newTreeSet(Iterables.transform(invalidPlates, Plate.ToStatus));
    warnings.add("Plate(s) have invalid status(es): " + Joiner.on(", ").join(invalidStatuses));

    return Joiner.on(". ").join(warnings);
  }
  
  @Override
  public String toString()
  {
    return "[" + _plates.first().getPlateNumber() + ".." + _plates.last().getPlateNumber() + "]";
  }
}
