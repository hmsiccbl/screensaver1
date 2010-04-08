// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.Comparator;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Compares AbstractEntity objects by their entity ID, rather of object identity
 * (which is the default in Screensaver).
 * 
 * @motivation Allows TreeSets and TreeMaps with AbstractEntity keys to work
 *             when provided key object has not been loaded from the same
 *             session in which the key object was added.
 */
public class AbstractEntityIdComparator<E extends AbstractEntity,T extends Comparable<T>>  implements Comparator<E>
{
  @SuppressWarnings("unchecked")
  public int compare(E e1, E e2)
  {
    T id1 = (T) e1.getEntityId();
    T id2 = (T) e2.getEntityId();
    assert id1 != null : "comparator can only be used with persistent entities";
    assert id2 != null : "comparator can only be used with persistent entities";
    return id1.compareTo(id2);
  }
}
