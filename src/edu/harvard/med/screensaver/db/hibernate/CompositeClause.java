// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

public abstract class CompositeClause extends Clause
{
  protected List<Clause> _clauses = new LinkedList<Clause>();

  public CompositeClause add(Clause clause)
  {
    _clauses.add(clause);
    return this;
  }

  protected LinkedHashSet<Clause> getUniqueOrderedClauses()
  {
    return new LinkedHashSet<Clause>(_clauses);
  }

  public int size()
  {
    return _clauses.size();
  }
}