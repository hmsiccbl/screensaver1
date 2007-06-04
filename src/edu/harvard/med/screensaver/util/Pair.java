// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

/**
 * A 2-tuple.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Pair<F,S>
{

  private F _first;
  private S _second;
  
  /**
   * Construct a new <code>Pair</code> object.
   * @param first the first element of the pair
   * @param second the second element of the pair
   */
  public Pair(F first, S second)
  {
    _first = first;
    _second = second;
  }

  /**
   * Get the first element of the pair.
   * @return the first element of the pair
   */
  public F getFirst()
  {
    return _first;
  }

  /**
   * Set the first element of the pair.
   * @param first the first element of the pair
   */
  public void setFirst(F first)
  {
    _first = first;
  }

  /**
   * Get the second element of the pair.
   * @return the second element of the pair
   */
  public S getSecond()
  {
    return _second;
  }

  /**
   * Set the second element of the pair.
   * @param second the second element of the pair
   */
  public void setSecond(S second)
  {
    _second = second;
  }

  @Override
  public int hashCode()
  {
    final int PRIME = 31;
    return PRIME * (_first == null ? 0 : _first.hashCode()) + (_second == null ? 0 : _second.hashCode()); 
  }

  @Override
  public boolean equals(Object object)
  {
    if (this == object) {
      return true;
    }
    if (! (object instanceof Pair)) {
      return false;
    }
    final Pair that = (Pair) object;
    return
    (_first == null ? that.getFirst() == null : _first.equals(that.getFirst())) &&
    (_second == null ? that.getSecond() == null : _second.equals(that.getSecond()));
  }
}
