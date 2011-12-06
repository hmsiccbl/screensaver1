package edu.harvard.med.screensaver.ui.screenresults;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;

public class CollationOrder implements Iterable<PlateOrderingGroup>
{
  private List<PlateOrderingGroup> _ordering;

  public CollationOrder(List<PlateOrderingGroup> ordering)
  {
    _ordering = ordering;
  }
  
  public List<PlateOrderingGroup> getOrdering() { return _ordering; } 

  @Override
  public String toString()
  {
    return Joiner.on(" \u2192 ").join(_ordering);
  }

  @Override
  public Iterator<PlateOrderingGroup> iterator()
  {
    return _ordering.iterator();
  }

  @Override
  public int hashCode()
  {
    return _ordering.hashCode();
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof CollationOrder) {
      return _ordering.equals(((CollationOrder) other)._ordering);
    }
    return false;
  }
}
