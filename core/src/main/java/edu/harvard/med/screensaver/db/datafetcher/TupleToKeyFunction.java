// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import com.google.common.base.Function;


public class TupleToKeyFunction<K> implements Function<Tuple<K>,K>
{
  @Override
  public K apply(Tuple<K> from)
  {
    return from.getKey();
  }
}
