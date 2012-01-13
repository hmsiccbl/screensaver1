// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.util.Pair;

public class LibraryCopyPlateListParserResult
{
  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(LibraryCopyPlateListParserResult.class);

  private List<String> _errors = Lists.newArrayList();
  private Set<Integer> _plates = Sets.newTreeSet();
  private Set<Pair<Integer,Integer>> _plateRanges = Sets.newTreeSet(new Pair.PairComparator<Integer,Integer>());
  private Set<String> _copies = Sets.newTreeSet();

  public void addPlate(Integer plate)
  {
    _plates.add(plate);
  }
  
  public Set<Integer> getPlates()
  {
    return _plates;
  }

  public void addError(String error)
  {
    _errors.add(error);
  }
  
  public void addPlateRange(Pair<Integer,Integer> plateRange)
  {
    _plateRanges.add(plateRange);
  }

  public Set<Pair<Integer,Integer>> getPlateRanges()
  {
    return _plateRanges;
  }

  public void addCopy(String copy)
  {
    _copies.add(copy);
  }

  public Set<String> getCopies()
  {
    return _copies;
  }

  /**
   * Get all the numerical plates specified, as ranges, where single values are ranges with first=second.
   */
  public Set<Pair<Integer,Integer>> getCompletePlateRanges()
  {
    Set<Pair<Integer,Integer>> ranges = Sets.newHashSet(getPlateRanges());
    for (Integer plateNumber : getPlates()) {
      ranges.add(Pair.newPair(plateNumber, plateNumber));
    }
    return ranges;
  }

  /**
   * Return true if either syntax or fatal errors were found.
   * 
   * @return true if errors were found while parsing and/or looking up wells in
   *         the database.
   */
  public boolean hasErrors()
  {
    return _errors.size() > 0; 
  }    

  public List<String> getErrors()
  {
    return _errors;
  }
  
  public String print()
  {
    StringBuilder displayString = new StringBuilder();
    String sep = ""; 
    Joiner joiner = Joiner.on(", ");

    // TODO: collate and sort plates and ranges, interleaving as necessary

    if (!_plates.isEmpty()) {
      displayString.append(sep)
        .append("Plate" + (_plates.size() > 1 ? "s" : "") + ": ")
        .append(joiner.join(_plates));
      sep = ", ";
    }

    if (!_plateRanges.isEmpty()) {
      displayString.append(sep)
        .append("Plate range" + (_plateRanges.size() > 1 ? "s" : "") + ": ")
        .append(joiner.join(_plateRanges));
      sep = ", ";
    }

    if (!_copies.isEmpty()) {
      displayString.append(sep)
        .append("Cop" + (_copies.size() > 1 ? "ies" : "y") + ": ")
        .append(joiner.join(_copies));
      sep = ", ";
    }
    return displayString.toString();
  }

  public String toString()
  {
    return print();
  }
  
  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if(obj == null || ! (obj instanceof LibraryCopyPlateListParserResult) ) return false;
    return toString().equals(((LibraryCopyPlateListParserResult)obj).toString());
  }
}
