// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.Iterator;

public class ParallelIterator<C1,C2> implements Iterator<Pair<C1,C2>>
{
  private Iterator<C1> _iterator1;
  private Iterator<C2> _iterator2;
  private C1 _next1;
  private C2 _next2;


  public ParallelIterator(Iterator<C1> c1,
                          Iterator<C2> c2)
  {
    _iterator1 = c1;
    _iterator2 = c2;
  }

  public boolean hasNext()
  {
    return _iterator1.hasNext() && _iterator2.hasNext();
  }

  public Pair<C1,C2> next()
  {
    _next1 = _iterator1.next();
    _next2 = _iterator2.next();
    return new Pair<C1,C2>(_next1, _next2);
  }

  public C1 getFirst()
  {
    return _next1;
  }

  public C2 getSecond()
  {
    return _next2;
  }

  public void remove()
  {
    _iterator1.remove();
    _iterator2.remove();
  }
}
