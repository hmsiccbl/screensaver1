// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hibernate;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class CompositePredicate extends Predicate
{
  protected List<Predicate> _predicates = new LinkedList<Predicate>();

  public CompositePredicate add(Predicate predicate)
  {
    _predicates.add(predicate);
    return this;
  }

  protected LinkedHashSet<Predicate> getUniqueOrderedClauses()
  {
    return new LinkedHashSet<Predicate>(_predicates);
  }

  public int size()
  {
    return _predicates.size();
  }
}