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
 * A 4-tuple.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Quadruple<F,S,T, Q>
{

  private F _first;
  private S _second;
  private T _third;
  private Q _fourth;
  
  /**
   * Construct a new <code>Quadruple</code> object.
   * @param first the first element of the quadruple
   * @param second the second element of the quadruple
   * @param third the third element of the quadruple
   * @param forth the forth element of the quadruple
   */
  public Quadruple(F first, S second, T third, Q fourth)
  {
    _first = first;
    _second = second;
    _third = third;
    _fourth = fourth;
  }

  /**
   * Get the first element of the quadruple.
   * @return the first element of the quadruple
   */
  public F getFirst()
  {
    return _first;
  }

  /**
   * Set the first element of the quadruple.
   * @param first the first element of the quadruple
   */
  public void setFirst(F first)
  {
    _first = first;
  }

  /**
   * Get the second element of the quadruple.
   * @return the second element of the quadruple
   */
  public S getSecond()
  {
    return _second;
  }

  /**
   * Set the second element of the quadruple.
   * @param second the second element of the quadruple
   */
  public void setSecond(S second)
  {
    _second = second;
  }

  /**
   * Set the third element of the quadruple.
   * @param third the third element of the quadruple
   */
  public void setThird(T third)
  {
    _third = third;
  }

  /**
   * Get the third element of the quadruple.
   * @return the third element of the quadruple
   */
  public T getThird()
  {
    return _third;
  }

  /**
   * Get the forth element of the quadruple.
   * @return the forth element of the quadruple
   */
  public Q getFourth()
  {
    return _fourth;
  }

  /**
   * Set the forth element of the quadruple
   * @param fourth the forth element of the quadruple
   */
  public void setFourth(Q fourth)
  {
    _fourth = fourth;
  }

  @Override
  public int hashCode()
  {
    final int PRIME1 = 2718;
    final int PRIME2 = 383;
    final int PRIME3 = 31;
    return
      PRIME1 * getFirst().hashCode() +
      PRIME2 * getSecond().hashCode() +
      PRIME3 * getThird().hashCode() +
      getFourth().hashCode();
  }

  @Override
  public boolean equals(Object object)
  {
    if (this == object) {
      return true;
    }
    if (! (object instanceof Quadruple)) {
      return false;
    }
    final Quadruple that = (Quadruple) object;
    return
      getFirst().equals(that.getFirst()) &&
      getSecond().equals(that.getSecond()) &&
      getThird().equals(that.getThird()) &&
      getFourth().equals(that.getFourth());
  }
}
