package edu.harvard.med.screensaver.ui.screenresults;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class CollationOrder implements Iterable<PlateOrderingGroup>
{
  private List<PlateOrderingGroup> _ordering;

  
  // a mapping to be used to easily input these orderings from a command line
	public static Map<String,CollationOrder> orderings = Maps.newHashMap();
	static {
		orderings.put("PQCR", new CollationOrder(ImmutableList.of(PlateOrderingGroup.Plates, 
				PlateOrderingGroup.Quadrants, PlateOrderingGroup.Conditions, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts)));
		orderings.put("PQRC", new CollationOrder(ImmutableList.of(PlateOrderingGroup.Plates, 
				PlateOrderingGroup.Quadrants, PlateOrderingGroup.Replicates, PlateOrderingGroup.Conditions, PlateOrderingGroup.Readouts)));
		orderings.put("CPQR", new CollationOrder(ImmutableList.of(PlateOrderingGroup.Conditions, 
				PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts)));
		orderings.put("CRPQ", new CollationOrder(ImmutableList.of(PlateOrderingGroup.Conditions, 
				PlateOrderingGroup.Replicates, PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Readouts)));
		orderings.put("RPQC", new CollationOrder(ImmutableList.of(PlateOrderingGroup.Replicates, 
				PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Conditions, PlateOrderingGroup.Readouts)));
		orderings.put("RCPQ", new CollationOrder(ImmutableList.of(PlateOrderingGroup.Replicates, 
				PlateOrderingGroup.Conditions, PlateOrderingGroup.Plates, PlateOrderingGroup.Quadrants, PlateOrderingGroup.Readouts)));
	}

	public static CollationOrder getOrder(String ordering)
	{
		return orderings.get(ordering.toUpperCase());
	}
  
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
  
  public String toShortString() {
  	String shortString = "";
  	for(PlateOrderingGroup p:this) {
  		shortString += p.toString().charAt(0);
  	}
  	return shortString;
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
